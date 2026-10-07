package com.example.service

import android.util.Log
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.model.NotificationAlertEntity
import com.example.util.WeatherNotificationHelper
import com.example.util.WeatherPrefsManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KrishiMithraFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "KrishiMithraFCM"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM Token: $token")
        // Sync FCM topics with current saved location
        val prefsManager = WeatherPrefsManager(applicationContext)
        val lat = prefsManager.getLastKnownLat()
        val lon = prefsManager.getLastKnownLon()
        val district = prefsManager.getSettings().manualDistrict
        WeatherNotificationHelper.syncFcmTopics(applicationContext, lat, lon, district)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        val prefsManager = WeatherPrefsManager(applicationContext)
        val settings = prefsManager.getSettings()

        if (!settings.enabled) {
            Log.d(TAG, "Weather notifications disabled by user. Ignoring payload.")
            return
        }

        val data = remoteMessage.data
        val alertType = data["alertType"] ?: "rain_expected"
        val isSevere = data["isSevere"]?.toBoolean() ?: false

        // Check user preferences against alert category
        if (alertType.contains("rain") && !settings.rainAlertsEnabled) {
            return
        }
        if (isSevere && !settings.severeAlertsEnabled) {
            return
        }
        if (alertType == "daily_summary" && !settings.dailyUpdateEnabled) {
            return
        }

        val isTelugu = settings.preferredLanguage == "te"

        // Extract localized title and body
        val title = if (isTelugu) {
            data["titleTe"] ?: remoteMessage.notification?.title ?: "🌧️ KrishiMithra వాతావరణ సమాచారం"
        } else {
            data["titleEn"] ?: remoteMessage.notification?.title ?: "🌧️ KrishiMithra Weather Alert"
        }

        val body = if (isTelugu) {
            data["bodyTe"] ?: remoteMessage.notification?.body ?: "మీ ప్రాంతంలో రాబోయే కొన్ని గంటల్లో వాతావరణ మార్పు ఉండే అవకాశం ఉంది."
        } else {
            data["bodyEn"] ?: remoteMessage.notification?.body ?: "Weather updates expected in your region shortly."
        }

        // 1. Post local system notification
        WeatherNotificationHelper.showWeatherNotification(
            context = applicationContext,
            title = title,
            body = body,
            isSevere = isSevere
        )

        // 2. Save into Room Database Notification Alerts history
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)

                db.dao().insertNotification(
                    NotificationAlertEntity(
                        title = title,
                        message = body,
                        category = "Weather Alert"
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error persisting notification into DB: ${e.message}")
            }
        }
    }
}
