package com.ftsdoc.mobile.data.repository

import com.ftsdoc.mobile.data.local.UserPreferences
import com.ftsdoc.mobile.data.remote.RemoteStatisticsDataSource
import com.ftsdoc.mobile.data.stub.StubStatisticsDataSource
import com.ftsdoc.mobile.domain.model.DocumentStatistics
import com.ftsdoc.mobile.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.first

class StatisticsRepositoryImpl(
    private val preferences: UserPreferences,
    private val remote: RemoteStatisticsDataSource,
    private val stub: StubStatisticsDataSource,
) : StatisticsRepository {
    override suspend fun load(): Result<DocumentStatistics> = suspendResult {
        if (preferences.useStubApi.first()) {
            stub.load()
        } else {
            remote.load()
        }
    }
}
