package com.hellowo.colosseum.ui.dialog

import android.app.Dialog
import android.content.Context
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView

import com.hellowo.colosseum.R

class EnterCommentDialog : BottomSheetDialog() {
    private lateinit var dialogInterface: DialogInterface

    fun setDialogInterface(dialogInterface: DialogInterface) {
        this.dialogInterface = dialogInterface
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView = View.inflate(context, R.layout.dialog_input_text, null)
        dialog.setContentView(contentView)

        val layoutParams = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        sheetBehavior = layoutParams.behavior as BottomSheetBehavior<*>?
        if (sheetBehavior != null) {
            sheetBehavior.setBottomSheetCallback(mBottomSheetBehaviorCallback)

            val messageInput = contentView.findViewById<EditText>(R.id.messageInput)
            messageInput.setHint(R.string.enter_comment)

            val sendBtn = contentView.findViewById<TextView>(R.id.sendBtn)
            sendBtn.setOnClickListener {
                if (!TextUtils.isEmpty(messageInput.text)) {
                    dialogInterface.enterText(messageInput.text.toString())
                }
            }

            messageInput.postDelayed({
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(messageInput,
                        InputMethodManager.SHOW_IMPLICIT)
            }, 0)
        }
    }

    interface DialogInterface {
        fun enterText(text: String)
    }
}
