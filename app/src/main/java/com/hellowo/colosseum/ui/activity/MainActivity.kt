package com.hellowo.colosseum.ui.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.ImageButton
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.ui.fragment.HomeFragment
import com.hellowo.colosseum.ui.fragment.ProfileFragment
import com.hellowo.colosseum.viewmodel.MainViewModel
import com.hellowo.teamfinder.ui.fragment.IssueListFragment
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    private lateinit var viewModel: MainViewModel
    private val tabs = ArrayList<ImageButton>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        setContentView(R.layout.activity_main)
        initLayout()
        initObserve()
    }

    private fun initLayout() {
        tabs.add(friendsTab)
        tabs.add(chatTab)
        tabs.add(homeTab)
        tabs.add(historyTab)
        tabs.add(profileTab)
        friendsTab.setOnClickListener{ clickTab(it as ImageButton?) }
        chatTab.setOnClickListener{ clickTab(it as ImageButton?) }
        homeTab.setOnClickListener{ clickTab(it as ImageButton?) }
        historyTab.setOnClickListener{ clickTab(it as ImageButton?) }
        profileTab.setOnClickListener{ clickTab(it as ImageButton?) }
        clickTab(friendsTab)
    }

    private fun clickTab(item: ImageButton?) {
        tabs.forEach {
            if(item == it) {
                it.setColorFilter(resources.getColor(R.color.colorPrimary))
            }else {
                it.setColorFilter(resources.getColor(R.color.disableText))
            }
        }

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container,
                when (item) {
                    friendsTab -> ProfileFragment()
                    chatTab -> IssueListFragment()
                    homeTab -> HomeFragment()
                    historyTab -> ProfileFragment()
                    profileTab -> ProfileFragment()
                    else -> return
                })
        fragmentTransaction.commit()
    }

    private fun initObserve() {
        Me.observe(this, Observer { it?.let { updateProfileUI(it) } })
    }

    private fun updateProfileUI(user: User) {
        Glide.with(this).load(user.photoUrl).bitmapTransform(CropCircleTransformation(this)).into(profileTab)
    }
}
