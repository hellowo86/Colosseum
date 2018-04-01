package com.hellowo.colosseum.ui.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.ArrayMap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hellowo.colosseum.R
import kotlinx.android.synthetic.main.list_item_normal_check.view.*

class FavorablityCheckListAdapter(val context: Context, val mContentsList: ArrayList<String>, val checkList: ArrayList<Boolean>,
                                  val enalbeCheck: Boolean) : RecyclerView.Adapter<FavorablityCheckListAdapter.ViewHolder>() {

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_normal_check, parent, false))

    override fun onBindViewHolder(holder: FavorablityCheckListAdapter.ViewHolder, position: Int) {
        val title = mContentsList[position]
        val checked = checkList[position]
        val v = holder.itemView

        v.titleText.text = title
        v.checkbox.isChecked = checked

        if(enalbeCheck) {
            v.checkbox.isEnabled = true
            v.checkbox.setOnCheckedChangeListener { compoundButton, checked -> checkList[position] = checked }
        }else{
            v.checkbox.isEnabled = false
        }
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount() = mContentsList.size
}