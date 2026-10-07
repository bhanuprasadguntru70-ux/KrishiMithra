package com.example.data.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.api.GeminiHelper
import com.example.data.local.AppDatabase
import com.example.data.model.FarmingTipEntity
import com.example.data.model.NotificationAlertEntity
import com.example.data.repository.MarketRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Locale

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)

        val dao = db.dao()
        val marketRepo = MarketRepository(dao)

        return try {
            // 1. Fetch updated market crop prices from network and save to local DB
            val syncPricesResult = marketRepo.fetchAndCacheMarketPrices()

            // 2. Evaluate price alerts
            val allCrops = dao.getAllMarketCrops().first()
            val cropsWithAlerts = allCrops.filter { it.priceAlertEnabled }

            for (crop in cropsWithAlerts) {
                if (crop.todayPrice >= crop.alertPrice) {
                    val alertTitle = "Price Alert: ${crop.cropName} 📈"
                    val alertMsg = "Your alert for ${crop.cropName} in ${crop.market} has been triggered. Current price reaches ₹${crop.todayPrice.toInt()} (Target: ₹${crop.alertPrice.toInt()})."

                    // Save local Notification Alert in Database
                    dao.insertNotification(
                        NotificationAlertEntity(
                            title = alertTitle,
                            message = alertMsg,
                            category = "Market"
                        )
                    )

                    // Show local system notification
                    showSystemNotification(alertTitle, alertMsg, crop.id)

                    // Disable the alert so it only triggers once
                    dao.updateMarketCrop(crop.copy(priceAlertEnabled = false))
                }
            }

            // 3. Sync Farming Tips (localized to the current user's location & season)
            val currentUser = dao.getCurrentUser()
            val district = currentUser?.district ?: "Eluru"
            val state = currentUser?.state ?: "Andhra Pradesh"

            val calendar = Calendar.getInstance()
            val month = calendar.get(Calendar.MONTH)
            val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH) ?: "Current Month"
            
            val season = when (month) {
                in 5..9 -> "Kharif (Monsoon) Season"
                in 10..11, in 0..1 -> "Rabi (Winter) Season"
                else -> "Zaid (Summer) Season"
            }

            val prompt = """
                Provide 1 high-quality, actionable, highly-specific daily farming tip for a farmer located in $district, $state, India.
                The current season is $season (Month: $monthName).
                Base the tip on realistic crop cycles (e.g. rice, cotton, sugarcane, maize, groundnut or seasonal vegetables) during this season.
                The tip must be concise (2-3 sentences), highly practical, and focus on soil preparation, irrigation, fertilizer timing, pest management, or harvesting.
                Format the response as:
                
                **[Crop/Focus]**: [The actual practical tip]
            """.trimIndent()

            val systemInstruction = """
                You are an expert Indian agricultural advisor and agronomist. 
                Provide precise, practical, and highly localized advice for the specified region and season. 
                Be encouraging, professional, and clear. Do not use generic filler words.
            """.trimIndent()

            val tipResponse = GeminiHelper.generateResponse(prompt, systemInstruction)
            if (!tipResponse.startsWith("Error:") && !tipResponse.startsWith("API Error:") && tipResponse.isNotBlank()) {
                // Save newly fetched tip to cache
                dao.insertFarmingTip(
                    FarmingTipEntity(
                        tipText = tipResponse,
                        district = district,
                        state = state
                    )
                )

                // Insert into local notifications
                val tipTitle = "Daily Farming Tip Ready 🌿"
                val tipMsg = "Your customized farming advice for $district is updated: ${tipResponse.take(80)}..."
                dao.insertNotification(
                    NotificationAlertEntity(
                        title = tipTitle,
                        message = "Your daily personalized farming recommendation for $season has arrived. Tap to view on your dashboard.",
                        category = "Reminder"
                    )
                )

                // Trigger System Notification
                showSystemNotification(tipTitle, tipMsg, 9999)
            }

            Result.success()
        } catch (e: Exception) {
            // Return retry on transient failure
            Result.retry()
        }
    }

    private fun showSystemNotification(title: String, message: String, notificationId: Int) {
        val channelId = "agri_alerts_channel"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Agri AI Alerts & Tips",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Farming tips and price alerts from Agri AI"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, builder.build())
    }
}
