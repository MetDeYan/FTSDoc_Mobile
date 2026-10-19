package com.ftsdoc.mobile.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isAuthorized: Flow<Boolean>

    suspend fun login(username: String, password: String): Result<Unit>

    suspend fun logout()
}
