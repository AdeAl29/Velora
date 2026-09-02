package com.example.finora.core.database.dao

import androidx.room.*
import com.example.finora.core.database.entity.BillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bill: BillEntity): Long

    @Update
    suspend fun update(bill: BillEntity)

    @Delete
    suspend fun delete(bill: BillEntity)

    @Query("SELECT * FROM bills WHERE id = :id")
    suspend fun getById(id: Long): BillEntity?

    @Query("SELECT * FROM bills WHERE isActive = 1 ORDER BY dueDate ASC")
    fun observeAll(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE isActive = 1 AND isPaid = 0 ORDER BY dueDate ASC")
    fun observeUnpaid(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE isActive = 1 AND isPaid = 1 ORDER BY lastPaidDate DESC")
    fun observePaid(): Flow<List<BillEntity>>

    @Query("UPDATE bills SET isPaid = 1, lastPaidDate = :paidDate WHERE id = :id")
    suspend fun markPaid(id: Long, paidDate: Long)

    @Query("UPDATE bills SET isPaid = 0 WHERE id = :id")
    suspend fun markUnpaid(id: Long)

    @Query("SELECT COUNT(*) FROM bills WHERE isActive = 1 AND isPaid = 0")
    fun observeUnpaidCount(): Flow<Int>

    @Query("SELECT * FROM bills WHERE name LIKE '%' || :query || '%' AND isActive = 1")
    fun search(query: String): Flow<List<BillEntity>>

    @Query("""
        SELECT * FROM bills 
        WHERE isActive = 1 AND isPaid = 0 
        AND dueDate <= :dayOfMonth + :reminderDays 
        ORDER BY dueDate ASC
    """)
    suspend fun getBillsDueSoon(dayOfMonth: Int, reminderDays: Int = 3): List<BillEntity>
}
