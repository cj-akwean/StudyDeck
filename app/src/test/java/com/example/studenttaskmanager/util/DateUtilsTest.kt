package com.example.studenttaskmanager.util

import com.example.studenttaskmanager.data.model.DueGroup
import com.example.studenttaskmanager.data.model.Task
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DateUtilsTest {

    private val now: LocalDateTime = LocalDateTime.of(2026, 3, 10, 9, 0)

    private fun task(
        dueDate: LocalDate,
        dueTime: LocalTime? = null,
        completed: Boolean = false,
        id: Long = 1L
    ): Task = Task(
        id = id,
        userId = 1L,
        title = "Test task",
        dueDate = DateUtils.isoDate(dueDate),
        dueTime = dueTime?.let { DateUtils.isoTime(it) },
        completed = completed,
        createdAt = DateUtils.timestamp(now),
        updatedAt = DateUtils.timestamp(now)
    )

    @Test
    fun `task without a time is due at the end of the day`() {
        val due = DateUtils.dueDateTime(task(now.toLocalDate()))
        assertEquals(LocalDateTime.of(now.toLocalDate(), LocalTime.of(23, 59)), due)
    }

    @Test
    fun `due group marks yesterday as overdue`() {
        val group = DateUtils.dueGroup(task(now.toLocalDate().minusDays(1)), now)
        assertEquals(DueGroup.OVERDUE, group)
    }

    @Test
    fun `due group marks an earlier time today as overdue`() {
        val group = DateUtils.dueGroup(task(now.toLocalDate(), LocalTime.of(8, 0)), now)
        assertEquals(DueGroup.OVERDUE, group)
    }

    @Test
    fun `due group marks today and tomorrow`() {
        assertEquals(
            DueGroup.TODAY,
            DateUtils.dueGroup(task(now.toLocalDate(), LocalTime.of(18, 0)), now)
        )
        assertEquals(
            DueGroup.TOMORROW,
            DateUtils.dueGroup(task(now.toLocalDate().plusDays(1)), now)
        )
    }

    @Test
    fun `due group buckets the rest of the week and later`() {
        assertEquals(
            DueGroup.THIS_WEEK,
            DateUtils.dueGroup(task(now.toLocalDate().plusDays(3)), now)
        )
        assertEquals(
            DueGroup.LATER,
            DateUtils.dueGroup(task(now.toLocalDate().plusDays(10)), now)
        )
    }

    @Test
    fun `completed tasks always land in the completed group`() {
        val group = DateUtils.dueGroup(
            task(now.toLocalDate().minusDays(5), completed = true),
            now
        )
        assertEquals(DueGroup.COMPLETED, group)
    }

    @Test
    fun `due label describes overdue days without relying on locale`() {
        val label = DateUtils.dueLabel(task(now.toLocalDate().minusDays(2)), now)
        assertEquals("Overdue by 2 days", label)
    }

    @Test
    fun `due label says today and tomorrow for untimed tasks`() {
        assertEquals(
            "Today",
            DateUtils.dueLabel(task(now.toLocalDate()), now)
        )
        assertEquals(
            "Tomorrow",
            DateUtils.dueLabel(task(now.toLocalDate().plusDays(1)), now)
        )
    }

    @Test
    fun `grouping orders sections and puts completed last`() {
        val tasks = listOf(
            task(now.toLocalDate().plusDays(10), id = 1L),          // Later
            task(now.toLocalDate().minusDays(1), id = 2L),          // Overdue
            task(now.toLocalDate(), completed = true, id = 3L),     // Completed
            task(now.toLocalDate().plusDays(1), id = 4L),           // Tomorrow
            task(now.toLocalDate(), id = 5L)                        // Today
        )

        val groups = DateUtils.groupTasks(tasks, now).map { it.first }

        assertEquals(
            listOf(
                DueGroup.OVERDUE,
                DueGroup.TODAY,
                DueGroup.TOMORROW,
                DueGroup.LATER,
                DueGroup.COMPLETED
            ),
            groups
        )
    }

    @Test
    fun `grouping sorts by due date then priority`() {
        val high = task(now.toLocalDate().plusDays(2), id = 1L).copy(priority = 2)
        val low = task(now.toLocalDate().plusDays(2), id = 2L).copy(priority = 0)
        val sooner = task(now.toLocalDate().plusDays(1), id = 3L)

        val grouped = DateUtils.groupTasks(listOf(high, low, sooner), now)
        val thisWeek = grouped.first { it.first == DueGroup.THIS_WEEK }.second
        val tomorrow = grouped.first { it.first == DueGroup.TOMORROW }.second

        assertEquals(listOf(1L, 2L), thisWeek.map { it.id })
        assertEquals(listOf(3L), tomorrow.map { it.id })
    }

    @Test
    fun `greeting follows the hour of day`() {
        assertEquals("Good morning", DateUtils.greeting(LocalDateTime.of(2026, 3, 10, 7, 0)))
        assertEquals("Good afternoon", DateUtils.greeting(LocalDateTime.of(2026, 3, 10, 14, 0)))
        assertEquals("Good evening", DateUtils.greeting(LocalDateTime.of(2026, 3, 10, 20, 0)))
    }

    @Test
    fun `iso round trip keeps date and time`() {
        val date = LocalDate.of(2026, 12, 31)
        val time = LocalTime.of(23, 45)

        assertEquals(date, DateUtils.parseDate(DateUtils.isoDate(date)))
        assertEquals(time, DateUtils.parseTime(DateUtils.isoTime(time)))
        assertEquals(null, DateUtils.parseDate(null))
        assertEquals(null, DateUtils.parseTime(""))
    }
}
