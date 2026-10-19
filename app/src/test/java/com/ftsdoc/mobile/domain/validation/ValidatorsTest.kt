package com.ftsdoc.mobile.domain.validation

import com.ftsdoc.mobile.domain.model.DocumentCatalog
import com.ftsdoc.mobile.domain.model.OrganizationSettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {
    @Test
    fun login_requiresBothFields() {
        assertTrue(LoginValidator.validate("", "").hasAny)
        assertTrue(LoginValidator.validate("user", "   ").password)
        assertFalse(LoginValidator.validate("user", "secret").hasAny)
    }

    @Test
    fun settings_requiresNameIdAndUrl() {
        val errors = SettingsValidator.validate(
            OrganizationSettings(name = " ", organizationId = "", serverUrl = "not a url"),
        )
        assertTrue(errors.name)
        assertTrue(errors.organizationId)
        assertTrue(errors.userId)
        assertTrue(errors.serverUrl)
        assertFalse(
            SettingsValidator.validate(
                OrganizationSettings(
                    name = "Орг",
                    organizationId = "10",
                    userId = "42",
                    serverUrl = "https://10.0.2.2:443/dev010AI/hs",
                ),
            ).hasAny,
        )
    }

    @Test
    fun document_requiresNameTypeAndFile() {
        val errors = DocumentValidator.validate(" ", null, 0)
        assertTrue(errors.name)
        assertTrue(errors.type)
        assertTrue(errors.files)
        assertFalse(DocumentValidator.validate("scan.jpg", DocumentCatalog.types.first(), 1).hasAny)
    }
}
