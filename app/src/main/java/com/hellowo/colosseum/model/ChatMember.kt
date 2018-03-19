package com.hellowo.colosseum.model

import java.io.Serializable

data class ChatMember (
        var userId: String? = null,
        var name: String? = null,
        var role: String? = null,
        var lastConnectedTime: Long = 0,
        var live: Boolean = false,
        var pushToken: String? = null) : Serializable {
}