package com.hellowo.colosseum.model

import com.google.firebase.firestore.FieldValue
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap

data class Chat(
        var id: String? = null,
        var title: String? = null,
        var description: String? = null,
        var maxMemberCount: Int? = 2,
        var dtCreated: Date = Date(),
        var hostId: String? = null,
        var lastMessage: String? = null,
        var lastMessageTime: Date = Date(),
        var dtUpdated: Date = Date(),
        var maleId: String ?= null,
        var maleName: String ?= null,
        var femaleId: String ?= null,
        var femaleName: String ?= null): Serializable {

    fun getIdByGender(gender: Int): String? = if(gender == 0) maleId else femaleId

    fun getNameByGender(gender: Int): String? = if(gender == 0) maleName else femaleName

    fun makeMap() : HashMap<String, Any?>{
        val result = HashMap<String, Any?>()
        result.put("id", id)
        result.put("title", title)
        result.put("description", description)
        result.put("maxMemberCount", maxMemberCount)
        result.put("dtCreated", FieldValue.serverTimestamp())
        result.put("hostId", hostId)
        result.put("lastMessage", lastMessage)
        result.put("lastMessageTime", FieldValue.serverTimestamp())
        result.put("dtUpdated", FieldValue.serverTimestamp())
        result.put("maleId", maleId)
        result.put("maleName", maleName)
        result.put("femaleId", femaleId)
        result.put("femaleName", femaleName)
        return result
    }
}
