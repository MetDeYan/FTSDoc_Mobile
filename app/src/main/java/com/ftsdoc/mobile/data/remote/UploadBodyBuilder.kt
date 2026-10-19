package com.ftsdoc.mobile.data.remote

import com.ftsdoc.mobile.domain.model.DocumentFilePayload
import com.ftsdoc.mobile.domain.model.DocumentUpload
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.util.Base64

class UploadBodyBuilder(private val gson: Gson) {
    fun documentJson(
        upload: DocumentUpload,
        organizationId: String,
        userId: String,
    ): String {
        val payload = UploadDocumentJson(
            documentType = upload.documentType.code,
            documentName = upload.documentName,
            files = upload.files.map { file -> file.toJson() },
            organizationId = organizationId,
            userId = userId,
        )
        return gson.toJson(payload)
    }

    private fun DocumentFilePayload.toJson(): UploadFileJson {
        return UploadFileJson(
            fileName = fileName,
            data = Base64.getEncoder().encodeToString(bytes),
            pageCount = pageCount.coerceAtLeast(1),
        )
    }
}

internal data class UploadDocumentJson(
    @SerializedName("document_type") val documentType: String,
    @SerializedName("document_name") val documentName: String,
    @SerializedName("files") val files: List<UploadFileJson>,
    @SerializedName("organization_id") val organizationId: String,
    @SerializedName("user_id") val userId: String,
)

internal data class UploadFileJson(
    @SerializedName("ИмяФайла") val fileName: String,
    @SerializedName("Данные") val data: String,
    @SerializedName("КоличествоСтраниц") val pageCount: Int,
)
