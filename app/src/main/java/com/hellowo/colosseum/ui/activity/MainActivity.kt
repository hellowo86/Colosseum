package com.hellowo.colosseum.ui.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.ui.fragment.ChoiceFragment
import com.hellowo.colosseum.ui.fragment.ProfileFragment
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import com.hellowo.colosseum.viewmodel.MainViewModel
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    lateinit var viewModel: MainViewModel
    private var clickedTab: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        initLayout()
        initObserve()
        checkIntentExtra()
    }

    private fun initLayout() {
        communityTab.setOnLongClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
            return@setOnLongClickListener false
        }
        homeTab.setOnClickListener{ clickTab(homeTabImg) }
        matchingTab.setOnClickListener{ clickTab(matchingTabImg) }
        chatTab.setOnClickListener{ clickTab(chatTabImg) }
        communityTab.setOnClickListener{ clickTab(communityTabImg) }
        profileTab.setOnClickListener{ clickTab(profileImg) }
        clickTab(homeTabImg)
    }

    private fun clickTab(item: ImageView) {
        if(item != clickedTab) {
            if(clickedTab != profileImg) clickedTab?.setColorFilter(resources.getColor(R.color.disableText))
            if(item != profileImg) item.setColorFilter(resources.getColor(R.color.colorPrimary))
            clickedTab = item
        }

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container,
                when (item) {
                    homeTabImg -> ChoiceFragment()
                    matchingTabImg -> ChoiceFragment()
                    chatTabImg -> ChoiceFragment()
                    communityTabImg -> ChoiceFragment()
                    profileImg -> ProfileFragment()
                    else -> return
                })
        fragmentTransaction.commit()
    }

    private fun initObserve() {
        Me.observe(this, Observer { updateUserUI(it) })
    }

    private fun checkIntentExtra() {
        intent.extras?.let {
        }
    }

    private fun updateUserUI(user: User?) {
        user?.let {
            Glide.with(this).load(makePublicPhotoUrl(user.id)).placeholder(R.drawable.default_profile)
                    .bitmapTransform(CropCircleTransformation(this)).into(profileImg)
        }
    }
}
