package com.hellowo.colosseum.ui.activity

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Chat
import com.hellowo.colosseum.model.Message
import com.hellowo.colosseum.ui.adapter.ChatMemberListAdapter
import com.hellowo.colosseum.ui.adapter.MessageListAdapter
import com.hellowo.colosseum.utils.*
import com.hellowo.colosseum.viewmodel.ChatingViewModel
import gun0912.tedbottompicker.TedBottomPicker
import gun0912.tedbottompicker.util.RealPathUtil
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_chating.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.DateFormat
import java.util.*

class ChatingActivity : BaseActivity() {
    lateinit var viewModel: ChatingViewModel
    lateinit var adapter: MessageListAdapter
    lateinit var memberAdapter: ChatMemberListAdapter
    val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
    private var photoPicker: TedBottomPicker? = null

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
        messageLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        messageInput.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(text: CharSequence, p1: Int, p2: Int, p3: Int) {
                if(text.isNotEmpty()) {
                    sendBtn.visibility = View.VISIBLE
                }else {
                    sendBtn.visibility = View.GONE
                }
                viewModel.typingText(text)
            }
        })

        menuBtn.setOnClickListener{ drawerLy.openDrawer(chatMenuLy) }
        sendBtn.setOnClickListener { enterMessage() }
        backBtn.setOnClickListener{ finish() }
        outBtn.setOnClickListener{ showOutChatAlert() }
        sendImageBtn.setOnClickListener { checkExternalStoragePermission() }
        bottomScrolledLayout.setOnClickListener{
            recyclerView.scrollToPosition(0)
            bottomScrollMessageView.visibility = View.GONE
        }

        swipeRefreshView.setColorSchemeColors(resources.getColor(R.color.colorPrimary))
        swipeRefreshView.setOnRefreshListener { viewModel.loadMoreMessages() }
    }

    fun initListViews() {
        adapter = MessageListAdapter(this, viewModel.chatId, viewModel.members.value!!,
                viewModel.messages.value!!, viewModel.typings.value!!, object : MessageListAdapter.AdapterInterface {
            override fun onProfileClicked(userId: String) { startUserActivity(this@ChatingActivity, userId) }
            override fun onMessageClicked(message: Message) {}
            override fun onPhotoClicked(photoUrl: String) { startPhotoActivity(this@ChatingActivity, photoUrl) }
        })
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var delta = 0
            val offset = dpToPx(this@ChatingActivity, 200f)
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(layoutManager.findFirstVisibleItemPosition() >= 2) {
                    bottomScrolledLayout.visibility = View.VISIBLE
                }else {
                    bottomScrolledLayout.visibility = View.GONE
                    bottomScrollMessageView.visibility = View.GONE
                }
                delta += dy
                if(Math.abs(delta) > offset) {
                    setFloatingDateView()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(newState == 0) {
                    delta = 0
                }
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
        viewModel.messages.observe(this, Observer { adapter.notifyDataSetChanged() })
        viewModel.newMessage.observe(this, Observer {
            adapter.notifyItemInserted(0)
            adapter.notifyItemChanged(1)
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
        viewModel.savedMessage.observe(this, Observer { it?.let { viewModel.messages.value?.indexOf(it)?.let { adapter.notifyItemChanged(it) } } })
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
        if(!messageInput.text.toString().isBlank() && viewModel.messagesLoading.value == false) {
            viewModel.postMessage(messageInput.text.toString(), 0, null, 0, 0, null)
            messageInput.setText("")
        }
    }

    private fun updateChatUI(chat: Chat) {
        val yourGender = Me.value?.yourGender()!!
        titleText.text = chat.getNameByGender(yourGender)
        Glide.with(this).load(makePublicPhotoUrl(chat.getIdByGender(yourGender))).placeholder(R.drawable.img_default_profile)
                .bitmapTransform(CropCircleTransformation(this)).into(profileImg)
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

    var floatingDateViewFlag = false
    val floatingDateViewHandler = @SuppressLint("HandlerLeak")
    object : Handler(){
        override fun handleMessage(msg: android.os.Message?) {
            super.handleMessage(msg)
            if(floatingDateViewFlag) {
                val animSet = AnimatorSet()
                animSet.playTogether(ObjectAnimator.ofFloat(floatingDateView, "translationY", 0f, -dpToPx(this@ChatingActivity, 50f)).setDuration(250))
                animSet.interpolator = FastOutSlowInInterpolator()
                animSet.addListener(object : Animator.AnimatorListener{
                    override fun onAnimationRepeat(p0: Animator?) {}
                    override fun onAnimationCancel(p0: Animator?) {}
                    override fun onAnimationStart(p0: Animator?) { }
                    override fun onAnimationEnd(p0: Animator?) {
                        floatingDateViewFlag = false
                    }
                })
                animSet.start()
                removeMessages(0)
            }
        }
    }

    private fun setFloatingDateView() {
        setFloatingDateViewText()
        if(!floatingDateViewFlag) {
            floatingDateViewFlag = true
            floatingDateView.visibility = View.VISIBLE
            slideFromTop(floatingDateView, dpToPx(this, 50f))
            floatingDateViewHandler.sendEmptyMessageDelayed(0, 2000)
        }else {
            floatingDateViewHandler.removeMessages(0)
            floatingDateViewHandler.sendEmptyMessageDelayed(0, 2000)
        }
    }

    private fun setFloatingDateViewText() {
        val itemPos = layoutManager.findLastVisibleItemPosition()
        if(itemPos >= 0) {
            floatingDateText.text = DateFormat.getDateInstance(DateFormat.FULL).format(viewModel.getlastPostionDate(itemPos))
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
        photoPicker = TedBottomPicker.Builder(this)
                .setOnImageSelectedListener { uri -> viewModel.sendPhotoMessage(this, uri) }
                .create()
        photoPicker?.show(supportFragmentManager)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try{
            super.onActivityResult(requestCode, resultCode, data)
        }catch (e: Exception){
            data?.data?.let {
                if (it.toString().startsWith("content://com.google.android.apps.photos.content")){
                    try {
                        val inputSteam = contentResolver.openInputStream(it)
                        if (inputSteam != null) {
                            val pictureBitmap = BitmapFactory.decodeStream(inputSteam)
                            val baos = ByteArrayOutputStream()
                            pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                            val path = MediaStore.Images.Media.insertImage(contentResolver, pictureBitmap, "img", null)

                            val realPath = RealPathUtil.getRealPath(this, Uri.parse(path))
                            val selectedImageUri = Uri.fromFile(File(realPath))
                            viewModel.sendPhotoMessage(this, selectedImageUri)
                            photoPicker?.dismiss()
                            return
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }
        }
    }
}

