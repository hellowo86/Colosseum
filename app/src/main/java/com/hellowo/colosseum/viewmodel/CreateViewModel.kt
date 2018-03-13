package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.hellowo.colosseum.R
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.utils.isEmailValid

class CreateViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    var loading = MutableLiveData<Boolean>()

    init {
        initOpinion()
    }

    private fun initOpinion() {

    }
}