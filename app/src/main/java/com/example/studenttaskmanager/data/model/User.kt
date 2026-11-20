package com.example.studenttaskmanager.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.compose.runtime.Immutable

/**
 * Table: users (documentation pack section 5.2).
 * Passwords are never stored in plain text: [passwordHash] is PBKDF2 output and
 * [salt] is the random per-user salt.
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
@Immutable
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val username: String,
    @ColumnInfo(name = "full_name") val fullName: String,
    @ColumnInfo(name = "password_hash") val passwordHash: String,
    val salt: String,
    @ColumnInfo(name = "created_at") val createdAt: String
)
