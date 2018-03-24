package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.hellowo.colosseum.App
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.fcm.FirebaseInstanceIDService
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.profileImgUrlPrefix
import com.hellowo.colosseum.utils.getPath
import com.hellowo.colosseum.utils.makeProfileBitmapFromFile
import com.hellowo.colosseum.utils.toast
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import com.hellowo.colosseum.R

class SplashViewModel : ViewModel() {
    var loading = MutableLiveData<Boolean>()
    var successLogin = MutableLiveData<Boolean>()


    fun signIn(context: Context, email: String, password: String) {
        loading.value = true
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener({ task ->
                    loading.value = false
                    if (!task.isSuccessful) {
                        try {
                            throw task.exception!!
                        } catch (e: FirebaseAuthInvalidUserException) {
                            toast(context, R.string.not_existed_user)
                        } catch (e: FirebaseAuthInvalidCredentialsException) {
                            toast(context, R.string.incorrect_password)
                        } catch (e: Exception) {
                            toast(context, R.string.signup_failed)
                        }
                    } else {
                        FirebaseInstanceIDService.sendRegistrationToServer()
                        successLogin.value = true
                    }
                })
    }

    fun signUp(context: Context, user: User, password: String, uri: Uri) {
        loading.value = true
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(user.email!!, password)
                .addOnCompleteListener({
                    if (!it.isSuccessful) {
                        try{
                            throw it.exception!!
                        } catch (e: FirebaseAuthUserCollisionException){
                            toast(context, R.string.existed_email)
                        } catch (e: Exception) {
                            toast(context, R.string.signup_failed)
                        }
                        loading.value = false
                    }else {
                        initUserProfile(context, it.result.user, user, uri)
                    }
                })
    }

    private fun initUserProfile(context: Context, auth: FirebaseUser, user: User, uri: Uri) {
        user.id = auth.uid
        try {
            val filePath = getPath(context, uri)
            val bitmap = makeProfileBitmapFromFile(filePath!!)
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("$profileImgUrlPrefix${user.id}.jpg")
            val baos = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 90, baos)
            val data = baos.toByteArray()
            val bis = ByteArrayInputStream(data)
            val uploadTask = storageRef.putStream(bis)

            uploadTask.addOnFailureListener { exception ->
                bitmap?.recycle()
            }.addOnSuccessListener { taskSnapshot ->
                bitmap?.recycle()
                FirebaseFirestore.getInstance().collection("users").document(user.id!!).set(user).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Me.push(user)
                        loading.value = false
                        successLogin.value = true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loading.value = false
        }
    }
}
