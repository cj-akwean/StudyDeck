package com.example.studenttaskmanager.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.studenttaskmanager.StudyDeckApplication

/**
 * Fires the notification for one task. It re-reads the task so a reminder never
 * shows for work that was completed after the job was scheduled.
 */
class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getLong(KEY_TASK_ID, -1L)
        if (taskId <= 0L) return Result.success()

        val app = applicationContext as? StudyDeckApplication ?: return Result.success()
        val task = app.container.taskRepository.getTask(taskId) ?: return Result.success()
        if (task.completed) return Result.success()

        NotificationHelper.notifyTask(
            context = applicationContext,
            task = task,
            subjectName = inputData.getString(KEY_SUBJECT)
        )
        return Result.success()
    }

    companion object {
        const val KEY_TASK_ID = "task_id"
        const val KEY_SUBJECT = "subject_name"
    }
}
