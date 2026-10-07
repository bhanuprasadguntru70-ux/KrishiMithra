package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val phone: String,
    val district: String,
    val state: String,
    val language: String, // "English", "Telugu", "Hindi"
    val isLoggedIn: Boolean = false,
    val isPremium: Boolean = false,
    val subscriptionPlan: String = "Free",
    val subscriptionExpiry: Long = 0L,
    val photoUri: String? = null,
    val landHolding: String? = null,
    val primaryCrops: String? = null
)

@Entity(tableName = "farms")
data class FarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val crop: String,
    val area: String,
    val soil: String,
    val plantingDate: String,
    val expectedHarvest: String,
    val irrigationReminder: Boolean = true,
    val fertilizerReminder: Boolean = true
)

@Entity(tableName = "disease_reports")
data class DiseaseReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cropName: String,
    val diseaseName: String,
    val confidenceScore: Double,
    val cause: String,
    val symptoms: String,
    val treatment: String,
    val organicSolution: String,
    val chemicalSolution: String,
    val preventiveMeasures: String,
    val nearbyOffice: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null
)

@Entity(tableName = "market_crops")
data class MarketCropEntity(
    val cropName: String,
    val market: String,
    val district: String,
    val todayPrice: Double,
    val yesterdayPrice: Double,
    val highestPrice: Double,
    val lowestPrice: Double,
    val isFavorite: Boolean = false,
    val priceAlertEnabled: Boolean = false,
    val alertPrice: Double = 0.0,
    val cropNameTe: String = "",
    val category: String = "Food Grains",
    val state: String = "Andhra Pradesh",
    val imageUrl: String = "",
    val lastUpdated: String = "Today, 09:00 AM",
    val arrivalQuantity: Double = 120.0,
    val unit: String = "Quintal",
    val marketDate: String = "Today",
    val source: String = "e-NAM / AGMARKNET",
    @PrimaryKey(autoGenerate = true) val id: Int = 0
) {
    companion object {
        fun determineCategory(cropName: String): String {
            val name = cropName.lowercase()
            return when {
                // Vegetables
                name.contains("tomato") || name.contains("onion") || name.contains("brinjal") ||
                name.contains("okra") || name.contains("ladies finger") || name.contains("chilli") ||
                name.contains("potato") || name.contains("cabbage") || name.contains("cauliflower") ||
                name.contains("carrot") || name.contains("beetroot") || name.contains("radish") ||
                name.contains("cucumber") || name.contains("capsicum") || name.contains("pumpkin") ||
                name.contains("gourd") || name.contains("drumstick") || name.contains("yam") ||
                name.contains("colocasia") || name.contains("spinach") || name.contains("mint") ||
                name.contains("beans") || name.contains("peas") && !name.contains("pigeon") && !name.contains("chickpea") -> "Vegetables"

                // Fruits
                name.contains("mango") || name.contains("banana") || name.contains("papaya") ||
                name.contains("orange") || name.contains("mosambi") || name.contains("apple") ||
                name.contains("guava") || name.contains("pomegranate") || name.contains("grape") ||
                name.contains("watermelon") || name.contains("melon") || name.contains("pineapple") ||
                name.contains("sapota") || name.contains("jackfruit") || name.contains("lemon") ||
                name.contains("lime") || name.contains("custard apple") || name.contains("coconut") -> "Fruits"

                // Food Grains
                name.contains("paddy") || name.contains("rice") || name.contains("wheat") ||
                name.contains("maize") || name.contains("corn") || name.contains("sorghum") ||
                name.contains("jowar") || name.contains("bajra") || name.contains("ragi") ||
                name.contains("millets") || name.contains("barley") || name.contains("oats") ||
                name.contains("grain") || name.contains("cereal") -> "Food Grains"

                // Flowers
                name.contains("jasmine") || name.contains("rose") || name.contains("marigold") ||
                name.contains("chrysanthemum") || name.contains("lily") || name.contains("lotus") ||
                name.contains("hibiscus") || name.contains("tulip") || name.contains("flower") ||
                name.contains("carnation") || name.contains("gerbera") -> "Flowers"

                // Spices
                name.contains("turmeric") || name.contains("ginger") || name.contains("garlic") ||
                name.contains("cardamom") || name.contains("pepper") || name.contains("clove") ||
                name.contains("cinnamon") || name.contains("cumin") || name.contains("coriander") ||
                name.contains("fennel") || name.contains("fenugreek") || name.contains("mustard") ||
                name.contains("tamarind") || name.contains("nutmeg") || name.contains("spice") ||
                name.contains("chilli") && (name.contains("dry") || name.contains("powder") || name.contains("guntur")) -> "Spices"

                // Oil Seeds
                name.contains("groundnut") || name.contains("peanut") || name.contains("sesame") ||
                name.contains("till") || name.contains("sunflower seed") || name.contains("mustard seed") ||
                name.contains("soybean") || name.contains("castor") || name.contains("safflower") ||
                name.contains("linseed") || name.contains("oilseed") || name.contains("copra") -> "Oil Seeds"

                // Cotton
                name.contains("cotton") || name.contains("kapas") || name.contains("lint") ||
                name.contains("jute") || name.contains("fiber") || name.contains("fibre") -> "Cotton"

                // Pulses
                name.contains("gram") || name.contains("chana") || name.contains("urad") ||
                name.contains("moong") || name.contains("toor") || name.contains("arhar") ||
                name.contains("pulse") || name.contains("lentil") || name.contains("pea") ||
                name.contains("cowpea") || name.contains("rajma") || name.contains("bean") ||
                name.contains("dhal") || name.contains("dal") || name.contains("pigeon pea") -> "Pulses"

                else -> "Food Grains" // Default fallback category
            }
        }
    }
}

@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val titleTe: String = "",
    val content: String,
    val category: String, // "AP Agriculture", "Telangana Agriculture", "India Agriculture", "Organic Farming", etc.
    val videoUrl: String? = null,
    val imageUrl: String? = null,
    val source: String = "AgriDept",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val category: String, // "Rain", "Disease", "Market", "Reminder"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val orderId: String,
    val amount: Double,
    val planName: String,
    val paymentMethod: String, // "UPI", "Card", "Net Banking", "Wallet"
    val status: String, // "SUCCESS", "FAILED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "farming_tips")
data class FarmingTipEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tipText: String,
    val district: String,
    val state: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val locationKey: String,
    val locationName: String,
    val weatherJson: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "crop_calendars")
data class CropCalendarEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val farmName: String,
    val cropName: String,
    val cropNameTe: String = "",
    val variety: String = "",
    val farmArea: String = "1 Acre",
    val soilType: String = "Black Soil",
    val plantingDate: String,
    val expectedHarvestDate: String,
    val totalDurationDays: Int = 120,
    val notes: String = "",
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "crop_stages")
data class CropStageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val calendarId: Int,
    val stageName: String,
    val stageNameTe: String = "",
    val startDay: Int,
    val endDay: Int,
    val startDate: String,
    val endDate: String,
    val status: String = "Upcoming",
    val description: String,
    val descriptionTe: String = "",
    val iconType: String = "Sowing"
)

@Entity(tableName = "crop_task_reminders")
data class CropTaskReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val calendarId: Int,
    val taskTitle: String,
    val taskTitleTe: String = "",
    val category: String,
    val categoryTe: String = "",
    val dueDate: String,
    val dayOffset: Int,
    val isCompleted: Boolean = false,
    val isReminderEnabled: Boolean = true,
    val instructions: String = "",
    val instructionsTe: String = "",
    val priority: String = "High",
    val reminderTime: String = "08:00 AM"
)

@Entity(tableName = "women_farmer_stories")
data class WomanFarmerStoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val village: String,
    val cropOrWork: String,
    val category: String, // "మహిళా రైతు", "వ్యవసాయ కార్మికురాలు", "పశుపోషణ", "కూరగాయల సాగు", "పాడి పరిశ్రమ", "విత్తన/పంట పనులు", "స్వయం ఉపాధి"
    val shortStoryTe: String,
    val shortStoryEn: String = "",
    val fullStoryTe: String,
    val fullStoryEn: String = "",
    val photoUrl: String = "",
    val likeCount: Int = 10,
    val isFeaturedToday: Boolean = false,
    val isApproved: Boolean = true, // Default pre-populated = true, farmer submitted = false (Admin approval required)
    val hasConsent: Boolean = true, // Explicit privacy permission given
    val submittedBy: String = "Admin", // "Admin" or "Farmer"
    val submittedPhone: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_calculations")
data class SavedCalculationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val calculatorType: String,
    val resultFormatted: String,
    val summaryDetails: String,
    val inputsJson: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "moneybook_expenses")
data class ExpenseEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val date: String,
    val category: String, // Seeds, Fertilizer, Pesticides, Labour, Tractor, Diesel, Electricity, Irrigation, Transport, Machinery, Rent, Other
    val description: String,
    val amount: Double,
    val crop: String = "",
    val fieldId: Int? = null,
    val fieldName: String = "",
    val receiptNote: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "moneybook_income")
data class IncomeEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val cropSold: String,
    val quantity: Double,
    val unit: String = "Bags", // Bags, Quintal, Tonne, Kg, Box
    val pricePerUnit: Double,
    val totalAmount: Double,
    val buyerName: String = "",
    val marketName: String = "",
    val date: String,
    val fieldId: Int? = null,
    val fieldName: String = "",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "farmer_fields")
data class FarmerFieldEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val fieldName: String,
    val area: Double,
    val unit: String = "Acre", // Acre, Guntha, Cent, Hectare
    val cropName: String = "",
    val season: String = "Kharif",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)


