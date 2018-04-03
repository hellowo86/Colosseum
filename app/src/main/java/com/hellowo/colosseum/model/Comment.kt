package com.hellowo.colosseum.model

import java.io.Serializable

data class Comment(
        var id: String? = null,
        var text: String? = null,
        var userName: String? = null,
        var userId: String? = null,
        var userPushId: String? = null,
        var dtCreated: Long = 0,
        var like: HashMap<String, Boolean> = HashMap(),
        var likeCount: Int = 0,
        var replyCount: Int = 0)