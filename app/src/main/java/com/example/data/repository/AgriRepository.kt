package com.example.data.repository

import android.graphics.Bitmap
import com.example.data.api.*
import com.example.BuildConfig
import com.example.data.local.AgriDao
import com.example.data.model.*
import com.example.ui.screens.WeatherInfo
import com.example.ui.screens.HourlyForecast
import com.example.ui.screens.DailyForecast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first

class AgriRepository(private val dao: AgriDao) {

    // --- USER ---
    val currentUser: Flow<UserEntity?> = dao.getCurrentUserFlow().catch { emit(null) }

    suspend fun registerUser(user: UserEntity) {
        dao.insertUser(user)
    }

    suspend fun loginUser(email: String, password: String): Boolean {
        val user = dao.getUserByEmail(email)
        return if (user != null) {
            dao.logoutAll() // log out other sessions
            dao.insertUser(user.copy(isLoggedIn = true))
            true
        } else {
            false
        }
    }

    suspend fun logout() {
        dao.logoutAll()
    }

    suspend fun updateUserProfile(user: UserEntity) {
        dao.updateUser(user)
    }

    // --- FARMS ---
    val allFarms: Flow<List<FarmEntity>> = dao.getAllFarms().catch { emit(emptyList()) }

    suspend fun addFarm(farm: FarmEntity) {
        dao.insertFarm(farm)
        // Add a notification alert for reminder
        val farmName = "${farm.crop} Farm"
        dao.insertNotification(
            NotificationAlertEntity(
                title = "New Farm Added",
                message = "Your $farmName has been registered successfully. Reminders are configured.",
                category = "Reminder"
            )
        )
    }

    suspend fun removeFarm(farm: FarmEntity) {
        dao.deleteFarm(farm)
    }

    // --- DISEASE REPORTS ---
    val allReports: Flow<List<DiseaseReportEntity>> = dao.getAllDiseaseReports().catch { emit(emptyList()) }

    suspend fun saveReport(report: DiseaseReportEntity) {
        dao.insertDiseaseReport(report)
        // Trigger a notification
        dao.insertNotification(
            NotificationAlertEntity(
                title = "Disease Report Saved",
                message = "Report for ${report.cropName} (${report.diseaseName}) has been saved to your profile.",
                category = "Disease"
            )
        )
    }

    suspend fun deleteReport(id: Int) {
        dao.deleteDiseaseReportById(id)
    }

    // --- MARKET CROPS ---
    val allMarketCrops: Flow<List<MarketCropEntity>> = dao.getAllMarketCrops().catch { emit(emptyList()) }

    suspend fun updateMarketCrop(crop: MarketCropEntity) {
        dao.updateMarketCrop(crop)
    }

    suspend fun refreshLiveMarketPrices(): Result<Unit> {
        return MarketRepository(dao).fetchAndCacheMarketPrices()
    }
    suspend fun prepopulateMarketCrops() {
        val currentCrops = dao.getAllMarketCrops().first()
        // If the database has fewer than 35 crops, let's clear it and insert the rich, full categories list
        if (currentCrops.size < 35) {
            dao.deleteAllMarketCrops()
            val initialCrops = listOf(
                // ==================== VEGETABLES ====================
                // AP
                MarketCropEntity(
                    cropName = "Tomato",
                    market = "Madanapalle Mandi",
                    district = "Chittoor",
                    todayPrice = 1800.0,
                    yesterdayPrice = 1950.0,
                    highestPrice = 2400.0,
                    lowestPrice = 1200.0,
                    isFavorite = true,
                    cropNameTe = "టమోటా",
                    category = "Vegetables",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1518977676601-b53f82aba655?q=80&w=400",
                    lastUpdated = "Today, 09:30 AM"
                ),
                MarketCropEntity(
                    cropName = "Onion",
                    market = "Kurnool APMC",
                    district = "Kurnool",
                    todayPrice = 2600.0,
                    yesterdayPrice = 2700.0,
                    highestPrice = 3000.0,
                    lowestPrice = 1800.0,
                    cropNameTe = "ఉల్లిపాయ",
                    category = "Vegetables",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1508747703725-719777637510?q=80&w=400",
                    lastUpdated = "Today, 08:45 AM"
                ),
                MarketCropEntity(
                    cropName = "Brinjal",
                    market = "Guntur Market",
                    district = "Guntur",
                    todayPrice = 1200.0,
                    yesterdayPrice = 1150.0,
                    highestPrice = 1500.0,
                    lowestPrice = 900.0,
                    cropNameTe = "వంకాయ",
                    category = "Vegetables",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1590379497901-bf193051bc72?q=80&w=400",
                    lastUpdated = "Today, 09:10 AM"
                ),
                MarketCropEntity(
                    cropName = "Ladies Finger (Okra)",
                    market = "Nellore Mandi",
                    district = "Nellore",
                    todayPrice = 1500.0,
                    yesterdayPrice = 1600.0,
                    highestPrice = 1800.0,
                    lowestPrice = 1200.0,
                    cropNameTe = "బెండకాయ",
                    category = "Vegetables",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1628155930542-3c7a64e2c833?q=80&w=400",
                    lastUpdated = "Today, 08:20 AM"
                ),
                MarketCropEntity(
                    cropName = "Green Chilli",
                    market = "Anantapur Yard",
                    district = "Anantapur",
                    todayPrice = 3200.0,
                    yesterdayPrice = 3100.0,
                    highestPrice = 3500.0,
                    lowestPrice = 2800.0,
                    cropNameTe = "పచ్చిమిర్చి",
                    category = "Vegetables",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1565557623262-b51c2513a641?q=80&w=400",
                    lastUpdated = "Today, 09:15 AM"
                ),
                // TS
                MarketCropEntity(
                    cropName = "Tomato",
                    market = "Bowenpally Mandi",
                    district = "Hyderabad",
                    todayPrice = 2100.0,
                    yesterdayPrice = 2050.0,
                    highestPrice = 2500.0,
                    lowestPrice = 1500.0,
                    cropNameTe = "టమోటా",
                    category = "Vegetables",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1595855759920-86582396756a?q=80&w=400",
                    lastUpdated = "Today, 10:15 AM"
                ),
                MarketCropEntity(
                    cropName = "Onion",
                    market = "Mahabubnagar APMC",
                    district = "Mahabubnagar",
                    todayPrice = 2500.0,
                    yesterdayPrice = 2600.0,
                    highestPrice = 2800.0,
                    lowestPrice = 2000.0,
                    cropNameTe = "ఉల్లిపాయ",
                    category = "Vegetables",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1618213837799-25d55524f676?q=80&w=400",
                    lastUpdated = "Today, 09:40 AM"
                ),
                MarketCropEntity(
                    cropName = "Brinjal",
                    market = "Warangal APMC",
                    district = "Warangal",
                    todayPrice = 1100.0,
                    yesterdayPrice = 1150.0,
                    highestPrice = 1300.0,
                    lowestPrice = 850.0,
                    cropNameTe = "వంకాయ",
                    category = "Vegetables",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1590379497901-bf193051bc72?q=80&w=400",
                    lastUpdated = "Today, 08:50 AM"
                ),
                MarketCropEntity(
                    cropName = "Ladies Finger (Okra)",
                    market = "Khammam Mandi",
                    district = "Khammam",
                    todayPrice = 1650.0,
                    yesterdayPrice = 1600.0,
                    highestPrice = 1900.0,
                    lowestPrice = 1300.0,
                    cropNameTe = "బెండకాయ",
                    category = "Vegetables",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1628155930542-3c7a64e2c833?q=80&w=400",
                    lastUpdated = "Today, 08:55 AM"
                ),
                MarketCropEntity(
                    cropName = "Green Chilli",
                    market = "Medak APMC",
                    district = "Medak",
                    todayPrice = 3300.0,
                    yesterdayPrice = 3400.0,
                    highestPrice = 3700.0,
                    lowestPrice = 3000.0,
                    cropNameTe = "పచ్చిమిర్చి",
                    category = "Vegetables",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1565557623262-b51c2513a641?q=80&w=400",
                    lastUpdated = "Today, 09:20 AM"
                ),
                // All India
                MarketCropEntity(
                    cropName = "Potato",
                    market = "Agra Mandi",
                    district = "Agra (UP)",
                    todayPrice = 1450.0,
                    yesterdayPrice = 1400.0,
                    highestPrice = 1600.0,
                    lowestPrice = 1100.0,
                    cropNameTe = "బంగాళాదుంప",
                    category = "Vegetables",
                    state = "All Indian States",
                    imageUrl = "https://images.unsplash.com/photo-1518977676601-b53f82aba655?q=80&w=400",
                    lastUpdated = "Today, 09:15 AM"
                ),
                MarketCropEntity(
                    cropName = "Onion (Nashik)",
                    market = "Lasalgaon Market",
                    district = "Nashik (MH)",
                    todayPrice = 2850.0,
                    yesterdayPrice = 2800.0,
                    highestPrice = 3200.0,
                    lowestPrice = 2000.0,
                    isFavorite = true,
                    cropNameTe = "ఉల్లిపాయ",
                    category = "Vegetables",
                    state = "All Indian States",
                    imageUrl = "https://images.unsplash.com/photo-1618213837799-25d55524f676?q=80&w=400",
                    lastUpdated = "Today, 11:00 AM"
                ),

                // ==================== FRUITS ====================
                // AP
                MarketCropEntity(
                    cropName = "Mango (Banginapalli)",
                    market = "Nuzvid Market",
                    district = "Eluru",
                    todayPrice = 4500.0,
                    yesterdayPrice = 4200.0,
                    highestPrice = 4800.0,
                    lowestPrice = 3500.0,
                    isFavorite = true,
                    cropNameTe = "బంగినపల్లి మామిడి",
                    category = "Fruits",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1553279768-865429fa0078?q=80&w=400",
                    lastUpdated = "Today, 10:00 AM"
                ),
                MarketCropEntity(
                    cropName = "Banana",
                    market = "Pulivendula APMC",
                    district = "Kadapa",
                    todayPrice = 1200.0,
                    yesterdayPrice = 1150.0,
                    highestPrice = 1300.0,
                    lowestPrice = 900.0,
                    cropNameTe = "అరటి పండు",
                    category = "Fruits",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?q=80&w=400",
                    lastUpdated = "Today, 08:30 AM"
                ),
                MarketCropEntity(
                    cropName = "Papaya",
                    market = "Anantapur APMC",
                    district = "Anantapur",
                    todayPrice = 1800.0,
                    yesterdayPrice = 1750.0,
                    highestPrice = 2000.0,
                    lowestPrice = 1500.0,
                    cropNameTe = "బొప్పాయి",
                    category = "Fruits",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1614707267537-b85acf00c4b8?q=80&w=400",
                    lastUpdated = "Today, 08:45 AM"
                ),
                MarketCropEntity(
                    cropName = "Sweet Orange (Mosambi)",
                    market = "Tirupati Mandi",
                    district = "Tirupati",
                    todayPrice = 3100.0,
                    yesterdayPrice = 3000.0,
                    highestPrice = 3400.0,
                    lowestPrice = 2700.0,
                    cropNameTe = "బత్తాయి",
                    category = "Fruits",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?q=80&w=400",
                    lastUpdated = "Today, 09:30 AM"
                ),
                // TS
                MarketCropEntity(
                    cropName = "Mango",
                    market = "Jagtial Mandi",
                    district = "Jagtial",
                    todayPrice = 4300.0,
                    yesterdayPrice = 4400.0,
                    highestPrice = 4700.0,
                    lowestPrice = 3400.0,
                    cropNameTe = "మామిడి పండు",
                    category = "Fruits",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1553279768-865429fa0078?q=80&w=400",
                    lastUpdated = "Today, 10:15 AM"
                ),
                MarketCropEntity(
                    cropName = "Sweet Orange (Mosambi)",
                    market = "Nalgonda Mandi",
                    district = "Nalgonda",
                    todayPrice = 3200.0,
                    yesterdayPrice = 3350.0,
                    highestPrice = 3600.0,
                    lowestPrice = 2800.0,
                    cropNameTe = "బత్తాయి",
                    category = "Fruits",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?q=80&w=400",
                    lastUpdated = "Today, 09:45 AM"
                ),
                MarketCropEntity(
                    cropName = "Banana",
                    market = "Khammam APMC",
                    district = "Khammam",
                    todayPrice = 1150.0,
                    yesterdayPrice = 1100.0,
                    highestPrice = 1250.0,
                    lowestPrice = 850.0,
                    cropNameTe = "అరటి పండు",
                    category = "Fruits",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?q=80&w=400",
                    lastUpdated = "Today, 08:35 AM"
                ),
                MarketCropEntity(
                    cropName = "Guava",
                    market = "Rangareddy Yard",
                    district = "Rangareddy",
                    todayPrice = 2200.0,
                    yesterdayPrice = 2100.0,
                    highestPrice = 2500.0,
                    lowestPrice = 1800.0,
                    cropNameTe = "జామ పండు",
                    category = "Fruits",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?q=80&w=400",
                    lastUpdated = "Today, 09:10 AM"
                ),
                // All India
                MarketCropEntity(
                    cropName = "Apple (Shimla)",
                    market = "Azadpur Mandi",
                    district = "Delhi (DL)",
                    todayPrice = 9500.0,
                    yesterdayPrice = 9800.0,
                    highestPrice = 11000.0,
                    lowestPrice = 8000.0,
                    cropNameTe = "ఆపిల్ పండు",
                    category = "Fruits",
                    state = "All Indian States",
                    imageUrl = "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?q=80&w=400",
                    lastUpdated = "Today, 11:15 AM"
                ),

                // ==================== FOOD GRAINS ====================
                // AP
                MarketCropEntity(
                    cropName = "Paddy (Rice)",
                    market = "Nellore Mandi",
                    district = "Nellore",
                    todayPrice = 2450.0,
                    yesterdayPrice = 2400.0,
                    highestPrice = 2600.0,
                    lowestPrice = 2200.0,
                    cropNameTe = "వరి ధాన్యం",
                    category = "Food Grains",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1586201375761-83865001e31c?q=80&w=400",
                    lastUpdated = "Today, 07:45 AM"
                ),
                MarketCropEntity(
                    cropName = "Paddy (Super Fine)",
                    market = "Kakinada APMC",
                    district = "East Godavari",
                    todayPrice = 2600.0,
                    yesterdayPrice = 2550.0,
                    highestPrice = 2750.0,
                    lowestPrice = 2300.0,
                    cropNameTe = "సన్న వరి ధాన్యం",
                    category = "Food Grains",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1586201375761-83865001e31c?q=80&w=400",
                    lastUpdated = "Today, 08:00 AM"
                ),
                MarketCropEntity(
                    cropName = "Maize (Corn)",
                    market = "Eluru Mandi",
                    district = "Eluru",
                    todayPrice = 2050.0,
                    yesterdayPrice = 2000.0,
                    highestPrice = 2150.0,
                    lowestPrice = 1850.0,
                    cropNameTe = "మొక్కజొన్న",
                    category = "Food Grains",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1551754625-702917c30f4e?q=80&w=400",
                    lastUpdated = "Today, 08:50 AM"
                ),
                MarketCropEntity(
                    cropName = "Sorghum (Jowar)",
                    market = "Kurnool APMC",
                    district = "Kurnool",
                    todayPrice = 2800.0,
                    yesterdayPrice = 2750.0,
                    highestPrice = 2950.0,
                    lowestPrice = 2500.0,
                    cropNameTe = "జొన్నలు",
                    category = "Food Grains",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?q=80&w=400",
                    lastUpdated = "Today, 09:05 AM"
                ),
                // TS
                MarketCropEntity(
                    cropName = "Paddy (Rice)",
                    market = "Suryapet Mandi",
                    district = "Suryapet",
                    todayPrice = 2380.0,
                    yesterdayPrice = 2390.0,
                    highestPrice = 2550.0,
                    lowestPrice = 2150.0,
                    cropNameTe = "వరి ధాన్యం",
                    category = "Food Grains",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1586201375761-83865001e31c?q=80&w=400",
                    lastUpdated = "Today, 08:15 AM"
                ),
                MarketCropEntity(
                    cropName = "Paddy (Sona Masuri)",
                    market = "Nizamabad Mandi",
                    district = "Nizamabad",
                    todayPrice = 2550.0,
                    yesterdayPrice = 2500.0,
                    highestPrice = 2700.0,
                    lowestPrice = 2350.0,
                    cropNameTe = "సోనా మసూరి వరి",
                    category = "Food Grains",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1586201375761-83865001e31c?q=80&w=400",
                    lastUpdated = "Today, 08:25 AM"
                ),
                MarketCropEntity(
                    cropName = "Maize (Corn)",
                    market = "Warangal APMC",
                    district = "Warangal",
                    todayPrice = 2150.0,
                    yesterdayPrice = 2100.0,
                    highestPrice = 2250.0,
                    lowestPrice = 1900.0,
                    cropNameTe = "మొక్కజొన్న",
                    category = "Food Grains",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1551754625-702917c30f4e?q=80&w=400",
                    lastUpdated = "Today, 09:00 AM"
                ),
                MarketCropEntity(
                    cropName = "Sorghum (Jowar)",
                    market = "Adilabad APMC",
                    district = "Adilabad",
                    todayPrice = 2750.0,
                    yesterdayPrice = 2800.0,
                    highestPrice = 2900.0,
                    lowestPrice = 2400.0,
                    cropNameTe = "జొన్నలు",
                    category = "Food Grains",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?q=80&w=400",
                    lastUpdated = "Today, 09:30 AM"
                ),
                // All India
                MarketCropEntity(
                    cropName = "Wheat",
                    market = "Khanna Mandi",
                    district = "Ludhiana (PB)",
                    todayPrice = 2275.0,
                    yesterdayPrice = 2270.0,
                    highestPrice = 2350.0,
                    lowestPrice = 2150.0,
                    cropNameTe = "గోధుమలు",
                    category = "Food Grains",
                    state = "All Indian States",
                    imageUrl = "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?q=80&w=400",
                    lastUpdated = "Today, 11:30 AM"
                ),

                // ==================== FLOWERS ====================
                // AP
                MarketCropEntity(
                    cropName = "Jasmine",
                    market = "Guntur Flower Yard",
                    district = "Guntur",
                    todayPrice = 180.0,
                    yesterdayPrice = 150.0,
                    highestPrice = 220.0,
                    lowestPrice = 120.0,
                    cropNameTe = "మల్లెపూలు",
                    category = "Flowers",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1596436889106-be35e843f974?q=80&w=400",
                    lastUpdated = "Today, 07:30 AM"
                ),
                MarketCropEntity(
                    cropName = "Rose",
                    market = "Kadiyam Nurseries",
                    district = "East Godavari",
                    todayPrice = 250.0,
                    yesterdayPrice = 240.0,
                    highestPrice = 280.0,
                    lowestPrice = 200.0,
                    cropNameTe = "గులాబీలు",
                    category = "Flowers",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=400",
                    lastUpdated = "Today, 07:45 AM"
                ),
                MarketCropEntity(
                    cropName = "Chrysanthemum",
                    market = "Chittoor Market",
                    district = "Chittoor",
                    todayPrice = 120.0,
                    yesterdayPrice = 110.0,
                    highestPrice = 140.0,
                    lowestPrice = 90.0,
                    cropNameTe = "చామంతి పూలు",
                    category = "Flowers",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1596436889106-be35e843f974?q=80&w=400",
                    lastUpdated = "Today, 08:10 AM"
                ),
                // TS
                MarketCropEntity(
                    cropName = "Marigold",
                    market = "Gudimalkapur Flower Yard",
                    district = "Hyderabad",
                    todayPrice = 60.0,
                    yesterdayPrice = 65.0,
                    highestPrice = 80.0,
                    lowestPrice = 40.0,
                    cropNameTe = "బంతిపూలు",
                    category = "Flowers",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1508747703725-719777637510?q=80&w=400",
                    lastUpdated = "Today, 08:00 AM"
                ),
                MarketCropEntity(
                    cropName = "Jasmine",
                    market = "Rangareddy Flower Yard",
                    district = "Rangareddy",
                    todayPrice = 190.0,
                    yesterdayPrice = 180.0,
                    highestPrice = 230.0,
                    lowestPrice = 130.0,
                    cropNameTe = "మల్లెపూలు",
                    category = "Flowers",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1596436889106-be35e843f974?q=80&w=400",
                    lastUpdated = "Today, 07:50 AM"
                ),
                MarketCropEntity(
                    cropName = "Rose",
                    market = "Gudimalkapur Flower Yard",
                    district = "Hyderabad",
                    todayPrice = 270.0,
                    yesterdayPrice = 260.0,
                    highestPrice = 300.0,
                    lowestPrice = 220.0,
                    cropNameTe = "గులాబీలు",
                    category = "Flowers",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=400",
                    lastUpdated = "Today, 08:05 AM"
                ),

                // ==================== SPICES ====================
                // AP
                MarketCropEntity(
                    cropName = "Guntur Red Chilli",
                    market = "Guntur Mirchi Yard",
                    district = "Guntur",
                    todayPrice = 21500.0,
                    yesterdayPrice = 21800.0,
                    highestPrice = 24000.0,
                    lowestPrice = 18000.0,
                    isFavorite = true,
                    cropNameTe = "గుంటూరు మిరపకాయ",
                    category = "Spices",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?q=80&w=400",
                    lastUpdated = "Today, 09:10 AM"
                ),
                MarketCropEntity(
                    cropName = "Turmeric",
                    market = "Duggirala Mandi",
                    district = "Guntur",
                    todayPrice = 12800.0,
                    yesterdayPrice = 12700.0,
                    highestPrice = 13500.0,
                    lowestPrice = 11000.0,
                    cropNameTe = "పసుపు",
                    category = "Spices",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?q=80&w=400",
                    lastUpdated = "Today, 09:25 AM"
                ),
                MarketCropEntity(
                    cropName = "Garlic",
                    market = "Kurnool APMC",
                    district = "Kurnool",
                    todayPrice = 14500.0,
                    yesterdayPrice = 14000.0,
                    highestPrice = 16000.0,
                    lowestPrice = 12000.0,
                    cropNameTe = "వెల్లుల్లి",
                    category = "Spices",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?q=80&w=400",
                    lastUpdated = "Today, 09:50 AM"
                ),
                // TS
                MarketCropEntity(
                    cropName = "Red Chilli",
                    market = "Warangal Mirchi Yard",
                    district = "Warangal",
                    todayPrice = 20500.0,
                    yesterdayPrice = 20200.0,
                    highestPrice = 22500.0,
                    lowestPrice = 17500.0,
                    cropNameTe = "వరంగల్ మిరపకాయ",
                    category = "Spices",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?q=80&w=400",
                    lastUpdated = "Today, 09:15 AM"
                ),
                MarketCropEntity(
                    cropName = "Turmeric",
                    market = "Nizamabad Mandi",
                    district = "Nizamabad",
                    todayPrice = 13200.0,
                    yesterdayPrice = 13000.0,
                    highestPrice = 14500.0,
                    lowestPrice = 11500.0,
                    cropNameTe = "పసుపు",
                    category = "Spices",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?q=80&w=400",
                    lastUpdated = "Today, 10:00 AM"
                ),
                MarketCropEntity(
                    cropName = "Ginger",
                    market = "Zaheerabad Mandi",
                    district = "Sangareddy",
                    todayPrice = 11500.0,
                    yesterdayPrice = 11200.0,
                    highestPrice = 12500.0,
                    lowestPrice = 9800.0,
                    cropNameTe = "అల్లం",
                    category = "Spices",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?q=80&w=400",
                    lastUpdated = "Today, 09:35 AM"
                ),

                // ==================== OIL SEEDS ====================
                // AP
                MarketCropEntity(
                    cropName = "Groundnut (Peanuts)",
                    market = "Anantapur APMC",
                    district = "Anantapur",
                    todayPrice = 7400.0,
                    yesterdayPrice = 7300.0,
                    highestPrice = 7800.0,
                    lowestPrice = 6500.0,
                    cropNameTe = "వేరుశనగ",
                    category = "Oil Seeds",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1568254183919-78a4f43a2877?q=80&w=400",
                    lastUpdated = "Today, 10:20 AM"
                ),
                MarketCropEntity(
                    cropName = "Sesame Seeds (Till)",
                    market = "Kadapa APMC",
                    district = "Kadapa",
                    todayPrice = 1150.0,
                    yesterdayPrice = 1120.0,
                    highestPrice = 1250.0,
                    lowestPrice = 1000.0,
                    cropNameTe = "నువ్వులు",
                    category = "Oil Seeds",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1597848212624-a19eb35e2651?q=80&w=400",
                    lastUpdated = "Today, 09:40 AM"
                ),
                // TS
                MarketCropEntity(
                    cropName = "Sunflower Seeds",
                    market = "Karimnagar APMC",
                    district = "Karimnagar",
                    todayPrice = 5850.0,
                    yesterdayPrice = 5900.0,
                    highestPrice = 6200.0,
                    lowestPrice = 5200.0,
                    cropNameTe = "పొద్దుతిరుగుడు విత్తనాలు",
                    category = "Oil Seeds",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1597848212624-a19eb35e2651?q=80&w=400",
                    lastUpdated = "Today, 09:15 AM"
                ),
                MarketCropEntity(
                    cropName = "Groundnut (Peanuts)",
                    market = "Mahabubnagar APMC",
                    district = "Mahabubnagar",
                    todayPrice = 7250.0,
                    yesterdayPrice = 7100.0,
                    highestPrice = 7600.0,
                    lowestPrice = 6400.0,
                    cropNameTe = "వేరుశనగ",
                    category = "Oil Seeds",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1568254183919-78a4f43a2877?q=80&w=400",
                    lastUpdated = "Today, 10:10 AM"
                ),

                // ==================== COTTON ====================
                // AP
                MarketCropEntity(
                    cropName = "Cotton (Medium Staple)",
                    market = "Guntur APMC",
                    district = "Guntur",
                    todayPrice = 7100.0,
                    yesterdayPrice = 7050.0,
                    highestPrice = 7400.0,
                    lowestPrice = 6500.0,
                    cropNameTe = "పత్తి",
                    category = "Cotton",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1594489428504-5c0c480a15fd?q=80&w=400",
                    lastUpdated = "Today, 09:10 AM"
                ),
                MarketCropEntity(
                    cropName = "Cotton (Fine)",
                    market = "Kurnool APMC",
                    district = "Kurnool",
                    todayPrice = 7350.0,
                    yesterdayPrice = 7300.0,
                    highestPrice = 7650.0,
                    lowestPrice = 6700.0,
                    cropNameTe = "సన్న పత్తి",
                    category = "Cotton",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1594489428504-5c0c480a15fd?q=80&w=400",
                    lastUpdated = "Today, 09:20 AM"
                ),
                // TS
                MarketCropEntity(
                    cropName = "Cotton (Long Staple)",
                    market = "Adilabad APMC",
                    district = "Adilabad",
                    todayPrice = 7450.0,
                    yesterdayPrice = 7380.0,
                    highestPrice = 7800.0,
                    lowestPrice = 6800.0,
                    cropNameTe = "పత్తి (పొడుగు పింజ)",
                    category = "Cotton",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1594489428504-5c0c480a15fd?q=80&w=400",
                    lastUpdated = "Today, 09:30 AM"
                ),
                MarketCropEntity(
                    cropName = "Cotton (Medium Staple)",
                    market = "Warangal APMC",
                    district = "Warangal",
                    todayPrice = 7200.0,
                    yesterdayPrice = 7150.0,
                    highestPrice = 7500.0,
                    lowestPrice = 6600.0,
                    cropNameTe = "పత్తి",
                    category = "Cotton",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1594489428504-5c0c480a15fd?q=80&w=400",
                    lastUpdated = "Today, 09:40 AM"
                ),
                // All India
                MarketCropEntity(
                    cropName = "Cotton (Kalyan)",
                    market = "Rajkot APMC",
                    district = "Rajkot (GJ)",
                    todayPrice = 7150.0,
                    yesterdayPrice = 7200.0,
                    highestPrice = 7500.0,
                    lowestPrice = 6600.0,
                    cropNameTe = "కల్యాణ్ పత్తి",
                    category = "Cotton",
                    state = "All Indian States",
                    imageUrl = "https://images.unsplash.com/photo-1594489428504-5c0c480a15fd?q=80&w=400",
                    lastUpdated = "Today, 10:45 AM"
                ),

                // ==================== PULSES ====================
                // AP
                MarketCropEntity(
                    cropName = "Bengal Gram (Chana)",
                    market = "Kurnool APMC",
                    district = "Kurnool",
                    todayPrice = 6200.0,
                    yesterdayPrice = 6150.0,
                    highestPrice = 6500.0,
                    lowestPrice = 5700.0,
                    cropNameTe = "శనగలు",
                    category = "Pulses",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1515543904379-3d757afe72e2?q=80&w=400",
                    lastUpdated = "Today, 10:10 AM"
                ),
                MarketCropEntity(
                    cropName = "Black Gram (Urad)",
                    market = "Guntur APMC",
                    district = "Guntur",
                    todayPrice = 8800.0,
                    yesterdayPrice = 8700.0,
                    highestPrice = 9200.0,
                    lowestPrice = 8200.0,
                    cropNameTe = "మినుములు",
                    category = "Pulses",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1515543904379-3d757afe72e2?q=80&w=400",
                    lastUpdated = "Today, 09:45 AM"
                ),
                MarketCropEntity(
                    cropName = "Green Gram (Moong)",
                    market = "Nellore Mandi",
                    district = "Nellore",
                    todayPrice = 7800.0,
                    yesterdayPrice = 7850.0,
                    highestPrice = 8200.0,
                    lowestPrice = 7400.0,
                    cropNameTe = "పెసలు",
                    category = "Pulses",
                    state = "Andhra Pradesh",
                    imageUrl = "https://images.unsplash.com/photo-1515543904379-3d757afe72e2?q=80&w=400",
                    lastUpdated = "Today, 09:55 AM"
                ),
                // TS
                MarketCropEntity(
                    cropName = "Red Gram (Toor)",
                    market = "Tandur APMC",
                    district = "Vikarabad",
                    todayPrice = 11200.0,
                    yesterdayPrice = 11000.0,
                    highestPrice = 12000.0,
                    lowestPrice = 9800.0,
                    cropNameTe = "కందులు",
                    category = "Pulses",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1546833999-b9f581a1996d?q=80&w=400",
                    lastUpdated = "Today, 09:50 AM"
                ),
                MarketCropEntity(
                    cropName = "Green Gram (Moong)",
                    market = "Khammam Mandi",
                    district = "Khammam",
                    todayPrice = 7900.0,
                    yesterdayPrice = 7800.0,
                    highestPrice = 8300.0,
                    lowestPrice = 7500.0,
                    cropNameTe = "పెసలు",
                    category = "Pulses",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1515543904379-3d757afe72e2?q=80&w=400",
                    lastUpdated = "Today, 10:05 AM"
                ),
                MarketCropEntity(
                    cropName = "Black Gram (Urad)",
                    market = "Warangal APMC",
                    district = "Warangal",
                    todayPrice = 8950.0,
                    yesterdayPrice = 8900.0,
                    highestPrice = 9350.0,
                    lowestPrice = 8400.0,
                    cropNameTe = "మినుములు",
                    category = "Pulses",
                    state = "Telangana",
                    imageUrl = "https://images.unsplash.com/photo-1515543904379-3d757afe72e2?q=80&w=400",
                    lastUpdated = "Today, 10:15 AM"
                )
            )
            dao.insertMarketCrops(initialCrops)
        }
    }

    // --- NEWS ---
    val allNews: Flow<List<NewsEntity>> = dao.getAllNews().catch { emit(emptyList()) }

    suspend fun prepopulateNews() {
        val currentNews = dao.getAllNews().first()
        if (currentNews.size < 16) {
            dao.deleteAllNews() // clear existing if incomplete to populate all categories
            val initialNews = listOf(
                NewsEntity(
                    title = "AP Agriculture: Rythu Bharosa Funds to Be Credited This Week",
                    titleTe = "ఏపీ వ్యవసాయం: రైతు భరోసా నిధులు ఈ వారమే జమ",
                    content = "The Andhra Pradesh government announced that the Rythu Bharosa financial aid of Rs. 13,500 will be directly credited to eligible farmers' bank accounts this week. Around 50 lakh farmer families across the state will benefit from this scheme. Ensure your bank account is linked with Aadhaar.",
                    category = "AP Agriculture News",
                    source = "AP Agri Dept",
                    imageUrl = "https://images.unsplash.com/photo-1592982537447-7440770cbfc9?q=80&w=600"
                ),
                NewsEntity(
                    title = "Telangana: Free Power to Farmers Under New Irrigation Guidelines",
                    titleTe = "తెలంగాణ: కొత్త నీటిపారుదల నిబంధనల ప్రకారం రైతులకు ఉచిత విద్యుత్",
                    content = "The Telangana State Electricity Regulatory Commission (TSERC) has issued new guidelines ensuring uninterrupted 24/7 free electricity supply to agriculture pump sets. Farmers are requested to register their borewell capacity on the portal.",
                    category = "Telangana Agriculture News",
                    source = "Telangana Power",
                    imageUrl = "https://images.unsplash.com/photo-1500937386664-56d1dfef3854?q=80&w=600"
                ),
                NewsEntity(
                    title = "National MSP Hiked for Kharif Crops to Boost Farm Incomes",
                    titleTe = "రైతుల ఆదాయాన్ని పెంచేందుకు ఖరీఫ్ పంటల జాతీయ మద్దతు ధర పెంపు",
                    content = "The Cabinet Committee on Economic Affairs has approved an increase in the Minimum Support Prices (MSP) for all mandated Kharif crops, encouraging farmers to diversify and cultivate pulses and oilseeds.",
                    category = "India Agriculture News",
                    source = "Ministry of Agriculture India",
                    imageUrl = "https://images.unsplash.com/photo-1530595467537-0b5996c41f2d?q=80&w=600"
                ),
                NewsEntity(
                    title = "Organic Farming: Neem Seed Kernel Extract Proves Highly Effective Against Pests",
                    titleTe = "సేంద్రీయ వ్యవసాయం: తెగుళ్ళపై వేప గింజల కషాయం అద్భుత ప్రభావం",
                    content = "Agricultural scientists confirmed that spraying 5% Neem Seed Kernel Extract (NSKE) is extremely useful for controlling sucking pests like whiteflies and jassids on cotton and vegetable crops, preserving beneficial field insects.",
                    category = "Organic Farming",
                    source = "ICAR India",
                    imageUrl = "https://images.unsplash.com/photo-1595855759920-86582396756a?q=80&w=600"
                ),
                NewsEntity(
                    title = "Subhash Palekar Zero Budget Natural Farming Techniques Gain Popularity",
                    titleTe = "సుభాష్ పాలేకర్ జీరో బడ్జెట్ ప్రకృతి వ్యవసాయ పద్ధతులకు పెరుగుతున్న ఆదరణ",
                    content = "Jeevamrutha and Beejamrutha soil inoculants show extraordinary capacity in cultivating high yields with absolute zero chemical inputs. Farmers report a 60% reduction in cultivation costs.",
                    category = "Natural Farming",
                    source = "Natural Farming Board",
                    imageUrl = "https://images.unsplash.com/photo-1464226184884-fa280b87c399?q=80&w=600"
                ),
                NewsEntity(
                    title = "Chittoor Farmer Generates Record Revenue with Precision Chilli Farming",
                    titleTe = "ఖచ్చితమైన మిరప వ్యవసాయంతో రికార్డు ఆదాయాన్ని సాధించిన చిత్తూరు రైతు",
                    content = "A progressive farmer in Chittoor district successfully harvested 45 quintals of high-quality red chilli per acre using drip irrigation and mulching sheets, inspiring neighboring young farmers.",
                    category = "Farmer Success Stories",
                    source = "Krishi Vigyan Kendra",
                    imageUrl = "https://images.unsplash.com/photo-1593113598332-cd288d649433?q=80&w=600"
                ),
                NewsEntity(
                    title = "New Technologies: Drones Deployed for Efficient Urea Spraying in Eluru",
                    titleTe = "నూతన సాంకేతికతలు: ఏలూరులో డ్రోన్ల ద్వారా సమర్థవంతమైన యూరియా పిచికారీ",
                    content = "Agricultural drones were tested successfully for spraying nano-urea over paddy fields in Eluru district. This technology cuts fertilizer wastage by 40% and completes spraying on one acre of field in less than 7 minutes.",
                    category = "New Technologies",
                    source = "AgriTech Lab",
                    imageUrl = "https://images.unsplash.com/photo-1508962914676-134849a727f0?q=80&w=600"
                ),
                NewsEntity(
                    title = "New Solar Subsidy Program Approved for Agriculture Pumpsets",
                    titleTe = "వ్యవసాయ పంప్‌సెట్ల కోసం కొత్త సోలార్ సబ్సిడీ కార్యక్రమానికి ఆమోదం",
                    content = "Government launches an 80% subsidized solar water pump installation scheme for small and marginal landholders, replacing reliance on coal grid power and reducing utility bills.",
                    category = "Government Announcements",
                    source = "Central Ministry",
                    imageUrl = "https://images.unsplash.com/photo-1509391366360-2e959784a276?q=80&w=600"
                ),
                NewsEntity(
                    title = "Micro-Drip Line Irrigation Saves Significant Ground Water Levels",
                    titleTe = "మైక్రో-డ్రిప్ లైన్ నీటిపారుదల ద్వారా భూగర్భ జలాల ఆదా",
                    content = "A comprehensive study on localized water tables shows that replacing flood irrigation with micro-drip networks prevents soil salinity and increases crop health with target delivery.",
                    category = "Irrigation",
                    source = "Hydrology Dept",
                    imageUrl = "https://images.unsplash.com/photo-1563514223300-b3b3d373f2ec?q=80&w=600"
                ),
                NewsEntity(
                    title = "Southwest Monsoon Expected to Be Normal to Excess This Month",
                    titleTe = "ఈ నెలలో నైరుతి రుతుపవనాలు సాధారణం కంటే ఎక్కువగా ఉండే అవకాశం",
                    content = "Meteorological department predicts widespread rainfall across southern peninsula during this fortnight, suggesting farmers clear secondary drainage channels to prevent root water-logging.",
                    category = "Weather",
                    source = "Indian Met Dept",
                    imageUrl = "https://images.unsplash.com/photo-1534274988757-a28bf1a57c17?q=80&w=600"
                ),
                NewsEntity(
                    title = "Whitefly Control Strategy for Cotton Crops Released",
                    titleTe = "పత్తి పంటలలో తెల్లదోమ నివారణ వ్యూహాల విడుదల",
                    content = "Scientists recommend installing yellow sticky traps (25 per acre) combined with neem oil sprays to effectively suppress initial whitefly vectors before they spread leaf curl virus.",
                    category = "Pest Control",
                    source = "Pest Control Board",
                    imageUrl = "https://images.unsplash.com/photo-1473081556163-2a17de81fc97?q=80&w=600"
                ),
                NewsEntity(
                    title = "Bio-Fertilizer Inoculants Boost Soil Nitrogen Availability",
                    titleTe = "మట్టి నత్రజని లభ్యతను పెంచే జీవ ఎరువుల వాడకం",
                    content = "Applying Azotobacter and Rhizobium cultures increases atmospheric nitrogen fixation by 20-30 kg per hectare, lowering chemical urea expenditure and boosting soil texture.",
                    category = "Fertilizers",
                    source = "National Fertilizer Lab",
                    imageUrl = "https://images.unsplash.com/photo-1592982537447-7440770cbfc9?q=80&w=600"
                ),
                NewsEntity(
                    title = "Dairy Farming: Milk Yield Boosted by 15% with Balanced Azolla Feed",
                    titleTe = "పాడి పరిశ్రమ: సమతుల్య అజొల్లా దాణా ద్వారా 15% పెరిగిన పాలు",
                    content = "Feeding milch buffaloes with 2kg of fresh Azolla daily along with regular concentrate feed is reported to improve average daily milk production by 15% and fat content by 0.5% due to high protein composition.",
                    category = "Dairy",
                    source = "NDDB India",
                    imageUrl = "https://images.unsplash.com/photo-1570042225831-d98fa7577f1e?q=80&w=600"
                ),
                NewsEntity(
                    title = "Aerator Management Practices in Vannamei Shrimp Farms",
                    titleTe = "వనామీ రొయ్యల చెరువులలో ఎరేటర్ నిర్వహణ పద్ధతులు",
                    content = "Maintaining dissolved oxygen above 4.0 ppm using paddle wheel aerators during midnight hours ensures optimum feed consumption and prevents stress mortality in intensive shrimp ponds.",
                    category = "Fisheries",
                    source = "Fisheries Department",
                    imageUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?q=80&w=600"
                ),
                NewsEntity(
                    title = "Biosecurity Measures Implemented Against Avian Influenza Viruses",
                    titleTe = "బర్డ్ ఫ్లూ వైరస్‌లకు వ్యతిరేకంగా బయోసెక్యూరిటీ చర్యల అమలు",
                    content = "Poultry farmers are advised to restrict vehicle entry, spray disinfectant footbaths at shed doors, and provide vitamin C supplements to build flock immune systems.",
                    category = "Poultry",
                    source = "Poultry Association",
                    imageUrl = "https://images.unsplash.com/photo-1548550023-2bdb3c5beed7?q=80&w=600"
                ),
                NewsEntity(
                    title = "Polyhouse Rose Floriculture Offers Stable Year-Round Incomes",
                    titleTe = "పోలీహౌస్ రోజ్ ఫ్లోరికల్చర్ ద్వారా సంవత్సరం పొడవునా స్థిరమైన ఆదాయం",
                    content = "Cultivating Dutch roses under controlled polyhouse structures protects delicate petals from direct wind and pest damage, ensuring premium quality exports for overseas markets.",
                    category = "Horticulture",
                    source = "Horticulture Board",
                    imageUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=600"
                )
            )
            dao.insertNews(initialNews)
        }
    }

    suspend fun addNews(news: NewsEntity) {
        dao.insertSingleNews(news)
    }

    // --- NOTIFICATIONS ---
    val allNotifications: Flow<List<NotificationAlertEntity>> = dao.getAllNotifications().catch { emit(emptyList()) }

    suspend fun addNotification(notification: NotificationAlertEntity) {
        dao.insertNotification(notification)
    }

    suspend fun markNotificationRead(id: Int) {
        dao.markNotificationAsRead(id)
    }

    suspend fun deleteNotification(id: Int) {
        dao.deleteNotificationById(id)
    }

    // --- GEMINI API CALLS ---

    suspend fun analyzeCropDisease(bitmap: Bitmap): String {
        val systemInstruction = """
            You are an expert plant pathologist and agricultural consultant. Analyze the plant image provided.
            Analyze the disease of the crop and output ONLY a structured text in the following exact format, with no markdown code blocks, asterisks, or extra symbols. Fill in details accurately:
            Crop Name: [Name of crop]
            Disease Name: [Name of disease]
            Confidence Score: [A score from 0 to 100 representing confidence]
            Cause: [What causes this disease]
            Symptoms: [The visual signs seen on the plant]
            Treatment: [Short summary of the cure]
            Organic Solution: [Natural treatment, bio-pesticides, neem oil, etc.]
            Chemical Solution: [Chemical fungicides or pesticides with specific names if applicable]
            Preventive Measures: [How to prevent this in future crops]
            Nearby Agriculture Office: [Standard advice for contacting local Krishi Vigyan Kendra or Agri officer]
        """.trimIndent()

        val prompt = "Identify the crop disease present in this image and provide treatment options, following the exact structure specified."
        return GeminiHelper.generateMultimodalResponse(prompt, bitmap, systemInstruction)
    }

    suspend fun getCropRecommendation(
        soilType: String,
        state: String,
        district: String,
        season: String,
        waterAvailability: String,
        farmSize: String
    ): String {
        val systemInstruction = """
            You are an agronomy advisor. Provide a crop recommendation.
            Format the output ONLY with these clean headings:
            Best Crop: [Name of recommended crop]
            Expected Yield: [Average yield per acre]
            Profit Estimate: [Estimated profit in currency or % margin]
            Growing Duration: [Number of days/months to harvest]
            
            Provide a detailed 1-2 paragraph description on planting instructions, spacing, and critical guidelines.
        """.trimIndent()

        val prompt = """
            Recommend the best crop to grow under these conditions:
            Soil Type: $soilType
            Location: $district, $state
            Season: $season
            Water Availability: $waterAvailability
            Farm Size: $farmSize
        """.trimIndent()

        return GeminiHelper.generateResponse(prompt, systemInstruction)
    }

    suspend fun getFertilizerRecommendation(
        crop: String,
        disease: String,
        soil: String,
        growthStage: String
    ): String {
        val systemInstruction = """
            You are a crop fertilizer expert. Provide professional suggestions.
            Format the output ONLY with these clean headings:
            Recommended Fertilizer: [Name of organic or synthetic fertilizers]
            Quantity: [Amount required per acre or per plant]
            Application Method: [How to apply e.g. broad-casting, foliar spray, fertigation]
            Organic Alternative: [Natural alternative e.g. vermicompost, bone meal]
            
            Include a short 3-4 sentence tip on safe application, wearing masks, and timing.
        """.trimIndent()

        val prompt = """
            Suggest the best fertilizer and application details for:
            Crop: $crop
            Disease/Deficiency: $disease
            Soil Type: $soil
            Growth Stage: $growthStage
        """.trimIndent()

        return GeminiHelper.generateResponse(prompt, systemInstruction)
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = java.io.ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.NO_WRAP)
    }

    suspend fun chatWithAI(message: String, language: String, chatHistory: List<Content>, image: Bitmap? = null): String {
        val systemInstruction = """
            You are Agri AI, a helpful and knowledgeable agricultural chatbot assistant.
            You help farmers solve issues regarding crop disease, pest control, weather guidelines, market prices, organic farming, and government schemes.
            The user wants the response in $language. If language is Telugu, write in Telugu script. If language is Hindi, write in Hindi script (Devanagari). Otherwise write in English.
            Keep your responses concise, highly practical, and tailored to Indian farming contexts.
        """.trimIndent()

        // Combine history with latest content
        val contents = chatHistory.toMutableList()
        
        val parts = mutableListOf<Part>()
        if (message.isNotBlank()) {
            parts.add(Part(text = message))
        } else if (image != null) {
            parts.add(Part(text = "Analyze this crop leaf / agricultural image for disease detection, fertilizer recommendation, organic farming tips, or crop planning guidelines as applicable."))
        }
        
        if (image != null) {
            parts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = image.toBase64())))
        }
        
        contents.add(Content(parts = parts, role = "user"))

        val request = GenerateContentRequest(
            contents = contents,
            systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
        )

        val apiKey = try { BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String ?: "" } catch (e: Exception) { "" }
        if (apiKey.isEmpty()) {
            return "Error: Gemini API Key is missing. Live data connection not configured."
        }

        return try {
            val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I couldn't generate a response. Please try again."
        } catch (e: Exception) {
            "Error: ${e.localizedMessage ?: e.message}"
        }
    }

    suspend fun getCropYieldPrediction(
        crop: String,
        area: String,
        soil: String,
        plantingDate: String,
        state: String,
        district: String
    ): String {
        val systemInstruction = """
            You are an expert AI agricultural data scientist and predictive agronomist.
            Predict crop yield based on the crop, soil, acreage, planting date, and location.
            Format the output ONLY with these clean, bold headings and follow with clear details:
            
            Predicted Total Yield: [E.g., 24.5 Tons or Quintals]
            Yield Per Acre: [E.g., 8.2 Quintals/Acre]
            Confidence Level: [E.g., 88% (High)]
            Historical Regional Benchmark: [Comparison with typical historic yield in that district]
            Factor Analysis: [Explain how soil, planting window, and region affect this estimate]
            Yield Optimization Hacks: [3 highly actionable bulleted tips to increase this yield by 15-20%]
        """.trimIndent()

        val prompt = """
            Run yield prediction analysis for:
            Crop: $crop
            Farm Area: $area
            Soil Type: $soil
            Planting Date: $plantingDate
            Location: $district, $state
        """.trimIndent()

        return GeminiHelper.generateResponse(prompt, systemInstruction)
    }

    // --- TRANSACTIONS ---
    fun getAllTransactions(email: String): Flow<List<TransactionEntity>> {
        return dao.getAllTransactionsForUser(email)
    }

    suspend fun addTransaction(transaction: TransactionEntity) {
        dao.insertTransaction(transaction)
    }

    // --- FARMING TIPS ---
    val latestFarmingTip: Flow<FarmingTipEntity?> = dao.getLatestFarmingTip().catch { emit(null) }

    suspend fun getLatestFarmingTipSync(): FarmingTipEntity? {
        return dao.getLatestFarmingTipSync()
    }

    suspend fun saveFarmingTip(tip: FarmingTipEntity) {
        dao.insertFarmingTip(tip)
    }

    // --- LIVE WEATHER SYSTEM (OPENWEATHER API) ---
    private fun getOpenWeatherApiKey(): String {
        val key = BuildConfig.OPENWEATHER_API_KEY.trim()
        if (key.isBlank() || key == "MY_OPENWEATHER_API_KEY" || key == "YOUR_API_KEY" || key.contains("PLACEHOLDER") || key == "MY_NEW_API_KEY_DEFAULT_VALUE") {
            return ""
        }
        return key
    }

    suspend fun getLiveWeather(query: String, lang: String = "en"): WeatherInfo {
        val apiKey = getOpenWeatherApiKey()
        if (apiKey.isBlank()) {
            throw Exception("OpenWeather API key is required.")
        }

        try {
            val currentResponse = WeatherRetrofitClient.openWeatherService.getCurrentWeatherByQuery(
                query = query,
                apiKey = apiKey,
                units = "metric",
                lang = if (lang == "te") "te" else "en"
            )

            val lat = currentResponse.coord?.lat ?: 16.7107
            val lon = currentResponse.coord?.lon ?: 81.1042

            val forecastResponse = try {
                WeatherRetrofitClient.openWeatherService.getForecastByCoords(
                    lat = lat,
                    lon = lon,
                    apiKey = apiKey,
                    units = "metric",
                    lang = if (lang == "te") "te" else "en"
                )
            } catch (e: Exception) {
                null
            }

            return parseOpenWeatherToWeatherInfo(
                current = currentResponse,
                forecast = forecastResponse,
                customLocationName = currentResponse.name ?: query,
                lang = lang
            )
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 401) {
                throw Exception("OpenWeather API key is required.")
            } else {
                throw Exception("Unable to fetch live weather. Please try again.")
            }
        } catch (e: Exception) {
            if (e.message?.contains("API key") == true) {
                throw e
            }
            throw Exception("Unable to fetch live weather. Please try again.")
        }
    }

    suspend fun getLiveWeatherForGps(lat: Double, lon: Double, context: android.content.Context, lang: String = "en"): WeatherInfo {
        val apiKey = getOpenWeatherApiKey()
        if (apiKey.isBlank()) {
            throw Exception("OpenWeather API key is required.")
        }

        try {
            val currentResponse = WeatherRetrofitClient.openWeatherService.getCurrentWeatherByCoords(
                lat = lat,
                lon = lon,
                apiKey = apiKey,
                units = "metric",
                lang = if (lang == "te") "te" else "en"
            )

            var locName = currentResponse.name ?: "Current Location"
            var districtName = currentResponse.sys?.country ?: "India"
            var stateName = "India"

            try {
                val geoList = WeatherRetrofitClient.openWeatherService.reverseGeocode(lat, lon, 1, apiKey)
                if (geoList.isNotEmpty()) {
                    val geo = geoList.first()
                    if (!geo.name.isNullOrBlank()) locName = geo.name
                    if (!geo.state.isNullOrBlank()) stateName = geo.state
                }
            } catch (e: Exception) {
                // Ignore geocode error
            }

            val forecastResponse = try {
                WeatherRetrofitClient.openWeatherService.getForecastByCoords(
                    lat = lat,
                    lon = lon,
                    apiKey = apiKey,
                    units = "metric",
                    lang = if (lang == "te") "te" else "en"
                )
            } catch (e: Exception) {
                null
            }

            return parseOpenWeatherToWeatherInfo(
                current = currentResponse,
                forecast = forecastResponse,
                customLocationName = locName,
                district = districtName,
                state = stateName,
                lang = lang
            )
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 401) {
                throw Exception("OpenWeather API key is required.")
            } else {
                throw Exception("Unable to fetch live weather. Please try again.")
            }
        } catch (e: Exception) {
            if (e.message?.contains("API key") == true) {
                throw e
            }
            throw Exception("Unable to fetch live weather. Please try again.")
        }
    }

    private fun parseOpenWeatherToWeatherInfo(
        current: OpenWeatherCurrentResponse,
        forecast: OpenWeatherForecastResponse?,
        customLocationName: String,
        district: String = "Local",
        state: String = "India",
        lang: String = "en"
    ): WeatherInfo {
        val mainData = current.main
        val temp = mainData?.temp ?: 0.0
        val feelsLike = mainData?.feelsLike ?: temp
        val humidity = mainData?.humidity ?: 0
        val pressure = mainData?.pressure ?: 1013
        val visibilityKm = (current.visibility ?: 10000.0) / 1000.0

        val windSpeedMps = current.wind?.speed ?: 0.0
        val windSpeedKmh = (windSpeedMps * 3.6 * 10).toInt() / 10.0
        val windDeg = current.wind?.deg ?: 0
        val windDirection = convertDegreesToCardinalDirection(windDeg)

        val weatherItem = current.weather?.firstOrNull()
        val conditionRaw = weatherItem?.main ?: "Clear"
        val descriptionRaw = weatherItem?.description ?: conditionRaw
        val conditionMapped = mapOpenWeatherConditionToDisplay(conditionRaw, descriptionRaw, lang)

        val rainMm = current.rain?.rain1h ?: current.rain?.rain3h ?: 0.0
        val rainProb = if (rainMm > 0) ((rainMm * 20).coerceIn(20.0, 100.0)).toInt() else (forecast?.list?.firstOrNull()?.pop?.let { (it * 100).toInt() } ?: 0)

        val clouds = current.clouds?.all ?: 0

        val timeSdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
        val sunriseTime = current.sys?.sunrise?.let { timeSdf.format(java.util.Date(it * 1000L)) } ?: "06:00 AM"
        val sunsetTime = current.sys?.sunset?.let { timeSdf.format(java.util.Date(it * 1000L)) } ?: "06:30 PM"
        val lastUpdatedTime = current.dt?.let { timeSdf.format(java.util.Date(it * 1000L)) } ?: timeSdf.format(java.util.Date())

        // Hourly forecast items
        val hourlyList = mutableListOf<HourlyForecast>()
        val hourSdf = java.text.SimpleDateFormat("hh a", java.util.Locale.US)
        forecast?.list?.take(12)?.forEachIndexed { index, item ->
            val displayTime = if (index == 0) "Now" else item.dt?.let { hourSdf.format(java.util.Date(it * 1000L)) } ?: "00 PM"
            val itemTemp = item.main?.temp ?: temp
            val itemWeather = item.weather?.firstOrNull()
            val itemCondition = mapOpenWeatherConditionToDisplay(itemWeather?.main ?: "Clear", itemWeather?.description ?: "", lang)
            val itemPop = ((item.pop ?: 0.0) * 100).toInt()
            hourlyList.add(HourlyForecast(time = displayTime, temperature = itemTemp, condition = itemCondition, rainChance = itemPop))
        }

        // Daily forecast items
        val dailyList = mutableListOf<DailyForecast>()
        val daySdf = java.text.SimpleDateFormat("EEEE", java.util.Locale.US)
        val dateSdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        if (forecast?.list != null) {
            val grouped = forecast.list.groupBy { 
                it.dt?.let { timestamp -> dateSdf.format(java.util.Date(timestamp * 1000L)) } ?: ""
            }
            grouped.entries.filter { it.key.isNotBlank() }.take(7).forEachIndexed { idx, entry ->
                val dayLabel = if (idx == 0) (if (lang == "te") "నేడు" else "Today") else if (idx == 1) (if (lang == "te") "రేపు" else "Tomorrow") else {
                    entry.value.firstOrNull()?.dt?.let { daySdf.format(java.util.Date(it * 1000L)) } ?: entry.key
                }
                val minT = entry.value.mapNotNull { it.main?.tempMin }.minOrNull() ?: temp
                val maxT = entry.value.mapNotNull { it.main?.tempMax }.maxOrNull() ?: temp
                val maxPop = entry.value.mapNotNull { it.pop }.maxOrNull() ?: 0.0
                val dayWeather = entry.value.firstOrNull()?.weather?.firstOrNull()
                val dayCond = mapOpenWeatherConditionToDisplay(dayWeather?.main ?: "Clear", dayWeather?.description ?: "", lang)

                dailyList.add(
                    DailyForecast(
                        day = dayLabel,
                        condition = dayCond,
                        minTemp = minT,
                        maxTemp = maxT,
                        rainChance = (maxPop * 100).toInt()
                    )
                )
            }
        }

        val advisories = generateDynamicFarmingAdvisories(
            temp = temp,
            rainProb = rainProb,
            humidity = humidity,
            windKmh = windSpeedKmh,
            condition = conditionRaw,
            lang = lang
        )

        return WeatherInfo(
            locationName = customLocationName,
            district = district,
            state = state,
            temperature = temp,
            feelsLike = feelsLike,
            condition = conditionMapped,
            rainProbability = rainProb,
            humidity = humidity,
            windSpeed = windSpeedKmh,
            windDirection = windDirection,
            uvIndex = if (clouds < 30 && temp > 30) 8 else if (clouds < 60) 5 else 3,
            airPressure = pressure,
            visibility = visibilityKm,
            sunrise = sunriseTime,
            sunset = sunsetTime,
            cloudCoverage = clouds,
            aqi = null,
            lastUpdated = lastUpdatedTime,
            hourlyForecast = hourlyList,
            dailyForecast = dailyList,
            farmingAdvisory = advisories
        )
    }

    private fun convertDegreesToCardinalDirection(deg: Int): String {
        val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val index = ((deg + 22.5) / 45.0).toInt() % 8
        return directions[index]
    }

    private fun mapOpenWeatherConditionToDisplay(main: String, description: String, lang: String): String {
        val normalized = main.trim().lowercase()
        if (lang == "te") {
            return when {
                normalized.contains("rain") || normalized.contains("drizzle") -> "వర్షం"
                normalized.contains("thunderstorm") -> "ఉరుములతో కూడిన వర్షం"
                normalized.contains("cloud") -> "మబ్బులు"
                normalized.contains("clear") || normalized.contains("sun") -> "నిర్మలం / ఎండ"
                normalized.contains("fog") || normalized.contains("mist") || normalized.contains("haze") -> "పొగమంచు"
                normalized.contains("wind") -> "ఈదురుగాలులు"
                else -> if (description.isNotBlank()) description else "నిర్మలం"
            }
        } else {
            return when {
                normalized.contains("rain") || normalized.contains("drizzle") -> "Rain"
                normalized.contains("thunderstorm") -> "Thunderstorm"
                normalized.contains("cloud") -> "Cloudy"
                normalized.contains("clear") || normalized.contains("sun") -> "Sunny"
                normalized.contains("fog") || normalized.contains("mist") || normalized.contains("haze") -> "Fog"
                normalized.contains("wind") -> "Windy"
                else -> if (main.isNotBlank()) main else "Sunny"
            }
        }
    }

    private fun generateDynamicFarmingAdvisories(
        temp: Double,
        rainProb: Int,
        humidity: Int,
        windKmh: Double,
        condition: String,
        lang: String
    ): List<String> {
        val list = mutableListOf<String>()
        if (lang == "te") {
            if (rainProb > 50 || condition.lowercase().contains("rain")) {
                list.add("వర్ష సూచన (${rainProb}%): పురుగుమందుల పిచికారీని మరియు ఎరువుల వాడకాన్ని వాయిదా వేయండి.")
                list.add("చేనులో నిల్వ నీరు చేరకుండా పారుదల కాలువలను సిద్ధం చేయండి.")
            } else {
                list.add("అనుకూల వాతావరణం: పిచికారీ మరియు పొలం పనులకు అనుకూలమైన సమయం.")
            }
            if (temp > 35) {
                list.add("అధిక ఉష్ణోగ్రత (${temp.toInt()}°C): పైరుకు ఉదయం లేదా సాయంత్రం వేళల్లో నీటి తడులు ఇవ్వండి.")
            }
            if (humidity > 75) {
                list.add("అధిక గాలి తేమ (${humidity}%): వరి మరియు పత్తి పంటల్లో శిలీంధ్ర తెగుళ్లను గమనించండి.")
            }
            if (windKmh > 20) {
                list.add("ఈదురుగాలులు (${windKmh.toInt()} km/h): అరటి మరియు చెరకు పంటలకు కర్రల మద్దతు ఇవ్వండి.")
            }
        } else {
            if (rainProb > 50 || condition.lowercase().contains("rain")) {
                list.add("Rain forecast (${rainProb}%): Postpone pesticide spraying and fertilizer application.")
                list.add("Ensure field drainage channels are clear to prevent waterlogging.")
            } else {
                list.add("Favorable weather: Suitable for routine farm operations and field spraying.")
            }
            if (temp > 35) {
                list.add("High temperature (${temp.toInt()}°C): Provide light irrigation during early morning or evening hours.")
            }
            if (humidity > 75) {
                list.add("High humidity (${humidity}%): Inspect paddy and cotton crops for pest and fungal infections.")
            }
            if (windKmh > 20) {
                list.add("High wind speed (${windKmh.toInt()} km/h): Provide stake support for tall crops like banana and sugarcane.")
            }
        }
        return list
    }

    private fun mapWmoCodeToCondition(code: Int): String {
        return when (code) {
            0 -> "Sunny"
            1, 2, 3 -> "Cloudy"
            45, 48 -> "Fog"
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> "Rain"
            95, 96, 99 -> "Thunderstorm"
            else -> "Cloudy"
        }
    }

    private fun mapDegreesToDirection(degrees: Double): String {
        val directions = listOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        val index = ((degrees + 11.25) / 22.5).toInt() % 16
        return directions[index]
    }

    private fun generateFarmingAdvisories(
        temp: Double,
        rainChance: Int,
        humidity: Int,
        windSpeed: Double,
        condition: String
    ): List<String> {
        val list = mutableListOf<String>()
        if (rainChance > 50) {
            list.add("💦 Irrigation: Hold off on general irrigation as a high probability of rain ($rainChance%) is forecast.")
            list.add("🐛 Pesticides: Do not spray today. Rainfall will wash away chemical applications, reducing effectiveness and causing runoff.")
            list.add("🌾 Harvesting: If grains or pulses are ready for harvest, complete them immediately and shelter the produce in dry warehouses.")
        } else {
            if (temp > 35) {
                list.add("💧 Irrigation: High daytime temperatures of ${temp.toInt()}°C. Provide light and frequent irrigation in early morning or late evening to prevent crop wilting.")
                list.add("🌱 Fertilizer: Avoid urea top-dressing during high midday heat. Apply early in the morning when soil is cool and moist.")
            } else {
                list.add("💧 Irrigation: Normal irrigation schedules can be followed today based on crop requirements.")
                list.add("🌱 Fertilizer: Suitable conditions for fertilizer application. Ensure soil has optimal moisture.")
            }
            if (windSpeed > 20) {
                list.add("💨 Wind Alert: High winds ($windSpeed km/h) expected. Postpone foliar spraying of pesticides or weedicides to prevent spray drift.")
                list.add("🎋 Crop Support: Provide proper staking or support for tall crops (like banana or sugarcane) to avoid lodging from strong winds.")
            } else {
                list.add("🐛 Pesticides: Moderate wind speed of ${windSpeed.toInt()} km/h is highly suitable for pesticide or foliar nutrient sprays.")
            }
        }
        
        if (humidity > 85) {
            list.add("🍄 Disease Warning: High relative humidity ($humidity%) increases the risk of fungal leaf spot, blast, and blight. Monitor crops closely.")
        }
        
        if (condition == "Thunderstorm") {
            list.add("⚡ Weather Warning: Thunderstorms expected. Avoid working in open fields, stay away from tall trees and electrical installations.")
        }

        if (list.size < 4) {
            list.add("🚜 General: Clean weeding and drainage channels to ensure healthy root aeration and healthy plant growth.")
        }
        return list.take(4)
    }

    suspend fun saveCachedWeather(key: String, info: WeatherInfo) {
        try {
            val json = serializeWeatherInfo(info)
            dao.insertWeatherCache(
                WeatherCacheEntity(
                    locationKey = key.trim().lowercase(),
                    locationName = info.locationName,
                    weatherJson = json,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            // Ignore cache write error
        }
    }

    suspend fun getCachedWeather(key: String): WeatherInfo? {
        try {
            val cache = dao.getWeatherCache(key.trim().lowercase()) ?: return null
            // Check if cache is older than 2 hours to keep it reasonably fresh
            if (System.currentTimeMillis() - cache.timestamp > 2 * 60 * 60 * 1000) {
                return null
            }
            return deserializeWeatherInfo(cache.weatherJson)
        } catch (e: Exception) {
            return null
        }
    }

    private fun serializeWeatherInfo(info: WeatherInfo): String {
        val obj = org.json.JSONObject()
        obj.put("locationName", info.locationName)
        obj.put("district", info.district)
        obj.put("state", info.state)
        obj.put("temperature", info.temperature)
        obj.put("feelsLike", info.feelsLike)
        obj.put("condition", info.condition)
        obj.put("rainProbability", info.rainProbability)
        obj.put("humidity", info.humidity)
        obj.put("windSpeed", info.windSpeed)
        obj.put("windDirection", info.windDirection)
        obj.put("uvIndex", info.uvIndex)
        obj.put("airPressure", info.airPressure)
        obj.put("visibility", info.visibility)
        obj.put("sunrise", info.sunrise)
        obj.put("sunset", info.sunset)
        obj.put("cloudCoverage", info.cloudCoverage)
        obj.put("aqi", info.aqi ?: -1)
        obj.put("lastUpdated", info.lastUpdated)

        val hourlyArray = org.json.JSONArray()
        for (h in info.hourlyForecast) {
            val hObj = org.json.JSONObject()
            hObj.put("time", h.time)
            hObj.put("temperature", h.temperature)
            hObj.put("condition", h.condition)
            hObj.put("rainChance", h.rainChance)
            hourlyArray.put(hObj)
        }
        obj.put("hourlyForecast", hourlyArray)

        val dailyArray = org.json.JSONArray()
        for (d in info.dailyForecast) {
            val dObj = org.json.JSONObject()
            dObj.put("day", d.day)
            dObj.put("condition", d.condition)
            dObj.put("minTemp", d.minTemp)
            dObj.put("maxTemp", d.maxTemp)
            dObj.put("rainChance", d.rainChance)
            dailyArray.put(dObj)
        }
        obj.put("dailyForecast", dailyArray)

        val advisoryArray = org.json.JSONArray()
        for (a in info.farmingAdvisory) {
            advisoryArray.put(a)
        }
        obj.put("farmingAdvisory", advisoryArray)

        return obj.toString()
    }

    private fun deserializeWeatherInfo(jsonStr: String): WeatherInfo {
        val obj = org.json.JSONObject(jsonStr)
        val hourlyList = mutableListOf<HourlyForecast>()
        val hourlyArray = obj.getJSONArray("hourlyForecast")
        for (i in 0 until hourlyArray.length()) {
            val hObj = hourlyArray.getJSONObject(i)
            hourlyList.add(
                HourlyForecast(
                    time = hObj.getString("time"),
                    temperature = hObj.getDouble("temperature"),
                    condition = hObj.getString("condition"),
                    rainChance = hObj.getInt("rainChance")
                )
            )
        }

        val dailyList = mutableListOf<DailyForecast>()
        val dailyArray = obj.getJSONArray("dailyForecast")
        for (i in 0 until dailyArray.length()) {
            val dObj = dailyArray.getJSONObject(i)
            dailyList.add(
                DailyForecast(
                    day = dObj.getString("day"),
                    condition = dObj.getString("condition"),
                    minTemp = dObj.getDouble("minTemp"),
                    maxTemp = dObj.getDouble("maxTemp"),
                    rainChance = dObj.getInt("rainChance")
                )
            )
        }

        val advisories = mutableListOf<String>()
        val advisoryArray = obj.getJSONArray("farmingAdvisory")
        for (i in 0 until advisoryArray.length()) {
            advisories.add(advisoryArray.getString(i))
        }

        val aqiVal = obj.optInt("aqi", -1)

        return WeatherInfo(
            locationName = obj.getString("locationName"),
            district = obj.getString("district"),
            state = obj.getString("state"),
            temperature = obj.getDouble("temperature"),
            feelsLike = obj.getDouble("feelsLike"),
            condition = obj.getString("condition"),
            rainProbability = obj.getInt("rainProbability"),
            humidity = obj.getInt("humidity"),
            windSpeed = obj.getDouble("windSpeed"),
            windDirection = obj.getString("windDirection"),
            uvIndex = obj.getInt("uvIndex"),
            airPressure = obj.getInt("airPressure"),
            visibility = obj.getDouble("visibility"),
            sunrise = obj.getString("sunrise"),
            sunset = obj.getString("sunset"),
            cloudCoverage = obj.getInt("cloudCoverage"),
            aqi = if (aqiVal == -1) null else aqiVal,
            lastUpdated = obj.getString("lastUpdated"),
            hourlyForecast = hourlyList,
            dailyForecast = dailyList,
            farmingAdvisory = advisories
        )
    }

    // --- CROP CALENDAR SYSTEM ---
    val allCropCalendars: Flow<List<CropCalendarEntity>> = dao.getAllCropCalendars()

    fun getStagesForCalendar(calendarId: Int): Flow<List<CropStageEntity>> {
        return dao.getStagesForCalendar(calendarId)
    }

    fun getTasksForCalendar(calendarId: Int): Flow<List<CropTaskReminderEntity>> {
        return dao.getTasksForCalendar(calendarId)
    }

    suspend fun updateTaskReminder(task: CropTaskReminderEntity) {
        dao.updateTaskReminder(task)
        if (task.isCompleted) {
            dao.insertNotification(
                NotificationAlertEntity(
                    title = "Task Completed ✅",
                    message = "Task '${task.taskTitle}' marked as finished.",
                    category = "Reminder"
                )
            )
        }
    }

    suspend fun addCustomTask(task: CropTaskReminderEntity) {
        dao.insertSingleTaskReminder(task)
        dao.insertNotification(
            NotificationAlertEntity(
                title = "New Task Reminder Set 🔔",
                message = "${task.taskTitle} scheduled for ${task.dueDate}",
                category = "Reminder"
            )
        )
    }

    suspend fun deleteTaskReminder(task: CropTaskReminderEntity) {
        dao.deleteTaskReminder(task)
    }

    suspend fun deleteCropCalendar(calendar: CropCalendarEntity) {
        dao.deleteStagesForCalendar(calendar.id)
        dao.deleteTasksForCalendar(calendar.id)
        dao.deleteCropCalendar(calendar)
    }

    suspend fun prepopulateCropCalendars() {
        val existing = dao.getAllCropCalendars().first()
        if (existing.isEmpty()) {
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            // Create initial default Crop Calendars for demonstration
            generateAndSaveCropCalendar(
                farmName = "Sri Lakshmi Farm (ఉత్తర పొలం)",
                cropName = "Paddy (Rice)",
                cropNameTe = "వరి (ప్యాడీ)",
                variety = "BPT 5204 (Samba Mahsuri)",
                farmArea = "3.5 Acres",
                soilType = "Black Cotton Soil",
                plantingDate = today,
                notes = "Main Kharif crop with drip irrigation and organic fertilizer schedule."
            )

            generateAndSaveCropCalendar(
                farmName = "Ganga Krishna Field (తూర్పు తోట)",
                cropName = "Chilli",
                cropNameTe = "మిరప (చిల్లీ)",
                variety = "Guntur Sannam (334)",
                farmArea = "2.0 Acres",
                soilType = "Red Sandy Soil",
                plantingDate = today,
                notes = "High-grade spicy chilli cultivation for market export."
            )
        }
    }

    suspend fun generateAndSaveCropCalendar(
        farmName: String,
        cropName: String,
        cropNameTe: String,
        variety: String,
        farmArea: String,
        soilType: String,
        plantingDate: String,
        notes: String = ""
    ): Long {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val startDateObj = try { sdf.parse(plantingDate) } catch (e: Exception) { java.util.Date() }
        val calendarCal = java.util.Calendar.getInstance().apply { time = startDateObj }

        // Generate stages & tasks according to crop profile
        val (durationDays, stages, tasks) = generateScheduleForCrop(
            cropName = cropName,
            cropNameTe = cropNameTe,
            startDateCal = calendarCal,
            sdf = sdf
        )

        val expectedHarvestCal = (calendarCal.clone() as java.util.Calendar).apply {
            add(java.util.Calendar.DAY_OF_YEAR, durationDays)
        }
        val expectedHarvestDateStr = sdf.format(expectedHarvestCal.time)

        val calendarEntity = CropCalendarEntity(
            farmName = farmName,
            cropName = cropName,
            cropNameTe = if (cropNameTe.isBlank()) getTeluguCropName(cropName) else cropNameTe,
            variety = variety,
            farmArea = farmArea,
            soilType = soilType,
            plantingDate = plantingDate,
            expectedHarvestDate = expectedHarvestDateStr,
            totalDurationDays = durationDays,
            notes = notes
        )

        val calendarId = dao.insertCropCalendar(calendarEntity).toInt()

        // Assign calendarId to stages and tasks
        val finalStages = stages.map { it.copy(calendarId = calendarId) }
        val finalTasks = tasks.map { it.copy(calendarId = calendarId) }

        dao.insertCropStages(finalStages)
        dao.insertCropTaskReminders(finalTasks)

        // Post notification alert for user
        dao.insertNotification(
            NotificationAlertEntity(
                title = "Crop Calendar Created 📅",
                message = "Crop calendar for $farmName ($cropName) created with ${finalStages.size} growth stages and ${finalTasks.size} task reminders.",
                category = "Reminder"
            )
        )

        return calendarId.toLong()
    }

    private fun getTeluguCropName(cropName: String): String {
        val c = cropName.lowercase()
        return when {
            c.contains("rice") || c.contains("paddy") -> "వరి"
            c.contains("chilli") || c.contains("chili") -> "మిరప"
            c.contains("cotton") -> "పత్తి"
            c.contains("groundnut") || c.contains("peanut") -> "వేరుశెనగ"
            c.contains("maize") || c.contains("corn") -> "మొక్కజొన్న"
            c.contains("tomato") -> "టమోటా"
            c.contains("sugarcane") -> "చెరకు"
            c.contains("turmeric") -> "పసుపు"
            c.contains("mango") -> "మామిడి"
            c.contains("onion") -> "ఉల్లిపాయ"
            c.contains("wheat") -> "గోధుమ"
            else -> cropName
        }
    }

    private fun generateScheduleForCrop(
        cropName: String,
        cropNameTe: String,
        startDateCal: java.util.Calendar,
        sdf: java.text.SimpleDateFormat
    ): Triple<Int, List<CropStageEntity>, List<CropTaskReminderEntity>> {
        val nameLower = cropName.lowercase()

        return when {
            nameLower.contains("chilli") || nameLower.contains("chili") -> generateChilliSchedule(startDateCal, sdf)
            nameLower.contains("cotton") -> generateCottonSchedule(startDateCal, sdf)
            nameLower.contains("groundnut") || nameLower.contains("peanut") -> generateGroundnutSchedule(startDateCal, sdf)
            nameLower.contains("maize") || nameLower.contains("corn") -> generateMaizeSchedule(startDateCal, sdf)
            nameLower.contains("tomato") -> generateTomatoSchedule(startDateCal, sdf)
            else -> generatePaddySchedule(startDateCal, sdf) // Default Paddy / Rice
        }
    }

    private fun calculateDate(baseCal: java.util.Calendar, offsetDays: Int, sdf: java.text.SimpleDateFormat): String {
        val tempCal = (baseCal.clone() as java.util.Calendar).apply {
            add(java.util.Calendar.DAY_OF_YEAR, offsetDays)
        }
        return sdf.format(tempCal.time)
    }

    // --- PADDY (RICE) SCHEDULE ---
    private fun generatePaddySchedule(
        startDateCal: java.util.Calendar,
        sdf: java.text.SimpleDateFormat
    ): Triple<Int, List<CropStageEntity>, List<CropTaskReminderEntity>> {
        val totalDays = 120
        val stages = listOf(
            CropStageEntity(
                calendarId = 0,
                stageName = "1. Nursery & Sowing Stage",
                stageNameTe = "1. నారుమడి పెంపకం & విత్తనాలు నాటడం",
                startDay = 0, endDay = 20,
                startDate = calculateDate(startDateCal, 0, sdf),
                endDate = calculateDate(startDateCal, 20, sdf),
                status = "Active",
                description = "Seed treatment with Carbendazim, land preparation, and nursery bed irrigation.",
                descriptionTe = "కార్బండజిమ్‌తో విత్తన శుద్ధి, నారుమడి తవ్వకం మరియు సక్రమ నీటి పారుదల.",
                iconType = "Sowing"
            ),
            CropStageEntity(
                calendarId = 0,
                stageName = "2. Main Field Transplanting & Vegetative Stage",
                stageNameTe = "2. నాట్లు వేయడం & శాఖీయ ఎదుగుదల",
                startDay = 21, endDay = 50,
                startDate = calculateDate(startDateCal, 21, sdf),
                endDate = calculateDate(startDateCal, 50, sdf),
                status = "Upcoming",
                description = "Transplanting 20-25 day seedlings in puddle field. Basal NPK fertilizer application.",
                descriptionTe = "20-25 రోజుల నారును నాట్లు వేయడం. బేసల్ డోస్ ఎరువుల వాడకం.",
                iconType = "Vegetative"
            ),
            CropStageEntity(
                calendarId = 0,
                stageName = "3. Tillering & Panicle Initiation",
                stageNameTe = "3. పిలకల సంఖ్య పెరుగుదల & ఈనె తొడగడం",
                startDay = 51, endDay = 80,
                startDate = calculateDate(startDateCal, 51, sdf),
                endDate = calculateDate(startDateCal, 80, sdf),
                status = "Upcoming",
                description = "Critical stage for nitrogen top dressing, active weed management, and leaf folder protection.",
                descriptionTe = "యూరియా పైపాటుగా చల్లడం, కలుపు నివారణ మరియు ఆకు చుట్టు పురుగు యాజమాన్యం.",
                iconType = "Tillering"
            ),
            CropStageEntity(
                calendarId = 0,
                stageName = "4. Flowering & Grain Filling",
                stageNameTe = "4. పూత దశ & పాలు పోసుకునే సమయం",
                startDay = 81, endDay = 105,
                startDate = calculateDate(startDateCal, 81, sdf),
                endDate = calculateDate(startDateCal, 105, sdf),
                status = "Upcoming",
                description = "Maintain 2-5 cm standing water layer. Spray MOP/Potash for heavy golden grains.",
                descriptionTe = "పొలంలో 2-5 సెం.మీ నీటిని ఉంచడం. మంచి గింజ బరువు కోసం పొటాష్ చల్లడం.",
                iconType = "Flowering"
            ),
            CropStageEntity(
                calendarId = 0,
                stageName = "5. Maturity & Golden Harvest",
                stageNameTe = "5. పక్వతకు రావడం & పంట కోత",
                startDay = 106, endDay = 120,
                startDate = calculateDate(startDateCal, 106, sdf),
                endDate = calculateDate(startDateCal, 120, sdf),
                status = "Upcoming",
                description = "Drain field 10 days before harvest. Combine harvesting when 85% grains turn golden yellow.",
                descriptionTe = "కోతకు 10 రోజుల ముందు నీటిని తీసివేయడం. 85% ధాన్యం బంగారు రంగులోకి వచ్చాక కోత.",
                iconType = "Harvest"
            )
        )

        val tasks = listOf(
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Nursery Land Puddling & Seed Sowing",
                taskTitleTe = "నారుమడి దుక్కి దున్నడం & విత్తనాలు చల్లడం",
                category = "Planting", categoryTe = "విత్తనాలు నాటడం",
                dueDate = calculateDate(startDateCal, 1, sdf), dayOffset = 1,
                instructions = "Prepare raised beds, treat 25kg seeds with Carbendazim @2g/kg.",
                instructionsTe = "ఎత్తైన నారుమడి తయారీ, 25 కేజీల విత్తనాలకు విత్తన శుద్ధి చేసుకోవాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Main Field Puddling & Basal Fertilizer (DAP 50kg)",
                taskTitleTe = "ప్రధాన పొలం బురద దుక్కి & డి.ఎ.పి ఎరువుల వాడకం",
                category = "Fertilization", categoryTe = "ఎరువులు",
                dueDate = calculateDate(startDateCal, 20, sdf), dayOffset = 20,
                instructions = "Apply DAP 50kg, MOP 25kg, and Zinc Sulphate 10kg per acre during final puddling.",
                instructionsTe = "ఎకరాకు 50 కేజీల డిఎపి, 25 కేజీల పొటాష్, 10 కేజీల జింక్ సల్ఫేట్ వేయాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Transplanting Seedlings to Main Field",
                taskTitleTe = "ప్రధాన పొలంలో నారు నాట్లు వేయడం",
                category = "Planting", categoryTe = "నాట్లు వేయడం",
                dueDate = calculateDate(startDateCal, 22, sdf), dayOffset = 22,
                instructions = "Transplant 2-3 seedlings per hill at 20x15 cm spacing.",
                instructionsTe = "చదరపు మీటరుకు 33-40 చొప్పున 2-3 నారు మొక్కలు నాటుకోవాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "First Herbicide Application (Pretilachlor)",
                taskTitleTe = "మొదటి కలుపు నివారణ మందు (ప్రిటిలాక్లోర్)",
                category = "Weeding", categoryTe = "కలుపు నివారణ",
                dueDate = calculateDate(startDateCal, 25, sdf), dayOffset = 25,
                instructions = "Apply Pretilachlor 50 EC @500ml/acre with sand within 3-5 days of transplanting.",
                instructionsTe = "నాటిన 3-5 రోజుల్లో ప్రిటిలాక్లోర్ 500 మి.లీ ఇసుకలో కలిపి చల్లాలి.",
                priority = "Medium"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "First Urea Top Dressing (25kg/Acre)",
                taskTitleTe = "మొదటి విడత యూరియా పైపాటుగా చల్లడం (25 కేజీలు)",
                category = "Fertilization", categoryTe = "ఎరువులు",
                dueDate = calculateDate(startDateCal, 40, sdf), dayOffset = 40,
                instructions = "Top dress 25kg Neem Coated Urea per acre during peak tillering stage.",
                instructionsTe = "పిలకల దశలో ఎకరాకు 25 కేజీల వేప పూత పూసిన యూరియా చల్లాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Stem Borer & Leaf Folder Spray (Chlorantraniliprole)",
                taskTitleTe = "కాండం తొలుచు పురుగు & ఆకు చుట్టు పురుగు నివారణ",
                category = "Pest Control", categoryTe = "పురుగుల నివారణ",
                dueDate = calculateDate(startDateCal, 55, sdf), dayOffset = 55,
                instructions = "Spray Coragen (Chlorantraniliprole) @60ml/acre in 200L water.",
                instructionsTe = "కోరాజెన్ మందును ఎకరాకు 60 మి.లీ చొప్పున నీటిలో కలిపి పిచికారీ చేయాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Second Urea & Potash Top Dressing",
                taskTitleTe = "రెండో విడత యూరియా & పొటాష్ ఎరువుల వాడకం",
                category = "Fertilization", categoryTe = "ఎరువులు",
                dueDate = calculateDate(startDateCal, 75, sdf), dayOffset = 75,
                instructions = "Apply 25kg Urea + 15kg MOP at panicle initiation stage.",
                instructionsTe = "ఈనె తొడిగే దశలో 25 కేజీల యూరియా + 15 కేజీల పొటాష్ వేయాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Neck Blast & Sheath Blight Preventive Spray",
                taskTitleTe = "మెడ విరుపు తెగులు & పొర తెగులు నివారణ మందు",
                category = "Pest Control", categoryTe = "పురుగుల నివారణ",
                dueDate = calculateDate(startDateCal, 90, sdf), dayOffset = 90,
                instructions = "Spray Tricyclazole 75 WP @0.6g/L or Nativo @120g/acre.",
                instructionsTe = "ట్రైసైక్లజోల్ 75 WP 0.6 గ్రా/లీ లేదా నాటివో 120 గ్రా చొప్పున పిచికారీ చేయాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Field Water Drainage Prior to Harvest",
                taskTitleTe = "కోతకు ముందు పొలంలో నీటిని తీసివేయుట",
                category = "Irrigation", categoryTe = "నీటి పారుదల",
                dueDate = calculateDate(startDateCal, 110, sdf), dayOffset = 110,
                instructions = "Completely drain all standing water from the field to enable machine combine harvesting.",
                instructionsTe = "హార్వెస్టర్ యంత్రం రాక కోసం పొలంలోని నీటిని పూర్తిగా తీసివేయాలి.",
                priority = "Medium"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Paddy Combine Harvesting & Bagging",
                taskTitleTe = "వరి కోత మిషన్ ద్వారా కోత & బస్తాలు నింపడం",
                category = "Harvesting", categoryTe = "పంట కోత",
                dueDate = calculateDate(startDateCal, 120, sdf), dayOffset = 120,
                instructions = "Harvest crop, dry paddy to <14% moisture content, and pack in gunny bags.",
                instructionsTe = "పంట కోత కోసి, ధాన్యం తడి 14% కంటే తగ్గేవరకు ఆరబెట్టి బస్తాల్లో నింపాలి.",
                priority = "High"
            )
        )

        return Triple(totalDays, stages, tasks)
    }

    // --- CHILLI SCHEDULE ---
    private fun generateChilliSchedule(
        startDateCal: java.util.Calendar,
        sdf: java.text.SimpleDateFormat
    ): Triple<Int, List<CropStageEntity>, List<CropTaskReminderEntity>> {
        val totalDays = 150
        val stages = listOf(
            CropStageEntity(
                calendarId = 0,
                stageName = "1. Nursery & Seedling Establishment",
                stageNameTe = "1. నారుమడి పెంచడం & మొలక దశ",
                startDay = 0, endDay = 35,
                startDate = calculateDate(startDateCal, 0, sdf),
                endDate = calculateDate(startDateCal, 35, sdf),
                status = "Active",
                description = "Pro-tray seedling nursery production with cocopeat and shade-net protection.",
                descriptionTe = "ప్రోట్రేలలో కోకోపీట్ సహాయంతో నారు పెంపకం మరియు నీడ వల రక్షణ.",
                iconType = "Sowing"
            ),
            CropStageEntity(
                calendarId = 0,
                stageName = "2. Ridge & Furrow Transplanting",
                stageNameTe = "2. బోదెలపై నాట్లు వేయడం",
                startDay = 36, endDay = 65,
                startDate = calculateDate(startDateCal, 36, sdf),
                endDate = calculateDate(startDateCal, 65, sdf),
                status = "Upcoming",
                description = "Transplanting 35-day seedlings with black mulch sheet and drip fertigation setup.",
                descriptionTe = "నల్లటి మల్చింగ్ షీట్ మరియు బిందు సేద్యం వ్యవస్థతో నాట్లు వేయడం.",
                iconType = "Vegetative"
            ),
            CropStageEntity(
                calendarId = 0,
                stageName = "3. Flowering & Branching Stage",
                stageNameTe = "3. కొమ్మల విస్తరణ & పూత దశ",
                startDay = 66, endDay = 100,
                startDate = calculateDate(startDateCal, 66, sdf),
                endDate = calculateDate(startDateCal, 100, sdf),
                status = "Upcoming",
                description = "Intensive micronutrient, 19-19-19 soluble fertilizer spraying, and thrips management.",
                descriptionTe = "19-19-19 నీటిలో కరిగే ఎరువులు మరియు తామర పురుగుల సమగ్ర నివారణ.",
                iconType = "Flowering"
            ),
            CropStageEntity(
                calendarId = 0,
                stageName = "4. Green Fruit Setting & Red Picking",
                stageNameTe = "4. పచ్చి మిరప కాయల అభివృద్ధి & మొదటి కోత",
                startDay = 101, endDay = 130,
                startDate = calculateDate(startDateCal, 101, sdf),
                endDate = calculateDate(startDateCal, 130, sdf),
                status = "Upcoming",
                description = "First picking for green chilli or ripening for red dry chilli.",
                descriptionTe = "పచ్చి మిరప కోత లేదా ఎర్ర మిరపకాయల కోసం పక్వతకు రావడం.",
                iconType = "Harvest"
            ),
            CropStageEntity(
                calendarId = 0,
                stageName = "5. Dry Red Chilli Harvesting & Drying",
                stageNameTe = "5. ఎండు మిరపకాయల కోత & ఆరబెట్టడం",
                startDay = 131, endDay = 150,
                startDate = calculateDate(startDateCal, 131, sdf),
                endDate = calculateDate(startDateCal, 150, sdf),
                status = "Upcoming",
                description = "Final picking, tarpaulin sheet drying, grading according to color and pungency.",
                descriptionTe = "చివరి కోత, టార్పాలిన్ పరదాలపై ఆరబెట్టడం, రంగు ఆధారంగా గ్రేడింగ్.",
                iconType = "Harvest"
            )
        )

        val tasks = listOf(
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Chilli Seed Bed Preparation & Sowing",
                taskTitleTe = "మిరప విత్తనాలు నాటడం & నారుమడి సిద్ధం చేయుట",
                category = "Planting", categoryTe = "విత్తనాలు నాటడం",
                dueDate = calculateDate(startDateCal, 1, sdf), dayOffset = 1,
                instructions = "Sow seeds in 98-cavity pro-trays filled with cocopeat and Trichoderma.",
                instructionsTe = "కోకోపీట్ మరియు ట్రైకోడెర్మాతో ప్రొట్రేలలో విత్తనాలు చల్లుకోవాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Black Mulching Sheet Laying & Drip Line Setup",
                taskTitleTe = "నల్ల మల్చింగ్ షీట్ పరచడం & డ్రిప్ లైన్ అమరిక",
                category = "Irrigation", categoryTe = "నీటి పారుదల",
                dueDate = calculateDate(startDateCal, 30, sdf), dayOffset = 30,
                instructions = "Lay 25-micron silver-black mulching film and punch holes at 45cm distance.",
                instructionsTe = "25 మైక్రాన్ మల్చింగ్ షీట్ పరిచి 45 సెం.మీ దూరంలో రంధ్రాలు చేయాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Chilli Seedling Field Transplanting",
                taskTitleTe = "మిరప నారు పొలంలో నాటడం",
                category = "Planting", categoryTe = "నాట్లు వేయడం",
                dueDate = calculateDate(startDateCal, 35, sdf), dayOffset = 35,
                instructions = "Transplant healthy seedlings during evening hours and apply light drip irrigation.",
                instructionsTe = "సాయంత్రం వేళల్లో నారు నాటుకొని వెంటనే తేలికపాటి డ్రిప్ నీరు ఇవ్వాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Thrips & Mites Spray (Fipronil / Exponent)",
                taskTitleTe = "తామర పురుగులు & నల్లి నివారణ పిచికారీ",
                category = "Pest Control", categoryTe = "పురుగుల నివారణ",
                dueDate = calculateDate(startDateCal, 50, sdf), dayOffset = 50,
                instructions = "Spray Fipronil 5 SC @2ml/L or Pegasus @1g/L for leaf curl protection.",
                instructionsTe = "ఆకు ముడత నివారణకు ఫిప్రోనిల్ 2 మి.లీ లేదా పెగాసస్ 1 గ్రా చొప్పున పిచికారీ చేయాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Fertigation with 19-19-19 & Micronutrients",
                taskTitleTe = "డ్రిప్ ద్వారా 19-19-19 & సూక్ష్మపోషకాలు అందించడం",
                category = "Fertilization", categoryTe = "ఎరువులు",
                dueDate = calculateDate(startDateCal, 65, sdf), dayOffset = 65,
                instructions = "Fertigate 5kg NPK 19:19:19 per acre along with Formula 4 micronutrients.",
                instructionsTe = "ఎకరాకు 5 కేజీల 19-19-19 ఎరువులు డ్రిప్ ద్వారా అందించి పూతను ప్రోత్సహించాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "First Green Chilli Harvesting Pass",
                taskTitleTe = "మొదటి విడత పచ్చి మిరప కోత",
                category = "Harvesting", categoryTe = "పంట కోత",
                dueDate = calculateDate(startDateCal, 110, sdf), dayOffset = 110,
                instructions = "Pick mature firm green pods without damaging flowering shoots.",
                instructionsTe = "పువ్వులకు హాని కలగకుండా ముదిరిన పచ్చి మిరపకాయలను జాగ్రత్తగా కోయాలి.",
                priority = "Medium"
            )
        )

        return Triple(totalDays, stages, tasks)
    }

    // --- COTTON SCHEDULE ---
    private fun generateCottonSchedule(
        startDateCal: java.util.Calendar,
        sdf: java.text.SimpleDateFormat
    ): Triple<Int, List<CropStageEntity>, List<CropTaskReminderEntity>> {
        val totalDays = 160
        val stages = listOf(
            CropStageEntity(
                calendarId = 0,
                stageName = "1. Germination & Seedling Phase",
                stageNameTe = "1. విత్తనం మొలకెత్తడం & మొలక దశ",
                startDay = 0, endDay = 30,
                startDate = calculateDate(startDateCal, 0, sdf),
                endDate = calculateDate(startDateCal, 30, sdf),
                status = "Active",
                description = "Dibbling seeds in 90x60 cm grid. Gap filling and thinning at 12 days.",
                descriptionTe = "90x60 సెం.మీ దూరంలో విత్తనాలు విత్తుకోవాలి. ఖాళీలు పూడ్చడం.",
                iconType = "Sowing"
            ),
            CropStageEntity(
                calendarId = 0,
                stageName = "2. Square Formation & Vegetative Branching",
                stageNameTe = "2. పత్తి మొగ్గలు (స్క్వేర్స్) రావడం",
                startDay = 31, endDay = 70,
                startDate = calculateDate(startDateCal, 31, sdf),
                endDate = calculateDate(startDateCal, 70, sdf),
                status = "Upcoming",
                description = "Inter-cultivation weeding, earthing up, and sucking pest management.",
                descriptionTe = "గుంటక తోలడం, మొదళ్లకు మట్టి తోయడం మరియు రసం పీల్చే పురుగుల నివారణ.",
                iconType = "Vegetative"
            ),
            CropStageEntity(
                calendarId = 0,
                stageName = "3. Flowering & Boll Formation",
                stageNameTe = "3. పూత & పత్తి కాయల ఏర్పడే దశ",
                startDay = 71, endDay = 120,
                startDate = calculateDate(startDateCal, 71, sdf),
                endDate = calculateDate(startDateCal, 120, sdf),
                status = "Upcoming",
                description = "Pink bollworm monitoring with pheromone traps and boron/KNO3 foliar spray.",
                descriptionTe = "గులాబీ రంగు పురుగు నివారణకు లింగాకర్షక బుట్టల ఏర్పాటు, బోరాన్ పిచికారీ.",
                iconType = "Flowering"
            ),
            CropStageEntity(
                calendarId = 0,
                stageName = "4. Boll Bursting & White Gold Harvest",
                stageNameTe = "4. కాయలు పగలడం & తెల్ల పత్తి కోత",
                startDay = 121, endDay = 160,
                startDate = calculateDate(startDateCal, 121, sdf),
                endDate = calculateDate(startDateCal, 160, sdf),
                status = "Upcoming",
                description = "Picking clean cotton without trash in bright sunlight and grading for ginning mill.",
                descriptionTe = "ఆకులు, చెత్త లేకుండా శుభ్రమైన పత్తి కోసి ఎండలో ఆరబెట్టుకోవాలి.",
                iconType = "Harvest"
            )
        )

        val tasks = listOf(
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Cotton Seed Dibbling & Basal Fertilizer",
                taskTitleTe = "పత్తి విత్తనాలు నాటడం & కాంప్లెక్స్ ఎరువుల వాడకం",
                category = "Planting", categoryTe = "విత్తనాలు నాటడం",
                dueDate = calculateDate(startDateCal, 1, sdf), dayOffset = 1,
                instructions = "Sow Bt Cotton seeds at 3x2 feet spacing along with 50kg NPK 20-20-0-13.",
                instructionsTe = "3x2 అడుగుల దూరంలో బిటి పత్తి విత్తుకోవాలి, 50 కేజీల కాంప్లెక్స్ ఎరువులు వేయాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Sucking Pest Spray (Imidacloprid / Acetamiprid)",
                taskTitleTe = "రసం పీల్చే పురుగుల నివారణ పిచికారీ",
                category = "Pest Control", categoryTe = "పురుగుల నివారణ",
                dueDate = calculateDate(startDateCal, 25, sdf), dayOffset = 25,
                instructions = "Spray Imidacloprid 17.8 SL @0.4ml/L for aphids, jassids, and thrips.",
                instructionsTe = "పేనుబంక, పచ్చదోమ నివారణకు ఇమిడాక్లోప్రిడ్ 0.4 మి.లీ/లీ చొప్పున పిచికారీ చేయాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Earthing Up & Top Dress Urea (35kg/Acre)",
                taskTitleTe = "మొదళ్లకు మట్టి తోయడం & యూరియా చల్లడం",
                category = "Fertilization", categoryTe = "ఎరువులు",
                dueDate = calculateDate(startDateCal, 45, sdf), dayOffset = 45,
                instructions = "Perform earthing up with tractor/bullocks and apply 35kg Urea + 15kg MOP.",
                instructionsTe = "గుంటక తోలి మొదళ్లకు మట్టి తోసి 35 కేజీల యూరియా, 15 కేజీల పొటాష్ అందించాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Pink Bollworm Pheromone Traps Installation",
                taskTitleTe = "గులాబీ రంగు పురుగు లింగాకర్షక బుట్టల ఏర్పాటు",
                category = "Pest Control", categoryTe = "పురుగుల నివారణ",
                dueDate = calculateDate(startDateCal, 60, sdf), dayOffset = 60,
                instructions = "Install 4 pheromone traps per acre to monitor pink bollworm moth catches.",
                instructionsTe = "ఎకరాకు 4 లింగాకర్షక బుట్టలు అమర్చి ఉధృతిని గమనించాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "First Cotton Picking Pass",
                taskTitleTe = "మొదటి విడత తెల్ల పత్తి ఏరడం (కోత)",
                category = "Harvesting", categoryTe = "పంట కోత",
                dueDate = calculateDate(startDateCal, 130, sdf), dayOffset = 130,
                instructions = "Pick fully opened white bolls after 10 AM when dew evaporates.",
                instructionsTe = "మంచు విడిపోయాక బాగా విచ్చుకున్న తెల్ల పత్తిని జాగ్రత్తగా ఏరాలి.",
                priority = "High"
            )
        )

        return Triple(totalDays, stages, tasks)
    }

    // --- GROUNDNUT SCHEDULE ---
    private fun generateGroundnutSchedule(
        startDateCal: java.util.Calendar,
        sdf: java.text.SimpleDateFormat
    ): Triple<Int, List<CropStageEntity>, List<CropTaskReminderEntity>> {
        val totalDays = 105
        val stages = listOf(
            CropStageEntity(
                calendarId = 0,
                stageName = "1. Sowing & Seedling Phase",
                stageNameTe = "1. విత్తనం నాటడం & మొలక దశ",
                startDay = 0, endDay = 25,
                startDate = calculateDate(startDateCal, 0, sdf),
                endDate = calculateDate(startDateCal, 25, sdf),
                status = "Active",
                description = "Trichoderma seed treatment, sowing with seed drill at 30x10 cm.",
                descriptionTe = "ట్రైకోడెర్మాతో విత్తన శుద్ధి, గొర్రుతో నాటుకోవాలి.",
                iconType = "Sowing"
            ),
            CropStageEntity(
                calendarId = 0,
                stageName = "2. Flowering & Peg Initiation (Critical)",
                stageNameTe = "2. పూత దశ & ఊడలు దిగే సమయం (కీలకం)",
                startDay = 26, endDay = 55,
                startDate = calculateDate(startDateCal, 26, sdf),
                endDate = calculateDate(startDateCal, 55, sdf),
                status = "Upcoming",
                description = "Gypsum application (200kg/acre) for pod development and peg entry into soil.",
                descriptionTe = "జిప్సం (200 కేజీలు) వేసి ఊడలు నేలలోకి సులభంగా దిగేలా చూడడం.",
                iconType = "Flowering"
            ),
            CropStageEntity(
                calendarId = 0,
                stageName = "3. Pod Development & Maturity",
                stageNameTe = "3. కాయ ఊరడం & ముదిరే సమయం",
                startDay = 56, endDay = 105,
                startDate = calculateDate(startDateCal, 56, sdf),
                endDate = calculateDate(startDateCal, 105, sdf),
                status = "Upcoming",
                description = "Maintain soil moisture. Check inner shell blackening for harvest maturity.",
                descriptionTe = "తగినంత తేమను ఉంచాలి. కాయ పెంకు లోపల నల్లటి రంగు వచ్చాక పక్వతను గుర్తించాలి.",
                iconType = "Harvest"
            )
        )

        val tasks = listOf(
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Groundnut Seed Treatment & Sowing",
                taskTitleTe = "వేరుశెనగ విత్తన శుద్ధి & గొర్రుతో విత్తుట",
                category = "Planting", categoryTe = "విత్తనాలు నాటడం",
                dueDate = calculateDate(startDateCal, 1, sdf), dayOffset = 1,
                instructions = "Treat seeds with Mancozeb 3g/kg and Rhizobium culture prior to sowing.",
                instructionsTe = "మాంకోజెబ్ 3 గ్రా/కేజీ మరియు రైజోబియం కల్చర్‌తో విత్తన శుద్ధి చేయాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Gypsum Soil Application (200kg/Acre)",
                taskTitleTe = "జిప్సం ఎరువు భూమిలో వేయుట (200 కేజీలు)",
                category = "Fertilization", categoryTe = "ఎరువులు",
                dueDate = calculateDate(startDateCal, 35, sdf), dayOffset = 35,
                instructions = "Broadcast Gypsum 200kg/acre at peg initiation stage around plant base.",
                instructionsTe = "ఊడలు దిగే దశలో ఎకరాకు 200 కేజీల జిప్సం చల్లి మట్టి కప్పాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Tikka Leaf Spot Fungicide Spray",
                taskTitleTe = "టిక్కా ఆకుమచ్చ తెగులు నివారణ మందు పిచికారీ",
                category = "Pest Control", categoryTe = "పురుగుల నివారణ",
                dueDate = calculateDate(startDateCal, 50, sdf), dayOffset = 50,
                instructions = "Spray Hexaconazole 5 EC @2ml/L or Saaf @2g/L.",
                instructionsTe = "టిక్కా మచ్చల నివారణకు హెక్సాకోనజోల్ 2 మి.లీ నీటిలో కలిపి చల్లాలి.",
                priority = "Medium"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Groundnut Pod Harvesting & Threshing",
                taskTitleTe = "వేరుశెనగ పంట తవ్వకం (తవ్వడం) & కాయలు వేరు చేయుట",
                category = "Harvesting", categoryTe = "పంట కోత",
                dueDate = calculateDate(startDateCal, 105, sdf), dayOffset = 105,
                instructions = "Uproot plants using groundnut digger, dry pods in sun for 3 days.",
                instructionsTe = "బ్లేడు ద్వారా చెట్లను తవ్వి 3 రోజులు ఎండలో ఆరబెట్టి కాయలు వేరు చేయాలి.",
                priority = "High"
            )
        )

        return Triple(totalDays, stages, tasks)
    }

    // --- MAIZE SCHEDULE ---
    private fun generateMaizeSchedule(
        startDateCal: java.util.Calendar,
        sdf: java.text.SimpleDateFormat
    ): Triple<Int, List<CropStageEntity>, List<CropTaskReminderEntity>> {
        val totalDays = 110
        val stages = listOf(
            CropStageEntity(
                calendarId = 0,
                stageName = "1. Germination & Knee-High Phase",
                stageNameTe = "1. మొలకెత్తడం & మోకాలు ఎత్తు ఎదుగుదల",
                startDay = 0, endDay = 30,
                startDate = calculateDate(startDateCal, 0, sdf),
                endDate = calculateDate(startDateCal, 30, sdf),
                status = "Active",
                description = "Seed sowing at 60x20 cm. Fall Armyworm scouting in leaf whorls.",
                descriptionTe = "60x20 సెం.మీ దూరంలో నాటుకోవాలి. లద్దె పురుగు (ఫాల్ ఆర్మీవార్మ్) నివారణ.",
                iconType = "Sowing"
            ),
            CropStageEntity(
                calendarId = 0,
                stageName = "2. Tasseling & Silking Stage",
                stageNameTe = "2. పూత & కంకి మగ/ఆడ పూత రావడం",
                startDay = 31, endDay = 70,
                startDate = calculateDate(startDateCal, 31, sdf),
                endDate = calculateDate(startDateCal, 70, sdf),
                status = "Upcoming",
                description = "Critical water stress sensitive phase. Urea top dressing.",
                descriptionTe = "నీటి పారుదల అత్యంత కీలక దశ. యూరియా పైపాటుగా అందించడం.",
                iconType = "Flowering"
            ),
            CropStageEntity(
                calendarId = 0,
                stageName = "3. Grain Filling & Cob Harvesting",
                stageNameTe = "3. గింజలు ఊరడం & మొక్కజొన్న కంకుల కోత",
                startDay = 71, endDay = 110,
                startDate = calculateDate(startDateCal, 71, sdf),
                endDate = calculateDate(startDateCal, 110, sdf),
                status = "Upcoming",
                description = "Black layer formation on grain base indicating full maturity.",
                descriptionTe = "గింజ మొదట్లో నల్లటి చుక్క ఏర్పడి పక్వతకు రావడము.",
                iconType = "Harvest"
            )
        )

        val tasks = listOf(
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Hybrid Maize Seed Sowing & Atrazine Spray",
                taskTitleTe = "హైబ్రిడ్ మొక్కజొన్న నాటడం & అట్రాజిన్ కలుపు మందు",
                category = "Planting", categoryTe = "విత్తనాలు నాటడం",
                dueDate = calculateDate(startDateCal, 1, sdf), dayOffset = 1,
                instructions = "Sow 8kg seeds/acre and spray Atrazine 50 WP @1kg/acre as pre-emergence.",
                instructionsTe = "ఎకరాకు 8 కేజీల విత్తనాలు నాటి, మరుసటి రోజు అట్రాజిన్ మందు చల్లాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Fall Armyworm Insecticide Poison Baiting",
                taskTitleTe = "కత్తెర పురుగు (లద్దె పురుగు) నివారణ మందు/రసం చల్లుట",
                category = "Pest Control", categoryTe = "పురుగుల నివారణ",
                dueDate = calculateDate(startDateCal, 20, sdf), dayOffset = 20,
                instructions = "Apply Emamectin Benzoate @0.4g/L or Spinetoram in plant whorls.",
                instructionsTe = "సుడిలో ఎమామెక్టిన్ బెంజోయేట్ 0.4 గ్రా/లీ లేదా విషపు ఎర వేయాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Maize Cob Harvesting & De-husking",
                taskTitleTe = "కంకుల కోత & పొట్టు తీసి ఆరబెట్టుట",
                category = "Harvesting", categoryTe = "పంట కోత",
                dueDate = calculateDate(startDateCal, 110, sdf), dayOffset = 110,
                instructions = "Harvest dry cobs when husks turn papery pale white.",
                instructionsTe = "కంకి పొట్టు తెల్లగా మారిన తర్వాత కోసి త్రెషర్ ద్వారా గింజలు వేరు చేయాలి.",
                priority = "High"
            )
        )

        return Triple(totalDays, stages, tasks)
    }

    // --- TOMATO SCHEDULE ---
    private fun generateTomatoSchedule(
        startDateCal: java.util.Calendar,
        sdf: java.text.SimpleDateFormat
    ): Triple<Int, List<CropStageEntity>, List<CropTaskReminderEntity>> {
        val totalDays = 120
        val stages = listOf(
            CropStageEntity(
                calendarId = 0,
                stageName = "1. Nursery Bed & Staking Setup",
                stageNameTe = "1. నారుమడి & కర్రలు కట్టడం (స్టేకింగ్)",
                startDay = 0, endDay = 30,
                startDate = calculateDate(startDateCal, 0, sdf),
                endDate = calculateDate(startDateCal, 30, sdf),
                status = "Active",
                description = "Pro-tray nursery raising and trellising bamboo pole preparation.",
                descriptionTe = "ప్రోట్రేలలో నారు పెంపకం మరియు వెదురు బొంగులతో పందిరి అమరిక.",
                iconType = "Sowing"
            ),
            CropStageEntity(
                calendarId = 0,
                stageName = "2. Field Transplanting & Flowering",
                stageNameTe = "2. పొలంలో నాటడం & పూత దశ",
                startDay = 31, endDay = 65,
                startDate = calculateDate(startDateCal, 31, sdf),
                endDate = calculateDate(startDateCal, 65, sdf),
                status = "Upcoming",
                description = "Transplanting with drip fertigation and calcium nitrate spray for blossom end rot prevention.",
                descriptionTe = "డ్రిప్ ద్వారా ఎరువులు మరియు కాల్సియం నైట్రేట్ చల్లడం.",
                iconType = "Flowering"
            ),
            CropStageEntity(
                calendarId = 0,
                stageName = "3. Fruit Picking Passes (Multiple)",
                stageNameTe = "3. టమోటా కాయల కోతలు (పలసల వారీగా)",
                startDay = 66, endDay = 120,
                startDate = calculateDate(startDateCal, 66, sdf),
                endDate = calculateDate(startDateCal, 120, sdf),
                status = "Upcoming",
                description = "Harvesting breaker-stage firm red tomatoes every 3-4 days.",
                descriptionTe = "ప్రతి 3-4 రోజులకు ఒకసారి దోరగా పండిన టమోటాలు కోయాలి.",
                iconType = "Harvest"
            )
        )

        val tasks = listOf(
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Tomato Seedling Transplanting & Mulching",
                taskTitleTe = "టమోటా నారు నాట్లు వేయడం & మల్చింగ్",
                category = "Planting", categoryTe = "విత్తనాలు నాటడం",
                dueDate = calculateDate(startDateCal, 25, sdf), dayOffset = 25,
                instructions = "Transplant 25-day seedlings on raised beds with drip irrigation.",
                instructionsTe = "ఎత్తైన మడులపై 25 రోజుల నారు నాటుకొని నీరు అందించాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Bamboo Staking & String Tying",
                taskTitleTe = "వెదురు కర్రలు నాటడం & తాడుతో కట్టడం (స్టేకింగ్)",
                category = "Planting", categoryTe = "మొక్కలకు సపోర్ట్",
                dueDate = calculateDate(startDateCal, 40, sdf), dayOffset = 40,
                instructions = "Erect bamboo poles every 10 feet and tie plastic twines for plant support.",
                instructionsTe = "మొక్కలు కింద పడిపోకుండా వెదురు బొంగులు నాటి తాడుతో కట్టాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "Calcium Nitrate & Boron Foliar Spray",
                taskTitleTe = "కాల్సియం నైట్రేట్ & బోరాన్ పిచికారీ",
                category = "Fertilization", categoryTe = "ఎరువులు",
                dueDate = calculateDate(startDateCal, 55, sdf), dayOffset = 55,
                instructions = "Spray Calcium Nitrate @5g/L + Boron @1g/L to prevent fruit cracking and blossom rot.",
                instructionsTe = "కాయలు పగలకుండా కాల్సియం నైట్రేట్ 5 గ్రా/లీ + బోరాన్ పిచికారీ చేయాలి.",
                priority = "High"
            ),
            CropTaskReminderEntity(
                calendarId = 0,
                taskTitle = "First Major Tomato Fruit Harvesting",
                taskTitleTe = "మొదటి పెద్ద విడత టమోటా కాయల కోత",
                category = "Harvesting", categoryTe = "పంట కోత",
                dueDate = calculateDate(startDateCal, 75, sdf), dayOffset = 75,
                instructions = "Harvest shiny breaker stage fruits early morning into wooden plastic crates.",
                instructionsTe = "ఉదయాన్నే దోర పండ్లను జాగ్రత్తగా కోసి ప్లాస్టిక్ క్రేట్లలో భద్రపరచాలి.",
                priority = "High"
            )
        )

        return Triple(totalDays, stages, tasks)
    }

    // --- WOMEN FARMER STORIES ("మా మహిళా రైతుల స్ఫూర్తి కథలు") ---
    val allApprovedWomenFarmerStories: Flow<List<WomanFarmerStoryEntity>> = dao.getAllApprovedWomenFarmerStories()
    val pendingWomenFarmerStories: Flow<List<WomanFarmerStoryEntity>> = dao.getPendingWomenFarmerStories()
    val featuredTodayStory: Flow<WomanFarmerStoryEntity?> = dao.getFeaturedTodayStory()

    suspend fun getApprovedStoriesListOnce(): List<WomanFarmerStoryEntity> = dao.getApprovedStoriesListOnce()

    suspend fun submitWomanFarmerStory(story: WomanFarmerStoryEntity): Long {
        return dao.insertWomanFarmerStory(story)
    }

    suspend fun incrementStoryLike(id: Int) {
        dao.incrementStoryLike(id)
    }

    suspend fun approveStory(id: Int) {
        dao.approveStory(id)
    }

    suspend fun deleteStory(story: WomanFarmerStoryEntity) {
        dao.deleteWomanFarmerStory(story)
    }

    suspend fun setFeaturedStory(id: Int) {
        dao.setFeaturedStory(id)
    }

    suspend fun prepopulateWomenFarmerStories() {
        if (dao.getWomenFarmerStoriesCount() == 0) {
            val initialStories = listOf(
                WomanFarmerStoryEntity(
                    name = "గొర్ల వెంకటలక్ష్మి",
                    village = "సురేపల్లె, ముసునూరు మండలం, ఏలూరు జిల్లా",
                    cropOrWork = "సేంద్రీయ కూరగాయలు & పశుపోషణ",
                    category = "కూరగాయల సాగు",
                    shortStoryTe = "3 ఎకరాల్లో సహజ సిద్ధమైన జీవామృతం ఉపయోగించి పండిస్తున్న టమోటా, వంకాయ సాగు. ప్రతి రోజూ ఉదయాన్నే 4 గంటలకే లేచి పొలం పనులు చేస్తూ గ్రామంలోని ఇతర మహిళలకు ఆదర్శంగా నిలిచారు.",
                    shortStoryEn = "Cultivates 3 acres of organic tomatoes and brinjal using natural Jeevamrutham. Waking up at 4 AM daily, she inspires women across the village.",
                    fullStoryTe = "ఏలూరు జిల్లా ముసునూరు మండలం సురేపల్లె గ్రామానికి చెందిన వెంకటలక్ష్మి గారు గత 8 ఏళ్ళుగా రసాయనాలు లేకుండా కేవలం దేశవాళీ ఆవు మూత్రం, పేడతో జీవామృతం తయారుచేసి కూరగాయలు పండిస్తున్నారు. మొదట్లో ఇబ్బందులు ఎదురైనా, ఆమె పట్టుదలతో నాణ్యమైన దిగుబడి సాధించి నేరుగా రైతు బజార్లలో విక్రయిస్తూ కుటుంబానికి ఆసరాగా నిలిచారు.",
                    fullStoryEn = "In Surepalle village, Venkatalakshmi has been practicing zero-chemical organic farming for 8 years using Jeevamrutham made from indigenous cow dung and urine. Despite initial hardships, her courage helped her harvest quality produce and earn direct profit at Rythu Bazaars.",
                    photoUrl = "https://images.unsplash.com/photo-1595273670150-bd0c3c392e46?w=600&auto=format&fit=crop&q=60",
                    likeCount = 48,
                    isFeaturedToday = true,
                    isApproved = true,
                    hasConsent = true,
                    submittedBy = "Admin"
                ),
                WomanFarmerStoryEntity(
                    name = "మండల లలితమ్మ",
                    village = "నూజివీడు మండలం, ఏలూరు జిల్లా",
                    cropOrWork = "పాడి పరిశ్రమ & స్వచ్ఛమైన పాల సేకరణ",
                    category = "పాడి పరిశ్రమ",
                    shortStoryTe = "రెండు గేదెలతో ప్రారంభించి, నేడు 8 పాడి పశువులతో విజయవంతంగా డైరీ ఫారమ్ నడుపుతున్నారు. మహిళా సహకార సంఘం ద్వారా గ్రామంలో 30 మంది మహిళలకు స్థిరమైన ఉపాధి కల్పించారు.",
                    shortStoryEn = "Started with 2 buffaloes, now runs a successful dairy unit with 8 cattle, creating sustainable income for 30 village women through a self-help cooperative.",
                    fullStoryTe = "కష్టపడే తత్వమే ఆయుధంగా లలితమ్మ గారు పాడి పరిశ్రమను నమ్ముకున్నారు. నాణ్యమైన పచ్చిగడ్డి పెంపకం, సకాలంలో పశువైద్యం మరియు శుభ్రమైన యజమాన్య పద్ధతుల ద్వారా నెలకు స్థిరమైన ఆదాయం పొందుతున్నారు. గ్రామంలోని ఇతర మహిళా రైతులకు పాడి నిర్వహణలో శిక్షణ ఇస్తూ స్వయం సమృద్ధికి దోహదపడుతున్నారు.",
                    fullStoryEn = "Believing in hard work, Lalithamma established her dairy farm with proper green fodder management and hygienic milking practices. She provides hands-on dairy training to fellow village women to promote financial independence.",
                    photoUrl = "https://images.unsplash.com/photo-1544717305-2782549b5136?w=600&auto=format&fit=crop&q=60",
                    likeCount = 35,
                    isFeaturedToday = false,
                    isApproved = true,
                    hasConsent = true,
                    submittedBy = "Admin"
                ),
                WomanFarmerStoryEntity(
                    name = "చింతా సీతారావమ్మ",
                    village = "చేబ్రోలు గ్రామం, గుంటూరు జిల్లా",
                    cropOrWork = "విత్తన శుద్ధి & పరంపరాగత విత్తన పరిరక్షణ",
                    category = "విత్తన/పంట పనులు",
                    shortStoryTe = "వందల ఏళ్ల నాటి దేశవాళీ మిరప మరియు వరి విత్తనాలను సేకరించి, భద్రపరిచి తోటి రైతులకు ఉచితంగా అందిస్తున్నారు.",
                    shortStoryEn = "Conserves traditional indigenous chilli and paddy seed varieties, distributing them freely to preserve native agricultural biodiversity.",
                    fullStoryTe = "గుంటూరు జిల్లా చేబ్రోలుకు చెందిన సీతారావమ్మ గారు పూర్వీకుల నుంచి వస్తున్న విత్తన పరిరక్షణ పద్ధతులను కాపాడుతున్నారు. పురుగు పట్టకుండా వేప ఆకులు, బూడిదను ఉపయోగించి సహజ విత్తన బ్యాంక్ ఏర్పాటు చేశారు. ప్రకృతి విపత్తుల సమయంలోనూ తట్టుకునే విత్తనాలను వర్ధమాన రైతులకు అందిస్తూ ప్రశంసలు పొందారు.",
                    fullStoryEn = "Seetharavamma has maintained an indigenous seed bank preserved naturally using neem leaves and ash. During pest outbreaks or climate stress, her traditional resilient seeds have saved crops for scores of small farmers.",
                    photoUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=600&auto=format&fit=crop&q=60",
                    likeCount = 59,
                    isFeaturedToday = false,
                    isApproved = true,
                    hasConsent = true,
                    submittedBy = "Admin"
                ),
                WomanFarmerStoryEntity(
                    name = "కమ్మరి లక్ష్మీదేవి",
                    village = "జంగారెడ్డిగూడెం, పశ్చిమ గోదావరి",
                    cropOrWork = "డ్రోన్ నిర్వహణ & ఆధునిక యంత్రాల వినియోగం",
                    category = "స్వయం ఉపాధి",
                    shortStoryTe = "అగ్రి డ్రోన్ పైలట్ శిక్షణ పొంది, తక్కువ సమయంలో పంటలపై ఎరువులు మరియు మందులు పిచికారీ చేస్తూ తోటి రైతులకు సాయపడుతున్నారు.",
                    shortStoryEn = "Trained agri-drone operator assisting regional farmers with precision spraying of organic bio-nutrients and plant protection.",
                    fullStoryTe = "వ్యవసాయంలో ఆధునిక సాంకేతికతను జోడించి లక్ష్మీదేవి గారు డ్రోన్ పిచికారీ పైలట్‌గా ఎదిగారు. శ్రమ తగ్గించి సమయాన్ని పొదుపు చేస్తూ, ఎకరాల కొద్దీ పొలాల్లో సమర్థవంతంగా డ్రోన్ సేవలందిస్తూ స్వయం ఉపాధి పొందుతున్నారు.",
                    fullStoryEn = "Integrating modern technology with agriculture, Lakshmidevi became a certified drone pilot, offering efficient precision spraying services that save time, labor, and input costs for local farmers.",
                    photoUrl = "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?w=600&auto=format&fit=crop&q=60",
                    likeCount = 68,
                    isFeaturedToday = false,
                    isApproved = true,
                    hasConsent = true,
                    submittedBy = "Admin"
                ),
                WomanFarmerStoryEntity(
                    name = "పెద్దింటి అనసూయమ్మ",
                    village = "నార్కట్‌పల్లి, నల్గొండ జిల్లా",
                    cropOrWork = "పశుపోషణ & జీవాల పెంపకం",
                    category = "పశుపోషణ",
                    shortStoryTe = "జీవాల పోషణలో విశేష అనుభవం సంపాదించి, శాస్త్రీయ పద్ధతిలో మేకలు మరియు గొర్రెల పెంపకం ద్వారా కుటుంబాన్ని ఉన్నతంగా తీర్చిదిద్దారు.",
                    shortStoryEn = "Mastered scientific sheep and goat rearing, transforming small livestock farming into a lucrative livelihood for her family.",
                    fullStoryTe = "నల్గొండ జిల్లాకు చెందిన అనసూయమ్మ గారు కరువు ప్రాంతాల్లో కూడా మేకల పెంపకంతో ఆర్థిక భద్రత సాధించారు. వర్షాభావ పరిస్థితుల్లోనూ పశుగ్రాస నిర్వహణ మరియు వ్యాధి నిరోధక టీకాలు సకాలంలో వేయిస్తూ నష్టాలు లేకుండా జీవాల సంరక్షణ చేస్తున్నారు.",
                    fullStoryEn = "Operating in drought-prone conditions, Anasuyamma achieved financial stability through systematic goat rearing, timely vaccinations, and dry fodder storage techniques.",
                    photoUrl = "https://images.unsplash.com/photo-1607746882042-944635d10e46?w=600&auto=format&fit=crop&q=60",
                    likeCount = 31,
                    isFeaturedToday = false,
                    isApproved = true,
                    hasConsent = true,
                    submittedBy = "Admin"
                ),
                WomanFarmerStoryEntity(
                    name = "బాపట్ల సత్యవతి",
                    village = "తాడేపల్లిగూడెం, ఏలూరు సమీపం",
                    cropOrWork = "వ్యవసాయ కూలీ పనులు & మహిళా శ్రమ శక్తి",
                    category = "వ్యవసాయ కార్మికురాలు",
                    shortStoryTe = "30 ఏళ్లుగా ప్రతిరోజూ నారు నాట్లు, కలుపు తీత, కోతల పనుల్లో నిబద్ధతతో పనిచేస్తూ పిల్లలను ఉన్నత చదువులు చదివించారు.",
                    shortStoryEn = "Dedicated 30 years as an agricultural worker in transplanting, weeding, and harvesting, proudly educating her children into professionals.",
                    fullStoryTe = "ఎండ, వాన లెక్కచేయకుండా ప్రతిరోజూ పొలాల్లో చెమటోడ్చే సత్యవతి గారి జీవితం ప్రతి ఒక్కరికీ స్ఫూర్తిదాయకం. మహిళా కూలీల హక్కుల కోసం, సరైన వేతనాల కోసం శ్రమిస్తూ, తన శ్రమ ద్వారా కూతురిని సాఫ్ట్‌వేర్ ఇంజనీర్‌గా తీర్చిదిద్దిన ధీరమాత.",
                    fullStoryEn = "Working tirelessly under sun and rain, Satyavathi's honest manual labor empowered her to put her daughter through engineering college. Her resilience embodies the noble spirit of women agricultural workers.",
                    photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=600&auto=format&fit=crop&q=60",
                    likeCount = 76,
                    isFeaturedToday = false,
                    isApproved = true,
                    hasConsent = true,
                    submittedBy = "Admin"
                ),
                WomanFarmerStoryEntity(
                    name = "నర్సమ్మ గారు",
                    village = "మెదక్ జిల్లా",
                    cropOrWork = "చిరుధాన్యాల సాగు (Millets Cultivation)",
                    category = "మహిళా రైతు",
                    shortStoryTe = "తక్కువ నీటితో పండే కొర్రలు, సామలు, రాగులు సాగుచేస్తూ ఆరోగ్యకరమైన ఆహార ధాన్యాలను సమాజానికి అందిస్తున్నారు.",
                    shortStoryEn = "Cultivates drought-hardy native millets like Foxtail and Finger Millet, promoting nutritional security in her community.",
                    fullStoryTe = "చిరుధాన్యాల విశిష్టతను పునరుద్ధరించడంలో నర్సమ్మ గారు ప్రముఖ పాత్ర పోషించారు. నీటి ఎద్దడి ఉన్న భూముల్లో కూడా కంటికింపైన చిరుధాన్య పంటలు పండిస్తూ, మహిళా రైతు సంఘాల ద్వారా ప్రాసెసింగ్ చేసి విక్రయిస్తున్నారు.",
                    fullStoryEn = "Narsamma revived rainfed millet farming in arid soil, organizing women's self-help groups to value-add and market healthy millet products.",
                    photoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=600&auto=format&fit=crop&q=60",
                    likeCount = 51,
                    isFeaturedToday = false,
                    isApproved = true,
                    hasConsent = true,
                    submittedBy = "Admin"
                )
            )
            dao.insertWomenFarmerStories(initialStories)
        }
    }
}

