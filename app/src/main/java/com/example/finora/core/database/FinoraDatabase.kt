package com.example.finora.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.finora.core.database.dao.*
import com.example.finora.core.database.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        WalletEntity::class,
        TransferEntity::class,
        BudgetEntity::class,
        FinancialGoalEntity::class,
        BillEntity::class,
        RecurringTransactionEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class FinoraDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun walletDao(): WalletDao
    abstract fun transferDao(): TransferDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun billDao(): BillDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao

    companion object {
        const val DATABASE_NAME = "finora_db"

        fun buildDatabase(context: Context): FinoraDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                FinoraDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = buildDatabase(context)
                            populateDefaultCategories(database.categoryDao())
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
        }

        private suspend fun populateDefaultCategories(categoryDao: CategoryDao) {
            val categories = listOf(
                // Income categories
                CategoryEntity(name = "Gaji", iconName = "salary", colorHex = "#34D399", type = "INCOME", sortOrder = 0),
                CategoryEntity(name = "Uang Saku", iconName = "pocket_money", colorHex = "#6EE7B7", type = "INCOME", sortOrder = 1),
                CategoryEntity(name = "Bonus", iconName = "bonus", colorHex = "#A78BFA", type = "INCOME", sortOrder = 2),
                CategoryEntity(name = "Freelance", iconName = "freelance", colorHex = "#38BDF8", type = "INCOME", sortOrder = 3),
                CategoryEntity(name = "Hadiah", iconName = "gift", colorHex = "#FB7185", type = "INCOME", sortOrder = 4),
                CategoryEntity(name = "Investasi", iconName = "investment", colorHex = "#FBBF24", type = "INCOME", sortOrder = 5),
                CategoryEntity(name = "Lainnya", iconName = "other", colorHex = "#94A3B8", type = "INCOME", sortOrder = 6),

                // Fixed expense categories
                CategoryEntity(name = "Kos/Sewa", iconName = "housing", colorHex = "#F87171", type = "EXPENSE_FIXED", sortOrder = 0),
                CategoryEntity(name = "Listrik", iconName = "electricity", colorHex = "#FBBF24", type = "EXPENSE_FIXED", sortOrder = 1),
                CategoryEntity(name = "Internet", iconName = "internet", colorHex = "#38BDF8", type = "EXPENSE_FIXED", sortOrder = 2),
                CategoryEntity(name = "Cicilan", iconName = "installment", colorHex = "#F472B6", type = "EXPENSE_FIXED", sortOrder = 3),
                CategoryEntity(name = "Asuransi", iconName = "insurance", colorHex = "#818CF8", type = "EXPENSE_FIXED", sortOrder = 4),
                CategoryEntity(name = "Langganan", iconName = "subscription", colorHex = "#A78BFA", type = "EXPENSE_FIXED", sortOrder = 5),
                CategoryEntity(name = "Tagihan Rutin", iconName = "bill", colorHex = "#FB923C", type = "EXPENSE_FIXED", sortOrder = 6),

                // Variable expense categories
                CategoryEntity(name = "Makanan", iconName = "food", colorHex = "#FB923C", type = "EXPENSE_VARIABLE", sortOrder = 0),
                CategoryEntity(name = "Transportasi", iconName = "transport", colorHex = "#38BDF8", type = "EXPENSE_VARIABLE", sortOrder = 1),
                CategoryEntity(name = "Belanja", iconName = "shopping", colorHex = "#F472B6", type = "EXPENSE_VARIABLE", sortOrder = 2),
                CategoryEntity(name = "Hiburan", iconName = "entertainment", colorHex = "#A78BFA", type = "EXPENSE_VARIABLE", sortOrder = 3),
                CategoryEntity(name = "Nongkrong", iconName = "hangout", colorHex = "#FBBF24", type = "EXPENSE_VARIABLE", sortOrder = 4),
                CategoryEntity(name = "Kesehatan", iconName = "health", colorHex = "#34D399", type = "EXPENSE_VARIABLE", sortOrder = 5),
                CategoryEntity(name = "Pendidikan", iconName = "education", colorHex = "#818CF8", type = "EXPENSE_VARIABLE", sortOrder = 6),
                CategoryEntity(name = "Teknologi", iconName = "technology", colorHex = "#6366F1", type = "EXPENSE_VARIABLE", sortOrder = 7),
                CategoryEntity(name = "Lainnya", iconName = "other_expense", colorHex = "#94A3B8", type = "EXPENSE_VARIABLE", sortOrder = 8)
            )
            categoryDao.insertAll(categories)
        }
    }
}
