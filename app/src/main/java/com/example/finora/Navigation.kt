package com.example.finora

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.finora.core.datastore.FinoraDataStore
import com.example.finora.ui.screen.bill.*
import com.example.finora.ui.screen.budget.*
import com.example.finora.ui.screen.calendar.CalendarScreen
import com.example.finora.ui.screen.calendar.CalendarViewModel
import com.example.finora.ui.screen.goal.*
import com.example.finora.ui.screen.main.MainDashboardScreen
import com.example.finora.ui.screen.onboarding.OnboardingScreen
import com.example.finora.ui.screen.onboarding.OnboardingViewModel
import com.example.finora.ui.screen.recurring.*
import com.example.finora.ui.screen.report.ReportScreen
import com.example.finora.ui.screen.report.ReportViewModel
import com.example.finora.ui.screen.search.SearchScreen
import com.example.finora.ui.screen.search.SearchViewModel
import com.example.finora.ui.screen.settings.SettingsScreen
import com.example.finora.ui.screen.settings.SettingsViewModel
import com.example.finora.ui.screen.splash.SplashScreen
import com.example.finora.ui.screen.transaction.*
import com.example.finora.ui.screen.wallet.*

@Composable
fun MainNavigation(dataStore: FinoraDataStore) {
    val backStack = rememberNavBackStack(SplashRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            // 1. Splash Screen
            entry<SplashRoute> {
                SplashScreen(
                    dataStore = dataStore,
                    onNavigateToOnboarding = {
                        backStack.clear()
                        backStack.add(OnboardingRoute)
                    },
                    onNavigateToMain = {
                        backStack.clear()
                        backStack.add(MainDashboardRoute)
                    }
                )
            }

            // 2. Onboarding Screen
            entry<OnboardingRoute> {
                val vm: OnboardingViewModel = hiltViewModel()
                OnboardingScreen(
                    viewModel = vm,
                    onOnboardingFinished = {
                        backStack.clear()
                        backStack.add(MainDashboardRoute)
                    }
                )
            }

            // 3. Main Dashboard (5 bottom tabs)
            entry<MainDashboardRoute> {
                MainDashboardScreen(
                    onNavigateToAddTransaction = { type -> backStack.add(AddTransactionRoute(initialType = type)) },
                    onNavigateToTransactionDetail = { id -> backStack.add(TransactionDetailRoute(transactionId = id)) },
                    onNavigateToGoalDetail = { id -> backStack.add(GoalDetailRoute(goalId = id)) },
                    onNavigateToAddGoal = { backStack.add(AddGoalRoute()) },
                    onNavigateToWallets = { backStack.add(WalletListRoute) },
                    onNavigateToBudgets = { backStack.add(BudgetListRoute) },
                    onNavigateToBills = { backStack.add(BillListRoute) },
                    onNavigateToRecurring = { backStack.add(RecurringListRoute) },
                    onNavigateToCalendar = { backStack.add(CalendarRoute) },
                    onNavigateToReports = { backStack.add(ReportRoute) },
                    onNavigateToSettings = { backStack.add(SettingsRoute) },
                    onNavigateToSearch = { backStack.add(SearchRoute) }
                )
            }

            // 4. Add/Edit Transaction
            entry<AddTransactionRoute> { route ->
                val vm: AddTransactionViewModel = hiltViewModel()
                AddTransactionScreen(
                    viewModel = vm,
                    transactionId = route.transactionId,
                    initialType = route.initialType,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onSaved = { backStack.removeLastOrNull() }
                )
            }

            // 5. Transaction Detail
            entry<TransactionDetailRoute> { route ->
                val vm: TransactionDetailViewModel = hiltViewModel()
                TransactionDetailScreen(
                    viewModel = vm,
                    transactionId = route.transactionId,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToEdit = { id -> backStack.add(AddTransactionRoute(transactionId = id)) }
                )
            }

            // 6. Wallet List
            entry<WalletListRoute> {
                val vm: WalletViewModel = hiltViewModel()
                WalletListScreen(
                    viewModel = vm,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToAddWallet = { backStack.add(AddWalletRoute()) },
                    onNavigateToTransfer = { backStack.add(TransferRoute) },
                    onNavigateToEditWallet = { id -> backStack.add(AddWalletRoute(walletId = id)) }
                )
            }

            // 7. Add/Edit Wallet
            entry<AddWalletRoute> { route ->
                val vm: WalletViewModel = hiltViewModel()
                AddWalletScreen(
                    viewModel = vm,
                    walletId = route.walletId,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }

            // 8. Transfer
            entry<TransferRoute> {
                val vm: TransferViewModel = hiltViewModel()
                TransferScreen(
                    viewModel = vm,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }

            // 9. Budget List
            entry<BudgetListRoute> {
                val vm: BudgetViewModel = hiltViewModel()
                BudgetListScreen(
                    viewModel = vm,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToAddBudget = { backStack.add(AddBudgetRoute()) },
                    onNavigateToEditBudget = { id -> backStack.add(AddBudgetRoute(budgetId = id)) }
                )
            }

            // 10. Add/Edit Budget
            entry<AddBudgetRoute> { route ->
                val vm: BudgetViewModel = hiltViewModel()
                AddBudgetScreen(
                    viewModel = vm,
                    budgetId = route.budgetId,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }

            // 11. Goal List
            entry<GoalListRoute> {
                val vm: GoalViewModel = hiltViewModel()
                GoalListScreen(
                    viewModel = vm,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToAddGoal = { backStack.add(AddGoalRoute()) },
                    onNavigateToGoalDetail = { id -> backStack.add(GoalDetailRoute(goalId = id)) }
                )
            }

            // 12. Add/Edit Goal
            entry<AddGoalRoute> { route ->
                val vm: GoalViewModel = hiltViewModel()
                AddGoalScreen(
                    viewModel = vm,
                    goalId = route.goalId,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }

            // 13. Goal Detail
            entry<GoalDetailRoute> { route ->
                val vm: GoalViewModel = hiltViewModel()
                GoalDetailScreen(
                    viewModel = vm,
                    goalId = route.goalId,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToEdit = { id -> backStack.add(AddGoalRoute(goalId = id)) }
                )
            }

            // 14. Bill List
            entry<BillListRoute> {
                val vm: BillViewModel = hiltViewModel()
                BillListScreen(
                    viewModel = vm,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToAddBill = { backStack.add(AddBillRoute()) },
                    onNavigateToEditBill = { id -> backStack.add(AddBillRoute(billId = id)) }
                )
            }

            // 15. Add/Edit Bill
            entry<AddBillRoute> { route ->
                val vm: BillViewModel = hiltViewModel()
                AddBillScreen(
                    viewModel = vm,
                    billId = route.billId,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }

            // 16. Recurring List
            entry<RecurringListRoute> {
                val vm: RecurringViewModel = hiltViewModel()
                RecurringListScreen(
                    viewModel = vm,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToAddRecurring = { backStack.add(AddRecurringRoute()) },
                    onNavigateToEditRecurring = { id -> backStack.add(AddRecurringRoute(recurringId = id)) }
                )
            }

            // 17. Add/Edit Recurring
            entry<AddRecurringRoute> { route ->
                val vm: RecurringViewModel = hiltViewModel()
                AddRecurringScreen(
                    viewModel = vm,
                    recurringId = route.recurringId,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }

            // 18. Calendar
            entry<CalendarRoute> {
                val vm: CalendarViewModel = hiltViewModel()
                CalendarScreen(
                    viewModel = vm,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToTransactionDetail = { id -> backStack.add(TransactionDetailRoute(transactionId = id)) }
                )
            }

            // 19. Report
            entry<ReportRoute> {
                val vm: ReportViewModel = hiltViewModel()
                ReportScreen(
                    viewModel = vm,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }

            // 20. Global Search
            entry<SearchRoute> {
                val vm: SearchViewModel = hiltViewModel()
                SearchScreen(
                    viewModel = vm,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToTransactionDetail = { id -> backStack.add(TransactionDetailRoute(transactionId = id)) },
                    onNavigateToGoalDetail = { id -> backStack.add(GoalDetailRoute(goalId = id)) }
                )
            }

            // 21. Settings
            entry<SettingsRoute> {
                val vm: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = vm,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
