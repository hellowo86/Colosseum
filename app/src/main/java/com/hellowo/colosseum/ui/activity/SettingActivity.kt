package com.hellowo.colosseum.ui.activity

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import com.hellowo.colosseum.R
import com.hellowo.colosseum.viewmodel.BasicViewModel
import kotlinx.android.synthetic.main.activity_setting.*
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import io.realm.Realm


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

        notiBtn.setOnClickListener {
            startActivity(Intent(this, NotiSettingActivity::class.java))
        }

        try {
            val version = packageManager.getPackageInfo(packageName, 0).versionName
            versionText.text = version
        }catch(e: Exception){}

        privacyBtn.setOnClickListener {
            val url = "https://colosseum-eb02c.firebaseapp.com/privacy.html"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        logoutBtn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.logout)
            builder.setCancelable(true)
            builder.setMessage(R.string.logout_sub)
            builder.setPositiveButton(R.string.ok) { _,_ ->
                Realm.getDefaultInstance().executeTransaction {
                    it.deleteAll()
                }
                val finishIntent = Intent(this, SplashActivity::class.java)
                finishIntent.putExtra("logout", true)
                startActivity(finishIntent)
                finish()
                MainActivity.instance?.finish()
            }
            builder.setNegativeButton(R.string.cancel, null)
            builder.show()
        }
    }

    private fun initObserve() {
    }
}