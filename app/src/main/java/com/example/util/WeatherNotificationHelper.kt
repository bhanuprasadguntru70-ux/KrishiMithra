package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.google.firebase.messaging.FirebaseMessaging

object WeatherNotificationHelper {

    private const val TAG = "KrishiMithraWeatherNotif"
    const val CHANNEL_SEVERE_WEATHER = "krishimithra_severe_weather_channel"
    const val CHANNEL_DAILY_WEATHER = "krishimithra_daily_weather_channel"

    /**
     * Initializes Android Notification Channels (API 26+)
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Severe weather channel (High priority / Heads up)
            val severeChannel = NotificationChannel(
                CHANNEL_SEVERE_WEATHER,
                "వాతావరణ అత్యవసర హెచ్చరికలు (Severe Weather Alerts)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "వర్షాలు, తుఫానులు, తీవ్రమైన వాతావరణ సమాచారం"
                enableVibration(true)
                enableLights(true)
            }

            // Daily weather update channel (Default priority)
            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY_WEATHER,
                "దినసరి వాతావరణ సమాచారం (Daily Weather Updates)",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "రోజువారీ ఉదయం వాతావరణ అంచనా"
            }

            notificationManager.createNotificationChannel(severeChannel)
            notificationManager.createNotificationChannel(dailyChannel)
        }
    }

    /**
     * Updates Firebase Cloud Messaging (FCM) topic subscriptions based on farmer's location.
     * Prevents subscribing to every GPS point by subscribing to Geohash & District topics.
     */
    fun syncFcmTopics(context: Context, lat: Double, lon: Double, district: String) {
        val prefsManager = WeatherPrefsManager(context)
        val settings = prefsManager.getSettings()

        if (!settings.enabled) {
            Log.d(TAG, "Notifications disabled. Unsubscribing FCM topics...")
            unsubscribeAllTopics(settings.activeGeohash, settings.activeDistrictTopic)
            return
        }

        val newGeohashTopic = LocationUtils.getGeohashFcmTopic(lat, lon)
        val newDistrictTopic = LocationUtils.getDistrictFcmTopic(district)

        try {
            // Unsubscribe from old topics if changed
            if (settings.activeGeohash.isNotBlank() && settings.activeGeohash != newGeohashTopic) {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(settings.activeGeohash)
            }
            if (settings.activeDistrictTopic.isNotBlank() && settings.activeDistrictTopic != newDistrictTopic) {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(settings.activeDistrictTopic)
            }

            // Subscribe to new location-specific FCM topics
            FirebaseMessaging.getInstance().subscribeToTopic(newGeohashTopic)
            FirebaseMessaging.getInstance().subscribeToTopic(newDistrictTopic)

            // Save updated active topics
            prefsManager.saveSettings(
                settings.copy(
                    activeGeohash = newGeohashTopic,
                    activeDistrictTopic = newDistrictTopic
                )
            )

            Log.d(TAG, "Subscribed to FCM Topics: $newGeohashTopic, $newDistrictTopic")
        } catch (e: Throwable) {
            Log.e(TAG, "Error subscribing to FCM topics: ${e.message}")
        }
    }

    private fun unsubscribeAllTopics(geohashTopic: String, districtTopic: String) {
        try {
            if (geohashTopic.isNotBlank()) {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(geohashTopic)
            }
            if (districtTopic.isNotBlank()) {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(districtTopic)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to unsubscribe FCM topics: ${e.message}")
        }
    }

    /**
     * Shows a local status notification on the farmer's device.
     */
    fun showWeatherNotification(
        context: Context,
        title: String,
        body: String,
        isSevere: Boolean = false,
        notificationId: Int = (System.currentTimeMillis() % 10000).toInt()
    ) {
        createNotificationChannels(context)

        val channelId = if (isSevere) CHANNEL_SEVERE_WEATHER else CHANNEL_DAILY_WEATHER
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("screen", "weather")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(if (isSevere) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, builder.build())
    }
}
