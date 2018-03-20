package com.hellowo.colosseum.ui.activity

import android.animation.LayoutTransition
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.view.animation.AnimationUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Chat
import com.hellowo.colosseum.viewmodel.BasicViewModel
import kotlinx.android.synthetic.main.activity_create_chat.*
import java.util.*

class CreateChatActivity : BaseActivity() {
    private lateinit var viewModel: BasicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(BasicViewModel::class.java)
        setContentView(R.layout.activity_create_chat)
        initLayout()
        initObserve()
    }

    private fun initLayout() {
        isuueTypeLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        posiAndNegBtn.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                it.elevation = it.elevation + 10
            }
        }

        nextBtn.setOnClickListener { goNext() }
        backBtn.setOnClickListener{ onBackPressed() }
    }

    private fun initObserve() {
    }

    private fun goNext(): Boolean {
        if(viewFlipper.currentView == isuueTypeLy) {
            createChat()
        }else {
            viewFlipper.inAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_right)
            viewFlipper.outAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_out_left)
            viewFlipper.displayedChild = viewFlipper.displayedChild + 1
        }
        return true
    }

    override fun onBackPressed() {
        if(viewFlipper.currentView == isuueTypeLy) {
            finish()
        }else {
            viewFlipper.inAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_left)
            viewFlipper.outAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_out_right)
            viewFlipper.displayedChild = viewFlipper.displayedChild - 1
        }
    }

    private fun createChat() {
        showProgressDialog()
        val me = Me.value
        val db = FirebaseFirestore.getInstance()
        val chatMember = me?.makeChatMember()
        val chat = Chat(UUID.randomUUID().toString(), Me.value?.id, "타이틀 테스트 입니다")
        val batch = db.batch()

        val myChatRef = db.collection("users").document(me?.id!!).collection("chats").document(chat.id!!)
        batch.set(myChatRef, chat.makeMyChatDataMap())
        val chatRef = db.collection("chats").document(chat.id!!)
        batch.set(chatRef, chat.makeChatDataMap())
        val memberRef = db.collection("chats").document(chat.id!!).collection("members").document(chatMember?.userId!!)
        batch.set(memberRef, chatMember)

        batch.commit().addOnCompleteListener{
            hideProgressDialog()
            finish()
        }
    }
}