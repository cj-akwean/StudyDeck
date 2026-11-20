package com.example.studenttaskmanager.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.studenttaskmanager.StudyDeckApplication
import com.example.studenttaskmanager.ui.auth.AuthViewModel
import com.example.studenttaskmanager.ui.home.DashboardViewModel
import com.example.studenttaskmanager.ui.settings.SettingsViewModel
import com.example.studenttaskmanager.ui.tasks.TaskFormViewModel
import com.example.studenttaskmanager.ui.tasks.TaskListViewModel

/**
 * One factory for every screen ViewModel; dependencies come from the app's
 * manual container.
 */
object AppViewModelProvider {

    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = this.application()
            AuthViewModel(app.container.userRepository, app.container.settingsRepository)
        }
        initializer {
            val app = this.application()
            DashboardViewModel(
                app.container.taskRepository,
                app.container.subjectRepository,
                app.container.userRepository,
                app.container.settingsRepository
            )
        }
        initializer {
            val app = this.application()
            TaskListViewModel(
                app.container.taskRepository,
                app.container.subjectRepository,
                app.container.settingsRepository,
                app.container.reminderScheduler
            )
        }
        initializer {
            val app = this.application()
            TaskFormViewModel(
                app.container.taskRepository,
                app.container.subjectRepository,
                app.container.settingsRepository,
                app.container.reminderScheduler
            )
        }
        initializer {
            val app = this.application()
            SettingsViewModel(
                app.container.settingsRepository,
                app.container.userRepository,
                app.container.taskRepository
            )
        }
    }
}

private fun CreationExtras.application(): StudyDeckApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as StudyDeckApplication
