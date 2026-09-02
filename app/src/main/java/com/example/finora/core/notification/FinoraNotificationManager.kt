package com.example.finora.core.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.finora.FinoraApplication
import com.example.finora.MainActivity
import com.example.finora.core.util.CurrencyFormatter

object FinoraNotificationManager {

    fun showBillReminderNotification(
        context: Context,
        billName: String,
        amount: Long,
        dueDaysLeft: Int
    ) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val amountStr = CurrencyFormatter.formatRupiah(amount)
        val title = "Pengingat Tagihan: $billName"
        val message = if (dueDaysLeft <= 0) {
            "Tagihan $billName sebesar $amountStr jatuh tempo HARI INI!"
        } else {
            "Tagihan $billName sebesar $amountStr akan jatuh tempo dalam $dueDaysLeft hari."
        }

        val notification = NotificationCompat.Builder(context, FinoraApplication.CHANNEL_BILL_REMINDER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(billName.hashCode(), notification)
    }

    fun showRecurringTransactionNotification(
        context: Context,
        recurringName: String,
        amount: Long
    ) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val amountStr = CurrencyFormatter.formatRupiah(amount)
        val title = "Transaksi Berulang Dibuat"
        val message = "Pengeluaran tetap '$recurringName' sebesar $amountStr telah otomatis dicatat."

        val notification = NotificationCompat.Builder(context, FinoraApplication.CHANNEL_RECURRING)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(recurringName.hashCode(), notification)
    }
}
