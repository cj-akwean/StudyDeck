package com.example.studenttaskmanager.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studenttaskmanager.data.model.Subject
import com.example.studenttaskmanager.data.model.Task
import com.example.studenttaskmanager.data.model.TaskFilter
import com.example.studenttaskmanager.ui.AppViewModelProvider
import com.example.studenttaskmanager.ui.common.InitialsAvatar
import com.example.studenttaskmanager.ui.common.LocalNow
import com.example.studenttaskmanager.ui.common.ProgressRing
import com.example.studenttaskmanager.ui.common.SectionHeader
import com.example.studenttaskmanager.ui.common.StatCard
import com.example.studenttaskmanager.ui.common.SubjectTag
import com.example.studenttaskmanager.ui.theme.CardShape
import com.example.studenttaskmanager.ui.theme.BottomContentPadding
import com.example.studenttaskmanager.ui.theme.ShapeCache
import com.example.studenttaskmanager.ui.theme.Spacing
import com.example.studenttaskmanager.ui.theme.dangerColor
import com.example.studenttaskmanager.ui.theme.isDarkTheme
import com.example.studenttaskmanager.ui.theme.priorityColor
import com.example.studenttaskmanager.ui.theme.successColor
import com.example.studenttaskmanager.ui.theme.warningColor
import com.example.studenttaskmanager.util.DateUtils
import java.time.LocalDateTime
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOpenTasks: (TaskFilter) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenTask: (Long) -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val dark = isDarkTheme
    val now = LocalNow.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        LargeTopAppBar(
            title = {
                Column {
                    Text(
                        text = "${DateUtils.greeting(now)}, ${state.userName}",
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = DateUtils.formatDateLong(now.toLocalDate()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            actions = {
                InitialsAvatar(
                    name = state.userName,
                    modifier = Modifier
                        .padding(end = Spacing.lg)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenProfile)
                )
            },
            scrollBehavior = scrollBehavior
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = Spacing.lg, end = Spacing.lg, top = Spacing.sm, bottom = BottomContentPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item {
                ProgressCard(
                    progress = state.progress,
                    completedDueToday = state.completedDueToday,
                    dueToday = state.dueToday.size,
                    overdue = state.counts.overdue,
                    dark = dark
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        StatCard(
                            value = state.counts.total,
                            label = "Total",
                            icon = Icons.AutoMirrored.Rounded.List,
                            tint = MaterialTheme.colorScheme.primary,
                            onClick = { onOpenTasks(TaskFilter.ALL) },
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            value = state.counts.pending,
                            label = "Pending",
                            icon = Icons.Rounded.Schedule,
                            tint = warningColor(dark),
                            onClick = { onOpenTasks(TaskFilter.PENDING) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        StatCard(
                            value = state.counts.completed,
                            label = "Completed",
                            icon = Icons.Rounded.CheckCircle,
                            tint = successColor(dark),
                            onClick = { onOpenTasks(TaskFilter.COMPLETED) },
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            value = state.counts.overdue,
                            label = "Overdue",
                            icon = Icons.Rounded.Warning,
                            tint = dangerColor(dark),
                            onClick = { onOpenTasks(TaskFilter.OVERDUE) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Due today",
                    count = state.dueToday.size,
                    modifier = Modifier.padding(top = Spacing.sm)
                )
            }

            if (state.dueToday.isEmpty()) {
                item { HintCard("Nothing due today. Enjoy the breathing room.") }
            } else {
                items(state.dueToday.take(3), key = { "due-${it.id}" }) { task ->
                    DashboardTaskRow(
                        task = task,
                        subject = state.subjects[task.subjectId],
                        now = now,
                        onClick = { onOpenTask(task.id) }
                    )
                }
            }

            item {
                SectionHeader(
                    title = "Next up",
                    count = state.upcoming.size,
                    modifier = Modifier.padding(top = Spacing.sm)
                )
            }

            if (state.upcoming.isEmpty()) {
                item { HintCard("No upcoming deadlines. Add one so future-you remembers.") }
            } else {
                items(state.upcoming, key = { "next-${it.id}" }) { task ->
                    DashboardTaskRow(
                        task = task,
                        subject = state.subjects[task.subjectId],
                        now = now,
                        onClick = { onOpenTask(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressCard(
    progress: Float,
    completedDueToday: Int,
    dueToday: Int,
    overdue: Long,
    dark: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProgressRing(progress = progress) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (dueToday == 0) "—" else "${(progress * 100).roundToInt()}%",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (dueToday == 0) "due today" else "done",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(Spacing.xl))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Today's progress",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = when {
                        dueToday == 0 -> "No tasks due today. You're all clear."
                        completedDueToday >= dueToday -> "All $dueToday tasks due today are done."
                        else -> "$completedDueToday of $dueToday tasks due today completed."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.md))
                Box(
                    modifier = Modifier
                        .clip(ShapeCache.smoothPill)
                        .background(
                            if (overdue > 0) {
                                dangerColor(dark).copy(alpha = 0.12f)
                            } else {
                                successColor(dark).copy(alpha = 0.12f)
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = when {
                            overdue > 0 -> "$overdue overdue — start there."
                            dueToday > 0 && completedDueToday >= dueToday -> "All done for today."
                            else -> "You're on track."
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (overdue > 0) dangerColor(dark) else successColor(dark)
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardTaskRow(
    task: Task,
    subject: Subject?,
    now: LocalDateTime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dotColor = when {
        task.completed -> successColor(isDarkTheme)
        else -> priorityColor(task.priorityLevel)
    }
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (task.completed) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (task.completed) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (subject != null) {
                        SubjectTag(subject = subject, modifier = Modifier.weight(1f, fill = false))
                        Spacer(Modifier.width(Spacing.sm))
                    }
                    Text(
                        text = DateUtils.dueLabel(task, now),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun HintCard(text: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(Spacing.lg)
        )
    }
}
