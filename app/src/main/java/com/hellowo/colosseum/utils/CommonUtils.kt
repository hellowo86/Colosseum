package com.hellowo.colosseum.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.support.v7.app.AlertDialog
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import com.hellowo.colosseum.App
import com.hellowo.colosseum.R

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

fun makePublicPhotoUrl(userId: String?): String = "https://firebasestorage.googleapis.com/v0/b/colosseum-eb02c.appspot.com/o/profileImg%2F${userId}.jpg?alt=media"

fun toast(context: Context, stringId: Int) {
    Toast.makeText(context, stringId, Toast.LENGTH_SHORT).show()
}

fun distFrom(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val earthRadius = 3958.75
    val dLat = Math.toRadians(lat2-lat1)
    val dLng = Math.toRadians(lng2-lng1)
    val a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                    Math.sin(dLng/2) * Math.sin(dLng/2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
    return earthRadius * c
}

fun dpToPx(context: Context, dp: Float): Int {
    val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    return px.toInt()
}

fun runCallbackAfterViewDrawed(view: View, callback: Runnable) {
    view.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                @SuppressLint("ObsoleteSdkInt")
                override fun onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    } else {
                        view.viewTreeObserver.removeGlobalOnLayoutListener(this)
                    }
                    callback.run()
                }
            })
}


