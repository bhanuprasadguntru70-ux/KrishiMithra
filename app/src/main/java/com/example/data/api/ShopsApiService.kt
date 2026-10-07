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
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

// ============================================================
// NOMINATIM
// ============================================================

@JsonClass(generateAdapter = true)
data class NominatimSearchResult(

    @Json(name = "place_id")
    val placeId: Long? = null,

    @Json(name = "lat")
    val latitude: String? = null,

    @Json(name = "lon")
    val longitude: String? = null,

    @Json(name = "display_name")
    val displayName: String? = null,

    @Json(name = "type")
    val type: String? = null
)


// ============================================================
// OVERPASS / OPENSTREETMAP
// ============================================================

@JsonClass(generateAdapter = true)
data class OverpassElement(

    @Json(name = "type")
    val type: String,

    @Json(name = "id")
    val id: Long,

    @Json(name = "lat")
    val latitude: Double? = null,

    @Json(name = "lon")
    val longitude: Double? = null,

    @Json(name = "tags")
    val tags: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class OverpassResponse(

    @Json(name = "version")
    val version: Double? = null,

    @Json(name = "elements")
    val elements: List<OverpassElement> = emptyList()
)


// ============================================================
// GOOGLE PLACES
// ============================================================

@JsonClass(generateAdapter = true)
data class PlaceLocation(

    @Json(name = "lat")
    val latitude: Double,

    @Json(name = "lng")
    val longitude: Double
)

@JsonClass(generateAdapter = true)
data class PlaceGeometry(

    @Json(name = "location")
    val location: PlaceLocation
)

@JsonClass(generateAdapter = true)
data class GooglePlaceItem(

    @Json(name = "place_id")
    val placeId: String,

    @Json(name = "name")
    val name: String,

    @Json(name = "vicinity")
    val vicinity: String? = null,

    @Json(name = "geometry")
    val geometry: PlaceGeometry,

    @Json(name = "rating")
    val rating: Double? = null,

    @Json(name = "types")
    val types: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GooglePlacesResponse(

    @Json(name = "results")
    val results: List<GooglePlaceItem> = emptyList(),

    @Json(name = "status")
    val status: String
)


// ============================================================
// API SERVICE
// ============================================================

interface ShopsApiService {

    // --------------------------------------------------------
    // OpenStreetMap Overpass
    // --------------------------------------------------------

    @GET
    suspend fun queryOverpass(
        @Url url: String
    ): OverpassResponse


    // --------------------------------------------------------
    // Nominatim Search
    // --------------------------------------------------------

    @GET("search")
    suspend fun searchNominatim(

        @Query("q")
        query: String,

        @Query("format")
        format: String = "json",

        @Query("addressdetails")
        addressDetails: Int = 1,

        @Query("limit")
        limit: Int = 5
    ): List<NominatimSearchResult>


    // --------------------------------------------------------
    // Nominatim Reverse Geocoding
    // --------------------------------------------------------

    @GET("reverse")
    suspend fun reverseGeocode(

        @Query("lat")
        latitude: Double,

        @Query("lon")
        longitude: Double,

        @Query("format")
        format: String = "json"
    ): NominatimSearchResult


    // --------------------------------------------------------
    // Google Places Nearby Search
    // --------------------------------------------------------

    @GET("maps/api/place/nearbysearch/json")
    suspend fun searchGooglePlacesNearby(
        @Query("location") location: String,
        @Query("radius") radiusMeters: Int,
        @Query("keyword") keyword: String,
        @Query("key") apiKey: String
    ): GooglePlacesResponse
}


// ============================================================
// API CLIENT
// ============================================================

object ApiClient {

    private const val OVERPASS_BASE_URL =
        "https://overpass-api.de/"

    private const val NOMINATIM_BASE_URL =
        "https://nominatim.openstreetmap.org/"

    private const val GOOGLE_BASE_URL =
        "https://maps.googleapis.com/"


    // --------------------------------------------------------
    // HTTP CLIENT
    // --------------------------------------------------------

    private val okHttpClient: OkHttpClient by lazy {

        OkHttpClient.Builder()

            .connectTimeout(
                15,
                TimeUnit.SECONDS
            )

            .readTimeout(
                20,
                TimeUnit.SECONDS
            )

            .writeTimeout(
                20,
                TimeUnit.SECONDS
            )

            .retryOnConnectionFailure(true)

            .addInterceptor { chain ->

                val request =
                    chain.request()
                        .newBuilder()
                        .header(
                            "User-Agent",
                            "KrishiMithraAgriApp/1.0"
                        )
                        .build()

                chain.proceed(request)
            }

            .build()
    }


    // --------------------------------------------------------
    // MOSHI
    // --------------------------------------------------------

    private val moshi: Moshi by lazy {

        Moshi.Builder()

            .addLast(
                KotlinJsonAdapterFactory()
            )

            .build()
    }


    // --------------------------------------------------------
    // RETROFIT FACTORY
    // --------------------------------------------------------

    private fun retrofit(
        baseUrl: String
    ): Retrofit {

        return Retrofit.Builder()

            .baseUrl(baseUrl)

            .client(okHttpClient)

            .addConverterFactory(
                MoshiConverterFactory.create(
                    moshi
                )
            )

            .build()
    }


    // --------------------------------------------------------
    // OVERPASS SERVICE
    // --------------------------------------------------------

    val overpass: ShopsApiService by lazy {

        retrofit(
            OVERPASS_BASE_URL
        ).create(
            ShopsApiService::class.java
        )
    }


    // --------------------------------------------------------
    // NOMINATIM SERVICE
    // --------------------------------------------------------

    val nominatim: ShopsApiService by lazy {

        retrofit(
            NOMINATIM_BASE_URL
        ).create(
            ShopsApiService::class.java
        )
    }


    // --------------------------------------------------------
    // GOOGLE PLACES SERVICE
    // --------------------------------------------------------

    val googlePlaces: ShopsApiService by lazy {

        retrofit(
            GOOGLE_BASE_URL
        ).create(
            ShopsApiService::class.java
        )
    }
}