package com.ftsdoc.mobile.di

import android.content.Context
import com.ftsdoc.mobile.data.local.DocumentFileReader
import com.ftsdoc.mobile.data.local.UserPreferences
import com.ftsdoc.mobile.data.remote.CredentialHolder
import com.ftsdoc.mobile.data.remote.NetworkModule
import com.ftsdoc.mobile.data.remote.RemoteAuthDataSource
import com.ftsdoc.mobile.data.remote.RemoteDocumentDataSource
import com.ftsdoc.mobile.data.remote.RemoteStatisticsDataSource
import com.ftsdoc.mobile.data.remote.UploadBodyBuilder
import com.ftsdoc.mobile.data.repository.AuthRepositoryImpl
import com.ftsdoc.mobile.data.repository.DocumentRepositoryImpl
import com.ftsdoc.mobile.data.repository.SettingsRepositoryImpl
import com.ftsdoc.mobile.data.repository.StatisticsRepositoryImpl
import com.ftsdoc.mobile.data.stub.StubAuthDataSource
import com.ftsdoc.mobile.data.stub.StubDocumentDataSource
import com.ftsdoc.mobile.data.stub.StubStatisticsDataSource
import com.ftsdoc.mobile.domain.repository.AuthRepository
import com.ftsdoc.mobile.domain.repository.DocumentRepository
import com.ftsdoc.mobile.domain.repository.SettingsRepository
import com.ftsdoc.mobile.domain.repository.StatisticsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val preferences = UserPreferences(appContext)
    val credentials = CredentialHolder()
    val fileReader = DocumentFileReader(appContext)

    private val api = NetworkModule.create(appContext, credentials)
    private val uploadBodyBuilder = UploadBodyBuilder(NetworkModule.gson)

    val settingsRepository: SettingsRepository = SettingsRepositoryImpl(preferences)

    val authRepository: AuthRepository = AuthRepositoryImpl(
        preferences = preferences,
        remote = RemoteAuthDataSource(api, preferences, credentials),
        stub = StubAuthDataSource(preferences),
        credentials = credentials,
    )

    val documentRepository: DocumentRepository = DocumentRepositoryImpl(
        preferences = preferences,
        remote = RemoteDocumentDataSource(api, preferences, uploadBodyBuilder),
        stub = StubDocumentDataSource(preferences),
    )

    val statisticsRepository: StatisticsRepository = StatisticsRepositoryImpl(
        preferences = preferences,
        remote = RemoteStatisticsDataSource(api, preferences, NetworkModule.gson),
        stub = StubStatisticsDataSource(preferences),
    )

    init {
        val session = runBlocking(Dispatchers.IO) { preferences.session.first() }
        if (session.isAuthorized) {
            credentials.update(session.username, session.password)
        }
    }
}
