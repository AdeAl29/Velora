package com.example.finora.core.util

import com.example.finora.core.database.entity.*
import com.example.finora.core.database.dao.*
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first

object SampleData {

    suspend fun loadSampleData(
        transactionDao: TransactionDao,
        walletDao: WalletDao,
        transferDao: TransferDao,
        budgetDao: BudgetDao,
        goalDao: GoalDao,
        billDao: BillDao,
        recurringTransactionDao: RecurringTransactionDao,
        categoryDao: CategoryDao
    ) {
        // Wallets
        val wallets = listOf(
            WalletEntity(name = "Cash", iconName = "cash", initialBalance = 1_500_000, colorHex = "#34D399"),
            WalletEntity(name = "BCA", iconName = "bank", initialBalance = 8_000_000, colorHex = "#38BDF8"),
            WalletEntity(name = "Mandiri", iconName = "bank", initialBalance = 3_000_000, colorHex = "#FBBF24"),
            WalletEntity(name = "DANA", iconName = "ewallet", initialBalance = 500_000, colorHex = "#38BDF8"),
            WalletEntity(name = "GoPay", iconName = "ewallet", initialBalance = 250_000, colorHex = "#34D399"),
        )
        val walletIds = wallets.map { walletDao.insert(it) }

        val today = LocalDate.now()
        fun daysAgo(days: Int): Long =
            today.minusDays(days.toLong()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Get category IDs — assume defaults already loaded
        // We use fixed IDs since default categories are inserted in order: 1-7 income, 8-14 fixed, 15-23 variable

        // Transactions - Income
        val incomes = listOf(
            TransactionEntity(amount = 5_000_000, transactionType = "INCOME", categoryId = 1, walletId = walletIds[1], date = daysAgo(25), time = "09:00", note = "Gaji bulan ini"),
            TransactionEntity(amount = 500_000, transactionType = "INCOME", categoryId = 2, walletId = walletIds[0], date = daysAgo(20), time = "12:00", note = "Uang saku dari orang tua"),
            TransactionEntity(amount = 1_500_000, transactionType = "INCOME", categoryId = 4, walletId = walletIds[1], date = daysAgo(15), time = "18:00", note = "Project freelance desain"),
            TransactionEntity(amount = 300_000, transactionType = "INCOME", categoryId = 3, walletId = walletIds[1], date = daysAgo(10), time = "10:00", note = "Bonus target tercapai"),
        )

        // Transactions - Fixed Expenses
        val fixedExpenses = listOf(
            TransactionEntity(amount = 1_000_000, transactionType = "EXPENSE_FIXED", categoryId = 8, walletId = walletIds[1], date = daysAgo(28), time = "08:00", note = "Bayar kos bulan ini"),
            TransactionEntity(amount = 300_000, transactionType = "EXPENSE_FIXED", categoryId = 10, walletId = walletIds[1], date = daysAgo(22), time = "14:00", note = "Internet bulanan"),
            TransactionEntity(amount = 150_000, transactionType = "EXPENSE_FIXED", categoryId = 9, walletId = walletIds[1], date = daysAgo(20), time = "09:00", note = "Token listrik"),
            TransactionEntity(amount = 186_000, transactionType = "EXPENSE_FIXED", categoryId = 13, walletId = walletIds[1], date = daysAgo(18), time = "19:00", note = "Netflix subscription"),
        )

        // Transactions - Variable Expenses
        val variableExpenses = listOf(
            TransactionEntity(amount = 35_000, transactionType = "EXPENSE_VARIABLE", categoryId = 15, walletId = walletIds[0], date = daysAgo(0), time = "12:30", note = "Makan siang nasi goreng"),
            TransactionEntity(amount = 25_000, transactionType = "EXPENSE_VARIABLE", categoryId = 15, walletId = walletIds[0], date = daysAgo(0), time = "07:00", note = "Sarapan"),
            TransactionEntity(amount = 15_000, transactionType = "EXPENSE_VARIABLE", categoryId = 16, walletId = walletIds[3], date = daysAgo(0), time = "08:00", note = "Ojol ke kantor"),
            TransactionEntity(amount = 50_000, transactionType = "EXPENSE_VARIABLE", categoryId = 15, walletId = walletIds[0], date = daysAgo(1), time = "19:00", note = "Makan malam"),
            TransactionEntity(amount = 150_000, transactionType = "EXPENSE_VARIABLE", categoryId = 17, walletId = walletIds[3], date = daysAgo(1), time = "15:00", note = "Belanja kebutuhan"),
            TransactionEntity(amount = 75_000, transactionType = "EXPENSE_VARIABLE", categoryId = 19, walletId = walletIds[0], date = daysAgo(2), time = "20:00", note = "Nongkrong bareng teman"),
            TransactionEntity(amount = 40_000, transactionType = "EXPENSE_VARIABLE", categoryId = 15, walletId = walletIds[0], date = daysAgo(2), time = "12:00", note = "Makan siang"),
            TransactionEntity(amount = 200_000, transactionType = "EXPENSE_VARIABLE", categoryId = 18, walletId = walletIds[1], date = daysAgo(3), time = "21:00", note = "Nonton bioskop"),
            TransactionEntity(amount = 30_000, transactionType = "EXPENSE_VARIABLE", categoryId = 16, walletId = walletIds[4], date = daysAgo(3), time = "17:00", note = "Grab ke mall"),
            TransactionEntity(amount = 45_000, transactionType = "EXPENSE_VARIABLE", categoryId = 15, walletId = walletIds[0], date = daysAgo(4), time = "13:00", note = "Makan siang"),
            TransactionEntity(amount = 100_000, transactionType = "EXPENSE_VARIABLE", categoryId = 20, walletId = walletIds[1], date = daysAgo(5), time = "10:00", note = "Cek kesehatan"),
            TransactionEntity(amount = 60_000, transactionType = "EXPENSE_VARIABLE", categoryId = 21, walletId = walletIds[3], date = daysAgo(7), time = "09:00", note = "Beli buku"),
            TransactionEntity(amount = 350_000, transactionType = "EXPENSE_VARIABLE", categoryId = 17, walletId = walletIds[1], date = daysAgo(10), time = "14:00", note = "Beli sepatu"),
        )

        (incomes + fixedExpenses + variableExpenses).forEach { transactionDao.insert(it) }

        // Transfer
        transferDao.insert(TransferEntity(fromWalletId = walletIds[1], toWalletId = walletIds[3], amount = 500_000, date = daysAgo(5), note = "Top up DANA"))

        // Budgets
        val budgets = listOf(
            BudgetEntity(categoryId = 15, amount = 1_500_000, period = "MONTHLY", startDate = daysAgo(30)),
            BudgetEntity(categoryId = 16, amount = 500_000, period = "MONTHLY", startDate = daysAgo(30)),
            BudgetEntity(categoryId = 18, amount = 300_000, period = "MONTHLY", startDate = daysAgo(30)),
            BudgetEntity(categoryId = 17, amount = 500_000, period = "MONTHLY", startDate = daysAgo(30)),
        )
        budgets.forEach { budgetDao.insert(it) }

        // Financial Goals
        val goals = listOf(
            FinancialGoalEntity(
                name = "MacBook Pro",
                targetAmount = 25_000_000,
                currentAmount = 8_500_000,
                iconName = "laptop",
                colorHex = "#6C63FF",
                deadline = today.plusMonths(10).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                priority = 1
            ),
            FinancialGoalEntity(
                name = "Dana Darurat",
                targetAmount = 30_000_000,
                currentAmount = 12_000_000,
                iconName = "emergency",
                colorHex = "#34D399",
                deadline = today.plusMonths(18).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                priority = 1
            ),
            FinancialGoalEntity(
                name = "Liburan Bali",
                targetAmount = 5_000_000,
                currentAmount = 2_200_000,
                iconName = "travel",
                colorHex = "#38BDF8",
                deadline = today.plusMonths(3).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                priority = 2
            ),
        )
        goals.forEach { goalDao.insert(it) }

        // Bills
        val bills = listOf(
            BillEntity(name = "Internet", amount = 300_000, categoryId = 10, walletId = walletIds[1], dueDate = 5, frequency = "MONTHLY", reminderDaysBefore = 3),
            BillEntity(name = "Kos", amount = 1_000_000, categoryId = 8, walletId = walletIds[1], dueDate = 10, frequency = "MONTHLY", reminderDaysBefore = 5),
            BillEntity(name = "Netflix", amount = 186_000, categoryId = 13, walletId = walletIds[1], dueDate = 15, frequency = "MONTHLY", reminderDaysBefore = 2),
            BillEntity(name = "Listrik", amount = 150_000, categoryId = 9, walletId = walletIds[1], dueDate = 20, frequency = "MONTHLY", reminderDaysBefore = 3),
        )
        bills.forEach { billDao.insert(it) }

        // Recurring Transactions
        val recurring = listOf(
            RecurringTransactionEntity(
                name = "Kos Bulanan",
                amount = 1_000_000,
                transactionType = "EXPENSE_FIXED",
                categoryId = 8,
                walletId = walletIds[1],
                frequency = "MONTHLY",
                startDate = daysAgo(60),
                nextExecutionDate = today.plusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            ),
            RecurringTransactionEntity(
                name = "Internet Bulanan",
                amount = 300_000,
                transactionType = "EXPENSE_FIXED",
                categoryId = 10,
                walletId = walletIds[1],
                frequency = "MONTHLY",
                startDate = daysAgo(60),
                nextExecutionDate = today.plusMonths(1).withDayOfMonth(5).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            ),
        )
        recurring.forEach { recurringTransactionDao.insert(it) }
    }

    suspend fun clearAllData(
        transactionDao: TransactionDao,
        walletDao: WalletDao,
        transferDao: TransferDao,
        budgetDao: BudgetDao,
        goalDao: GoalDao,
        billDao: BillDao,
        recurringTransactionDao: RecurringTransactionDao
    ) {
        // Delete in reverse dependency order
        val txs = transactionDao.observeAll().first()
        txs.forEach { transactionDao.delete(it) }

        val transfers = transferDao.observeAll().first()
        transfers.forEach { transferDao.delete(it) }

        val budgets = budgetDao.observeAll().first()
        budgets.forEach { budgetDao.delete(it) }

        val goals = goalDao.observeAll().first()
        goals.forEach { goalDao.delete(it) }

        val bills = billDao.observeAll().first()
        bills.forEach { billDao.delete(it) }

        val recurrings = recurringTransactionDao.observeAll().first()
        recurrings.forEach { recurringTransactionDao.delete(it) }

        val wallets = walletDao.observeAll().first()
        wallets.forEach { walletDao.delete(it) }
    }
}
