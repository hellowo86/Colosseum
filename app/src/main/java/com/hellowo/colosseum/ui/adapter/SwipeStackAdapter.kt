package com.hellowo.colosseum.ui.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.utils.distFrom
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import java.util.*

class SwipeStackAdapter(private val activity: Activity) : ArrayAdapter<User>(activity, 0) {
    private val cal = Calendar.getInstance()

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, contentView: View?, parent: ViewGroup): View {
        var contentView = contentView
        val holder: ViewHolder

        if (contentView == null) {
            val inflater = LayoutInflater.from(context)
            contentView = inflater.inflate(R.layout.item_card_stack, parent, false)
            holder = ViewHolder(contentView)
            contentView!!.tag = holder
        } else {
            holder = contentView.tag as ViewHolder
        }

        val user = getItem(position)

        Glide.with(activity).load(makePublicPhotoUrl(user.id)).into(holder.profileImg)
        holder.nameText.text = "${user.nickName}"
        try{
            holder.locText.text = " : ${Math.round(distFrom(user.lat, user.lng, Me.value?.lat!!, Me.value?.lng!!) * 100) / 100.0}km"
        }catch (e: Exception){}
        holder.ageText.text = " : ${cal.get(Calendar.YEAR) - user.birth + 1}"
        holder.moreText.text = user.moreInfo

        return contentView
    }

    private class ViewHolder(v: View) {
        val profileImg: ImageView = v.findViewById(R.id.profileImg)
        val nameText: TextView = v.findViewById(R.id.nameText)
        val ageText: TextView = v.findViewById(R.id.ageText)
        val locText: TextView = v.findViewById(R.id.locText)
        val moreText: TextView = v.findViewById(R.id.moreText)
    }

}