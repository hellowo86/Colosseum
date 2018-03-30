package com.hellowo.colosseum.ui.adapter

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.ArrayMap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Couple
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.item_couple_test.view.*

class FavorabilityTestAdapter(private val context: Context, private val mItems: ArrayMap<String, Couple>,
                              private val adapterInterface: (Couple) -> Unit) : RecyclerView.Adapter<FavorabilityTestAdapter.ViewHolder>() {
    val myGender = Me.value?.gender as Int
    val yourGender = Me.value?.getYourGender() as Int

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_couple_test, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mItems.valueAt(position)
        val v = holder.itemView

        v.nameText.text = String.format(context.getString(R.string.couple_test_item_title), item.getNameByGender(yourGender))

        when(item.step) {
            0 -> {
                v.levelView1.setBackgroundColor(context.resources.getColor(R.color.green))
                v.levelView2.setBackgroundColor(Color.TRANSPARENT)
                v.levelView3.setBackgroundColor(Color.TRANSPARENT)
                v.levelView4.setBackgroundColor(Color.TRANSPARENT)
            }
            1 -> {
                v.levelView1.setBackgroundColor(context.resources.getColor(R.color.colorPrimary))
                v.levelView2.setBackgroundColor(context.resources.getColor(R.color.green))
                v.levelView3.setBackgroundColor(Color.TRANSPARENT)
                v.levelView4.setBackgroundColor(Color.TRANSPARENT)
            }
            2 -> {
                v.levelView1.setBackgroundColor(context.resources.getColor(R.color.colorPrimary))
                v.levelView2.setBackgroundColor(context.resources.getColor(R.color.colorPrimary))
                v.levelView3.setBackgroundColor(context.resources.getColor(R.color.green))
                v.levelView4.setBackgroundColor(Color.TRANSPARENT)
            }
            3 -> {
                v.levelView1.setBackgroundColor(context.resources.getColor(R.color.colorPrimary))
                v.levelView2.setBackgroundColor(context.resources.getColor(R.color.colorPrimary))
                v.levelView3.setBackgroundColor(context.resources.getColor(R.color.colorPrimary))
                v.levelView4.setBackgroundColor(context.resources.getColor(R.color.green))
            }
        }

        Glide.with(context).load(makePublicPhotoUrl(item.getIdByGender(yourGender)))
                .bitmapTransform(CropCircleTransformation(context))
                .placeholder(User.getDefaultImgId(yourGender)).into(v.profileImg)
        v.setOnClickListener { adapterInterface.invoke(item) }
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemCount(): Int = mItems.size
}