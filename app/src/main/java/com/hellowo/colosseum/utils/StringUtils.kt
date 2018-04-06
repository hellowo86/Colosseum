package com.hellowo.colosseum.utils

import android.content.Context
import android.text.TextUtils
import com.hellowo.colosseum.R
import java.text.DateFormat
import java.util.*
import android.widget.TextView
import android.text.Spannable
import android.graphics.Color
import android.text.style.ForegroundColorSpan
import android.text.SpannableString



fun isEmailValid(email: String): Boolean {
    return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun makeActiveTimeText(context: Context, dtCreated: Long): String {
    val m = (System.currentTimeMillis() - dtCreated) / (1000 * 60)
    return when(m){
        in 0..9 -> context.getString(R.string.just_before)
        in 10..59 -> String.format(context.getString(R.string.min_before), m)
        in 60..(60 * 24) -> String.format(context.getString(R.string.hour_before), m / 60)
        else -> DateFormat.getDateTimeInstance().format(Date(dtCreated))
    }
}

fun makeMessageLastTimeText(context: Context, dtCreated: Long): String {
    val m = (System.currentTimeMillis() - dtCreated) / (1000 * 60)
    return when(m){
        in 0..0 -> context.getString(R.string.just_before)
        in 1..59 -> String.format(context.getString(R.string.min_before), m)
        in 60..(60 * 24) -> String.format(context.getString(R.string.hour_before), m / 60)
        else -> DateFormat.getDateTimeInstance().format(Date(dtCreated))
    }
}

fun setTextColor(textView: TextView, text: String, start: Int, end: Int, color: Int) {
    val spannable = SpannableString(text)
    spannable.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    textView.setText(spannable, TextView.BufferType.SPANNABLE)
}