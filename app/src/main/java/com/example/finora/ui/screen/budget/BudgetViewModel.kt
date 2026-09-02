package com.example.finora.ui.screen.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finora.core.database.entity.BudgetEntity
import com.example.finora.core.database.entity.CategoryEntity
import com.example.finora.core.repository.BudgetRepository
import com.example.finora.core.repository.CategoryRepository
import com.example.finora.core.repository.TransactionRepository
import com.example.finora.core.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BudgetWithDetails(
    val budget: BudgetEntity,
    val category: CategoryEntity?,
    val spentAmount: Long,
    val percentage: Float
)

data class BudgetListUiState(
    val budgets: List<BudgetWithDetails> = emptyList(),
    val totalBudget: Long = 0,
    val totalSpent: Long = 0,
    val isLoading: Boolean = true
)

data class AddBudgetUiState(
    val selectedCategoryId: Long? = null,
    val amount: String = "",
    val period: String = "MONTHLY",
    val categories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isEditing: Boolean = false,
    val editingBudgetId: Long? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(BudgetListUiState())
    val listState: StateFlow<BudgetListUiState> = _listState.asStateFlow()

    private val _addState = MutableStateFlow(AddBudgetUiState())
    val addState: StateFlow<AddBudgetUiState> = _addState.asStateFlow()

    init {
        loadBudgets()
        loadExpenseCategories()
    }

    private fun loadExpenseCategories() {
        viewModelScope.launch {
            categoryRepository.observeAll().collect { list ->
                val expenseCats = list.filter { it.type != "INCOME" }
                _addState.update {
                    it.copy(
                        categories = expenseCats,
                        selectedCategoryId = it.selectedCategoryId ?: expenseCats.firstOrNull()?.id
                    )
                }
            }
        }
    }

    fun loadBudgets() {
        val startOfMonth = DateUtils.getStartOfMonth()
        val endOfMonth = DateUtils.getEndOfMonth()

        viewModelScope.launch {
            budgetRepository.observeAll().collect { budgetList ->
                val detailed = budgetList.map { budget ->
                    val category = categoryRepository.getById(budget.categoryId)
                    val spent = transactionRepository.observeExpenseByCategory(
                        budget.categoryId,
                        startOfMonth,
                        endOfMonth
                    ).first()
                    val pct = if (budget.amount > 0) (spent.toFloat() / budget.amount * 100) else 0f
                    BudgetWithDetails(budget, category, spent, pct)
                }
                val totalB = detailed.sumOf { it.budget.amount }
                val totalS = detailed.sumOf { it.spentAmount }
                _listState.update {
                    it.copy(
                        budgets = detailed,
                        totalBudget = totalB,
                        totalSpent = totalS,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectCategory(categoryId: Long) {
        _addState.update { it.copy(selectedCategoryId = categoryId, errorMessage = null) }
    }

    fun setAmount(amount: String) {
        val cleaned = amount.filter { it.isDigit() }
        _addState.update { it.copy(amount = cleaned, errorMessage = null) }
    }

    fun setPeriod(period: String) {
        _addState.update { it.copy(period = period) }
    }

    fun loadBudgetForEdit(budgetId: Long) {
        viewModelScope.launch {
            val b = budgetRepository.getById(budgetId) ?: return@launch
            _addState.update {
                it.copy(
                    isEditing = true,
                    editingBudgetId = budgetId,
                    selectedCategoryId = b.categoryId,
                    amount = b.amount.toString(),
                    period = b.period
                )
            }
        }
    }

    fun resetAddState() {
        _addState.update {
            AddBudgetUiState(categories = it.categories, selectedCategoryId = it.categories.firstOrNull()?.id)
        }
    }

    fun saveBudget() {
        val state = _addState.value
        val amountLong = state.amount.toLongOrNull()

        if (state.selectedCategoryId == null) {
            _addState.update { it.copy(errorMessage = "Pilih kategori anggaran") }
            return
        }

        if (amountLong == null || amountLong <= 0) {
            _addState.update { it.copy(errorMessage = "Nominal anggaran harus lebih dari 0") }
            return
        }

        _addState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val budget = BudgetEntity(
                id = state.editingBudgetId ?: 0L,
                categoryId = state.selectedCategoryId,
                amount = amountLong,
                period = state.period,
                startDate = DateUtils.getStartOfMonth()
            )
            if (state.isEditing) {
                budgetRepository.update(budget)
            } else {
                budgetRepository.insert(budget)
            }
            _addState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            budgetRepository.delete(budget)
        }
    }
}
