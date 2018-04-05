package com.hellowo.colosseum.ui.activity

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import com.hellowo.colosseum.R
import com.hellowo.colosseum.viewmodel.BasicViewModel
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : BaseActivity() {
    private lateinit var viewModel: BasicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(BasicViewModel::class.java)
        setContentView(R.layout.activity_setting)
        initLayout()
        initObserve()
    }

    private fun initLayout() {
        backBtn.setOnClickListener { finish() }
    }

    private fun initObserve() {
    }
}