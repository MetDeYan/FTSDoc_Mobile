package com.ftsdoc.mobile.data.remote

import com.ftsdoc.mobile.domain.model.DocumentFilePayload
import com.google.gson.Gson
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Base64

class UploadBodyBuilderTest {
    @Test
    fun filesJson_containsFileNameAndBase64() {
        val json = UploadBodyBuilder(Gson()).filesJson(
            listOf(DocumentFilePayload(fileName = "scan.jpg", bytes = "hi".toByteArray(), pageCount = 1)),
        )
        val item = JsonParser.parseString(json).asJsonArray[0].asJsonObject
        assertEquals("scan.jpg", item.get("ИмяФайла").asString)
        assertEquals(
            Base64.getEncoder().encodeToString("hi".toByteArray()),
            item.get("Данные").asString,
        )
    }
}
