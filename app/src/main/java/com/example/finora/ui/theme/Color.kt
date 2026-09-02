package com.example.finora.ui.theme

import androidx.compose.ui.graphics.Color

// ===== FINORA DARK THEME PALETTE =====
val FinoraDarkBackground = Color(0xFF0A0E1A)
val FinoraDarkSurface = Color(0xFF141929)
val FinoraDarkSurfaceVariant = Color(0xFF1C2237)
val FinoraDarkSurfaceElevated = Color(0xFF232940)
val FinoraDarkBorder = Color(0xFF2A3150)

// ===== FINORA LIGHT THEME PALETTE =====
val FinoraLightBackground = Color(0xFFF8FAFC)
val FinoraLightSurface = Color(0xFFFFFFFF)
val FinoraLightSurfaceVariant = Color(0xFFF1F5F9)
val FinoraLightSurfaceElevated = Color(0xFFE8ECF4)
val FinoraLightBorder = Color(0xFFE2E8F0)

// ===== PRIMARY BRAND COLORS (Aruphia Modern Blue) =====
val FinoraPrimary = Color(0xFF2563EB)
val FinoraPrimaryLight = Color(0xFF60A5FA)
val FinoraPrimaryDark = Color(0xFF1D4ED8)
val FinoraPrimaryContainer = Color(0xFF1E3A8A)
val FinoraPrimaryContainerLight = Color(0xFFDBEAFE)

// ===== SECONDARY / ACCENT (Ocean Cyan / Cerulean) =====
val FinoraSecondary = Color(0xFF0EA5E9)
val FinoraSecondaryDark = Color(0xFF0284C7)
val FinoraSecondaryContainer = Color(0xFF0C4A6E)
val FinoraSecondaryContainerLight = Color(0xFFE0F2FE)

// ===== SEMANTIC COLORS =====
val FinoraIncome = Color(0xFF34D399)
val FinoraIncomeDark = Color(0xFF059669)
val FinoraIncomeContainer = Color(0xFF064E3B)
val FinoraIncomeContainerLight = Color(0xFFD1FAE5)

val FinoraExpense = Color(0xFFF87171)
val FinoraExpenseDark = Color(0xFFDC2626)
val FinoraExpenseContainer = Color(0xFF7F1D1D)
val FinoraExpenseContainerLight = Color(0xFFFEE2E2)

val FinoraWarning = Color(0xFFFBBF24)
val FinoraWarningDark = Color(0xFFD97706)
val FinoraWarningContainer = Color(0xFF78350F)
val FinoraWarningContainerLight = Color(0xFFFEF3C7)

val FinoraInfo = Color(0xFF60A5FA)
val FinoraInfoContainer = Color(0xFF1E3A5F)
val FinoraInfoContainerLight = Color(0xFFDBEAFE)

// ===== TEXT COLORS =====
val FinoraDarkTextPrimary = Color(0xFFF1F5F9)
val FinoraDarkTextSecondary = Color(0xFF94A3B8)
val FinoraDarkTextTertiary = Color(0xFF64748B)

val FinoraLightTextPrimary = Color(0xFF0F172A)
val FinoraLightTextSecondary = Color(0xFF475569)
val FinoraLightTextTertiary = Color(0xFF94A3B8)

// ===== CATEGORY COLORS (for Canvas-drawn icons) =====
val CategoryFoodColor = Color(0xFFFB923C)
val CategoryTransportColor = Color(0xFF38BDF8)
val CategoryShoppingColor = Color(0xFFF472B6)
val CategoryEntertainmentColor = Color(0xFFA78BFA)
val CategoryHealthColor = Color(0xFF34D399)
val CategoryEducationColor = Color(0xFF818CF8)
val CategoryHousingColor = Color(0xFFF87171)
val CategoryElectricityColor = Color(0xFFFBBF24)
val CategoryInternetColor = Color(0xFF38BDF8)
val CategoryInsuranceColor = Color(0xFF818CF8)
val CategorySubscriptionColor = Color(0xFFA78BFA)
val CategorySalaryColor = Color(0xFF34D399)
val CategoryBonusColor = Color(0xFFA78BFA)
val CategoryFreelanceColor = Color(0xFF38BDF8)
val CategoryGiftColor = Color(0xFFFB7185)
val CategoryInvestmentColor = Color(0xFFFBBF24)
val CategoryTechnologyColor = Color(0xFF6366F1)
val CategoryOtherColor = Color(0xFF94A3B8)
val CategoryHangoutColor = Color(0xFFFBBF24)
val CategoryInstallmentColor = Color(0xFFF472B6)
val CategoryBillColor = Color(0xFFFB923C)

// ===== GRADIENT DEFINITIONS =====
val FinoraPrimaryGradient = listOf(Color(0xFF2563EB), Color(0xFF0EA5E9))
val FinoraIncomeGradient = listOf(Color(0xFF34D399), Color(0xFF6EE7B7))
val FinoraExpenseGradient = listOf(Color(0xFFF87171), Color(0xFFFCA5A5))
val FinoraCardGradient = listOf(Color(0xFF1C2237), Color(0xFF232940))
val FinoraCardGradientLight = listOf(Color(0xFFFFFFFF), Color(0xFFF8FAFC))
val FinoraBalanceGradient = listOf(Color(0xFF1D4ED8), Color(0xFF2563EB), Color(0xFF38BDF8))

fun getCategoryColor(colorHex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        CategoryOtherColor
    }
}
