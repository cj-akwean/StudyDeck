package com.example.studenttaskmanager.ui.tasks

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studenttaskmanager.data.model.Subject
import com.example.studenttaskmanager.data.model.Task
import com.example.studenttaskmanager.data.model.TaskFilter
import com.example.studenttaskmanager.data.model.TaskSort
import com.example.studenttaskmanager.data.prefs.SettingsRepository
import com.example.studenttaskmanager.data.repository.SubjectRepository
import com.example.studenttaskmanager.data.repository.TaskRepository
import com.example.studenttaskmanager.reminder.ReminderScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val subjectById: Map<Long, Subject> = emptyMap(),
    val filter: TaskFilter = TaskFilter.ALL,
    val sort: TaskSort = TaskSort.DUE_DATE,
    val loaded: Boolean = false
)

sealed interface TaskListEvent {
    data class ShowUndo(val task: Task) : TaskListEvent
    data class Message(val text: String) : TaskListEvent
}

private data class QuerySnapshot(
    val userId: Long,
    val filter: TaskFilter,
    val sort: TaskSort,
    val query: String
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class TaskListViewModel(
    private val taskRepository: TaskRepository,
    private val subjectRepository: SubjectRepository,
    settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val queryInput = MutableStateFlow("")
    val queryText: StateFlow<String> = queryInput.asStateFlow()

    /**
     * The text field updates immediately; Room only sees a settled query. A
     * blank query is not delayed, so clearing search feels instant.
     */
    private val debouncedQuery = queryInput
        .debounce { if (it.isBlank()) 0L else 220L }
        .distinctUntilChanged()

    private val filter = MutableStateFlow(TaskFilter.ALL)
    private val sort = MutableStateFlow(TaskSort.DUE_DATE)

    private val eventsChannel = Channel<TaskListEvent>(Channel.BUFFERED)
    val events = eventsChannel.receiveAsFlow()

    val uiState: StateFlow<TaskListUiState> = combine(
        settingsRepository.settings.map { it.sessionUserId }.distinctUntilChanged(),
        debouncedQuery,
        filter,
        sort
    ) { userId, query, selectedFilter, selectedSort ->
        QuerySnapshot(userId, selectedFilter, selectedSort, query)
    }.flatMapLatest { snapshot ->
        if (snapshot.userId <= 0L) {
            flowOf(
                TaskListUiState(
                    filter = snapshot.filter,
                    sort = snapshot.sort,
                    loaded = true
                )
            )
        } else {
            combine(
                taskRepository.observeTasks(
                    userId = snapshot.userId,
                    filter = snapshot.filter.value,
                    query = snapshot.query,
                    sort = snapshot.sort.value
                ),
                subjectRepository.observeForUser(snapshot.userId)
            ) { tasks, subjects ->
                TaskListUiState(
                    tasks = tasks,
                    subjects = subjects,
                    subjectById = subjects.associateBy { it.id },
                    filter = snapshot.filter,
                    sort = snapshot.sort,
                    loaded = true
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TaskListUiState()
    )

    fun setFilter(value: TaskFilter) {
        filter.value = value
    }

    fun setSort(value: TaskSort) {
        sort.value = value
    }

    fun setQuery(value: String) {
        queryInput.value = value
    }

    fun toggleCompleted(task: Task) {
        viewModelScope.launch {
            val nowCompleted = !task.completed
            taskRepository.setCompleted(task, nowCompleted)
            if (nowCompleted) {
                reminderScheduler.cancel(task.id)
            } else if (task.reminderMinutes != null) {
                reminderScheduler.schedule(task.copy(completed = false), subjectName(task.subjectId))
            }
        }
    }

    fun delete(task: Task) {
        viewModelScope.launch {
            taskRepository.delete(task)
            reminderScheduler.cancel(task.id)
            eventsChannel.send(TaskListEvent.ShowUndo(task))
        }
    }

    fun undoDelete(task: Task) {
        viewModelScope.launch {
            taskRepository.restore(task)
            if (task.reminderMinutes != null && !task.completed) {
                reminderScheduler.schedule(task, subjectName(task.subjectId))
            }
            eventsChannel.send(TaskListEvent.Message("Task restored"))
        }
    }

    private fun subjectName(subjectId: Long?): String? =
        uiState.value.subjects.firstOrNull { it.id == subjectId }?.name
}
