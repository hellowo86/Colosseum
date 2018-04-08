package com.hellowo.colosseum.model

import com.google.firebase.firestore.FieldValue
import java.util.*

data class Message (
        var text: String? = null,
        var userName: String? = null,
        var userId: String? = null,
        var dtCreated: Date = Date(),
        var type: Int = 0,
        var dataUri: String? = null,
        var width: Int = 0,
        var height: Int = 0,
        var serverSaved: Boolean = true) {

    fun makeMap() : HashMap<String, Any?>{
        val result = HashMap<String, Any?>()
        result.put("text", text)
        result.put("userName", userName)
        result.put("userId", userId)
        result.put("dtCreated", FieldValue.serverTimestamp())
        result.put("type", type)
        result.put("dataUri", dataUri)
        result.put("width", width)
        result.put("height", height)
        return result
    }
}