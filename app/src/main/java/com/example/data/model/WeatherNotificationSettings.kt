package com.example.data.model

data class WeatherNotificationSettings(
    val enabled: Boolean = true,
    val dailyUpdateEnabled: Boolean = true,
    val rainAlertsEnabled: Boolean = true,
    val severeAlertsEnabled: Boolean = true,
    val morningNotificationTime: String = "06:00", // 6:00 AM default
    val locationMode: String = "GPS", // "GPS" or "MANUAL"
    val manualLocality: String = "Anantapur",
    val manualDistrict: String = "Anantapur",
    val manualState: String = "Andhra Pradesh",
    val manualLat: Double = 14.6819,
    val manualLon: Double = 77.6006,
    val activeGeohash: String = "tf3k",
    val activeDistrictTopic: String = "weather_district_anantapur",
    val preferredLanguage: String = "te" // "te" (Telugu) or "en" (English)
)
