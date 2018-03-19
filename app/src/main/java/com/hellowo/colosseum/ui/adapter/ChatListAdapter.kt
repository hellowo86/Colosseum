package com.hellowo.teamfinder.ui.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v4.util.ArrayMap
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.model.Chat
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.list_item_big_chat.view.*

class ChatListAdapter(private val context: Context,
                      private val mContentsList: ArrayList<Chat>,
                      private val adapterInterface: (chat: Chat) -> Unit) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_big_chat, parent, false))

    override fun onBindViewHolder(holder: ChatListAdapter.ViewHolder, position: Int) {
        val chat = mContentsList[position]
        val v = holder.itemView

        v.titleText.text = chat.title
        /*
        v.contentsText.text = chat.description
        v.lastMessageText.text = chat.lastMessage ?: ""
        v.lastTimeText.text = if(chat.lastMessageTime > 0) makeMessageLastTimeText(chat.lastMessageTime) else ""
*/
        Glide.with(context)
                .load(makePublicPhotoUrl(chat.host))
                .bitmapTransform(CropCircleTransformation(context))
                .placeholder(R.drawable.img_default_profile)
                .into(v.chatImage)

        v.setOnClickListener { adapterInterface.invoke(chat) }
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount() = mContentsList.size
}