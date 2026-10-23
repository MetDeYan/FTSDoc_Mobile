package com.ftsdoc.mobile.data.remote

import com.ftsdoc.mobile.data.local.UserPreferences
import com.ftsdoc.mobile.domain.ServerEndpoints
import com.ftsdoc.mobile.domain.model.DocumentStatistics
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
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
        val userId = settings.userId.trim()
        if (userId.isEmpty()) {
            return local.copy(
                notice = "Не задан идентификатор пользователя. Показаны данные этого устройства.",
            )
        }
        return try {
            val response = api.statistics(
                ServerEndpoints.statistics(settings.serverUrl),
                userId,
            )
            if (!response.isSuccessful) {
                response.errorBody()?.close()
                response.body()?.close()
                local.copy(
                    notice = "Сервер недоступен (${response.code()}). Показаны данные этого устройства.",
                )
            } else {
                val raw = response.body()?.string().orEmpty()
                when (val parsed = parseStatisticsText(raw, gson)) {
                    is StatisticsTextParse.Text -> local.copy(serverText = parsed.value)
                    StatisticsTextParse.Empty ->
                        local.copy(notice = "Сервер вернул пустой ответ. Показаны данные этого устройства.")
                    StatisticsTextParse.Invalid ->
                        local.copy(notice = "Сервер вернул некорректный ответ. Показаны данные этого устройства.")
                }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            local.copy(notice = "Сервер недоступен. Показаны данные этого устройства.")
        }
    }
}

internal sealed class StatisticsTextParse {
    data class Text(val value: String) : StatisticsTextParse()
    data object Empty : StatisticsTextParse()
    data object Invalid : StatisticsTextParse()
}

internal fun parseStatisticsText(raw: String, gson: Gson): StatisticsTextParse {
    if (raw.isBlank()) return StatisticsTextParse.Empty
    val dto = try {
        gson.fromJson(raw, StatisticsDto::class.java)
    } catch (_: JsonSyntaxException) {
        return StatisticsTextParse.Invalid
    } ?: return StatisticsTextParse.Empty
    val text = dto.statistics
        ?.replace("\r\n", "\n")
        ?.replace('\r', '\n')
        ?.trim()
        .orEmpty()
    return if (text.isEmpty()) StatisticsTextParse.Empty else StatisticsTextParse.Text(text)
}

private class StatisticsDto {
    @SerializedName("statistics")
    var statistics: String? = null
}
