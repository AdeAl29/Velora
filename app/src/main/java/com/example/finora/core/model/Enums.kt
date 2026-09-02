package com.example.finora.core.model

enum class TransactionType {
    INCOME,
    EXPENSE_FIXED,
    EXPENSE_VARIABLE;

    val isExpense: Boolean get() = this == EXPENSE_FIXED || this == EXPENSE_VARIABLE
    val isIncome: Boolean get() = this == INCOME
}

enum class ExpenseType {
    FIXED,
    VARIABLE
}

enum class Frequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY;

    fun toDisplayString(): String = when (this) {
        DAILY -> "Harian"
        WEEKLY -> "Mingguan"
        MONTHLY -> "Bulanan"
        YEARLY -> "Tahunan"
    }
}

enum class BudgetPeriod {
    WEEKLY,
    MONTHLY,
    YEARLY;

    fun toDisplayString(): String = when (this) {
        WEEKLY -> "Mingguan"
        MONTHLY -> "Bulanan"
        YEARLY -> "Tahunan"
    }
}

enum class GoalPriority(val value: Int) {
    HIGH(1),
    MEDIUM(2),
    LOW(3);

    fun toDisplayString(): String = when (this) {
        HIGH -> "Tinggi"
        MEDIUM -> "Sedang"
        LOW -> "Rendah"
    }
}

data class CurrencyInfo(
    val code: String,
    val symbol: String,
    val name: String,
    val decimalPlaces: Int = 0
) {
    companion object {
        val IDR = CurrencyInfo("IDR", "Rp", "Rupiah Indonesia", 0)
        val USD = CurrencyInfo("USD", "$", "US Dollar", 2)
        val EUR = CurrencyInfo("EUR", "€", "Euro", 2)
        val GBP = CurrencyInfo("GBP", "£", "British Pound", 2)
        val JPY = CurrencyInfo("JPY", "¥", "Japanese Yen", 0)
        val SGD = CurrencyInfo("SGD", "S$", "Singapore Dollar", 2)
        val MYR = CurrencyInfo("MYR", "RM", "Malaysian Ringgit", 2)

        val ALL = listOf(IDR, USD, EUR, GBP, JPY, SGD, MYR)

        fun fromCode(code: String): CurrencyInfo =
            ALL.find { it.code == code } ?: IDR
    }
}
