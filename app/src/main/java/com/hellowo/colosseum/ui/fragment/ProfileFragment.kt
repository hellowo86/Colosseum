package com.hellowo.colosseum.ui.fragment

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
import com.hellowo.colosseum.ui.activity.NotiSettingActivity
import com.hellowo.colosseum.ui.activity.SettingActivity
import com.hellowo.colosseum.ui.activity.SplashActivity
import com.hellowo.colosseum.utils.getUserDeviceInfoText
import com.hellowo.colosseum.utils.log
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import io.realm.Realm
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment(), BillingProcessor.IBillingHandler {
    var bp: BillingProcessor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bp = BillingProcessor(context, inAppKey, this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notiBtn.setOnClickListener {
            activity?.startActivity(Intent(activity!!, NotiSettingActivity::class.java))
        }

        settingBtn.setOnClickListener {
            activity?.startActivity(Intent(activity!!, SettingActivity::class.java))
        }

        inviteBtn.setOnClickListener {
            bp?.purchase(activity!!, "coin_10")
        }

        evaluationBtn.setOnClickListener {
            val uri = Uri.parse("market://details?id=com.hellowo.colosseum")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            try {
                activity?.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                activity?.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=com.hellowo.colosseum")))
            }
        }

        contactBtn.setOnClickListener {
            val sendIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "dayfly86@gmail.com", null))
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "")
            sendIntent.putExtra(Intent.EXTRA_TEXT, getUserDeviceInfoText(context!!))
            activity?.startActivity(sendIntent)
        }

        logoutBtn.setOnClickListener {
            val builder = AlertDialog.Builder(activity!!)
            builder.setTitle(R.string.logout)
            builder.setCancelable(true)
            builder.setMessage(R.string.logout_sub)
            builder.setPositiveButton(R.string.ok) { _,_ ->
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(activity, SplashActivity::class.java))
                activity?.finish()
                Realm.getDefaultInstance().executeTransaction {
                    it.deleteAll()
                }
            }
            builder.setNegativeButton(R.string.cancel, null)
            builder.show()
        }

        Me.observe(this, Observer { updateUserUI(it) })
    }

    private fun updateUserUI(user: User?) {
        user?.let {
            Glide.with(this).load(makePublicPhotoUrl(it.id)).placeholder(R.drawable.img_default_profile)
                    .bitmapTransform(CropCircleTransformation(context)).into(profileImage)
            nameText.text = it.nickName
            profileSettingText.text = context?.getString(R.string.profile_setting_sub)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!bp?.handleActivityResult(requestCode, resultCode, data)!!) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onBillingInitialized() {

    }

    override fun onPurchaseHistoryRestored() {}

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {}

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        log(error.toString())
    }
}
