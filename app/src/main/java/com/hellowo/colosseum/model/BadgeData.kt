package com.hellowo.colosseum.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class BadgeData(@PrimaryKey var id: String? = null,
                     var type: String? = null,
                     var count : Int = 0): RealmObject()