package com.hellowo.colosseum.model

import com.hellowo.colosseum.split
import java.io.Serializable

data class Couple(
        var id: String ?= null,
        var me: User? = null,
        var you: User? = null,
        var level: Int = 0,
        var step: Int = 0,
        var photoUrl0: String ?= null,
        var photoUrl1: String ?= null,
        var photoLike0: Int = -1,
        var photoLike1: Int = -1): Serializable {
    companion object {
        fun makeCoupleKey(me: User, you: User) : String {
            return if(me.gender == 0) {
                "${me.id}$split${you.id}"
            }else {
                "${you.id}$split${me.id}"
            }
        }
        fun getYourIdFromCoupleKey(myGender: Int, coupleKey: String): String = coupleKey.split(split)[if(myGender == 0) 1 else 0]
    }
}