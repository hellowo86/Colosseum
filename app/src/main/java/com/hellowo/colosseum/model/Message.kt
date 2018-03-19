package com.hellowo.colosseum.model

import java.io.Serializable
import java.util.*

data class Message (
        val id: String? = null,
        var text: String? = null,
        var userName: String? = null,
        var userId: String? = null,
        var dtCreated: Long = 0,
        var type: Int = 0) : Serializable {
    //type 0 = normal
    //type 1 = entered
}