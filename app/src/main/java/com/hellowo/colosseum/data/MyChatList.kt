package com.hellowo.colosseum.data

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.hellowo.colosseum.model.Chat
import com.hellowo.colosseum.model.User
import java.util.*
import kotlin.collections.ArrayList
import java.nio.file.Files.exists
import com.hellowo.colosseum.utils.log


object MyChatList : LiveData<ArrayList<Chat>>() {
    var listener: ListenerRegistration? = null

    init {
        value = ArrayList()
    }

    override fun onActive() {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(Me.value?.id!!).collection("chats")
        listener = docRef.addSnapshotListener{ snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            snapshot.forEach {
                db.collection("chats").document(it.id).get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document != null && document.exists()) {
                            value?.add(document.toObject(Chat::class.java))
                        }
                    }
                }
                return@addSnapshotListener
            }
        }
    }

    override fun onInactive() {
        listener?.remove()
    }
}