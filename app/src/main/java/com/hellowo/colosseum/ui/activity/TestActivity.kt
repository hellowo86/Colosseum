package com.hellowo.colosseum.ui.activity

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.transition.*
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.BounceInterpolator
import android.widget.Button
import android.widget.ImageView
import com.hellowo.colosseum.R
import kotlinx.android.synthetic.main.activity_test.*


class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        // Set a click listener for button widget
        btn_auto.setOnClickListener{
            TransitionManager.beginDelayedTransition(coordinator_layout, makeAutoTransition())
            toggleVisibility(iv, iv_second)
        }
        btn_fade.setOnClickListener{
            TransitionManager.beginDelayedTransition(coordinator_layout, makeFadeTransition())
            toggleVisibility(iv, iv_second)
        }
        btn_slide.setOnClickListener{
            TransitionManager.beginDelayedTransition(coordinator_layout, makeSlideTransition())
            toggleVisibility(iv, iv_second)
        }
        btn_explode.setOnClickListener{
            TransitionManager.beginDelayedTransition(coordinator_layout, makeExplodeTransition())
            toggleVisibility(iv, iv_second)
        }
    }

    private fun makeFadeTransition(): Fade {
        val fade = Fade()
        fade.setDuration(250)
        fade.setInterpolator(AccelerateInterpolator())
        return fade
    }

    private fun makeSlideTransition(): Slide {
        val slide = Slide()
        slide.setSlideEdge(Gravity.BOTTOM)
        slide.setInterpolator(BounceInterpolator())
        slide.setDuration(250)
        return slide
    }

    private fun makeExplodeTransition(): Explode {
        val explode = Explode()
        explode.setDuration(500)
        explode.setInterpolator(AnticipateOvershootInterpolator())
        return explode
    }

    private fun makeAutoTransition(): AutoTransition {
        val autoTransition = AutoTransition()
        autoTransition.setDuration(250)
        autoTransition.setInterpolator(BounceInterpolator())
        return autoTransition
    }

    // Custom method to toggle visibility of views
    private fun toggleVisibility(vararg views: View) {
        // Loop through the views
        for (v in views) {
            if (v.getVisibility() === View.VISIBLE) {
                v.setVisibility(View.INVISIBLE)
            } else {
                v.setVisibility(View.VISIBLE)
            }
        }
    }
}