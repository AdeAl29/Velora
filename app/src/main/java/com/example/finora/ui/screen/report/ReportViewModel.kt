package com.example.finora.ui.screen.report

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finora.core.database.entity.TransactionEntity
import com.example.finora.core.export.CsvExporter
import com.example.finora.core.export.PdfExporter
import com.example.finora.core.repository.CategoryRepository
import com.example.finora.core.repository.TransactionRepository
import com.example.finora.core.repository.WalletRepository
import com.example.finora.core.util.DateUtils
import com.example.finora.ui.screen.statistics.StatPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportUiState(
    val period: StatPeriod = StatPeriod.THIS_MONTH,
    val totalIncome: Long = 0,
    val totalExpense: Long = 0,
    val totalFixedExpense: Long = 0,
    val totalVariableExpense: Long = 0,
    val totalSavings: Long = 0,
    val transactions: List<TransactionEntity> = emptyList(),
    val isExporting: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    private var reportJob: kotlinx.coroutines.Job? = null

    init {
        loadReport(StatPeriod.THIS_MONTH)
    }

    fun setPeriod(period: StatPeriod) {
        _uiState.update { it.copy(period = period, isLoading = true) }
        loadReport(period)
    }

    private fun loadReport(period: StatPeriod) {
        val (startDate, endDate) = when (period) {
            StatPeriod.THIS_WEEK -> DateUtils.getStartOfWeek() to System.currentTimeMillis()
            StatPeriod.THIS_MONTH -> DateUtils.getStartOfMonth() to DateUtils.getEndOfMonth()
            StatPeriod.LAST_MONTH -> DateUtils.getStartOfLastMonth() to DateUtils.getEndOfLastMonth()
            StatPeriod.THREE_MONTHS -> DateUtils.getStartOfNMonthsAgo(3) to System.currentTimeMillis()
            StatPeriod.SIX_MONTHS -> DateUtils.getStartOfNMonthsAgo(6) to System.currentTimeMillis()
            StatPeriod.THIS_YEAR -> DateUtils.getStartOfYear() to DateUtils.getEndOfYear()
        }

        reportJob?.cancel()
        reportJob = viewModelScope.launch {
            combine(
                transactionRepository.observeTotalIncome(startDate, endDate),
                transactionRepository.observeTotalExpense(startDate, endDate),
                transactionRepository.observeTotalFixedExpense(startDate, endDate),
                transactionRepository.observeTotalVariableExpense(startDate, endDate),
                transactionRepository.observeByDateRange(startDate, endDate)
            ) { inc, exp, fixed, variable, txs ->
                ReportUiState(
                    period = period,
                    totalIncome = inc,
                    totalExpense = exp,
                    totalFixedExpense = fixed,
                    totalVariableExpense = variable,
                    totalSavings = (inc - exp).coerceAtLeast(0L),
                    transactions = txs,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun exportCsv(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val file = CsvExporter.exportTransactionsToCsv(
                context = context,
                transactions = _uiState.value.transactions,
                categoryRepository = categoryRepository,
                walletRepository = walletRepository
            )
            _uiState.update { it.copy(isExporting = false) }
            CsvExporter.shareFile(context, file, "text/csv")
        }
    }

    fun exportPdf(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val state = _uiState.value
            val file = PdfExporter.generatePdfReport(
                context = context,
                title = "Laporan Keuangan",
                periodName = state.period.label,
                totalIncome = state.totalIncome,
                totalExpense = state.totalExpense,
                transactions = state.transactions,
                categoryRepository = categoryRepository,
                walletRepository = walletRepository
            )
            _uiState.update { it.copy(isExporting = false) }
            CsvExporter.shareFile(context, file, "application/pdf")
        }
    }
}
