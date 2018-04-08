package com.hellowo.colosseum.model

import com.google.firebase.firestore.FieldValue
import com.hellowo.colosseum.R
import java.io.Serializable
import java.util.*

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
        var dtConnected: Date = Date(),
        var dtCreated: Date = Date(),
        var pushToken: String? = null,
        var coin: Int = 0): Serializable {

    fun makeMap() : HashMap<String, Any?>{
        val result = HashMap<String, Any?>()
        result.put("id", id)
        result.put("nickName", nickName)
        result.put("email", email)
        result.put("gender", gender)
        result.put("birth", birth)
        result.put("lat", lat)
        result.put("lng", lng)
        result.put("location", location)
        result.put("moreInfo", moreInfo)
        result.put("dtConnected", FieldValue.serverTimestamp())
        result.put("dtCreated", FieldValue.serverTimestamp())
        result.put("pushToken", pushToken)
        result.put("coin", coin)
        return result
    }

    companion object {
        private val cal = Calendar.getInstance()
        fun getDefaultImgId(gender: Int) = if(gender == 0) R.drawable.man_default else R.drawable.woman_default
        fun getAge(birth: Int) = cal.get(Calendar.YEAR) - birth + 1
    }

    fun makeChatMember(): ChatMember {
        val time = Date()
        return ChatMember(id, nickName, time, time, true, pushToken)
    }

    fun yourGender(): Int = if(gender == 0) 1 else 0
}