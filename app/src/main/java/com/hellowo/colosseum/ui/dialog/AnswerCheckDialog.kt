package com.hellowo.colosseum.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.utils.makeMessageLastTimeText
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import jp.wasabeef.glide.transformations.CropCircleTransformation

@SuppressLint("ValidFragment")
class AnswerCheckDialog(private val answer: String?, private val evalutaion : Boolean, private val id: String?, private val name: String?,
                        private val dialogInterface: (Boolean) -> Unit) : BottomSheetDialog() {

    lateinit var profileImage: ImageView
    lateinit var nameText: TextView
    lateinit var messageText: TextView
    lateinit var evaluationLy: LinearLayout
    lateinit var likeBtn: FrameLayout
    lateinit var hateBtn: FrameLayout

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView = View.inflate(context, R.layout.dialog_answer_check, null)
        dialog.setContentView(contentView)
        (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))

        val layoutParams = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        sheetBehavior = layoutParams.behavior as BottomSheetBehavior<*>?
        if (sheetBehavior != null) {
            sheetBehavior?.setBottomSheetCallback(mBottomSheetBehaviorCallback)

            profileImage = contentView.findViewById(R.id.profileImage)
            nameText = contentView.findViewById(R.id.nameText)
            messageText = contentView.findViewById(R.id.messageText)
            evaluationLy = contentView.findViewById(R.id.evaluationLy)
            likeBtn = contentView.findViewById(R.id.likeBtn)
            hateBtn = contentView.findViewById(R.id.hateBtn)

            nameText.text = name
            messageText.text = answer

            Glide.with(context)
                    .load(makePublicPhotoUrl(id))
                    .bitmapTransform(CropCircleTransformation(context))
                    .placeholder(R.drawable.default_profile)
                    .into(profileImage)

            if(evalutaion) {
                evaluationLy.visibility = View.VISIBLE
                likeBtn.setOnClickListener {
                    dialogInterface.invoke(true)
                    dismiss()
                }
                hateBtn.setOnClickListener {
                    dialogInterface.invoke(false)
                    dismiss()
                }
            }else {
                evaluationLy.visibility = View.GONE
            }
        }
    }
}