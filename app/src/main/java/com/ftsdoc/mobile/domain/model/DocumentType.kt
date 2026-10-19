package com.ftsdoc.mobile.domain.model

data class DocumentType(
    val code: String,
    val title: String,
)

object DocumentCatalog {
    val types: List<DocumentType> = listOf(
        DocumentType(code = "ТТН", title = "ТТН"),
        DocumentType(code = "ТН", title = "ТН"),
    )
}
