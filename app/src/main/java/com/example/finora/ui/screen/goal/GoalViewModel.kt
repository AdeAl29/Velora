package com.example.finora.ui.screen.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finora.core.database.entity.FinancialGoalEntity
import com.example.finora.core.repository.GoalRepository
import com.example.finora.core.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class GoalListUiState(
    val activeGoals: List<FinancialGoalEntity> = emptyList(),
    val completedGoals: List<FinancialGoalEntity> = emptyList(),
    val totalTarget: Long = 0,
    val totalCollected: Long = 0,
    val isLoading: Boolean = true
)

data class AddGoalUiState(
    val name: String = "",
    val targetAmount: String = "",
    val currentAmount: String = "",
    val iconName: String = "laptop",
    val colorHex: String = "#6C63FF",
    val deadline: Long? = null,
    val priority: Int = 2,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isEditing: Boolean = false,
    val editingGoalId: Long? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(GoalListUiState())
    val listState: StateFlow<GoalListUiState> = _listState.asStateFlow()

    private val _addState = MutableStateFlow(AddGoalUiState())
    val addState: StateFlow<AddGoalUiState> = _addState.asStateFlow()

    private val _selectedGoal = MutableStateFlow<FinancialGoalEntity?>(null)
    val selectedGoal: StateFlow<FinancialGoalEntity?> = _selectedGoal.asStateFlow()

    init {
        loadGoals()
    }

    fun loadGoals() {
        viewModelScope.launch {
            combine(
                goalRepository.observeActive(),
                goalRepository.observeCompleted()
            ) { active, completed ->
                val all = active + completed
                val target = all.sumOf { it.targetAmount }
                val collected = all.sumOf { it.currentAmount }
                GoalListUiState(
                    activeGoals = active,
                    completedGoals = completed,
                    totalTarget = target,
                    totalCollected = collected,
                    isLoading = false
                )
            }.collect { state ->
                _listState.value = state
            }
        }
    }

    fun loadGoalDetail(id: Long) {
        viewModelScope.launch {
            goalRepository.observeById(id).collect { goal ->
                _selectedGoal.value = goal
            }
        }
    }

    fun setName(name: String) {
        _addState.update { it.copy(name = name, errorMessage = null) }
    }

    fun setTargetAmount(amount: String) {
        val cleaned = amount.filter { it.isDigit() }
        _addState.update { it.copy(targetAmount = cleaned, errorMessage = null) }
    }

    fun setCurrentAmount(amount: String) {
        val cleaned = amount.filter { it.isDigit() }
        _addState.update { it.copy(currentAmount = cleaned) }
    }

    fun setIconName(icon: String) {
        _addState.update { it.copy(iconName = icon) }
    }

    fun setColorHex(color: String) {
        _addState.update { it.copy(colorHex = color) }
    }

    fun setDeadline(deadline: Long?) {
        _addState.update { it.copy(deadline = deadline) }
    }

    fun setPriority(priority: Int) {
        _addState.update { it.copy(priority = priority) }
    }

    fun loadGoalForEdit(id: Long) {
        viewModelScope.launch {
            val g = goalRepository.getById(id) ?: return@launch
            _addState.update {
                it.copy(
                    isEditing = true,
                    editingGoalId = id,
                    name = g.name,
                    targetAmount = g.targetAmount.toString(),
                    currentAmount = g.currentAmount.toString(),
                    iconName = g.iconName,
                    colorHex = g.colorHex,
                    deadline = g.deadline,
                    priority = g.priority
                )
            }
        }
    }

    fun resetAddState() {
        val nextYear = LocalDate.now().plusMonths(6).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        _addState.value = AddGoalUiState(deadline = nextYear)
    }

    fun saveGoal() {
        val state = _addState.value
        val targetLong = state.targetAmount.toLongOrNull()
        val currentLong = state.currentAmount.toLongOrNull() ?: 0L

        if (state.name.isBlank()) {
            _addState.update { it.copy(errorMessage = "Nama tujuan wajib diisi") }
            return
        }

        if (targetLong == null || targetLong <= 0) {
            _addState.update { it.copy(errorMessage = "Target dana harus lebih dari 0") }
            return
        }

        _addState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val goal = FinancialGoalEntity(
                id = state.editingGoalId ?: 0L,
                name = state.name.trim(),
                targetAmount = targetLong,
                currentAmount = currentLong,
                iconName = state.iconName,
                colorHex = state.colorHex,
                deadline = state.deadline,
                priority = state.priority,
                isCompleted = currentLong >= targetLong
            )
            if (state.isEditing) {
                goalRepository.update(goal)
            } else {
                goalRepository.insert(goal)
            }
            _addState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }

    fun addFunds(goalId: Long, amount: Long) {
        viewModelScope.launch {
            goalRepository.addFunds(goalId, amount)
            val updated = goalRepository.getById(goalId)
            if (updated != null && updated.currentAmount >= updated.targetAmount) {
                goalRepository.markCompleted(goalId)
            }
        }
    }

    fun removeFunds(goalId: Long, amount: Long) {
        viewModelScope.launch {
            goalRepository.removeFunds(goalId, amount)
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            val g = goalRepository.getById(goalId) ?: return@launch
            goalRepository.delete(g)
        }
    }
}
