package com.example.finora.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.finora.core.database.entity.TransactionEntity
import com.example.finora.core.model.Frequency
import com.example.finora.core.notification.FinoraNotificationManager
import com.example.finora.core.repository.RecurringTransactionRepository
import com.example.finora.core.repository.TransactionRepository
import com.example.finora.core.util.DateUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.ZoneId

@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val recurringRepository: RecurringTransactionRepository,
    private val transactionRepository: TransactionRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val now = System.currentTimeMillis()
            val dueItems = recurringRepository.getDueRecurring(now)

            for (item in dueItems) {
                // Insert transaction record
                val transaction = TransactionEntity(
                    amount = item.amount,
                    transactionType = item.transactionType,
                    categoryId = item.categoryId,
                    walletId = item.walletId,
                    date = now,
                    time = DateUtils.getCurrentTimeString(),
                    note = "Otomatis: ${item.name}",
                    isRecurring = true,
                    recurringId = item.id
                )
                transactionRepository.insert(transaction)

                // Calculate next execution date
                val currentLocal = DateUtils.toLocalDate(item.nextExecutionDate)
                val freq = try { Frequency.valueOf(item.frequency) } catch (_: Exception) { Frequency.MONTHLY }
                val nextLocal = when (freq) {
                    Frequency.DAILY -> currentLocal.plusDays(1)
                    Frequency.WEEKLY -> currentLocal.plusWeeks(1)
                    Frequency.MONTHLY -> currentLocal.plusMonths(1)
                    Frequency.YEARLY -> currentLocal.plusYears(1)
                }
                val nextEpoch = nextLocal.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                recurringRepository.updateExecutionDates(item.id, now, nextEpoch)

                // Trigger notification
                FinoraNotificationManager.showRecurringTransactionNotification(
                    appContext,
                    item.name,
                    item.amount
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
