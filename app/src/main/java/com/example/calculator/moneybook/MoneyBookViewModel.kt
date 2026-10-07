package com.example.calculator.moneybook

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.model.ExpenseEntryEntity
import com.example.data.model.FarmerFieldEntity
import com.example.data.model.IncomeEntryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MoneyBookViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    private val dao = db.dao()

    val activeUserEmail = MutableStateFlow("farmer_default@krishimithra.org")
    val farmerProfileName = MutableStateFlow("Bhanu Prasad")

    fun setActiveUser(email: String, name: String) {
        activeUserEmail.value = email.ifEmpty { "farmer_default@krishimithra.org" }
        farmerProfileName.value = name.ifEmpty { "Farmer" }
    }

    val expenses: StateFlow<List<ExpenseEntryEntity>> = activeUserEmail
        .flatMapLatest { email -> dao.getAllExpensesForUser(email) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val incomeList: StateFlow<List<IncomeEntryEntity>> = activeUserEmail
        .flatMapLatest { email -> dao.getAllIncomeForUser(email) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val fields: StateFlow<List<FarmerFieldEntity>> = activeUserEmail
        .flatMapLatest { email -> dao.getAllFieldsForUser(email) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Language state
    private val _appLanguage = MutableStateFlow("en")
    val appLanguage: StateFlow<String> = _appLanguage

    fun setAppLanguage(lang: String) {
        _appLanguage.value = lang
    }

    // Add Expense
    fun addExpense(
        category: String,
        description: String,
        amount: Double,
        crop: String,
        fieldName: String = "",
        dateStr: String = getCurrentDateFormatted(),
        receiptNote: String = ""
    ) {
        viewModelScope.launch {
            val entity = ExpenseEntryEntity(
                userEmail = activeUserEmail.value,
                date = dateStr.ifEmpty { getCurrentDateFormatted() },
                category = category,
                description = description,
                amount = amount,
                crop = crop,
                fieldName = fieldName,
                receiptNote = receiptNote
            )
            dao.insertExpense(entity)
        }
    }

    // Delete Expense
    fun deleteExpense(id: Int) {
        viewModelScope.launch {
            dao.deleteExpenseById(id)
        }
    }

    // Add Income
    fun addIncome(
        cropSold: String,
        quantity: Double,
        unit: String,
        pricePerUnit: Double,
        totalAmount: Double,
        buyerName: String = "",
        marketName: String = "",
        fieldName: String = "",
        dateStr: String = getCurrentDateFormatted(),
        notes: String = ""
    ) {
        viewModelScope.launch {
            val entity = IncomeEntryEntity(
                userEmail = activeUserEmail.value,
                cropSold = cropSold,
                quantity = quantity,
                unit = unit,
                pricePerUnit = pricePerUnit,
                totalAmount = totalAmount,
                buyerName = buyerName,
                marketName = marketName,
                date = dateStr.ifEmpty { getCurrentDateFormatted() },
                fieldName = fieldName,
                notes = notes
            )
            dao.insertIncome(entity)
        }
    }

    // Delete Income
    fun deleteIncome(id: Int) {
        viewModelScope.launch {
            dao.deleteIncomeById(id)
        }
    }

    // Add Field
    fun addField(
        fieldName: String,
        area: Double,
        unit: String,
        cropName: String,
        season: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val entity = FarmerFieldEntity(
                userEmail = activeUserEmail.value,
                fieldName = fieldName,
                area = area,
                unit = unit,
                cropName = cropName,
                season = season,
                notes = notes
            )
            dao.insertField(entity)
        }
    }

    // Delete Field
    fun deleteField(id: Int) {
        viewModelScope.launch {
            dao.deleteFieldById(id)
        }
    }

    companion object {
        fun getCurrentDateFormatted(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}
