package com.ftsdoc.mobile.ui.common

import com.ftsdoc.mobile.domain.model.UserMessageException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

fun Throwable.toUiText(): String {
    return when (this) {
        is UserMessageException -> message ?: "Ошибка"
        is UnknownHostException,
        is ConnectException,
        -> "Сервер недоступен. Проверьте адрес и сеть."
        is SocketTimeoutException -> "Превышено время ожидания сервера."
        is SSLException -> "Не удалось установить защищённое соединение с сервером."
        else -> "Не удалось выполнить запрос."
    }
}
