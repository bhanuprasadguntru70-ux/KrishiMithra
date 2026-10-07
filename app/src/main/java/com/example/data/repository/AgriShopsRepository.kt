package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.model.AgriShopItem
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class AgriShopsRepository {

    private val overpassApi = ApiClient.overpass
    private val nominatimApi = ApiClient.nominatim

    /**
     * Calculate distance between two latitude/longitude points.
     * Result is in kilometres.
     */
    fun calculateDistanceKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {

        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a =
            sin(dLat / 2).pow(2) +
                    cos(Math.toRadians(lat1)) *
                    cos(Math.toRadians(lat2)) *
                    sin(dLon / 2).pow(2)

        val c =
            2.0 * atan2(
                sqrt(a),
                sqrt(1.0 - a)
            )

        val distance = earthRadiusKm * c

        return (distance * 10.0).roundToInt() / 10.0
    }

    /**
     * Find agricultural shops near the selected location.
     *
     * Uses OpenStreetMap Overpass API.
     */
    suspend fun getNearbyShops(
        centerLat: Double,
        centerLng: Double,
        radiusKm: Int = 10,
        categoryFilter: String = "All"
    ): List<AgriShopItem> {

        val radiusMeters = radiusKm * 1000

        val shops = mutableListOf<AgriShopItem>()

        // =====================================================
        // OPENSTREETMAP / OVERPASS
        // =====================================================

        try {

            val overpassQuery = """
                [out:json][timeout:20];

                (
                    node["shop"="agrarian"]
                        (around:$radiusMeters,$centerLat,$centerLng);

                    node["shop"="farm"]
                        (around:$radiusMeters,$centerLat,$centerLng);

                    node["shop"="fertilizer"]
                        (around:$radiusMeters,$centerLat,$centerLng);

                    node["shop"="hardware"]
                        (around:$radiusMeters,$centerLat,$centerLng);

                    node["shop"="machinery"]
                        (around:$radiusMeters,$centerLat,$centerLng);

                    node["name"~"Agri|Agro|Fertilizer|Seed|Pesticide|Tractor|Krishi|Farm|Rythu",i]
                        (around:$radiusMeters,$centerLat,$centerLng);
                );

                out body;
            """.trimIndent()

            val encodedQuery =
                java.net.URLEncoder.encode(
                    overpassQuery,
                    "UTF-8"
                )

            val url =
                "https://overpass-api.de/api/interpreter?data=$encodedQuery"

            val response =
                overpassApi.queryOverpass(url)

            response.elements.forEach { element ->

                val latitude =
                    element.latitude
                        ?: return@forEach

                val longitude =
                    element.longitude
                        ?: return@forEach

                val tags =
                    element.tags ?: emptyMap()

                val name =
                    tags["name"]
                        ?: tags["brand"]
                        ?: "Agricultural Supply Store"

                val phone =
                    tags["phone"]
                        ?: tags["contact:phone"]
                        ?: tags["mobile"]

                val address =
                    tags["addr:street"]
                        ?: tags["addr:suburb"]
                        ?: tags["addr:village"]
                        ?: tags["addr:town"]
                        ?: tags["addr:city"]
                        ?: "Address unavailable"

                val openingHours =
                    tags["opening_hours"]
                        ?: "Opening hours unavailable"

                val category =
                    determineCategory(
                        name = name,
                        tags = tags
                    )

                val distance =
                    calculateDistanceKm(
                        lat1 = centerLat,
                        lon1 = centerLng,
                        lat2 = latitude,
                        lon2 = longitude
                    )

                if (distance <= radiusKm) {

                    shops.add(
                        AgriShopItem(
                            id = "osm_${element.id}",
                            name = name,
                            nameTe = getTeluguNameForShop(
                                name,
                                category
                            ),
                            category = category,
                            address = address,
                            latitude = latitude,
                            longitude = longitude,
                            distanceKm = distance,
                            phone = phone,
                            openingHours = openingHours,
                            source = "OpenStreetMap"
                        )
                    )
                }
            }

        } catch (e: Exception) {

            // If Overpass fails, return an empty list instead of crashing.
        }

        // =====================================================
        // REMOVE DUPLICATES
        // =====================================================

        val uniqueShops =
            shops.distinctBy { shop ->

                "${shop.name.trim().lowercase()}_" +
                        "${(shop.latitude * 1000).toInt()}_" +
                        "${(shop.longitude * 1000).toInt()}"
            }

        // =====================================================
        // CATEGORY FILTER
        // =====================================================

        val filteredShops =
            if (
                categoryFilter.isBlank() ||
                categoryFilter.equals(
                    "All",
                    ignoreCase = true
                )
            ) {

                uniqueShops

            } else {

                uniqueShops.filter { shop ->

                    shop.category.equals(
                        categoryFilter,
                        ignoreCase = true
                    )
                }
            }

        // =====================================================
        // SORT BY DISTANCE
        // =====================================================

        return filteredShops.sortedBy {
            it.distanceKm
        }
    }

    // =========================================================
    // SEARCH LOCATION
    // =========================================================

    suspend fun searchLocationCoordinates(
        query: String
    ): Pair<Double, Double>? {

        return try {

            val results =
                nominatimApi.searchNominatim(
                    query = query
                )

            val first =
                results.firstOrNull()
                    ?: return null

            val latitude =
                first.latitude
                    ?.toDoubleOrNull()
                    ?: return null

            val longitude =
                first.longitude
                    ?.toDoubleOrNull()
                    ?: return null

            Pair(
                latitude,
                longitude
            )

        } catch (e: Exception) {

            null
        }
    }

    // =========================================================
    // REVERSE GEOCODING
    // =========================================================

    suspend fun getPlaceNameFromCoordinates(
        lat: Double,
        lon: Double
    ): String {

        return try {

            val result =
                nominatimApi.reverseGeocode(
                    latitude = lat,
                    longitude = lon
                )

            result.displayName
                ?.split(",")
                ?.take(3)
                ?.joinToString(", ")
                ?: "Location unavailable"

        } catch (e: Exception) {

            "Current Location"
        }
    }

    // =========================================================
    // CATEGORY
    // =========================================================

    private fun determineCategory(
        name: String,
        tags: Map<String, String>
    ): String {

        val lowerName =
            name.lowercase()

        val shopTag =
            tags["shop"]
                ?.lowercase()
                ?: ""

        return when {

            lowerName.contains("fertilizer") ||
                    lowerName.contains("fertiliser") ||
                    lowerName.contains("ఎరువు") ||
                    shopTag == "fertilizer" ->
                "Fertilizer"

            lowerName.contains("seed") ||
                    lowerName.contains("విత్తన") ->
                "Seed"

            lowerName.contains("pesticide") ||
                    lowerName.contains("పురుగుమంద") ->
                "Pesticide"

            lowerName.contains("tractor") ||
                    lowerName.contains("machinery") ||
                    lowerName.contains("ట్రాక్టర్") ||
                    shopTag == "machinery" ->
                "Machinery"

            lowerName.contains("equipment") ||
                    lowerName.contains("hardware") ||
                    lowerName.contains("పరికర") ||
                    shopTag == "hardware" ->
                "Equipment"

            else ->
                "Fertilizer"
        }
    }

    // =========================================================
    // TELUGU SHOP NAME
    // =========================================================

    private fun getTeluguNameForShop(
        nameEn: String,
        category: String
    ): String {

        return when (category) {

            "Fertilizer" ->

                nameEn
                    .replace(
                        "Fertilizers",
                        "ఫెర్టిలైజర్స్",
                        ignoreCase = true
                    )
                    .replace(
                        "Fertilizer",
                        "ఫెర్టిలైజర్",
                        ignoreCase = true
                    )

            "Seed" ->

                nameEn
                    .replace(
                        "Seeds",
                        "సీడ్స్",
                        ignoreCase = true
                    )
                    .replace(
                        "Seed",
                        "విత్తనాలు",
                        ignoreCase = true
                    )

            "Pesticide" ->

                nameEn
                    .replace(
                        "Pesticides",
                        "పెస్టిసైడ్స్",
                        ignoreCase = true
                    )
                    .replace(
                        "Pesticide",
                        "పెస్టిసైడ్",
                        ignoreCase = true
                    )

            "Machinery" ->

                nameEn
                    .replace(
                        "Tractors",
                        "ట్రాక్టర్లు",
                        ignoreCase = true
                    )
                    .replace(
                        "Tractor",
                        "ట్రాక్టర్",
                        ignoreCase = true
                    )
                    .replace(
                        "Machinery",
                        "వ్యవసాయ యంత్రాలు",
                        ignoreCase = true
                    )

            "Equipment" ->

                nameEn
                    .replace(
                        "Agri",
                        "అగ్రి",
                        ignoreCase = true
                    )
                    .replace(
                        "Hardware",
                        "హార్డ్‌వేర్",
                        ignoreCase = true
                    )

            else ->
                nameEn
        }
    }
}