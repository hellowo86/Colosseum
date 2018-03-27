package com.hellowo.colosseum.ui.dialog

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Couple
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import com.hellowo.colosseum.utils.runCallbackAfterViewDrawed
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.dialog_interest_completed.*

class InterestCompletedDialog(activity: Activity, private val you: User) : BaseDialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_interest_completed)
        setLayout()

    }

    private fun setLayout() {
        val lp = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        rootLy.layoutParams = lp
        runCallbackAfterViewDrawed(rootLy, Runnable{

        })
        Glide.with(context).load(makePublicPhotoUrl(Me.value?.id))
                .bitmapTransform(CropCircleTransformation(context))
                .placeholder(Me.value?.getDefaultImgId()!!).into(profileImg)
        Glide.with(context).load(makePublicPhotoUrl(you.id))
                .bitmapTransform(CropCircleTransformation(context))
                .placeholder(you.getDefaultImgId()).into(youImg)

        rootLy.postDelayed({ dismiss() }, 5000)
    }

}