package com.hellowo.colosseum.data

import android.content.Context
import com.hellowo.colosseum.R

object ChemistryQuest {
    val titleListMap = HashMap<Int, Array<String>>()
    val subListMap = HashMap<Int, Array<String>>()
    val imageResourcMap = HashMap<Int, Array<Int>>()
    val questMaxCount = Array(4, {_ -> 0})
    val letter = arrayOf(R.drawable.l_1, R.drawable.l_2, R.drawable.l_3)

    fun init(context: Context) {
        var title = context.resources.getStringArray(R.array.step_1_quest_title)
        questMaxCount[0] = title.size
        titleListMap.put(0, title)
        subListMap.put(0, context.resources.getStringArray(R.array.step_1_quest_sub))
        imageResourcMap.put(0, arrayOf(R.drawable.q1_1, R.drawable.q1_2, R.drawable.q1_3, R.drawable.q1_4, R.drawable.q1_5, R.drawable.q1_6))

        title = context.resources.getStringArray(R.array.step_2_quest_title)
        questMaxCount[1] = title.size
        titleListMap.put(1, title)
        subListMap.put(1, context.resources.getStringArray(R.array.step_2_quest_sub))
        imageResourcMap.put(1, arrayOf(R.drawable.q2_1, R.drawable.q2_2))

        title = context.resources.getStringArray(R.array.step_3_quest_title)
        questMaxCount[2] = title.size
        titleListMap.put(2, title)
        subListMap.put(2, context.resources.getStringArray(R.array.step_3_quest_sub))
        imageResourcMap.put(2, arrayOf(R.drawable.q3_1, R.drawable.q3_2, R.drawable.q3_3, R.drawable.q3_4, R.drawable.q3_5, R.drawable.q3_6, R.drawable.q3_7))
    }

    fun questImgResource(step: Int, questId: Int): Int {
        return imageResourcMap[step]?.get(questId % imageResourcMap[step]?.size!!)!!
    }

    fun questTitle(step: Int, questId: Int): String {
        return titleListMap[step]?.get(questId % titleListMap[step]?.size!!)!!
    }

    fun questSub(step: Int, questId: Int): String {
        return subListMap[step]?.get(questId % subListMap[step]?.size!!)!!
    }

    fun letterImg(questId: Int): Int {
        return letter[questId % letter.size]
    }

    fun questIconResource(step: Int): Int {
        return when(step) {
            0 -> R.drawable.ic_image_black_48dp
            1 -> R.drawable.ic_mic_black_48dp
            2 -> R.drawable.ic_lightbulb_outline_black_48dp
            3 -> R.drawable.ic_lightbulb_outline_black_48dp
            else -> R.drawable.ic_sentiment_dissatisfied_black_48dp
        }
    }
}