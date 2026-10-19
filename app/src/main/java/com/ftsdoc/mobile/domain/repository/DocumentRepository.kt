package com.ftsdoc.mobile.domain.repository

import com.ftsdoc.mobile.domain.model.DocumentUpload
import com.ftsdoc.mobile.domain.model.UploadResult

interface DocumentRepository {
    suspend fun upload(upload: DocumentUpload): Result<UploadResult>
}
