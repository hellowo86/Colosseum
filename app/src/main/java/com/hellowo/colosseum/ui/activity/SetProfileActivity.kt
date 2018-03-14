package com.hellowo.colosseum.ui.activity

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.hellowo.colosseum.R
import com.hellowo.colosseum.utils.makeExplodeTransition
import com.hellowo.colosseum.viewmodel.SetProfileViewModel
import kotlinx.android.synthetic.main.activity_set_profile.*

class SetProfileActivity : AppCompatActivity() {
    private lateinit var viewModel: SetProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SetProfileViewModel::class.java)
        setContentView(R.layout.activity_set_profile)
        initLayout()
        initObserve()
    }

    private fun initLayout() {
        topLy.setOnClickListener {
            TransitionManager.beginDelayedTransition(rootLy, makeExplodeTransition())
            toggleVisibility(subTitleText)
            titleText.text = System.currentTimeMillis().toString()
        }
    }

    private fun initObserve() {

    }

    // Custom method to toggle visibility of views
    private fun toggleVisibility(vararg views: View) {
        // Loop through the views
        for (v in views) {
            if (v.visibility == View.VISIBLE) {
                v.visibility = View.INVISIBLE
            } else {
                v.visibility = View.VISIBLE
            }
        }
    }
}