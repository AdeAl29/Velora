package com.example.finora.core.repository

import com.example.finora.core.database.dao.*
import com.example.finora.core.database.entity.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    fun observeAll(): Flow<List<TransactionEntity>> = transactionDao.observeAll()

    fun observeByDateRange(startDate: Long, endDate: Long) =
        transactionDao.observeByDateRange(startDate, endDate)

    fun observeByType(type: String) = transactionDao.observeByType(type)

    fun observeByWallet(walletId: Long) = transactionDao.observeByWallet(walletId)

    fun observeByCategory(categoryId: Long) = transactionDao.observeByCategory(categoryId)

    fun observeTotalIncome(startDate: Long, endDate: Long) =
        transactionDao.observeTotalIncome(startDate, endDate)

    fun observeTotalExpense(startDate: Long, endDate: Long) =
        transactionDao.observeTotalExpense(startDate, endDate)

    fun observeTotalFixedExpense(startDate: Long, endDate: Long) =
        transactionDao.observeTotalFixedExpense(startDate, endDate)

    fun observeTotalVariableExpense(startDate: Long, endDate: Long) =
        transactionDao.observeTotalVariableExpense(startDate, endDate)

    fun observeRecent(limit: Int) = transactionDao.observeRecent(limit)

    fun observeNetBalance() = transactionDao.observeNetBalance()

    fun observeCount() = transactionDao.observeCount()

    fun search(query: String) = transactionDao.search(query)

    fun observeExpenseByCategory(categoryId: Long, startDate: Long, endDate: Long) =
        transactionDao.observeExpenseByCategory(categoryId, startDate, endDate)

    fun observeFiltered(
        startDate: Long,
        endDate: Long,
        type: String? = null,
        categoryId: Long? = null,
        walletId: Long? = null,
        sortByAmount: Boolean = false
    ) = transactionDao.observeFiltered(startDate, endDate, type, categoryId, walletId, sortByAmount)

    suspend fun insert(transaction: TransactionEntity): Long = transactionDao.insert(transaction)

    suspend fun update(transaction: TransactionEntity) = transactionDao.update(transaction)

    suspend fun delete(transaction: TransactionEntity) = transactionDao.delete(transaction)

    suspend fun deleteById(id: Long) = transactionDao.deleteById(id)

    suspend fun getById(id: Long) = transactionDao.getById(id)

    fun observeById(id: Long) = transactionDao.observeById(id)
}

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    fun observeAll() = categoryDao.observeAll()
    fun observeByType(type: String) = categoryDao.observeByType(type)
    fun search(query: String) = categoryDao.search(query)
    suspend fun getById(id: Long) = categoryDao.getById(id)
    fun observeById(id: Long) = categoryDao.observeById(id)
    suspend fun insert(category: CategoryEntity) = categoryDao.insert(category)
    suspend fun update(category: CategoryEntity) = categoryDao.update(category)
    suspend fun delete(category: CategoryEntity) = categoryDao.delete(category)
    suspend fun getCount() = categoryDao.getCount()
}

@Singleton
class WalletRepository @Inject constructor(
    private val walletDao: WalletDao,
    private val transferDao: TransferDao,
    private val transactionDao: TransactionDao
) {
    fun observeAll() = walletDao.observeAll()
    suspend fun getAll() = walletDao.getAll()
    fun observeCount() = walletDao.observeCount()
    fun observeTotalInitialBalance() = walletDao.observeTotalInitialBalance()
    fun search(query: String) = walletDao.search(query)
    suspend fun getById(id: Long) = walletDao.getById(id)
    fun observeById(id: Long) = walletDao.observeById(id)
    suspend fun insert(wallet: WalletEntity) = walletDao.insert(wallet)
    suspend fun update(wallet: WalletEntity) = walletDao.update(wallet)
    suspend fun delete(wallet: WalletEntity) = walletDao.delete(wallet)

    suspend fun getWalletBalance(walletId: Long): Long {
        val wallet = walletDao.getById(walletId) ?: return 0
        val totalIncome = transactionDao.getTotalIncomeForWallet(walletId)
        val totalExpense = transactionDao.getTotalExpenseForWallet(walletId)
        val transfersIn = transferDao.getTotalTransfersIn(walletId)
        val transfersOut = transferDao.getTotalTransfersOut(walletId)
        return wallet.initialBalance + totalIncome - totalExpense + transfersIn - transfersOut
    }

    suspend fun transfer(fromWalletId: Long, toWalletId: Long, amount: Long, note: String? = null): Long {
        val transfer = TransferEntity(
            fromWalletId = fromWalletId,
            toWalletId = toWalletId,
            amount = amount,
            date = System.currentTimeMillis(),
            note = note
        )
        return transferDao.insert(transfer)
    }

    fun observeTransfers() = transferDao.observeAll()
    fun observeTransfersByWallet(walletId: Long) = transferDao.observeByWallet(walletId)
    fun observeTransfersByDateRange(startDate: Long, endDate: Long) =
        transferDao.observeByDateRange(startDate, endDate)
}

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao
) {
    fun observeAll() = budgetDao.observeAll()
    fun observeCount() = budgetDao.observeCount()
    fun observeByCategoryId(categoryId: Long) = budgetDao.observeByCategoryId(categoryId)
    suspend fun getById(id: Long) = budgetDao.getById(id)
    suspend fun insert(budget: BudgetEntity) = budgetDao.insert(budget)
    suspend fun update(budget: BudgetEntity) = budgetDao.update(budget)
    suspend fun delete(budget: BudgetEntity) = budgetDao.delete(budget)
}

@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao
) {
    fun observeAll() = goalDao.observeAll()
    fun observeActive() = goalDao.observeActive()
    fun observeCompleted() = goalDao.observeCompleted()
    fun observeActiveCount() = goalDao.observeActiveCount()
    fun search(query: String) = goalDao.search(query)
    suspend fun getById(id: Long) = goalDao.getById(id)
    fun observeById(id: Long) = goalDao.observeById(id)
    suspend fun insert(goal: FinancialGoalEntity) = goalDao.insert(goal)
    suspend fun update(goal: FinancialGoalEntity) = goalDao.update(goal)
    suspend fun delete(goal: FinancialGoalEntity) = goalDao.delete(goal)
    suspend fun addFunds(id: Long, amount: Long) = goalDao.addFunds(id, amount)
    suspend fun removeFunds(id: Long, amount: Long) = goalDao.removeFunds(id, amount)
    suspend fun markCompleted(id: Long) = goalDao.markCompleted(id)
    suspend fun markIncomplete(id: Long) = goalDao.markIncomplete(id)
}

@Singleton
class BillRepository @Inject constructor(
    private val billDao: BillDao
) {
    fun observeAll() = billDao.observeAll()
    fun observeUnpaid() = billDao.observeUnpaid()
    fun observePaid() = billDao.observePaid()
    fun observeUnpaidCount() = billDao.observeUnpaidCount()
    fun search(query: String) = billDao.search(query)
    suspend fun getById(id: Long) = billDao.getById(id)
    suspend fun insert(bill: BillEntity) = billDao.insert(bill)
    suspend fun update(bill: BillEntity) = billDao.update(bill)
    suspend fun delete(bill: BillEntity) = billDao.delete(bill)
    suspend fun markPaid(id: Long, paidDate: Long) = billDao.markPaid(id, paidDate)
    suspend fun markUnpaid(id: Long) = billDao.markUnpaid(id)
    suspend fun getBillsDueSoon(dayOfMonth: Int, reminderDays: Int = 3) =
        billDao.getBillsDueSoon(dayOfMonth, reminderDays)
}

@Singleton
class RecurringTransactionRepository @Inject constructor(
    private val recurringTransactionDao: RecurringTransactionDao
) {
    fun observeAll() = recurringTransactionDao.observeAll()
    fun observeCount() = recurringTransactionDao.observeCount()
    suspend fun getById(id: Long) = recurringTransactionDao.getById(id)
    suspend fun insert(recurring: RecurringTransactionEntity) = recurringTransactionDao.insert(recurring)
    suspend fun update(recurring: RecurringTransactionEntity) = recurringTransactionDao.update(recurring)
    suspend fun delete(recurring: RecurringTransactionEntity) = recurringTransactionDao.delete(recurring)
    suspend fun getDueRecurring(currentDate: Long) = recurringTransactionDao.getDueRecurring(currentDate)
    suspend fun updateExecutionDates(id: Long, executedDate: Long, nextDate: Long) =
        recurringTransactionDao.updateExecutionDates(id, executedDate, nextDate)
    suspend fun deactivate(id: Long) = recurringTransactionDao.deactivate(id)
}
