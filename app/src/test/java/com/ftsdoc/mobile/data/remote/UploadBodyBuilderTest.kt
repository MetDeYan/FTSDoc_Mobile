package com.ftsdoc.mobile.data.remote

import com.ftsdoc.mobile.domain.model.DocumentFilePayload
import com.ftsdoc.mobile.domain.model.DocumentType
import com.ftsdoc.mobile.domain.model.DocumentUpload
import com.google.gson.Gson
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Base64

class UploadBodyBuilderTest {
    @Test
    fun documentJson_matchesUploadContract() {
        val json = UploadBodyBuilder(Gson()).documentJson(
            upload = DocumentUpload(
                documentName = "тест00",
                documentType = DocumentType(code = "ТН", title = "ТН"),
                files = listOf(
                    DocumentFilePayload(fileName = "ТН номер 10.jpg", bytes = "hi".toByteArray(), pageCount = 1),
                ),
            ),
            organizationId = "org-1",
            userId = "user-7",
        )
        val body = JsonParser.parseString(json).asJsonObject
        assertEquals("ТН", body.get("document_type").asString)
        assertEquals("тест00", body.get("document_name").asString)
        assertEquals("org-1", body.get("organization_id").asString)
        assertEquals("user-7", body.get("user_id").asString)
        val file = body.getAsJsonArray("files")[0].asJsonObject
        assertEquals("ТН номер 10.jpg", file.get("ИмяФайла").asString)
        assertEquals(
            Base64.getEncoder().encodeToString("hi".toByteArray()),
            file.get("Данные").asString,
        )
        assertEquals(1, file.get("КоличествоСтраниц").asInt)
    }
}
