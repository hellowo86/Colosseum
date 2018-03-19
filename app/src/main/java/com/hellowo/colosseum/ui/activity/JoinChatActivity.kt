package com.hellowo.colosseum.ui.activity

import android.os.Bundle
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Chat
import com.hellowo.colosseum.model.Message
import com.hellowo.colosseum.utils.log
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_join_chat.*
import java.util.*

class JoinChatActivity : BaseActivity() {
    private lateinit var chat: Chat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_chat)
        chat = intent.getSerializableExtra("Chat") as Chat
        log(chat.toString())
        initLayout()
        initObserve()
    }

    private fun initLayout() {
        Glide.with(this)
                .load(makePublicPhotoUrl(chat.host))
                .bitmapTransform(CropCircleTransformation(this))
                .placeholder(R.drawable.img_default_profile)
                .into(chatImage)

        titleText.text = chat.title

        joinBtn.setOnClickListener { joinChat() }
    }

    private fun initObserve() {
    }

    private fun joinChat() {
        val me = Me.value
        val db = FirebaseFirestore.getInstance()
        val chatMember = me?.makeChatMember()
        val msg = Message(UUID.randomUUID().toString(), "", me?.nickName, me?.id, chatMember?.lastConnectedTime as Long, 1)

        val batch = db.batch()

        val chatRef = db.collection("chats").document(chat.id!!).collection("members").document(chatMember.userId!!)
        batch.set(chatRef, chatMember)

        val msgRef = db.collection("chats").document(chat.id!!).collection("messages").document(msg.id!!)
        batch.set(msgRef, msg)

        batch.commit().addOnCompleteListener{
            finish()
        }
    }
}