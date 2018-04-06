package com.hellowo.colosseum.ui.activity

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import com.hellowo.colosseum.R
import com.hellowo.colosseum.viewmodel.BasicViewModel
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_noti_setting.*

class NotiSettingActivity : BaseActivity() {
    private lateinit var viewModel: BasicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(BasicViewModel::class.java)
        setContentView(R.layout.activity_noti_setting)
        initLayout()
    }

    private fun initLayout() {
        backBtn.setOnClickListener { finish() }

        chatNotiSwitch.isChecked = Prefs.getBoolean("chatNotiSwitch", true)
        chatNotiSwitch.setOnCheckedChangeListener { compoundButton, checked ->
            Prefs.putBoolean("chatNotiSwitch", checked)
        }

        interestNotiSwitch.isChecked = Prefs.getBoolean("interestNotiSwitch", true)
        interestNotiSwitch.setOnCheckedChangeListener { compoundButton, checked ->
            Prefs.putBoolean("interestNotiSwitch", checked)
        }

        chemistryNotiSwitch.isChecked = Prefs.getBoolean("chemistryNotiSwitch", true)
        chemistryNotiSwitch.setOnCheckedChangeListener { compoundButton, checked ->
            Prefs.putBoolean("chemistryNotiSwitch", checked)
        }

        likeNotiSwitch.isChecked = Prefs.getBoolean("likeNotiSwitch", true)
        likeNotiSwitch.setOnCheckedChangeListener { compoundButton, checked ->
            Prefs.putBoolean("likeNotiSwitch", checked)
        }
    }
}