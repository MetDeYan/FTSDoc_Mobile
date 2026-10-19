package com.ftsdoc.mobile.data.repository

import com.ftsdoc.mobile.data.local.UserPreferences
import com.ftsdoc.mobile.data.remote.RemoteDocumentDataSource
import com.ftsdoc.mobile.data.stub.StubDocumentDataSource
import com.ftsdoc.mobile.domain.model.DocumentUpload
import com.ftsdoc.mobile.domain.model.UploadResult
import com.ftsdoc.mobile.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.first

class DocumentRepositoryImpl(
    private val preferences: UserPreferences,
    private val remote: RemoteDocumentDataSource,
    private val stub: StubDocumentDataSource,
) : DocumentRepository {
    override suspend fun upload(upload: DocumentUpload): Result<UploadResult> = suspendResult {
        if (preferences.useStubApi.first()) {
            stub.upload(upload)
        } else {
            remote.upload(upload)
        }
    }
}
