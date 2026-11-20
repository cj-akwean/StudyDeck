package com.example.studenttaskmanager.di

import android.content.Context
import com.example.studenttaskmanager.data.local.AppDatabase
import com.example.studenttaskmanager.data.prefs.SettingsRepository
import com.example.studenttaskmanager.data.repository.SubjectRepository
import com.example.studenttaskmanager.data.repository.TaskRepository
import com.example.studenttaskmanager.data.repository.UserRepository
import com.example.studenttaskmanager.reminder.ReminderScheduler

/**
 * Manual dependency container. One database, one set of repositories, one
 * scheduler for the lifetime of the process.
 */
class AppContainer(context: Context) {

    private val database: AppDatabase = AppDatabase.build(context)

    val settingsRepository: SettingsRepository = SettingsRepository(context)
    val userRepository: UserRepository = UserRepository(database.userDao(), database.subjectDao())
    val subjectRepository: SubjectRepository = SubjectRepository(database.subjectDao())
    val taskRepository: TaskRepository = TaskRepository(database.taskDao())
    val reminderScheduler: ReminderScheduler = ReminderScheduler(context)
}
