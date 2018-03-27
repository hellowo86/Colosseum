package com.hellowo.colosseum.ui.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.model.Couple
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.item_couple_test.view.*

class CoupleTestAdapter(private val context: Context, private val mItems: ArrayList<Couple>,
                            private val adapterInterface: (Couple) -> Unit) : RecyclerView.Adapter<CoupleTestAdapter.ViewHolder>() {

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_couple_test, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mItems[position]
        val v = holder.itemView

        Glide.with(context).load(makePublicPhotoUrl(item.you.id))
                .bitmapTransform(CropCircleTransformation(context))
                .placeholder(item.you.getDefaultImgId()).into(v.profileImg)
        v.setOnClickListener { adapterInterface.invoke(item) }
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemCount(): Int = mItems.size

    fun refresh(list: ArrayList<Couple>) {
        mItems.clear()
        mItems.addAll(list)
        notifyDataSetChanged()
    }
}