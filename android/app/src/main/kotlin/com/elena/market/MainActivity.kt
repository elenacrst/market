package com.elena.market

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.core.view.postDelayed
import androidx.core.view.WindowCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import io.flutter.embedding.android.FlutterActivity

class MainActivity : FlutterActivity() {

    private var flutterUIReady: Boolean = false
    private var initialAnimationFinished: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This activity will be handling the splash screen transition.
        val splashScreen = installSplashScreen()

        // The splash screen goes edge to edge, so for a smooth transition to our app, also
        // want to draw edge to edge.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController?.isAppearanceLightNavigationBars = true
        insetsController?.isAppearanceLightStatusBars = true

        // Setting an OnExitAnimationListener on the splash screen indicates
        // to the system that the application will handle the exit animation.
        // The listener will be called once the app is ready.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
                onSplashScreenExit(splashScreenViewProvider)
            }
        }
    }

    override fun onFlutterUiDisplayed() {
        flutterUIReady = true

        if (initialAnimationFinished) {
            hideSplashScreenAnimation()
        }
    }

    override fun onFlutterUiNoLongerDisplayed() {
        flutterUIReady = false
    }

    /**
     * Hides the splash screen only when the entire animation has finished and the Flutter UI is ready to display.
     */
    private fun hideSplashScreenAnimation() {
        val splashView = findViewById<ImageView>(R.id.imageView)
        splashView
            .animate()
            .alpha(0.0f)
            .duration = SPLASHSCREEN_ALPHA_ANIMATION_DURATION

        val heightAnimation = ObjectAnimator.ofFloat(
            splashView,
            View.SCALE_Y,
            splashView.height.toFloat(),
            0f
        )
        heightAnimation.duration = 10000
        heightAnimation.interpolator = FastOutLinearInInterpolator()
        // And play all of the animation together.
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(heightAnimation)
    }

    /**
     * Handles the transition from the splash screen to the application.
     */
    private fun onSplashScreenExit(splashScreenViewProvider: SplashScreenViewProvider) {
        val accelerateInterpolator = FastOutLinearInInterpolator()
        val splashScreenView = splashScreenViewProvider.view
        val iconView = splashScreenViewProvider.iconView

        // Change the alpha of the main view.
        val alpha = ValueAnimator.ofInt(255, 0)
        alpha.duration = SPLASHSCREEN_ALPHA_ANIMATION_DURATION
        alpha.interpolator = accelerateInterpolator

        val scaleY = ObjectAnimator.ofFloat(
            iconView,
            View.SCALE_Y,
            iconView.scaleY,
            0.0f
        )
        scaleY.duration = SPLASHSCREEN_ALPHA_ANIMATION_DURATION
        scaleY.interpolator = accelerateInterpolator

        val scaleX = ObjectAnimator.ofFloat(
            iconView,
            View.SCALE_X,
            iconView.scaleX,
            0.0f
        )
        scaleX.duration = SPLASHSCREEN_ALPHA_ANIMATION_DURATION
        scaleX.interpolator = accelerateInterpolator

        // And play all of the animation together.
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(alpha, scaleY, scaleX)

        animatorSet.doOnEnd {
            splashScreenViewProvider.remove()
        }

        waitForAnimatedIconToFinish(splashScreenViewProvider, splashScreenView) {
            animatorSet.start()
        }
    }

    /**
     * Wait until the AVD animation is finished before starting the splash screen dismiss animation.
     */
    private fun SplashScreenViewProvider.remainingAnimationDuration() = iconAnimationStartMillis +
            iconAnimationDurationMillis - System.currentTimeMillis()

    private fun waitForAnimatedIconToFinish(
        splashScreenViewProvider: SplashScreenViewProvider,
        view: View,
        onAnimationFinished: () -> Unit
    ) {
        // If wanting to wait for our Animated Vector Drawable to finish animating, can compute
        // the remaining time to delay the start of the exit animation.
        val delayMillis: Long =
            if (WAIT_FOR_AVD_TO_FINISH) splashScreenViewProvider.remainingAnimationDuration() else 0
        view.postDelayed(delayMillis, onAnimationFinished)
    }

    private companion object {
        const val SPLASHSCREEN_ALPHA_ANIMATION_DURATION = 500L
        const val WAIT_FOR_AVD_TO_FINISH = false
    }
}
