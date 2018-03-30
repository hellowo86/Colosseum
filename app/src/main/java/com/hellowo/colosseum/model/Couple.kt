package com.hellowo.colosseum.model

import android.content.Context
import com.hellowo.colosseum.R
import com.hellowo.colosseum.split
import java.io.Serializable

data class Couple(
        var id: String ?= null,
        var maleId: String ?= null,
        var maleName: String ?= null,
        var malePushToken: String ?= null,
        var maleInterest: Int = 0,
        var femaleId: String ?= null,
        var femaleName: String ?= null,
        var femalePushToken: String ?= null,
        var femaleInterest: Int = 0,
        var level: Int = 0,
        var step: Int = 0,
        val status: Int = 0,
        var malePhotoUrl: String ?= null,
        var femalePhotoUrl: String ?= null,
        var malePhotoLike: Int = -1,
        var femalePhotoLike: Int = -1,
        var maleVoiceUrl: String ?= null,
        var femaleVoiceUrl: String ?= null,
        var maleVoiceLike: Int = -1,
        var femaleVoiceLike: Int = -1,
        val maleQuestion: Array<String?> = arrayOfNulls(3),
        val femaleQuestion: Array<String?> = arrayOfNulls(3),
        val maleAnswer: Array<String?> = arrayOfNulls(3),
        val femaleAnswer: Array<String?> = arrayOfNulls(3),
        val maleAnswerLike: Int = -1,
        val femaleAnswerLike: Int = -1,
        val maleOx: String? = null,
        val femaleOx: String? = null): Serializable {
    companion object {
        fun makeCoupleKey(me: User, you: User) : String {
            return if(me.gender == 0) {
                "${me.id}${you.id}"
            }else {
                "${you.id}${me.id}"
            }
        }
        fun getYourIdFromCoupleKey(myGender: Int, coupleKey: String): String = coupleKey.split(split)[if(myGender == 0) 1 else 0]
        fun getGenderKey(gender: Int): String = if(gender == 0) "male" else "female"
    }

    fun getIdByGender(gender: Int): String? = if(gender == 0) maleId else femaleId
    fun getNameByGender(gender: Int): String? = if(gender == 0) maleName else femaleName
    fun getPushTokenByGender(gender: Int): String? = if(gender == 0) malePushToken else femalePushToken
    fun getInterestByGender(gender: Int): Int? = if(gender == 0) maleInterest else femaleInterest
    fun getPhotoUrlByGender(gender: Int): String? = if(gender == 0) malePhotoUrl else femalePhotoUrl
    fun getPhotoLikeByGender(gender: Int): Int = if(gender == 0) malePhotoLike else femalePhotoLike
    fun getVoiceUrlByGender(gender: Int): String? = if(gender == 0) maleVoiceUrl else femaleVoiceUrl
    fun getVoiceLikeByGender(gender: Int): Int = if(gender == 0) maleVoiceLike else femaleVoiceLike
    fun getQuestionByGender(gender: Int): Array<String?> = if(gender == 0) maleQuestion else femaleQuestion
    fun getAnswerByGender(gender: Int): Array<String?> = if(gender == 0) maleAnswer else femaleAnswer
    fun getAnswerLikeByGender(gender: Int): Int = if(gender == 0) maleAnswerLike else femaleAnswerLike
    fun getOxByGender(gender: Int): String? = if(gender == 0) maleOx else femaleOx

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