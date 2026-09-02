package com.example.finora.core.database.dao

import androidx.room.*
import com.example.finora.core.database.entity.WalletEntity
import com.example.finora.core.database.entity.TransferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wallet: WalletEntity): Long

    @Update
    suspend fun update(wallet: WalletEntity)

    @Delete
    suspend fun delete(wallet: WalletEntity)

    @Query("SELECT * FROM wallets WHERE id = :id")
    suspend fun getById(id: Long): WalletEntity?

    @Query("SELECT * FROM wallets WHERE id = :id")
    fun observeById(id: Long): Flow<WalletEntity?>

    @Query("SELECT * FROM wallets WHERE isActive = 1 ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE isActive = 1 ORDER BY createdAt ASC")
    suspend fun getAll(): List<WalletEntity>

    @Query("SELECT COUNT(*) FROM wallets WHERE isActive = 1")
    fun observeCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(initialBalance), 0) FROM wallets WHERE isActive = 1")
    fun observeTotalInitialBalance(): Flow<Long>

    @Query("SELECT * FROM wallets WHERE name LIKE '%' || :query || '%' AND isActive = 1")
    fun search(query: String): Flow<List<WalletEntity>>
}

@Dao
interface TransferDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transfer: TransferEntity): Long

    @Delete
    suspend fun delete(transfer: TransferEntity)

    @Query("SELECT * FROM transfers ORDER BY date DESC")
    fun observeAll(): Flow<List<TransferEntity>>

    @Query("SELECT * FROM transfers WHERE fromWalletId = :walletId OR toWalletId = :walletId ORDER BY date DESC")
    fun observeByWallet(walletId: Long): Flow<List<TransferEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transfers WHERE toWalletId = :walletId")
    suspend fun getTotalTransfersIn(walletId: Long): Long

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transfers WHERE fromWalletId = :walletId")
    suspend fun getTotalTransfersOut(walletId: Long): Long

    @Query("SELECT * FROM transfers WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun observeByDateRange(startDate: Long, endDate: Long): Flow<List<TransferEntity>>
}
