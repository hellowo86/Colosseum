package com.hellowo.colosseum.ui.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.ArrayMap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.BadgeData
import com.hellowo.colosseum.model.Couple
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.utils.makeMessageLastTimeText
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import io.realm.Realm
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.item_couple_test.view.*

class ChemistryListAdapter(private val context: Context, private val mItems: ArrayMap<String, Couple>,
                           private val adapterInterface: (Couple) -> Unit) : RecyclerView.Adapter<ChemistryListAdapter.ViewHolder>() {
    val realm: Realm = Realm.getDefaultInstance()
    val myGender = Me.value?.gender as Int
    val yourGender = Me.value?.getYourGender() as Int

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_couple_test, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val couple = mItems.valueAt(position)
        val v = holder.itemView

        v.titleText.text = String.format(context.getString(R.string.couple_test_item_title), couple.getNameByGender(yourGender))
        v.subText.text = couple.getStatusText(context)
        v.subText.setTextColor(couple.getStatusColor(context) as Int)
        v.lastTimeText.text = if(couple.dtUpdated > 0) makeMessageLastTimeText(context, couple.dtUpdated) else ""

        realm.executeTransaction { _ ->
            val badgeData = realm.where(BadgeData::class.java).equalTo("id", "chemistry${couple.id}").findFirst()
            if(badgeData != null && badgeData.count > 0) {
                v.badgeView.visibility = View.VISIBLE
                v.badgeText.text = badgeData.count.toString()
            }else {
                v.badgeView.visibility = View.GONE
            }
        }

        Glide.with(context).load(makePublicPhotoUrl(couple.getIdByGender(yourGender)))
                .bitmapTransform(CropCircleTransformation(context))
                .placeholder(User.getDefaultImgId(yourGender)).into(v.profileImage)
        v.setOnClickListener {
            realm.executeTransaction { _ ->
                val badgeData = realm.where(BadgeData::class.java).equalTo("id", "chemistry${couple.id}").findFirst()
                if(badgeData != null) {
                    badgeData.count = 0
                }
            }
            v.badgeView.visibility = View.GONE
            adapterInterface.invoke(couple)
        }
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemCount(): Int = mItems.size
}