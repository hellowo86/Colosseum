package com.hellowo.colosseum.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.KeyEvent
import android.view.Window
import android.view.WindowManager

open class BaseDialog(val activity: Activity) : Dialog(activity) {

    fun showDialog(cancelable: Boolean, backgroundDim: Boolean, backgroundTrans: Boolean, touchableOutside: Boolean) {
        try {
            setCancelable(cancelable) // 백키로 종료하기
            setOnKeyListener { dialogInterface, i, keyEvent ->
                !cancelable && keyEvent.action == KeyEvent.KEYCODE_BACK
            }
            requestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀 숨기기
            if (!backgroundDim) { // 배경 어둡게 하기
                window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            }
            if (backgroundTrans) { // 배경 투명하게 하기
                window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            if (touchableOutside) { // 외부 터치 가능하게 하기
                window!!.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
            }
            show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}