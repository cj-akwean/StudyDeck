package com.example.studenttaskmanager.data.repository

import com.example.studenttaskmanager.data.local.TaskDao
import com.example.studenttaskmanager.data.model.Task
import com.example.studenttaskmanager.data.model.TaskCounts
import com.example.studenttaskmanager.util.DateUtils
import kotlinx.coroutines.flow.Flow

/**
 * The only class that talks to [TaskDao]. All reads are reactive Flows, all
 * writes are suspend functions executed off the main thread by Room.
 */
class TaskRepository(private val taskDao: TaskDao) {

    fun observeTasks(userId: Long, filter: Int, query: String, sort: Int): Flow<List<Task>> =
        taskDao.observeTasks(userId, filter, escapeLike(query), sort)

    fun observeCounts(userId: Long, today: String, nowTime: String): Flow<TaskCounts> =
        taskDao.observeCounts(userId, today, nowTime)

    fun observeDueToday(userId: Long, today: String): Flow<List<Task>> =
        taskDao.observeDueToday(userId, today)

    fun observeUpcoming(userId: Long, today: String, limit: Int = 3): Flow<List<Task>> =
        taskDao.observeUpcoming(userId, today, limit)

    fun observeCompletedCount(userId: Long): Flow<Int> = taskDao.observeCompletedCount(userId)

    fun observeCompletedThisWeek(userId: Long, weekStart: String): Flow<Int> =
        taskDao.observeCompletedThisWeek(userId, weekStart)

    fun observeTask(id: Long): Flow<Task?> = taskDao.observeById(id)

    suspend fun getTask(id: Long): Task? = taskDao.getById(id)

    /** Insert (id = 0) or update an existing task; returns the row id. */
    suspend fun save(task: Task): Long {
        return if (task.id == 0L) {
            taskDao.insert(task)
        } else {
            taskDao.update(task)
            task.id
        }
    }

    suspend fun setCompleted(task: Task, completed: Boolean) {
        taskDao.setCompleted(
            id = task.id,
            completed = completed,
            completedAt = if (completed) DateUtils.timestamp() else null,
            updatedAt = DateUtils.timestamp()
        )
    }

    suspend fun delete(task: Task) = taskDao.deleteById(task.id)

    /** Undo: re-insert the deleted row with its original id. */
    suspend fun restore(task: Task) = taskDao.insert(task)

    suspend fun clearCompleted(userId: Long): Int = taskDao.deleteCompleted(userId)

    /**
     * Makes user text literal inside a SQL LIKE pattern. Without this, "%"
     * matches everything and "_" matches any character.
     */
    private fun escapeLike(value: String): String = value
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_")
}
