package com.hellowo.colosseum.ui.dialog

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.media.*
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.view.View
import android.widget.*
import com.google.firebase.storage.FirebaseStorage
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.hellowo.colosseum.R
import com.skyfishjy.library.RippleBackground
import java.io.*
import java.util.ArrayList

@SuppressLint("ValidFragment")
class SearchFilterDialog() : BottomSheetDialog() {

    lateinit var titleText: TextView

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView = View.inflate(context, R.layout.dialog_search_filter, null)
        dialog.setContentView(contentView)
        (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))

        val layoutParams = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        sheetBehavior = layoutParams.behavior as BottomSheetBehavior<*>?
        if (sheetBehavior != null) {
            sheetBehavior?.setBottomSheetCallback(mBottomSheetBehaviorCallback)

            titleText = contentView.findViewById(R.id.titleText)


        }
    }
}