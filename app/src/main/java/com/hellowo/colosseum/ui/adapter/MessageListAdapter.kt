package com.hellowo.colosseum.ui.adapter

import android.content.Context
import android.support.v4.util.ArrayMap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.ChatMember
import com.hellowo.colosseum.model.Message
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import com.hellowo.colosseum.utils.setClipBoardLink
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.list_item_message_me.view.*
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
    private var lastReadPosition = -1
    private val timeDf = DateFormat.getTimeInstance(DateFormat.SHORT)

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
        val prevMessage = getItem(position - 1)
        message?.let {
            currentCal.timeInMillis = message.dtCreated.time
            nextCal.timeInMillis = nextMessage?.dtCreated?.time ?: 0

            if(viewType == VIEW_TYPE_NOTICE) {
                setNoticeView(v, message)
            }else {
                val isContinueMessage = nextMessage != null
                        && nextMessage.type == 0
                        && message.userId.equals(nextMessage.userId)
                        && message.dtCreated.time - nextMessage.dtCreated.time < 1000 * 60

                val isEndOfContext = prevMessage == null
                        || prevMessage.type != 0
                        || !message.userId.equals(prevMessage.userId)
                        || prevMessage.dtCreated.time - message.dtCreated.time > 1000 * 30

                var uncheckCount = 0
                memberMap.forEach { if(!it.value.live && it.value.lastConnectedTime < message.dtCreated) uncheckCount++ }

                v.topMargin.visibility = if(isContinueMessage) View.GONE else View.VISIBLE
                if(isEndOfContext) {
                    v.timeText.visibility = View.VISIBLE
                    v.timeText.text = timeDf.format(message.dtCreated)
                }else {
                    v.timeText.visibility = View.GONE
                }
                v.uncheckText.text = if(uncheckCount == 0) "" else uncheckCount.toString()

                setContents(v, message)

                when (viewType) {
                    VIEW_TYPE_YOU, VIEW_TYPE_PHOTO_YOU -> setYouView(v, message, isContinueMessage)
                    VIEW_TYPE_ME, VIEW_TYPE_PHOTO_ME -> setMeView(v, message, isContinueMessage)
                }
            }

            if(position == lastReadPosition) {
                v.dateDivider.visibility = View.VISIBLE
                v.dateDividerText.text = context.getString(R.string.last_read_here)
            }else if(currentCal.get(Calendar.YEAR) == nextCal.get(Calendar.YEAR)
                    && currentCal.get(Calendar.DAY_OF_YEAR) == nextCal.get(Calendar.DAY_OF_YEAR)) {
                v.dateDivider.visibility = View.GONE
            }else {
                v.dateDivider.visibility = View.VISIBLE
                v.dateDividerText.text = DateFormat.getDateInstance(DateFormat.FULL).format(message.dtCreated)
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
                v.messageView.setOnClickListener { adapterInterface.onMessageClicked(message) }
                v.messageView.setOnLongClickListener {
                    message.text?.let { setClipBoardLink(context, it) }
                    return@setOnLongClickListener false
                }
            }
            3 -> {
                v.messageText.visibility = View.GONE
                v.photoImg.visibility = View.VISIBLE
                v.photoImg.layout(0, 0, message.width, message.height)
                Glide.with(context).load(message.dataUri).into(v.photoImg)
                v.messageView.setOnClickListener { adapterInterface.onPhotoClicked(message.dataUri!!) }
                v.messageView.setOnLongClickListener {
                    return@setOnLongClickListener false
                }
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

    private fun setMeView(v: View, message: Message, isContinueMessage: Boolean) {
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

        if(message.serverSaved) {
            v.progressBar.visibility = View.GONE
            v.uncheckText.visibility = View.VISIBLE
        }else {
            v.progressBar.visibility = View.VISIBLE
            v.uncheckText.visibility = View.GONE
        }
    }

    private fun setNoticeView(v: View, message: Message) {
        when (message.type) {
            1 -> v.messageText.text = String.format(context.getString(R.string.who_joined_chat), message.userName)
            2 -> v.messageText.text = String.format(context.getString(R.string.who_out_of_chat), message.userName)
            else -> v.messageText.text = message.text
        }
    }

    private fun getItem(position: Int): Message? {
        try{
            if(position in 0 until messageList.size) {
                return messageList[position]
            }
        }catch (e: Exception) {}
        return null
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

    fun setlastReadPos(position: Int) {
        lastReadPosition = position
        notifyItemChanged(position)
    }
}