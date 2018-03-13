package com.hellowo.colosseum.model

data class Issue(
        var id: String ?= null,
        var title: String? = null,
        var contents: List<Page>? = null,
        var leftCnt: Int = 0,
        var midCnt: Int = 0,
        var rightCnt: Int = 0)