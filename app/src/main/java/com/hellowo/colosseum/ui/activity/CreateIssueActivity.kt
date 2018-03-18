package com.hellowo.colosseum.ui.activity

import android.animation.LayoutTransition
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import com.hellowo.colosseum.R
import com.hellowo.colosseum.viewmodel.BasicViewModel
import kotlinx.android.synthetic.main.activity_create_issue.*

class CreateIssueActivity : BaseActivity() {
    private lateinit var viewModel: BasicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(BasicViewModel::class.java)
        setContentView(R.layout.activity_create_issue)
        initLayout()
        initObserve()
    }

    private fun initLayout() {
        isuueTypeLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        posiAndNegBtn.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                it.elevation = it.elevation + 10
            }
        }

        nextBtn.setOnClickListener { goNext() }
        backBtn.setOnClickListener{ onBackPressed() }
    }

    private fun initObserve() {
    }

    private fun goNext(): Boolean {
        viewFlipper.inAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_right)
        viewFlipper.outAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_out_left)
        viewFlipper.displayedChild = viewFlipper.displayedChild + 1
        return true
    }

    override fun onBackPressed() {
        if(viewFlipper.currentView == isuueTypeLy) {
            finish()
        }else {
            viewFlipper.inAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_left)
            viewFlipper.outAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_out_right)
            viewFlipper.displayedChild = viewFlipper.displayedChild - 1
        }
    }
}