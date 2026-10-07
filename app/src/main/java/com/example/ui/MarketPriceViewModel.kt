package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.MarketRetrofitClient
import com.example.data.local.AgriDao
import com.example.data.model.MarketCropEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.IOException

class MarketRepository(
    private val dao: AgriDao
) {

    /**
     * All market crops stored in Room.
     *
     * The UI observes this Flow, so whenever Room is updated,
     * the market screen automatically receives the new data.
     */
    val allMarketCrops: Flow<List<MarketCropEntity>> =
        dao.getAllMarketCrops()

    /**
     * Update an existing market crop.
     *
     * Used for:
     * - Favorites
     * - Price alerts
     */
    suspend fun updateMarketCrop(crop: MarketCropEntity) {
        dao.updateMarketCrop(crop)
    }

    /**
     * Fetch latest market prices from Data.gov.in
     * and save them into the local Room database.
     *
     * User preferences such as favorites and price alerts
     * are preserved when fresh market data arrives.
     */
    suspend fun fetchAndCacheMarketPrices(): Result<Unit> {

        return try {

            // API key comes from Windows Environment Variables
            // through BuildConfig.
            val apiKey = BuildConfig.MANDI_API_KEY

            // Do not call the API if the key was not configured.
            if (apiKey.isBlank()) {
                return Result.failure(
                    IllegalStateException(
                        "MANDI_API_KEY is not configured."
                    )
                )
            }

            // Fetch latest market data.
            val response =
                MarketRetrofitClient.service.getMarketPrices(
                    apiKey = apiKey,
                    format = "json",
                    limit = 100
                )

            val fetchedCrops = response.records

            // API returned no records.
            if (fetchedCrops.isEmpty()) {
                return Result.failure(
                    IllegalStateException(
                        "Market API returned no records."
                    )
                )
            }

            // Read existing Room data.
            // This allows us to preserve favorites and alerts.
            val existingCrops =
                dao.getAllMarketCrops().first()

            val existingMap =
                existingCrops.associateBy {
                    createCropKey(
                        cropName = it.cropName,
                        market = it.market,
                        district = it.district,
                        state = it.state
                    )
                }

            /**
             * Convert API DTOs into Room entities.
             */
            val entitiesToInsert =
                fetchedCrops.map { dto ->

                    val cropName =
                        dto.resolvedCropName.trim()

                    val market =
                        dto.resolvedMarket.trim()

                    val district =
                        dto.resolvedDistrict.trim()

                    val state =
                        dto.resolvedState.trim()

                    val key =
                        createCropKey(
                            cropName = cropName,
                            market = market,
                            district = district,
                            state = state
                        )

                    val existing =
                        existingMap[key]

                    val modalPrice =
                        dto.resolvedModalPrice

                    val minPrice =
                        dto.resolvedMinPrice

                    val maxPrice =
                        dto.resolvedMaxPrice

                    MarketCropEntity(

                        id = existing?.id ?: 0,

                        cropName = cropName,

                        market = market,

                        district = district,

                        state = state,

                        todayPrice = modalPrice,

                        /**
                         * If we already have this crop,
                         * keep its previous price.
                         *
                         * Otherwise use the current price
                         * as the initial previous value.
                         */
                        yesterdayPrice =
                            existing?.todayPrice
                                ?: modalPrice,

                        highestPrice = maxPrice,

                        lowestPrice = minPrice,

                        /**
                         * Preserve user favorite setting.
                         */
                        isFavorite =
                            existing?.isFavorite
                                ?: false,

                        /**
                         * Preserve price-alert setting.
                         */
                        priceAlertEnabled =
                            existing?.priceAlertEnabled
                                ?: false,

                        /**
                         * Preserve alert target.
                         */
                        alertPrice =
                            existing?.alertPrice
                                ?: 0.0,

                        cropNameTe =
                            dto.cropNameTe
                                ?.takeIf { it.isNotBlank() }
                                ?: existing?.cropNameTe
                                ?: "",

                        category =
                            resolveCategory(
                                cropName = cropName,
                                apiCategory = dto.category
                            ),

                        imageUrl =
                            dto.imageUrl
                                ?.takeIf { it.isNotBlank() }
                                ?: existing?.imageUrl
                                ?: "",

                        lastUpdated =
                            dto.arrival_date
                                ?: dto.resolvedArrivalDate
                    )
                }

            /**
             * Insert/update fresh market data in Room.
             */
            dao.insertMarketCrops(
                entitiesToInsert
            )

            Result.success(Unit)

        } catch (e: IOException) {

            // Internet/network problem.
            Result.failure(
                IOException(
                    "Unable to connect to the market API.",
                    e
                )
            )

        } catch (e: Exception) {

            // API, JSON, Room or other unexpected problem.
            Result.failure(e)
        }
    }

    /**
     * Creates a stable key for matching fresh API data
     * with existing Room data.
     */
    private fun createCropKey(
        cropName: String?,
        market: String?,
        district: String?,
        state: String?
    ): String {

        return listOf(
            cropName.orEmpty(),
            market.orEmpty(),
            district.orEmpty(),
            state.orEmpty()
        )
            .joinToString("|")
            .lowercase()
            .trim()
    }

    /**
     * Determines a useful category when the API does not
     * provide a reliable category.
     */
    private fun resolveCategory(
        cropName: String,
        apiCategory: String?
    ): String {

        val category =
            apiCategory
                ?.trim()
                ?.takeIf {
                    it.isNotEmpty() &&
                            !it.equals(
                                "Food Grains",
                                ignoreCase = true
                            )
                }

        if (category != null) {
            return category
        }

        return MarketCropEntity.determineCategory(
            cropName
        )
    }
}