package com.hellowo.colosseum.ui.fragment

import android.app.Activity.RESULT_OK
import android.arch.lifecycle.Observer
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.inAppKey
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.requestCodeFinish
import com.hellowo.colosseum.ui.activity.CoinActivity
import com.hellowo.colosseum.ui.activity.NotiSettingActivity
import com.hellowo.colosseum.ui.activity.SettingActivity
import com.hellowo.colosseum.ui.activity.SplashActivity
import com.hellowo.colosseum.utils.getUserDeviceInfoText
import com.hellowo.colosseum.utils.log
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import io.realm.Realm
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingBtn.setOnClickListener { startActivity(Intent(activity, SettingActivity::class.java)) }
        coinBtn.setOnClickListener { startActivity(Intent(activity, CoinActivity::class.java)) }
        coin2Btn.setOnClickListener { startActivity(Intent(activity, CoinActivity::class.java)) }

        evaluationBtn.setOnClickListener {
            val uri = Uri.parse("market://details?id=com.hellowo.colosseum")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=com.hellowo.colosseum")))
            }
        }

        contactBtn.setOnClickListener {
            val sendIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "dayfly86@gmail.com", null))
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "")
            sendIntent.putExtra(Intent.EXTRA_TEXT, getUserDeviceInfoText(context!!))
            startActivity(sendIntent)
        }

        Me.observe(this, Observer { updateUserUI(it) })
    }

    private fun updateUserUI(user: User?) {
        user?.let {
            Glide.with(this).load(makePublicPhotoUrl(it.id)).placeholder(R.drawable.img_default_profile)
                    .bitmapTransform(CropCircleTransformation(context)).into(profileImage)
            nameText.text = it.nickName
            profileSettingText.text = context?.getString(R.string.profile_setting_sub)
            coinText.text = it.coin.toString()
        }
    }
}
