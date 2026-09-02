package com.example.finora.core.database.dao

import androidx.room.*
import com.example.finora.core.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun observeById(id: Long): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions ORDER BY date DESC, time DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE date >= :startDate AND date <= :endDate 
        ORDER BY date DESC, time DESC
    """)
    fun observeByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE transactionType = :type 
        ORDER BY date DESC, time DESC
    """)
    fun observeByType(type: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE walletId = :walletId 
        ORDER BY date DESC, time DESC
    """)
    fun observeByWallet(walletId: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE categoryId = :categoryId 
        ORDER BY date DESC, time DESC
    """)
    fun observeByCategory(categoryId: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE transactionType = 'INCOME' 
        AND date >= :startDate AND date <= :endDate
    """)
    fun observeTotalIncome(startDate: Long, endDate: Long): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE transactionType IN ('EXPENSE_FIXED', 'EXPENSE_VARIABLE') 
        AND date >= :startDate AND date <= :endDate
    """)
    fun observeTotalExpense(startDate: Long, endDate: Long): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE transactionType = 'EXPENSE_FIXED' 
        AND date >= :startDate AND date <= :endDate
    """)
    fun observeTotalFixedExpense(startDate: Long, endDate: Long): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE transactionType = 'EXPENSE_VARIABLE' 
        AND date >= :startDate AND date <= :endDate
    """)
    fun observeTotalVariableExpense(startDate: Long, endDate: Long): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE transactionType = 'INCOME' AND walletId = :walletId
    """)
    suspend fun getTotalIncomeForWallet(walletId: Long): Long

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE transactionType IN ('EXPENSE_FIXED', 'EXPENSE_VARIABLE') AND walletId = :walletId
    """)
    suspend fun getTotalExpenseForWallet(walletId: Long): Long

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE categoryId = :categoryId 
        AND transactionType IN ('EXPENSE_FIXED', 'EXPENSE_VARIABLE')
        AND date >= :startDate AND date <= :endDate
    """)
    fun observeExpenseByCategory(categoryId: Long, startDate: Long, endDate: Long): Flow<Long>

    @Query("""
        SELECT * FROM transactions 
        ORDER BY date DESC, time DESC 
        LIMIT :limit
    """)
    fun observeRecent(limit: Int): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE note LIKE '%' || :query || '%' 
        ORDER BY date DESC
    """)
    fun search(query: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN transactionType = 'INCOME' THEN amount ELSE 0 END), 0) -
               COALESCE(SUM(CASE WHEN transactionType IN ('EXPENSE_FIXED', 'EXPENSE_VARIABLE') THEN amount ELSE 0 END), 0)
        FROM transactions
    """)
    fun observeNetBalance(): Flow<Long>

    @Query("SELECT COUNT(*) FROM transactions")
    fun observeCount(): Flow<Int>

    @Query("""
        SELECT * FROM transactions 
        WHERE date >= :startDate AND date <= :endDate
        AND (:type IS NULL OR transactionType = :type)
        AND (:categoryId IS NULL OR categoryId = :categoryId)
        AND (:walletId IS NULL OR walletId = :walletId)
        ORDER BY 
            CASE WHEN :sortByAmount = 1 THEN amount END DESC,
            CASE WHEN :sortByAmount = 0 THEN date END DESC,
            time DESC
    """)
    fun observeFiltered(
        startDate: Long,
        endDate: Long,
        type: String?,
        categoryId: Long?,
        walletId: Long?,
        sortByAmount: Boolean = false
    ): Flow<List<TransactionEntity>>
}
