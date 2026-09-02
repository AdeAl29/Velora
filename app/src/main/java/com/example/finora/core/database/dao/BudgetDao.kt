package com.example.finora.core.database.dao

import androidx.room.*
import com.example.finora.core.database.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getById(id: Long): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE isActive = 1 ORDER BY id ASC")
    fun observeAll(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND isActive = 1")
    fun observeByCategoryId(categoryId: Long): Flow<BudgetEntity?>

    @Query("SELECT COUNT(*) FROM budgets WHERE isActive = 1")
    fun observeCount(): Flow<Int>
}
