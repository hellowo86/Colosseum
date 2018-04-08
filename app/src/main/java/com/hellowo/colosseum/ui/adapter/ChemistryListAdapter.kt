package com.hellowo.colosseum.ui.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.ArrayMap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.ChemistryQuest
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.BadgeData
import com.hellowo.colosseum.model.Couple
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.utils.makeMessageLastTimeText
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import io.realm.Realm
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.item_chemistry.view.*

class ChemistryListAdapter(private val context: Context, private val mItems: ArrayMap<String, Couple>,
                           private val adapterInterface: (Couple) -> Unit) : RecyclerView.Adapter<ChemistryListAdapter.ViewHolder>() {
    val realm: Realm = Realm.getDefaultInstance()
    val myGender = Me.value?.gender as Int
    val yourGender = Me.value?.yourGender() as Int

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chemistry, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val couple = mItems.valueAt(position)
        val v = holder.itemView

        v.titleText.text = String.format(context.getString(R.string.couple_test_item_title), couple.getNameByGender(yourGender))
        v.statusText.text = couple.getStatusText(context)
        v.statusText.setTextColor(couple.getStatusColor(context)!!)
        v.stepText.text = couple.getStepText(context)
        v.lastTimeText.text = if(couple.dtUpdated.time > 0) makeMessageLastTimeText(context, couple.dtUpdated.time) else ""

        when {
            couple.status == 0 -> {
                val iconId = ChemistryQuest.questIconResource(couple.step)
                v.questIcon.setImageResource(iconId)
                val questId = couple.questId()
                Glide.with(context).load(ChemistryQuest.questImgResource(couple.step, questId)).into(v.questImg)
                v.questText.text = ChemistryQuest.questTitle(couple.step, questId)
                when(couple.step){
                    0 -> {
                        val myPhotoUrl = couple.getPhotoUrlByGender(myGender)
                        val yourPhotoUrl = couple.getPhotoUrlByGender(yourGender)
                        val myPhotoLike = couple.getPhotoLikeByGender(myGender)
                        if(myPhotoUrl.isNullOrEmpty()) {
                            v.questSubText.text = context.getString(R.string.reg_photo)
                        }else {
                            if(yourPhotoUrl.isNullOrEmpty()) {
                                v.questSubText.text = context.getString(R.string.you_reg_photo)
                            }else {
                                if(myPhotoLike == -1) {
                                    v.questSubText.text = context.getString(R.string.me_photo_check)
                                }else {
                                    v.questSubText.text = context.getString(R.string.me_photo_waiting)
                                }
                            }
                        }
                    }
                    1 -> {
                    }
                    2 -> {
                    }
                }
                v.questSubText.visibility = View.GONE
            }
            couple.status == 1 -> {
                v.questIcon.setImageResource(R.drawable.logo_normal)
                Glide.with(context).load(R.drawable.c_1).into(v.questImg)
                v.questText.text = context.getString(R.string.congratulation)
                v.questSubText.text = context.getString(R.string.join_chat)
                v.questSubText.visibility = View.VISIBLE
            }
            couple.status == -1 -> {
                v.questIcon.setImageResource(R.drawable.broken_heart)
                Glide.with(context).load(R.drawable.f_1).into(v.questImg)
                v.questText.text = context.getString(R.string.sorry)
                v.questSubText.text = context.getString(R.string.retry)
                v.questSubText.visibility = View.VISIBLE
            }
        }

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
            v.badgeView.visibility = View.GONE
            adapterInterface.invoke(couple)
        }
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemCount(): Int = mItems.size
}