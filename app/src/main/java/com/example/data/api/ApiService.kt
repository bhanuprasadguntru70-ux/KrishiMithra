package com.example.data.api

import com.example.data.model.GeminiSearchRequest
import com.example.data.model.GeminiSearchResponse
import com.example.data.model.MandiApiResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface AgmarknetProxyApi {
    @GET("resource/9efc1e83-5cba-4274-910e-9411d31cf407")
    suspend fun getAgmarknetRatesDirect(
        @Query("api-key") apiKey: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 50,
        @Query("filters[state]") state: String? = null,
        @Query("filters[district]") district: String? = null,
        @Query("filters[commodity]") commodity: String? = null
    ): MandiApiResponse

    @GET("getLiveMandiRates")
    suspend fun getLiveRatesFromCloudProxy(
        @Query("state") state: String? = null,
        @Query("district") district: String? = null,
        @Query("commodity") commodity: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("forceRefresh") forceRefresh: Boolean = false
    ): MandiApiResponse
}

interface GeminiSearchApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContentWithSearch(
        @Query("key") apiKey: String,
        @Body request: GeminiSearchRequest
    ): GeminiSearchResponse
}

object NetworkClient {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val agmarknetDirectApi: AgmarknetProxyApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.data.gov.in/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AgmarknetProxyApi::class.java)
    }

    val geminiApi: GeminiSearchApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiSearchApi::class.java)
    }
}
