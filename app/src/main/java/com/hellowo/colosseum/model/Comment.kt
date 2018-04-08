package com.hellowo.colosseum.model

import com.google.firebase.firestore.FieldValue
import java.io.Serializable
import java.util.*

data class Comment(
        var id: String? = null,
        var text: String? = null,
        var userName: String? = null,
        var userId: String? = null,
        var userPushId: String? = null,
        var dtCreated: Date = Date(),
        var like: HashMap<String, Boolean> = HashMap(),
        var likeCount: Int = 0,
        var replyCount: Int = 0) {

    fun makeMap() : java.util.HashMap<String, Any?> {
        val result = java.util.HashMap<String, Any?>()
        result.put("id", id)
        result.put("text", text)
        result.put("userName", userName)
        result.put("userId", userId)
        result.put("userPushId", userPushId)
        result.put("dtCreated", FieldValue.serverTimestamp())
        result.put("like", like)
        result.put("likeCount", likeCount)
        result.put("replyCount", replyCount)
        return result
    }
}