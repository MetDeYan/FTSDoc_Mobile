package com.ftsdoc.mobile.data.repository

import kotlinx.coroutines.CancellationException

internal suspend fun <T> suspendResult(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }
}
