package com.example.finora.core.di

import android.content.Context
import com.example.finora.core.database.FinoraDatabase
import com.example.finora.core.database.dao.*
import com.example.finora.core.datastore.FinoraDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFinoraDataStore(
        @ApplicationContext context: Context
    ): FinoraDataStore = FinoraDataStore(context)
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): FinoraDatabase = FinoraDatabase.buildDatabase(context)

    @Provides
    fun provideTransactionDao(db: FinoraDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideCategoryDao(db: FinoraDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideWalletDao(db: FinoraDatabase): WalletDao = db.walletDao()

    @Provides
    fun provideTransferDao(db: FinoraDatabase): TransferDao = db.transferDao()

    @Provides
    fun provideBudgetDao(db: FinoraDatabase): BudgetDao = db.budgetDao()

    @Provides
    fun provideGoalDao(db: FinoraDatabase): GoalDao = db.goalDao()

    @Provides
    fun provideBillDao(db: FinoraDatabase): BillDao = db.billDao()

    @Provides
    fun provideRecurringTransactionDao(db: FinoraDatabase): RecurringTransactionDao =
        db.recurringTransactionDao()
}
