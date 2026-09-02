package com.example.finora.ui.screen.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finora.core.database.entity.WalletEntity
import com.example.finora.core.repository.WalletRepository
import com.example.finora.core.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.finora.core.audio.SoundEffectManager

data class TransferUiState(
    val wallets: List<WalletEntity> = emptyList(),
    val fromWalletId: Long? = null,
    val toWalletId: Long? = null,
    val amount: String = "",
    val note: String = "",
    val date: Long = DateUtils.today(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val soundEffectManager: SoundEffectManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    init {
        loadWallets()
    }

    private fun loadWallets() {
        viewModelScope.launch {
            walletRepository.observeAll().collect { list ->
                _uiState.update {
                    it.copy(
                        wallets = list,
                        fromWalletId = it.fromWalletId ?: list.firstOrNull()?.id,
                        toWalletId = it.toWalletId ?: list.getOrNull(1)?.id
                    )
                }
            }
        }
    }

    fun setFromWallet(id: Long) {
        _uiState.update { it.copy(fromWalletId = id, errorMessage = null) }
    }

    fun setToWallet(id: Long) {
        _uiState.update { it.copy(toWalletId = id, errorMessage = null) }
    }

    fun setAmount(amount: String) {
        val cleaned = amount.filter { it.isDigit() }
        _uiState.update { it.copy(amount = cleaned, errorMessage = null) }
    }

    fun setNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun transferFunds() {
        val state = _uiState.value
        val amountLong = state.amount.toLongOrNull()

        if (state.fromWalletId == null || state.toWalletId == null) {
            _uiState.update { it.copy(errorMessage = "Pilih rekening asal dan tujuan") }
            return
        }

        if (state.fromWalletId == state.toWalletId) {
            _uiState.update { it.copy(errorMessage = "Rekening asal dan tujuan tidak boleh sama") }
            return
        }

        if (amountLong == null || amountLong <= 0) {
            _uiState.update { it.copy(errorMessage = "Nominal transfer harus lebih dari 0") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                walletRepository.transfer(
                    fromWalletId = state.fromWalletId,
                    toWalletId = state.toWalletId,
                    amount = amountLong,
                    note = state.note.takeIf { it.isNotBlank() }
                )
                soundEffectManager.playWhoosh()
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}
