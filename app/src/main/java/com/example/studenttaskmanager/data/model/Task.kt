package com.example.studenttaskmanager.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.compose.runtime.Immutable

/**
 * Table: tasks (documentation pack section 5.4).
 *
 * Dates are ISO text (`yyyy-MM-dd`), times are `HH:mm`, timestamps are ISO local
 * date-time strings, so ordering and SQLite `date()`/`time()` comparisons work.
 * A null [dueTime] means "end of day".
 */
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(name = "idx_tasks_user_due", value = ["user_id", "completed", "due_date"]),
        Index(name = "idx_tasks_title", value = ["user_id", "title"]),
        Index(name = "idx_tasks_subject", value = ["subject_id"])
    ]
)
@Immutable
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "subject_id") val subjectId: Long? = null,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val title: String,
    val description: String = "",
    /** 0 Low, 1 Medium, 2 High. */
    val priority: Int = Priority.MEDIUM.value,
    @ColumnInfo(name = "due_date") val dueDate: String,
    @ColumnInfo(name = "due_time") val dueTime: String? = null,
    /** Lead time in minutes before the deadline; null means no reminder. */
    @ColumnInfo(name = "reminder_minutes") val reminderMinutes: Int? = null,
    val completed: Boolean = false,
    @ColumnInfo(name = "completed_at") val completedAt: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String
) {
    val priorityLevel: Priority get() = Priority.from(priority)
}
