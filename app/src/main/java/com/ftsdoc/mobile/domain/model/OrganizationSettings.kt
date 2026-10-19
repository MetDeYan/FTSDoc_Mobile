package com.ftsdoc.mobile.domain.model

const val DEFAULT_SERVER_URL = "https://localhost:443/dev010AI/hs"

data class OrganizationSettings(
    val name: String = "",
    val organizationId: String = "",
    val serverUrl: String = DEFAULT_SERVER_URL,
    val operatorName: String = "",
)
