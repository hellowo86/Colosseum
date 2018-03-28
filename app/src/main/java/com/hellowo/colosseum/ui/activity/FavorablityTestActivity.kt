package com.hellowo.colosseum.ui.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.model.Couple
import com.hellowo.colosseum.utils.dpToPx
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import com.hellowo.colosseum.viewmodel.FavobalityTestViewModel
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_favorabiliy_test.*

class FavorablityTestActivity : BaseActivity() {
    private lateinit var viewModel: FavobalityTestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FavobalityTestViewModel::class.java)
        setContentView(R.layout.activity_favorabiliy_test)
        initLayout()
        initObserve()
        viewModel.initCouple(intent.getSerializableExtra("couple") as Couple)
    }

    private fun initLayout() {
        backBtn.setOnClickListener { finish() }
        swipeRefreshView.setColorSchemeColors(resources.getColor(R.color.colorPrimary))
        swipeRefreshView.setOnRefreshListener {  }
        swipeRefreshView.setProgressViewOffset(true, dpToPx(this, 100f), dpToPx(this, 200f))
        step2Ly.setOnClickListener {
            val intent = Intent(this, VoiceActivity::class.java)
            intent.putExtra("couple", viewModel.couple.value)
            startActivityForResult(intent, 1)
        }
        startChatBtn.setOnClickListener { viewModel.createChat() }
    }

    private fun initObserve() {
        viewModel.couple.observe(this, Observer { it?.let { updateUI(it) } })
    }

    private fun updateUI(couple: Couple) {
        titleText.text = String.format(getString(R.string.couple_test_item_title), couple.you?.nickName)
        Glide.with(this).load(makePublicPhotoUrl(couple.you?.id))
                .bitmapTransform(CropCircleTransformation(this))
                .placeholder(couple.you?.getDefaultImgId()!!).into(profileImg)
    }
}