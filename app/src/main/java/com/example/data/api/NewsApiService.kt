package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class NewsDto(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "title") val title: String,
    @Json(name = "titleTe") val titleTe: String = "",
    @Json(name = "content") val content: String,
    @Json(name = "category") val category: String = "AP Agriculture News",
    @Json(name = "videoUrl") val videoUrl: String? = null,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "source") val source: String = "AgriDept",
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class NewsApiResponse(
    @Json(name = "status") val status: String = "ok",
    @Json(name = "totalResults") val totalResults: Int = 0,
    @Json(name = "articles") val articles: List<NewsDto> = emptyList()
)

interface NewsApiService {
    @GET("resource/agri-daily-news")
    suspend fun getDailyAgriNews(
        @Query("api-key") apiKey: String? = null,
        @Query("category") category: String = "agriculture",
        @Query("limit") limit: Int = 20
    ): NewsApiResponse
}

object NewsRetrofitClient {
    private const val BASE_URL = "https://api.data.gov.in/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(NewsApiService::class.java)
    }
}
