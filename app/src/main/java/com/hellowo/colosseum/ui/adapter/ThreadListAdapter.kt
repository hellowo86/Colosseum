package com.hellowo.colosseum.ui.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.model.Thread
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.list_item_thread.view.*


class ThreadListAdapter(private val context: Context, private val mContentsList: List<Thread>,
                        private val adapterInterface: AdapterInterface) : RecyclerView.Adapter<ThreadListAdapter.ViewHolder>() {

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_thread, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val thread = mContentsList[position]
        val v = holder.itemView

        v.nameText.text = thread.userName
        v.contentsText.text = thread.contents

        Glide.with(context)
                .load(R.drawable.img_default_profile)
                .bitmapTransform(CropCircleTransformation(context))
                .placeholder(R.drawable.img_default_profile)
                .into(v.profileImage)

        v.profileImage.setOnClickListener { thread.userId?.let { id -> adapterInterface.onUserClicked(id) } }
        v.setOnClickListener { adapterInterface.onItemClick(thread) }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return mContentsList.size
    }

    interface AdapterInterface {
        fun onUserClicked(userId: String)
        fun onItemClick(thread: Thread)
    }
}