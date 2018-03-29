package com.hellowo.colosseum.model

import android.content.Context
import com.hellowo.colosseum.R
import com.hellowo.colosseum.split
import java.io.Serializable

data class Couple(
        var id: String ?= null,
        var me: User? = null,
        var you: User? = null,
        var level: Int = 0,
        var step: Int = 0,
        val status: Int = 0,
        var photoUrl0: String ?= null,
        var photoUrl1: String ?= null,
        var photoLike0: Int = -1,
        var photoLike1: Int = -1,
        var voiceUrl0: String ?= null,
        var voiceUrl1: String ?= null,
        var voiceLike0: Int = -1,
        var voiceLike1: Int = -1,
        val question0: Array<String?> = arrayOfNulls(3),
        val question1: Array<String?> = arrayOfNulls(3),
        val answer0: Array<String?> = arrayOfNulls(3),
        val answer1: Array<String?> = arrayOfNulls(3),
        val answerLike0: Int = -1,
        val answerLike1: Int = -1,
        val ox0: String? = null,
        val ox1: String? = null): Serializable {
    companion object {
        fun makeCoupleKey(me: User, you: User) : String {
            return if(me.gender == 0) {
                "${me.id}$split${you.id}"
            }else {
                "${you.id}$split${me.id}"
            }
        }
        fun makePhotoUrlPath(me: User, you: User) : String {
            return if(me.gender == 0) {
                "${me.id}${you.id}"
            }else {
                "${you.id}${me.id}"
            }
        }
        fun getYourIdFromCoupleKey(myGender: Int, coupleKey: String): String = coupleKey.split(split)[if(myGender == 0) 1 else 0]
    }

    fun getPhotoUrlByGender(gender: Int): String? = if(gender == 0) photoUrl0 else photoUrl1
    fun getPhotoLikeByGender(gender: Int): Int = if(gender == 0) photoLike0 else photoLike1
    fun getVoiceUrlByGender(gender: Int): String? = if(gender == 0) voiceUrl0 else voiceUrl1
    fun getVoiceLikeByGender(gender: Int): Int = if(gender == 0) voiceLike0 else voiceLike1
    fun getQuestionByGender(gender: Int): Array<String?> = if(gender == 0) question0 else question1
    fun getAnswerByGender(gender: Int): Array<String?> = if(gender == 0) answer0 else answer1
    fun getAnswerLikeByGender(gender: Int): Int = if(gender == 0) answerLike0 else answerLike1
    fun getOxByGender(gender: Int): String? = if(gender == 0) ox0 else ox1

    fun getStepText(context: Context): String? {
        return when(step) {
            0 -> context.getString(R.string.show_another_photo)
            1 -> context.getString(R.string.check_voice)
            2 -> context.getString(R.string.qna)
            3 -> context.getString(R.string.blind_test)
            else -> null
        }
    }

    fun getStepSubText(context: Context): String? {
        return when(step) {
            0 -> context.getString(R.string.show_another_photo_sub)
            1 -> context.getString(R.string.check_voice_sub)
            2 -> context.getString(R.string.qna_sub)
            3 -> context.getString(R.string.blind_test_sub)
            else -> null
        }
    }

    fun getStatusText(context: Context): String? {
        return when(status) {
            -1 -> context.getString(R.string.failed)
            0 -> context.getString(R.string.ing)
            1 -> context.getString(R.string.success)
            else -> null
        }
    }

    fun getStatusColor(context: Context): Int? {
        return when(status) {
            -1 -> context.resources.getColor(R.color.disableText)
            0 -> context.resources.getColor(R.color.blue)
            1 -> context.resources.getColor(R.color.colorPrimary)
            else -> null
        }
    }
}