package com.example.finora.ui.screen.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finora.core.database.entity.*
import com.example.finora.core.repository.*
import com.example.finora.core.util.DateUtils
import com.example.finora.ui.screen.home.TransactionWithCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionListUiState(
    val transactions: Map<String, List<TransactionWithCategory>> = emptyMap(),
    val categories: List<CategoryEntity> = emptyList(),
    val wallets: List<WalletEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedTypeFilter: String? = null,
    val selectedCategoryId: Long? = null,
    val selectedWalletId: Long? = null,
    val sortByAmount: Boolean = false,
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false
)

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionListUiState())
    val uiState: StateFlow<TransactionListUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            categoryRepository.observeAll().collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
        viewModelScope.launch {
            walletRepository.observeAll().collect { wallets ->
                _uiState.update { it.copy(wallets = wallets) }
            }
        }
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            val state = _uiState.value
            val flow = if (state.searchQuery.isNotBlank()) {
                transactionRepository.search(state.searchQuery)
            } else {
                transactionRepository.observeAll()
            }

            flow.collect { transactions ->
                var filtered = transactions

                // Apply filters
                state.selectedTypeFilter?.let { type ->
                    filtered = filtered.filter { it.transactionType == type }
                }
                state.selectedCategoryId?.let { catId ->
                    filtered = filtered.filter { it.categoryId == catId }
                }
                state.selectedWalletId?.let { walletId ->
                    filtered = filtered.filter { it.walletId == walletId }
                }

                if (state.sortByAmount) {
                    filtered = filtered.sortedByDescending { it.amount }
                }

                // Enrich with category/wallet data
                val enriched = filtered.map { tx ->
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

                // Group by date
                val grouped = enriched.groupBy { DateUtils.getDateGroupKey(it.transaction.date) }

                _uiState.update {
                    it.copy(
                        transactions = grouped,
                        isLoading = false,
                        isEmpty = grouped.isEmpty()
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadTransactions()
    }

    fun setTypeFilter(type: String?) {
        _uiState.update { it.copy(selectedTypeFilter = type) }
        loadTransactions()
    }

    fun setCategoryFilter(categoryId: Long?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        loadTransactions()
    }

    fun setWalletFilter(walletId: Long?) {
        _uiState.update { it.copy(selectedWalletId = walletId) }
        loadTransactions()
    }

    fun toggleSortByAmount() {
        _uiState.update { it.copy(sortByAmount = !it.sortByAmount) }
        loadTransactions()
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            transactionRepository.deleteById(transactionId)
        }
    }
}
