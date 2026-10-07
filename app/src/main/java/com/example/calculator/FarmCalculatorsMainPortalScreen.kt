package com.example.calculator

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculator.crop.*
import com.example.calculator.financial.*
import com.example.calculator.history.SavedCalculationsHistoryScreen
import com.example.calculator.irrigation.IrrigationCostCalculatorScreen
import com.example.calculator.labour.DailyWorkCalculatorScreen
import com.example.calculator.labour.LabourWageCalculatorScreen
import com.example.calculator.moneybook.*
import com.example.calculator.profit.ProfitLossCalculatorScreen
import com.example.calculator.sale.*
import com.example.calculator.tractor.TractorAreaCalculatorScreen
import com.example.calculator.tractor.TractorWorkCalculatorScreen

@Composable
fun FarmCalculatorsMainPortalScreen(
    viewModel: FarmCalculatorsViewModel = viewModel(),
    onBackToDashboard: () -> Unit
) {
    val savedCalculations by viewModel.savedCalculations.collectAsStateWithLifecycle()
    val marketCrops by viewModel.marketCrops.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()

    var currentRoute by remember { mutableStateOf("home") }
    var prefilledCropName by remember { mutableStateOf("") }
    var prefilledMandiPrice by remember { mutableStateOf("") }

    // Pre-fill state for My Book
    var bookPreFillType by remember { mutableStateOf<String?>(null) }
    var bookPreFillAmount by remember { mutableStateOf<Double?>(null) }
    var bookPreFillDesc by remember { mutableStateOf<String?>(null) }

    val handleSave: (String, String, String, String) -> Unit = { title, type, result, details ->
        viewModel.saveCalculation(title, type, result, details)
    }

    val handleLanguage: (String) -> Unit = { lang ->
        viewModel.setAppLanguage(lang)
    }

    val handleRecordInBook: (String, Double, String) -> Unit = { type, amt, desc ->
        bookPreFillType = type
        bookPreFillAmount = amt
        bookPreFillDesc = desc
        currentRoute = "my_book"
    }

    when (currentRoute) {
        "home" -> FarmToolsHomeScreen(
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onSelectCalculator = { route ->
                if (route == "my_book") {
                    bookPreFillType = null
                    bookPreFillAmount = null
                    bookPreFillDesc = null
                }
                currentRoute = route
            },
            onOpenHistory = { currentRoute = "history" },
            onBack = onBackToDashboard
        )
        "my_book" -> MoneyBookPortalScreen(
            appLanguage = appLanguage,
            initialPreFillType = bookPreFillType,
            initialAmount = bookPreFillAmount,
            initialDesc = bookPreFillDesc,
            onLanguageSelected = handleLanguage,
            onBack = { currentRoute = "home" }
        )
        "interest_calc" -> InterestCalculatorScreen(
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onSaveCalculation = handleSave,
            onRecordInMoneyBook = handleRecordInBook,
            onBack = { currentRoute = "home" }
        )
        "emi_calc" -> EmiCalculatorScreen(
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onSaveCalculation = handleSave,
            onRecordInMoneyBook = handleRecordInBook,
            onBack = { currentRoute = "home" }
        )
        "sip_calc" -> SipCalculatorScreen(
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onSaveCalculation = handleSave,
            onBack = { currentRoute = "home" }
        )

        "tractor_work" -> TractorWorkCalculatorScreen(
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onSaveCalculation = handleSave,
            onBack = { currentRoute = "home" }
        )
        "tractor_area" -> TractorAreaCalculatorScreen(
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onSaveCalculation = handleSave,
            onBack = { currentRoute = "home" }
        )
        "crop_cost" -> CropCostCalculatorScreen(
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onSaveCalculation = handleSave,
            onBack = { currentRoute = "home" }
        )
        "crop_yield_bags" -> CropYieldBagsCalculatorScreen(
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onSaveCalculation = handleSave,
            onBack = { currentRoute = "home" }
        )
        "bag_to_rupees" -> BagToRupeesCalculatorScreen(
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onSaveCalculation = handleSave,
            onBack = { currentRoute = "home" }
        )
        "weight_converter" -> WeightConverterScreen(
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onBack = { currentRoute = "home" }
        )
        "crop_sale" -> CropSaleCalculatorScreen(
            appLanguage = appLanguage,
            initialCropName = prefilledCropName,
            initialMandiPrice = prefilledMandiPrice,
            onLanguageSelected = handleLanguage,
            onSaveCalculation = handleSave,
            onBack = { currentRoute = "home" }
        )
        "profit_loss" -> ProfitLossCalculatorScreen(
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onSaveCalculation = handleSave,
            onBack = { currentRoute = "home" }
        )
        "labour_wage" -> LabourWageCalculatorScreen(
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onSaveCalculation = handleSave,
            onBack = { currentRoute = "home" }
        )
        "daily_work" -> DailyWorkCalculatorScreen(
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onSaveCalculation = handleSave,
            onBack = { currentRoute = "home" }
        )
        "seed_calc" -> SeedCalculatorScreen(
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onSaveCalculation = handleSave,
            onBack = { currentRoute = "home" }
        )
        "fertilizer_calc" -> FertilizerCalculatorScreen(
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onSaveCalculation = handleSave,
            onBack = { currentRoute = "home" }
        )
        "pesticide_calc" -> PesticideCalculatorScreen(
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onSaveCalculation = handleSave,
            onBack = { currentRoute = "home" }
        )
        "irrigation_calc" -> IrrigationCostCalculatorScreen(
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onSaveCalculation = handleSave,
            onBack = { currentRoute = "home" }
        )
        "cost_per_acre" -> CropCostPerAcreScreen(
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onSaveCalculation = handleSave,
            onBack = { currentRoute = "home" }
        )
        "target_price" -> SalePriceTargetScreen(
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onSaveCalculation = handleSave,
            onBack = { currentRoute = "home" }
        )
        "mandi_calc" -> MandiIntegrationCalculatorScreen(
            marketCrops = marketCrops,
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onNavigateToSaleCalc = { name, price ->
                prefilledCropName = name
                prefilledMandiPrice = price
                currentRoute = "crop_sale"
            },
            onBack = { currentRoute = "home" }
        )
        "compare_markets" -> CompareMarketsCalculatorScreen(
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onBack = { currentRoute = "home" }
        )
        "history" -> SavedCalculationsHistoryScreen(
            savedCalculations = savedCalculations,
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onDeleteCalculation = { id -> viewModel.deleteSavedCalculation(id) },
            onClearAll = { viewModel.clearAllCalculations() },
            onBack = { currentRoute = "home" }
        )
        else -> FarmToolsHomeScreen(
            appLanguage = appLanguage,
            onLanguageSelected = handleLanguage,
            onSelectCalculator = { route -> currentRoute = route },
            onOpenHistory = { currentRoute = "history" },
            onBack = onBackToDashboard
        )
    }
}
