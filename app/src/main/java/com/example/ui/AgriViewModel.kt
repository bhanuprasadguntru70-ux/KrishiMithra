package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.api.Content
import com.example.data.api.Part
import com.example.data.datastore.LanguagePreferencesDataStore
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AgriRepository
import com.example.data.repository.DiseaseClassifierRepository
import com.example.ui.screens.WeatherInfo
import com.example.ui.screens.HourlyForecast
import com.example.ui.screens.DailyForecast
import com.example.data.sync.WeatherSyncWorker
import com.example.util.WeatherNotificationHelper
import com.example.util.WeatherPrefsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AgriViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    private val repository = AgriRepository(db.dao())

    // --- INITIALIZE DATA ---
    init {
        viewModelScope.launch {
            try {
                repository.prepopulateMarketCrops()
                repository.prepopulateNews()
                repository.prepopulateCropCalendars()
                repository.prepopulateWomenFarmerStories()
            } catch (e: Throwable) {
                // Ignore prepopulate errors on startup
            }
            try {
                repository.refreshLiveMarketPrices()
            } catch (e: Throwable) {
                // Ignore network error during startup sync
            }
            try {
                val cached = repository.getLatestFarmingTipSync()
                if (cached != null && _dailyFarmingTip.value == null) {
                    _dailyFarmingTip.value = cached.tipText
                }
            } catch (e: Throwable) {
                // Ignore startup cache read error
            }
            try {
                WeatherSyncWorker.schedulePeriodicWork(application)
            } catch (e: Throwable) {
                // Ignore work manager initialization in testing environment if any
            }
        }
    }

    // --- PERSISTENT LANGUAGE SELECTION (DATASTORE) ---
    private val languageDataStore = LanguagePreferencesDataStore(application)

    val appLanguage: StateFlow<String> = languageDataStore.languageFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = LanguagePreferencesDataStore.LANGUAGE_ENGLISH
    )

    val isTelugu: StateFlow<Boolean> = appLanguage
        .map { it == LanguagePreferencesDataStore.LANGUAGE_TELUGU }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    fun setAppLanguage(languageCode: String) {
        viewModelScope.launch {
            languageDataStore.saveLanguage(languageCode)
            // Synchronize with Weather notification settings preference
            val prefsManager = WeatherPrefsManager(getApplication())
            val settings = prefsManager.getSettings()
            prefsManager.saveSettings(settings.copy(preferredLanguage = languageCode))
        }
    }

    fun toggleLanguage() {
        val current = appLanguage.value
        val next = if (current == LanguagePreferencesDataStore.LANGUAGE_TELUGU) {
            LanguagePreferencesDataStore.LANGUAGE_ENGLISH
        } else {
            LanguagePreferencesDataStore.LANGUAGE_TELUGU
        }
        setAppLanguage(next)
    }

    // --- AUTHENTICATION STATE ---
    val currentUser = repository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _isRegisterSuccess = MutableStateFlow(false)
    val isRegisterSuccess: StateFlow<Boolean> = _isRegisterSuccess.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginError.value = null
            if (email.isBlank() || password.isBlank()) {
                _loginError.value = "Email and Password cannot be empty."
                return@launch
            }
            // Simply log in with standard validation
            val success = repository.loginUser(email, password)
            if (!success) {
                // Register user dynamically if login credentials don't exist yet
                val newUser = UserEntity(
                    email = email,
                    name = email.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }.ifBlank { "Farmer" },
                    phone = "",
                    district = "",
                    state = "",
                    language = "English",
                    isLoggedIn = true,
                    landHolding = null,
                    primaryCrops = null
                )
                repository.registerUser(newUser)
                _isRegisterSuccess.value = true
            }
        }
    }

    fun register(
        name: String,
        email: String,
        phone: String = "",
        district: String = "",
        state: String = "",
        language: String = "English",
        landHolding: String? = null,
        primaryCrops: String? = null
    ) {
        viewModelScope.launch {
            _loginError.value = null
            _isRegisterSuccess.value = false
            if (name.isBlank() || email.isBlank()) {
                _loginError.value = "Name and Email are required."
                return@launch
            }
            val newUser = UserEntity(
                email = email,
                name = name,
                phone = phone,
                district = district,
                state = state,
                language = language,
                landHolding = landHolding,
                primaryCrops = primaryCrops,
                isLoggedIn = true
            )
            repository.registerUser(newUser)
            _isRegisterSuccess.value = true
        }
    }

    fun resetRegisterSuccess() {
        _isRegisterSuccess.value = false
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun updateProfile(user: UserEntity) {
        viewModelScope.launch {
            repository.updateUserProfile(user)
        }
    }

    // --- FARMS MANAGEMENT ---
    val allFarms = repository.allFarms.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addFarm(crop: String, area: String, soil: String, plantingDate: String, expectedHarvest: String) {
        viewModelScope.launch {
            val farm = FarmEntity(
                crop = crop,
                area = area,
                soil = soil,
                plantingDate = plantingDate,
                expectedHarvest = expectedHarvest
            )
            repository.addFarm(farm)
        }
    }

    fun removeFarm(farm: FarmEntity) {
        viewModelScope.launch {
            repository.removeFarm(farm)
        }
    }

    // --- CROP CALENDAR MANAGEMENT ---
    val allCropCalendars = repository.allCropCalendars.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedCalendarId = MutableStateFlow<Int?>(null)
    val selectedCalendarId: StateFlow<Int?> = _selectedCalendarId.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedCalendarStages: StateFlow<List<CropStageEntity>> = _selectedCalendarId
        .flatMapLatest { id ->
            if (id != null) repository.getStagesForCalendar(id)
            else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedCalendarTasks: StateFlow<List<CropTaskReminderEntity>> = _selectedCalendarId
        .flatMapLatest { id ->
            if (id != null) repository.getTasksForCalendar(id)
            else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectCropCalendar(id: Int?) {
        _selectedCalendarId.value = id
    }

    private val _isCreatingCalendar = MutableStateFlow(false)
    val isCreatingCalendar: StateFlow<Boolean> = _isCreatingCalendar.asStateFlow()

    fun createCropCalendar(
        farmName: String,
        cropName: String,
        cropNameTe: String = "",
        variety: String = "",
        farmArea: String = "1 Acre",
        soilType: String = "Black Soil",
        plantingDate: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            _isCreatingCalendar.value = true
            try {
                val newId = repository.generateAndSaveCropCalendar(
                    farmName = farmName,
                    cropName = cropName,
                    cropNameTe = cropNameTe,
                    variety = variety,
                    farmArea = farmArea,
                    soilType = soilType,
                    plantingDate = plantingDate,
                    notes = notes
                )
                _selectedCalendarId.value = newId.toInt()
            } catch (e: Exception) {
                // handle error
            } finally {
                _isCreatingCalendar.value = false
            }
        }
    }

    fun toggleTaskCompletion(task: CropTaskReminderEntity) {
        viewModelScope.launch {
            val updated = task.copy(isCompleted = !task.isCompleted)
            repository.updateTaskReminder(updated)
        }
    }

    fun toggleTaskReminder(task: CropTaskReminderEntity) {
        viewModelScope.launch {
            val updated = task.copy(isReminderEnabled = !task.isReminderEnabled)
            repository.updateTaskReminder(updated)
        }
    }

    fun addCustomTask(
        calendarId: Int,
        taskTitle: String,
        taskTitleTe: String = "",
        category: String,
        categoryTe: String = "",
        dueDate: String,
        instructions: String = "",
        instructionsTe: String = "",
        priority: String = "High"
    ) {
        viewModelScope.launch {
            val task = CropTaskReminderEntity(
                calendarId = calendarId,
                taskTitle = taskTitle,
                taskTitleTe = taskTitleTe,
                category = category,
                categoryTe = categoryTe,
                dueDate = dueDate,
                dayOffset = 0,
                instructions = instructions,
                instructionsTe = instructionsTe,
                priority = priority
            )
            repository.addCustomTask(task)
        }
    }

    fun deleteCropCalendar(calendar: CropCalendarEntity) {
        viewModelScope.launch {
            if (_selectedCalendarId.value == calendar.id) {
                _selectedCalendarId.value = null
            }
            repository.deleteCropCalendar(calendar)
        }
    }

    // --- DISEASE DETECTION ---
    val allReports = repository.allReports.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val diseaseClassifier by lazy { DiseaseClassifierRepository(getApplication()) }

    private val _diseaseResult = MutableStateFlow<DiseaseReportEntity?>(null)
    val diseaseResult: StateFlow<DiseaseReportEntity?> = _diseaseResult.asStateFlow()

    private val _isAnalyzingDisease = MutableStateFlow(false)
    val isAnalyzingDisease: StateFlow<Boolean> = _isAnalyzingDisease.asStateFlow()

    private val _lastClassificationEngine = MutableStateFlow("TensorFlow Lite v2.14 On-Device")
    val lastClassificationEngine: StateFlow<String> = _lastClassificationEngine.asStateFlow()

    private val _lastExecutionTimeMs = MutableStateFlow(0L)
    val lastExecutionTimeMs: StateFlow<Long> = _lastExecutionTimeMs.asStateFlow()

    fun analyzeDiseaseImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _isAnalyzingDisease.value = true
            _diseaseResult.value = null
            try {
                // 1. Instant local TensorFlow Lite classification
                val tfLiteResult = diseaseClassifier.classifyCropDisease(bitmap)
                _lastClassificationEngine.value = tfLiteResult.engineName
                _lastExecutionTimeMs.value = tfLiteResult.executionTimeMs

                val tfLiteReport = diseaseClassifier.toEntity(tfLiteResult)
                _diseaseResult.value = tfLiteReport

                // 2. Multimodal enhancement check if Gemini online API is reachable
                try {
                    val rawText = repository.analyzeCropDisease(bitmap)
                    if (!rawText.startsWith("Error") && !rawText.startsWith("API Error") && rawText.isNotBlank()) {
                        val parsedReport = parseDiseaseReportText(rawText)
                        _diseaseResult.value = parsedReport.copy(
                            confidenceScore = if (parsedReport.confidenceScore > 0) parsedReport.confidenceScore else tfLiteReport.confidenceScore
                        )
                    }
                } catch (e: Exception) {
                    // Retain TensorFlow Lite report which is already rendered
                }
            } catch (e: Exception) {
                _diseaseResult.value = DiseaseReportEntity(
                    cropName = "Crop (Analysis Failed)",
                    diseaseName = "Error Analyzing Image",
                    confidenceScore = 0.0,
                    cause = e.message ?: "Unknown error",
                    symptoms = "Could not process plant leaf symptoms.",
                    treatment = "Please check picture clarity.",
                    organicSolution = "N/A",
                    chemicalSolution = "N/A",
                    preventiveMeasures = "Retry with a clear photo of leaf.",
                    nearbyOffice = "Contact local Agriculture Extension Officer."
                )
            } finally {
                _isAnalyzingDisease.value = false
            }
        }
    }

    fun saveDiseaseReport(report: DiseaseReportEntity) {
        viewModelScope.launch {
            repository.saveReport(report)
        }
    }

    fun deleteReport(id: Int) {
        viewModelScope.launch {
            repository.deleteReport(id)
        }
    }

    private fun parseDiseaseReportText(text: String): DiseaseReportEntity {
        var cropName = "Paddy"
        var diseaseName = "Leaf Blast"
        var confidenceScore = 92.0
        var cause = "Fungus Magnaporthe oryzae"
        var symptoms = "Spindle-shaped lesions with grayish centers on leaves."
        var treatment = "Apply recommended systemic fungicides immediately."
        var organicSolution = "Foliar spray of 5% Neem Seed Kernel Extract (NSKE)."
        var chemicalSolution = "Spray Tricyclazole 75 WP @ 0.6 g/liter of water."
        var preventiveMeasures = "Use resistant varieties; avoid excessive nitrogen fertilizers."
        var nearbyOffice = "Kisan Call Centre or nearest KVK Anantapur."

        val lines = text.lines()
        for (line in lines) {
            val parts = line.split(":", limit = 2)
            if (parts.size < 2) continue
            val key = parts[0].replace("*", "").replace("-", "").replace("_", "").trim().lowercase()
            val value = parts[1].replace("**", "").replace("*", "").trim().removeSurrounding("\"").removePrefix(":").trim()

            when {
                key.contains("crop") -> cropName = value
                key.contains("disease") -> diseaseName = value
                key.contains("confidence") -> {
                    confidenceScore = value.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 90.0
                }
                key.contains("cause") -> cause = value
                key.contains("symptom") -> symptoms = value
                key.contains("treatment") -> treatment = value
                key.contains("organic") -> organicSolution = value
                key.contains("chemical") -> chemicalSolution = value
                key.contains("prevent") || key.contains("measure") -> preventiveMeasures = value
                key.contains("office") || key.contains("nearby") -> nearbyOffice = value
            }
        }

        return DiseaseReportEntity(
            cropName = cropName,
            diseaseName = diseaseName,
            confidenceScore = confidenceScore,
            cause = cause,
            symptoms = symptoms,
            treatment = treatment,
            organicSolution = organicSolution,
            chemicalSolution = chemicalSolution,
            preventiveMeasures = preventiveMeasures,
            nearbyOffice = nearbyOffice
        )
    }

    // --- CROP RECOMMENDATION ---
    private val _cropRecommendation = MutableStateFlow<String?>(null)
    val cropRecommendation: StateFlow<String?> = _cropRecommendation.asStateFlow()

    private val _isGeneratingCrop = MutableStateFlow(false)
    val isGeneratingCrop: StateFlow<Boolean> = _isGeneratingCrop.asStateFlow()

    fun generateCropRecommendation(
        soilType: String,
        state: String,
        district: String,
        season: String,
        waterAvailability: String,
        farmSize: String
    ) {
        viewModelScope.launch {
            _isGeneratingCrop.value = true
            _cropRecommendation.value = null
            val result = repository.getCropRecommendation(soilType, state, district, season, waterAvailability, farmSize)
            _cropRecommendation.value = result
            _isGeneratingCrop.value = false

            // Save notification alert
            repository.addNotification(
                NotificationAlertEntity(
                    title = "New Crop Advice Generated",
                    message = "Recommended crop for soil $soilType in $district season $season is ready.",
                    category = "Reminder"
                )
            )
        }
    }

    // --- FERTILIZER RECOMMENDATION ---
    private val _fertilizerRecommendation = MutableStateFlow<String?>(null)
    val fertilizerRecommendation: StateFlow<String?> = _fertilizerRecommendation.asStateFlow()

    private val _isGeneratingFertilizer = MutableStateFlow(false)
    val isGeneratingFertilizer: StateFlow<Boolean> = _isGeneratingFertilizer.asStateFlow()

    fun generateFertilizerRecommendation(
        crop: String,
        disease: String,
        soil: String,
        growthStage: String
    ) {
        viewModelScope.launch {
            _isGeneratingFertilizer.value = true
            _fertilizerRecommendation.value = null
            val result = repository.getFertilizerRecommendation(crop, disease, soil, growthStage)
            _fertilizerRecommendation.value = result
            _isGeneratingFertilizer.value = false

            // Save notification alert
            repository.addNotification(
                NotificationAlertEntity(
                    title = "Fertilizer Schedule Ready",
                    message = "Tailored fertilizer suggestion for $crop ($growthStage stage) is prepared.",
                    category = "Reminder"
                )
            )
        }
    }

    // --- CROP YIELD PREDICTION ---
    private val _yieldPrediction = MutableStateFlow<String?>(null)
    val yieldPrediction: StateFlow<String?> = _yieldPrediction.asStateFlow()

    private val _isPredictingYield = MutableStateFlow(false)
    val isPredictingYield: StateFlow<Boolean> = _isPredictingYield.asStateFlow()

    fun predictCropYield(
        crop: String,
        area: String,
        soil: String,
        plantingDate: String,
        state: String,
        district: String
    ) {
        viewModelScope.launch {
            _isPredictingYield.value = true
            _yieldPrediction.value = null
            val result = repository.getCropYieldPrediction(crop, area, soil, plantingDate, state, district)
            _yieldPrediction.value = result
            _isPredictingYield.value = false

            // Save notification alert
            repository.addNotification(
                NotificationAlertEntity(
                    title = "Crop Yield Forecast Ready",
                    message = "Yield forecast analysis for your $crop has been calculated.",
                    category = "Reminder"
                )
            )
        }
    }

    fun clearYieldPrediction() {
        _yieldPrediction.value = null
    }

    // --- PREMIUM SUBSCRIPTION & BILLING STATE ---
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userTransactions: StateFlow<List<TransactionEntity>> = currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getAllTransactions(user.email)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun subscribeUser(planName: String, amount: Double, paymentMethod: String, orderId: String, status: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            
            // 1. Create and save transaction
            val transaction = TransactionEntity(
                userEmail = user.email,
                orderId = orderId,
                amount = amount,
                planName = planName,
                paymentMethod = paymentMethod,
                status = status
            )
            repository.addTransaction(transaction)

            // 2. If payment was successful, unlock premium status!
            if (status == "SUCCESS") {
                val expiryDuration = when (planName) {
                    "Monthly Premium" -> 30L * 24L * 60L * 60L * 1000L // 30 days
                    "Yearly Premium" -> 365L * 24L * 60L * 60L * 1000L // 365 days
                    "Lifetime Pro" -> 100L * 365L * 24L * 60L * 60L * 1000L // ~100 years
                    else -> 0L
                }
                
                val updatedUser = user.copy(
                    isPremium = true,
                    subscriptionPlan = planName,
                    subscriptionExpiry = System.currentTimeMillis() + expiryDuration
                )
                repository.updateUserProfile(updatedUser)

                // 3. Insert notification alert
                repository.addNotification(
                    NotificationAlertEntity(
                        title = "Premium Subscription Activated! 🎉",
                        message = "Thank you for subscribing to $planName. All premium AI features are now unlocked!",
                        category = "Reminder"
                    )
                )
            } else {
                repository.addNotification(
                    NotificationAlertEntity(
                        title = "Payment Attempt Failed ❌",
                        message = "Your payment of ₹$amount for $planName has failed. If money was debited, it will be refunded shortly.",
                        category = "Reminder"
                    )
                )
            }
        }
    }

    fun cancelSubscription() {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val updatedUser = user.copy(
                isPremium = false,
                subscriptionPlan = "Free",
                subscriptionExpiry = 0L
            )
            repository.updateUserProfile(updatedUser)
            repository.addNotification(
                NotificationAlertEntity(
                    title = "Subscription Cancelled",
                    message = "Your premium benefits have been disabled. We hope to see you back soon!",
                    category = "Reminder"
                )
            )
        }
    }

    // --- MARKET PRICES & SEARCH ---
    val allMarketCrops = repository.allMarketCrops.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun refreshLiveMarketPrices(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                repository.refreshLiveMarketPrices()
            } catch (e: Exception) {
                // Ignore exception
            } finally {
                onComplete?.invoke()
            }
        }
    }

    private val _marketSearchQuery = MutableStateFlow("")
    val marketSearchQuery: StateFlow<String> = _marketSearchQuery.asStateFlow()

    fun searchMarketCrops(query: String) {
        _marketSearchQuery.value = query
    }

    fun toggleCropFavorite(crop: MarketCropEntity) {
        viewModelScope.launch {
            repository.updateMarketCrop(crop.copy(isFavorite = !crop.isFavorite))
        }
    }

    fun setCropPriceAlert(crop: MarketCropEntity, enabled: Boolean, targetPrice: Double) {
        viewModelScope.launch {
            repository.updateMarketCrop(crop.copy(priceAlertEnabled = enabled, alertPrice = targetPrice))
            if (enabled) {
                repository.addNotification(
                    NotificationAlertEntity(
                        title = "Price Alert Configured",
                        message = "Alert set for ${crop.cropName} when price reaches ₹$targetPrice.",
                        category = "Market"
                    )
                )
            }
        }
    }

    fun updateMarketCrop(crop: MarketCropEntity) {
        viewModelScope.launch {
            repository.updateMarketCrop(crop)
        }
    }

    // --- NEWS AND SCHEMES ---
    val allNews = repository.allNews.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun createAdminNews(title: String, content: String, category: String, videoUrl: String? = null) {
        viewModelScope.launch {
            val news = NewsEntity(title = title, content = content, category = category, videoUrl = videoUrl)
            repository.addNews(news)
        }
    }

    // --- NOTIFICATIONS & ALERTS ---
    val allNotifications = repository.allNotifications.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun markNotificationRead(id: Int) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    // --- AI CHATBOT SYSTEM ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("Namaste! I am Agri AI. How can I help you today? You can ask me about soil health, plant diseases, fertilizer use, or crop prices.", false)
    ))
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    fun sendMessage(text: String, language: String, image: Bitmap? = null) {
        if (text.isBlank() && image == null) return
        val userMsg = ChatMessage(text, true, image = image)
        _chatMessages.value = _chatMessages.value + userMsg
        _isChatLoading.value = true

        viewModelScope.launch {
            // Map our chat message structures to Content / Part for Gemini REST
            val history = _chatMessages.value.dropLast(1).map { msg ->
                val partsList = mutableListOf<Part>()
                if (msg.text.isNotBlank()) {
                    partsList.add(Part(text = msg.text))
                }
                if (msg.image != null) {
                    // Send previous images as inline data if history keeps them,
                    // but for text-only history to save context tokens we can just map text.
                    // Let's do simple mapping
                }
                Content(
                    parts = partsList,
                    role = if (msg.isUser) "user" else "model"
                )
            }

            try {
                val response = repository.chatWithAI(text, language, history, image)
                _chatMessages.value = _chatMessages.value + ChatMessage(response, false)
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + ChatMessage("Sorry, I could not reach my brain right now. Please check your connectivity: ${e.message}", false)
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage("Namaste! Chat history cleared. How can I help you now?", false)
        )
    }

    // --- LOCAL SETTINGS & GENERAL STATE ---
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun toggleNotifications() {
        _notificationsEnabled.value = !_notificationsEnabled.value
    }

    // --- DAILY FARMING TIPS ---
    private val _dailyFarmingTip = MutableStateFlow<String?>(null)
    val dailyFarmingTip: StateFlow<String?> = _dailyFarmingTip.asStateFlow()

    private val _isTipLoading = MutableStateFlow(false)
    val isTipLoading: StateFlow<Boolean> = _isTipLoading.asStateFlow()

    fun fetchDailyFarmingTip(district: String, state: String, forceRefresh: Boolean = false) {
        if (!forceRefresh && _dailyFarmingTip.value != null) return
        _isTipLoading.value = true
        viewModelScope.launch {
            val calendar = java.util.Calendar.getInstance()
            val month = calendar.get(java.util.Calendar.MONTH)
            val monthName = calendar.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.LONG, java.util.Locale.ENGLISH) ?: "Current Month"
            
            val season = when (month) {
                in 5..9 -> "Kharif (Monsoon) Season"
                in 10..11, in 0..1 -> "Rabi (Winter) Season"
                else -> "Zaid (Summer) Season"
            }

            try {
                val prompt = """
                    Provide 1 high-quality, actionable, highly-specific daily farming tip for a farmer located in $district, $state, India.
                    The current season is $season (Month: $monthName).
                    Base the tip on realistic crop cycles (e.g. rice, cotton, sugarcane, maize, groundnut or seasonal vegetables) during this season.
                    The tip must be concise (2-3 sentences), highly practical, and focus on soil preparation, irrigation, fertilizer timing, pest management, or harvesting.
                    Format the response as:
                    
                    **[Crop/Focus]**: [The actual practical tip]
                """.trimIndent()

                val systemInstruction = """
                    You are an expert Indian agricultural advisor and agronomist. 
                    Provide precise, practical, and highly localized advice for the specified region and season. 
                    Be encouraging, professional, and clear. Do not use generic filler words.
                """.trimIndent()

                val response = com.example.data.api.GeminiHelper.generateResponse(prompt, systemInstruction)
                if (response.startsWith("Error:") || response.startsWith("API Error:") || response.isBlank()) {
                    val cached = repository.getLatestFarmingTipSync()
                    if (cached != null) {
                        _dailyFarmingTip.value = cached.tipText
                    } else {
                        _dailyFarmingTip.value = getDefaultTip(season)
                    }
                } else {
                    _dailyFarmingTip.value = response
                    // Save to Room cache
                    repository.saveFarmingTip(FarmingTipEntity(tipText = response, district = district, state = state))
                }
            } catch (e: Exception) {
                val cached = repository.getLatestFarmingTipSync()
                if (cached != null) {
                    _dailyFarmingTip.value = cached.tipText
                } else {
                    _dailyFarmingTip.value = "Tip of the Day: Keep your fields well-drained during monsoon showers to prevent waterlogging and root rot."
                }
            } finally {
                _isTipLoading.value = false
            }
        }
    }

    private fun getDefaultTip(season: String): String {
        return when {
            season.contains("Kharif") -> "**Monsoon Drainage**: Ensure your fields (especially cotton and maize) have clean drainage channels to prevent waterlogging during heavy downpours, which can cause root damage."
            season.contains("Rabi") -> "**Moisture Conservation**: Apply a light layer of organic mulch around young wheat or gram crops to conserve soil moisture and protect roots from cold winter mornings."
            else -> "**Summer Irrigation**: Program drip irrigation for early morning hours to minimize water loss from evaporation under the midday sun and protect young seedlings."
        }
    }

    // --- LIVE WEATHER STATE & FETCHING ---
    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()

    fun fetchWeather(query: String, lang: String = "en", forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _weatherState.value = WeatherState.Loading
            try {
                val weather = repository.getLiveWeather(query, lang)
                _weatherState.value = WeatherState.Success(weather)
            } catch (e: Exception) {
                _weatherState.value = WeatherState.Error(e.message ?: "Unable to fetch live weather. Please try again.")
            }
        }
    }

    fun fetchWeatherForGps(lat: Double, lon: Double, lang: String = "en") {
        viewModelScope.launch {
            _weatherState.value = WeatherState.Loading
            try {
                val weather = repository.getLiveWeatherForGps(lat, lon, getApplication(), lang)
                _weatherState.value = WeatherState.Success(weather)

                // Sync FCM topics & save last known location for background workers
                val prefsManager = WeatherPrefsManager(getApplication())
                prefsManager.saveLastKnownLocation(lat, lon, weather.locationName)
                WeatherNotificationHelper.syncFcmTopics(getApplication(), lat, lon, weather.district)
            } catch (e: Exception) {
                _weatherState.value = WeatherState.Error(e.message ?: "Unable to fetch live weather. Please try again.")
            }
        }
    }

    fun updateWeatherNotificationSettings(settings: WeatherNotificationSettings) {
        viewModelScope.launch {
            val prefsManager = WeatherPrefsManager(getApplication())
            prefsManager.saveSettings(settings)
            val lat = if (settings.locationMode == "GPS") prefsManager.getLastKnownLat() else settings.manualLat
            val lon = if (settings.locationMode == "GPS") prefsManager.getLastKnownLon() else settings.manualLon
            val district = if (settings.locationMode == "GPS") prefsManager.getSettings().manualDistrict else settings.manualDistrict
            WeatherNotificationHelper.syncFcmTopics(getApplication(), lat, lon, district)
        }
    }

    // --- WOMEN FARMER STORIES ("మా మహిళా రైతుల స్ఫూర్తి కథలు") ---
    val approvedWomenFarmerStories: StateFlow<List<WomanFarmerStoryEntity>> = repository.allApprovedWomenFarmerStories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val pendingWomenFarmerStories: StateFlow<List<WomanFarmerStoryEntity>> = repository.pendingWomenFarmerStories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val featuredTodayStory: StateFlow<WomanFarmerStoryEntity?> = repository.featuredTodayStory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun submitWomanFarmerStory(story: WomanFarmerStoryEntity, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repository.submitWomanFarmerStory(story)
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    fun incrementStoryLike(id: Int) {
        viewModelScope.launch {
            repository.incrementStoryLike(id)
        }
    }

    fun approveStory(id: Int) {
        viewModelScope.launch {
            repository.approveStory(id)
        }
    }

    fun deleteStory(story: WomanFarmerStoryEntity) {
        viewModelScope.launch {
            repository.deleteStory(story)
        }
    }

    fun setFeaturedStory(id: Int) {
        viewModelScope.launch {
            repository.setFeaturedStory(id)
        }
    }

    fun cycleNextFeaturedStory() {
        viewModelScope.launch {
            val list = repository.getApprovedStoriesListOnce()
            if (list.isNotEmpty()) {
                val current = _featuredIndex.value
                val nextIndex = (current + 1) % list.size
                _featuredIndex.value = nextIndex
                val nextStory = list[nextIndex]
                repository.setFeaturedStory(nextStory.id)
            }
        }
    }

    private val _featuredIndex = MutableStateFlow(0)
}


sealed class WeatherState {
    object Loading : WeatherState()
    data class Success(val weatherInfo: WeatherInfo) : WeatherState()
    data class Error(val message: String) : WeatherState()
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val image: Bitmap? = null
)
