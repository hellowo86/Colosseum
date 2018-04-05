package com.hellowo.colosseum.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.support.v7.util.SortedList
import android.util.ArrayMap
import android.view.ViewAnimationUtils
import com.google.firebase.firestore.*
import com.hellowo.colosseum.model.Chat
import com.hellowo.colosseum.model.Couple
import com.hellowo.colosseum.model.Message
import com.hellowo.colosseum.model.MyChat
import com.hellowo.colosseum.utils.log
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashSet

object MyChatList : LiveData<ArrayList<MyChat>>() {
    val chatMap = HashMap<String, MyChat>()
    val listenerMap = HashMap<String, ListenerRegistration>()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    init {
        value = ArrayList()
    }

    override fun onActive() {
        loadChatList()
        log("MyChatList onActive")
    }

    override fun onInactive() {
        listenerMap.forEach { it.value.remove() }
        listenerMap.clear()
        log("MyChatList onInactive")
    }

    fun loadChatList() {
        loading.value = true
        val deleteMap = HashSet<String>()
        deleteMap.addAll(chatMap.keys)
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(Me.value?.id!!).collection("chats").get().addOnCompleteListener { task ->
            if(task.isSuccessful) {
                if(task.result.size() > 0) {
                    val totalSize = task.result.size()
                    var count = 0

                    task.result.forEach { doc ->
                        val chatId = doc.id
                        deleteMap.remove(chatId)
                        db.collection("chats").document(chatId).get().addOnCompleteListener { task ->
                            count++
                            if(task.isSuccessful) {
                                val yourGenderKey = Couple.getGenderKey(Me.value?.getYourGender()!!)
                                val myChat: MyChat
                                val title = task.result.getString("${yourGenderKey}Name")
                                val hostId = task.result.getString("${yourGenderKey}Id")
                                if(chatMap.containsKey(chatId)) {
                                    myChat = chatMap[chatId]!!
                                    myChat.title = title
                                    myChat.hostId = hostId
                                }else {
                                    myChat = MyChat(id = chatId, title = title, hostId = hostId)
                                    value?.add(myChat)
                                }
                                chatMap.put(chatId, myChat)

                                if(!listenerMap.containsKey(chatId)) {
                                    val listener = db.collection("chats").document(chatId).collection("messages")
                                            .orderBy("dtCreated", Query.Direction.DESCENDING).limit(1).addSnapshotListener{ snapshots, e ->
                                        if (e != null) {
                                            return@addSnapshotListener
                                        }
                                        snapshots.documentChanges.forEach {
                                            val message = it.document.toObject(Message::class.java)
                                            when{
                                                it.type == DocumentChange.Type.ADDED -> {
                                                    value?.filter { it.id == myChat.id }?.forEach {
                                                        it.lastMessage = message.text
                                                        it.lastMessageTime = message.dtCreated
                                                    }
                                                }
                                            }
                                            sort()
                                            value = value
                                        }
                                    }
                                    listenerMap.put(chatId, listener)
                                }
                            }

                            if(count == totalSize) {
                                sort()
                                value = value
                                loading.value = false
                            }
                        }
                    }

                    deleteMap.forEach {
                        try{
                            listenerMap.remove(it)
                            value?.remove(chatMap.remove(it))
                        }catch (e: Exception){}
                    }
                }else {
                    chatMap.clear()
                    listenerMap.clear()
                    value?.clear()
                    value = value
                    loading.value = false
                }
            }
        }
    }

    private fun sort() {
        Collections.sort(value) { p0, p1 ->
            return@sort if(p0.lastMessageTime > p1.lastMessageTime) -1 else if(p0.lastMessageTime < p1.lastMessageTime) 1 else 0
        }
    }
}