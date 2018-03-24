package com.hellowo.colosseum.data

import android.arch.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hellowo.colosseum.model.User
import java.util.*

object Me : LiveData<User>() {
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val mAuthListener: FirebaseAuth.AuthStateListener = FirebaseAuth.AuthStateListener { auth ->
        auth.currentUser?.let {
            FirebaseFirestore.getInstance().collection("users").document(it.uid).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        value = document.toObject(User::class.java)
                        return@addOnCompleteListener
                    }
                }
                value = null
            }
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

    fun push(user: User) { value = user }
}