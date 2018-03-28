package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.net.Uri
import android.support.v4.util.ArrayMap
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.messaging.FirebaseMessaging
import com.hellowo.colosseum.model.Chat
import com.hellowo.colosseum.model.ChatMember
import com.hellowo.colosseum.model.Message
import com.hellowo.colosseum.utils.log
import com.hellowo.colosseum.utils.uploadPhoto
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class ChatingViewModel : ViewModel() {
    val chat = MutableLiveData<Chat>()
    val isReady = MutableLiveData<Boolean>()
    val messages = MutableLiveData<ArrayList<Message>>()
    val newMessage = MutableLiveData<Message>()
    val typings = MutableLiveData<ArrayList<String>>()
    val members = MutableLiveData<ArrayMap<String, ChatMember>>()
    val isUploading = MutableLiveData<Boolean>()
    val outOfChat = MutableLiveData<Boolean>()

    val db = FirebaseFirestore.getInstance()
    var lastTime: Long = System.currentTimeMillis()
    var chatLoading = false
    var messagesLoading = false
    var membersLoading = false
    var isTyping = false
    var isOut = false
    val limit = 100
    var chatId: String = ""
    var dtEntered: Long = 0
/*
    val messageListListener: ValueEventListener = object : ValueEventListener {
        override fun onCancelled(error: DatabaseError?) {
            messagesLoading = false
            checkReady()
        }
        override fun onDataChange(snapshot: DataSnapshot) {
            val currentPos = messageList.size
            for (postSnapshot in snapshot.children) {
                postSnapshot.getValue(Message::class.java)?.let {
                    messageList.add(currentPos, it)
                }
            }

            if(snapshot.children.count() > 0) {
                lastTime = messageList[messageList.size - 1].dtCreated - 1
            }else {
                lastTime = 0
            }
            messages.value = messageList
            messagesLoading = false
            checkReady()
        }
    }

    val messageAddListener: ChildEventListener = object : ChildEventListener {
        override fun onCancelled(error: DatabaseError) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            snapshot.getValue(Message::class.java)?.let {
                messageList.add(0, it)
                newMessage.value = it
            }
        }
        override fun onChildRemoved(snapshot: DataSnapshot) {}
    }

    val typingListListener: ValueEventListener = object : ValueEventListener {
        override fun onCancelled(error: DatabaseError?) {}
        override fun onDataChange(snapshot: DataSnapshot) {
            typingList.clear()
            for (postSnapshot in snapshot.children) {
                postSnapshot.getValue(Boolean::class.java)?.let {
                    if(it) { typingList.add(postSnapshot.key) }
                }
            }
            typings.value = typingList
        }
    }
*/
    private var messageListenerRegistration: ListenerRegistration? = null
    private var typingListenerRegistration: ListenerRegistration? = null
    private var membersListenerRegistration: ListenerRegistration? = null

    fun initChat(chatId: String, dtEntered: Long) {
        isReady.value = false
        messagesLoading = true
        this.chatId = chatId
        this.dtEntered = dtEntered

        messages.value = ArrayList()
        typings.value = ArrayList()
        members.value = ArrayMap()

        loadChat()
        loadMessages(chatId, lastTime.toDouble())
        setMessageListListener()
        setTypingListListener()
        setMembersListListener()
    }

    private fun loadChat() {
        chatLoading = true
        db.collection("chats").document(chatId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                chat.value = task.result.toObject(Chat::class.java)
            }
            chatLoading = false
            checkReady()
        }
    }

    private fun loadMessages(chatId: String, lastTime: Double) {
        messagesLoading = true/*
        ref.child(KEY_MESSAGE)
                .child(chatId)
                .orderByChild(KEY_DT_CREATED)
                .startAt(dtEntered.toDouble())
                .endAt(lastTime)
                .limitToLast(limit)
                .addListenerForSingleValueEvent(messageListListener)*/
    }

    private fun setMessageListListener() {
/*
        ref.child(KEY_MESSAGE).child(chatId).orderByChild(KEY_DT_CREATED).startAt(lastTime.toDouble())
                .addChildEventListener(messageAddListener)*/
    }

    private fun setTypingListListener() {
/*
        ref.child(KEY_TYPING).child(chatId).addValueEventListener(typingListListener)*/
    }

    private fun setMembersListListener() {
        membersLoading = true
        membersListenerRegistration = db.collection("chats").document(chatId).collection("members").addSnapshotListener { snapshots, e ->
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
            membersLoading = false
            checkReady()
        }
    }

    override fun onCleared() {
        super.onCleared()
        messageListenerRegistration?.remove()
        typingListenerRegistration?.remove()
        membersListenerRegistration?.remove()
    }

    fun loadMoreMessages() {
        if(!messagesLoading && lastTime > 0) {
            loadMessages(chatId, lastTime.toDouble())
        }
    }

    fun postMessage(text: String, type: Int, onSuccess: Runnable?) {/*
        chat.value?.let {
            val newMessage = Message(text, me?.nickName, me?.id, System.currentTimeMillis(), type)

            val childUpdates = HashMap<String, Any>()
            val key = ref.child(KEY_MESSAGE).child(chatId).push().key

            childUpdates.put("/$KEY_MESSAGE/$chatId/$key", newMessage)
            childUpdates.put("/$KEY_CHAT/$chatId/$KEY_LAST_MESSAGE", if(type == 0) text else App.context.getString(R.string.photo))
            childUpdates.put("/$KEY_CHAT/$chatId/$KEY_LAST_MESSAGE_TIME", newMessage.dtCreated)

            ref.updateChildren(childUpdates) { e, _ ->
                if(e == null) {
                    increaseMessageCount()
                    sendPushMessage(newMessage)
                    onSuccess?.run()
                }
            }
        }*/
    }

    private fun increaseMessageCount() {
        /*
        ref.child(KEY_CHAT).child(chatId).child(KEY_MESSAGE_COUNT).runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                if(mutableData.value == null) {
                    mutableData.value = 1
                }else {
                    mutableData.value = mutableData.value.toString().toInt() + 1
                }
                return Transaction.success(mutableData)
            }
            override fun onComplete(e: DatabaseError?, c: Boolean, d: DataSnapshot) {
                if(e == null) {}
            }
        })*/
    }

    private fun sendPushMessage(message: Message) {
        /*
        Thread {
            memberMap.forEach {
                try {
                    if(!it.value.live && !(it.value.userId.equals(me?.id))) {
                        it.value.pushToken?.let{ pushToken->
                            val data = JSONObject()
                            data.put("pushType", PUSH_TYPE_CHAT_MESSAGE)
                            data.put("userId", message.userId)
                            data.put("userName", message.userName)
                            data.put("message", if(message.type == 0) message.text else App.context.getString(R.string.photo))
                            data.put("chatId", chatId)

                            val bodyBuilder = FormBody.Builder()
                            bodyBuilder.add("to", pushToken)
                            bodyBuilder.add("data", data.toString())
                            val request = Request.Builder()
                                    .url("https://fcm.googleapis.com/fcm/send")
                                    .addHeader("Authorization", KEY_PUSH_AUTH)
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
        */
    }

    fun typingText(text: CharSequence) {/*
        if((text.isNotEmpty() && !isTyping) || (text.isEmpty() && isTyping)) {
            isTyping = !isTyping
            ref.child(KEY_TYPING).child(chatId).child(me?.id).setValue(isTyping)
        }*/
    }

    fun getlastPostionDate(itemPos: Int) = messages.value?.get(itemPos)?.dtCreated

    fun loginChat() {/*
        me?.let {
            val childUpdates = HashMap<String, Any>()
            childUpdates.put("/$KEY_CHAT_MEMBERS/$chatId/${it.id}/$KEY_LIVE", true)
            childUpdates.put("/$KEY_CHAT_MEMBERS/$chatId/${it.id}/$KEY_LAST_CONNECTED_TIME", System.currentTimeMillis())
            FirebaseMessaging.getInstance().subscribeToTopic("news")
            ref.updateChildren(childUpdates) { e, _ ->
                if(e == null) {}
            }
        }*/
    }

    fun logoutChat() {/*
        if(!isOut) {
            isTyping = false
            me?.let {
                val childUpdates = HashMap<String, Any>()
                childUpdates.put("/$KEY_CHAT_MEMBERS/$chatId/${it.id}/$KEY_LIVE", false)
                childUpdates.put("/$KEY_CHAT_MEMBERS/$chatId/${it.id}/$KEY_LAST_CONNECTED_TIME", System.currentTimeMillis())
                childUpdates.put("/$KEY_TYPING/$chatId/${it.id}", isTyping)
                childUpdates.put("/$KEY_USERS/${it.id}/$KEY_CHAT/$chatId/$KEY_LAST_CHECK_INDEX", currentChat.messageCount)
                ref.updateChildren(childUpdates) { e, _ ->
                    if(e == null) {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(chatId)
                    }
                }
            }
        }*/
    }

    fun checkReady() {
        isReady.value = !chatLoading && !messagesLoading && !membersLoading
    }

    fun outOfChat() {/*
        me?.let {
            val newMessage = Message("", it.nickName, it.id, System.currentTimeMillis(), 2)
            val key = ref.child(KEY_MESSAGE).child(chatId).push().key
            val childUpdates = HashMap<String, Any?>()
            childUpdates.put("/$KEY_MESSAGE/$chatId/$key", newMessage)
            childUpdates.put("/$KEY_CHAT_MEMBERS/$chatId/${it.id}", null)
            childUpdates.put("/$KEY_TYPING/$chatId/${it.id}", null)
            childUpdates.put("/$KEY_USERS/${it.id}/$KEY_CHAT/$chatId", null)

            ref.updateChildren(childUpdates) { error, _ ->
                if(error == null) {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(chatId)
                    isOut = true
                    outOfChat.value = isOut
                }
            }
        }*/
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
                            postMessage(json.toString(), 3, Runnable{
                                isUploading.value = false
                            })
                        }
                    },
                    { e ->
                        isUploading.value = false
                    }
            )
        }
    }
}