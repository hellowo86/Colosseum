package com.hellowo.colosseum.model

import com.google.firebase.firestore.FieldValue
import java.util.*

data class ChatMember (
        var userId: String? = null,
        var name: String? = null,
        var lastConnectedTime: Date = Date(),
        var dtEntered: Date = Date(),
        var live: Boolean = false,
        var pushToken: String? = null) {

    fun makeMap() : HashMap<String, Any?>{
        val result = HashMap<String, Any?>()
        result.put("userId", userId)
        result.put("name", name)
        result.put("lastConnectedTime", FieldValue.serverTimestamp())
        result.put("dtEntered", FieldValue.serverTimestamp())
        result.put("live", live)
        result.put("pushToken", pushToken)
        return result
    }
}