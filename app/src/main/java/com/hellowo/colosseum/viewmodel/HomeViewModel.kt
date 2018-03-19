package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.hellowo.colosseum.model.Chat
import com.hellowo.colosseum.utils.log


class HomeViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    val loading = MutableLiveData<Boolean>()
    val chatList = MutableLiveData<ArrayList<Chat>>()

    init {
        chatList.value = ArrayList()
    }

    fun search(searchText: String) {
        loading.value = true
        if(searchText.isNotEmpty()) {
             db.collection("chats").whereEqualTo("s_$searchText", true).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    chatList.value?.clear()
                    task.result.map { it.toObject(Chat::class.java) }
                            .forEach { chatList.value?.add(it) }
                    chatList.value = chatList.value
                } else {
                    log("Error getting documents: " + task.exception)
                }
                loading.value = false
            }
        }
    }
}