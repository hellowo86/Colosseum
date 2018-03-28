package com.hellowo.colosseum.model

data class Message (
        var text: String? = null,
        var userName: String? = null,
        var userId: String? = null,
        var dtCreated: Long = 0,
        var type: Int = 0)