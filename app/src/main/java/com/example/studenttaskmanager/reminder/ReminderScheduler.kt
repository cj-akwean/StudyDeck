package com.example.studenttaskmanager.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.studenttaskmanager.data.model.Task
import com.example.studenttaskmanager.util.DateUtils
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * One unique WorkManager job per task, scheduled `reminder_minutes` before the
 * deadline. Editing reschedules, completing/deleting cancels.
 */
class ReminderScheduler(private val context: Context) {

    fun schedule(task: Task, subjectName: String?) {
        cancel(task.id)
        val leadMinutes = task.reminderMinutes ?: return
        if (task.completed) return

        val triggerAt = DateUtils.dueDateTime(task).minusMinutes(leadMinutes.toLong())
        val delayMillis = Duration.between(LocalDateTime.now(), triggerAt).toMillis()
        if (delayMillis <= 0L) return

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    ReminderWorker.KEY_TASK_ID to task.id,
                    ReminderWorker.KEY_SUBJECT to (subjectName ?: "")
                )
            )
            .addTag(tag(task.id))
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(uniqueName(task.id), ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(taskId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueName(taskId))
    }

    private fun uniqueName(taskId: Long) = "task_reminder_$taskId"

    private fun tag(taskId: Long) = "task_reminder_tag_$taskId"
}
