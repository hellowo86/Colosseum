package com.hellowo.colosseum.ui.adapter

import android.content.Context
import android.support.v4.util.ArrayMap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Chat
import com.hellowo.colosseum.model.ChatMember
import com.hellowo.colosseum.model.Message
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.list_item_message.view.*
import org.json.JSONObject
import java.text.DateFormat
import java.util.*

class MessageListAdapter(val context: Context,
                         val chatId: String,
                         val memberMap: ArrayMap<String, ChatMember>,
                         val messageList: List<Message>,
                         val typingList: List<String>,
                         val adapterInterface: AdapterInterface) : RecyclerView.Adapter<MessageListAdapter.ViewHolder>() {
    private val VIEW_TYPE_YOU = 1
    private val VIEW_TYPE_ME = 2
    private val VIEW_TYPE_NOTICE = 3
    private val VIEW_TYPE_PHOTO_YOU = 4
    private val VIEW_TYPE_PHOTO_ME = 5
    private val currentCal: Calendar = Calendar.getInstance()
    private val nextCal: Calendar = Calendar.getInstance()
    private val myId = Me.value?.id

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(parent.context).inflate(
            when (viewType) {
                VIEW_TYPE_YOU -> R.layout.list_item_message
                VIEW_TYPE_ME -> R.layout.list_item_message_me
                else -> R.layout.list_item_message_notice
            }, parent, false))

    override fun onBindViewHolder(holder: MessageListAdapter.ViewHolder, position: Int) {
        val v = holder.itemView
        val viewType = getItemViewType(position)
        val message = getItem(position)
        val nextMessage = getItem(position + 1)
        message?.let {
            currentCal.timeInMillis = message.dtCreated
            nextCal.timeInMillis = nextMessage?.dtCreated ?: 0

            if(viewType == VIEW_TYPE_NOTICE) {
                setNoticeView(v, message)
            }else {
                val isContinueMessage = nextMessage != null
                        && nextMessage.type == 0
                        && message.userId.equals(nextMessage.userId)
                        && message.dtCreated - nextMessage.dtCreated < 1000 * 60

                var uncheckCount = 0
                memberMap.forEach { if(!it.value.live && it.value.lastConnectedTime < message.dtCreated) uncheckCount++ }

                v.topMargin.visibility = if(isContinueMessage) View.GONE else View.VISIBLE
                v.timeText.text = if(isContinueMessage) "" else DateFormat.getTimeInstance(DateFormat.SHORT).format(currentCal.time)
                v.uncheckText.text = if(uncheckCount == 0) "" else uncheckCount.toString()

                setContents(v, message)

                when (viewType) {
                    VIEW_TYPE_YOU, VIEW_TYPE_PHOTO_YOU -> setYouView(v, message, isContinueMessage)
                    VIEW_TYPE_ME, VIEW_TYPE_PHOTO_ME -> setMeView(v, message)
                }
            }

            if(currentCal.get(Calendar.YEAR) == nextCal.get(Calendar.YEAR)
                    && currentCal.get(Calendar.DAY_OF_YEAR) == nextCal.get(Calendar.DAY_OF_YEAR)) {
                v.dateDivider.visibility = View.GONE
            }else {
                v.dateDivider.visibility = View.VISIBLE
                v.dateDividerText.text = DateFormat.getDateInstance(DateFormat.FULL).format(Date(message.dtCreated))
            }
        }
    }

    private fun setContents(v: View, message: Message) {
        when (message.type){
            0 -> {
                v.messageText.text = message.text
                v.messageText.visibility = View.VISIBLE
                v.photoImg.visibility = View.GONE
                v.photoImg.setImageDrawable(null)
                v.setOnClickListener { adapterInterface.onMessageClicked(message) }
            }
            3 -> {
                v.messageText.visibility = View.GONE
                v.photoImg.visibility = View.VISIBLE
                val json = JSONObject(message.dataUri)
                val photoUrl = json.getString("url")
                val w = json.getInt("w")
                val h = json.getInt("h")
                v.photoImg.layoutParams = FrameLayout.LayoutParams(w, h)
                Glide.with(context).load(photoUrl).into(v.photoImg)
                v.setOnClickListener { adapterInterface.onPhotoClicked(photoUrl) }
            }
        }
    }

    private fun setYouView(v: View, message: Message, isContinueMessage: Boolean) {
        if(isContinueMessage) {
            v.profileImage.visibility = View.INVISIBLE
            v.nameLy.visibility = View.GONE
        }else {
            v.profileImage.visibility = View.VISIBLE
            v.nameLy.visibility = View.VISIBLE
            v.nameText.text = message.userName

            Glide.with(context)
                    .load(makePublicPhotoUrl(message.userId))
                    .bitmapTransform(CropCircleTransformation(context))
                    .placeholder(R.drawable.default_profile)
                    .into(v.profileImage)
            v.profileImage.setOnClickListener{ adapterInterface.onProfileClicked(message.userId!!) }
        }
    }

    private fun setMeView(v: View, message: Message) {}

    private fun setNoticeView(v: View, message: Message) {
        when (message.type) {
            1 -> v.messageText.text = String.format(context.getString(R.string.who_joined_chat), message.userName)
            2 -> v.messageText.text = String.format(context.getString(R.string.who_out_of_chat), message.userName)
            else -> v.messageText.text = message.text
        }
    }

    private fun getItem(position: Int): Message? {
        try{
            return messageList[position]
        }catch (e: Exception) {
            return null
        }
    }

    override fun getItemViewType(position: Int): Int {
        getItem(position)?.let{
            if(it.type == 0 || it.type == 3) {
                if(it.userId.equals(myId)) {
                    return VIEW_TYPE_ME
                }else if(!it.userId.equals(myId)){
                    return VIEW_TYPE_YOU
                }
            }else if(it.type == 1 || it.type == 2) {
                return VIEW_TYPE_NOTICE
            }
        }
        return -1
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount() = messageList.size

    interface AdapterInterface {
        fun onMessageClicked(message: Message)
        fun onPhotoClicked(photoUrl: String)
        fun onProfileClicked(userId: String)
    }

    fun  refreshTypingList() {
        notifyItemChanged(0)
    }
}