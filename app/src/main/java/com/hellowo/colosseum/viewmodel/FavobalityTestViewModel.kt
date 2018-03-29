package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Chat
import com.hellowo.colosseum.model.Couple
import com.hellowo.colosseum.model.Message
import com.hellowo.colosseum.utils.log
import com.hellowo.colosseum.utils.uploadPhoto
import java.util.*
import kotlin.collections.HashMap

class FavobalityTestViewModel : ViewModel() {
    val db = FirebaseFirestore.getInstance()
    var couple = MutableLiveData<Couple>()
    var loading = MutableLiveData<Boolean>()
    val isUploading = MutableLiveData<Boolean>()
    var myAudioFilePath = MutableLiveData<String>()

    init {
        loading.value = false
    }

    fun initCouple(c: Couple) {
        couple.value = c
    }

    fun loadCouple() {
        loading.value = true
        couple.value?.let {
            db.collection("couples").document(it.id!!).get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val item = task.result.toObject(Couple::class.java)
                            item.id = task.result.id
                            item.me = it.me
                            item.you = it.you
                            couple.value = item
                        }
                        loading.value = false
                    }
        }
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

    fun uploadPhoto(context: Context, uri: Uri?) {
        isUploading.value = true
        uri?.let {
            val myGender = couple.value?.me?.gender
            val filePath = "couplePhoto/${Couple.makePhotoUrlPath(couple.value?.me!!, couple.value?.you!!)}/$myGender"
            log(filePath)
            uploadPhoto(context, uri, filePath,
                    { snapshot, bitmap ->
                        snapshot.downloadUrl?.let{
                            val photoUrl = it.toString()
                            FirebaseFirestore.getInstance().collection("couples").document(couple.value?.id!!)
                                    .update("photoUrl$myGender", photoUrl).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    if(myGender == 0) {
                                        couple.value?.photoUrl0 = photoUrl
                                    }else {
                                        couple.value?.photoUrl1 = photoUrl
                                    }
                                    couple.value = couple.value
                                }
                                isUploading.value = false
                            }
                        }
                    },
                    { e ->
                        isUploading.value = false
                    }
            )
        }
    }

}