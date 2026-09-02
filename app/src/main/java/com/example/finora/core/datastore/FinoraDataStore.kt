package com.example.finora.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "finora_settings")

@Singleton
class FinoraDataStore @Inject constructor(
    private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val USER_NAME = stringPreferencesKey("user_name")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DATE_FORMAT = stringPreferencesKey("date_format")
        val HIDE_BALANCE = booleanPreferencesKey("hide_balance")
        val BILL_REMINDER_ENABLED = booleanPreferencesKey("bill_reminder_enabled")
        val RECURRING_NOTIFICATION_ENABLED = booleanPreferencesKey("recurring_notification_enabled")
        val BUDGET_WARNING_ENABLED = booleanPreferencesKey("budget_warning_enabled")
        val GOAL_REMINDER_ENABLED = booleanPreferencesKey("goal_reminder_enabled")
        val SAMPLE_DATA_LOADED = booleanPreferencesKey("sample_data_loaded")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val APP_LOCK_PIN = stringPreferencesKey("app_lock_pin")
    }

    val isOnboardingCompleted: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[ONBOARDING_COMPLETED] ?: false }

    val userName: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[USER_NAME] ?: "" }

    val currencyCode: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[CURRENCY_CODE] ?: "IDR" }

    val themeMode: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[THEME_MODE] ?: "system" }

    val isBalanceHidden: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[HIDE_BALANCE] ?: false }

    val isBillReminderEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[BILL_REMINDER_ENABLED] ?: true }

    val isBudgetWarningEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[BUDGET_WARNING_ENABLED] ?: true }

    val isGoalReminderEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[GOAL_REMINDER_ENABLED] ?: true }

    val isSampleDataLoaded: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[SAMPLE_DATA_LOADED] ?: false }

    val isAppLockEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[APP_LOCK_ENABLED] ?: false }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setUserName(name: String) {
        dataStore.edit { it[USER_NAME] = name }
    }

    suspend fun setCurrencyCode(code: String) {
        dataStore.edit { it[CURRENCY_CODE] = code }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun setBalanceHidden(hidden: Boolean) {
        dataStore.edit { it[HIDE_BALANCE] = hidden }
    }

    suspend fun setBillReminderEnabled(enabled: Boolean) {
        dataStore.edit { it[BILL_REMINDER_ENABLED] = enabled }
    }

    suspend fun setBudgetWarningEnabled(enabled: Boolean) {
        dataStore.edit { it[BUDGET_WARNING_ENABLED] = enabled }
    }

    suspend fun setGoalReminderEnabled(enabled: Boolean) {
        dataStore.edit { it[GOAL_REMINDER_ENABLED] = enabled }
    }

    suspend fun setSampleDataLoaded(loaded: Boolean) {
        dataStore.edit { it[SAMPLE_DATA_LOADED] = loaded }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        dataStore.edit { it[APP_LOCK_ENABLED] = enabled }
    }

    suspend fun setAppLockPin(pin: String) {
        dataStore.edit { it[APP_LOCK_PIN] = pin }
    }

    suspend fun getAppLockPin(): String {
        var pin = ""
        dataStore.edit { pin = it[APP_LOCK_PIN] ?: "" }
        return pin
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
