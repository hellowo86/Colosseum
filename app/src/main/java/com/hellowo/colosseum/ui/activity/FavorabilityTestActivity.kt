package com.hellowo.colosseum.ui.activity

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.transition.TransitionManager
import android.view.View
import com.bumptech.glide.Glide
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Couple
import com.hellowo.colosseum.model.User
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
        viewModel.initCouple(intent.getStringExtra("coupleId"))
        setContentView(R.layout.activity_favorabiliy_test)
        initLayout()
        initObserve()
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
        val myGender = Me.value?.gender as Int
        val yourGender = Me.value?.getYourGender() as Int
        titleText.text = String.format(getString(R.string.couple_test_item_title), couple.getNameByGender(yourGender))

        Glide.with(this).load(makePublicPhotoUrl(couple.getIdByGender(yourGender)))
                .bitmapTransform(CropCircleTransformation(this))
                .placeholder(User.getDefaultImgId(yourGender)).into(youImg)
        Glide.with(this).load(makePublicPhotoUrl(couple.getIdByGender(myGender)))
                .bitmapTransform(CropCircleTransformation(this))
                .placeholder(User.getDefaultImgId(myGender)).into(meImg)

        meNameText.text = getString(R.string.me)
        youNameText.text = couple.getNameByGender(yourGender)

        TransitionManager.beginDelayedTransition(rootLy, makeSlideFromBottomTransition())

        stepText.text = couple.getStepText(this)
        stepSubText.text = couple.getStepSubText(this)
        statusText.text = couple.getStatusText(this)
        statusText.setTextColor(couple.getStatusColor(this)!!)

        when(couple.step) {
            0 -> {
                youLy.visibility = View.VISIBLE
                meLy.visibility = View.VISIBLE
                setPhotoView(couple, myGender, yourGender)
            }
            1 -> {
                youLy.visibility = View.VISIBLE
                meLy.visibility = View.VISIBLE
            }
        }
    }

    private fun setPhotoView(couple: Couple, myGender: Int, yourGender: Int) {
        val myPhotoUrl = couple.getPhotoUrlByGender(myGender)
        val yourPhotoUrl = couple.getPhotoUrlByGender(yourGender)
        val myPhotoLike = couple.getPhotoLikeByGender(myGender)
        val yourPhotoLike = couple.getPhotoLikeByGender(yourGender)

        if(myPhotoUrl.isNullOrEmpty()) {
            meStatusIconImg.setImageResource(R.drawable.color_in_love)
            meStatusText.text = getString(R.string.me_reg_photo)
            regLy.visibility = View.VISIBLE
            regBtn.setOnClickListener { checkExternalStoragePermission() }
        }else {
            regLy.visibility = View.INVISIBLE
            when(yourPhotoLike) {
                -1 -> {
                    meStatusIconImg.setImageResource(R.drawable.color_letter)
                    meStatusText.text = getString(R.string.me_photo_waiting)
                }
                0 -> {
                    meStatusIconImg.setImageResource(R.drawable.color_heart)
                    meStatusText.text = getString(R.string.hate_it)
                }
                1 -> {
                    meStatusIconImg.setImageResource(R.drawable.color_care)
                    meStatusText.text = getString(R.string.like_it)
                }
            }
        }

        if(yourPhotoUrl.isNullOrEmpty()) {
            youStatusIconImg.setImageResource(R.drawable.color_in_love)
            youStatusText.text = getString(R.string.you_reg_photo)
            likeLy.visibility = View.INVISIBLE
            hateLy.visibility = View.INVISIBLE
            youLy.setOnClickListener(null)
        }else {
            when(myPhotoLike) {
                -1 -> {
                    youStatusIconImg.setImageResource(R.drawable.color_love_letter)
                    youStatusText.text = getString(R.string.you_photo_waiting)
                    youLy.setOnClickListener {
                        val intent = Intent(this, PhotoActivity::class.java)
                        intent.putExtra("photoUrl", yourPhotoUrl)
                        startActivity(intent)
                    }
                    likeLy.visibility = View.VISIBLE
                    hateLy.visibility = View.VISIBLE
                    likeLy.setOnClickListener { viewModel.like("${Couple.getGenderKey(myGender)}PhotoLike", 1, yourPhotoLike) }
                    hateLy.setOnClickListener { viewModel.like("${Couple.getGenderKey(myGender)}PhotoLike", 0, yourPhotoLike) }
                }
                0 -> {
                    youStatusIconImg.setImageResource(R.drawable.color_heart)
                    youStatusText.text = getString(R.string.hate_it)
                    likeLy.visibility = View.INVISIBLE
                    hateLy.visibility = View.INVISIBLE
                }
                1 -> {
                    youStatusIconImg.setImageResource(R.drawable.color_care)
                    youStatusText.text = getString(R.string.like_it)
                    likeLy.visibility = View.INVISIBLE
                    hateLy.visibility = View.INVISIBLE
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