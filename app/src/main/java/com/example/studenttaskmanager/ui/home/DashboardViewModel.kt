package com.example.studenttaskmanager.ui.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studenttaskmanager.data.model.Subject
import com.example.studenttaskmanager.data.model.Task
import com.example.studenttaskmanager.data.model.TaskCounts
import com.example.studenttaskmanager.data.prefs.SettingsRepository
import com.example.studenttaskmanager.data.repository.SubjectRepository
import com.example.studenttaskmanager.data.repository.TaskRepository
import com.example.studenttaskmanager.data.repository.UserRepository
import com.example.studenttaskmanager.util.DateUtils
import com.example.studenttaskmanager.util.TimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Immutable
data class DashboardUiState(
    val userName: String = "Student",
    val counts: TaskCounts = TaskCounts(),
    val dueToday: List<Task> = emptyList(),
    val upcoming: List<Task> = emptyList(),
    val subjects: Map<Long, Subject> = emptyMap(),
    val loaded: Boolean = false
) {
    val completedDueToday: Int get() = dueToday.count { it.completed }

    /** Ring progress: tasks due today that are completed, over all due today. */
    val progress: Float
        get() = if (dueToday.isEmpty()) 0f else (completedDueToday.toFloat() / dueToday.size).coerceIn(0f, 1f)
}

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val taskRepository: TaskRepository,
    private val subjectRepository: SubjectRepository,
    private val userRepository: UserRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = settingsRepository.settings
        .map { it.sessionUserId }
        .distinctUntilChanged()
        .flatMapLatest { userId ->
            if (userId <= 0L) {
                flowOf(DashboardUiState(loaded = true))
            } else {
                TimeProvider.nowFlow().flatMapLatest { now ->
                    val today = DateUtils.isoDate(now.toLocalDate())
                    val nowTime = DateUtils.sqlTime(now)
                    val core = combine(
                        userRepository.observeById(userId),
                        taskRepository.observeCounts(userId, today, nowTime),
                        taskRepository.observeDueToday(userId, today),
                        subjectRepository.observeForUser(userId)
                    ) { user, counts, dueToday, subjects ->
                        DashboardUiState(
                            userName = user?.fullName?.trim()?.substringBefore(' ')
                                ?.takeIf { it.isNotBlank() } ?: "Student",
                            counts = counts,
                            dueToday = dueToday,
                            subjects = subjects.associateBy { it.id },
                            loaded = true
                        )
                    }
                    combine(core, taskRepository.observeUpcoming(userId, today, 3)) { state, upcoming ->
                        state.copy(upcoming = upcoming)
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState()
        )
}
