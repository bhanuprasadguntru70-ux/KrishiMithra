package com.example.calculator.models

data class SavedCalculation(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val calculatorName: String,
    val date: String,
    val summary: String,
    val details: Map<String, String> = emptyMap()
)

data class CalculationHistoryItem(
    val id: String = System.currentTimeMillis().toString(),
    val calculatorName: String,
    val date: String,
    val summary: String,
    val resultAmount: String
)

enum class LandUnit(val label: String, val toAcresRatio: Double) {
    ACRES("Acres / ఎకరాలు", 1.0),
    CENTS("Cents / సెంట్లు", 0.01),
    HECTARES("Hectares / హెక్టార్లు", 2.47105),
    SQ_METERS("Sq Meters / చదరపు మీటర్లు", 0.000247105)
}

enum class WeightUnit(val label: String, val toKgRatio: Double) {
    KG("Kilograms (kg) / కిలోలు", 1.0),
    QUINTAL("Quintals (qtl) / క్వింటాళ్లు", 100.0),
    TONNE("Tonnes (t) / టన్నులు", 1000.0)
}
