package com.hellowo.colosseum.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.hellowo.colosseum.storageUrl
import com.hellowo.colosseum.ui.activity.PhotoActivity
import com.hellowo.colosseum.ui.activity.UserActivity
import gun0912.tedbottompicker.TedBottomPicker
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.ArrayList


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

fun uploadPhoto(context: Context, uri: Uri, fileName: String, onSuccess: (UploadTask.TaskSnapshot, Bitmap?) -> Unit, onFailed: (Exception) -> Unit) {
    try {
        val filePath = getPath(context, uri)
        val bitmap = makeProfileBitmapFromFile(filePath!!)
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("${storageUrl}$fileName.jpg")
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
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("${storageUrl}$fileName")
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

fun startUserActivity(activity: Activity, userId: String) {
    val intent = Intent(activity, UserActivity::class.java)
    intent.putExtra("userId", userId)
    activity.startActivity(intent)
}

fun startPhotoActivity(activity: Activity, photoUrl: String) {
    val intent = Intent(activity, PhotoActivity::class.java)
    intent.putExtra("photoUrl", photoUrl)
    activity.startActivity(intent)
}
