package com.hellowo.colosseum.model

import java.util.*

data class Comment(
        var id: String ?= null,
        var userId: String? = null,
        var userName: String? = null,
        var contents: String? = null,
        var lastUpdated: Date? = null)