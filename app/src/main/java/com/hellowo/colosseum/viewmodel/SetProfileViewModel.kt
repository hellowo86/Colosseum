package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class SetProfileViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    init {
    }
}