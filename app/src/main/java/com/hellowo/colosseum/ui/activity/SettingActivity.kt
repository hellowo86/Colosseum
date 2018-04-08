package com.hellowo.colosseum.ui.activity

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import com.hellowo.colosseum.R
import com.hellowo.colosseum.viewmodel.BasicViewModel
import kotlinx.android.synthetic.main.activity_setting.*
import android.content.Intent
import android.net.Uri


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
        privacyBtn.setOnClickListener {
            val url = "https://colosseum-eb02c.firebaseapp.com/privacy.html"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
    }

    private fun initObserve() {
    }
}