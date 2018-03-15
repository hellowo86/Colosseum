package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore


class MainViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    var loading = MutableLiveData<Boolean>()

    init {
    }

}