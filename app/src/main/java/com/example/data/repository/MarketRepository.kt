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

    val allMarketCrops: Flow<List<MarketCropEntity>> =
        dao.getAllMarketCrops()

    suspend fun updateMarketCrop(
        crop: MarketCropEntity
    ) {
        dao.updateMarketCrop(crop)
    }

    suspend fun fetchAndCacheMarketPrices(
        apiKey: String
    ): Result<Unit> {

        return try {

            val response =
                MarketRetrofitClient.service.getMarketPrices(
                    apiKey = BuildConfig.MANDI_API_KEY,
                    format = "json",
                    limit = 100
                )

            val fetchedCrops = response.records

            if (fetchedCrops.isEmpty()) {
                return Result.failure(
                    IllegalStateException(
                        "No current market records were returned."
                    )
                )
            }

            val existingCrops =
                dao.getAllMarketCrops().first()

            val existingMap =
                existingCrops.associateBy {
                    "${it.cropName}_${it.market}_${it.district}_${it.state}"
                }

            val entitiesToInsert =
                fetchedCrops.mapNotNull { dto ->

                    val modalPrice =
                        dto.modal_price
                            ?.trim()
                            ?.toDoubleOrNull()

                    val minPrice =
                        dto.min_price
                            ?.trim()
                            ?.toDoubleOrNull()

                    val maxPrice =
                        dto.max_price
                            ?.trim()
                            ?.toDoubleOrNull()

                    if (
                        modalPrice == null &&
                        minPrice == null &&
                        maxPrice == null
                    ) {
                        return@mapNotNull null
                    }

                    val cropName =
                        dto.commodity
                            ?.trim()
                            ?.takeIf { it.isNotEmpty() }
                            ?: dto.cropName
                                ?.trim()
                                ?.takeIf { it.isNotEmpty() }
                            ?: return@mapNotNull null

                    val market =
                        dto.market
                            ?.trim()
                            ?.takeIf { it.isNotEmpty() }
                            ?: return@mapNotNull null

                    val district =
                        dto.district
                            ?.trim()
                            ?.takeIf { it.isNotEmpty() }
                            ?: return@mapNotNull null

                    val state =
                        dto.state
                            ?.trim()
                            ?.takeIf { it.isNotEmpty() }
                            ?: return@mapNotNull null

                    val key =
                        "${cropName}_${market}_${district}_${state}"

                    val existing =
                        existingMap[key]

                    MarketCropEntity(
                        id = existing?.id ?: 0,

                        cropName = cropName,

                        market = market,

                        district = district,

                        state = state,

                        todayPrice =
                            modalPrice
                                ?: minPrice
                                ?: maxPrice
                                ?: return@mapNotNull null,

                        yesterdayPrice =
                            existing?.yesterdayPrice ?: 0.0,

                        highestPrice =
                            maxPrice
                                ?: existing?.highestPrice
                                ?: 0.0,

                        lowestPrice =
                            minPrice
                                ?: existing?.lowestPrice
                                ?: 0.0,

                        isFavorite =
                            existing?.isFavorite ?: false,

                        priceAlertEnabled =
                            existing?.priceAlertEnabled ?: false,

                        alertPrice =
                            existing?.alertPrice ?: 0.0,

                        cropNameTe =
                            dto.cropNameTe
                                ?.trim()
                                ?.takeIf { it.isNotEmpty() }
                                ?: existing?.cropNameTe
                                ?: "",

                        category =
                            dto.category
                                ?.trim()
                                ?.takeIf { it.isNotEmpty() }
                                ?: MarketCropEntity.determineCategory(
                                    cropName
                                ),

                        imageUrl =
                            dto.imageUrl
                                ?.trim()
                                ?.takeIf { it.isNotEmpty() }
                                ?: existing?.imageUrl
                                ?: "",

                        lastUpdated =
                            dto.arrival_date
                                ?.trim()
                                ?.takeIf { it.isNotEmpty() }
                                ?.let {
                                    "Market date: $it"
                                }
                                ?: "Market date unavailable"
                    )
                }

            if (entitiesToInsert.isEmpty()) {
                return Result.failure(
                    IllegalStateException(
                        "No valid market price records were received."
                    )
                )
            }

            dao.insertMarketCrops(
                entitiesToInsert
            )

            Result.success(Unit)

        } catch (e: IOException) {

            Result.failure(
                IOException(
                    "Unable to connect to the market data service.",
                    e
                )
            )

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}