package com.example.studenttaskmanager.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.studenttaskmanager.data.model.DueGroup
import com.example.studenttaskmanager.data.model.Subject
import com.example.studenttaskmanager.data.model.Task
import com.example.studenttaskmanager.data.model.TaskFilter
import com.example.studenttaskmanager.data.model.TaskSort
import com.example.studenttaskmanager.ui.theme.CardGap
import com.example.studenttaskmanager.ui.theme.CardPadding
import com.example.studenttaskmanager.ui.theme.CardShape
import com.example.studenttaskmanager.ui.theme.ChipShape
import com.example.studenttaskmanager.ui.theme.MotionSprings
import com.example.studenttaskmanager.ui.theme.ScreenPadding
import com.example.studenttaskmanager.ui.theme.ShapeCache
import com.example.studenttaskmanager.ui.theme.SmoothCornerShape
import com.example.studenttaskmanager.ui.theme.Spacing
import com.example.studenttaskmanager.ui.theme.dangerColor
import com.example.studenttaskmanager.ui.theme.isDarkTheme
import com.example.studenttaskmanager.ui.theme.motionEnabled
import com.example.studenttaskmanager.ui.theme.subjectContainerColor
import com.example.studenttaskmanager.ui.theme.subjectContentColor
import com.example.studenttaskmanager.ui.theme.successColor
import com.example.studenttaskmanager.ui.theme.warningColor
import com.example.studenttaskmanager.util.DateUtils
import java.time.LocalTime

@Composable
fun StudyDeckLogo(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(SmoothCornerShape(size * 0.32f))
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = "StudyDeck logo",
            tint = contentColor,
            modifier = Modifier.size(size * 0.52f)
        )
    }
}

@Composable
fun InitialsAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val initials = name.trim()
        .split(' ')
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "S" }

    Box(
        modifier = modifier
            .size(size)
            .clip(if (size >= 40.dp) SmoothCornerShape(size * 0.34f) else CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun StatCard(
    value: Long,
    label: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CardPadding)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(SmoothCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(Spacing.md))
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    count: Int? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (count != null) {
            Spacer(Modifier.width(Spacing.sm))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.weight(1f))
        trailing?.invoke()
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val motion = motionEnabled
    val entrance by animateFloatAsState(
        targetValue = if (!motion || appeared) 1f else 0.9f,
        animationSpec = if (motion) MotionSprings.Soft else snap(),
        label = "emptyEntrance"
    )
    val entranceAlpha by animateFloatAsState(
        targetValue = if (!motion || appeared) 1f else 0f,
        animationSpec = if (motion) tween(220) else snap(),
        label = "emptyAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = entrance
                scaleY = entrance
                alpha = entranceAlpha
            }
            .padding(horizontal = Spacing.xxl, vertical = Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(SmoothCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(Modifier.height(Spacing.lg))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Spacing.lg))
            androidx.compose.material3.Button(
                onClick = onAction,
                shape = ShapeCache.smooth16
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun ShimmerBlock(
    modifier: Modifier = Modifier,
    shape: Shape = ShapeCache.smooth16
) {
    val base = MaterialTheme.colorScheme.surfaceVariant
    if (!motionEnabled) {
        Box(modifier.clip(shape).background(base.copy(alpha = 0.5f)))
        return
    }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(base.copy(alpha = alpha))
    )
}

@Composable
fun TaskListSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(CardGap)
    ) {
        repeat(4) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(CardPadding)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ShimmerBlock(Modifier.size(20.dp), CircleShape)
                    Spacer(Modifier.width(Spacing.md))
                    ShimmerBlock(Modifier.height(14.dp).fillMaxWidth(0.6f))
                }
                Spacer(Modifier.height(Spacing.md))
                ShimmerBlock(Modifier.height(10.dp).fillMaxWidth(0.9f))
                Spacer(Modifier.height(Spacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    ShimmerBlock(Modifier.height(20.dp).width(72.dp), ChipShape)
                    ShimmerBlock(Modifier.height(20.dp).width(92.dp), ChipShape)
                }
            }
        }
    }
}

@Composable
fun SubjectTag(subject: Subject, modifier: Modifier = Modifier) {
    val dark = isDarkTheme
    Box(
        modifier = modifier
            .clip(ChipShape)
            .background(subjectContainerColor(subject.color, dark))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = subject.name,
            style = MaterialTheme.typography.labelMedium,
            color = subjectContentColor(subject.color, dark),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SubjectTag(name: String, colorArgb: Int, modifier: Modifier = Modifier) {
    val dark = isDarkTheme
    Box(
        modifier = modifier
            .clip(ChipShape)
            .background(subjectContainerColor(colorArgb, dark))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = subjectContentColor(colorArgb, dark),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DueChip(task: Task, modifier: Modifier = Modifier) {
    val dark = isDarkTheme
    val now = LocalNow.current
    val group = DateUtils.dueGroup(task, now)
    val color = when (group) {
        DueGroup.OVERDUE -> dangerColor(dark)
        DueGroup.TODAY -> warningColor(dark)
        DueGroup.COMPLETED -> successColor(dark)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Rounded.Schedule,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = DateUtils.dueLabel(task, now),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun FilterChipsRow(
    selected: TaskFilter,
    onSelect: (TaskFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        items(TaskFilter.entries.toList()) { filter ->
            val isSelected = filter == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(filter) },
                label = {
                    Text(
                        text = filter.label,
                        fontWeight = if (isSelected) {
                            androidx.compose.ui.text.font.FontWeight.SemiBold
                        } else {
                            androidx.compose.ui.text.font.FontWeight.Medium
                        }
                    )
                },
                shape = ChipShape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
fun SortMenu(
    selected: TaskSort,
    onSelect: (TaskSort) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Sort,
                contentDescription = "Sort tasks",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TaskSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(option.label, modifier = Modifier.weight(1f))
                            if (option == selected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TrailingMenu(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Task actions",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    expanded = false
                    onEdit()
                }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    expanded = false
                    onDelete()
                }
            )
        }
    }
}

@Composable
fun TimeLabel(time: LocalTime?, modifier: Modifier = Modifier) {
    if (time != null) {
        Text(
            text = DateUtils.formatTime(time),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
        )
    }
}

@Composable
fun NoTasksHint(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Nothing here yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Floating squircle snackbar. When it carries an action (Undo), a thin line
 * drains across the bottom so the remaining time is visible — the reference
 * pattern for destructive feedback.
 */
@Composable
fun StudyDeckSnackbar(
    data: SnackbarData,
    modifier: Modifier = Modifier
) {
    val actionLabel = data.visuals.actionLabel
    val countdown = remember { Animatable(1f) }

    LaunchedEffect(data) {
        if (actionLabel != null) {
            countdown.snapTo(1f)
            countdown.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 4_000, easing = LinearEasing)
            )
        }
    }

    Surface(
        modifier = modifier.padding(horizontal = ScreenPadding),
        shape = ShapeCache.smooth16,
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        shadowElevation = 8.dp
    ) {
        Column {
            Row(
                modifier = Modifier.padding(start = Spacing.lg, end = Spacing.sm, top = Spacing.sm, bottom = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.visuals.message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                if (actionLabel != null) {
                    TextButton(onClick = { data.performAction() }) {
                        Text(
                            text = actionLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.inversePrimary
                        )
                    }
                }
            }
            if (actionLabel != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.18f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(countdown.value)
                            .height(3.dp)
                            .background(MaterialTheme.colorScheme.inversePrimary)
                    )
                }
            }
        }
    }
}
