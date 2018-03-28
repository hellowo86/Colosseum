package com.hellowo.colosseum.model

import java.io.Serializable

data class MyChat(
        var id: String? = null,
        var title: String? = null,
        var hostId: String? = null,
        var lastMessage: String? = null,
        var lastMessageTime: Long = 0,
        var messageCount: Int = 0,
        var lastConnectedTime: Long = 0): Serializable