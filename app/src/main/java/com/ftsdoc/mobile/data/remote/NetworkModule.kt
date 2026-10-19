package com.ftsdoc.mobile.data.remote

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object NetworkModule {
    val gson: Gson = Gson()

    fun create(context: Context, credentials: CredentialHolder): ApiService {
        val debug = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val header = credentials.authorizationHeader()
                val request = if (header == null) {
                    chain.request()
                } else {
                    chain.request().newBuilder()
                        .header("Authorization", header)
                        .build()
                }
                chain.proceed(request)
            }

        if (debug) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            }
            clientBuilder.addInterceptor(logging)
            clientBuilder.trustDebugCertificates()
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(PLACEHOLDER_BASE_URL)
            .client(clientBuilder.build())
            .build()
        return retrofit.create(ApiService::class.java)
    }

    @SuppressLint("CustomX509TrustManager", "TrustAllX509TrustManager")
    private fun OkHttpClient.Builder.trustDebugCertificates() {
        val trustAll = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit

            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit

            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
        }
        val context = SSLContext.getInstance("TLS")
        context.init(null, arrayOf<TrustManager>(trustAll), SecureRandom())
        sslSocketFactory(context.socketFactory, trustAll)
        hostnameVerifier { _, _ -> true }
    }

    private const val PLACEHOLDER_BASE_URL = "https://localhost/"
}
