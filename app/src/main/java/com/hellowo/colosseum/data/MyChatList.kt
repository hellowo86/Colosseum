package com.hellowo.colosseum.data

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hellowo.colosseum.model.Chat
import com.hellowo.colosseum.model.User
import java.util.*
import kotlin.collections.ArrayList

object MyChatList : LiveData<ArrayList<Chat>>() {
    @SuppressLint("StaticFieldLeak")
    val db = FirebaseFirestore.getInstance()

    init {
        value = ArrayList()
    }

    override fun onActive() {

    }

    override fun onInactive() {
    }
}