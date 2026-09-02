package com.example.finora.core.database.dao

import androidx.room.*
import com.example.finora.core.database.entity.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recurring: RecurringTransactionEntity): Long

    @Update
    suspend fun update(recurring: RecurringTransactionEntity)

    @Delete
    suspend fun delete(recurring: RecurringTransactionEntity)

    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getById(id: Long): RecurringTransactionEntity?

    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 ORDER BY nextExecutionDate ASC")
    fun observeAll(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 AND nextExecutionDate <= :currentDate")
    suspend fun getDueRecurring(currentDate: Long): List<RecurringTransactionEntity>

    @Query("UPDATE recurring_transactions SET lastExecutedDate = :executedDate, nextExecutionDate = :nextDate WHERE id = :id")
    suspend fun updateExecutionDates(id: Long, executedDate: Long, nextDate: Long)

    @Query("UPDATE recurring_transactions SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)

    @Query("SELECT COUNT(*) FROM recurring_transactions WHERE isActive = 1")
    fun observeCount(): Flow<Int>
}
