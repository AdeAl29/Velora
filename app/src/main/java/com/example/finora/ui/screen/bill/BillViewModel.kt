package com.example.finora.ui.screen.bill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finora.core.database.entity.BillEntity
import com.example.finora.core.database.entity.CategoryEntity
import com.example.finora.core.database.entity.WalletEntity
import com.example.finora.core.repository.BillRepository
import com.example.finora.core.repository.CategoryRepository
import com.example.finora.core.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BillListUiState(
    val unpaidBills: List<BillEntity> = emptyList(),
    val paidBills: List<BillEntity> = emptyList(),
    val totalUnpaid: Long = 0,
    val isLoading: Boolean = true
)

data class AddBillUiState(
    val name: String = "",
    val amount: String = "",
    val dueDate: Int = 10,
    val selectedCategoryId: Long? = null,
    val selectedWalletId: Long? = null,
    val reminderDaysBefore: Int = 3,
    val categories: List<CategoryEntity> = emptyList(),
    val wallets: List<WalletEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isEditing: Boolean = false,
    val editingBillId: Long? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class BillViewModel @Inject constructor(
    private val billRepository: BillRepository,
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(BillListUiState())
    val listState: StateFlow<BillListUiState> = _listState.asStateFlow()

    private val _addState = MutableStateFlow(AddBillUiState())
    val addState: StateFlow<AddBillUiState> = _addState.asStateFlow()

    init {
        loadBills()
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

    fun loadBills() {
        viewModelScope.launch {
            combine(
                billRepository.observeUnpaid(),
                billRepository.observePaid()
            ) { unpaid, paid ->
                BillListUiState(
                    unpaidBills = unpaid,
                    paidBills = paid,
                    totalUnpaid = unpaid.sumOf { it.amount },
                    isLoading = false
                )
            }.collect { state ->
                _listState.value = state
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

    fun setDueDate(day: Int) {
        _addState.update { it.copy(dueDate = day) }
    }

    fun setCategory(id: Long?) {
        _addState.update { it.copy(selectedCategoryId = id) }
    }

    fun setWallet(id: Long?) {
        _addState.update { it.copy(selectedWalletId = id) }
    }

    fun setReminderDays(days: Int) {
        _addState.update { it.copy(reminderDaysBefore = days) }
    }

    fun loadBillForEdit(id: Long) {
        viewModelScope.launch {
            val b = billRepository.getById(id) ?: return@launch
            _addState.update {
                it.copy(
                    isEditing = true,
                    editingBillId = id,
                    name = b.name,
                    amount = b.amount.toString(),
                    dueDate = b.dueDate,
                    selectedCategoryId = b.categoryId,
                    selectedWalletId = b.walletId,
                    reminderDaysBefore = b.reminderDaysBefore
                )
            }
        }
    }

    fun resetAddState() {
        _addState.value = AddBillUiState(
            categories = _addState.value.categories,
            wallets = _addState.value.wallets,
            selectedWalletId = _addState.value.wallets.firstOrNull()?.id
        )
    }

    fun saveBill() {
        val state = _addState.value
        val amountLong = state.amount.toLongOrNull()

        if (state.name.isBlank()) {
            _addState.update { it.copy(errorMessage = "Nama tagihan wajib diisi") }
            return
        }

        if (amountLong == null || amountLong <= 0) {
            _addState.update { it.copy(errorMessage = "Nominal tagihan harus lebih dari 0") }
            return
        }

        _addState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val bill = BillEntity(
                id = state.editingBillId ?: 0L,
                name = state.name.trim(),
                amount = amountLong,
                categoryId = state.selectedCategoryId,
                walletId = state.selectedWalletId,
                dueDate = state.dueDate,
                reminderDaysBefore = state.reminderDaysBefore
            )
            if (state.isEditing) {
                billRepository.update(bill)
            } else {
                billRepository.insert(bill)
            }
            _addState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }

    fun markPaid(billId: Long) {
        viewModelScope.launch {
            billRepository.markPaid(billId, System.currentTimeMillis())
        }
    }

    fun markUnpaid(billId: Long) {
        viewModelScope.launch {
            billRepository.markUnpaid(billId)
        }
    }

    fun deleteBill(bill: BillEntity) {
        viewModelScope.launch {
            billRepository.delete(bill)
        }
    }
}
