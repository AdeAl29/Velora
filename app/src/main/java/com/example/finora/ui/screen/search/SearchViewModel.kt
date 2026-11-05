package com.example.finora.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finora.core.database.entity.*
import com.example.finora.core.repository.*
import com.example.finora.ui.screen.home.TransactionWithCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchResultUiState(
    val query: String = "",
    val transactions: List<TransactionWithCategory> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val wallets: List<WalletEntity> = emptyList(),
    val goals: List<FinancialGoalEntity> = emptyList(),
    val bills: List<BillEntity> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository,
    private val goalRepository: GoalRepository,
    private val billRepository: BillRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchResultUiState())
    val uiState: StateFlow<SearchResultUiState> = _uiState.asStateFlow()

    private var searchJob: kotlinx.coroutines.Job? = null

    fun setQuery(query: String) {
        _uiState.update { it.copy(query = query) }
        if (query.isBlank()) {
            searchJob?.cancel()
            _uiState.update {
                it.copy(
                    transactions = emptyList(),
                    categories = emptyList(),
                    wallets = emptyList(),
                    goals = emptyList(),
                    bills = emptyList(),
                    hasSearched = false
                )
            }
            return
        }

        performSearch(query.trim())
    }

    private fun performSearch(query: String) {
        _uiState.update { it.copy(isSearching = true, hasSearched = true) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            combine(
                transactionRepository.search(query),
                categoryRepository.search(query),
                walletRepository.search(query),
                goalRepository.search(query),
                billRepository.search(query)
            ) { txs, cats, wList, gList, bList ->
                val enrichedTxs = txs.map { tx ->
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

                SearchResultUiState(
                    query = query,
                    transactions = enrichedTxs,
                    categories = cats,
                    wallets = wList,
                    goals = gList,
                    bills = bList,
                    isSearching = false,
                    hasSearched = true
                )
            }.collect { result ->
                _uiState.value = result
            }
        }
    }
}
