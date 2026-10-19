package com.ftsdoc.mobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ftsdoc.mobile.domain.model.DEFAULT_SERVER_URL
import com.ftsdoc.mobile.domain.model.DocumentStatistics
import com.ftsdoc.mobile.domain.model.OrganizationSettings
import com.ftsdoc.mobile.domain.model.StatsSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "fts_doc")

data class StoredSession(
    val isAuthorized: Boolean,
    val username: String,
    val password: String,
)

class UserPreferences(context: Context) {
    private val store = context.applicationContext.dataStore

    val session: Flow<StoredSession> = store.data.map { prefs ->
        StoredSession(
            isAuthorized = prefs[Keys.AUTHORIZED] == true,
            username = prefs[Keys.USERNAME].orEmpty(),
            password = prefs[Keys.PASSWORD].orEmpty(),
        )
    }

    val isAuthorized: Flow<Boolean> = session.map { it.isAuthorized }

    val settings: Flow<OrganizationSettings> = store.data.map { prefs ->
        OrganizationSettings(
            name = prefs[Keys.ORG_NAME].orEmpty(),
            organizationId = prefs[Keys.ORG_ID].orEmpty(),
            serverUrl = prefs[Keys.SERVER_URL] ?: DEFAULT_SERVER_URL,
        )
    }

    val useStubApi: Flow<Boolean> = store.data.map { prefs ->
        prefs[Keys.USE_STUB] ?: true
    }

    val localStatistics: Flow<DocumentStatistics> = store.data.map { prefs ->
        DocumentStatistics(
            documentsCount = prefs[Keys.SENT_DOCS] ?: 0,
            pagesCount = prefs[Keys.SENT_PAGES] ?: 0,
            source = StatsSource.LOCAL,
        )
    }

    suspend fun saveSession(username: String, password: String) {
        store.edit { prefs ->
            prefs[Keys.AUTHORIZED] = true
            prefs[Keys.USERNAME] = username.trim()
            prefs[Keys.PASSWORD] = password
        }
    }

    suspend fun clearSession() {
        store.edit { prefs ->
            prefs[Keys.AUTHORIZED] = false
            prefs.remove(Keys.PASSWORD)
        }
    }

    suspend fun saveSettings(settings: OrganizationSettings) {
        store.edit { prefs ->
            prefs[Keys.ORG_NAME] = settings.name.trim()
            prefs[Keys.ORG_ID] = settings.organizationId.trim()
            prefs[Keys.SERVER_URL] = settings.serverUrl.trim()
            prefs.remove(Keys.OPERATOR)
        }
    }

    suspend fun updateServerUrl(serverUrl: String) {
        store.edit { prefs ->
            prefs[Keys.SERVER_URL] = serverUrl.trim()
        }
    }

    suspend fun setUseStubApi(enabled: Boolean) {
        store.edit { prefs ->
            prefs[Keys.USE_STUB] = enabled
        }
    }

    suspend fun incrementSent(documents: Int, pages: Int) {
        store.edit { prefs ->
            prefs[Keys.SENT_DOCS] = (prefs[Keys.SENT_DOCS] ?: 0) + documents
            prefs[Keys.SENT_PAGES] = (prefs[Keys.SENT_PAGES] ?: 0) + pages
        }
    }

    private object Keys {
        val AUTHORIZED = booleanPreferencesKey("authorized")
        val USERNAME = stringPreferencesKey("username")
        val PASSWORD = stringPreferencesKey("password")
        val ORG_NAME = stringPreferencesKey("org_name")
        val ORG_ID = stringPreferencesKey("org_id")
        val SERVER_URL = stringPreferencesKey("server_url")
        val OPERATOR = stringPreferencesKey("operator_name")
        val USE_STUB = booleanPreferencesKey("use_stub_api")
        val SENT_DOCS = intPreferencesKey("sent_documents")
        val SENT_PAGES = intPreferencesKey("sent_pages")
    }
}
