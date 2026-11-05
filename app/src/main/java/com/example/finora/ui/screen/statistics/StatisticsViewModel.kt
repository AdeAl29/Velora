package com.example.finora.ui.screen.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finora.core.database.entity.TransactionEntity
import com.example.finora.core.repository.CategoryRepository
import com.example.finora.core.repository.TransactionRepository
import com.example.finora.core.util.DateUtils
import com.example.finora.ui.components.charts.ChartDataPoint
import com.example.finora.ui.screen.home.CategoryExpense
import com.example.finora.ui.theme.FinoraExpense
import com.example.finora.ui.theme.FinoraIncome
import com.example.finora.ui.theme.FinoraPrimary
import com.example.finora.ui.theme.FinoraSecondary
import com.example.finora.ui.theme.FinoraWarning
import com.example.finora.ui.theme.getCategoryColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class StatPeriod(val label: String) {
    THIS_WEEK("Minggu Ini"),
    THIS_MONTH("Bulan Ini"),
    LAST_MONTH("Bulan Lalu"),
    THREE_MONTHS("3 Bulan"),
    SIX_MONTHS("6 Bulan"),
    THIS_YEAR("Tahun Ini")
}

data class FinancialInsight(
    val title: String,
    val description: String,
    val isPositive: Boolean = true
)

data class StatisticsUiState(
    val selectedPeriod: StatPeriod = StatPeriod.THIS_MONTH,
    val totalIncome: Long = 0,
    val totalExpense: Long = 0,
    val netCashFlow: Long = 0,
    val totalSavings: Long = 0,
    val fixedExpense: Long = 0,
    val variableExpense: Long = 0,
    val categoryExpenses: List<CategoryExpense> = emptyList(),
    val incomeExpenseBarData: List<ChartDataPoint> = emptyList(),
    val cashFlowLineData: List<ChartDataPoint> = emptyList(),
    val insights: List<FinancialInsight> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private var statsJob: kotlinx.coroutines.Job? = null

    init {
        loadStatistics(StatPeriod.THIS_MONTH)
    }

    fun setPeriod(period: StatPeriod) {
        _uiState.update { it.copy(selectedPeriod = period, isLoading = true) }
        loadStatistics(period)
    }

    private fun loadStatistics(period: StatPeriod) {
        val (startDate, endDate) = getDateRange(period)

        statsJob?.cancel()
        statsJob = viewModelScope.launch {
            combine(
                transactionRepository.observeTotalIncome(startDate, endDate),
                transactionRepository.observeTotalExpense(startDate, endDate),
                transactionRepository.observeTotalFixedExpense(startDate, endDate),
                transactionRepository.observeTotalVariableExpense(startDate, endDate),
                transactionRepository.observeByDateRange(startDate, endDate)
            ) { income, expense, fixed, variable, transactions ->
                val cashFlow = income - expense
                val savings = (income - expense).coerceAtLeast(0L)

                // Category Expenses
                val expTxs = transactions.filter { it.transactionType != "INCOME" }
                val catMap = mutableMapOf<Long, Long>()
                expTxs.forEach { tx ->
                    tx.categoryId?.let { cId ->
                        catMap[cId] = (catMap[cId] ?: 0L) + tx.amount
                    }
                }
                val catExpenses = catMap.entries.map { (catId, amt) ->
                    val cat = categoryRepository.getById(catId)
                    CategoryExpense(
                        categoryName = cat?.name ?: "Lainnya",
                        categoryIconName = cat?.iconName ?: "other",
                        categoryColorHex = cat?.colorHex ?: "#94A3B8",
                        amount = amt
                    )
                }.sortedByDescending { it.amount }

                // Bar Data
                val barData = listOf(
                    ChartDataPoint("Pemasukan", income.toFloat(), FinoraIncome),
                    ChartDataPoint("Pengeluaran", expense.toFloat(), FinoraExpense)
                )

                // Cash Flow Trend (points across sub-periods)
                val lineData = generateLineData(transactions, startDate, endDate)

                // Dynamic Insights
                val insights = generateDynamicInsights(income, expense, fixed, variable, catExpenses)

                StatisticsUiState(
                    selectedPeriod = period,
                    totalIncome = income,
                    totalExpense = expense,
                    netCashFlow = cashFlow,
                    totalSavings = savings,
                    fixedExpense = fixed,
                    variableExpense = variable,
                    categoryExpenses = catExpenses,
                    incomeExpenseBarData = barData,
                    cashFlowLineData = lineData,
                    insights = insights,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun generateLineData(
        transactions: List<TransactionEntity>,
        startDate: Long,
        endDate: Long
    ): List<ChartDataPoint> {
        val days = 6
        val step = (endDate - startDate) / days
        val points = mutableListOf<ChartDataPoint>()
        for (i in 0..days) {
            val subStart = startDate + (i * step)
            val subEnd = subStart + step
            val income = transactions.filter { it.date in subStart..subEnd && it.transactionType == "INCOME" }.sumOf { it.amount }
            val expense = transactions.filter { it.date in subStart..subEnd && it.transactionType != "INCOME" }.sumOf { it.amount }
            val balanceAtPoint = income - expense
            val label = DateUtils.formatShortDate(subStart)
            points.add(ChartDataPoint(label, balanceAtPoint.toFloat(), FinoraPrimary))
        }
        return points
    }

    private fun generateDynamicInsights(
        income: Long,
        expense: Long,
        fixed: Long,
        variable: Long,
        categoryExpenses: List<CategoryExpense>
    ): List<FinancialInsight> {
        val list = mutableListOf<FinancialInsight>()

        // 1. Savings rate insight
        if (income > 0) {
            val savingsRate = ((income - expense).toFloat() / income * 100).toInt()
            if (savingsRate >= 20) {
                list.add(
                    FinancialInsight(
                        title = "Tingkat Tabungan Sangat Baik",
                        description = "Anda berhasil menyisihkan $savingsRate% dari pemasukan periode ini. Pertahankan pola keuangan sehat ini!",
                        isPositive = true
                    )
                )
            } else if (savingsRate < 0) {
                list.add(
                    FinancialInsight(
                        title = "Pengeluaran Melebihi Pemasukan",
                        description = "Defisit terdeteksi sebesar ${com.example.finora.core.util.CurrencyFormatter.formatRupiah(expense - income)}. Kurangi pengeluaran tidak tetap.",
                        isPositive = false
                    )
                )
            }
        }

        // 2. Fixed expense ratio insight
        if (income > 0 && fixed > 0) {
            val fixedPct = ((fixed.toFloat() / income) * 100).toInt()
            list.add(
                FinancialInsight(
                    title = "Porsi Pengeluaran Tetap",
                    description = "Anda mengalokasikan $fixedPct% pemasukan untuk pengeluaran tetap (seperti kos, listrik, dan tagihan rutin).",
                    isPositive = fixedPct <= 50
                )
            )
        }

        // 3. Largest category insight
        if (categoryExpenses.isNotEmpty()) {
            val top = categoryExpenses.first()
            val pctOfTotal = if (expense > 0) ((top.amount.toFloat() / expense) * 100).toInt() else 0
            list.add(
                FinancialInsight(
                    title = "Kategori Pengeluaran Terbesar",
                    description = "Kategori ${top.categoryName} mendominasi dengan kontribusi $pctOfTotal% (${com.example.finora.core.util.CurrencyFormatter.formatRupiah(top.amount)}) dari total belanja.",
                    isPositive = true
                )
            )
        }

        // 4. Fixed vs Variable comparison
        if (fixed > 0 && variable > 0) {
            val variablePct = ((variable.toFloat() / expense) * 100).toInt()
            list.add(
                FinancialInsight(
                    title = "Fleksibilitas Belanja",
                    description = "$variablePct% pengeluaran Anda bersifat tidak tetap dan dapat dievaluasi untuk menambah alokasi tabungan.",
                    isPositive = true
                )
            )
        }

        return list
    }

    private fun getDateRange(period: StatPeriod): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        return when (period) {
            StatPeriod.THIS_WEEK -> DateUtils.getStartOfWeek() to now
            StatPeriod.THIS_MONTH -> DateUtils.getStartOfMonth() to DateUtils.getEndOfMonth()
            StatPeriod.LAST_MONTH -> DateUtils.getStartOfLastMonth() to DateUtils.getEndOfLastMonth()
            StatPeriod.THREE_MONTHS -> DateUtils.getStartOfNMonthsAgo(3) to now
            StatPeriod.SIX_MONTHS -> DateUtils.getStartOfNMonthsAgo(6) to now
            StatPeriod.THIS_YEAR -> DateUtils.getStartOfYear() to DateUtils.getEndOfYear()
        }
    }
}
