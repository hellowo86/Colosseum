package com.hellowo.colosseum.ui.activity

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.transition.TransitionManager
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import com.bumptech.glide.Glide
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.ChemistryQuest
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Couple
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.ui.dialog.AnswerCheckDialog
import com.hellowo.colosseum.ui.dialog.EnterCommentDialog
import com.hellowo.colosseum.ui.dialog.FavorablityCheckListDialog
import com.hellowo.colosseum.ui.dialog.VoiceRecordDialog
import com.hellowo.colosseum.utils.*
import com.hellowo.colosseum.viewmodel.ChemistryViewModel
import gun0912.tedbottompicker.TedBottomPicker
import gun0912.tedbottompicker.util.RealPathUtil
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_chemistry.*
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.ArrayList

class ChemistryActivity : BaseActivity() {
    private lateinit var viewModel: ChemistryViewModel
    private var photoPicker: TedBottomPicker? = null
    private val myGender = Me.value?.gender as Int
    private val yourGender = Me.value?.yourGender() as Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ChemistryViewModel::class.java)
        viewModel.initCouple(intent.getStringExtra("coupleId"))
        setContentView(R.layout.activity_chemistry)
        initLayout()
        initObserve()
    }

    private fun initLayout() {
        progressLy.visibility = View.INVISIBLE
        questLy.visibility = View.INVISIBLE
        actionLy.visibility = View.INVISIBLE
        backBtn.setOnClickListener { finish() }
    }

    private fun initObserve() {
        viewModel.couple.observe(this, Observer { it?.let { updateUI(it) }})
        viewModel.nextStepCouple.observe(this, Observer { it?.let { updateNextStepUI(it) }})
        viewModel.loading.observe(this, Observer { progressBar.visibility = if(it as Boolean) View.VISIBLE else View.GONE })
        viewModel.isUploading.observe(this, Observer { if(it as Boolean) showProgressDialog() else hideProgressDialog() })
        viewModel.joinChat.observe(this, Observer {
            val chatIntent = Intent(this, ChatingActivity::class.java)
            chatIntent.putExtra("chatId", it)
            startActivity(chatIntent)
            finish()
        })
    }

    private fun updateNextStepUI(it: Couple) {
        TransitionManager.beginDelayedTransition(contentLy, makeSlideFromBottomTransition())
        successLy.visibility = View.INVISIBLE
        konfettiView.visibility = View.INVISIBLE
        questLy.visibility = View.INVISIBLE
        actionLy.visibility = View.INVISIBLE
        rootLy.postDelayed({ viewModel.couple.value = viewModel.nextStepCouple.value }, 1000)
    }

    private fun updateUI(couple: Couple) {
        TransitionManager.beginDelayedTransition(contentLy, makeSlideFromBottomTransition())

        titleText.text = String.format(getString(R.string.couple_test_item_title), couple.getNameByGender(yourGender))
        Glide.with(this).load(makePublicPhotoUrl(couple.getIdByGender(yourGender)))
                .bitmapTransform(CropCircleTransformation(this))
                .placeholder(User.getDefaultImgId(yourGender)).into(profileImg)

        statusText.text = couple.getStatusText(this)
        statusText.setTextColor(couple.getStatusColor(this)!!)
        stepText.text = couple.getStepText(this)
        progressLy.visibility = View.VISIBLE

        when {
            couple.status == 0 -> {
                successLy.visibility = View.INVISIBLE
                konfettiView.visibility = View.INVISIBLE
                questLy.visibility = View.VISIBLE
                actionLy.visibility = View.VISIBLE

                heartImg.setImageResource(R.drawable.ic_favorite_black_48dp)
                heartImg.setColorFilter(resources.getColor(R.color.colorPrimary))
                val anim = ScaleAnimation(1f, 0.8f, 1f, 0.8f, dpToPx(this, 15f), dpToPx(this, 15f))
                anim.duration = 500
                anim.repeatCount = Animation.INFINITE
                anim.repeatMode = Animation.REVERSE
                anim.interpolator = FastOutSlowInInterpolator()
                heartImg.startAnimation(anim)

                val iconId = ChemistryQuest.questIconResource(couple.step)
                questIcon.setImageResource(iconId)
                meIcon.setImageResource(iconId)
                youIcon.setImageResource(iconId)

                val questId = couple.questId()
                Glide.with(this).load(ChemistryQuest.questImgResource(couple.step, questId)).into(questImg)
                questText.text = ChemistryQuest.questTitle(couple.step, questId)
                questSubText.text = ChemistryQuest.questSub(couple.step, questId)
            }
            couple.status == 1 -> setSuccessLy(couple, true)
            else -> setSuccessLy(couple, false)
        }

        var stickToX = 0f
        when(couple.step){
            0 -> {
                stickToX = 0.0f
                setPhotoActionLy(couple)
            }
            1 -> {
                stickToX = 0.33f
                setVoiceLy(couple)
            }
            2 -> {
                stickToX = 0.66f
                setAnswerLy(couple)
            }
            3 -> {
                stickToX = 1f
            }
        }

        progressStickView.postDelayed({
            val anim = AnimatorSet()
            anim.playTogether(ObjectAnimator.ofFloat(progressStickView, "scaleX", progressStickView.scaleX, stickToX).setDuration(3000))
            anim.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {}
                override fun onAnimationEnd(animator: Animator) {
                    progressStickView.scaleX = stickToX
                }
                override fun onAnimationCancel(animator: Animator) {}
                override fun onAnimationRepeat(animator: Animator) {}
            })
            anim.interpolator = FastOutSlowInInterpolator()
            anim.setDuration(500).start()
        }, 500)
    }

    private fun setPhotoActionLy(couple: Couple) {
        val myPhotoUrl = couple.getPhotoUrlByGender(myGender)
        val yourPhotoUrl = couple.getPhotoUrlByGender(yourGender)
        val myPhotoLike = couple.getPhotoLikeByGender(myGender)
        val yourPhotoLike = couple.getPhotoLikeByGender(yourGender)

        if(myPhotoUrl.isNullOrEmpty()) {
            meLy.setBackgroundResource(R.drawable.dash_rect)
            meCard.visibility = View.GONE
            meText.text = getString(R.string.reg_photo)
            sendBtn.visibility = View.VISIBLE
            sendBtn.setOnClickListener { showPhotoPicker() }
        }else {
            meCard.visibility = View.VISIBLE
            Glide.with(this).load(myPhotoUrl).into(meCardImg)
            when(yourPhotoLike) {
                -1 -> {
                    meLy.setBackgroundResource(R.drawable.dash_rect)
                    meCardCoverView.setBackgroundResource(R.color.blackAlpha)
                    meCardText.text = getString(R.string.me_photo_waiting)
                    meCardIcon.visibility = View.GONE
                    sendBtn.visibility = View.GONE
                }
                0 -> {
                    meLy.setBackgroundResource(R.drawable.dash_rect)
                    meCardIcon.setImageResource(R.drawable.ic_sentiment_dissatisfied_black_48dp)
                    meCardCoverView.setBackgroundResource(R.color.greyAlpha)
                    meCardText.text = getString(R.string.you_hate)
                    meCardIcon.visibility = View.VISIBLE
                    sendBtn.visibility = View.GONE
                }
                1 -> {
                    meLy.setBackgroundResource(R.drawable.dash_rect_primary)
                    meCardIcon.setImageResource(R.drawable.ic_sentiment_very_satisfied_black_48dp)
                    meCardCoverView.setBackgroundResource(R.color.colorPrimaryAlpha)
                    meCardText.text = getString(R.string.you_like)
                    meCardIcon.visibility = View.VISIBLE
                    sendBtn.visibility = View.GONE
                }
            }
        }

        if(yourPhotoUrl.isNullOrEmpty()) {
            youLy.setBackgroundResource(R.drawable.dash_rect)
            youCard.visibility = View.GONE
            youText.text = getString(R.string.reg_photo_y)
        }else {
            youCard.visibility = View.VISIBLE
            Glide.with(this).load(yourPhotoUrl).into(youCardImg)
            when(myPhotoLike) {
                -1 -> {
                    youLy.setBackgroundResource(R.drawable.dash_rect)
                    youCardCoverView.setBackgroundResource(R.color.blackAlpha)
                    youCardText.text = getString(R.string.me_photo_check)
                    youCardIcon.visibility = View.GONE
                    checkBtn.visibility = View.VISIBLE
                    checkBtn.setOnClickListener { startPhotoActivity(yourPhotoUrl!!, true) }
                }
                0 -> {
                    youLy.setBackgroundResource(R.drawable.dash_rect)
                    youCardIcon.setImageResource(R.drawable.ic_sentiment_dissatisfied_black_48dp)
                    youCardCoverView.setBackgroundResource(R.color.greyAlpha)
                    youCardText.text = getString(R.string.hate_it)
                    youCardIcon.visibility = View.VISIBLE
                    checkBtn.visibility = View.GONE
                }
                1 -> {
                    youLy.setBackgroundResource(R.drawable.dash_rect_primary)
                    youCardIcon.setImageResource(R.drawable.ic_sentiment_very_satisfied_black_48dp)
                    youCardCoverView.setBackgroundResource(R.color.colorPrimaryAlpha)
                    youCardText.text = getString(R.string.like_it)
                    youCardIcon.visibility = View.VISIBLE
                    checkBtn.visibility = View.GONE
                }
            }
        }
    }

    private fun setVoiceLy(couple: Couple) {
        val myVoiceUrl = couple.getVoiceUrlByGender(myGender)
        val yourVoiceUrl = couple.getVoiceUrlByGender(yourGender)
        val myVoiceLike = couple.getVoiceLikeByGender(myGender)
        val yourVoiceLike = couple.getVoiceLikeByGender(yourGender)

        if(myVoiceUrl.isNullOrEmpty()) {
            meLy.setBackgroundResource(R.drawable.dash_rect)
            meCard.visibility = View.GONE
            meText.text = getString(R.string.reg_voice)
            sendBtn.visibility = View.VISIBLE
            sendBtn.setOnClickListener { showVoiceDialog() }
        }else {
            meCard.visibility = View.VISIBLE
            Glide.with(this).load(ChemistryQuest.letterImg(couple.questId())).into(meCardImg)
            when(yourVoiceLike) {
                -1 -> {
                    meLy.setBackgroundResource(R.drawable.dash_rect)
                    meCardCoverView.setBackgroundResource(R.color.blackAlpha)
                    meCardText.text = getString(R.string.me_voice_waiting)
                    meCardIcon.visibility = View.GONE
                    sendBtn.visibility = View.GONE
                }
                0 -> {
                    meLy.setBackgroundResource(R.drawable.dash_rect)
                    meCardIcon.setImageResource(R.drawable.ic_sentiment_dissatisfied_black_48dp)
                    meCardCoverView.setBackgroundResource(R.color.greyAlpha)
                    meCardText.text = getString(R.string.you_hate)
                    meCardIcon.visibility = View.VISIBLE
                    sendBtn.visibility = View.GONE
                }
                1 -> {
                    meLy.setBackgroundResource(R.drawable.dash_rect_primary)
                    meCardIcon.setImageResource(R.drawable.ic_sentiment_very_satisfied_black_48dp)
                    meCardCoverView.setBackgroundResource(R.color.colorPrimaryAlpha)
                    meCardText.text = getString(R.string.you_like)
                    meCardIcon.visibility = View.VISIBLE
                    sendBtn.visibility = View.GONE
                }
            }
        }

        if(yourVoiceUrl.isNullOrEmpty()) {
            youLy.setBackgroundResource(R.drawable.dash_rect)
            youCard.visibility = View.GONE
            youText.text = getString(R.string.reg_voice_y)
        }else {
            youCard.visibility = View.VISIBLE
            Glide.with(this).load(ChemistryQuest.letterImg(couple.questId())).into(youCardImg)
            when(myVoiceLike) {
                -1 -> {
                    youLy.setBackgroundResource(R.drawable.dash_rect)
                    youCardCoverView.setBackgroundResource(R.color.blackAlpha)
                    youCardText.text = getString(R.string.me_voice_check)
                    youCardIcon.visibility = View.GONE
                    checkBtn.visibility = View.VISIBLE
                    checkBtn.setOnClickListener { showVoiceHearDialog(yourVoiceUrl!!, true, myGender) }
                }
                0 -> {
                    youLy.setBackgroundResource(R.drawable.dash_rect)
                    youCardIcon.setImageResource(R.drawable.ic_sentiment_dissatisfied_black_48dp)
                    youCardCoverView.setBackgroundResource(R.color.greyAlpha)
                    youCardText.text = getString(R.string.hate_it)
                    youCardIcon.visibility = View.VISIBLE
                    checkBtn.visibility = View.GONE
                }
                1 -> {
                    youLy.setBackgroundResource(R.drawable.dash_rect_primary)
                    youCardIcon.setImageResource(R.drawable.ic_sentiment_very_satisfied_black_48dp)
                    youCardCoverView.setBackgroundResource(R.color.colorPrimaryAlpha)
                    youCardText.text = getString(R.string.like_it)
                    youCardIcon.visibility = View.VISIBLE
                    checkBtn.visibility = View.GONE
                }
            }
        }
    }

    private fun setAnswerLy(couple: Couple) {
        val myAnswer = couple.getAnswerByGender(myGender)
        val yourAnswer = couple.getAnswerByGender(yourGender)
        val myAnswerLike = couple.getAnswerLikeByGender(myGender)
        val yourAnswerLike = couple.getAnswerLikeByGender(yourGender)

        if(myAnswer.isNullOrEmpty()) {
            meLy.setBackgroundResource(R.drawable.dash_rect)
            meCard.visibility = View.GONE
            meText.text = getString(R.string.reg_ans)
            sendBtn.visibility = View.VISIBLE
            sendBtn.setOnClickListener { showAnswerDialog() }
        }else {
            meCard.visibility = View.VISIBLE
            Glide.with(this).load(ChemistryQuest.letterImg(couple.questId() + 1)).into(meCardImg)
            when(yourAnswerLike) {
                -1 -> {
                    meLy.setBackgroundResource(R.drawable.dash_rect)
                    meCardCoverView.setBackgroundResource(R.color.blackAlpha)
                    meCardText.text = getString(R.string.me_ans_waiting)
                    meCardIcon.visibility = View.GONE
                    sendBtn.visibility = View.GONE
                }
                0 -> {
                    meLy.setBackgroundResource(R.drawable.dash_rect)
                    meCardIcon.setImageResource(R.drawable.ic_sentiment_dissatisfied_black_48dp)
                    meCardCoverView.setBackgroundResource(R.color.greyAlpha)
                    meCardText.text = getString(R.string.you_hate)
                    meCardIcon.visibility = View.VISIBLE
                    sendBtn.visibility = View.GONE
                }
                1 -> {
                    meLy.setBackgroundResource(R.drawable.dash_rect_primary)
                    meCardIcon.setImageResource(R.drawable.ic_sentiment_very_satisfied_black_48dp)
                    meCardCoverView.setBackgroundResource(R.color.colorPrimaryAlpha)
                    meCardText.text = getString(R.string.you_like)
                    meCardIcon.visibility = View.VISIBLE
                    sendBtn.visibility = View.GONE
                }
            }
        }

        if(yourAnswer.isNullOrEmpty()) {
            youLy.setBackgroundResource(R.drawable.dash_rect)
            youCard.visibility = View.GONE
            youText.text = getString(R.string.reg_ans_y)
        }else {
            youCard.visibility = View.VISIBLE
            Glide.with(this).load(ChemistryQuest.letterImg(couple.questId() + 1)).into(youCardImg)
            when(myAnswerLike) {
                -1 -> {
                    youLy.setBackgroundResource(R.drawable.dash_rect)
                    youCardCoverView.setBackgroundResource(R.color.blackAlpha)
                    youCardText.text = getString(R.string.me_asw_check)
                    youCardIcon.visibility = View.GONE
                    checkBtn.visibility = View.VISIBLE
                    checkBtn.setOnClickListener { showCheckAnswerDialog(yourAnswer, true, couple.getIdByGender(yourGender), couple.getNameByGender(yourGender)) }
                }
                0 -> {
                    youLy.setBackgroundResource(R.drawable.dash_rect)
                    youCardIcon.setImageResource(R.drawable.ic_sentiment_dissatisfied_black_48dp)
                    youCardCoverView.setBackgroundResource(R.color.greyAlpha)
                    youCardText.text = getString(R.string.hate_it)
                    youCardIcon.visibility = View.VISIBLE
                    checkBtn.visibility = View.GONE
                }
                1 -> {
                    youLy.setBackgroundResource(R.drawable.dash_rect_primary)
                    youCardIcon.setImageResource(R.drawable.ic_sentiment_very_satisfied_black_48dp)
                    youCardCoverView.setBackgroundResource(R.color.colorPrimaryAlpha)
                    youCardText.text = getString(R.string.like_it)
                    youCardIcon.visibility = View.VISIBLE
                    checkBtn.visibility = View.GONE
                }
            }
        }
    }

    fun setSuccessLy(couple: Couple, isSuccess: Boolean) {
        successLy.visibility = View.VISIBLE

        Glide.with(this).load(makePublicPhotoUrl(couple.getIdByGender(yourGender)))
                .bitmapTransform(CropCircleTransformation(this))
                .placeholder(User.getDefaultImgId(yourGender)).into(successYouImg)
        Glide.with(this).load(makePublicPhotoUrl(couple.getIdByGender(myGender)))
                .bitmapTransform(CropCircleTransformation(this))
                .placeholder(User.getDefaultImgId(myGender)).into(successMeImg)

        if(isSuccess) {
            konfettiView.visibility = View.VISIBLE
            successCoverImage.setImageResource(R.drawable.c_1)
            successIcon.setImageResource(R.drawable.logo_normal)
            successTitleText.text = getString(R.string.congratulation)
            successSubText.text = getString(R.string.success_matching)
            resultText.text = getString(R.string.join_chat)
            coinText.text = "x10"
            resultBtn.setOnClickListener { viewModel.createChat() }

            konfettiView.postDelayed({
                konfettiView.build()
                        .addColors(resources.getColor(R.color.colorPrimary))
                        .setDirection(0.0, 180.0)
                        .setSpeed(5f, 10f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(2000L)
                        .addShapes(Shape.RECT, Shape.CIRCLE)
                        .addSizes(Size(12))
                        .setPosition(-50f, konfettiView.width + 50f, -50f, -50f)
                        .stream(50, 5000L)
            }, 1000)
        }else {
            heartImg.setImageResource(R.drawable.broken_heart)
            heartImg.setColorFilter(resources.getColor(R.color.iconTint))
            heartImg.clearAnimation()
            successCoverImage.setImageResource(R.drawable.f_1)
            successIcon.setImageResource(R.drawable.broken_heart)
            successTitleText.text = getString(R.string.sorry)
            successSubText.text = getString(R.string.failed_matching)
            resultText.text = getString(R.string.retry)
            coinText.text = "x1"
            resultBtn.setOnClickListener { viewModel.retry() }
        }
    }

    fun showPhotoPicker() {
        TedPermission(this)
                .setPermissionListener(object : PermissionListener {
                    override fun onPermissionGranted() {
                        photoPicker = TedBottomPicker.Builder(this@ChemistryActivity)
                                .setOnImageSelectedListener { uri -> viewModel.uploadPhoto(this@ChemistryActivity, uri) }
                                .create()
                        photoPicker?.show(supportFragmentManager)
                    }
                    override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {}
                })
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check()
    }

    fun startPhotoActivity(myPhotoUrl: String, evaluation: Boolean) {
        val intent = Intent(this, PhotoActivity::class.java)
        intent.putExtra("photoUrl", myPhotoUrl)
        intent.putExtra("evaluation", evaluation)
        startActivityForResult(intent, 1)
    }

    fun showVoiceDialog() {
        VoiceRecordDialog(null, false){ filePath, evalutaion ->
            viewModel.uploadVoice(this, filePath)
        }.show(supportFragmentManager, null)
    }

    fun showVoiceHearDialog(voiceUrl: String, evaluation: Boolean, myGender: Int) {
        VoiceRecordDialog(voiceUrl, evaluation){ filePath, result ->
            if(evaluation) { viewModel.like(this@ChemistryActivity, if(result) 1 else 0) }
        }.show(supportFragmentManager, null)
    }

    fun showAnswerDialog() {
        EnterCommentDialog{
            viewModel.answer(this@ChemistryActivity, it)
        }.show(supportFragmentManager, null)
    }

    private fun showCheckAnswerDialog(answer: String?, evaluation: Boolean, id: String?, name: String?) {
        AnswerCheckDialog(answer, evaluation, id, name){ result ->
            viewModel.like(this@ChemistryActivity, if(result) 1 else 0)
        }.show(supportFragmentManager, null)
    }

    fun showCheckListDialog(oxString: String, enableCheck: Boolean, myGender: Int) {
        FavorablityCheckListDialog(oxString, enableCheck){
            viewModel.setCheckList("${Couple.getGenderKey(myGender)}Ox", it)
        }.show(supportFragmentManager, null)
    }

    override fun onResume() {
        super.onResume()
        viewModel.login()
    }

    override fun onStop() {
        super.onStop()
        viewModel.logout()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try{
            super.onActivityResult(requestCode, resultCode, data)
        }catch (e: Exception){
            data?.data?.let {
                if (it.toString().startsWith("content://com.google.android.apps.photos.content")){
                    try {
                        val inputSteam = contentResolver.openInputStream(it)
                        if (inputSteam != null) {
                            val pictureBitmap = BitmapFactory.decodeStream(inputSteam)
                            val baos = ByteArrayOutputStream()
                            pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                            val path = MediaStore.Images.Media.insertImage(contentResolver, pictureBitmap, "img", null)

                            val realPath = RealPathUtil.getRealPath(this, Uri.parse(path))
                            val selectedImageUri = Uri.fromFile(File(realPath))
                            viewModel.uploadPhoto(this, selectedImageUri)
                            photoPicker?.dismiss()
                            return
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }
        }

        if(requestCode == 1) {
            val myGender = Me.value?.gender as Int
            if(resultCode == 2) {
                viewModel.like(this@ChemistryActivity, 1)
            }else if(resultCode == 1){
                viewModel.like(this@ChemistryActivity, 0)
            }
        }
    }
}