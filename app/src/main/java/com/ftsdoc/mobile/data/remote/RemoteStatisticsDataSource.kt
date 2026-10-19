package com.ftsdoc.mobile.data.remote

import com.ftsdoc.mobile.data.local.UserPreferences
import com.ftsdoc.mobile.domain.ServerEndpoints
import com.ftsdoc.mobile.domain.model.DocumentStatistics
import com.ftsdoc.mobile.domain.model.StatsSource
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

class RemoteStatisticsDataSource(
    private val api: ApiService,
    private val preferences: UserPreferences,
    private val gson: Gson,
) {
    suspend fun load(): DocumentStatistics {
        val settings = preferences.settings.first()
        val local = preferences.localStatistics.first()
        if (!ServerEndpoints.isValidBase(settings.serverUrl)) {
            return local.copy(notice = "Адрес сервера не задан. Показаны данные этого устройства.")
        }
        return try {
            val response = api.statistics(ServerEndpoints.statistics(settings.serverUrl))
            if (!response.isSuccessful) {
                response.errorBody()?.close()
                response.body()?.close()
                local.copy(
                    notice = "Сервер недоступен (${response.code()}). Показаны данные этого устройства.",
                )
            } else {
                val raw = response.body()?.string().orEmpty()
                val dto = gson.fromJson(raw, StatisticsDto::class.java)
                if (dto == null) {
                    local.copy(notice = "Сервер вернул пустой ответ. Показаны данные этого устройства.")
                } else {
                    DocumentStatistics(
                        documentsCount = dto.documentsCount,
                        pagesCount = dto.pagesCount,
                        source = StatsSource.REMOTE,
                    )
                }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            local.copy(notice = "Сервер недоступен. Показаны данные этого устройства.")
        }
    }
}

private class StatisticsDto {
    @SerializedName("documents_count")
    var documentsCount: Int = 0

    @SerializedName("pages_count")
    var pagesCount: Int = 0
}
