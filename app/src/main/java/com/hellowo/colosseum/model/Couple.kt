package com.hellowo.colosseum.model

import android.content.Context
import com.google.firebase.firestore.FieldValue
import com.hellowo.colosseum.R
import com.hellowo.colosseum.split
import java.io.Serializable
import java.util.*

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
        val maleQuestion: String ?= null,
        val femaleQuestion: String ?= null,
        val maleAnswer: String ?= null,
        val femaleAnswer: String ?= null,
        val maleAnswerLike: Int = -1,
        val femaleAnswerLike: Int = -1,
        val maleOx: String? = null,
        val femaleOx: String? = null,
        val maleLive: Boolean = false,
        val femaleLive: Boolean = false,
        val dtUpdated: Date = Date()): Serializable {

    fun makeMap() : HashMap<String, Any?>{
        val result = HashMap<String, Any?>()
        result.put("id", id)
        result.put("maleId", maleId)
        result.put("maleName", maleName)
        result.put("malePushToken", malePushToken)
        result.put("maleInterest", maleInterest)
        result.put("femaleId", femaleId)
        result.put("femaleName", femaleName)
        result.put("femalePushToken", femalePushToken)
        result.put("femaleInterest", femaleInterest)
        result.put("level", level)
        result.put("step", step)
        result.put("status", status)
        result.put("malePhotoUrl", malePhotoUrl)
        result.put("femalePhotoUrl", femalePhotoUrl)
        result.put("malePhotoLike", malePhotoLike)
        result.put("femalePhotoLike", femalePhotoLike)
        result.put("maleVoiceUrl", maleVoiceUrl)
        result.put("femaleVoiceUrl", femaleVoiceUrl)
        result.put("maleQuestion", maleQuestion)
        result.put("femaleQuestion", femaleQuestion)
        result.put("maleAnswer", maleAnswer)
        result.put("femaleAnswer", femaleAnswer)
        result.put("maleAnswerLike", maleAnswerLike)
        result.put("femaleAnswerLike", femaleAnswerLike)
        result.put("maleOx", maleOx)
        result.put("femaleOx", femaleOx)
        result.put("dtUpdated", FieldValue.serverTimestamp())
        return result
    }

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

        fun evaluateCheckList(myOxString: String?, yourOxString: String?) : Boolean {
            var count = 0
            (0 until myOxString?.length!!).forEach {
                if(myOxString[it] == yourOxString!![it]) count++
            }
            return count >= 5
        }
    }

    fun getIdByGender(gender: Int): String? = if(gender == 0) maleId else femaleId
    fun getNameByGender(gender: Int): String? = if(gender == 0) maleName else femaleName
    fun getPushTokenByGender(gender: Int): String? = if(gender == 0) malePushToken else femalePushToken
    fun getInterestByGender(gender: Int): Int? = if(gender == 0) maleInterest else femaleInterest
    fun getPhotoUrlByGender(gender: Int): String? = if(gender == 0) malePhotoUrl else femalePhotoUrl
    fun getPhotoLikeByGender(gender: Int): Int = if(gender == 0) malePhotoLike else femalePhotoLike
    fun getVoiceUrlByGender(gender: Int): String? = if(gender == 0) maleVoiceUrl else femaleVoiceUrl
    fun getVoiceLikeByGender(gender: Int): Int = if(gender == 0) maleVoiceLike else femaleVoiceLike
    fun getAnswerByGender(gender: Int): String? = if(gender == 0) maleAnswer else femaleAnswer
    fun getAnswerLikeByGender(gender: Int): Int = if(gender == 0) maleAnswerLike else femaleAnswerLike
    fun getOxByGender(gender: Int): String? = if(gender == 0) maleOx else femaleOx
    fun getLiveByGender(gender: Int): Boolean = if(gender == 0) maleLive else femaleLive

    fun getStepText(context: Context): String? {
        return when(step) {
            0 -> context.getString(R.string.step_1_title)
            1 -> context.getString(R.string.step_2_title)
            2 -> context.getString(R.string.step_3_title)
            3 -> context.getString(R.string.step_4_title)
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
        return when {
            status == 1 -> context.getString(R.string.success)
            status == 0 -> context.getString(R.string.ing)
            else -> context.getString(R.string.failed)
        }
    }

    fun getStatusColor(context: Context): Int? {
        return when {
            status == 1 -> context.resources.getColor(R.color.blue)
            status == 0 -> context.resources.getColor(R.color.colorPrimary)
            else -> context.resources.getColor(R.color.iconTint)
        }
    }

    fun questId(): Int {
        return maleName!![0].toInt() + femaleName!![1].toInt()
    }
}