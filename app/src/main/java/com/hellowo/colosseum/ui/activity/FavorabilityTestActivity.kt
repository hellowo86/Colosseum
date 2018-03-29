package com.hellowo.colosseum.ui.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.transition.TransitionManager
import android.view.View
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.model.Couple
import com.hellowo.colosseum.utils.dpToPx
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import com.hellowo.colosseum.utils.makeSlideFromBottomTransition
import com.hellowo.colosseum.viewmodel.FavobalityTestViewModel
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_favorabiliy_test.*

class FavorabilityTestActivity : BaseActivity() {
    private lateinit var viewModel: FavobalityTestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FavobalityTestViewModel::class.java)
        viewModel.initCouple(intent.getSerializableExtra("couple") as Couple)
        setContentView(R.layout.activity_favorabiliy_test)
        initLayout()
        initObserve()
        viewModel.loadCouple()
    }

    private fun initLayout() {
        backBtn.setOnClickListener { finish() }
        swipeRefreshView.setColorSchemeColors(resources.getColor(R.color.colorPrimary))
        swipeRefreshView.setOnRefreshListener {  }
        swipeRefreshView.setProgressViewOffset(true, dpToPx(this, 100f), dpToPx(this, 200f))
    }

    private fun initObserve() {
        viewModel.couple.observe(this, Observer { it?.let { updateUI(it) } })
        viewModel.loading.observe(this, Observer { swipeRefreshView.isRefreshing = it as Boolean })
    }

    private fun updateUI(couple: Couple) {
        titleText.text = String.format(getString(R.string.couple_test_item_title), couple.you?.nickName)
        Glide.with(this).load(makePublicPhotoUrl(couple.you?.id))
                .bitmapTransform(CropCircleTransformation(this))
                .placeholder(couple.you?.getDefaultImgId()!!).into(profileImg)

        TransitionManager.beginDelayedTransition(rootLy, makeSlideFromBottomTransition())
        when(couple.step) {
            0 -> {
                step1Ly.visibility = View.VISIBLE
                setPhotoView(couple)
            }
        }
    }

    private fun setPhotoView(couple: Couple) {
        val myPhotoUrl = couple.getPhotoUrlByGender(couple.me?.gender!!)
        val yourPhotoUrl = couple.getPhotoUrlByGender(couple.you?.gender!!)
        val myPhotoLike = couple.getPhotoLikeByGender(couple.me?.gender!!)
        val yourPhotoLike = couple.getPhotoLikeByGender(couple.you?.gender!!)

        if(myPhotoUrl.isNullOrEmpty()) {

        }else {
            if(myPhotoLike != -1) {

            }else {

            }
        }

        if(yourPhotoUrl.isNullOrEmpty()) {

        }else {
            if(yourPhotoLike != -1) {

            }else {

            }
        }
    }
}