package com.hellowo.colosseum.utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.support.transition.Explode
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnticipateOvershootInterpolator

fun makeExplodeTransition(): Explode {
    val explode = Explode()
    explode.duration = 500
    explode.interpolator = AnticipateOvershootInterpolator()
    return explode
}