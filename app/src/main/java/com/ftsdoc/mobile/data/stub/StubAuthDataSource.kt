package com.ftsdoc.mobile.data.stub

import com.ftsdoc.mobile.data.local.UserPreferences
import com.ftsdoc.mobile.domain.model.UserMessageException
import kotlinx.coroutines.delay

/**
 * Локальная заглушка входа: сеть не вызывается.
 * Любые непустые логин и пароль считаются верными.
 */
class StubAuthDataSource(
    private val preferences: UserPreferences,
) {
    suspend fun login(username: String, password: String) {
        delay(STUB_DELAY_MS)
        if (username.isBlank() || password.isBlank()) {
            throw UserMessageException("Введите логин и пароль")
        }
        preferences.saveSession(username, password)
    }

    private companion object {
        const val STUB_DELAY_MS = 500L
    }
}
