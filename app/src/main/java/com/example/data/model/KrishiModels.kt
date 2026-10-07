package com.example.data.model

data class MandiCropRate(
    val id: String,
    val commodity: String,
    val variety: String = "Common",
    val state: String,
    val district: String,
    val market: String,
    val modalPrice: Double,
    val minPrice: Double,
    val maxPrice: Double,
    val unit: String = "/qtl",
    val updateTime: String = "10:30 AM",
    val arrivalQty: String = "450 qtl",
    val marketDate: String = "Today",
    val source: String = "e-NAM / AGMARKNET",
    val imageResId: Int? = null,
    val isVerified: Boolean = true
)

data class WeatherInfo(
    val location: String = "Eluru, Andhra Pradesh",
    val tempC: Int = 31,
    val condition: String = "Partly Cloudy",
    val humidity: Int = 68,
    val rainfallChance: Int = 20,
    val windSpeedKm: Int = 12,
    val advisory: String = "Ideal weather for field irrigation and spraying in early morning.",
    val lastUpdated: String = "Just now"
)

enum class ToolCategory {
    ALL, CROP, FINANCIAL, LABOUR, TRACTOR, SALE, IRRIGATION
}

data class FarmToolItem(
    val id: String,
    val name: String,
    val category: ToolCategory,
    val iconName: String,
    val description: String
)

data class MoneyBookEntry(
    val id: String,
    val title: String,
    val amount: Double,
    val isIncome: Boolean,
    val category: String,
    val date: String,
    val notes: String = "",
    val cropName: String = "Paddy",
    val fieldName: String = "North Field (2.5 Ac)"
)

data class CropListing(
    val id: String,
    val sellerName: String,
    val location: String,
    val cropName: String,
    val quantityQtl: Int,
    val pricePerQtl: Double,
    val phone: String,
    val datePosted: String
)

data class FarmerStory(
    val id: String,
    val farmerName: String,
    val location: String,
    val title: String,
    val story: String,
    val likes: Int = 124
)

data class AgriNewsItem(
    val id: String,
    val title: String,
    val date: String,
    val category: String,
    val summary: String
)
