package com.ftsdoc.mobile.data.remote

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Авторизация: POST {адрес}/import/authorization/ , заголовок Basic.
 * Код 200 — вход выполнен.
 *
 * Отправка: POST application/json на {адрес}/import/docs/
 * Заголовок MobileApplication: true
 * {
 *   "document_type": "ТН",
 *   "document_name": "...",
 *   "files": [{ "ИмяФайла": "...", "Данные": "<base64>", "КоличествоСтраниц": 1 }],
 *   "organization_id": "...",
 *   "user_id": "..."
 * }
 *
 * Статистика: GET {адрес}/import/statistics/
 * { "documents_count": 0, "pages_count": 0 }
 */
interface ApiService {
    @POST
    suspend fun authorize(@Url url: String): Response<ResponseBody>

    @Headers("MobileApplication: true")
    @POST
    suspend fun uploadDocument(
        @Url url: String,
        @Body body: RequestBody,
    ): Response<ResponseBody>

    @GET
    suspend fun statistics(@Url url: String): Response<ResponseBody>
}
