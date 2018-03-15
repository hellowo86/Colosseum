package com.hellowo.colosseum.ui.activity

import android.Manifest
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.profileImgUrlPrefix
import com.hellowo.colosseum.storageUrl
import com.hellowo.colosseum.utils.getPath
import com.hellowo.colosseum.utils.makeProfileBitmapFromFile
import gun0912.tedbottompicker.TedBottomPicker
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_set_profile.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URL
import java.util.*

class SetProfileActivity : BaseActivity() {
    var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_profile)
        initLayout()
        initObserve()
    }

    private fun initLayout() {
        profileImg.setOnClickListener(this::checkExternalStoragePermission)
        saveBtn.setOnClickListener{ saveUserData() }
    }

    private fun initObserve() {}

    private val permissionlistener = object : PermissionListener {
        override fun onPermissionGranted() { showPhotoPicker() }
        override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {}
    }

    private fun checkExternalStoragePermission(view: View) {
        TedPermission(view.context)
                .setPermissionListener(permissionlistener)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check()
    }

    private fun showPhotoPicker() {
        val bottomSheetDialogFragment = TedBottomPicker.Builder(this)
                .setOnImageSelectedListener{
                    uri = it
                    Glide.with(this).load(it).bitmapTransform(CropCircleTransformation(this)).into(profileImg)
                }
                .create()
        bottomSheetDialogFragment.show(supportFragmentManager)
    }

    private fun saveUserData() {
        if(uri == null) {

        }else if(nickNameEdit.text.length < 2) {

        }else {
            showProgressDialog()
            try {
                val filePath = getPath(this, uri!!)
                val bitmap = makeProfileBitmapFromFile(filePath!!)
                val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("$profileImgUrlPrefix${Me.value?.id}.jpg")

                val baos = ByteArrayOutputStream()
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                val bis = ByteArrayInputStream(data)

                val uploadTask = storageRef.putStream(bis)

                uploadTask.addOnFailureListener { exception ->
                    bitmap?.recycle()
                    hideProgressDialog()
                }.addOnSuccessListener { taskSnapshot ->
                    bitmap?.recycle()
                    Me.value?.let {
                        it.nickName = nickNameEdit.text.toString()
                        it.photoUrl = taskSnapshot.downloadUrl.toString()
                        Me.update(Runnable { finish() })
                    }
                    hideProgressDialog()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                hideProgressDialog()
            }
        }
    }
}