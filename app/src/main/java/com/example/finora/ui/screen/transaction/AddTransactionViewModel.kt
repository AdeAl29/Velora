package com.example.finora.ui.screen.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finora.core.audio.SoundEffectManager
import com.example.finora.core.database.entity.*
import com.example.finora.core.model.TransactionType
import com.example.finora.core.repository.*
import com.example.finora.core.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddTransactionUiState(
    val transactionType: TransactionType = TransactionType.EXPENSE_VARIABLE,
    val amount: String = "",
    val selectedCategoryId: Long? = null,
    val selectedWalletId: Long? = null,
    val date: Long = DateUtils.today(),
    val time: String = DateUtils.getCurrentTimeString(),
    val note: String = "",
    val isRecurring: Boolean = false,
    val categories: List<CategoryEntity> = emptyList(),
    val wallets: List<WalletEntity> = emptyList(),
    val selectedCategory: CategoryEntity? = null,
    val selectedWallet: WalletEntity? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false,
    val editingTransactionId: Long? = null,
    // Validation
    val amountError: String? = null,
    val categoryError: String? = null,
    val walletError: String? = null
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository,
    private val soundEffectManager: SoundEffectManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private var categoryJob: Job? = null

    init {
        loadWallets()
        filterCategoriesByType()
    }

    private fun loadWallets() {
        viewModelScope.launch {
            walletRepository.observeAll().collect { wallets ->
                _uiState.update { state ->
                    val currentId = state.selectedWalletId ?: wallets.firstOrNull()?.id
                    val currentWallet = wallets.find { it.id == currentId } ?: wallets.firstOrNull()
                    state.copy(
                        wallets = wallets,
                        selectedWalletId = currentId,
                        selectedWallet = currentWallet
                    )
                }
            }
        }
    }

    fun setTransactionType(type: TransactionType) {
        if (_uiState.value.transactionType == type && _uiState.value.categories.isNotEmpty()) return
        _uiState.update {
            it.copy(
                transactionType = type,
                selectedCategoryId = null,
                selectedCategory = null,
                categoryError = null
            )
        }
        filterCategoriesByType()
    }

    private fun filterCategoriesByType() {
        val type = _uiState.value.transactionType.name
        categoryJob?.cancel()
        categoryJob = viewModelScope.launch {
            categoryRepository.observeByType(type).collect { filtered ->
                _uiState.update { state ->
                    val validSelectedId = state.selectedCategoryId?.takeIf { id -> filtered.any { it.id == id } }
                        ?: filtered.firstOrNull()?.id
                    val validSelectedCat = filtered.find { it.id == validSelectedId }
                    state.copy(
                        categories = filtered,
                        selectedCategoryId = validSelectedId,
                        selectedCategory = validSelectedCat
                    )
                }
            }
        }
    }

    fun setAmount(amount: String) {
        val cleaned = amount.filter { it.isDigit() }
        _uiState.update { it.copy(amount = cleaned, amountError = null) }
    }

    fun selectCategory(category: CategoryEntity) {
        _uiState.update { it.copy(selectedCategoryId = category.id, selectedCategory = category, categoryError = null) }
    }

    fun selectWallet(wallet: WalletEntity) {
        _uiState.update { it.copy(selectedWalletId = wallet.id, selectedWallet = wallet, walletError = null) }
    }

    fun setDate(date: Long) {
        _uiState.update { it.copy(date = date) }
    }

    fun setTime(time: String) {
        _uiState.update { it.copy(time = time) }
    }

    fun setNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun setRecurring(isRecurring: Boolean) {
        _uiState.update { it.copy(isRecurring = isRecurring) }
    }

    fun loadTransaction(transactionId: Long) {
        viewModelScope.launch {
            val tx = transactionRepository.getById(transactionId) ?: return@launch
            val category = tx.categoryId?.let { categoryRepository.getById(it) }
            val wallet = walletRepository.getById(tx.walletId)
            val type = try { TransactionType.valueOf(tx.transactionType) } catch (e: Exception) { TransactionType.EXPENSE_VARIABLE }

            _uiState.update {
                it.copy(
                    isEditing = true,
                    editingTransactionId = transactionId,
                    transactionType = type,
                    amount = tx.amount.toString(),
                    selectedCategoryId = tx.categoryId,
                    selectedCategory = category,
                    selectedWalletId = tx.walletId,
                    selectedWallet = wallet,
                    date = tx.date,
                    time = tx.time,
                    note = tx.note ?: "",
                    isRecurring = tx.isRecurring
                )
            }
            filterCategoriesByType()
        }
    }

    fun applyScannedReceipt(scanned: com.example.finora.core.ocr.ScannedReceipt) {
        val matchingCat = scanned.detectedCategoryHint?.let { hint ->
            _uiState.value.categories.firstOrNull { it.name.equals(hint, ignoreCase = true) }
        }

        _uiState.update { state ->
            state.copy(
                amount = scanned.totalAmount?.toString() ?: state.amount,
                note = scanned.merchantName ?: state.note,
                date = scanned.date ?: state.date,
                selectedCategoryId = matchingCat?.id ?: state.selectedCategoryId,
                selectedCategory = matchingCat ?: state.selectedCategory,
                amountError = null,
                categoryError = null
            )
        }
    }

    fun save() {
        val state = _uiState.value

        var hasError = false
        val amountLong = state.amount.toLongOrNull()
        if (amountLong == null || amountLong <= 0) {
            _uiState.update { it.copy(amountError = "Nominal harus lebih dari 0") }
            hasError = true
        }
        if (state.selectedCategoryId == null) {
            _uiState.update { it.copy(categoryError = "Pilih kategori") }
            hasError = true
        }
        if (state.selectedWalletId == null) {
            _uiState.update { it.copy(walletError = "Pilih rekening") }
            hasError = true
        }
        if (hasError) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val transaction = TransactionEntity(
                    id = state.editingTransactionId ?: 0,
                    amount = amountLong!!,
                    transactionType = state.transactionType.name,
                    categoryId = state.selectedCategoryId,
                    walletId = state.selectedWalletId!!,
                    date = state.date,
                    time = state.time,
                    note = state.note.takeIf { it.isNotBlank() },
                    isRecurring = state.isRecurring,
                    updatedAt = System.currentTimeMillis()
                )

                if (state.isEditing) {
                    transactionRepository.update(transaction)
                } else {
                    transactionRepository.insert(transaction)
                }

                if (state.transactionType == TransactionType.INCOME) {
                    soundEffectManager.playChaChing()
                } else {
                    soundEffectManager.playSuccess()
                }

                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
