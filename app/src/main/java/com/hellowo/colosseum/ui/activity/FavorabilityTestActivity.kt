package com.hellowo.colosseum.ui.activity

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.transition.TransitionManager
import android.view.View
import com.bumptech.glide.Glide
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.hellowo.colosseum.R
import com.hellowo.colosseum.model.Couple
import com.hellowo.colosseum.utils.dpToPx
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import com.hellowo.colosseum.utils.makeSlideFromBottomTransition
import com.hellowo.colosseum.viewmodel.FavobalityTestViewModel
import gun0912.tedbottompicker.TedBottomPicker
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_favorabiliy_test.*
import java.util.ArrayList

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
    }

    private fun initObserve() {
        viewModel.couple.observe(this, Observer { it?.let { updateUI(it) } })
        viewModel.loading.observe(this, Observer { progressBar.visibility = if(it as Boolean) View.VISIBLE else View.GONE })
        viewModel.isUploading.observe(this, Observer { if(it as Boolean) showProgressDialog() else hideProgressDialog() })
    }

    private fun updateUI(couple: Couple) {
        titleText.text = String.format(getString(R.string.couple_test_item_title), couple.you?.nickName)
        Glide.with(this).load(makePublicPhotoUrl(couple.you?.id))
                .bitmapTransform(CropCircleTransformation(this))
                .placeholder(couple.you?.getDefaultImgId()!!).into(youImg)
        Glide.with(this).load(makePublicPhotoUrl(couple.me?.id))
                .bitmapTransform(CropCircleTransformation(this))
                .placeholder(couple.you?.getDefaultImgId()!!).into(meImg)

        meNameText.text = couple.me?.nickName
        youNameText.text = couple.you?.nickName

        TransitionManager.beginDelayedTransition(rootLy, makeSlideFromBottomTransition())

        stepText.text = couple.getStepText(this)
        stepSubText.text = couple.getStepSubText(this)
        statusText.text = couple.getStatusText(this)
        statusText.setTextColor(couple.getStatusColor(this)!!)

        when(couple.step) {
            0 -> {
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
            meStatusIconImg.setImageResource(R.drawable.color_in_love)
            meStatusText.text = getString(R.string.me_reg_photo)
            regLy.visibility = View.VISIBLE
            regBtn.setOnClickListener { checkExternalStoragePermission() }
        }else {
            regLy.visibility = View.GONE
            when(yourPhotoLike) {
                -1 -> {
                    meStatusIconImg.setImageResource(R.drawable.color_letter)
                    meStatusText.text = getString(R.string.me_photo_waiting)
                }
                0 -> {

                }
                1 -> {

                }
            }
        }

        if(yourPhotoUrl.isNullOrEmpty()) {
            youStatusIconImg.setImageResource(R.drawable.color_in_love)
            youStatusText.text = getString(R.string.you_reg_photo)
            likeLy.visibility = View.GONE
            hateLy.visibility = View.GONE
        }else {
            when(myPhotoLike) {
                -1 -> {

                }
                0 -> {

                }
                1 -> {

                }
            }
        }
    }

    private val permissionlistener = object : PermissionListener {
        override fun onPermissionGranted() {
            showPhotoPicker()
        }
        override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {}
    }

    private fun checkExternalStoragePermission() {
        TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check()
    }

    private fun showPhotoPicker() {
        val bottomSheetDialogFragment = TedBottomPicker.Builder(this)
                .setOnImageSelectedListener { uri -> viewModel.uploadPhoto(this, uri) }
                .setPreviewMaxCount(100)
                .create()
        bottomSheetDialogFragment.show(supportFragmentManager)
    }
}