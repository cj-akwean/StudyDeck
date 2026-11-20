package com.example.studenttaskmanager.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.studenttaskmanager.data.model.Subject
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(subject: Subject): Long

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM subjects WHERE user_id = :userId ORDER BY name COLLATE NOCASE ASC")
    fun observeForUser(userId: Long): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE user_id = :userId ORDER BY name COLLATE NOCASE ASC")
    suspend fun getForUser(userId: Long): List<Subject>
}
