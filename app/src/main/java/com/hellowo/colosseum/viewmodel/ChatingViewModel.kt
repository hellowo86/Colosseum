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
import com.google.firebase.iid.FirebaseInstanceId
import com.hellowo.colosseum.utils.log
import java.text.DateFormat


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
    var lastReadPosition = MutableLiveData<Int>()
    var isTyping = false
    var isOut = false
    val limit = 100L
    var chatId: String = ""
    var myLastConnectedTime: Long = Long.MAX_VALUE

    private var messageListenerRegistration: ListenerRegistration? = null
    private var typingListenerRegistration: ListenerRegistration? = null
    private var membersListenerRegistration: ListenerRegistration? = null

    fun initChat(id: String) {
        lastReadPosition.value = -1
        messagesLoading.value = true
        chatId = id
        messages.value = ArrayList()
        typings.value = ArrayList()
        members.value = ArrayMap()
        loadChat()
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

    private fun loadMessages(lastTime: Double?) {
        log("loadMessages")
        messagesLoading.value = true
        var query = ref.document(chatId).collection("messages").orderBy("dtCreated", Query.Direction.DESCENDING)
        lastVisibleSnapshot?.let { query = query.startAfter(it) }
        query = if(lastTime == null) {
            query.limit(limit)
        }else {
            query.endAt(lastTime)
        }
        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result.documents.forEach {
                    messages.value?.add(it.toObject(Message::class.java))
                }

                if(task.result.documents.isNotEmpty()) {
                    val lastSnapshot = task.result.documents.last()
                    if(lastVisibleSnapshot == null && myLastConnectedTime < lastSnapshot.getLong("dtCreated")) {
                        lastVisibleSnapshot = lastSnapshot
                        loadMessages(myLastConnectedTime.toDouble())
                        return@addOnCompleteListener
                    }
                    lastVisibleSnapshot = lastSnapshot
                }

                messages.value = messages.value

                if(lastReadPosition.value == -1 && messages.value?.size!! > 0) {
                    if(lastTime != null) {
                        lastReadPosition.value = messages.value?.size!! - 1
                    }else {
                        log("myLastConnectedTime" + DateFormat.getDateTimeInstance().format(Date(myLastConnectedTime)))
                        messages.value?.firstOrNull { it.dtCreated < myLastConnectedTime }?.let {
                            log(it.toString())
                            val pos = messages.value?.indexOf(it)
                            log(""+pos)
                            if(pos!! > 5) {
                                lastReadPosition.value = pos
                            }
                        }
                    }
                }
            }
            messagesLoading.value = false
        }
    }

    private fun setMessageListListener() {
        messageListenerRegistration?.remove()
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
        membersListenerRegistration?.remove()
        membersListenerRegistration = ref.document(chatId).collection("members").addSnapshotListener { snapshots, e ->
            if (e == null) {
                snapshots.documentChanges.forEach {
                    val chatMember = it.document.toObject(ChatMember::class.java)
                    when{
                        it.type == DocumentChange.Type.ADDED -> {
                            members.value?.put(chatMember.userId, chatMember)
                            if(chatMember.userId == Me.value?.id) {
                                myLastConnectedTime = chatMember.lastConnectedTime
                            }
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
                if(lastVisibleSnapshot == null) {
                    loadMessages(null)
                }
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
        loadMessages(null)
    }

    fun postMessage(text: String, type: Int, dataUri: String?, onSuccess: Runnable?) {
        chat.value?.let {
            val message = Message(text, Me.value?.nickName, Me.value?.id, System.currentTimeMillis(), type, dataUri)
            log(message.toString())
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
            members.value?.forEach { member ->
                try {
                    if(!member.value.live && !(member.value.userId.equals(Me.value?.id))) {
                        member.value.pushToken?.let{ pushToken->
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
                            val response = OkHttpClient().newCall(request).execute()
                            log("push to " + member.value.pushToken + " : " + response.toString())
                            log("my token " + FirebaseInstanceId.getInstance().token)
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
        ref.document(chatId).collection("members").document(Me.value?.id!!).update(data).addOnCompleteListener {
            setMessageListListener()
            setTypingListListener()
            setMembersListListener()
        }
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