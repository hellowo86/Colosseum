package com.hellowo.colosseum.model

import java.io.Serializable
import java.util.*

data class Opinion(
        var id: String ?= null,
        var userId: String? = null,
        var userNickname: String? = null,
        var userPhotoUrl: String? = null,
        var accessLevel: Int = 0,
        var formType: Int = 0,
        var mainText: String? = null,
        var mainImgUrl: String? = null,
        var likeCnt: Int = 0,
        var extendedProperties: String? = null,
        var lastUpdated: Date? = null) : Serializable