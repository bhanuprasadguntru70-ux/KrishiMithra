package com.example.calculator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.model.MarketCropEntity
import com.example.data.model.SavedCalculationEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FarmCalculatorsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    private val dao = db.dao()

    val savedCalculations: StateFlow<List<SavedCalculationEntity>> = dao.getAllSavedCalculations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val marketCrops: StateFlow<List<MarketCropEntity>> = dao.getAllMarketCrops()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _appLanguage = MutableStateFlow("en")
    val appLanguage: StateFlow<String> = _appLanguage

    fun setAppLanguage(lang: String) {
        _appLanguage.value = lang
    }

    fun saveCalculation(
        title: String,
        type: String,
        resultFormatted: String,
        summaryDetails: String,
        inputsJson: String = ""
    ) {
        viewModelScope.launch {
            val entity = SavedCalculationEntity(
                title = title,
                calculatorType = type,
                resultFormatted = resultFormatted,
                summaryDetails = summaryDetails,
                inputsJson = inputsJson,
                timestamp = System.currentTimeMillis()
            )
            dao.insertSavedCalculation(entity)
        }
    }

    fun deleteSavedCalculation(id: Int) {
        viewModelScope.launch {
            dao.deleteSavedCalculationById(id)
        }
    }

    fun clearAllCalculations() {
        viewModelScope.launch {
            dao.deleteAllSavedCalculations()
        }
    }
}
