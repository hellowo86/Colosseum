package com.hellowo.colosseum.ui.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ImageButton
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Issue
import com.hellowo.colosseum.model.Thread
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.ui.adapter.ThreadListAdapter
import com.hellowo.colosseum.ui.dialog.EnterCommentDialog
import com.hellowo.colosseum.ui.fragment.ProfileFragment
import com.hellowo.colosseum.utils.log
import com.hellowo.colosseum.viewmodel.MainViewModel
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        setContentView(R.layout.activity_main)
        initLayout()
        initObserve()
    }

    private fun initLayout() {
        friendsTab.setOnClickListener{ clickTab(it as ImageButton?) }
        chatTab.setOnClickListener{ clickTab(it as ImageButton?) }
        findTab.setOnClickListener{ clickTab(it as ImageButton?) }
        historyTab.setOnClickListener{ clickTab(it as ImageButton?) }
        profileTab.setOnClickListener{ clickTab(it as ImageButton?) }
        clickTab(friendsTab)
    }

    private fun clickTab(item: ImageButton?) {
        friendsTab.setColorFilter(resources.getColor(R.color.disableText))
        chatTab.setColorFilter(resources.getColor(R.color.disableText))
        findTab.setColorFilter(resources.getColor(R.color.disableText))
        historyTab.setColorFilter(resources.getColor(R.color.disableText))
        if(item?.id != profileTab.id) {
            item?.setColorFilter(resources.getColor(R.color.grey))
        }

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container,
                when (item) {
                    friendsTab -> ProfileFragment()
                    chatTab -> ProfileFragment()
                    findTab -> ProfileFragment()
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
