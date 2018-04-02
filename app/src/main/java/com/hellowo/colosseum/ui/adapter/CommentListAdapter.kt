package com.hellowo.colosseum.ui.adapter

import android.content.Context
import android.support.v4.util.ArrayMap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.model.ChatMember
import com.hellowo.colosseum.model.Comment
import com.hellowo.colosseum.utils.makeMessageLastTimeText
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.list_item_comment.view.*

class CommentListAdapter(val context: Context,
                         val mContentsList: ArrayList<Comment>,
                         val adapterInterface: (comment: Comment) -> Unit) : RecyclerView.Adapter<CommentListAdapter.ViewHolder>() {

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_comment, parent, false))

    override fun onBindViewHolder(holder: CommentListAdapter.ViewHolder, position: Int) {
        val comment = mContentsList[position]
        val v = holder.itemView

        v.nameText.text = comment.userName
        v.messageText.text = comment.text
        v.activeTimeText.text = makeMessageLastTimeText(context, comment.dtCreated)

        Glide.with(context)
                .load(makePublicPhotoUrl(comment.userId))
                .bitmapTransform(CropCircleTransformation(context))
                .placeholder(R.drawable.default_profile)
                .into(v.profileImage)

        v.setOnClickListener { adapterInterface.invoke(comment) }
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount() = mContentsList.size
}