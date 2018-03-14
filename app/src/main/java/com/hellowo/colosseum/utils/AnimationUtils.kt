package com.hellowo.colosseum.utils

import android.support.transition.Explode
import android.view.animation.AnticipateOvershootInterpolator

fun makeExplodeTransition(): Explode {
    val explode = Explode()
    explode.duration = 500
    explode.interpolator = AnticipateOvershootInterpolator()
    return explode
}