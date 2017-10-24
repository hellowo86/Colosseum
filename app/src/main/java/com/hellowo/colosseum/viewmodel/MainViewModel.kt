package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.hellowo.colosseum.model.Issue


class MainViewModel : ViewModel() {
    val db = FirebaseFirestore.getInstance()
    var loading = MutableLiveData<Boolean>()
    var issue = MutableLiveData<Issue>()

    init {
        loading.value = true
        val docRef = db.collection("issue").document("00000000")
        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null) {
                    Log.d("MainViewModel.init()", "DocumentSnapshot data: " + document.data)
                    issue.value = document.toObject(Issue::class.java)
                } else {
                    Log.d("MainViewModel.init()", "No such document")
                }
            } else {
                Log.d("MainViewModel.init()", "get failed with ", task.exception)
            }
            loading.value = false
        }
    }
}