package com.hellowo.colosseum.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.support.transition.Explode
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator

fun makeExplodeTransition(): Explode {
    val explode = Explode()
    explode.duration = 500
    explode.interpolator = AnticipateOvershootInterpolator()
    return explode
}

fun setScale(view: View, scale: Float) {
    val animSet = AnimatorSet()
    animSet.playTogether(
            ObjectAnimator.ofFloat(view, "scaleX", view.scaleX, scale).setDuration(250),
            ObjectAnimator.ofFloat(view, "scaleY", view.scaleY, scale).setDuration(250))
    animSet.interpolator = FastOutSlowInInterpolator()
    animSet.start()
}