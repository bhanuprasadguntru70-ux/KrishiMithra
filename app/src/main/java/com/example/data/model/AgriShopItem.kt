package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AgriShopItem(
    val id: String,
    val name: String,
    val nameTe: String = "",
    val category: String, // "Fertilizer", "Seed", "Pesticide", "Equipment", "Machinery", "General Agri"
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double,
    val phone: String? = null,
    val openingHours: String? = null,
    val rating: Double? = null,
    val isOpenNow: Boolean? = true,
    val source: String = "Live OSM Places"
)
