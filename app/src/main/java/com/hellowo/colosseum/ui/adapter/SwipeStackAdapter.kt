package com.hellowo.colosseum.ui.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.hellowo.colosseum.R


class SwipeStackAdapter(val activity: Activity, private val mData: List<String>) : BaseAdapter() {

    override fun getCount(): Int {
        return mData.size
    }

    override fun getItem(position: Int): String {
        return mData[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View

        if(convertView != null) view = convertView
        else view = activity.layoutInflater.inflate(R.layout.item_card_stack, parent, false)

        val textViewCard = view.findViewById<TextView>(R.id.textViewCard)
        textViewCard.text = mData[position]

        return view
    }
}