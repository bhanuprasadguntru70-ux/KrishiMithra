package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.models.CalculationHistoryItem
import com.example.calculator.models.SavedCalculation
import com.example.data.model.*
import com.example.data.repository.KrishiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class KrishiUiState(
    val currentTab: Int = 0, // 0: Home, 1: Markets, 2: Sell, 3: My Book, 4: Profile, 5: Calculators Portal
    val selectedLocation: String = "Eluru, Andhra Pradesh",
    val selectedLanguage: String = "English", // "English" or "తెలుగు"
    val isLoggedIn: Boolean = false,
    val userName: String = "Farmer",
    val unreadNotifications: Int = 3,
    val searchQuery: String = "",
    val mandiRates: List<MandiCropRate> = emptyList(),
    val filteredMandiRates: List<MandiCropRate> = emptyList(),
    val weather: WeatherInfo = WeatherInfo(),
    val farmTools: List<FarmToolItem> = emptyList(),
    val moneyBookEntries: List<MoneyBookEntry> = emptyList(),
    val cropListings: List<CropListing> = emptyList(),
    val farmerStories: List<FarmerStory> = emptyList(),
    val newsItems: List<AgriNewsItem> = emptyList(),
    // Saved Calculations & History
    val savedCalculations: List<SavedCalculation> = listOf(
        SavedCalculation(
            id = "1",
            title = "My Maize Cultivation Cost",
            calculatorName = "Crop Cultivation Cost Calculator",
            date = "04 Aug 2026",
            summary = "₹48,500 Total Cost",
            details = mapOf("Area" to "3.5 Acres", "Cost/Acre" to "₹13,857")
        ),
        SavedCalculation(
            id = "2",
            title = "Tractor Plowing Expense",
            calculatorName = "Tractor Work Calculator",
            date = "03 Aug 2026",
            summary = "₹4,400 Total",
            details = mapOf("Rate" to "₹800/hr", "Hours" to "5h 30m")
        )
    ),
    val calculationHistory: List<CalculationHistoryItem> = listOf(
        CalculationHistoryItem("101", "Bag → Rupees Calculator", "05 Aug 2026", "50 bags (20 qtl)", "₹50,000"),
        CalculationHistoryItem("102", "Labour Wage Calculator", "04 Aug 2026", "5 workers x 2 days", "₹5,000")
    ),
    val activeCalculatorId: String? = null,
    // Active Modal / Dialog States
    val activeTool: FarmToolItem? = null,
    val isVoiceAssistantActive: Boolean = false,
    val isLocationSelectorActive: Boolean = false,
    val isNotificationsActive: Boolean = false,
    val isAgriDoctorActive: Boolean = false,
    val isSellCropModalActive: Boolean = false,
    val isAddBookEntryActive: Boolean = false,
    val isWeatherDetailActive: Boolean = false,
    val isStoriesDetailActive: Boolean = false,
    val isNewsDetailActive: Boolean = false,
    // Voice Assistant Output
    val voiceTranscript: String = "",
    val voiceResponse: String = "",
    // Disease Scanner State
    val diseaseScanResult: String? = null,
    val isScanningLeaf: Boolean = false
)

class KrishiViewModel(
    private val repository: KrishiRepository = KrishiRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(KrishiUiState())
    val uiState: StateFlow<KrishiUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val rates = repository.getMandiRates()
        _uiState.value = _uiState.value.copy(
            mandiRates = rates,
            filteredMandiRates = rates,
            farmTools = repository.getFarmTools(),
            moneyBookEntries = repository.getInitialMoneyBook(),
            cropListings = repository.getCropListings(),
            farmerStories = repository.getFarmerStories(),
            newsItems = repository.getAgriNews()
        )
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(currentTab = index)
    }

    fun updateLanguage(lang: String) {
        _uiState.value = _uiState.value.copy(selectedLanguage = lang)
    }

    fun updateLocation(loc: String) {
        _uiState.value = _uiState.value.copy(
            selectedLocation = loc,
            isLocationSelectorActive = false
        )
    }

    fun updateSearchQuery(query: String) {
        val allRates = _uiState.value.mandiRates
        val filtered = if (query.isBlank()) {
            allRates
        } else {
            allRates.filter {
                it.commodity.contains(query, ignoreCase = true) ||
                it.market.contains(query, ignoreCase = true) ||
                it.state.contains(query, ignoreCase = true)
            }
        }
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredMandiRates = filtered
        )
    }

    fun openTool(tool: FarmToolItem?) {
        _uiState.value = _uiState.value.copy(activeTool = tool)
    }

    fun toggleVoiceAssistant(active: Boolean) {
        _uiState.value = _uiState.value.copy(
            isVoiceAssistantActive = active,
            voiceTranscript = if (active) "నమస్కారం! మీ పంట లేదా మార్కెట్ ధర గురించి అడగండి..." else "",
            voiceResponse = if (active) "నమస్తే రైతు సోదరా! ఏలూరు మార్కెట్లో మిర్చి, టమోటా, వరి ధరణుల వివరాలు సిద్ధంగా ఉన్నాయి." else ""
        )
    }

    fun toggleLocationSelector(active: Boolean) {
        _uiState.value = _uiState.value.copy(isLocationSelectorActive = active)
    }

    fun toggleNotifications(active: Boolean) {
        _uiState.value = _uiState.value.copy(isNotificationsActive = active)
    }

    fun toggleAgriDoctor(active: Boolean) {
        _uiState.value = _uiState.value.copy(
            isAgriDoctorActive = active,
            diseaseScanResult = null
        )
    }

    fun scanLeafImage() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanningLeaf = true)
            kotlinx.coroutines.delay(1200)
            _uiState.value = _uiState.value.copy(
                isScanningLeaf = false,
                diseaseScanResult = "🌱 Detected: Tomato Early Blight (Alternaria solani)\n\n" +
                        "• Symptoms: Concentric brown spot rings on lower leaves.\n" +
                        "• Treatment Spray: Mancozeb 75% WP @ 2.5g/L water or Copper Oxychloride.\n" +
                        "• Prevention: Ensure proper row spacing and clean field drainage."
            )
        }
    }

    fun toggleSellCropModal(active: Boolean) {
        _uiState.value = _uiState.value.copy(isSellCropModalActive = active)
    }

    fun postCropListing(cropName: String, qtl: Int, price: Double, phone: String) {
        val currentName = if (_uiState.value.isLoggedIn) _uiState.value.userName else "Farmer"
        val newListing = CropListing(
            id = System.currentTimeMillis().toString(),
            sellerName = currentName,
            location = _uiState.value.selectedLocation,
            cropName = cropName,
            quantityQtl = qtl,
            pricePerQtl = price,
            phone = phone,
            datePosted = "05 Aug 2026"
        )
        val updated = listOf(newListing) + _uiState.value.cropListings
        _uiState.value = _uiState.value.copy(
            cropListings = updated,
            isSellCropModalActive = false
        )
    }

    fun addMoneyBookEntry(
        title: String,
        amount: Double,
        isIncome: Boolean,
        category: String,
        cropName: String = "Paddy",
        fieldName: String = "North Field (2.5 Ac)",
        notes: String = ""
    ) {
        val newEntry = MoneyBookEntry(
            id = System.currentTimeMillis().toString(),
            title = title,
            amount = amount,
            isIncome = isIncome,
            category = category,
            date = "05 Aug 2026",
            notes = notes,
            cropName = cropName,
            fieldName = fieldName
        )
        val updated = listOf(newEntry) + _uiState.value.moneyBookEntries
        _uiState.value = _uiState.value.copy(
            moneyBookEntries = updated,
            isAddBookEntryActive = false
        )
    }

    fun deleteMoneyBookEntry(id: String) {
        val updated = _uiState.value.moneyBookEntries.filter { it.id != id }
        _uiState.value = _uiState.value.copy(moneyBookEntries = updated)
    }

    fun toggleAddBookEntry(active: Boolean) {
        _uiState.value = _uiState.value.copy(isAddBookEntryActive = active)
    }

    fun toggleWeatherDetail(active: Boolean) {
        _uiState.value = _uiState.value.copy(isWeatherDetailActive = active)
    }

    fun toggleStoriesDetail(active: Boolean) {
        _uiState.value = _uiState.value.copy(isStoriesDetailActive = active)
    }

    fun toggleNewsDetail(active: Boolean) {
        _uiState.value = _uiState.value.copy(isNewsDetailActive = active)
    }

    fun saveCalculation(calc: SavedCalculation) {
        val updated = listOf(calc) + _uiState.value.savedCalculations
        _uiState.value = _uiState.value.copy(savedCalculations = updated)
    }

    fun deleteSavedCalculation(id: String) {
        val updated = _uiState.value.savedCalculations.filter { it.id != id }
        _uiState.value = _uiState.value.copy(savedCalculations = updated)
    }

    fun openCalculatorPortal(toolId: String? = null) {
        _uiState.value = _uiState.value.copy(
            currentTab = 5,
            activeCalculatorId = toolId
        )
    }

    fun closeCalculatorPortal() {
        _uiState.value = _uiState.value.copy(activeCalculatorId = null)
    }
}
