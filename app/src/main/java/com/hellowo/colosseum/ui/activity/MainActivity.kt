package com.hellowo.colosseum.ui.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.ChatTabBadge
import com.hellowo.colosseum.data.ChemistyTabBadge
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.data.MyChatList
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.ui.fragment.*
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import com.hellowo.colosseum.utils.startChatingActivity
import com.hellowo.colosseum.viewmodel.MainViewModel
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    companion object {
        var isCreated = false
        var instance : MainActivity? = null
    }

    lateinit var viewModel: MainViewModel
    private var clickedTab: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        initLayout()
        initObserve()
        isCreated = true
        instance = this
    }

    private fun initLayout() {
        homeTab.setOnClickListener{ clickTab(homeTabImg) }
        matchingTab.setOnClickListener{ clickTab(matchingTabImg) }
        chatTab.setOnClickListener{ clickTab(chatTabImg) }
        communityTab.setOnClickListener{ clickTab(communityTabImg) }
        profileTab.setOnClickListener{ clickTab(profileImg) }
        clickTab(homeTabImg)
    }

    fun clickTab(item: ImageView) {
        if(item != clickedTab) {
            if(clickedTab != profileImg) clickedTab?.setColorFilter(resources.getColor(R.color.iconTint))
            if(item != profileImg) item.setColorFilter(resources.getColor(R.color.colorPrimary))
            clickedTab = item
        }

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container,
                when (item) {
                    homeTabImg -> InterestFragment()
                    matchingTabImg -> ChemistryListFragment()
                    chatTabImg -> MyChatListFragment()
                    communityTabImg -> IssueFragment()
                    profileImg -> ProfileFragment()
                    else -> return
                })
        fragmentTransaction.commit()
    }

    private fun initObserve() {
        try{
            Me.observe(this, Observer { updateUserUI(it) })
            ChemistyTabBadge.observe(this, Observer {
                if(it as Int > 0) {
                    chemistryBadge.visibility = View.VISIBLE
                    chemistryBadgeText.text = it.toString()
                }else {
                    chemistryBadge.visibility = View.GONE
                }
            })
            ChatTabBadge.observe(this, Observer {
                if(it as Int > 0) {
                    chatBadge.visibility = View.VISIBLE
                    chatBadgeText.text = it.toString()
                }else {
                    chatBadge.visibility = View.GONE
                }
            })
        }catch (e: Exception){}
    }

    private fun checkIntentExtra() {
        intent.extras?.let { extra ->
            when {
                !extra.getString("chatId").isNullOrEmpty() -> {
                    startChatingActivity(this, extra.getString("chatId"))
                    intent.removeExtra("chatId")
                }
                extra.getBoolean("goChemistryTab", false) -> {
                    goChemistryTab()
                    intent.removeExtra("goChemistryTab")
                }
            }
        }
    }

    fun goChemistryTab() {
        clickTab(matchingTabImg)
    }

    private fun updateUserUI(user: User?) {
        user?.let {
            Glide.with(this).load(makePublicPhotoUrl(user.id)).placeholder(R.drawable.default_profile)
                    .bitmapTransform(CropCircleTransformation(this)).into(profileImg)
        }
    }

    override fun onResume() {
        super.onResume()
        checkIntentExtra()
    }

    override fun onDestroy() {
        super.onDestroy()
        Me.updateLastDtConnected()
        isCreated = false
        instance = null
    }
}
