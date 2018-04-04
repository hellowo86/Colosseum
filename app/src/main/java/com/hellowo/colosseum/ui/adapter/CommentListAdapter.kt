package com.hellowo.colosseum.ui.adapter

import android.annotation.SuppressLint
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
import com.hellowo.colosseum.model.Comment
import com.hellowo.colosseum.utils.makeMessageLastTimeText
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.list_item_comment.view.*

class CommentListAdapter(val context: Context,
                         val mContentsList: ArrayList<Comment>,
                         val onReplyClick: (comment: Comment) -> Unit,
                         val onLikeClick: (comment: Comment) -> Unit,
                         val onUserClick: (userId: String) -> Unit,
                         val replyMode: Boolean) : RecyclerView.Adapter<CommentListAdapter.ViewHolder>() {

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_comment, parent, false))

    @SuppressLint("SetTextI18n")
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

        v.profileImage.setOnClickListener {
            onUserClick.invoke(comment.userId!!)
        }

        if(replyMode) {
            v.replyCountText.visibility = View.GONE
            v.replyBtn.visibility = View.GONE
            v.likeCountText.visibility = View.GONE
            v.likeBtn.visibility = View.GONE
        }else {
            v.replyCountText.visibility = View.VISIBLE
            v.replyBtn.visibility = View.VISIBLE
            v.likeCountText.visibility = View.VISIBLE
            v.likeBtn.visibility = View.VISIBLE

            if(comment.like.containsKey(Me.value?.id)) {
                v.likeBtn.setColorFilter(context.resources.getColor(R.color.colorPrimary))
            }else {
                v.likeBtn.setColorFilter(context.resources.getColor(R.color.iconTint))
            }

            v.likeCountText.text = "${comment.likeCount} Like"
            v.likeBtn.setOnClickListener {
                v.progressBar.visibility = View.VISIBLE
                v.likeBtn.visibility = View.GONE
                onLikeClick.invoke(comment)
            }

            if(comment.replyCount > 0) {
                v.replyBtn.setColorFilter(context.resources.getColor(R.color.grey))
            }else {
                v.replyBtn.setColorFilter(context.resources.getColor(R.color.iconTint))
            }

            v.replyCountText.text = "${comment.replyCount} Reply"
            v.replyBtn.setOnClickListener {
                onReplyClick.invoke(comment)
            }
        }

        v.progressBar.visibility = View.GONE
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount() = mContentsList.size
}