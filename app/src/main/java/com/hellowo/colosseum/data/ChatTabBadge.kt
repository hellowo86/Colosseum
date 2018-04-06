package com.hellowo.colosseum.data

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import com.hellowo.colosseum.model.BadgeData
import io.realm.Realm
import io.realm.RealmResults

object ChatTabBadge : LiveData<Int>() {
    @SuppressLint("StaticFieldLeak")
    var resultList : RealmResults<BadgeData>? = null

    override fun onActive() {
        resultList = Realm.getDefaultInstance().where(BadgeData::class.java).equalTo("type", "chat").findAll()
        value = resultList?.sum("count")?.toInt()
        resultList?.addChangeListener { item -> value = resultList?.sum("count")?.toInt() }
    }

    override fun onInactive() {
        resultList?.removeAllChangeListeners()
    }
}