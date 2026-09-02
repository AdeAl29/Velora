package com.example.finora.core.database.dao

import androidx.room.*
import com.example.finora.core.database.entity.FinancialGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: FinancialGoalEntity): Long

    @Update
    suspend fun update(goal: FinancialGoalEntity)

    @Delete
    suspend fun delete(goal: FinancialGoalEntity)

    @Query("SELECT * FROM financial_goals WHERE id = :id")
    suspend fun getById(id: Long): FinancialGoalEntity?

    @Query("SELECT * FROM financial_goals WHERE id = :id")
    fun observeById(id: Long): Flow<FinancialGoalEntity?>

    @Query("SELECT * FROM financial_goals WHERE isCompleted = 0 ORDER BY priority ASC, deadline ASC")
    fun observeActive(): Flow<List<FinancialGoalEntity>>

    @Query("SELECT * FROM financial_goals ORDER BY priority ASC, deadline ASC")
    fun observeAll(): Flow<List<FinancialGoalEntity>>

    @Query("SELECT * FROM financial_goals WHERE isCompleted = 1 ORDER BY deadline DESC")
    fun observeCompleted(): Flow<List<FinancialGoalEntity>>

    @Query("UPDATE financial_goals SET currentAmount = currentAmount + :amount WHERE id = :id")
    suspend fun addFunds(id: Long, amount: Long)

    @Query("UPDATE financial_goals SET currentAmount = CASE WHEN currentAmount - :amount < 0 THEN 0 ELSE currentAmount - :amount END WHERE id = :id")
    suspend fun removeFunds(id: Long, amount: Long)

    @Query("UPDATE financial_goals SET isCompleted = 1 WHERE id = :id")
    suspend fun markCompleted(id: Long)

    @Query("SELECT COUNT(*) FROM financial_goals WHERE isCompleted = 0")
    fun observeActiveCount(): Flow<Int>

    @Query("SELECT * FROM financial_goals WHERE name LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<FinancialGoalEntity>>
}
