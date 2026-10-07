package com.example.data.sync

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.work.*
import com.example.data.api.WeatherRetrofitClient
import com.example.data.local.AppDatabase
import com.example.data.model.NotificationAlertEntity
import com.example.util.WeatherNotificationHelper
import com.example.util.WeatherPrefsManager
import java.util.concurrent.TimeUnit

class WeatherSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "WeatherSyncWorker"
        private const val WORK_NAME = "krishimithra_weather_sync_work"

        fun schedulePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<WeatherSyncWorker>(
                3, TimeUnit.HOURS // Check weather forecast every 3 hours
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        val prefsManager = WeatherPrefsManager(applicationContext)
        val settings = prefsManager.getSettings()

        if (!settings.enabled) {
            Log.d(TAG, "Weather notifications disabled in settings. Skipping background sync.")
            return Result.success()
        }

        val lat = if (settings.locationMode == "GPS") prefsManager.getLastKnownLat() else settings.manualLat
        val lon = if (settings.locationMode == "GPS") prefsManager.getLastKnownLon() else settings.manualLon
        val locationName = if (settings.locationMode == "GPS") prefsManager.getLastKnownLocationName() else settings.manualLocality

        return try {
            val forecast = WeatherRetrofitClient.forecastService.getForecast(lat, lon)
            val current = forecast.current
            val maxRainChance = forecast.daily.precipitationProbabilityMax?.firstOrNull() ?: 0

            val temp = current.temperature2m
            val rainMm = current.rain ?: current.precipitation ?: 0.0
            val windSpeed = current.windSpeed10m ?: 0.0
            val humidity = current.relativeHumidity2m?.toInt() ?: 0
            val weatherCode = current.weatherCode
            val isTelugu = settings.preferredLanguage == "te"

            var alertTitle: String? = null
            var alertBody: String? = null
            var isSevere = false

            // Severe weather evaluation
            if (weatherCode in listOf(95, 96, 99)) { // Thunderstorm
                isSevere = true
                alertTitle = if (isTelugu) "⚡ తుఫాను హెచ్చరిక - $locationName" else "⚡ Thunderstorm Alert - $locationName"
                alertBody = if (isTelugu) {
                    "మీ ప్రాంతం ($locationName)లో ఉరుములు, మెరుపులతో కూడిన తుఫాను వచ్చే అవకాశం ఉంది. పొలాల్లో పనిచేసే రైతులు ఎత్తైన చెట్లు మరియు విద్యుత్ స్తంభాలకు దూరంగా సురక్షిత ప్రాంతంలో ఉండండి."
                } else {
                    "Thunderstorms expected in $locationName. Farmers are advised to seek safe shelter away from trees and tall structures."
                }
            } else if (rainMm > 15.0 || maxRainChance >= 80) { // Heavy Rain
                isSevere = true
                alertTitle = if (isTelugu) "🌧️ భారీ వర్ష హెచ్చరిక - $locationName" else "🌧️ Heavy Rain Alert - $locationName"
                alertBody = if (isTelugu) {
                    "$locationName పరిసర ప్రాంతాల్లో రాబోయే కొన్ని గంటల్లో భారీ వర్షం పడే అవకాశం ఉంది. కోసిన ధాన్యం, పంట ఉత్పత్తుల పై తార్పాలిన్లు కప్పి సురక్షిత ప్రదేశాలకు తరలించండి."
                } else {
                    "Heavy rain predicted in $locationName. Protect harvested crops and ensure proper field drainage."
                }
            } else if (maxRainChance in 50..79 || rainMm > 1.0) { // Moderate Rain Expected
                if (settings.rainAlertsEnabled) {
                    alertTitle = if (isTelugu) "🌧️ వర్ష సూచన - $locationName" else "🌧️ Rain Forecast - $locationName"
                    alertBody = if (isTelugu) {
                        "$locationName లో వర్షం పడే అవకాశం ఉంది ($maxRainChance%). రైతులు అవసరమైన ఎరువుల పిచికారీ మరియు నీటిపారుదల పనులను వాయిదా వేసుకోవడం మంచిది."
                    } else {
                        "Rain expected in $locationName ($maxRainChance% chance). Hold off on spraying fertilizers and pesticides for now."
                    }
                }
            } else if (windSpeed > 38.0) { // Strong Wind
                if (settings.severeAlertsEnabled) {
                    isSevere = true
                    alertTitle = if (isTelugu) "💨 ఈదురు గాలుల హెచ్చరిక - $locationName" else "💨 Strong Wind Warning - $locationName"
                    alertBody = if (isTelugu) {
                        "$locationName లో గంటకు ${windSpeed.toInt()} కి.మీ వేగంతో బలమైన ఈదురు గాలులు వీచే అవకాశం ఉంది. అరటి, చెరుకు తోటలకు మద్దతు కట్టెల ఏర్పాటు చేయండి."
                    } else {
                        "High winds of ${windSpeed.toInt()} km/h forecast for $locationName. Provide support to banana and sugarcane crops."
                    }
                }
            } else if (temp >= 40.0) { // Extreme Heat
                if (settings.severeAlertsEnabled) {
                    alertTitle = if (isTelugu) "☀️ తీవ్ర ఎండల ప్రభావం - $locationName" else "☀️ Heatwave Warning - $locationName"
                    alertBody = if (isTelugu) {
                        "$locationName లో ఉష్ణోగ్రత ${temp.toInt()}°C చేరింది. పొలాలకు తగినంత నీటిపారుదల అందించండి, పశువులను నీడ ఉన్న ప్రదేశాల్లో ఉంచండి."
                    } else {
                        "High temperature of ${temp.toInt()}°C in $locationName. Ensure adequate irrigation for crops and shade for livestock."
                    }
                }
            }

            // Post notification if an alert condition is satisfied
            if (alertTitle != null && alertBody != null) {
                WeatherNotificationHelper.showWeatherNotification(
                    context = applicationContext,
                    title = alertTitle,
                    body = alertBody,
                    isSevere = isSevere
                )

                // Save to Room DB Notification log
                val db = AppDatabase.getInstance(applicationContext)

                db.dao().insertNotification(
                    NotificationAlertEntity(
                        title = alertTitle,
                        message = alertBody,
                        category = "Weather Alert"
                    )
                )
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed background weather check: ${e.message}")
            Result.retry()
        }
    }
}
