package com.example.data.model

import com.google.gson.annotations.SerializedName

data class MandiRateRecord(
    val commodity: String,
    val variety: String = "Standard",
    val state: String,
    val district: String,
    val market: String,
    @SerializedName("min_price") val minPrice: String,
    @SerializedName("max_price") val maxPrice: String,
    @SerializedName("modal_price") val modalPrice: String,
    @SerializedName("arrival_quantity") val arrivalQuantity: String = "N/A",
    val unit: String = "₹/quintal",
    @SerializedName("market_date") val marketDate: String,
    @SerializedName("fetched_at") val fetchedAt: String? = null,
    val source: String = "AGMARKNET / data.gov.in",
    @SerializedName("verification_status") val verificationStatus: String = "🟢 Verified Government Source",
    @SerializedName("is_cached") val isCached: Boolean = false
)

data class MandiApiResponse(
    val status: String,
    val source: String? = null,
    @SerializedName("cached_at") val cachedAt: String? = null,
    @SerializedName("is_cached") val isCached: Boolean = false,
    val total: Int = 0,
    val records: List<MandiRateRecord> = emptyList(),
    val error: String? = null,
    val message: String? = null
)

// Gemini Search Grounding API Models
data class GeminiSearchRequest(
    val contents: List<GeminiContent>,
    val tools: List<GeminiTool>? = listOf(GeminiTool(googleSearch = emptyMap()))
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiTool(
    @SerializedName("googleSearch") val googleSearch: Map<String, String> = emptyMap()
)

data class GeminiSearchResponse(
    val candidates: List<GeminiCandidate>? = null
)

data class GeminiCandidate(
    val content: GeminiContent? = null,
    val groundingMetadata: GroundingMetadata? = null
)

data class GroundingMetadata(
    val webSearchQueries: List<String>? = null,
    val searchEntryPoint: Map<String, String>? = null
)
