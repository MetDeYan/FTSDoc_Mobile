package com.ftsdoc.mobile.data.stub

import com.ftsdoc.mobile.data.local.UserPreferences
import com.ftsdoc.mobile.domain.model.DocumentUpload
import com.ftsdoc.mobile.domain.model.UploadResult
import kotlinx.coroutines.delay

/**
 * Локальная заглушка отправки: сервер не вызывается,
 * счётчики документов и страниц пишутся на устройство.
 */
class StubDocumentDataSource(
    private val preferences: UserPreferences,
) {
    suspend fun upload(upload: DocumentUpload): UploadResult {
        delay(STUB_DELAY_MS)
        val pages = upload.files.sumOf { file -> file.pageCount.coerceAtLeast(1) }
        preferences.incrementSent(documents = 1, pages = pages)
        return UploadResult(fileCount = upload.files.size, pageCount = pages)
    }

    private companion object {
        const val STUB_DELAY_MS = 800L
    }
}
