package com.ftsdoc.mobile.data.stub

import com.ftsdoc.mobile.data.local.UserPreferences
import com.ftsdoc.mobile.domain.model.DocumentStatistics
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/**
 * Локальная заглушка статистики: цифры берутся из DataStore,
 * куда они попадают после каждой успешной отправки.
 */
class StubStatisticsDataSource(
    private val preferences: UserPreferences,
) {
    suspend fun load(): DocumentStatistics {
        delay(STUB_DELAY_MS)
        return preferences.localStatistics.first().copy(
            notice = "Локальный режим. Показаны данные этого устройства.",
        )
    }

    private companion object {
        const val STUB_DELAY_MS = 300L
    }
}
