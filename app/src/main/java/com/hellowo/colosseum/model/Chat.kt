package com.hellowo.colosseum.model

import java.io.Serializable

data class Chat(
        var id: String? = null,
        var host: String? = null,
        var title: String? = null): Serializable {
    fun makeChatDataMap(): Map<String, Any?> {
        val map = HashMap<String, Any?>()
        map.put("id", id)
        map.put("title", title?.trim())
        map.put("host", host)
        title?.split(" ")?.forEach {
            if(it.isNotEmpty()) {
                map.put("s_$it", true)
            }
        }
        return map
    }
    fun makeMyChatDataMap(): Map<String, Any?> {
        val map = HashMap<String, Any?>()
        map.put("id", id)
        return map
    }
}