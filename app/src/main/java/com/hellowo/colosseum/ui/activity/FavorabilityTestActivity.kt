package com.hellowo.colosseum.ui.activity

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.transition.TransitionManager
import android.view.View
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Couple
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.ui.dialog.FavorablityCheckListDialog
import com.hellowo.colosseum.ui.dialog.VoiceRecordDialog
import com.hellowo.colosseum.utils.dpToPx
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import com.hellowo.colosseum.utils.makeSlideFromBottomTransition
import com.hellowo.colosseum.utils.showPhotoPicker
import com.hellowo.colosseum.viewmodel.FavobalityTestViewModel
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_favorabiliy_test.*
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size

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
        meLy.visibility = View.INVISIBLE
        youLy.visibility = View.INVISIBLE
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

        val myPhotoUrl = couple.getPhotoUrlByGender(myGender)
        val yourPhotoUrl = couple.getPhotoUrlByGender(yourGender)
        val myPhotoLike = couple.getPhotoLikeByGender(myGender)
        val yourPhotoLike = couple.getPhotoLikeByGender(yourGender)

        val myVoiceUrl = couple.getVoiceUrlByGender(myGender)
        val yourVoiceUrl = couple.getVoiceUrlByGender(yourGender)
        val myVoiceLike = couple.getVoiceLikeByGender(myGender)
        val yourVoiceLike = couple.getVoiceLikeByGender(yourGender)

        val myOxString = couple.getOxByGender(myGender)
        val yourOxString = couple.getOxByGender(yourGender)

        if(myPhotoLike != -1 && yourPhotoLike != -1 && myVoiceLike != -1 && yourVoiceLike != -1 &&
                !myOxString.isNullOrEmpty() && !yourOxString.isNullOrEmpty()) {
            youLy.visibility = View.INVISIBLE
            meLy.visibility = View.INVISIBLE
            successLy.visibility = View.VISIBLE
            konfettiView.visibility = View.VISIBLE

            Glide.with(this).load(makePublicPhotoUrl(couple.getIdByGender(yourGender)))
                    .bitmapTransform(CropCircleTransformation(this))
                    .placeholder(User.getDefaultImgId(yourGender)).into(successYouImg)
            Glide.with(this).load(makePublicPhotoUrl(couple.getIdByGender(myGender)))
                    .bitmapTransform(CropCircleTransformation(this))
                    .placeholder(User.getDefaultImgId(myGender)).into(successMeImg)

            joinChatBtn.setOnClickListener { viewModel.createChat() }

            konfettiView.postDelayed({
                konfettiView.build()
                        .addColors(resources.getColor(R.color.colorPrimary))
                        .setDirection(0.0, 359.0)
                        .setSpeed(1f, 5f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(2000L)
                        .addShapes(Shape.RECT, Shape.CIRCLE)
                        .addSizes(Size(12))
                        .setPosition(konfettiView.x + konfettiView.width / 2, konfettiView.y + konfettiView.height / 2)
                        .burst(50)
                //        .setPosition(-50f, konfettiView.width + 50f, -50f, -50f)
                //        .stream(30, 5000L)

            }, 1000)
            return
        }else {
            youLy.visibility = View.VISIBLE
            meLy.visibility = View.VISIBLE
            successLy.visibility = View.INVISIBLE
            konfettiView.visibility = View.INVISIBLE
        }

        if(myPhotoUrl.isNullOrEmpty()) {
            mePhotoBtn.cardElevation = dpToPx(this, 2f)
            mePhotoLy.setBackgroundResource(R.color.white)
            mePhotoIconImg.setImageResource(R.drawable.ic_add_a_photo_black_48dp)
            mePhotoIconImg.setColorFilter(resources.getColor(R.color.iconTint))
            mePhotoStatusText.text = getString(R.string.reg_photo)
            mePhotoStatusText.setTextColor(resources.getColor(R.color.disableText))
            mePhotoBtn.setOnClickListener { showPhotoPicker(this@FavorabilityTestActivity){
                uri -> viewModel.uploadPhoto(this, uri)
            }}
        }else {
            when(yourPhotoLike) {
                -1 -> {
                    mePhotoBtn.cardElevation = dpToPx(this, 0f)
                    mePhotoLy.setBackgroundResource(R.drawable.accent_rect_radius)
                    mePhotoIconImg.setImageResource(R.drawable.ic_person_black_24dp)
                    mePhotoIconImg.setColorFilter(resources.getColor(R.color.colorAccent))
                    mePhotoStatusText.text = getString(R.string.me_photo_waiting)
                    mePhotoStatusText.setTextColor(resources.getColor(R.color.colorAccent))
                    mePhotoBtn.setOnClickListener {
                        val intent = Intent(this, PhotoActivity::class.java)
                        intent.putExtra("photoUrl", yourPhotoUrl)
                        startActivityForResult(intent, 1)
                    }
                }
                0 -> {
                    mePhotoBtn.cardElevation = dpToPx(this, 0f)
                    mePhotoLy.setBackgroundResource(R.color.grey)
                    mePhotoIconImg.setImageResource(R.drawable.ic_person_black_24dp)
                    mePhotoIconImg.setColorFilter(resources.getColor(R.color.white))
                    mePhotoStatusText.text = getString(R.string.you_hate)
                    mePhotoStatusText.setTextColor(resources.getColor(R.color.white))
                    mePhotoBtn.setOnClickListener(null)
                }
                1 -> {
                    mePhotoBtn.cardElevation = dpToPx(this, 0f)
                    mePhotoLy.setBackgroundResource(R.color.colorPrimary)
                    mePhotoIconImg.setImageResource(R.drawable.ic_person_black_24dp)
                    mePhotoIconImg.setColorFilter(resources.getColor(R.color.white))
                    mePhotoStatusText.text = getString(R.string.you_like)
                    mePhotoStatusText.setTextColor(resources.getColor(R.color.white))
                    mePhotoBtn.setOnClickListener(null)
                }
            }
        }

        if(yourPhotoUrl.isNullOrEmpty()) {
            youPhotoBtn.cardElevation = dpToPx(this, 0f)
            youPhotoLy.setBackgroundResource(R.drawable.dash_rect)
            youPhotoIconImg.setImageResource(R.drawable.ic_person_black_24dp)
            youPhotoIconImg.setColorFilter(resources.getColor(R.color.iconTint))
            youPhotoStatusText.text = getString(R.string.reg_photo_y)
            youPhotoStatusText.setTextColor(resources.getColor(R.color.disableText))
            youPhotoBtn.setOnClickListener(null)
        }else {
            when(myPhotoLike) {
                -1 -> {
                    youPhotoBtn.cardElevation = dpToPx(this, 2f)
                    youPhotoLy.setBackgroundResource(R.color.white)
                    youPhotoIconImg.setImageResource(R.drawable.ic_person_black_24dp)
                    youPhotoIconImg.setColorFilter(resources.getColor(R.color.colorAccent))
                    youPhotoStatusText.text = getString(R.string.me_photo_check)
                    youPhotoStatusText.setTextColor(resources.getColor(R.color.colorAccent))
                    youPhotoBtn.setOnClickListener {
                        val intent = Intent(this, PhotoActivity::class.java)
                        intent.putExtra("photoUrl", yourPhotoUrl)
                        intent.putExtra("evaluation", true)
                        startActivityForResult(intent, 1)
                    }
                }
                0 -> {
                    youPhotoBtn.cardElevation = dpToPx(this, 0f)
                    youPhotoLy.setBackgroundResource(R.color.grey)
                    youPhotoIconImg.setImageResource(R.drawable.ic_person_black_24dp)
                    youPhotoIconImg.setColorFilter(resources.getColor(R.color.white))
                    youPhotoStatusText.text = getString(R.string.hate_it)
                    youPhotoStatusText.setTextColor(resources.getColor(R.color.white))
                    youPhotoBtn.setOnClickListener(null)
                }
                1 -> {
                    youPhotoBtn.cardElevation = dpToPx(this, 0f)
                    youPhotoLy.setBackgroundResource(R.color.colorPrimary)
                    youPhotoIconImg.setImageResource(R.drawable.ic_person_black_24dp)
                    youPhotoIconImg.setColorFilter(resources.getColor(R.color.white))
                    youPhotoStatusText.text = getString(R.string.like_it)
                    youPhotoStatusText.setTextColor(resources.getColor(R.color.white))
                    youPhotoBtn.setOnClickListener(null)
                }
            }
        }

        if(myVoiceUrl.isNullOrEmpty()) {
            meVoiceBtn.cardElevation = dpToPx(this, 2f)
            meVoiceLy.setBackgroundResource(R.color.white)
            meVoiceIconImg.setImageResource(R.drawable.abc_ic_voice_search_api_material)
            meVoiceIconImg.setColorFilter(resources.getColor(R.color.iconTint))
            meVoiceStatusText.text = getString(R.string.reg_voice)
            meVoiceStatusText.setTextColor(resources.getColor(R.color.disableText))
            meVoiceBtn.setOnClickListener {
                VoiceRecordDialog(null, false){ filePath, evalutaion ->
                    viewModel.uploadVoice(this, filePath)
                }.show(supportFragmentManager, null)
            }
        }else {
            when(yourVoiceLike) {
                -1 -> {
                    meVoiceBtn.cardElevation = dpToPx(this, 0f)
                    meVoiceLy.setBackgroundResource(R.drawable.accent_rect_radius)
                    meVoiceIconImg.setImageResource(R.drawable.ic_hearing_black_48dp)
                    meVoiceIconImg.setColorFilter(resources.getColor(R.color.colorAccent))
                    meVoiceStatusText.text = getString(R.string.me_voice_waiting)
                    meVoiceStatusText.setTextColor(resources.getColor(R.color.colorAccent))
                    meVoiceBtn.setOnClickListener {
                        VoiceRecordDialog(myVoiceUrl, false){ _, _ -> }.show(supportFragmentManager, null)
                    }
                }
                0 -> {
                    meVoiceBtn.cardElevation = dpToPx(this, 0f)
                    meVoiceLy.setBackgroundResource(R.color.grey)
                    meVoiceIconImg.setImageResource(R.drawable.ic_hearing_black_48dp)
                    meVoiceIconImg.setColorFilter(resources.getColor(R.color.white))
                    meVoiceStatusText.text = getString(R.string.you_hate)
                    meVoiceStatusText.setTextColor(resources.getColor(R.color.white))
                    meVoiceBtn.setOnClickListener(null)
                }
                1 -> {
                    meVoiceBtn.cardElevation = dpToPx(this, 0f)
                    meVoiceLy.setBackgroundResource(R.color.colorPrimary)
                    meVoiceIconImg.setImageResource(R.drawable.ic_hearing_black_48dp)
                    meVoiceIconImg.setColorFilter(resources.getColor(R.color.white))
                    meVoiceStatusText.text = getString(R.string.you_like)
                    meVoiceStatusText.setTextColor(resources.getColor(R.color.white))
                    meVoiceBtn.setOnClickListener(null)
                }
            }
        }

        if(yourVoiceUrl.isNullOrEmpty()) {
            youVoiceBtn.cardElevation = dpToPx(this, 0f)
            youVoiceLy.setBackgroundResource(R.drawable.dash_rect)
            youVoiceIconImg.setImageResource(R.drawable.abc_ic_voice_search_api_material)
            youVoiceIconImg.setColorFilter(resources.getColor(R.color.iconTint))
            youVoiceStatusText.text = getString(R.string.reg_voice_y)
            youVoiceStatusText.setTextColor(resources.getColor(R.color.disableText))
            youVoiceBtn.setOnClickListener(null)
        }else {
            when(myVoiceLike) {
                -1 -> {
                    youVoiceBtn.cardElevation = dpToPx(this, 2f)
                    youVoiceLy.setBackgroundResource(R.color.white)
                    youVoiceIconImg.setImageResource(R.drawable.ic_hearing_black_48dp)
                    youVoiceIconImg.setColorFilter(resources.getColor(R.color.colorAccent))
                    youVoiceStatusText.text = getString(R.string.me_voice_check)
                    youVoiceStatusText.setTextColor(resources.getColor(R.color.colorAccent))
                    youVoiceBtn.setOnClickListener {
                        VoiceRecordDialog(yourVoiceUrl, true){ filePath, evalutaion ->
                            viewModel.like("${Couple.getGenderKey(myGender)}VoiceLike", if(evalutaion) 1 else 0)
                        }.show(supportFragmentManager, null)
                    }
                }
                0 -> {
                    youVoiceBtn.cardElevation = dpToPx(this, 0f)
                    youVoiceLy.setBackgroundResource(R.color.grey)
                    youVoiceIconImg.setImageResource(R.drawable.ic_hearing_black_48dp)
                    youVoiceIconImg.setColorFilter(resources.getColor(R.color.white))
                    youVoiceStatusText.text = getString(R.string.hate_it)
                    youVoiceStatusText.setTextColor(resources.getColor(R.color.white))
                    youVoiceBtn.setOnClickListener(null)
                }
                1 -> {
                    youVoiceBtn.cardElevation = dpToPx(this, 0f)
                    youVoiceLy.setBackgroundResource(R.color.colorPrimary)
                    youVoiceIconImg.setImageResource(R.drawable.ic_hearing_black_48dp)
                    youVoiceIconImg.setColorFilter(resources.getColor(R.color.white))
                    youVoiceStatusText.text = getString(R.string.like_it)
                    youVoiceStatusText.setTextColor(resources.getColor(R.color.white))
                    youVoiceBtn.setOnClickListener(null)
                }
            }
        }

        if(myOxString.isNullOrEmpty()) {
            meQuestionBtn.cardElevation = dpToPx(this, 2f)
            meQuestionLy.setBackgroundResource(R.color.white)
            meQuestionIconImg.setImageResource(R.drawable.ic_lightbulb_outline_black_48dp)
            meQuestionIconImg.setColorFilter(resources.getColor(R.color.iconTint))
            meQuestionStatusText.text = getString(R.string.reg_questions)
            meQuestionStatusText.setTextColor(resources.getColor(R.color.disableText))
            meQuestionBtn.setOnClickListener {
                FavorablityCheckListDialog("0000000000", true){
                    viewModel.setCheckList("${Couple.getGenderKey(myGender)}Ox", it)
                }.show(supportFragmentManager, null)
            }
        }else {
            if(yourOxString.isNullOrEmpty()) {
                meQuestionBtn.cardElevation = dpToPx(this, 0f)
                meQuestionLy.setBackgroundResource(R.drawable.accent_rect_radius)
                meQuestionIconImg.setImageResource(R.drawable.ic_done_black_48dp)
                meQuestionIconImg.setColorFilter(resources.getColor(R.color.colorAccent))
                meQuestionStatusText.text = getString(R.string.me_check_waiting)
                meQuestionStatusText.setTextColor(resources.getColor(R.color.colorAccent))
                meQuestionBtn.setOnClickListener {
                    FavorablityCheckListDialog(myOxString!!, false){}.show(supportFragmentManager, null)
                }
            }
        }

        if(yourOxString.isNullOrEmpty()) {
            youQuestionBtn.cardElevation = dpToPx(this, 0f)
            youQuestionLy.setBackgroundResource(R.drawable.dash_rect)
            youQuestionIconImg.setImageResource(R.drawable.ic_lightbulb_outline_black_48dp)
            youQuestionIconImg.setColorFilter(resources.getColor(R.color.iconTint))
            youQuestionStatusText.text = getString(R.string.reg_questions_y)
            youQuestionStatusText.setTextColor(resources.getColor(R.color.disableText))
            youQuestionBtn.setOnClickListener(null)
        }else {
            if(myOxString.isNullOrEmpty()) {
                youQuestionBtn.cardElevation = dpToPx(this, 0f)
                youQuestionLy.setBackgroundResource(R.drawable.accent_rect_radius)
                youQuestionIconImg.setImageResource(R.drawable.ic_done_black_48dp)
                youQuestionIconImg.setColorFilter(resources.getColor(R.color.colorAccent))
                youQuestionStatusText.text = getString(R.string.you_check_waiting)
                youQuestionStatusText.setTextColor(resources.getColor(R.color.colorAccent))
                youQuestionBtn.setOnClickListener(null)
            }
        }

        if(!myOxString.isNullOrEmpty() && !yourOxString.isNullOrEmpty()) {
            if(evaluateCheckList(myOxString, yourOxString)) {
                meQuestionBtn.cardElevation = dpToPx(this, 0f)
                meQuestionLy.setBackgroundResource(R.color.colorPrimary)
                meQuestionIconImg.setImageResource(R.drawable.ic_done_black_48dp)
                meQuestionIconImg.setColorFilter(resources.getColor(R.color.white))
                meQuestionStatusText.text = getString(R.string.check_list_success)
                meQuestionStatusText.setTextColor(resources.getColor(R.color.white))
                meQuestionBtn.setOnClickListener(null)
                youQuestionBtn.cardElevation = dpToPx(this, 0f)
                youQuestionLy.setBackgroundResource(R.color.colorPrimary)
                youQuestionIconImg.setImageResource(R.drawable.ic_done_black_48dp)
                youQuestionIconImg.setColorFilter(resources.getColor(R.color.white))
                youQuestionStatusText.text = getString(R.string.check_list_success)
                youQuestionStatusText.setTextColor(resources.getColor(R.color.white))
                youQuestionBtn.setOnClickListener(null)
            }else {
                meQuestionBtn.cardElevation = dpToPx(this, 0f)
                meQuestionLy.setBackgroundResource(R.color.grey)
                meQuestionIconImg.setImageResource(R.drawable.ic_done_black_48dp)
                meQuestionIconImg.setColorFilter(resources.getColor(R.color.white))
                meQuestionStatusText.text = getString(R.string.check_list_failed)
                meQuestionStatusText.setTextColor(resources.getColor(R.color.white))
                meQuestionBtn.setOnClickListener(null)
                youQuestionBtn.cardElevation = dpToPx(this, 0f)
                youQuestionLy.setBackgroundResource(R.color.grey)
                youQuestionIconImg.setImageResource(R.drawable.ic_done_black_48dp)
                youQuestionIconImg.setColorFilter(resources.getColor(R.color.white))
                youQuestionStatusText.text = getString(R.string.check_list_failed)
                youQuestionStatusText.setTextColor(resources.getColor(R.color.white))
                youQuestionBtn.setOnClickListener(null)
            }
        }
    }

    private fun evaluateCheckList(myOxString: String?, yourOxString: String?) : Boolean {
        var count = 0
        (0 until myOxString?.length!!).forEach {
            if(myOxString[it] == yourOxString!![it]) count++
        }
        return count >= 5
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1) {
            val myGender = Me.value?.gender as Int
            if(resultCode == Activity.RESULT_OK) {
                viewModel.like("${Couple.getGenderKey(myGender)}PhotoLike", 1)
            }else if(resultCode == Activity.RESULT_CANCELED){
                viewModel.like("${Couple.getGenderKey(myGender)}PhotoLike", 0)
            }
        }
    }
}