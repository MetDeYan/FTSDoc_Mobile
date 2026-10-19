package com.ftsdoc.mobile.domain.validation

import com.ftsdoc.mobile.domain.ServerEndpoints
import com.ftsdoc.mobile.domain.model.DocumentType
import com.ftsdoc.mobile.domain.model.OrganizationSettings

data class LoginErrors(
    val username: Boolean,
    val password: Boolean,
) {
    val hasAny: Boolean get() = username || password
}

data class SettingsErrors(
    val name: Boolean,
    val organizationId: Boolean,
    val serverUrl: Boolean,
) {
    val hasAny: Boolean get() = name || organizationId || serverUrl
}

data class DocumentErrors(
    val name: Boolean,
    val type: Boolean,
    val files: Boolean,
) {
    val hasAny: Boolean get() = name || type || files
}

object LoginValidator {
    fun validate(username: String, password: String): LoginErrors {
        return LoginErrors(
            username = username.trim().isEmpty(),
            password = password.isBlank(),
        )
    }
}

object SettingsValidator {
    fun validate(settings: OrganizationSettings): SettingsErrors {
        return SettingsErrors(
            name = settings.name.isBlank(),
            organizationId = settings.organizationId.isBlank(),
            serverUrl = !ServerEndpoints.isValidBase(settings.serverUrl),
        )
    }
}

object DocumentValidator {
    fun validate(name: String, type: DocumentType?, fileCount: Int): DocumentErrors {
        return DocumentErrors(
            name = name.isBlank(),
            type = type == null,
            files = fileCount <= 0,
        )
    }
}
