package com.hellowo.colosseum.model

data class ChatMember (
        var userId: String? = null,
        var name: String? = null,
        var lastConnectedTime: Long = 0,
        var live: Boolean = false,
        var pushToken: String? = null)