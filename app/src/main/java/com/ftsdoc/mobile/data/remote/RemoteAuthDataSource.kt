package com.ftsdoc.mobile.data.remote

import com.ftsdoc.mobile.data.local.UserPreferences
import com.ftsdoc.mobile.domain.ServerEndpoints
import com.ftsdoc.mobile.domain.model.UserMessageException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

class RemoteAuthDataSource(
    private val api: ApiService,
    private val preferences: UserPreferences,
    private val credentials: CredentialHolder,
) {
    suspend fun login(username: String, password: String) {
        val settings = preferences.settings.first()
        if (!ServerEndpoints.isValidBase(settings.serverUrl)) {
            throw UserMessageException("Укажите корректный адрес сервера")
        }
        val previous = credentials.snapshot()
        credentials.update(username, password)
        try {
            val response = api.authorize(ServerEndpoints.authorization(settings.serverUrl))
            if (response.isSuccessful) {
                response.body()?.close()
                preferences.saveSession(username, password)
            } else {
                val raw = response.errorBody()?.string()
                credentials.restore(previous)
                throw httpError(response.code(), raw)
            }
        } catch (error: CancellationException) {
            credentials.restore(previous)
            throw error
        } catch (error: UserMessageException) {
            throw error
        } catch (error: Exception) {
            credentials.restore(previous)
            throw error
        }
    }
}
