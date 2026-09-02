package com.example.finora

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FinoraApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val channels = listOf(
            NotificationChannel(
                CHANNEL_BILL_REMINDER,
                "Pengingat Tagihan",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi untuk mengingatkan tagihan yang akan jatuh tempo"
            },
            NotificationChannel(
                CHANNEL_RECURRING,
                "Transaksi Berulang",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifikasi transaksi berulang yang telah dibuat otomatis"
            },
            NotificationChannel(
                CHANNEL_BUDGET,
                "Peringatan Anggaran",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi ketika anggaran mendekati atau melebihi batas"
            },
            NotificationChannel(
                CHANNEL_GOAL,
                "Tujuan Keuangan",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifikasi progress tujuan keuangan"
            }
        )

        val notificationManager = getSystemService(NotificationManager::class.java)
        channels.forEach { notificationManager.createNotificationChannel(it) }
    }

    companion object {
        const val CHANNEL_BILL_REMINDER = "finora_bill_reminder"
        const val CHANNEL_RECURRING = "finora_recurring"
        const val CHANNEL_BUDGET = "finora_budget"
        const val CHANNEL_GOAL = "finora_goal"
    }
}
