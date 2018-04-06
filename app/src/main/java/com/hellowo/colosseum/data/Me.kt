package com.hellowo.colosseum.data

import android.arch.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.hellowo.colosseum.fcm.FirebaseInstanceIDService
import com.hellowo.colosseum.model.User

object Me : LiveData<User>() {
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val mAuthListener: FirebaseAuth.AuthStateListener = FirebaseAuth.AuthStateListener { auth ->
        auth.currentUser?.let {
            loadUser(it.uid)
            return@AuthStateListener
        }
        value = null
    }

    override fun onActive() {
        mAuth.addAuthStateListener(mAuthListener)
    }

    override fun onInactive() {
        mAuth.removeAuthStateListener(mAuthListener)
    }

    fun loadUser(id: String) {
        FirebaseFirestore.getInstance().collection("users").document(id).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    value = document.toObject(User::class.java)
                    if(value?.pushToken != FirebaseInstanceId.getInstance().token) {
                        FirebaseInstanceIDService.sendRegistrationToServer()
                    }
                    return@addOnCompleteListener
                }
            }
        }
    }

    fun push(user: User) { value = user }

    fun updateLastDtConnected() {
        value?.id?.let {
            FirebaseFirestore.getInstance().collection("users").document(it).update("dtConnected", System.currentTimeMillis())
        }
    }
}