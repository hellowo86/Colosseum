package com.hellowo.colosseum.utils

import android.text.TextUtils

fun isEmailValid(email: String): Boolean {
    return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
