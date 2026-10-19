package com.ftsdoc.mobile.data.repository

import com.ftsdoc.mobile.data.local.UserPreferences
import com.ftsdoc.mobile.data.remote.CredentialHolder
import com.ftsdoc.mobile.data.remote.RemoteAuthDataSource
import com.ftsdoc.mobile.data.stub.StubAuthDataSource
import com.ftsdoc.mobile.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AuthRepositoryImpl(
    private val preferences: UserPreferences,
    private val remote: RemoteAuthDataSource,
    private val stub: StubAuthDataSource,
    private val credentials: CredentialHolder,
) : AuthRepository {
    override val isAuthorized: Flow<Boolean> = preferences.isAuthorized

    override suspend fun login(username: String, password: String): Result<Unit> = suspendResult {
        val trimmed = username.trim()
        if (preferences.useStubApi.first()) {
            stub.login(trimmed, password)
        } else {
            remote.login(trimmed, password)
        }
        credentials.update(trimmed, password)
    }

    override suspend fun logout() {
        preferences.clearSession()
        credentials.clear()
    }
}
