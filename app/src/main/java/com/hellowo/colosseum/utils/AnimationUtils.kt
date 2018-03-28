package com.hellowo.colosseum.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.support.transition.Explode
import android.support.transition.Slide
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnticipateOvershootInterpolator

fun makeSlideFromBottomTransition(): Slide {
    val transition = Slide()
    transition.slideEdge = Gravity.BOTTOM
    transition.duration = 500
    transition.interpolator = FastOutSlowInInterpolator()
    return transition
}

fun fadeIn(view: View) {
    val animSet = AnimatorSet()
    animSet.playTogether(
            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).setDuration(1000)
    )
    animSet.interpolator = FastOutSlowInInterpolator()
    animSet.start()
}

fun fadeOut(view: View, adapter: AnimatorListenerAdapter) {
    val animSet = AnimatorSet()
    animSet.playTogether(
            ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).setDuration(1000)
    )
    animSet.interpolator = FastOutSlowInInterpolator()
    animSet.addListener(adapter)
    animSet.start()
}

fun slideFromBottom(view: View, offset: Float) {
    val animSet = AnimatorSet()
    animSet.playTogether(
            ObjectAnimator.ofFloat(view, "translationY", offset, 0f).setDuration(250),
            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).setDuration(250)
    )
    animSet.interpolator = FastOutSlowInInterpolator()
    animSet.start()
}

fun slideToBottom(view: View, offset: Float) {
    val animSet = AnimatorSet()
    animSet.playTogether(
            ObjectAnimator.ofFloat(view, "translationY", 0f, offset).setDuration(250),
            ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).setDuration(250)
    )
    animSet.interpolator = FastOutSlowInInterpolator()
    animSet.start()
}

fun slideToTop(view: View, offset: Float) {
    val animSet = AnimatorSet()
    animSet.playTogether(
            ObjectAnimator.ofFloat(view, "translationY", 0f, -offset).setDuration(250),
            ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).setDuration(250)
    )
    animSet.interpolator = FastOutSlowInInterpolator()
    animSet.start()
}

fun slideFromTop(view: View, offset: Float) {
    val animSet = AnimatorSet()
    animSet.playTogether(
            ObjectAnimator.ofFloat(view, "translationY", -offset, 0f).setDuration(250),
            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).setDuration(250)
    )
    animSet.interpolator = FastOutSlowInInterpolator()
    animSet.start()
}
