package com.hellowo.colosseum.model

import java.io.Serializable

data class Chat(
        var id: String? = null,
        var title: String? = null,
        var description: String? = null,
        var maxMemberCount: Int? = 2,
        var dtCreated: Long = System.currentTimeMillis(),
        var hostId: String? = null,
        var lastMessage: String? = null,
        var lastMessageTime: Long = 0,
        var messageCount: Int = 0,
        var lat: Double = 0.0,
        var lng: Double = 0.0,
        var location: String? = null,
        var dtUpdated: Long = 0,
        var maleId: String ?= null,
        var maleName: String ?= null,
        var femaleId: String ?= null,
        var femaleName: String ?= null): Serializable {

    fun getIdByGender(gender: Int): String? = if(gender == 0) maleId else femaleId

    fun getNameByGender(gender: Int): String? = if(gender == 0) maleName else femaleName
}
