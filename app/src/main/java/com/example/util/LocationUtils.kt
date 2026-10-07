package com.example.util

import java.util.Locale

/**
 * Geographic utility for KrishiMithra.
 * Provides Geohash calculation to group users into scalable geographic clusters
 * (~5km to ~20km radius) for FCM weather topic subscriptions.
 * Prevents creating millions of individual FCM topics while maintaining high spatial accuracy.
 */
object LocationUtils {

    private const val BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"

    /**
     * Encodes latitude and longitude into a Geohash string of requested length.
     * Length 4: ~20km x 20km area (Ideal for district/mandal weather clusters)
     * Length 5: ~5km x 5km area (Ideal for village/town clusters)
     */
    fun encodeGeohash(lat: Double, lon: Double, length: Int = 4): String {
        var isEven = true
        var latMin = -90.0
        var latMax = 90.0
        var lonMin = -180.0
        var lonMax = 180.0

        var bit = 0
        var ch = 0
        val geohash = StringBuilder()

        while (geohash.length < length) {
            if (isEven) {
                val mid = (lonMin + lonMax) / 2
                if (lon >= mid) {
                    ch = ch or (1 shl (4 - bit))
                    lonMin = mid
                } else {
                    lonMax = mid
                }
            } else {
                val mid = (latMin + latMax) / 2
                if (lat >= mid) {
                    ch = ch or (1 shl (4 - bit))
                    latMin = mid
                } else {
                    latMax = mid
                }
            }

            isEven = !isEven
            if (bit < 4) {
                bit++
            } else {
                geohash.append(BASE32[ch])
                bit = 0
                ch = 0
            }
        }

        return geohash.toString()
    }

    /**
     * Converts a location string (e.g., "Anantapur") into a clean FCM topic key.
     * FCM topics only allow letters, numbers, and underscores ([a-zA-Z0-9-_.~%]+).
     */
    fun sanitizeForFcmTopic(input: String): String {
        return input.trim().lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9_]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
            .take(32)
    }

    /**
     * Returns the FCM topic name for a geohash cluster.
     * Example: "weather_gh_tf3k"
     */
    fun getGeohashFcmTopic(lat: Double, lon: Double): String {
        val gh = encodeGeohash(lat, lon, length = 4)
        return "weather_gh_$gh"
    }

    /**
     * Returns the FCM topic name for a district region.
     * Example: "weather_district_anantapur"
     */
    fun getDistrictFcmTopic(districtName: String): String {
        val cleanName = sanitizeForFcmTopic(districtName)
        return if (cleanName.isNotBlank()) "weather_district_$cleanName" else "weather_district_general"
    }
}
