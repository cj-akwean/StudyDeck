package com.example.studenttaskmanager.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.studenttaskmanager.data.model.Task
import com.example.studenttaskmanager.data.model.TaskCounts
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    /**
     * The single list query: user, filter chip, search text and sort mode.
     * The CASE ordering keeps the three sort modes in one prepared statement
     * (documentation pack section 5.7).
     */
    @Query(
        """
        SELECT * FROM tasks
        WHERE user_id = :userId
          AND (:filter = 0
               OR (:filter = 1 AND completed = 0)
               OR (:filter = 2 AND completed = 1)
               OR (:filter = 3 AND completed = 0 AND priority = 2)
               OR (:filter = 4 AND completed = 0 AND (
                    due_date < date('now','localtime')
                    OR (due_date = date('now','localtime')
                        AND due_time IS NOT NULL AND due_time != ''
                        AND due_time < time('now','localtime'))
               )))
          AND title LIKE '%' || :query || '%' ESCAPE '\'
        ORDER BY
          CASE :sort WHEN 0 THEN due_date END ASC,
          CASE :sort WHEN 0 THEN COALESCE(NULLIF(due_time, ''), '23:59') END ASC,
          CASE :sort WHEN 1 THEN priority END DESC,
          CASE :sort WHEN 2 THEN created_at END DESC,
          due_date ASC,
          priority DESC
        """
    )
    fun observeTasks(userId: Long, filter: Int, query: String, sort: Int): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<Task?>

    /** REPLACE also implements Undo: the row is re-inserted with its original id. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        """
        UPDATE tasks
        SET completed = :completed, completed_at = :completedAt, updated_at = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun setCompleted(id: Long, completed: Boolean, completedAt: String?, updatedAt: String)

    @Query("DELETE FROM tasks WHERE user_id = :userId AND completed = 1")
    suspend fun deleteCompleted(userId: Long): Int

    @Query(
        """
        SELECT COUNT(*) AS total,
               COALESCE(SUM(CASE WHEN completed = 0 THEN 1 ELSE 0 END), 0) AS pending,
               COALESCE(SUM(CASE WHEN completed = 1 THEN 1 ELSE 0 END), 0) AS completed,
               COALESCE(SUM(CASE WHEN completed = 0 AND (
                    due_date < :today
                    OR (due_date = :today
                        AND due_time IS NOT NULL AND due_time != ''
                        AND due_time < :nowTime)
               ) THEN 1 ELSE 0 END), 0) AS overdue
        FROM tasks
        WHERE user_id = :userId
        """
    )
    fun observeCounts(userId: Long, today: String, nowTime: String): Flow<TaskCounts>

    @Query(
        """
        SELECT * FROM tasks
        WHERE user_id = :userId AND due_date = :today
        ORDER BY completed ASC, priority DESC, due_time ASC
        """
    )
    fun observeDueToday(userId: Long, today: String): Flow<List<Task>>

    @Query(
        """
        SELECT * FROM tasks
        WHERE user_id = :userId AND completed = 0
          AND due_date > :today
        ORDER BY due_date ASC, COALESCE(NULLIF(due_time, ''), '23:59') ASC
        LIMIT :limit
        """
    )
    fun observeUpcoming(userId: Long, today: String, limit: Int): Flow<List<Task>>

    @Query("SELECT COUNT(*) FROM tasks WHERE user_id = :userId AND completed = 1")
    fun observeCompletedCount(userId: Long): Flow<Int>

    @Query(
        """
        SELECT COUNT(*) FROM tasks
        WHERE user_id = :userId AND completed = 1
          AND completed_at IS NOT NULL
          AND date(completed_at) >= :weekStart
        """
    )
    fun observeCompletedThisWeek(userId: Long, weekStart: String): Flow<Int>
}
