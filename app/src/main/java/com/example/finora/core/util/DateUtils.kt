package com.example.finora.core.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object DateUtils {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("id", "ID"))
    private val shortDateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale("id", "ID"))
    private val dayFormatter = DateTimeFormatter.ofPattern("EEEE", Locale("id", "ID"))
    private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("id", "ID"))
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale("id", "ID"))

    fun getGreeting(): String {
        val hour = LocalTime.now().hour
        return when {
            hour in 4..10 -> "Selamat Pagi"
            hour in 11..14 -> "Selamat Siang"
            hour in 15..17 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
    }

    fun getGreetingEmoji(): String {
        val hour = LocalTime.now().hour
        return when {
            hour in 4..10 -> "☀️"
            hour in 11..14 -> "🌤️"
            hour in 15..17 -> "🌅"
            else -> "🌙"
        }
    }

    fun formatDate(epochMillis: Long): String {
        val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        return when (date) {
            today -> "Hari ini"
            yesterday -> "Kemarin"
            else -> date.format(dateFormatter)
        }
    }

    fun formatShortDate(epochMillis: Long): String {
        val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        return date.format(shortDateFormatter)
    }

    fun formatFullDate(epochMillis: Long): String {
        val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        return date.format(dateFormatter)
    }

    fun formatMonthYear(epochMillis: Long): String {
        val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        return date.format(monthYearFormatter)
    }

    fun formatDayName(epochMillis: Long): String {
        val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        return date.format(dayFormatter)
    }

    fun toEpochMillis(localDate: LocalDate): Long {
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun toLocalDate(epochMillis: Long): LocalDate {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    fun getStartOfDay(epochMillis: Long): Long {
        val date = toLocalDate(epochMillis)
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getEndOfDay(epochMillis: Long): Long {
        val date = toLocalDate(epochMillis)
        return date.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getStartOfWeek(): Long {
        val today = LocalDate.now()
        val startOfWeek = today.with(java.time.DayOfWeek.MONDAY)
        return toEpochMillis(startOfWeek)
    }

    fun getStartOfMonth(): Long {
        val today = LocalDate.now()
        return toEpochMillis(today.withDayOfMonth(1))
    }

    fun getEndOfMonth(): Long {
        val today = LocalDate.now()
        val endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth())
        return endOfMonth.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getStartOfLastMonth(): Long {
        val today = LocalDate.now().minusMonths(1)
        return toEpochMillis(today.withDayOfMonth(1))
    }

    fun getEndOfLastMonth(): Long {
        val today = LocalDate.now().minusMonths(1)
        val endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth())
        return endOfMonth.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getStartOfYear(): Long {
        val today = LocalDate.now()
        return toEpochMillis(today.withDayOfYear(1))
    }

    fun getEndOfYear(): Long {
        val today = LocalDate.now()
        val endOfYear = today.with(TemporalAdjusters.lastDayOfYear())
        return endOfYear.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getStartOfNMonthsAgo(n: Int): Long {
        val date = LocalDate.now().minusMonths(n.toLong())
        return toEpochMillis(date.withDayOfMonth(1))
    }

    fun getCurrentTimeString(): String {
        return LocalTime.now().format(timeFormatter)
    }

    fun today(): Long = toEpochMillis(LocalDate.now())
    fun now(): Long = System.currentTimeMillis()

    fun daysUntil(targetEpochMillis: Long): Long {
        val today = LocalDate.now()
        val target = toLocalDate(targetEpochMillis)
        return java.time.temporal.ChronoUnit.DAYS.between(today, target)
    }

    fun monthsUntil(targetEpochMillis: Long): Long {
        val today = LocalDate.now()
        val target = toLocalDate(targetEpochMillis)
        return java.time.temporal.ChronoUnit.MONTHS.between(today, target)
    }

    fun isSameDay(millis1: Long, millis2: Long): Boolean {
        return toLocalDate(millis1) == toLocalDate(millis2)
    }

    fun getDateGroupKey(epochMillis: Long): String {
        return formatDate(epochMillis)
    }
}
