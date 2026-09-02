package com.example.finora.ui.screen.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finora.core.database.entity.CategoryEntity
import com.example.finora.core.database.entity.TransactionEntity
import com.example.finora.core.database.entity.WalletEntity
import com.example.finora.core.repository.CategoryRepository
import com.example.finora.core.repository.TransactionRepository
import com.example.finora.core.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionDetailUiState(
    val transaction: TransactionEntity? = null,
    val category: CategoryEntity? = null,
    val wallet: WalletEntity? = null,
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false
)

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionDetailUiState())
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    fun loadTransaction(transactionId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val tx = transactionRepository.getById(transactionId)
            if (tx != null) {
                val category = tx.categoryId?.let { categoryRepository.getById(it) }
                val wallet = walletRepository.getById(tx.walletId)
                _uiState.update {
                    it.copy(
                        transaction = tx,
                        category = category,
                        wallet = wallet,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun deleteTransaction() {
        val tx = _uiState.value.transaction ?: return
        viewModelScope.launch {
            transactionRepository.delete(tx)
            _uiState.update { it.copy(isDeleted = true) }
        }
    }
}
