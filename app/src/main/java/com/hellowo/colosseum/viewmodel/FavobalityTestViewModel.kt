package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Chat
import com.hellowo.colosseum.model.Couple
import com.hellowo.colosseum.model.Message
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.utils.log
import java.util.*
import kotlin.collections.HashMap

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

    fun createChat() {
        Me.value?.let {
            couple.value?.let{
                val db = FirebaseFirestore.getInstance()
                val batch = db.batch()

                val chatRef = db.collection("chats").document(it.id!!)
                val chat = Chat(id = it.id!!, title = it.you?.nickName, hostId = Me.value?.id,
                        dtCreated = System.currentTimeMillis(),
                        dtUpdated = System.currentTimeMillis())
                batch.set(chatRef, chat)

                val messageRef = db.collection("chats").document(chat.id!!).collection("messages").document(UUID.randomUUID().toString())
                val message = Message("", Me.value?.nickName, Me.value?.id, System.currentTimeMillis(), 1)
                batch.set(messageRef, message)

                val memberRef = db.collection("chats").document(chat.id!!).collection("members").document(Me.value?.id!!)
                batch.set(memberRef, Me.value?.makeChatMember()!!)

                val userChatRef = db.collection("users").document(Me.value?.id!!).collection("chats").document(chat.id!!)
                batch.set(userChatRef, HashMap<String, Any?>())

                batch.commit().addOnCompleteListener({

                })
            }
        }
    }

}