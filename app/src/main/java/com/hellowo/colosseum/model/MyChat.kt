package com.hellowo.colosseum.model

import java.io.Serializable
import java.util.*

data class MyChat(
        var id: String? = null,
        var title: String? = null,
        var hostId: String? = null,
        var lastMessage: String? = null,
        var lastMessageTime: Date = Date(),
        var messageCount: Int = 0,
        var lastConnectedTime: Long = 0): Serializable