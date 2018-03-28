package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Couple
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.utils.log

class FavobalityTestViewModel : ViewModel() {
    val db = FirebaseFirestore.getInstance()
    var couple = MutableLiveData<Couple>()
    var loading = MutableLiveData<Boolean>()
    var myAudioFilePath = MutableLiveData<String>()

    init {
        loading.value = false
    }

    fun initCouple(couple: Couple) {
        this.couple.value = couple
    }

}