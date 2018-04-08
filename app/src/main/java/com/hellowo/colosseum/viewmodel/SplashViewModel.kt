package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.net.Uri
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.fcm.FirebaseInstanceIDService
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.utils.toast
import com.hellowo.colosseum.utils.uploadPhoto

class SplashViewModel : ViewModel() {
    var loading = MutableLiveData<Boolean>()

    fun signIn(context: Context, email: String, password: String) {
        loading.value = true
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener({ task ->
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
                        loading.value = false
                    } else {
                        FirebaseInstanceIDService.sendRegistrationToServer()
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
        uploadPhoto(context, uri, "profileImg/${user.id}",
                { snapshot, bitmap ->
                    val data = user.makeMap()
                    data.put("coin", 30)
                    FirebaseFirestore.getInstance().collection("users").document(user.id!!).set(data).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            loading.value = false
                            Me.push(user)
                        }
                    }
                },
                { e ->
                    loading.value = false
                }
        )
    }
}
