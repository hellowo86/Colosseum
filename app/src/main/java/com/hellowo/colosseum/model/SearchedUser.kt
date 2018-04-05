package com.hellowo.colosseum.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class SearchedUser(@PrimaryKey var id: String? = null): RealmObject()