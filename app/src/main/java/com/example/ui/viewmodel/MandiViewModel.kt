package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.MandiRateRecord
import com.example.data.repository.MandiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MandiUiState(
    val isLoading: Boolean = false,
    val rates: List<MandiRateRecord> = emptyList(),
    val selectedState: String = "All States",
    val selectedDistrict: String = "All Districts",
    val selectedCommodity: String = "All Commodities",
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val isProxyActive: Boolean = true,
    // Gemini Search Grounding State
    val geminiQuery: String = "",
    val isGeminiLoading: Boolean = false,
    val geminiAdvice: String? = null,
    val geminiSearchQueries: List<String> = emptyList()
)

class MandiViewModel(
    private val repository: MandiRepository = MandiRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MandiUiState())
    val uiState: StateFlow<MandiUiState> = _uiState.asStateFlow()

    val statesList = listOf("All States", "Andhra Pradesh", "Telangana", "Maharashtra", "Madhya Pradesh", "Karnataka")
    val commoditiesList = listOf("All Commodities", "Tomato", "Paddy (Dhan)", "Cotton", "Onion", "Chilli", "Wheat", "Maize")

    init {
        loadMandiRates()
    }

    fun loadMandiRates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val currentState = _uiState.value
            val result = repository.getMandiRates(
                state = currentState.selectedState,
                district = currentState.selectedDistrict,
                commodity = currentState.selectedCommodity
            )

            result.onSuccess { list ->
                val filteredList = if (currentState.searchQuery.isNotBlank()) {
                    list.filter {
                        it.commodity.contains(currentState.searchQuery, ignoreCase = true) ||
                        it.market.contains(currentState.searchQuery, ignoreCase = true) ||
                        it.district.contains(currentState.searchQuery, ignoreCase = true) ||
                        it.state.contains(currentState.searchQuery, ignoreCase = true)
                    }
                } else {
                    list
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    rates = filteredList
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to fetch Mandi rates"
                )
            }
        }
    }

    fun updateStateFilter(state: String) {
        _uiState.value = _uiState.value.copy(selectedState = state)
        loadMandiRates()
    }

    fun updateCommodityFilter(commodity: String) {
        _uiState.value = _uiState.value.copy(selectedCommodity = commodity)
        loadMandiRates()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadMandiRates()
    }

    fun searchWithGeminiGrounding(customPrompt: String? = null) {
        val prompt = customPrompt ?: _uiState.value.geminiQuery
        if (prompt.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGeminiLoading = true,
                geminiAdvice = null,
                geminiSearchQueries = emptyList()
            )

            val result = repository.queryGeminiMarketSearch(prompt)
            result.onSuccess { (advice, queries) ->
                _uiState.value = _uiState.value.copy(
                    isGeminiLoading = false,
                    geminiAdvice = advice,
                    geminiSearchQueries = queries
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isGeminiLoading = false,
                    geminiAdvice = "Error querying AI Market Search: ${error.message}"
                )
            }
        }
    }

    fun updateGeminiQuery(query: String) {
        _uiState.value = _uiState.value.copy(geminiQuery = query)
    }
}
