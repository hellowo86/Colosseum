package com.hellowo.colosseum.ui.activity

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.hellowo.colosseum.R
import com.hellowo.colosseum.viewmodel.BasicViewModel

class BasicActivity : BaseActivity() {
    private lateinit var viewModel: BasicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(BasicViewModel::class.java)
        setContentView(R.layout.activity_basic)
        initLayout()
        initObserve()
    }

    private fun initLayout() {

    }

    private fun initObserve() {
    }
}