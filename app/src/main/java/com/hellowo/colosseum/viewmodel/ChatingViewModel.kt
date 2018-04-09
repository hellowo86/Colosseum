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
import com.hellowo.colosseum.model.BadgeData
import com.hellowo.colosseum.utils.log
import io.realm.Realm
import java.text.DateFormat


class ChatingViewModel : ViewModel() {
    val chat = MutableLiveData<Chat>()
    val messages = MutableLiveData<ArrayList<Message>>()
    val newMessage = MutableLiveData<Message>()
    val savedMessage = MutableLiveData<Message>()
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
    val limit = 30L
    var chatId: String = ""
    var myLastConnectedTime: Long = Long.MAX_VALUE
    var myEnteredTime: Long = 0
    var enteredMessageMap = HashMap<String, Message>()

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

        Realm.getDefaultInstance().executeTransaction { realm ->
            val badgeData = realm.where(BadgeData::class.java).equalTo("id", "chat$chatId").findFirst()
            if(badgeData != null) {
                badgeData.count = 0
            }
        }
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
        if(myEnteredTime == -1L) { // 더이상 메세지가 없을때
            messagesLoading.value = false
            return
        }

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
                    val meesage = it.toObject(Message::class.java)
                    if(myEnteredTime < meesage.dtCreated.time) { // 들어온 시간보다 메세지시간이 미래인 경우에만 보여줌
                        messages.value?.add(meesage)
                    }else{
                        lastVisibleSnapshot = it
                        myEnteredTime = -1L
                        messages.value = messages.value
                        messagesLoading.value = false
                        return@addOnCompleteListener
                    }
                }

                if(task.result.documents.isNotEmpty()) {
                    val lastSnapshot = task.result.documents.last()
                    if(lastVisibleSnapshot == null && myLastConnectedTime < lastSnapshot.getDate("dtCreated").time) { // 마지막으로 로드한 메세지 캐싱
                        lastVisibleSnapshot = lastSnapshot
                        loadMessages(myLastConnectedTime.toDouble())
                        return@addOnCompleteListener
                    }
                    lastVisibleSnapshot = lastSnapshot
                }

                messages.value = messages.value

                if(lastReadPosition.value == -1 && messages.value?.size!! > 0) { // 마지막 읽은 메세지 처리
                    if(lastTime != null) {
                        lastReadPosition.value = messages.value?.size!! - 1
                    }else {
                        messages.value?.firstOrNull { it.dtCreated.time < myLastConnectedTime }?.let {
                            val pos = messages.value?.indexOf(it)
                            if(pos!! > 5) { // 5개 이상의 메세지가 있을경우만 처리
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
                .whereGreaterThan("dtCreated", Date())
                .orderBy("dtCreated").addSnapshotListener { snapshots, e ->
                    if (e == null) {
                        snapshots.documentChanges.forEach {
                            when{
                                it.type == DocumentChange.Type.ADDED -> {
                                    try{
                                        val message = it.document.toObject(Message::class.java)
                                        messages.value?.add(0, message)
                                        newMessage.value = message
                                    }catch (e: Exception){}
                                }
                                it.type == DocumentChange.Type.MODIFIED -> {
                                    if(enteredMessageMap.containsKey(it.document.id)) {
                                        enteredMessageMap.remove(it.document.id)?.let {
                                            it.serverSaved = true
                                            savedMessage.value = it
                                        }
                                    }
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
                    try{
                        val chatMember = it.document.toObject(ChatMember::class.java)
                        when{
                            it.type == DocumentChange.Type.ADDED -> {
                                members.value?.put(chatMember.userId, chatMember)
                                if(chatMember.userId == Me.value?.id) {
                                    myLastConnectedTime = chatMember.lastConnectedTime.time
                                    myEnteredTime = chatMember.dtEntered.time
                                }
                            }
                            it.type == DocumentChange.Type.REMOVED -> {
                                members.value?.remove(chatMember.userId)
                            }
                            it.type == DocumentChange.Type.MODIFIED -> {
                                members.value?.put(chatMember.userId, chatMember)
                            }
                        }
                    }catch (e: Exception){}
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

    fun postMessage(text: String, type: Int, dataUri: String?, width: Int, height: Int, onSuccess: Runnable?) {
        chat.value?.let {
            val id = UUID.randomUUID().toString()
            val message = Message(text, Me.value?.nickName, Me.value?.id, Date(), type, dataUri, width, height, false)
            ref.document(chatId).collection("messages").document(id).set(message.makeMap()).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    enteredMessageMap.put(id, message)
                    messages.value?.add(0, message)
                    newMessage.value = message
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
                            data.put("identifier", chatId)

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
            setMembersListListener()
            setMessageListListener()
            setTypingListListener()
        }
    }

    fun logoutChat() {
        if(!isOut) {
            isTyping = false
            val data = HashMap<String, Any?>()
            data.put("live", false)
            data.put("lastConnectedTime", FieldValue.serverTimestamp())
            data.put("pushToken", Me.value?.pushToken)
            ref.document(chatId).collection("members").document(Me.value?.id!!).update(data)
        }
    }

    fun outOfChat() {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        val messageRef = ref.document(chatId).collection("messages").document(UUID.randomUUID().toString())
        val message = Message("", Me.value?.nickName, Me.value?.id, Date(), 2)
        batch.set(messageRef, message.makeMap())

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
                        taskSnapshot.downloadUrl?.let{
                            postMessage(context.getString(R.string.photo), 3, it.toString(), bitmap?.width!!, bitmap.height, Runnable{ isUploading.value = false })
                        }
                    },
                    { e ->
                        isUploading.value = false
                    }
            )
        }
    }
}