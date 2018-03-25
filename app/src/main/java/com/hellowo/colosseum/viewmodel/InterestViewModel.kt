package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.utils.log

class InterestViewModel : ViewModel() {
    var interestList = MutableLiveData<ArrayList<User>>()
    var loading = MutableLiveData<Boolean>()

    init {
        interestList.value = ArrayList()
        loading.value = false
    }
}