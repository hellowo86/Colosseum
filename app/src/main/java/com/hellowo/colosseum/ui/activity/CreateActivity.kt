package com.hellowo.colosseum.ui.activity

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.hellowo.colosseum.R
import com.hellowo.colosseum.viewmodel.CreateViewModel

class CreateActivity : AppCompatActivity() {
    private lateinit var viewModel: CreateViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CreateViewModel::class.java)
        setContentView(R.layout.activity_create)
        initLayout()
        initObserve()
    }

    private fun initLayout() {

    }

    private fun initObserve() {
    }
}