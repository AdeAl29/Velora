package com.example.finora.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finora.core.database.dao.*
import com.example.finora.core.datastore.FinoraDataStore
import com.example.finora.core.model.CurrencyInfo
import com.example.finora.core.util.SampleData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userName: String = "",
    val currencyCode: String = "IDR",
    val themeMode: String = "system",
    val isBillReminderEnabled: Boolean = true,
    val isBudgetWarningEnabled: Boolean = true,
    val isGoalReminderEnabled: Boolean = true,
    val isSampleDataLoaded: Boolean = false,
    val isAppLockEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val isSoundEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val successMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: FinoraDataStore,
    private val transactionDao: TransactionDao,
    private val walletDao: WalletDao,
    private val transferDao: TransferDao,
    private val budgetDao: BudgetDao,
    private val goalDao: GoalDao,
    private val billDao: BillDao,
    private val recurringTransactionDao: RecurringTransactionDao,
    private val categoryDao: CategoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dataStore.userName.collect { name ->
                _uiState.update { it.copy(userName = name) }
            }
        }
        viewModelScope.launch {
            dataStore.currencyCode.collect { curr ->
                _uiState.update { it.copy(currencyCode = curr) }
            }
        }
        viewModelScope.launch {
            dataStore.themeMode.collect { theme ->
                _uiState.update { it.copy(themeMode = theme) }
            }
        }
        viewModelScope.launch {
            dataStore.isBillReminderEnabled.collect { billRem ->
                _uiState.update { it.copy(isBillReminderEnabled = billRem) }
            }
        }
        viewModelScope.launch {
            dataStore.isBudgetWarningEnabled.collect { budgWarn ->
                _uiState.update { it.copy(isBudgetWarningEnabled = budgWarn) }
            }
        }
        viewModelScope.launch {
            dataStore.isGoalReminderEnabled.collect { goalRem ->
                _uiState.update { it.copy(isGoalReminderEnabled = goalRem) }
            }
        }
        viewModelScope.launch {
            dataStore.isSampleDataLoaded.collect { sampleLoaded ->
                _uiState.update { it.copy(isSampleDataLoaded = sampleLoaded) }
            }
        }
        viewModelScope.launch {
            dataStore.isBiometricEnabled.collect { bio ->
                _uiState.update { it.copy(isBiometricEnabled = bio) }
            }
        }
        viewModelScope.launch {
            dataStore.isSoundEnabled.collect { snd ->
                _uiState.update { it.copy(isSoundEnabled = snd) }
            }
        }
    }

    fun setBiometric(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setBiometricEnabled(enabled)
        }
    }

    fun setSound(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setSoundEnabled(enabled)
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch {
            dataStore.setUserName(name)
        }
    }

    fun setCurrencyCode(code: String) {
        viewModelScope.launch {
            dataStore.setCurrencyCode(code)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            dataStore.setThemeMode(mode)
        }
    }

    fun setBillReminder(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setBillReminderEnabled(enabled)
        }
    }

    fun setBudgetWarning(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setBudgetWarningEnabled(enabled)
        }
    }

    fun setGoalReminder(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setGoalReminderEnabled(enabled)
        }
    }

    fun loadSampleData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            SampleData.loadSampleData(
                transactionDao = transactionDao,
                walletDao = walletDao,
                transferDao = transferDao,
                budgetDao = budgetDao,
                goalDao = goalDao,
                billDao = billDao,
                recurringTransactionDao = recurringTransactionDao,
                categoryDao = categoryDao
            )
            dataStore.setSampleDataLoaded(true)
            _uiState.update { it.copy(isLoading = false, successMessage = "Data demo berhasil dimuat!") }
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            SampleData.clearAllData(
                transactionDao = transactionDao,
                walletDao = walletDao,
                transferDao = transferDao,
                budgetDao = budgetDao,
                goalDao = goalDao,
                billDao = billDao,
                recurringTransactionDao = recurringTransactionDao
            )
            dataStore.setSampleDataLoaded(false)
            _uiState.update { it.copy(isLoading = false, successMessage = "Semua data berhasil dibersihkan!") }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
