package com.example.studenttaskmanager.ui.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studenttaskmanager.data.model.Accent
import com.example.studenttaskmanager.data.model.AppSettings
import com.example.studenttaskmanager.data.model.ThemeMode
import com.example.studenttaskmanager.data.prefs.SettingsRepository
import com.example.studenttaskmanager.data.repository.TaskRepository
import com.example.studenttaskmanager.data.repository.UserRepository
import com.example.studenttaskmanager.util.DateUtils
import com.example.studenttaskmanager.util.TimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters

@Immutable
data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val userName: String = "",
    val username: String = "",
    val memberSince: String? = null,
    val completedThisWeek: Int = 0,
    val completedCount: Int = 0,
    val loaded: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = settingsRepository.settings
        .flatMapLatest { settings ->
            if (settings.sessionUserId <= 0L) {
                flowOf(SettingsUiState(settings = settings, loaded = true))
            } else {
                TimeProvider.dateFlow().flatMapLatest { today ->
                    val weekStart = today
                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        .toString()
                    combine(
                        userRepository.observeById(settings.sessionUserId),
                        taskRepository.observeCompletedThisWeek(settings.sessionUserId, weekStart),
                        taskRepository.observeCompletedCount(settings.sessionUserId)
                    ) { user, completedThisWeek, completedCount ->
                        SettingsUiState(
                            settings = settings,
                            userName = user?.fullName.orEmpty(),
                            username = user?.username.orEmpty(),
                            memberSince = user?.createdAt
                                ?.substringBefore('T')
                                ?.let(DateUtils::parseDate)
                                ?.let(DateUtils::formatDateLong),
                            completedThisWeek = completedThisWeek,
                            completedCount = completedCount,
                            loaded = true
                        )
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setAccent(accent: Accent) {
        viewModelScope.launch { settingsRepository.setAccent(accent) }
    }

    fun setReduceMotion(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setReduceMotion(enabled) }
    }

    fun setReminderLeadMinutes(minutes: Int) {
        viewModelScope.launch { settingsRepository.setReminderLeadMinutes(minutes) }
    }

    fun clearCompleted() {
        val userId = uiState.value.settings.sessionUserId
        if (userId <= 0L) return
        viewModelScope.launch { taskRepository.clearCompleted(userId) }
    }

    fun logout() {
        viewModelScope.launch { settingsRepository.clearSession() }
    }
}
