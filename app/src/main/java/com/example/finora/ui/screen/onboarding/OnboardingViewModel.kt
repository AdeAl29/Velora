package com.example.finora.ui.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finora.core.database.entity.WalletEntity
import com.example.finora.core.datastore.FinoraDataStore
import com.example.finora.core.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentStep: Int = 0,
    val isSetupPhase: Boolean = false,
    val userName: String = "",
    val currencyCode: String = "IDR",
    val walletName: String = "Cash Utama",
    val initialBalance: String = "0",
    val wantBudget: Boolean = true,
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val dataStore: FinoraDataStore,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun nextStep() {
        if (_uiState.value.currentStep < 3) {
            _uiState.update { it.copy(currentStep = it.currentStep + 1) }
        } else {
            _uiState.update { it.copy(isSetupPhase = true) }
        }
    }

    fun prevStep() {
        if (_uiState.value.currentStep > 0) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    fun setUserName(name: String) {
        _uiState.update { it.copy(userName = name, error = null) }
    }

    fun setCurrencyCode(code: String) {
        _uiState.update { it.copy(currencyCode = code) }
    }

    fun setWalletName(name: String) {
        _uiState.update { it.copy(walletName = name, error = null) }
    }

    fun setInitialBalance(balance: String) {
        val cleaned = balance.filter { it.isDigit() }
        _uiState.update { it.copy(initialBalance = cleaned) }
    }

    fun setWantBudget(want: Boolean) {
        _uiState.update { it.copy(wantBudget = want) }
    }

    fun completeSetup() {
        val state = _uiState.value
        if (state.userName.isBlank()) {
            _uiState.update { it.copy(error = "Masukkan nama panggilan Anda") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            // Save initial wallet
            val initBal = state.initialBalance.toLongOrNull() ?: 0L
            walletRepository.insert(
                WalletEntity(
                    name = state.walletName.ifBlank { "Cash Utama" },
                    iconName = "cash",
                    initialBalance = initBal,
                    colorHex = "#34D399"
                )
            )

            // Save user preferences
            dataStore.setUserName(state.userName.trim())
            dataStore.setCurrencyCode(state.currencyCode)
            dataStore.setOnboardingCompleted(true)

            _uiState.update { it.copy(isLoading = false, isCompleted = true) }
        }
    }
}
