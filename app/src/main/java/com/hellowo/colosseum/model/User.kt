package com.hellowo.colosseum.model

import java.util.*

data class User (
        var id: String ?= null,
        var nickName: String? = null,
        var gender: Int = 0,
        var age: Int = 0,
        var latlng: String? = null,
        var isOnline: Boolean = false,
        var lastConnectedTime: Date? = null,
        var photoUrl: String? = null,
        var createdTime: Date? = null)