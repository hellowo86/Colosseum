package com.hellowo.colosseum.viewmodel

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.utils.log
import java.util.*

class SignUpViewModel : ViewModel() {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var loading = MutableLiveData<Boolean>()

    fun signIn(activity: Activity, acct: GoogleSignInAccount) {
        loading.value = true
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(activity, { task ->
                    if (task.isSuccessful && auth.currentUser != null) {
                        val fbUser = auth.currentUser
                        fbUser?.let {
                            FirebaseFirestore.getInstance().collection("users").document(fbUser.uid).get().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val document = task.result
                                    if (document != null && document.exists()) {
                                        val value = document.toObject(User::class.java)
                                    }else {
                                        Me.create(fbUser.uid)
                                    }
                                }
                                loading.value = false
                            }.addOnFailureListener {
                                loading.value = false
                            }
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        loading.value = false
                    }
                })
    }
}
