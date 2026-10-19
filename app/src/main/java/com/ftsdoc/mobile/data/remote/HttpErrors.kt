package com.ftsdoc.mobile.data.remote

import com.ftsdoc.mobile.domain.model.UserMessageException

fun httpError(
    code: Int,
    body: String?,
    unauthorized: String = "Неверный логин или пароль",
): UserMessageException {
    if (code == 401 || code == 403) {
        return UserMessageException(unauthorized)
    }
    val text = body?.trim().orEmpty()
    val readable = text.isNotEmpty() && text.length < 300 && !text.startsWith("<")
    val message = if (readable) text else "Сервер вернул ошибку $code"
    return UserMessageException(message)
}
