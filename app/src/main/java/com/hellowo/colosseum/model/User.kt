package com.hellowo.colosseum.model

import java.io.Serializable

data class User (
        var id: String ?= null,
        var nickName: String? = null,
        var email: String? = null,
        var gender: Int = 0,
        var birth: Int = 0,
        var lat: Double = 0.0,
        var lng: Double = 0.0,
        var location: String? = null,
        var moreInfo: String? = null,
        var dtConnected: Long = 0,
        var dtCreated: Long = 0,
        var pushToken: String? = null): Serializable {
}