package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Chat
import com.hellowo.colosseum.model.Couple
import com.hellowo.colosseum.model.Message
import com.hellowo.colosseum.ui.activity.FavorabilityTestActivity
import com.hellowo.colosseum.utils.log
import com.hellowo.colosseum.utils.uploadFile
import com.hellowo.colosseum.utils.uploadPhoto
import java.util.*
import kotlin.collections.HashMap

class FavobalityTestViewModel : ViewModel() {
    val db = FirebaseFirestore.getInstance()
    var couple = MutableLiveData<Couple>()
    var loading = MutableLiveData<Boolean>()
    val isUploading = MutableLiveData<Boolean>()
    var myAudioFilePath = MutableLiveData<String>()
    var coupleId: String? = null
    private var coupleListenerRegistration: ListenerRegistration? = null

    init {
        loading.value = false
    }

    fun initCouple(id: String?) {
        coupleId = id
        loading.value = true
        coupleId?.let {
            coupleListenerRegistration = db.collection("couples").document(it).addSnapshotListener { snapshot, e ->
                if (e == null) {
                    val item = snapshot.toObject(Couple::class.java)
                    item.id = snapshot.id
                    couple.value = item
                }
                loading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        coupleListenerRegistration?.remove()
    }

    fun createChat() {
        isUploading.value = true
        Me.value?.let {
            couple.value?.let{
                val db = FirebaseFirestore.getInstance()
                val batch = db.batch()

                val chatRef = db.collection("chats").document(it.id!!)
                val chat = Chat(id = it.id!!, title = "!!!", hostId = Me.value?.id,
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
                    isUploading.value = false
                })
            }
        }
    }

    fun like(likeKey: String, myLike: Int) {
        loading.value = true
        val data = HashMap<String, Any?>()
        data.put(likeKey, myLike)
        FirebaseFirestore.getInstance().collection("couples").document(couple.value?.id!!).update(data).addOnCompleteListener { task ->
            if (task.isSuccessful) {}
            isUploading.value = false
        }
    }

    fun setCheckList(checkListKey: String, oxString: String) {
        loading.value = true
        val data = HashMap<String, Any?>()
        data.put(checkListKey, oxString)
        FirebaseFirestore.getInstance().collection("couples").document(couple.value?.id!!).update(data).addOnCompleteListener { task ->
            if (task.isSuccessful) {}
            isUploading.value = false
        }
    }

    fun uploadPhoto(context: Context, uri: Uri?) {
        isUploading.value = true
        uri?.let {
            val myGender = Me.value?.gender as Int
            val fileName = "couplePhoto/$coupleId/$myGender"
            log(fileName)
            uploadPhoto(context, uri, fileName,
                    { snapshot, bitmap ->
                        snapshot.downloadUrl?.let{
                            val photoUrl = it.toString()
                            FirebaseFirestore.getInstance().collection("couples").document(couple.value?.id!!)
                                    .update("${Couple.getGenderKey(myGender)}PhotoUrl", photoUrl).addOnCompleteListener { task ->
                                if (task.isSuccessful) {}
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

    fun uploadVoice(context: Context, filePath: String) {
        isUploading.value = true
        val myGender = Me.value?.gender as Int
        val fileName = "coupleVoice/$coupleId/$myGender"
        uploadFile(context, filePath, fileName,
                { snapshot ->
                    snapshot.downloadUrl?.let{
                        val voiceUrl = it.toString()
                        FirebaseFirestore.getInstance().collection("couples").document(couple.value?.id!!)
                                .update("${Couple.getGenderKey(myGender)}VoiceUrl", voiceUrl).addOnCompleteListener { task ->
                            if (task.isSuccessful) {}
                            isUploading.value = false
                        }
                    }
                },
                { e ->
                    isUploading.value = false
                })
    }

}