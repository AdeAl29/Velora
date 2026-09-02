package com.example.finora.ui.screen.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finora.core.database.entity.TransactionEntity
import com.example.finora.core.repository.CategoryRepository
import com.example.finora.core.repository.TransactionRepository
import com.example.finora.core.repository.WalletRepository
import com.example.finora.core.util.DateUtils
import com.example.finora.ui.screen.home.TransactionWithCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class CalendarDayData(
    val date: LocalDate,
    val hasIncome: Boolean = false,
    val hasExpense: Boolean = false,
    val totalIncome: Long = 0,
    val totalExpense: Long = 0,
    val isSelected: Boolean = false,
    val isToday: Boolean = false,
    val isCurrentMonth: Boolean = true
)

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val daysInMonth: List<CalendarDayData> = emptyList(),
    val selectedDayTransactions: List<TransactionWithCategory> = emptyList(),
    val selectedDayIncome: Long = 0,
    val selectedDayExpense: Long = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadMonthData(YearMonth.now(), LocalDate.now())
    }

    fun nextMonth() {
        val next = _uiState.value.currentMonth.plusMonths(1)
        loadMonthData(next, next.atDay(1))
    }

    fun previousMonth() {
        val prev = _uiState.value.currentMonth.minusMonths(1)
        loadMonthData(prev, prev.atDay(1))
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        loadTransactionsForDate(date)
    }

    private fun loadMonthData(month: YearMonth, selectedDate: LocalDate) {
        val startEpoch = month.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endEpoch = month.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        viewModelScope.launch {
            transactionRepository.observeByDateRange(startEpoch, endEpoch).collect { transactions ->
                val today = LocalDate.now()
                val daysList = (1..month.lengthOfMonth()).map { dayNum ->
                    val date = month.atDay(dayNum)
                    val dayStart = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val dayEnd = date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                    val dayTxs = transactions.filter { it.date in dayStart..dayEnd }
                    val inc = dayTxs.filter { it.transactionType == "INCOME" }.sumOf { it.amount }
                    val exp = dayTxs.filter { it.transactionType != "INCOME" }.sumOf { it.amount }

                    CalendarDayData(
                        date = date,
                        hasIncome = inc > 0,
                        hasExpense = exp > 0,
                        totalIncome = inc,
                        totalExpense = exp,
                        isSelected = date == selectedDate,
                        isToday = date == today,
                        isCurrentMonth = true
                    )
                }

                _uiState.update {
                    it.copy(
                        currentMonth = month,
                        selectedDate = selectedDate,
                        daysInMonth = daysList,
                        isLoading = false
                    )
                }

                loadTransactionsForDate(selectedDate)
            }
        }
    }

    private fun loadTransactionsForDate(date: LocalDate) {
        val dayStart = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val dayEnd = date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        viewModelScope.launch {
            transactionRepository.observeByDateRange(dayStart, dayEnd).collect { txs ->
                val enriched = txs.map { tx ->
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
                val inc = txs.filter { it.transactionType == "INCOME" }.sumOf { it.amount }
                val exp = txs.filter { it.transactionType != "INCOME" }.sumOf { it.amount }

                _uiState.update {
                    it.copy(
                        selectedDayTransactions = enriched,
                        selectedDayIncome = inc,
                        selectedDayExpense = exp
                    )
                }
            }
        }
    }
}
