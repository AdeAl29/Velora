package com.example.finora.ui.screen.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finora.core.database.entity.CategoryEntity
import com.example.finora.core.database.entity.RecurringTransactionEntity
import com.example.finora.core.database.entity.WalletEntity
import com.example.finora.core.model.Frequency
import com.example.finora.core.repository.CategoryRepository
import com.example.finora.core.repository.RecurringTransactionRepository
import com.example.finora.core.repository.WalletRepository
import com.example.finora.core.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class RecurringWithDetails(
    val recurring: RecurringTransactionEntity,
    val category: CategoryEntity?,
    val wallet: WalletEntity?
)

data class RecurringListUiState(
    val items: List<RecurringWithDetails> = emptyList(),
    val totalMonthlyAmount: Long = 0,
    val isLoading: Boolean = true
)

data class AddRecurringUiState(
    val name: String = "",
    val amount: String = "",
    val transactionType: String = "EXPENSE_FIXED",
    val selectedCategoryId: Long? = null,
    val selectedWalletId: Long? = null,
    val frequency: Frequency = Frequency.MONTHLY,
    val startDate: Long = DateUtils.today(),
    val categories: List<CategoryEntity> = emptyList(),
    val wallets: List<WalletEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isEditing: Boolean = false,
    val editingId: Long? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class RecurringViewModel @Inject constructor(
    private val recurringRepository: RecurringTransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(RecurringListUiState())
    val listState: StateFlow<RecurringListUiState> = _listState.asStateFlow()

    private val _addState = MutableStateFlow(AddRecurringUiState())
    val addState: StateFlow<AddRecurringUiState> = _addState.asStateFlow()

    init {
        loadRecurring()
        loadCategoriesAndWallets()
    }

    private fun loadCategoriesAndWallets() {
        viewModelScope.launch {
            categoryRepository.observeAll().collect { cats ->
                _addState.update { it.copy(categories = cats.filter { c -> c.type != "INCOME" }) }
            }
        }
        viewModelScope.launch {
            walletRepository.observeAll().collect { wList ->
                _addState.update {
                    it.copy(
                        wallets = wList,
                        selectedWalletId = it.selectedWalletId ?: wList.firstOrNull()?.id
                    )
                }
            }
        }
    }

    fun loadRecurring() {
        viewModelScope.launch {
            recurringRepository.observeAll().collect { list ->
                val detailed = list.map { rec ->
                    val cat = categoryRepository.getById(rec.categoryId)
                    val w = walletRepository.getById(rec.walletId)
                    RecurringWithDetails(rec, cat, w)
                }
                val monthlyTotal = detailed.sumOf { it.recurring.amount }
                _listState.value = RecurringListUiState(
                    items = detailed,
                    totalMonthlyAmount = monthlyTotal,
                    isLoading = false
                )
            }
        }
    }

    fun setName(name: String) {
        _addState.update { it.copy(name = name, errorMessage = null) }
    }

    fun setAmount(amount: String) {
        val cleaned = amount.filter { it.isDigit() }
        _addState.update { it.copy(amount = cleaned, errorMessage = null) }
    }

    fun setCategory(id: Long?) {
        _addState.update { it.copy(selectedCategoryId = id) }
    }

    fun setWallet(id: Long?) {
        _addState.update { it.copy(selectedWalletId = id) }
    }

    fun setFrequency(freq: Frequency) {
        _addState.update { it.copy(frequency = freq) }
    }

    fun setStartDate(date: Long) {
        _addState.update { it.copy(startDate = date) }
    }

    fun loadForEdit(id: Long) {
        viewModelScope.launch {
            val r = recurringRepository.getById(id) ?: return@launch
            val freq = try { Frequency.valueOf(r.frequency) } catch (_: Exception) { Frequency.MONTHLY }
            _addState.update {
                it.copy(
                    isEditing = true,
                    editingId = id,
                    name = r.name,
                    amount = r.amount.toString(),
                    transactionType = r.transactionType,
                    selectedCategoryId = r.categoryId,
                    selectedWalletId = r.walletId,
                    frequency = freq,
                    startDate = r.startDate
                )
            }
        }
    }

    fun resetAddState() {
        _addState.value = AddRecurringUiState(
            categories = _addState.value.categories,
            wallets = _addState.value.wallets,
            selectedWalletId = _addState.value.wallets.firstOrNull()?.id,
            selectedCategoryId = _addState.value.categories.firstOrNull()?.id
        )
    }

    fun saveRecurring() {
        val state = _addState.value
        val amountLong = state.amount.toLongOrNull()

        if (state.name.isBlank()) {
            _addState.update { it.copy(errorMessage = "Nama pengeluaran tetap wajib diisi") }
            return
        }

        if (amountLong == null || amountLong <= 0) {
            _addState.update { it.copy(errorMessage = "Nominal harus lebih dari 0") }
            return
        }

        if (state.selectedCategoryId == null || state.selectedWalletId == null) {
            _addState.update { it.copy(errorMessage = "Pilih kategori dan rekening") }
            return
        }

        _addState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val startLocalDate = DateUtils.toLocalDate(state.startDate)
            val nextLocalDate = when (state.frequency) {
                Frequency.DAILY -> startLocalDate.plusDays(1)
                Frequency.WEEKLY -> startLocalDate.plusWeeks(1)
                Frequency.MONTHLY -> startLocalDate.plusMonths(1)
                Frequency.YEARLY -> startLocalDate.plusYears(1)
            }
            val nextDate = DateUtils.toEpochMillis(nextLocalDate)
            val rec = RecurringTransactionEntity(
                id = state.editingId ?: 0L,
                name = state.name.trim(),
                amount = amountLong,
                transactionType = state.transactionType,
                categoryId = state.selectedCategoryId,
                walletId = state.selectedWalletId,
                frequency = state.frequency.name,
                startDate = state.startDate,
                nextExecutionDate = nextDate
            )
            if (state.isEditing) {
                recurringRepository.update(rec)
            } else {
                recurringRepository.insert(rec)
            }
            _addState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }

    fun deleteRecurring(entity: RecurringTransactionEntity) {
        viewModelScope.launch {
            recurringRepository.delete(entity)
        }
    }
}
