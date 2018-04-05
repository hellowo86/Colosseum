package com.hellowo.colosseum.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.hellowo.colosseum.R
import com.hellowo.colosseum.ui.adapter.ChemistryCheckListAdapter

@SuppressLint("ValidFragment")
class FavorablityCheckListDialog(private val oxString: String, private val enableCheck : Boolean,
                                 private val dialogInterface: (String) -> Unit) : BottomSheetDialog() {
    lateinit var confirmBtn: LinearLayout
    lateinit var titleText: TextView
    lateinit var subText: TextView
    lateinit var recyclerView: RecyclerView

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView = View.inflate(context, R.layout.dialog_favorablity_check_list, null)
        dialog.setContentView(contentView)
        (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))

        val layoutParams = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        sheetBehavior = layoutParams.behavior as BottomSheetBehavior<*>?
        if (sheetBehavior != null) {
            sheetBehavior?.setBottomSheetCallback(mBottomSheetBehaviorCallback)

            confirmBtn = contentView.findViewById(R.id.confirmBtn)
            titleText = contentView.findViewById(R.id.titleText)
            subText = contentView.findViewById(R.id.subText)
            recyclerView = contentView.findViewById(R.id.recyclerView)

            val array = context?.resources?.getStringArray(R.array.favorablility_check_list)
            val items = ArrayList<String>()
            val checkeds = ArrayList<Boolean>()
            (0 until array?.size!!).forEach {
                items.add(array[it])
                checkeds.add(oxString[it].toString() == "1")
            }

            recyclerView.layoutManager = LinearLayoutManager(context!!)
            recyclerView.adapter = ChemistryCheckListAdapter(context!!, items, checkeds, enableCheck)

            if(enableCheck) {
                confirmBtn.visibility = View.VISIBLE
                confirmBtn.setBackgroundColor(activity?.resources?.getColor(R.color.lol_primary)!!)
                confirmBtn.setOnClickListener {
                    val result = checkeds.joinToString(separator = "", transform = { if(it) "1" else "0" })
                    dialogInterface.invoke(result)
                    dismiss()
                }
            }else {
                confirmBtn.visibility = View.GONE
            }
        }
    }
}