package com.ftsdoc.mobile.domain.model

data class PickedFile(
    val uri: String,
    val displayName: String,
    val mimeType: String,
    val pageCount: Int,
) {
    val isImage: Boolean get() = mimeType.startsWith("image/")
    val isPdf: Boolean get() = mimeType == "application/pdf"
}

data class DocumentFilePayload(
    val fileName: String,
    val bytes: ByteArray,
    val pageCount: Int,
)

data class DocumentUpload(
    val documentName: String,
    val documentType: DocumentType,
    val files: List<DocumentFilePayload>,
)

data class UploadResult(
    val fileCount: Int,
    val pageCount: Int,
)
