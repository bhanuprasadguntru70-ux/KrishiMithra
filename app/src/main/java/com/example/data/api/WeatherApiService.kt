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

// --- GEOCODING MODELS ---
@JsonClass(generateAdapter = true)
data class GeocodingResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null, // State
    val admin2: String? = null  // District
)

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    val results: List<GeocodingResult>? = null
)

// --- AIR QUALITY MODELS ---
@JsonClass(generateAdapter = true)
data class AirQualityCurrent(
    @Json(name = "us_aqi") val usAqi: Double? = null
)

@JsonClass(generateAdapter = true)
data class AirQualityResponse(
    val current: AirQualityCurrent? = null
)

// --- WEATHER FORECAST MODELS ---
@JsonClass(generateAdapter = true)
data class CurrentWeather(
    @Json(name = "temperature_2m") val temperature2m: Double,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Double? = null,
    @Json(name = "apparent_temperature") val apparentTemperature: Double? = null,
    val precipitation: Double? = null,
    val rain: Double? = null,
    @Json(name = "weather_code") val weatherCode: Int,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double? = null,
    @Json(name = "wind_direction_10m") val windDirection10m: Double? = null,
    @Json(name = "uv_index") val uvIndex: Double? = null
)

@JsonClass(generateAdapter = true)
data class HourlyForecastData(
    val time: List<String>,
    @Json(name = "temperature_2m") val temperature2m: List<Double>,
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int>? = null,
    @Json(name = "weather_code") val weatherCode: List<Int>
)

@JsonClass(generateAdapter = true)
data class DailyForecastData(
    val time: List<String>,
    @Json(name = "weather_code") val weatherCode: List<Int>,
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double>,
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double>,
    val sunrise: List<String>,
    val sunset: List<String>,
    @Json(name = "uv_index_max") val uvIndexMax: List<Double>? = null,
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoForecastResponse(
    val current: CurrentWeather,
    val hourly: HourlyForecastData,
    val daily: DailyForecastData
)

// --- OPENWEATHER MODELS ---
@JsonClass(generateAdapter = true)
data class OpenWeatherCurrentResponse(
    val coord: OpenWeatherCoord? = null,
    val weather: List<OpenWeatherWeatherItem>? = null,
    val main: OpenWeatherMainData? = null,
    val visibility: Double? = null,
    val wind: OpenWeatherWindData? = null,
    val rain: OpenWeatherRainData? = null,
    val clouds: OpenWeatherCloudData? = null,
    val dt: Long? = null,
    val sys: OpenWeatherSysData? = null,
    val timezone: Long? = null,
    val name: String? = null,
    val cod: Int? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherCoord(
    val lon: Double? = null,
    val lat: Double? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherWeatherItem(
    val id: Int? = null,
    val main: String? = null,
    val description: String? = null,
    val icon: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherMainData(
    val temp: Double? = null,
    @Json(name = "feels_like") val feelsLike: Double? = null,
    @Json(name = "temp_min") val tempMin: Double? = null,
    @Json(name = "temp_max") val tempMax: Double? = null,
    val pressure: Int? = null,
    val humidity: Int? = null,
    @Json(name = "sea_level") val seaLevel: Int? = null,
    @Json(name = "grnd_level") val grndLevel: Int? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherWindData(
    val speed: Double? = null,
    val deg: Int? = null,
    val gust: Double? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherRainData(
    @Json(name = "1h") val rain1h: Double? = null,
    @Json(name = "3h") val rain3h: Double? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherCloudData(
    val all: Int? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherSysData(
    val type: Int? = null,
    val id: Long? = null,
    val country: String? = null,
    val sunrise: Long? = null,
    val sunset: Long? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherGeoItem(
    val name: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val country: String? = null,
    val state: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherForecastResponse(
    val cod: String? = null,
    val list: List<OpenWeatherForecastListItem>? = null,
    val city: OpenWeatherCityData? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherForecastListItem(
    val dt: Long? = null,
    val main: OpenWeatherMainData? = null,
    val weather: List<OpenWeatherWeatherItem>? = null,
    val wind: OpenWeatherWindData? = null,
    val rain: OpenWeatherRainData? = null,
    val pop: Double? = null,
    @Json(name = "dt_txt") val dtTxt: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherCityData(
    val id: Long? = null,
    val name: String? = null,
    val coord: OpenWeatherCoord? = null,
    val country: String? = null,
    val sunrise: Long? = null,
    val sunset: Long? = null
)

// --- RETROFIT SERVICE INTERFACES ---
interface OpenWeatherApiService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeatherByCoords(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en"
    ): OpenWeatherCurrentResponse

    @GET("data/2.5/weather")
    suspend fun getCurrentWeatherByQuery(
        @Query("q") query: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en"
    ): OpenWeatherCurrentResponse

    @GET("data/2.5/forecast")
    suspend fun getForecastByCoords(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en"
    ): OpenWeatherForecastResponse

    @GET("data/2.5/forecast")
    suspend fun getForecastByQuery(
        @Query("q") query: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en"
    ): OpenWeatherForecastResponse

    @GET("geo/1.0/reverse")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String
    ): List<OpenWeatherGeoItem>

    @GET("geo/1.0/direct")
    suspend fun directGeocode(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String
    ): List<OpenWeatherGeoItem>
}

interface GeocodingApiService {
    @GET("v1/search")
    suspend fun searchLocation(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}

interface WeatherForecastApiService {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,rain,weather_code,wind_speed_10m,wind_direction_10m,uv_index",
        @Query("hourly") hourly: String = "temperature_2m,precipitation_probability,weather_code",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_probability_max",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoForecastResponse
}

interface AirQualityApiService {
    @GET("v1/air-quality")
    suspend fun getAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "us_aqi",
        @Query("timezone") timezone: String = "auto"
    ): AirQualityResponse
}

// --- RETROFIT CLIENTS ---
object WeatherRetrofitClient {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val openWeatherService: OpenWeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenWeatherApiService::class.java)
    }

    val geocodingService: GeocodingApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeocodingApiService::class.java)
    }

    val forecastService: WeatherForecastApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(WeatherForecastApiService::class.java)
    }

    val airQualityService: AirQualityApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://air-quality-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AirQualityApiService::class.java)
    }
}
