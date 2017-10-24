package com.hellowo.colosseum.model

data class Issue(
        var id: String ?= null,
        var title: String? = null,
        var contents: List<Page>? = null)