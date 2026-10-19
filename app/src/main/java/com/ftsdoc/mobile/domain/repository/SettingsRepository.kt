package com.ftsdoc.mobile.domain.repository

import com.ftsdoc.mobile.domain.model.OrganizationSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<OrganizationSettings>
    val useStubApi: Flow<Boolean>

    suspend fun save(settings: OrganizationSettings)

    suspend fun updateServerUrl(serverUrl: String)

    suspend fun setUseStubApi(enabled: Boolean)
}
