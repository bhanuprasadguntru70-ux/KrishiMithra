package com.example

import android.app.Application
import android.util.Log
import com.example.util.WeatherNotificationHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class KrishiMithraApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Safe Firebase App Initialization Fallback
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:100000000000:android:1234567890abcdef")
                    .setProjectId("krishimithra-app")
                    .setApiKey("AIzaSyDummyKeyForKrishiMithraStartup123")
                    .setGcmSenderId("100000000000")
                    .build()
                FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Throwable) {
            Log.e("KrishiMithraApp", "FirebaseApp safe init fallback: ${e.message}")
        }

        // 2. Notification Channels Setup
        try {
            WeatherNotificationHelper.createNotificationChannels(this)
        } catch (e: Throwable) {
            Log.e("KrishiMithraApp", "Notification channels setup error: ${e.message}")
        }
    }
}
