package com.ftsdoc.mobile.domain

import java.net.URI
import java.util.Locale

object ServerEndpoints {
    fun isValidBase(serverUrl: String): Boolean {
        val value = serverUrl.trim()
        if (value.isEmpty()) return false
        return try {
            val uri = URI(value)
            val scheme = uri.scheme?.lowercase(Locale.US)
            !uri.host.isNullOrBlank() && (scheme == "http" || scheme == "https")
        } catch (_: Exception) {
            false
        }
    }

    fun authorization(serverUrl: String): String = join(serverUrl, "import/authorization")

    fun documents(serverUrl: String): String = join(serverUrl, "import/docs")

    fun statistics(serverUrl: String): String = join(serverUrl, "import/statistics")

    fun join(serverUrl: String, path: String): String {
        val base = serverUrl.trim().trimEnd('/')
        val clean = path.trim('/')
        if (base.endsWith("/$clean")) return "$base/"
        return "$base/$clean/"
    }
}
