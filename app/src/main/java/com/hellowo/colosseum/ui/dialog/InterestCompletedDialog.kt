package com.hellowo.colosseum.ui.dialog

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Couple
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.utils.*
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.dialog_interest_completed.*
import android.view.Gravity
import android.view.Window

class InterestCompletedDialog(activity: Activity, private val you: User) : BaseDialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val wlp = window.attributes
        wlp.gravity = Gravity.TOP
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
        window.attributes = wlp
        setContentView(R.layout.dialog_interest_completed)
        setLayout()

    }

    private fun setLayout() {
        cardView.visibility = View.INVISIBLE
        Glide.with(context).load(makePublicPhotoUrl(you.id))
                .bitmapTransform(CropCircleTransformation(context))
                .placeholder(User.getDefaultImgId(you.gender)).into(profileImg)
        setTextColor(titleText, String.format(context.getString(R.string.interest_completed), you.nickName),
                0, you.nickName?.length!!, context.resources.getColor(R.color.colorPrimary))
        subText.text = context.getString(R.string.interest_completed_sub)
        runCallbackAfterViewDrawed(cardView, Runnable{
            cardView.postDelayed({
                cardView.visibility = View.VISIBLE
                slideFromTop(cardView, cardView.height.toFloat() + dpToPx(context, 15f))
                cardView.postDelayed({
                    slideToTop(cardView, cardView.height.toFloat() + dpToPx(context, 15f))
                    cardView.postDelayed({
                        dismiss()
                    }, 250)
                }, 3000)
            }, 100)
        })
    }

}