package com.example.data.api

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
data class MarketCropDto(
    val state: String? = "Andhra Pradesh",
    val district: String? = "Chittoor",
    val market: String? = "Madanapalle",
    val commodity: String? = null,
    val variety: String? = null,
    val arrival_date: String? = null,
    val min_price: String? = null,
    val max_price: String? = null,
    val modal_price: String? = null,
    // Fallback or secondary fields
    val cropName: String? = null,
    val todayPrice: Double? = null,
    val yesterdayPrice: Double? = null,
    val highestPrice: Double? = null,
    val lowestPrice: Double? = null,
    val cropNameTe: String? = null,
    val category: String? = null,
    val imageUrl: String? = null,
    val lastUpdated: String? = null
) {
    val resolvedCropName: String
        get() = commodity ?: cropName ?: "Crop"

    val resolvedModalPrice: Double
        get() = modal_price?.toDoubleOrNull() ?: todayPrice ?: 1500.0

    val resolvedMinPrice: Double
        get() = min_price?.toDoubleOrNull() ?: lowestPrice ?: (resolvedModalPrice * 0.85)

    val resolvedMaxPrice: Double
        get() = max_price?.toDoubleOrNull() ?: highestPrice ?: (resolvedModalPrice * 1.15)

    val resolvedMarket: String
        get() = market ?: "Local Market"

    val resolvedDistrict: String
        get() = district ?: "District"

    val resolvedState: String
        get() = state ?: "Andhra Pradesh"

    val resolvedArrivalDate: String
        get() = arrival_date ?: "Today"
}

@JsonClass(generateAdapter = true)
data class MarketPricesResponse(
    val records: List<MarketCropDto>
)

interface MarketApiService {
    @GET("resource/3596b026-6547-4ef8-8255-da7361a998d5")
    suspend fun getMarketPrices(
        @Query("api-key") apiKey: String?,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 100
    ): MarketPricesResponse
}

object MarketRetrofitClient {
    private const val BASE_URL = "https://api.data.gov.in/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: MarketApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MarketApiService::class.java)
    }
}
