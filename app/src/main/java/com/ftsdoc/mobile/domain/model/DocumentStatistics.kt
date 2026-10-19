package com.ftsdoc.mobile.domain.model

enum class StatsSource {
    REMOTE,
    LOCAL,
}

data class DocumentStatistics(
    val documentsCount: Int = 0,
    val pagesCount: Int = 0,
    val source: StatsSource = StatsSource.LOCAL,
    val notice: String? = null,
)
