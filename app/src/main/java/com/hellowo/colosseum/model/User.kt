package com.hellowo.colosseum.model

import com.google.firebase.firestore.Exclude
import com.hellowo.colosseum.R
import com.hellowo.colosseum.split
import java.io.Serializable

data class User (
        var id: String ?= null,
        var nickName: String? = null,
        var email: String? = null,
        var gender: Int = 0,
        var birth: Int = 0,
        var lat: Double = 0.0,
        var lng: Double = 0.0,
        var location: String? = null,
        var moreInfo: String? = null,
        var dtConnected: Long = 0,
        var dtCreated: Long = 0,
        var pushToken: String? = null): Serializable {

    companion object {
        fun getDefaultImgId(gender: Int) = if(gender == 0) R.drawable.man_default else R.drawable.woman_default
    }

    fun makeChatMember(): ChatMember {
        return ChatMember(id, nickName, System.currentTimeMillis(), true, pushToken)
    }

    @Exclude fun getYourGender(): Int = if(gender == 0) 1 else 0
}