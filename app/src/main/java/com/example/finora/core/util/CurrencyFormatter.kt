package com.example.finora.core.util

import com.example.finora.core.model.CurrencyInfo
import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    fun format(amount: Long, currency: CurrencyInfo = CurrencyInfo.IDR): String {
        return when (currency.code) {
            "IDR" -> formatRupiah(amount)
            else -> formatGeneric(amount, currency)
        }
    }

    fun formatRupiah(amount: Long): String {
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return "Rp${formatter.format(amount)}"
    }

    fun formatCompact(amount: Long, currency: CurrencyInfo = CurrencyInfo.IDR): String {
        val absAmount = kotlin.math.abs(amount)
        return when {
            absAmount >= 1_000_000_000 -> {
                val value = absAmount / 1_000_000_000.0
                "${currency.symbol}${String.format(Locale.US, "%.1f", value)}M"
            }
            absAmount >= 1_000_000 -> {
                val value = absAmount / 1_000_000.0
                "${currency.symbol}${String.format(Locale.US, "%.1f", value)}jt"
            }
            absAmount >= 1_000 -> {
                val value = absAmount / 1_000.0
                "${currency.symbol}${String.format(Locale.US, "%.0f", value)}rb"
            }
            else -> format(amount, currency)
        }
    }

    fun formatSigned(amount: Long, isIncome: Boolean, currency: CurrencyInfo = CurrencyInfo.IDR): String {
        val prefix = if (isIncome) "+" else "-"
        return "$prefix${format(amount, currency)}"
    }

    private fun formatGeneric(amount: Long, currency: CurrencyInfo): String {
        val divisor = Math.pow(10.0, currency.decimalPlaces.toDouble())
        val value = amount / divisor
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        formatter.minimumFractionDigits = currency.decimalPlaces
        formatter.maximumFractionDigits = currency.decimalPlaces
        return "${currency.symbol}${formatter.format(value)}"
    }

    fun parseAmount(input: String): Long? {
        val cleaned = input.replace(Regex("[^0-9]"), "")
        return cleaned.toLongOrNull()
    }
}
