package com.hellowo.colosseum

import android.app.Activity
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.util.Log

fun log(text: String){
    Log.d("aaa", text)
}

fun log(text: Int){
    Log.d("aaa", text.toString())
}

fun showAlertDialog(activity: Activity, title: String, message: String,
                    positiveListener: DialogInterface.OnClickListener?,
                    negativeListener: DialogInterface.OnClickListener?,
                    iconResourceId: Int) {
    val alertdialog = AlertDialog.Builder(activity)
    alertdialog.setMessage(message)

    if (positiveListener != null) {
        alertdialog.setPositiveButton(R.string.ok, positiveListener)
    }

    if (negativeListener != null) {
        alertdialog.setNegativeButton(R.string.cancel, negativeListener)
    }

    val alert = alertdialog.create()
    alert.setTitle(title)

    if (iconResourceId != 0) {
        alert.setIcon(iconResourceId)
    }

    alert.show()
}