package com.hellowo.colosseum.data

import android.arch.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


object Me : LiveData<FirebaseUser>() {
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val mAuthListener: FirebaseAuth.AuthStateListener = FirebaseAuth.AuthStateListener { auth ->
        value = auth.currentUser
    }

    override fun onActive() {
        mAuth.addAuthStateListener(mAuthListener)
    }

    override fun onInactive() {
        mAuth.removeAuthStateListener(mAuthListener)
    }
}