package com.example.finora

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute : NavKey

@Serializable
data object OnboardingRoute : NavKey

@Serializable
data object MainDashboardRoute : NavKey

@Serializable
data class AddTransactionRoute(val transactionId: Long? = null, val initialType: String? = null) : NavKey

@Serializable
data class TransactionDetailRoute(val transactionId: Long) : NavKey

@Serializable
data object WalletListRoute : NavKey

@Serializable
data class AddWalletRoute(val walletId: Long? = null) : NavKey

@Serializable
data object TransferRoute : NavKey

@Serializable
data object BudgetListRoute : NavKey

@Serializable
data class AddBudgetRoute(val budgetId: Long? = null) : NavKey

@Serializable
data object GoalListRoute : NavKey

@Serializable
data class AddGoalRoute(val goalId: Long? = null) : NavKey

@Serializable
data class GoalDetailRoute(val goalId: Long) : NavKey

@Serializable
data object BillListRoute : NavKey

@Serializable
data class AddBillRoute(val billId: Long? = null) : NavKey

@Serializable
data object RecurringListRoute : NavKey

@Serializable
data class AddRecurringRoute(val recurringId: Long? = null) : NavKey

@Serializable
data object CalendarRoute : NavKey

@Serializable
data object ReportRoute : NavKey

@Serializable
data object SearchRoute : NavKey

@Serializable
data object SettingsRoute : NavKey
