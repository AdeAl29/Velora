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
    version = 2,
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
                        insertDefaultCategories(db)
                    }

                    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                        super.onDestructiveMigration(db)
                        insertDefaultCategories(db)
                    }
                })
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }

        private fun insertDefaultCategories(db: SupportSQLiteDatabase) {
            val categories = listOf(
                // Income categories (type = INCOME)
                arrayOf("Gaji", "salary", "#34D399", "INCOME", "0", "1", "0"),
                arrayOf("Uang Saku", "pocket_money", "#6EE7B7", "INCOME", "0", "1", "1"),
                arrayOf("Bonus", "bonus", "#A78BFA", "INCOME", "0", "1", "2"),
                arrayOf("Freelance", "freelance", "#38BDF8", "INCOME", "0", "1", "3"),
                arrayOf("Hadiah", "gift", "#FB7185", "INCOME", "0", "1", "4"),
                arrayOf("Investasi", "investment", "#FBBF24", "INCOME", "0", "1", "5"),
                arrayOf("Lainnya", "other", "#94A3B8", "INCOME", "0", "1", "6"),

                // Fixed expense categories (type = EXPENSE_FIXED)
                arrayOf("Kos/Sewa", "housing", "#F87171", "EXPENSE_FIXED", "0", "1", "0"),
                arrayOf("Listrik", "electricity", "#FBBF24", "EXPENSE_FIXED", "0", "1", "1"),
                arrayOf("Internet", "internet", "#38BDF8", "EXPENSE_FIXED", "0", "1", "2"),
                arrayOf("Cicilan", "installment", "#F472B6", "EXPENSE_FIXED", "0", "1", "3"),
                arrayOf("Asuransi", "insurance", "#818CF8", "EXPENSE_FIXED", "0", "1", "4"),
                arrayOf("Langganan", "subscription", "#A78BFA", "EXPENSE_FIXED", "0", "1", "5"),
                arrayOf("Tagihan Rutin", "bill", "#FB923C", "EXPENSE_FIXED", "0", "1", "6"),

                // Variable expense categories (type = EXPENSE_VARIABLE)
                arrayOf("Makanan", "food", "#FB923C", "EXPENSE_VARIABLE", "0", "1", "0"),
                arrayOf("Transportasi", "transport", "#38BDF8", "EXPENSE_VARIABLE", "0", "1", "1"),
                arrayOf("Belanja", "shopping", "#F472B6", "EXPENSE_VARIABLE", "0", "1", "2"),
                arrayOf("Hiburan", "entertainment", "#A78BFA", "EXPENSE_VARIABLE", "0", "1", "3"),
                arrayOf("Nongkrong", "hangout", "#FBBF24", "EXPENSE_VARIABLE", "0", "1", "4"),
                arrayOf("Kesehatan", "health", "#34D399", "EXPENSE_VARIABLE", "0", "1", "5"),
                arrayOf("Pendidikan", "education", "#818CF8", "EXPENSE_VARIABLE", "0", "1", "6"),
                arrayOf("Teknologi", "technology", "#6366F1", "EXPENSE_VARIABLE", "0", "1", "7"),
                arrayOf("Lainnya", "other_expense", "#94A3B8", "EXPENSE_VARIABLE", "0", "1", "8")
            )

            db.beginTransaction()
            try {
                for (cat in categories) {
                    db.execSQL(
                        "INSERT INTO categories (name, iconName, colorHex, type, isCustom, isActive, sortOrder) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        cat
                    )
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }
}
