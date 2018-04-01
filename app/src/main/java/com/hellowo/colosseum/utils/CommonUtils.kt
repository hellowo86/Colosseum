package com.hellowo.colosseum.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.hellowo.colosseum.R
import com.hellowo.colosseum.storageUrl
import gun0912.tedbottompicker.TedBottomPicker
import java.io.*
import java.util.ArrayList

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

fun makePublicPhotoUrl(userId: String?): String = "https://firebasestorage.googleapis.com/v0/b/colosseum-eb02c.appspot.com/o/profileImg%2F$userId.jpg?alt=media"

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

fun dpToPx(context: Context, dp: Float): Float {
    val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    return px
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

fun showPhotoPicker(activity: AppCompatActivity, onComplete: (Uri) -> Unit) {
    TedPermission(activity)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    val bottomSheetDialogFragment = TedBottomPicker.Builder(activity)
                            .setOnImageSelectedListener { onComplete.invoke(it) }
                            .setPreviewMaxCount(100)
                            .create()
                    bottomSheetDialogFragment.show(activity.supportFragmentManager)
                }
                override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {}
            })
            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .check()
}

private fun checkExternalStoragePermission() {

}

fun uploadPhoto(context: Context, uri: Uri, fileName: String, onSuccess: (UploadTask.TaskSnapshot, Bitmap?) -> Unit, onFailed: (Exception) -> Unit) {
    try {
        val filePath = getPath(context, uri)
        val bitmap = makeProfileBitmapFromFile(filePath!!)
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("$storageUrl$fileName.jpg")
        val baos = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        val data = baos.toByteArray()
        val bis = ByteArrayInputStream(data)
        val uploadTask = storageRef.putStream(bis)

        uploadTask.addOnFailureListener { e ->
            onFailed.invoke(e)
            bitmap?.recycle()
        }.addOnSuccessListener { taskSnapshot ->
            onSuccess.invoke(taskSnapshot, bitmap)
            bitmap?.recycle()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        onFailed.invoke(e)
    }
}

fun uploadFile(context: Context, filePath: String, fileName: String, onSuccess: (UploadTask.TaskSnapshot) -> Unit, onFailed: (Exception) -> Unit) {
    try {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("$storageUrl$fileName")
        val file = File(filePath)
        val bis = ByteArrayInputStream(file.readBytes())
        val uploadTask = storageRef.putStream(bis)
        uploadTask.addOnFailureListener { e ->
            onFailed.invoke(e)
        }.addOnSuccessListener { taskSnapshot ->
            onSuccess.invoke(taskSnapshot)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        onFailed.invoke(e)
    }
}


