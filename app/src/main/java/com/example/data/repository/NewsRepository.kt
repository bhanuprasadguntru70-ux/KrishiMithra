package com.example.data.repository

import com.example.data.api.NewsDto
import com.example.data.api.NewsRetrofitClient
import com.example.data.local.AgriDao
import com.example.data.model.NewsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.IOException

class NewsRepository(private val dao: AgriDao) {

    val dailyNews: Flow<List<NewsEntity>> = dao.getAllNews()

    suspend fun addNews(news: NewsEntity) {
        dao.insertSingleNews(news)
    }

    /**
     * Fetches daily agriculture news via Retrofit and caches them into Room DB.
     * Follows an offline-first strategy:
     * 1. Attempts remote Retrofit API request.
     * 2. On success, maps response DTOs into Room entities and persists them.
     * 3. On failure (offline or missing key/endpoint), safely ensures fallback daily news is available in Room.
     */
    suspend fun fetchAndCacheDailyNews(): Result<List<NewsEntity>> {
        return try {
            val response = NewsRetrofitClient.service.getDailyAgriNews(apiKey = null)
            val articles = response.articles

            if (articles.isNotEmpty()) {
                val entities = articles.map { dto ->
                    NewsEntity(
                        title = dto.title,
                        titleTe = dto.titleTe,
                        content = dto.content,
                        category = dto.category,
                        videoUrl = dto.videoUrl,
                        imageUrl = dto.imageUrl,
                        source = dto.source,
                        timestamp = if (dto.timestamp > 0) dto.timestamp else System.currentTimeMillis()
                    )
                }
                dao.insertNews(entities)
                Result.success(entities)
            } else {
                ensureFallbackDailyNews()
                val cached = dao.getAllNews().first()
                Result.success(cached)
            }
        } catch (e: IOException) {
            // Network connection issue or timeout - fallback to local database
            ensureFallbackDailyNews()
            val cached = dao.getAllNews().first()
            Result.success(cached)
        } catch (e: Exception) {
            // Generic error - fallback to cached room news
            ensureFallbackDailyNews()
            val cached = dao.getAllNews().first()
            Result.success(cached)
        }
    }

    private suspend fun ensureFallbackDailyNews() {
        val currentNews = dao.getAllNews().first()
        if (currentNews.isEmpty()) {
            val defaultDailyNews = listOf(
                NewsEntity(
                    title = "AP Govt Announces 80% Subsidy on Micro-Irrigation Drip Kits for Rythus",
                    titleTe = "రైతులకు మైక్రో డ్రిప్ నీటిపారుదల పరికరాలపై 80% సబ్సిడీ ప్రకటించిన ఏపీ ప్రభుత్వం",
                    content = "Andhra Pradesh Agriculture Department released new guidelines offering 80% subsidy for small and marginal farmers buying automated drip and sprinkler systems. Register at local Rythu Bharosa Kendras (RBKs) with Pattadar Passbook.",
                    category = "AP Agriculture News",
                    source = "AP Govt Agri Portal",
                    timestamp = System.currentTimeMillis() - 3600000L
                ),
                NewsEntity(
                    title = "Telangana Free Power Supply Scheme Guidelines Released for Rabi Season",
                    titleTe = "యాసంగి సీజన్‌కు ఉచిత విద్యుత్ సరఫరా మార్గదర్శకాలను విడుదల చేసిన తెలంగాణ ప్రభుత్వం",
                    content = "Telangana State Electricity Board assured 24x7 uninterrupted free power supply for all agricultural pump sets during the upcoming Rabi cropping season. Inspections scheduled across all transformer points.",
                    category = "Telangana Agriculture News",
                    source = "TSSPDCL Bulletin",
                    timestamp = System.currentTimeMillis() - 7200000L
                ),
                NewsEntity(
                    title = "Cotton Market Prices Surge to ₹8,200 per Quintal in Warangal & Guntur Yards",
                    titleTe = "వరంగల్, గుంటూరు మార్కెట్లలో క్వింటాల్ పత్తి ధర ₹8,200 కు పెరిగింది",
                    content = "High export demand for long-staple cotton led to a ₹350 price jump per quintal across major cotton yards in Guntur, Warangal, and Khammam. Traders advise proper moisture testing before bringing crop to market.",
                    category = "India Agriculture News",
                    source = "AgriMarket Today",
                    timestamp = System.currentTimeMillis() - 10800000L
                ),
                NewsEntity(
                    title = "Organic Farming Incentive: ₹10,000 per Acre Grant for Zero Budget Farmers",
                    titleTe = "సేంద్రీయ వ్యవసాయం చేసే రైతులకు ఎకరానికి ₹10,000 ప్రోత్సాహకం",
                    content = "Department of Horticulture announced direct benefit transfer (DBT) grant of ₹10,000 per acre for certified organic and natural farmers who avoid chemical fertilizers for 3 consecutive seasons.",
                    category = "Organic Farming",
                    source = "Natural Farming Mission",
                    timestamp = System.currentTimeMillis() - 14400000L
                ),
                NewsEntity(
                    title = "IMD Heavy Rain Alert for Coastal AP Districts: Weather Advisory for Paddy Farmers",
                    titleTe = "కోస్తాంధ్ర జిల్లాలకు బారీ వర్ష సూచన: వరి రైతులకు వాతావరణ శాఖ హెచ్చరిక",
                    content = "Indian Meteorological Department issued yellow alert for Krishna, Guntur, and East Godavari. Farmers advised to clear drainage channels in field beds and delay paddy harvesting till storm passes.",
                    category = "Weather Alert",
                    source = "IMD Amaravati",
                    timestamp = System.currentTimeMillis() - 18000000L
                )
            )
            dao.insertNews(defaultDailyNews)
        }
    }
}
