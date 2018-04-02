package com.hellowo.colosseum.model

import java.io.Serializable

data class Comment(
        var text: String? = null,
        var userName: String? = null,
        var userId: String? = null,
        var userPushId: String? = null,
        var dtCreated: Long = 0,
        val like: Int = 0,
        val childList: ArrayList<Comment> = ArrayList(),
        var parent: Comment? = null) : Serializable