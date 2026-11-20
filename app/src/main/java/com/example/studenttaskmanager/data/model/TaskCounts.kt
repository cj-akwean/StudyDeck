package com.example.studenttaskmanager.data.model

import androidx.compose.runtime.Immutable

/**
 * Row returned by the dashboard counts query (documentation pack section 5.7).
 */
@Immutable
data class TaskCounts(
    val total: Long = 0,
    val pending: Long = 0,
    val completed: Long = 0,
    val overdue: Long = 0
)
