package com.hellowo.colosseum.model

import java.io.Serializable

data class Issue(
        var id: String? = null,
        var title: String? = null,
        var description: String? = null): Serializable