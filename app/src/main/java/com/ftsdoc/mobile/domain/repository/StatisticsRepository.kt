package com.ftsdoc.mobile.domain.repository

import com.ftsdoc.mobile.domain.model.DocumentStatistics

interface StatisticsRepository {
    suspend fun load(): Result<DocumentStatistics>
}
