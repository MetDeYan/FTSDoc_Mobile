package com.ftsdoc.mobile.data.remote

import okhttp3.Credentials

class CredentialHolder {
    data class Snapshot(
        val username: String,
        val password: String,
    )

    private var username: String = ""
    private var password: String = ""

    @Synchronized
    fun snapshot(): Snapshot = Snapshot(username, password)

    @Synchronized
    fun update(username: String, password: String) {
        this.username = username
        this.password = password
    }

    @Synchronized
    fun restore(snapshot: Snapshot) {
        username = snapshot.username
        password = snapshot.password
    }

    @Synchronized
    fun clear() {
        username = ""
        password = ""
    }

    @Synchronized
    fun authorizationHeader(): String? {
        if (username.isEmpty()) return null
        return Credentials.basic(username, password, Charsets.UTF_8)
    }
}
