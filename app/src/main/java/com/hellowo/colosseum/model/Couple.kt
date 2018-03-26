package com.hellowo.colosseum.model

import com.hellowo.colosseum.split
import java.io.Serializable

data class Couple(
        var id: String ?= null,
        var me: User,
        var you: User,
        var level: Int = 0): Serializable {
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