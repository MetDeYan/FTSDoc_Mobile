package com.ftsdoc.mobile.data.remote

import com.ftsdoc.mobile.data.local.UserPreferences
import com.ftsdoc.mobile.domain.ServerEndpoints
import com.ftsdoc.mobile.domain.model.DocumentUpload
import com.ftsdoc.mobile.domain.model.UploadResult
import com.ftsdoc.mobile.domain.model.UserMessageException
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class RemoteDocumentDataSource(
    private val api: ApiService,
    private val preferences: UserPreferences,
    private val uploadBodyBuilder: UploadBodyBuilder,
) {
    suspend fun upload(upload: DocumentUpload): UploadResult {
        val settings = preferences.settings.first()
        if (!ServerEndpoints.isValidBase(settings.serverUrl)) {
            throw UserMessageException("Укажите адрес сервера в настройках организации")
        }
        val parts = listOf(
            MultipartBody.Part.createFormData("document_name", upload.documentName),
            MultipartBody.Part.createFormData("document_type", upload.documentType.code),
            MultipartBody.Part.createFormData(
                "files",
                null,
                uploadBodyBuilder.filesJson(upload.files).toRequestBody(JSON),
            ),
        )
        val response = api.uploadDocument(ServerEndpoints.documents(settings.serverUrl), parts)
        if (!response.isSuccessful) {
            val raw = response.errorBody()?.string()
            throw httpError(
                code = response.code(),
                body = raw,
                unauthorized = "Сервер отклонил авторизацию. Войдите снова.",
            )
        }
        response.body()?.close()
        val pages = upload.files.sumOf { file -> file.pageCount.coerceAtLeast(1) }
        preferences.incrementSent(documents = 1, pages = pages)
        return UploadResult(fileCount = upload.files.size, pageCount = pages)
    }

    private companion object {
        val JSON = "application/json; charset=utf-8".toMediaType()
    }
}
