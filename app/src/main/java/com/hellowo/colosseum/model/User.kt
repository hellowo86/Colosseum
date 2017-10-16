package com.hellowo.colosseum.model

data class User (
        var id: String ?= null,
        var nickName: String? = null,
        var photoUrl: String? = null,
        var dtCreated: Long = 0)