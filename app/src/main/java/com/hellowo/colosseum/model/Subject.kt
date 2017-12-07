package com.hellowo.colosseum.model

import java.io.Serializable

data class Subject(
        var id: String ?= null,
        var title: String? = null,
        var img: String? = null) : Serializable