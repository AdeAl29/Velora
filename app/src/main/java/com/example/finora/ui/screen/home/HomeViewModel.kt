package com.example.finora.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finora.core.database.entity.*
import com.example.finora.core.datastore.FinoraDataStore
import com.example.finora.core.repository.*
import com.example.finora.core.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "",
    val greeting: String = DateUtils.getGreeting(),
    val totalBalance: Long = 0,
    val monthlyIncome: Long = 0,
    val monthlyExpense: Long = 0,
    val monthlySavings: Long = 0,
    val percentRemaining: Float = 100f,
    val percentChange: Float = 0f,
    val fixedExpense: Long = 0,
    val variableExpense: Long = 0,
    val recentTransactions: List<TransactionWithCategory> = emptyList(),
    val activeGoals: List<FinancialGoalEntity> = emptyList(),
    val unpaidBills: List<BillEntity> = emptyList(),
    val wallets: List<WalletWithBalance> = emptyList(),
    val sparklineData: List<Float> = emptyList(),
    val isBalanceHidden: Boolean = false,
    val isLoading: Boolean = true,
    val categoryExpenses: List<CategoryExpense> = emptyList()
)

data class TransactionWithCategory(
    val transaction: TransactionEntity,
    val categoryName: String = "",
    val categoryIconName: String = "other",
    val categoryColorHex: String = "#94A3B8",
    val walletName: String = ""
)

data class WalletWithBalance(
    val wallet: WalletEntity,
    val balance: Long
)

data class CategoryExpense(
    val categoryName: String,
    val categoryIconName: String,
    val categoryColorHex: String,
    val amount: Long
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository,
    private val goalRepository: GoalRepository,
    private val billRepository: BillRepository,
    private val dataStore: FinoraDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val startOfMonth = DateUtils.getStartOfMonth()
        val endOfMonth = DateUtils.getEndOfMonth()
        val startOfLastMonth = DateUtils.getStartOfLastMonth()
        val endOfLastMonth = DateUtils.getEndOfLastMonth()

        // User name
        viewModelScope.launch {
            dataStore.userName.collect { name ->
                _uiState.update { it.copy(userName = name) }
            }
        }

        // Balance hidden
        viewModelScope.launch {
            dataStore.isBalanceHidden.collect { hidden ->
                _uiState.update { it.copy(isBalanceHidden = hidden) }
            }
        }

        // Monthly income, expense, and percentChange combined reactively
        viewModelScope.launch {
            combine(
                transactionRepository.observeTotalIncome(startOfMonth, endOfMonth),
                transactionRepository.observeTotalExpense(startOfMonth, endOfMonth),
                transactionRepository.observeTotalIncome(startOfLastMonth, endOfLastMonth),
                transactionRepository.observeTotalExpense(startOfLastMonth, endOfLastMonth)
            ) { income, expense, lastIncome, lastExpense ->
                val savings = income - expense
                val percentRemaining = if (income > 0) {
                    ((income - expense).toFloat() / income * 100f).coerceIn(0f, 100f)
                } else {
                    100f
                }
                val lastNet = lastIncome - lastExpense
                val currentNet = income - expense
                val change = if (lastNet != 0L) {
                    ((currentNet - lastNet).toFloat() / kotlin.math.abs(lastNet) * 100f)
                } else {
                    0f
                }
                _uiState.update {
                    it.copy(
                        monthlyIncome = income,
                        monthlyExpense = expense,
                        monthlySavings = savings,
                        percentRemaining = percentRemaining,
                        percentChange = change,
                        isLoading = false
                    )
                }
            }.collect()
        }

        // Fixed vs variable expense
        viewModelScope.launch {
            transactionRepository.observeTotalFixedExpense(startOfMonth, endOfMonth).collect { fixed ->
                _uiState.update { it.copy(fixedExpense = fixed) }
            }
        }
        viewModelScope.launch {
            transactionRepository.observeTotalVariableExpense(startOfMonth, endOfMonth).collect { variable ->
                _uiState.update { it.copy(variableExpense = variable) }
            }
        }

        // Wallets with computed balances — reactive to wallet, transaction, AND transfer changes
        viewModelScope.launch {
            combine(
                walletRepository.observeAll(),
                transactionRepository.observeAll(),
                walletRepository.observeTransfers()
            ) { walletList, _, _ ->
                val walletsWithBalance = walletList.map { wallet ->
                    WalletWithBalance(wallet, walletRepository.getWalletBalance(wallet.id))
                }
                val totalBalance = walletsWithBalance.sumOf { it.balance }
                walletsWithBalance to totalBalance
            }.collect { (walletsWithBalance, totalBalance) ->
                _uiState.update {
                    it.copy(wallets = walletsWithBalance, totalBalance = totalBalance)
                }
            }
        }

        // Recent transactions with category & wallet info
        viewModelScope.launch {
            combine(
                transactionRepository.observeRecent(5),
                categoryRepository.observeAll(),
                walletRepository.observeAll()
            ) { transactions, _, _ ->
                transactions.map { tx ->
                    val cat = tx.categoryId?.let { categoryRepository.getById(it) }
                    val wallet = walletRepository.getById(tx.walletId)
                    TransactionWithCategory(
                        transaction = tx,
                        categoryName = cat?.name ?: "Lainnya",
                        categoryIconName = cat?.iconName ?: "other",
                        categoryColorHex = cat?.colorHex ?: "#94A3B8",
                        walletName = wallet?.name ?: ""
                    )
                }
            }.collect { enriched ->
                _uiState.update { it.copy(recentTransactions = enriched) }
            }
        }

        // Active goals
        viewModelScope.launch {
            goalRepository.observeActive().collect { goals ->
                _uiState.update { it.copy(activeGoals = goals.take(3)) }
            }
        }

        // Unpaid bills
        viewModelScope.launch {
            billRepository.observeUnpaid().collect { bills ->
                _uiState.update { it.copy(unpaidBills = bills.take(3)) }
            }
        }

        // Category expenses for donut chart + 7-day sparkline
        viewModelScope.launch {
            combine(
                transactionRepository.observeByDateRange(startOfMonth, endOfMonth),
                categoryRepository.observeAll()
            ) { transactions, _ ->
                val expenseTransactions = transactions.filter {
                    it.transactionType == "EXPENSE_FIXED" || it.transactionType == "EXPENSE_VARIABLE"
                }
                val categoryMap = mutableMapOf<Long, Long>()
                expenseTransactions.forEach { tx ->
                    tx.categoryId?.let { catId ->
                        categoryMap[catId] = (categoryMap[catId] ?: 0) + tx.amount
                    }
                }
                val categoryExpenses = categoryMap.entries.map { (catId, amount) ->
                    val cat = categoryRepository.getById(catId)
                    CategoryExpense(
                        categoryName = cat?.name ?: "Lainnya",
                        categoryIconName = cat?.iconName ?: "other",
                        categoryColorHex = cat?.colorHex ?: "#94A3B8",
                        amount = amount
                    )
                }.sortedByDescending { it.amount }

                val last7Days = (6 downTo 0).map { daysAgo ->
                    val dayStart = DateUtils.toEpochMillis(java.time.LocalDate.now().minusDays(daysAgo.toLong()))
                    val dayEnd = DateUtils.getEndOfDay(dayStart)
                    val dayExpense = transactions.filter { it.date in dayStart..dayEnd }
                        .filter { it.transactionType != "INCOME" }
                        .sumOf { it.amount }
                    dayExpense.toFloat()
                }
                categoryExpenses to last7Days
            }.collect { (categoryExpenses, last7Days) ->
                _uiState.update {
                    it.copy(
                        categoryExpenses = categoryExpenses,
                        sparklineData = last7Days
                    )
                }
            }
        }
    }

    fun toggleBalanceVisibility() {
        viewModelScope.launch {
            val current = _uiState.value.isBalanceHidden
            dataStore.setBalanceHidden(!current)
        }
    }
}
