package com.ftsdoc.mobile.data.repository

import com.ftsdoc.mobile.data.local.UserPreferences
import com.ftsdoc.mobile.domain.model.OrganizationSettings
import com.ftsdoc.mobile.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(
    private val preferences: UserPreferences,
) : SettingsRepository {
    override val settings: Flow<OrganizationSettings> = preferences.settings
    override val useStubApi: Flow<Boolean> = preferences.useStubApi

    override suspend fun save(settings: OrganizationSettings) {
        preferences.saveSettings(settings)
    }

    override suspend fun updateServerUrl(serverUrl: String) {
        preferences.updateServerUrl(serverUrl)
    }

    override suspend fun setUseStubApi(enabled: Boolean) {
        preferences.setUseStubApi(enabled)
    }
}
