package com.example.studenttaskmanager.util

import com.example.studenttaskmanager.data.model.DueGroup
import com.example.studenttaskmanager.data.model.Task
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * All date and time handling lives here so storage format, friendly labels and
 * grouping always agree (documentation pack section 2.4).
 */
object DateUtils {

    private val ISO_DATE: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val ISO_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
    private val SQL_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US)
    private val PRETTY_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())
    private val PRETTY_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

    fun today(): LocalDate = LocalDate.now()

    fun now(): LocalDateTime = LocalDateTime.now()

    fun todayIso(): String = today().toString()

    fun isoDate(date: LocalDate): String = date.format(ISO_DATE)

    fun isoTime(time: LocalTime): String = time.format(ISO_TIME)

    /** Seconds included, for SQLite `due_time < :nowTime` comparisons. */
    fun sqlTime(at: LocalDateTime = now()): String = at.toLocalTime().format(SQL_TIME)

    /** ISO local date-time used for created_at / updated_at / completed_at. */
    fun timestamp(at: LocalDateTime = now()): String = at.toString()

    fun parseDate(value: String?): LocalDate? =
        value?.takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it, ISO_DATE) }.getOrNull() }

    fun parseTime(value: String?): LocalTime? =
        value?.takeIf { it.isNotBlank() }?.let { runCatching { LocalTime.parse(it, ISO_TIME) }.getOrNull() }

    fun parseTimestamp(value: String?): LocalDateTime? =
        value?.takeIf { it.isNotBlank() }?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }

    /** A task without a time is due at the end of its day. */
    fun dueDateTime(task: Task): LocalDateTime {
        val date = parseDate(task.dueDate) ?: today()
        val time = parseTime(task.dueTime) ?: LocalTime.of(23, 59)
        return LocalDateTime.of(date, time)
    }

    fun dueDateTime(date: LocalDate, time: LocalTime?): LocalDateTime =
        LocalDateTime.of(date, time ?: LocalTime.of(23, 59))

    fun toEpochMillis(date: LocalDate, time: LocalTime? = null): Long =
        dueDateTime(date, time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    /** The Material date picker works in UTC midnights. */
    fun fromUtcPickerMillis(millis: Long): LocalDate =
        java.time.Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()

    fun toUtcPickerMillis(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()

    fun formatDateLong(date: LocalDate): String = date.format(PRETTY_DATE)

    fun formatTime(time: LocalTime): String = time.format(PRETTY_TIME)

    fun formatDateTime(date: LocalDate, time: LocalTime?): String =
        if (time == null) formatDateLong(date) else "${formatDateLong(date)}, ${formatTime(time)}"

    fun greeting(at: LocalDateTime = now()): String = when (at.hour) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }

    fun daysBetween(from: LocalDate, to: LocalDate): Long = ChronoUnit.DAYS.between(from, to)

    /**
     * Friendly due text: "Overdue by 2 days", "Today, 9:00 AM", "Tomorrow",
     * "In 3 days", "Mon, Nov 10". Completed tasks simply read "Completed".
     */
    fun dueLabel(task: Task, at: LocalDateTime = now()): String {
        if (task.completed) return "Completed"
        val dueDate = parseDate(task.dueDate) ?: at.toLocalDate()
        val time = parseTime(task.dueTime)
        val today = at.toLocalDate()
        val days = daysBetween(today, dueDate)
        val overdue = if (time == null) {
            // An all-day task is due at the end of its day, so it only becomes
            // overdue once the calendar day has passed.
            dueDate.isBefore(today)
        } else {
            LocalDateTime.of(dueDate, time).isBefore(at)
        }

        if (overdue) {
            return when {
                days <= -2L -> "Overdue by ${-days} days"
                days == -1L -> "Overdue since yesterday"
                else -> if (time != null) "Overdue since ${formatTime(time)}" else "Overdue"
            }
        }
        return when {
            days == 0L -> if (time != null) "Today, ${formatTime(time)}" else "Today"
            days == 1L -> if (time != null) "Tomorrow, ${formatTime(time)}" else "Tomorrow"
            days in 2L..6L -> if (time != null) "In $days days, ${formatTime(time)}" else "In $days days"
            else -> formatDateTime(dueDate, time)
        }
    }

    fun dueGroup(task: Task, at: LocalDateTime = now()): DueGroup {
        if (task.completed) return DueGroup.COMPLETED
        val dueDate = parseDate(task.dueDate) ?: at.toLocalDate()
        val time = parseTime(task.dueTime)
        val overdue = if (time == null) {
            dueDate.isBefore(at.toLocalDate())
        } else {
            LocalDateTime.of(dueDate, time).isBefore(at)
        }
        if (overdue) return DueGroup.OVERDUE
        val days = daysBetween(at.toLocalDate(), dueDate)
        return when {
            days == 0L -> DueGroup.TODAY
            days == 1L -> DueGroup.TOMORROW
            days in 2L..6L -> DueGroup.THIS_WEEK
            else -> DueGroup.LATER
        }
    }

    /**
     * Turns a flat sorted list into the list's sections, in display order, with
     * completed work last.
     */
    fun groupTasks(tasks: List<Task>, at: LocalDateTime = now()): List<Pair<DueGroup, List<Task>>> =
        tasks.groupBy { dueGroup(it, at) }
            .toList()
            .sortedBy { (group, _) -> group.ordinal }
            .map { (group, items) ->
                val sorted = when (group) {
                    DueGroup.COMPLETED -> items.sortedByDescending { parseTimestamp(it.completedAt) ?: LocalDateTime.MIN }
                    else -> items.sortedWith(
                        compareBy<Task> { dueDateTime(it) }
                            .thenByDescending { it.priority }
                    )
                }
                group to sorted
            }
}
