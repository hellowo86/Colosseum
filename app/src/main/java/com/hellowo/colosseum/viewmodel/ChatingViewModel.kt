package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.net.Uri
import android.support.v4.util.ArrayMap
import com.google.firebase.firestore.*
import com.hellowo.colosseum.App
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Chat
import com.hellowo.colosseum.model.ChatMember
import com.hellowo.colosseum.model.Message
import com.hellowo.colosseum.pushServerKey
import com.hellowo.colosseum.utils.uploadPhoto
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import com.google.android.gms.tasks.Task
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentReference
import android.provider.SyncStateContract.Helpers.update
import com.google.firebase.firestore.WriteBatch
import com.hellowo.colosseum.utils.log


class ChatingViewModel : ViewModel() {
    val chat = MutableLiveData<Chat>()
    val messages = MutableLiveData<ArrayList<Message>>()
    val newMessage = MutableLiveData<Message>()
    val typings = MutableLiveData<ArrayList<String>>()
    val members = MutableLiveData<ArrayMap<String, ChatMember>>()
    val isUploading = MutableLiveData<Boolean>()
    val outOfChat = MutableLiveData<Boolean>()

    val ref = FirebaseFirestore.getInstance().collection("chats")
    var lastVisibleSnapshot: DocumentSnapshot? = null
    var chatLoading = MutableLiveData<Boolean>()
    var messagesLoading = MutableLiveData<Boolean>()
    var membersLoading = MutableLiveData<Boolean>()
    var isTyping = false
    var isOut = false
    val limit = 100L
    var chatId: String = ""
    var lastConnectedTime: Long = 0

    private var messageListenerRegistration: ListenerRegistration? = null
    private var typingListenerRegistration: ListenerRegistration? = null
    private var membersListenerRegistration: ListenerRegistration? = null

    fun initChat(chatId: String, lastConnectedTime: Long) {
        this.chatId = chatId
        this.lastConnectedTime = lastConnectedTime

        messages.value = ArrayList()
        typings.value = ArrayList()
        members.value = ArrayMap()

        loadChat()
        loadMessages(chatId, null)
        setMessageListListener()
        setTypingListListener()
        setMembersListListener()
    }

    private fun loadChat() {
        log("loadChat")
        chatLoading.value = true
        ref.document(chatId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                chat.value = task.result.toObject(Chat::class.java)
            }
            chatLoading.value = false
        }
    }

    private fun loadMessages(chatId: String, lastTime: Double?) {
        log("loadMessages")
        messagesLoading.value = true
        var query = ref.document(chatId).collection("messages").orderBy("dtCreated", Query.Direction.DESCENDING)
        lastVisibleSnapshot?.let { query = query.startAfter(it) }
        lastTime?.let { query = query.endAt(it) }
        query.limit(limit).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result.documents.forEach {
                    messages.value?.add(it.toObject(Message::class.java))
                }
                if(task.result.documents.isNotEmpty()) {
                    lastVisibleSnapshot = task.result.documents.last()
                }
            }
            messages.value = messages.value
            messagesLoading.value = false
        }
    }

    private fun setMessageListListener() {
        messageListenerRegistration = ref.document(chatId).collection("messages")
                .whereGreaterThan("dtCreated", System.currentTimeMillis())
                .orderBy("dtCreated").addSnapshotListener { snapshots, e ->
                    if (e == null) {
                        snapshots.documentChanges.forEach {
                            val message = it.document.toObject(Message::class.java)
                            when{
                                it.type == DocumentChange.Type.ADDED -> {
                                    messages.value?.add(0, message)
                                    newMessage.value = message
                                }
                            }
                        }
                    }
                }
    }

    private fun setTypingListListener() {

    }

    private fun setMembersListListener() {
        membersLoading.value = true
        membersListenerRegistration = ref.document(chatId).collection("members").addSnapshotListener { snapshots, e ->
            if (e == null) {
                snapshots.documentChanges.forEach {
                    val chatMember = it.document.toObject(ChatMember::class.java)
                    when{
                        it.type == DocumentChange.Type.ADDED -> {
                            members.value?.put(chatMember.userId, chatMember)
                        }
                        it.type == DocumentChange.Type.REMOVED -> {
                            members.value?.remove(chatMember.userId)
                        }
                        it.type == DocumentChange.Type.MODIFIED -> {
                            members.value?.put(chatMember.userId, chatMember)
                        }
                    }
                }
                members.value = members.value
            }
            membersLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        messageListenerRegistration?.remove()
        typingListenerRegistration?.remove()
        membersListenerRegistration?.remove()
    }

    fun loadMoreMessages() {
        loadMessages(chatId, null)
    }

    fun postMessage(text: String, type: Int, dataUri: String?, onSuccess: Runnable?) {
        chat.value?.let {
            val message = Message(text, Me.value?.nickName, Me.value?.id, System.currentTimeMillis(), type, dataUri)
            ref.document(chatId).collection("messages").document(UUID.randomUUID().toString()).set(message).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sendPushMessage(message)
                    onSuccess?.run()
                }
            }
        }
    }

    private fun sendPushMessage(message: Message) {
        Thread {
            members.value?.forEach {
                try {
                    if(!it.value.live && !(it.value.userId.equals(Me.value?.id))) {
                        it.value.pushToken?.let{ pushToken->
                            val data = JSONObject()
                            data.put("pushType", 0)
                            data.put("userId", message.userId)
                            data.put("userName", message.userName)
                            data.put("message", message.text)
                            data.put("chatId", chatId)

                            val bodyBuilder = FormBody.Builder()
                            bodyBuilder.add("to", pushToken)
                            bodyBuilder.add("data", data.toString())
                            val request = Request.Builder()
                                    .url("https://fcm.googleapis.com/fcm/send")
                                    .addHeader("Authorization", pushServerKey)
                                    .post(bodyBuilder.build())
                                    .build()
                            OkHttpClient().newCall(request).execute()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()

    }

    fun typingText(text: CharSequence) {
        if((text.isNotEmpty() && !isTyping) || (text.isEmpty() && isTyping)) {
            isTyping = !isTyping
            //ref.child(KEY_TYPING).child(chatId).child(me?.id).setValue(isTyping)
        }
    }

    fun getlastPostionDate(itemPos: Int) = messages.value?.get(itemPos)?.dtCreated

    fun loginChat() {
        val data = HashMap<String, Any?>()
        data.put("live", true)
        data.put("lastConnectedTime", System.currentTimeMillis())
        ref.document(chatId).collection("members").document(Me.value?.id!!).update(data)
    }

    fun logoutChat() {
        if(!isOut) {
            isTyping = false
            val data = HashMap<String, Any?>()
            data.put("live", false)
            data.put("lastConnectedTime", System.currentTimeMillis())
            data.put("pushToken", Me.value?.pushToken)
            ref.document(chatId).collection("members").document(Me.value?.id!!).update(data)
        }
    }

    fun outOfChat() {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        val messageRef = ref.document(chatId).collection("messages").document(UUID.randomUUID().toString())
        val message = Message("", Me.value?.nickName, Me.value?.id, System.currentTimeMillis(), 2)
        batch.set(messageRef, message)

        val memberRef = ref.document(chatId).collection("members").document(Me.value?.id!!)
        batch.delete(memberRef)

        val userChatRef = db.collection("users").document(Me.value?.id!!).collection("chats").document(chatId)
        batch.delete(userChatRef)

        batch.commit().addOnCompleteListener({
            isOut = true
            outOfChat.value = isOut
        })
    }

    fun sendPhotoMessage(context: Context, uri: Uri?) {
        uri?.let {
            isUploading.value = true
            uploadPhoto(context, uri, "chatPhoto/$chatId/${System.currentTimeMillis()}",
                    { taskSnapshot, bitmap ->
                        val json = JSONObject()
                        json.put("w", bitmap?.width)
                        json.put("h", bitmap?.height)
                        taskSnapshot.downloadUrl?.let{
                            json.put("url", it.toString())
                            postMessage(context.getString(R.string.photo), 3, json.toString(), Runnable{ isUploading.value = false })
                        }
                    },
                    { e ->
                        isUploading.value = false
                    }
            )
        }
    }
}