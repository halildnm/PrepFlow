package com.prepflow.app.ui.components

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateTimeUtils {

    val turkishLocale: Locale = Locale("tr", "TR")

    private val fullDateFormatter = DateTimeFormatter.ofPattern("d MMMM EEEE", turkishLocale)
    private val shortDateFormatter = DateTimeFormatter.ofPattern("d MMM", turkishLocale)
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMM • HH:mm", turkishLocale)
    private val fullDateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", turkishLocale)
    private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", turkishLocale)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", turkishLocale)

    fun greetingForHour(hour: Int = LocalTime.now().hour): String = when (hour) {
        in 5..11 -> "Günaydın"
        in 12..17 -> "İyi günler"
        in 18..22 -> "İyi akşamlar"
        else -> "İyi çalışmalar"
    }

    fun formatTodayHeader(date: LocalDate = LocalDate.now()): String {
        return date.format(fullDateFormatter).replaceFirstChar { it.titlecase(turkishLocale) }
    }

    fun formatMonthYear(date: LocalDate): String {
        return date.format(monthYearFormatter).replaceFirstChar { it.titlecase(turkishLocale) }
    }

    fun formatShortDate(epochMillis: Long): String {
        val dt = epochMillisToLocalDateTime(epochMillis)
        return dt.format(shortDateFormatter)
    }

    fun formatDateTime(epochMillis: Long): String {
        val dt = epochMillisToLocalDateTime(epochMillis)
        return dt.format(dateTimeFormatter)
    }

    fun formatFullDateTime(epochMillis: Long): String {
        val dt = epochMillisToLocalDateTime(epochMillis)
        return dt.format(fullDateTimeFormatter)
    }

    fun formatTime(hour: Int, minute: Int): String {
        return LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59)).format(timeFormatter)
    }

    fun epochMillisToLocalDate(epochMillis: Long): LocalDate {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    fun epochMillisToLocalDateTime(epochMillis: Long): LocalDateTime {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    fun localDateTimeToEpochMillis(date: LocalDate, time: LocalTime): Long {
        return date.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun isOverdue(dueDateTime: Long, nowMillis: Long = System.currentTimeMillis()): Boolean {
        return dueDateTime < nowMillis
    }

    fun formatRemainingTime(targetMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        val diffMillis = targetMillis - nowMillis
        val absDuration = Duration.ofMillis(kotlin.math.abs(diffMillis))
        val days = absDuration.toDays()
        val hours = absDuration.toHours() % 24
        val minutes = absDuration.toMinutes() % 60

        return if (diffMillis < 0) {
            when {
                days >= 1 -> "$days gün gecikti"
                hours >= 1 -> "$hours saat gecikti"
                else -> "${minutes.coerceAtLeast(1)} dk gecikti"
            }
        } else {
            when {
                days >= 1 -> "$days gün kaldı"
                hours >= 1 -> "$hours saat kaldı"
                else -> "${minutes.coerceAtLeast(1)} dk kaldı"
            }
        }
    }

    fun formatExamCountdown(examDateMillis: Long, today: LocalDate = LocalDate.now()): String {
        val examDate = epochMillisToLocalDate(examDateMillis)
        val daysBetween = ChronoUnit.DAYS.between(today, examDate)
        return when {
            daysBetween < 0 -> "${-daysBetween} gün önce"
            daysBetween == 0L -> "Bugün"
            daysBetween == 1L -> "Yarın"
            else -> "$daysBetween gün kaldı"
        }
    }

    fun dayOfWeekShortTr(dayOfWeek: Int): String = when (dayOfWeek) {
        1 -> "Pzt"
        2 -> "Sal"
        3 -> "Çar"
        4 -> "Per"
        5 -> "Cum"
        6 -> "Cmt"
        7 -> "Paz"
        else -> ""
    }

    fun dayOfWeekFullTr(dayOfWeek: Int): String = when (dayOfWeek) {
        1 -> "Pazartesi"
        2 -> "Salı"
        3 -> "Çarşamba"
        4 -> "Perşembe"
        5 -> "Cuma"
        6 -> "Cumartesi"
        7 -> "Pazar"
        else -> ""
    }

    fun parseLocalTimeOrNull(timeStr: String): LocalTime? {
        return runCatching { LocalTime.parse(timeStr.trim(), timeFormatter) }.getOrNull()
    }
}
