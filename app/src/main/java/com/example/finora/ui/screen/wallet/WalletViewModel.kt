package com.example.finora.ui.screen.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finora.core.database.entity.WalletEntity
import com.example.finora.core.repository.WalletRepository
import com.example.finora.ui.screen.home.WalletWithBalance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletListUiState(
    val wallets: List<WalletWithBalance> = emptyList(),
    val totalBalance: Long = 0,
    val isLoading: Boolean = true
)

data class AddWalletUiState(
    val name: String = "",
    val iconName: String = "bank",
    val colorHex: String = "#6C63FF",
    val initialBalance: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isEditing: Boolean = false,
    val editingWalletId: Long? = null,
    val nameError: String? = null
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(WalletListUiState())
    val listState: StateFlow<WalletListUiState> = _listState.asStateFlow()

    private val _addState = MutableStateFlow(AddWalletUiState())
    val addState: StateFlow<AddWalletUiState> = _addState.asStateFlow()

    init {
        loadWallets()
    }

    fun loadWallets() {
        viewModelScope.launch {
            walletRepository.observeAll().collect { walletEntities ->
                val list = walletEntities.map { wallet ->
                    val balance = walletRepository.getWalletBalance(wallet.id)
                    WalletWithBalance(wallet, balance)
                }
                val total = list.sumOf { it.balance }
                _listState.update {
                    it.copy(
                        wallets = list,
                        totalBalance = total,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setName(name: String) {
        _addState.update { it.copy(name = name, nameError = null) }
    }

    fun setIconName(iconName: String) {
        _addState.update { it.copy(iconName = iconName) }
    }

    fun setColorHex(colorHex: String) {
        _addState.update { it.copy(colorHex = colorHex) }
    }

    fun setInitialBalance(balance: String) {
        val cleaned = balance.filter { it.isDigit() }
        _addState.update { it.copy(initialBalance = cleaned) }
    }

    fun loadWalletForEdit(walletId: Long) {
        viewModelScope.launch {
            val wallet = walletRepository.getById(walletId) ?: return@launch
            _addState.update {
                it.copy(
                    isEditing = true,
                    editingWalletId = walletId,
                    name = wallet.name,
                    iconName = wallet.iconName,
                    colorHex = wallet.colorHex,
                    initialBalance = wallet.initialBalance.toString()
                )
            }
        }
    }

    fun resetAddState() {
        _addState.value = AddWalletUiState()
    }

    fun saveWallet() {
        val state = _addState.value
        if (state.name.isBlank()) {
            _addState.update { it.copy(nameError = "Nama rekening wajib diisi") }
            return
        }

        _addState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val initialBal = state.initialBalance.toLongOrNull() ?: 0L
            val wallet = WalletEntity(
                id = state.editingWalletId ?: 0L,
                name = state.name.trim(),
                iconName = state.iconName,
                initialBalance = initialBal,
                colorHex = state.colorHex
            )
            if (state.isEditing) {
                walletRepository.update(wallet)
            } else {
                walletRepository.insert(wallet)
            }
            _addState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }

    fun deleteWallet(walletId: Long) {
        viewModelScope.launch {
            val wallet = walletRepository.getById(walletId) ?: return@launch
            walletRepository.delete(wallet)
        }
    }
}
