package com.ftsdoc.mobile.data.remote

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

/**
 * Авторизация: POST {адрес}/import/authorization/ , заголовок Basic.
 * Код 200 — вход выполнен.
 *
 * Отправка: POST multipart/form-data на {адрес}/import/docs/
 * - document_name — наименование документа
 * - document_type — вид документа (ТТН, ТН)
 * - files — JSON-массив { "ИмяФайла": "...", "Данные": "<base64>" }
 *
 * Статистика: GET {адрес}/import/statistics/
 * { "documents_count": 0, "pages_count": 0 }
 */
interface ApiService {
    @POST
    suspend fun authorize(@Url url: String): Response<ResponseBody>

    @Multipart
    @POST
    suspend fun uploadDocument(
        @Url url: String,
        @Part parts: List<MultipartBody.Part>,
    ): Response<ResponseBody>

    @GET
    suspend fun statistics(@Url url: String): Response<ResponseBody>
}
