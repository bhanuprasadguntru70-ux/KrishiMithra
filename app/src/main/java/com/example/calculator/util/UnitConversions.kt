package com.example.calculator.util

object LandUnitConversions {
    const val UNIT_ACRE = "Acres"
    const val UNIT_CENT = "Cents"
    const val UNIT_HECTARE = "Hectares"
    const val UNIT_SQ_METER = "Sq Meters"

    val SUPPORTED_UNITS = listOf(UNIT_ACRE, UNIT_CENT, UNIT_HECTARE, UNIT_SQ_METER)

    /**
     * Converts any supported land area to Acres accurately.
     */
    fun toAcres(value: Double, unit: String): Double {
        return when (unit) {
            UNIT_ACRE -> value
            UNIT_CENT -> value / 100.0
            UNIT_HECTARE -> value * 2.47105
            UNIT_SQ_METER -> value / 4046.86
            else -> value
        }
    }

    /**
     * Converts area in Acres to Hectares accurately.
     */
    fun acresToHectares(acres: Double): Double {
        return acres / 2.47105
    }
}

object WeightUnitConversions {
    const val UNIT_KG = "kg"
    const val UNIT_QUINTAL = "quintal"
    const val UNIT_TONNE = "tonne"
    const val UNIT_BAG = "bag"

    /**
     * Converts value from given unit to Kilograms.
     */
    fun toKilograms(value: Double, unit: String, kgPerBag: Double = 50.0): Double {
        return when (unit.lowercase()) {
            "kg", "kilograms" -> value
            "quintal", "quintals" -> value * 100.0
            "tonne", "tonnes" -> value * 1000.0
            "bag", "bags" -> value * kgPerBag
            else -> value
        }
    }

    fun kgToQuintals(kg: Double): Double = kg / 100.0
    fun kgToTonnes(kg: Double): Double = kg / 1000.0
}
