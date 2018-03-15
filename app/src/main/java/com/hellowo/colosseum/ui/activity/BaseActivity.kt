package com.hellowo.colosseum.ui.activity

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import com.hellowo.colosseum.R

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null

    fun showProgressDialog() {
        hideProgressDialog()
        progressDialog = ProgressDialog(this)
        progressDialog?.setCancelable(false)
        progressDialog?.setMessage(getString(R.string.plz_wait))
        progressDialog?.show()
    }

    fun hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog?.dismiss()
            progressDialog = null
        }
    }
}