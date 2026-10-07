package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.WeatherNotificationSettings

class WeatherPrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("krishimithra_weather_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_NOTIF_ENABLED = "key_notif_enabled"
        private const val KEY_DAILY_ENABLED = "key_daily_enabled"
        private const val KEY_RAIN_ALERTS_ENABLED = "key_rain_alerts_enabled"
        private const val KEY_SEVERE_ALERTS_ENABLED = "key_severe_alerts_enabled"
        private const val KEY_MORNING_TIME = "key_morning_time"
        private const val KEY_LOC_MODE = "key_loc_mode"
        private const val KEY_MANUAL_LOCALITY = "key_manual_locality"
        private const val KEY_MANUAL_DISTRICT = "key_manual_district"
        private const val KEY_MANUAL_STATE = "key_manual_state"
        private const val KEY_MANUAL_LAT = "key_manual_lat"
        private const val KEY_MANUAL_LON = "key_manual_lon"
        private const val KEY_ACTIVE_GEOHASH = "key_active_geohash"
        private const val KEY_ACTIVE_DISTRICT_TOPIC = "key_active_district_topic"
        private const val KEY_PREF_LANG = "key_pref_lang"
        private const val KEY_LAST_KNOWN_LAT = "key_last_known_lat"
        private const val KEY_LAST_KNOWN_LON = "key_last_known_lon"
        private const val KEY_LAST_KNOWN_LOCATION_NAME = "key_last_known_loc_name"
    }

    fun getSettings(): WeatherNotificationSettings {
        return WeatherNotificationSettings(
            enabled = prefs.getBoolean(KEY_NOTIF_ENABLED, true),
            dailyUpdateEnabled = prefs.getBoolean(KEY_DAILY_ENABLED, true),
            rainAlertsEnabled = prefs.getBoolean(KEY_RAIN_ALERTS_ENABLED, true),
            severeAlertsEnabled = prefs.getBoolean(KEY_SEVERE_ALERTS_ENABLED, true),
            morningNotificationTime = prefs.getString(KEY_MORNING_TIME, "06:00") ?: "06:00",
            locationMode = prefs.getString(KEY_LOC_MODE, "GPS") ?: "GPS",
            manualLocality = prefs.getString(KEY_MANUAL_LOCALITY, "Anantapur") ?: "Anantapur",
            manualDistrict = prefs.getString(KEY_MANUAL_DISTRICT, "Anantapur") ?: "Anantapur",
            manualState = prefs.getString(KEY_MANUAL_STATE, "Andhra Pradesh") ?: "Andhra Pradesh",
            manualLat = prefs.getFloat(KEY_MANUAL_LAT, 14.6819f).toDouble(),
            manualLon = prefs.getFloat(KEY_MANUAL_LON, 77.6006f).toDouble(),
            activeGeohash = prefs.getString(KEY_ACTIVE_GEOHASH, "tf3k") ?: "tf3k",
            activeDistrictTopic = prefs.getString(KEY_ACTIVE_DISTRICT_TOPIC, "weather_district_anantapur") ?: "weather_district_anantapur",
            preferredLanguage = prefs.getString(KEY_PREF_LANG, "te") ?: "te"
        )
    }

    fun saveSettings(settings: WeatherNotificationSettings) {
        prefs.edit()
            .putBoolean(KEY_NOTIF_ENABLED, settings.enabled)
            .putBoolean(KEY_DAILY_ENABLED, settings.dailyUpdateEnabled)
            .putBoolean(KEY_RAIN_ALERTS_ENABLED, settings.rainAlertsEnabled)
            .putBoolean(KEY_SEVERE_ALERTS_ENABLED, settings.severeAlertsEnabled)
            .putString(KEY_MORNING_TIME, settings.morningNotificationTime)
            .putString(KEY_LOC_MODE, settings.locationMode)
            .putString(KEY_MANUAL_LOCALITY, settings.manualLocality)
            .putString(KEY_MANUAL_DISTRICT, settings.manualDistrict)
            .putString(KEY_MANUAL_STATE, settings.manualState)
            .putFloat(KEY_MANUAL_LAT, settings.manualLat.toFloat())
            .putFloat(KEY_MANUAL_LON, settings.manualLon.toFloat())
            .putString(KEY_ACTIVE_GEOHASH, settings.activeGeohash)
            .putString(KEY_ACTIVE_DISTRICT_TOPIC, settings.activeDistrictTopic)
            .putString(KEY_PREF_LANG, settings.preferredLanguage)
            .apply()
    }

    fun saveLastKnownLocation(lat: Double, lon: Double, locationName: String) {
        prefs.edit()
            .putFloat(KEY_LAST_KNOWN_LAT, lat.toFloat())
            .putFloat(KEY_LAST_KNOWN_LON, lon.toFloat())
            .putString(KEY_LAST_KNOWN_LOCATION_NAME, locationName)
            .apply()
    }

    fun getLastKnownLat(): Double = prefs.getFloat(KEY_LAST_KNOWN_LAT, 14.6819f).toDouble()
    fun getLastKnownLon(): Double = prefs.getFloat(KEY_LAST_KNOWN_LON, 77.6006f).toDouble()
    fun getLastKnownLocationName(): String = prefs.getString(KEY_LAST_KNOWN_LOCATION_NAME, "Anantapur") ?: "Anantapur"
}
