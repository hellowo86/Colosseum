package com.hellowo.colosseum.ui.activity

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.hellowo.colosseum.R
import com.hellowo.colosseum.model.Chat
import com.hellowo.colosseum.model.Message
import com.hellowo.colosseum.ui.adapter.ChatMemberListAdapter
import com.hellowo.colosseum.ui.adapter.MessageListAdapter
import com.hellowo.colosseum.utils.fadeIn
import com.hellowo.colosseum.utils.fadeOut
import com.hellowo.colosseum.utils.log
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import com.hellowo.colosseum.viewmodel.ChatingViewModel
import gun0912.tedbottompicker.TedBottomPicker
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_chating.*
import java.text.DateFormat
import java.util.*

class ChatingActivity : BaseActivity() {
    lateinit var viewModel: ChatingViewModel
    lateinit var adapter: MessageListAdapter
    lateinit var memberAdapter: ChatMemberListAdapter
    val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
    var floatingDateViewFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chating)
        viewModel = ViewModelProviders.of(this).get(ChatingViewModel::class.java)
        viewModel.initChat(intent.getStringExtra("chatId"))
        initLayout()
        initListViews()
        initObserve()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loginChat()
    }

    override fun onStop() {
        super.onStop()
        viewModel.logoutChat()
    }

    fun initLayout() {
        messageInput.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(text: CharSequence, p1: Int, p2: Int, p3: Int) {
                viewModel.typingText(text)
            }
        })

        menuBtn.setOnClickListener{ drawerLy.openDrawer(chatMenuLy) }
        sendBtn.setOnClickListener { enterMessage() }
        backBtn.setOnClickListener{ finish() }
        outBtn.setOnClickListener{ showOutChatAlert() }
        sendImageBtn.setOnClickListener { checkExternalStoragePermission() }
        bottomScrolledLayout.setOnClickListener{ recyclerView.scrollToPosition(0) }

        swipeRefreshView.setColorSchemeColors(resources.getColor(R.color.colorPrimary))
        swipeRefreshView.setOnRefreshListener { viewModel.loadMoreMessages() }
    }

    fun initListViews() {
        adapter = MessageListAdapter(this, viewModel.chatId, viewModel.members.value!!,
                viewModel.messages.value!!, viewModel.typings.value!!, object : MessageListAdapter.AdapterInterface {
            override fun onProfileClicked(userId: String) { startUserActivity(userId) }
            override fun onMessageClicked(message: Message) {}
            override fun onPhotoClicked(photoUrl: String) { startPhotoActivity(photoUrl) }
        })
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(layoutManager.findFirstVisibleItemPosition() >= 2) {
                    bottomScrolledLayout.visibility = View.VISIBLE
                }else {
                    bottomScrolledLayout.visibility = View.GONE
                    bottomScrollMessageView.visibility = View.GONE
                }
                setFloatingDateViewText()
            }
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                setFloatingDateView(newState)
            }
        })

        memberAdapter = ChatMemberListAdapter(this, viewModel.members.value!!) {
            chatMember ->
        }
        memberRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        memberRecyclerView.adapter = memberAdapter

        bottomScrolledLayout.visibility = View.GONE
    }

    fun initObserve() {
        viewModel.messagesLoading.observe(this, Observer { swipeRefreshView.isRefreshing = it as Boolean })
        viewModel.chat.observe(this, Observer { it?.let { updateChatUI(it) } })
        viewModel.typings.observe(this, Observer { updateTypingUI(it) })
        viewModel.messages.observe(this, Observer {
            adapter.notifyDataSetChanged()
        })
        viewModel.newMessage.observe(this, Observer {
            adapter.notifyItemInserted(0)
            if(layoutManager.findFirstVisibleItemPosition() <= 1) {
                recyclerView.scrollToPosition(0)
            }else {
                bottomScrollMessageView.visibility = View.VISIBLE
                bottomScrollMessageText.text = it?.text
                Glide.with(this)
                        .load(makePublicPhotoUrl(it?.userId))
                        .bitmapTransform(CropCircleTransformation(this))
                        .placeholder(R.drawable.default_profile)
                        .into(bottomScrollProfileImg)
            }
        })
        viewModel.members.observe(this, Observer {
            memberAdapter.notifyDataSetChanged()
            adapter.notifyDataSetChanged()
        })
        viewModel.outOfChat.observe(this, Observer { if(it as Boolean) finish() })
        viewModel.isUploading.observe(this, Observer { if(it as Boolean) showProgressDialog() else hideProgressDialog() })
        viewModel.lastReadPosition.observe(this, Observer { if(it as Int >= 0) {
            recyclerView.scrollToPosition(it)
            adapter.setlastReadPos(it)
        }})
    }

    private fun enterMessage() {
        if(!messageInput.text.toString().isNullOrBlank()) {
            viewModel.postMessage(messageInput.text.toString(), 0, null, null)
            messageInput.setText("")
        }
    }

    private fun updateChatUI(chat: Chat) {
        titleText.text = chat.title
    }

    private fun updateTypingUI(typingList: List<String>?) {
        userChipLy.removeAllViews()
        if(typingList?.isEmpty() as Boolean) {
            typingView.visibility = View.GONE
            //startToBottomSlideDisappearAnimation(typingView, ViewUtil.dpToPx(this, 40f))
        }else {
            typingList.forEach {
                val imageView = ImageView(this)
                imageView.layoutParams = ViewGroup.LayoutParams(40, 40)
                Glide.with(this)
                        .load(makePublicPhotoUrl(it))
                        .bitmapTransform(CropCircleTransformation(this))
                        .placeholder(R.drawable.default_profile)
                        .into(imageView)
                userChipLy.addView(imageView)
            }
            typingView.visibility = View.VISIBLE
            //startFromBottomSlideAppearAnimation(typingView, ViewUtil.dpToPx(this, 40f))
        }
    }

    val floatingDateViewHandler = @SuppressLint("HandlerLeak")
    object : Handler(){
        override fun handleMessage(msg: android.os.Message?) {
            super.handleMessage(msg)
            if(floatingDateViewFlag) {
                fadeOut(floatingDateView, object : AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        floatingDateView.visibility = View.GONE
                    }
                })
            }
        }
    }

    private fun setFloatingDateView(newState: Int) {
        if(newState == 0/*scroll stop*/) {
            floatingDateViewFlag = true
            floatingDateViewHandler.removeMessages(0)
            floatingDateViewHandler.sendEmptyMessageDelayed(0, 1000)
        }else {
            floatingDateViewFlag = false
            if(floatingDateView.visibility == View.GONE) {
                floatingDateView.visibility = View.VISIBLE
                fadeIn(floatingDateView)
            }
        }
    }

    private fun setFloatingDateViewText() {
        val itemPos = layoutManager.findLastVisibleItemPosition()
        if(itemPos >= 0) {
            floatingDateText.text = DateFormat.getDateInstance(DateFormat.FULL).format(Date(viewModel.getlastPostionDate(itemPos) as Long))
        }
    }

    private fun showOutChatAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.out_of_chat)
        builder.setCancelable(true)
        builder.setMessage(R.string.out_of_chat_sub)
        builder.setPositiveButton(R.string.ok) { _,_ -> viewModel.outOfChat() }
        builder.setNegativeButton(R.string.cancel, null)
        builder.show()
    }

    private val permissionlistener = object : PermissionListener {
        override fun onPermissionGranted() { showPhotoPicker() }
        override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {}
    }

    fun checkExternalStoragePermission() {
        TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check()
    }

    private fun showPhotoPicker() {
        val bottomSheetDialogFragment = TedBottomPicker.Builder(this)
                .setOnImageSelectedListener { uri -> viewModel.sendPhotoMessage(this, uri) }
                .create()
        bottomSheetDialogFragment.show(supportFragmentManager)
    }

    private fun startUserActivity(userId: String) {}

    private fun startPhotoActivity(photoUrl: String) {
        val intent = Intent(this, PhotoActivity::class.java)
        intent.putExtra("photoUrl", photoUrl)
        startActivity(intent)
    }
}

