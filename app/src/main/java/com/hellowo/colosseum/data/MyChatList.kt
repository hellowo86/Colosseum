package com.hellowo.colosseum.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.ArrayMap
import android.view.ViewAnimationUtils
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.hellowo.colosseum.model.Chat
import com.hellowo.colosseum.model.MyChat
import com.hellowo.colosseum.utils.log

object MyChatList : LiveData<ArrayMap<String, MyChat>>() {
    private val listener = EventListener<QuerySnapshot> { snapshots, e ->
        if (e != null) {
            return@EventListener
        }
        snapshots.documentChanges.forEach {
            val chatId = it.document.id
            when{
                it.type == DocumentChange.Type.ADDED -> {
                    value?.contains(chatId)
                }
                it.type == DocumentChange.Type.REMOVED -> {
                    value?.remove(chatId)
                }
                it.type == DocumentChange.Type.MODIFIED -> {

                }
            }
        }
    }

    val listenerMap = HashMap<String, ListenerRegistration>()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    init {
        value = ArrayMap()
    }

    override fun onActive() {
        loadChatList()
    }

    override fun onInactive() {
        listenerMap.forEach { it.value.remove() }
        listenerMap.clear()
    }

    fun loadChatList() {
        loading.value = true
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(Me.value?.id!!).collection("chats").get().addOnCompleteListener { task ->
            if(task.isSuccessful) {
                value?.clear()
                if(task.result.size() > 0) {
                    val totalSize = task.result.size()
                    var count = 0
                    task.result.forEach { doc ->
                        db.collection("chats").document(doc.id).get().addOnCompleteListener { task ->
                            count++
                            if(task.isSuccessful) {
                                val myChat = MyChat(id = task.result.id, title = task.result.getString("title"), hostId = task.result.getString("hostId"))
                                value?.put(myChat.id, myChat)
                            }
                            if(count == totalSize) {
                                value = value
                                loading.value = false
                            }
                        }
                    }
                }else {
                    value = value
                    loading.value = false
                }
            }
        }
    }
}