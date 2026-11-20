package com.example.studenttaskmanager.data.model

/**
 * Priority values stored in the tasks table (0 Low, 1 Medium, 2 High).
 */
enum class Priority(val value: Int) {
    LOW(0), MEDIUM(1), HIGH(2);

    companion object {
        fun from(value: Int): Priority = entries.firstOrNull { it.value == value } ?: MEDIUM
    }
}

/**
 * Grouping used by the task list (documentation pack section 1.3).
 */
enum class DueGroup(val label: String) {
    OVERDUE("Overdue"),
    TODAY("Today"),
    TOMORROW("Tomorrow"),
    THIS_WEEK("This week"),
    LATER("Later"),
    COMPLETED("Completed")
}

/**
 * Filter chips on the task list.
 */
enum class TaskFilter(val value: Int, val label: String) {
    ALL(0, "All"),
    PENDING(1, "Pending"),
    COMPLETED(2, "Completed"),
    HIGH(3, "High priority"),
    OVERDUE(4, "Overdue")
}

/**
 * Sort options for the task list.
 */
enum class TaskSort(val value: Int, val label: String) {
    DUE_DATE(0, "Due date"),
    PRIORITY(1, "Priority"),
    NEWEST(2, "Newest")
}

/**
 * Convenience accessors used by the UI.
 */
val Priority.label: String
    get() = when (this) {
        Priority.LOW -> "Low"
        Priority.MEDIUM -> "Medium"
        Priority.HIGH -> "High"
    }
