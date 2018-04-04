package com.hellowo.colosseum.ui.adapter

import android.content.Context
import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.util.ArrayMap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.model.Chat
import com.hellowo.colosseum.model.MyChat
import com.hellowo.colosseum.utils.makeMessageLastTimeText
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import com.pixplicity.easyprefs.library.Prefs
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.list_item_chat.view.*

class MyChatListAdapter(val context: Context,
                        val mContentsList: ArrayList<MyChat>,
                        val adapterInterface: (chat: MyChat) -> Unit) : RecyclerView.Adapter<MyChatListAdapter.ViewHolder>() {
    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_chat, parent, false))

    override fun onBindViewHolder(holder: MyChatListAdapter.ViewHolder, position: Int) {
        val chat = mContentsList[position]
        val v = holder.itemView

        v.titleText.text = chat.title
        v.lastMessageText.text = chat.lastMessage ?: ""
        v.lastTimeText.text = if(chat.lastMessageTime > 0) makeMessageLastTimeText(context, chat.lastMessageTime) else ""

        val badgeCount = Prefs.getInt(chat.id, 0)
        if(badgeCount > 0) {
            v.badgeView.visibility = View.VISIBLE
            v.badgeText.text = badgeCount.toString()
        }else {
            v.badgeView.visibility = View.GONE
        }

        Glide.with(context)
                .load(makePublicPhotoUrl(chat.hostId))
                .bitmapTransform(CropCircleTransformation(context))
                .placeholder(R.drawable.default_profile)
                .into(v.chatImage)

        v.setOnClickListener {
            Prefs.putInt(chat.id, 0)
            adapterInterface.invoke(chat)
        }
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount() = mContentsList.size
}