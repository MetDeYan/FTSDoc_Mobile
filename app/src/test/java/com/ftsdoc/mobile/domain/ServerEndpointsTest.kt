package com.ftsdoc.mobile.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ServerEndpointsTest {
    @Test
    fun authorization_appendsImportPath() {
        assertEquals(
            "https://localhost:443/dev010AI/hs/import/authorization/",
            ServerEndpoints.authorization("https://localhost:443/dev010AI/hs"),
        )
    }

    @Test
    fun documents_keepsSingleTrailingSlash() {
        assertEquals(
            "https://localhost:443/dev010AI/hs/import/docs/",
            ServerEndpoints.documents("https://localhost:443/dev010AI/hs/"),
        )
    }

    @Test
    fun documents_doesNotDuplicateExistingPath() {
        assertEquals(
            "https://10.0.2.2:443/dev010AI/hs/import/docs/",
            ServerEndpoints.documents("https://10.0.2.2:443/dev010AI/hs/import/docs"),
        )
    }

    @Test
    fun isValidBase_acceptsHttpAndHttps() {
        assertTrue(ServerEndpoints.isValidBase("https://localhost:443/dev010AI/hs"))
        assertTrue(ServerEndpoints.isValidBase("http://10.0.2.2:8080/dev"))
        assertFalse(ServerEndpoints.isValidBase("localhost:443/dev"))
        assertFalse(ServerEndpoints.isValidBase(""))
    }
}
