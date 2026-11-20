package com.example.studenttaskmanager.ui.tasks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studenttaskmanager.data.model.Subject
import com.example.studenttaskmanager.data.model.Task
import com.example.studenttaskmanager.data.model.TaskFilter
import com.example.studenttaskmanager.ui.AppViewModelProvider
import com.example.studenttaskmanager.ui.common.EmptyState
import com.example.studenttaskmanager.ui.common.FilterChipsRow
import com.example.studenttaskmanager.ui.common.SectionHeader
import com.example.studenttaskmanager.ui.common.SortMenu
import com.example.studenttaskmanager.ui.common.TaskCard
import com.example.studenttaskmanager.ui.common.TaskListSkeleton
import com.example.studenttaskmanager.ui.theme.CardShape
import com.example.studenttaskmanager.ui.theme.BottomContentPadding
import com.example.studenttaskmanager.ui.theme.Spacing
import com.example.studenttaskmanager.ui.theme.dangerColor
import com.example.studenttaskmanager.ui.theme.isDarkTheme
import com.example.studenttaskmanager.ui.theme.successColor
import com.example.studenttaskmanager.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskListScreen(
    snackbarHostState: SnackbarHostState,
    pendingFilter: TaskFilter?,
    onFilterConsumed: () -> Unit,
    onOpenTask: (Long) -> Unit,
    onAddTask: () -> Unit,
    viewModel: TaskListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.queryText.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(pendingFilter) {
        pendingFilter?.let {
            viewModel.setFilter(it)
            onFilterConsumed()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TaskListEvent.ShowUndo -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Task deleted",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDelete(event.task)
                    }
                }
                is TaskListEvent.Message -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        LargeTopAppBar(
            title = { Text("Tasks") },
            actions = {
                SortMenu(
                    selected = state.sort,
                    onSelect = viewModel::setSort,
                    modifier = Modifier.padding(end = Spacing.sm)
                )
            },
            scrollBehavior = scrollBehavior
        )

        OutlinedTextField(
            value = query,
            onValueChange = viewModel::setQuery,
            placeholder = { Text("Search tasks") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setQuery("") }) {
                        Icon(Icons.Rounded.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg)
        )

        androidx.compose.foundation.layout.Spacer(Modifier.height(Spacing.sm))

        FilterChipsRow(selected = state.filter, onSelect = viewModel::setFilter)

        androidx.compose.foundation.layout.Spacer(Modifier.height(Spacing.sm))

        when {
            !state.loaded -> TaskListSkeleton(modifier = Modifier.padding(top = Spacing.sm))

            state.tasks.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val empty = when {
                        query.isNotBlank() -> Triple(
                            Icons.Rounded.Search,
                            "No matches",
                            "Try a different title or clear the search."
                        )
                        state.filter != TaskFilter.ALL -> Triple(
                            Icons.Rounded.Checklist,
                            "Nothing here",
                            "No tasks match this filter yet."
                        )
                        else -> Triple(
                            Icons.Rounded.Checklist,
                            "No tasks yet",
                            "Add your first task and it will appear here, grouped by when it is due."
                        )
                    }
                    EmptyState(
                        icon = empty.first,
                        title = empty.second,
                        message = empty.third,
                        actionLabel = if (query.isBlank() && state.filter == TaskFilter.ALL) "Add a task" else null,
                        onAction = if (query.isBlank() && state.filter == TaskFilter.ALL) onAddTask else null
                    )
                }
            }

            else -> {
                val groups = remember(state.tasks) { DateUtils.groupTasks(state.tasks) }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = Spacing.sm, bottom = BottomContentPadding),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    groups.forEach { (group, tasks) ->
                        stickyHeader(key = "header-${group.name}") {
                            SectionHeader(
                                title = group.label,
                                count = tasks.size,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                            )
                        }
                        items(tasks, key = { it.id }) { task ->
                            SwipeableTaskRow(
                                task = task,
                                subject = state.subjectById[task.subjectId],
                                onToggle = { viewModel.toggleCompleted(task) },
                                onClick = { onOpenTask(task.id) },
                                onEdit = { onOpenTask(task.id) },
                                onDelete = { viewModel.delete(task) },
                                modifier = Modifier
                                    .padding(horizontal = Spacing.lg)
                                    .animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTaskRow(
    task: Task,
    subject: Subject?,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isDarkTheme
    val haptics = LocalHapticFeedback.current

    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> totalDistance * 0.35f }
    )

    LaunchedEffect(dismissState.currentValue) {
        when (dismissState.currentValue) {
            SwipeToDismissBoxValue.StartToEnd -> {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggle()
                dismissState.reset()
            }
            SwipeToDismissBoxValue.EndToStart -> {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
            }
            SwipeToDismissBoxValue.Settled -> Unit
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val deleting = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            val swiping = dismissState.dismissDirection != SwipeToDismissBoxValue.Settled
            if (swiping) {
                val background = if (deleting) dangerColor(dark) else successColor(dark)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CardShape)
                        .background(background)
                        .padding(horizontal = 24.dp),
                    contentAlignment = if (deleting) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Icon(
                        imageVector = if (deleting) Icons.Rounded.Delete else Icons.Rounded.Check,
                        contentDescription = if (deleting) "Delete task" else "Mark completed",
                        tint = Color.White
                    )
                }
            }
        },
        content = {
            TaskCard(
                task = task,
                subject = subject,
                onToggle = onToggle,
                onClick = onClick,
                onEdit = onEdit,
                onDelete = onDelete
            )
        }
    )
}
