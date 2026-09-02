package com.example.finora.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.finora.core.notification.FinoraNotificationManager
import com.example.finora.core.repository.BillRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

@HiltWorker
class BillReminderWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val billRepository: BillRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val todayDay = LocalDate.now().dayOfMonth
            val dueSoonBills = billRepository.getBillsDueSoon(todayDay, reminderDays = 3)

            for (bill in dueSoonBills) {
                val daysLeft = bill.dueDate - todayDay
                FinoraNotificationManager.showBillReminderNotification(
                    context = appContext,
                    billName = bill.name,
                    amount = bill.amount,
                    dueDaysLeft = daysLeft
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
