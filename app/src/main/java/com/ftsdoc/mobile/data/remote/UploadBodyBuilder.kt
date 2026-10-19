package com.ftsdoc.mobile.data.remote

import com.ftsdoc.mobile.domain.model.DocumentFilePayload
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.util.Base64

class UploadBodyBuilder(private val gson: Gson) {
    fun filesJson(files: List<DocumentFilePayload>): String {
        val payload = files.map { file ->
            UploadFileJson(
                fileName = file.fileName,
                data = Base64.getEncoder().encodeToString(file.bytes),
            )
        }
        return gson.toJson(payload)
    }
}

internal data class UploadFileJson(
    @SerializedName("ИмяФайла") val fileName: String,
    @SerializedName("Данные") val data: String,
)
