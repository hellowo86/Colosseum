package com.hellowo.colosseum.viewmodel

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.hellowo.colosseum.model.User
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
                        val user = auth.currentUser
                        user?.let {
                            FirebaseFirestore.getInstance().collection("users").document(user.uid).get().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val document = task.result
                                    if (document != null && document.exists()) {
                                        val value = document.toObject(User::class.java)
                                    }
                                }
                                loading.value = false
                            }.addOnFailureListener {
                                db.collection("users").document(user.uid).set(User(id = user.uid, createdTime = Date()))
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
