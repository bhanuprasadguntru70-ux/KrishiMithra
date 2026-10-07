package com.example.calculator.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object IndianFormatter {

    fun formatCurrency(amount: Double): String {
        if (amount.isNaN() || amount.isInfinite()) return "₹0"
        val isNegative = amount < 0
        val absAmount = Math.abs(amount)
        
        val df = DecimalFormat("##,##,##0.##")
        val formattedNumber = df.format(absAmount)
        return if (isNegative) "-₹$formattedNumber" else "₹$formattedNumber"
    }

    fun formatNumber(number: Double, maxDecimals: Int = 2): String {
        if (number.isNaN() || number.isInfinite()) return "0"
        val pattern = if (maxDecimals <= 0) "##,##,##0" else "##,##,##0." + "#".repeat(maxDecimals)
        val df = DecimalFormat(pattern)
        return df.format(number)
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
        return sdf.format(Date(timestamp))
    }
}
