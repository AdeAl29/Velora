package com.example.finora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.work.*
import com.example.finora.core.datastore.FinoraDataStore
import com.example.finora.core.worker.BillReminderWorker
import com.example.finora.core.worker.RecurringTransactionWorker
import com.example.finora.ui.theme.FinoraTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dataStore: FinoraDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        scheduleBackgroundWorkers()

        setContent {
            val themeMode by dataStore.themeMode.collectAsState(initial = "system")
            val isDark = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            FinoraTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(dataStore = dataStore)
                }
            }
        }
    }

    private fun scheduleBackgroundWorkers() {
        try {
            val workManager = WorkManager.getInstance(this)

            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            // Recurring transactions check every 12 hours
            val recurringWork = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "finora_recurring_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                recurringWork
            )

            // Bill reminders check daily
            val billReminderWork = PeriodicWorkRequestBuilder<BillReminderWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "finora_bill_reminder_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                billReminderWork
            )
        } catch (e: Exception) {
            android.util.Log.e("Finora", "Failed to schedule background workers", e)
        }
    }
}
