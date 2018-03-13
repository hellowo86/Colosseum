package com.hellowo.colosseum.model

import java.util.*

data class Thread(
        var id: String ?= null,
        var userId: String? = null,
        var userName: String? = null,
        var contents: String? = null,
        var direction: Int = 0,
        var comments: List<Comment>? = null,
        var lastUpdated: Date? = null)