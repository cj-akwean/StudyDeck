package com.example.studenttaskmanager.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.compose.runtime.Immutable

/**
 * Table: subjects (documentation pack section 5.3).
 * Deleting a user removes their subjects; deleting a subject leaves its tasks in
 * place with a null subject (see [Task]).
 */
@Entity(
    tableName = "subjects",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["user_id"])]
)
@Immutable
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "user_id") val userId: Long,
    val name: String,
    /** ARGB colour chosen from [SubjectPalette]. */
    val color: Int
)
