package com.example.studenttaskmanager.ui.tasks

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studenttaskmanager.data.model.Priority
import com.example.studenttaskmanager.data.model.Subject
import com.example.studenttaskmanager.data.model.Task
import com.example.studenttaskmanager.data.prefs.SettingsRepository
import com.example.studenttaskmanager.data.repository.SubjectRepository
import com.example.studenttaskmanager.data.repository.TaskRepository
import com.example.studenttaskmanager.reminder.ReminderScheduler
import com.example.studenttaskmanager.util.DateUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@Immutable
data class TaskFormUiState(
    val initialized: Boolean = false,
    val isEdit: Boolean = false,
    val taskId: Long = 0L,
    val notFound: Boolean = false,
    val title: String = "",
    val description: String = "",
    val subjectId: Long? = null,
    val priority: Priority = Priority.MEDIUM,
    val dueDate: LocalDate = LocalDate.now(),
    val dueTime: LocalTime? = null,
    val reminderEnabled: Boolean = false,
    val reminderMinutes: Int = 60,
    val subjects: List<Subject> = emptyList(),
    val titleError: String? = null,
    val reminderError: String? = null,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

/**
 * One form for Add and Edit (documentation pack section 2.4). The form also
 * schedules or cancels the task's WorkManager reminder on save.
 */
class TaskFormViewModel(
    private val taskRepository: TaskRepository,
    private val subjectRepository: SubjectRepository,
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskFormUiState())
    val uiState = _uiState.asStateFlow()

    private var userId: Long = -1L
    private var existing: Task? = null
    private var started = false

    /** Called once from the screen with 0 for a new task or the task id for edit. */
    fun start(taskId: Long) {
        if (started) return
        started = true
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            userId = settings.sessionUserId
            val subjects = if (userId > 0L) subjectRepository.getForUser(userId) else emptyList()
            val task = if (taskId > 0L) taskRepository.getTask(taskId) else null
            val editableTask = task?.takeIf { it.userId == userId }
            val missing = taskId > 0L && editableTask == null
            existing = editableTask

            _uiState.update { state ->
                if (missing) {
                    state.copy(
                        initialized = true,
                        isEdit = true,
                        taskId = taskId,
                        notFound = true,
                        subjects = subjects
                    )
                } else if (editableTask != null) {
                    state.copy(
                        initialized = true,
                        isEdit = true,
                        taskId = editableTask.id,
                        title = editableTask.title,
                        description = editableTask.description,
                        subjectId = editableTask.subjectId,
                        priority = editableTask.priorityLevel,
                        dueDate = DateUtils.parseDate(editableTask.dueDate) ?: LocalDate.now(),
                        dueTime = DateUtils.parseTime(editableTask.dueTime),
                        reminderEnabled = editableTask.reminderMinutes != null,
                        reminderMinutes = editableTask.reminderMinutes ?: settings.reminderLeadMinutes,
                        subjects = subjects
                    )
                } else {
                    state.copy(
                        initialized = true,
                        subjects = subjects,
                        reminderMinutes = settings.reminderLeadMinutes
                    )
                }
            }
        }
    }

    fun onTitleChange(value: String) = _uiState.update {
        it.copy(title = value, titleError = null, error = null)
    }

    fun onDescriptionChange(value: String) = _uiState.update {
        it.copy(description = value)
    }

    fun onSubjectSelect(subjectId: Long?) = _uiState.update {
        it.copy(subjectId = subjectId)
    }

    fun onPriorityChange(priority: Priority) = _uiState.update {
        it.copy(priority = priority)
    }

    fun onDueDateChange(date: LocalDate) = _uiState.update {
        it.copy(dueDate = date, reminderError = null)
    }

    fun onDueTimeChange(time: LocalTime?) = _uiState.update {
        it.copy(dueTime = time, reminderError = null)
    }

    fun onReminderEnabledChange(enabled: Boolean) = _uiState.update {
        it.copy(reminderEnabled = enabled, reminderError = null)
    }

    fun onReminderMinutesChange(minutes: Int) = _uiState.update {
        it.copy(reminderMinutes = minutes)
    }

    fun addSubject(name: String, color: Int) {
        val clean = name.trim()
        if (clean.isEmpty() || userId <= 0L) return
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }
            try {
                val id = subjectRepository.add(userId, clean, color)
                val subjects = subjectRepository.getForUser(userId)
                _uiState.update { it.copy(subjects = subjects, subjectId = id, error = null) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(error = "Could not add that subject. Please try again.")
                }
            }
        }
    }

    /** Clears the form-level error when a new subject dialog is opened. */
    fun clearError() = _uiState.update { it.copy(error = null) }

    fun save() {
        val state = _uiState.value
        if (state.notFound) return
        val titleError = if (state.title.isBlank()) "Give the task a title." else null
        val dueAt = DateUtils.dueDateTime(state.dueDate, state.dueTime)
        val isCompleted = existing?.completed == true
        val reminderError = if (!isCompleted && state.reminderEnabled && !dueAt.isAfter(DateUtils.now())) {
            "That deadline has passed. Pick a later time or turn the reminder off."
        } else {
            null
        }

        if (titleError != null || reminderError != null) {
            _uiState.update { it.copy(titleError = titleError, reminderError = reminderError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(saving = true, error = null) }
            try {
                check(userId > 0L) { "Your session has expired. Please sign in again." }
                val now = DateUtils.timestamp()
                val draft = (existing ?: Task(
                    userId = userId,
                    title = "",
                    dueDate = DateUtils.todayIso(),
                    createdAt = now,
                    updatedAt = now
                )).copy(
                    userId = userId,
                    title = state.title.trim(),
                    description = state.description.trim(),
                    subjectId = state.subjectId,
                    priority = state.priority.value,
                    dueDate = DateUtils.isoDate(state.dueDate),
                    dueTime = state.dueTime?.let { DateUtils.isoTime(it) },
                    reminderMinutes = if (state.reminderEnabled) state.reminderMinutes else null,
                    updatedAt = now
                )

                val newId = taskRepository.save(draft)
                val saved = draft.copy(id = if (draft.id == 0L) newId else draft.id)
                val subjectName = state.subjects.firstOrNull { it.id == saved.subjectId }?.name
                if (saved.reminderMinutes != null && !saved.completed) {
                    reminderScheduler.schedule(saved, subjectName)
                } else {
                    reminderScheduler.cancel(saved.id)
                }
                _uiState.update { it.copy(saving = false, saved = true) }
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(saving = false, error = throwable.message ?: "Could not save this task.")
                }
            }
        }
    }

    fun consumeSaved() = _uiState.update { it.copy(saved = false) }

    fun consumeError() = _uiState.update { it.copy(error = null) }
}
