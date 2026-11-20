package com.example.studenttaskmanager.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.studenttaskmanager.data.model.Priority
import com.example.studenttaskmanager.data.model.Subject
import com.example.studenttaskmanager.data.model.Task
import com.example.studenttaskmanager.data.model.label
import com.example.studenttaskmanager.ui.theme.MotionSprings
import com.example.studenttaskmanager.ui.theme.ShapeCache
import com.example.studenttaskmanager.ui.theme.Spacing
import com.example.studenttaskmanager.ui.theme.isDarkTheme
import com.example.studenttaskmanager.ui.theme.motionEnabled
import com.example.studenttaskmanager.ui.theme.priorityColor
import com.example.studenttaskmanager.ui.theme.successColor

/**
 * The task row from the component catalog: priority rail, morphing check,
 * title, one-line description, subject tag, due chip and an overflow menu that
 * keeps swipe actions available to TalkBack users.
 *
 * The priority rail is painted by drawBehind instead of an IntrinsicSize row,
 * and the card shape is cached, so scrolling does not run a second measure pass
 * or rebuild a squircle path per row.
 */
@Composable
fun TaskCard(
    task: Task,
    subject: Subject?,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isDarkTheme
    val motion = motionEnabled
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (motion && pressed) 0.98f else 1f,
        animationSpec = if (motion) MotionSprings.Snappy else snap(),
        label = "taskScale"
    )

    val stripeColor = if (task.completed) {
        MaterialTheme.colorScheme.outlineVariant
    } else {
        priorityColor(task.priorityLevel)
    }
    val containerColor = if (task.completed) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (dark) 0.35f else 0.45f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val titleColor = if (task.completed) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = ShapeCache.smooth20,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (dark) 0.45f else 0.55f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(
                        color = stripeColor,
                        size = Size(4.dp.toPx(), size.height)
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    AnimatedTaskCheck(
                        checked = task.completed,
                        onToggle = onToggle,
                        modifier = Modifier.padding(start = 6.dp, top = 8.dp)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp, top = 10.dp)
                    ) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = titleColor,
                            textDecoration = if (task.completed) TextDecoration.LineThrough else null,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (task.description.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    TrailingMenu(onEdit = onEdit, onDelete = onDelete)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 46.dp, end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (subject != null) {
                            SubjectTag(subject = subject)
                        }
                        PriorityPill(priority = task.priorityLevel)
                    }
                    DueChip(task = task)
                }
            }
        }
    }
}

/**
 * Circular check that fills, pops and morphs its tick in with a bouncy spring.
 * A plain M3 checkbox cannot animate its corner/scale, and this keeps the
 * whole row's motion consistent.
 */
@Composable
private fun AnimatedTaskCheck(
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val success = successColor(isDarkTheme)
    val motion = motionEnabled
    val borderColor = if (checked) success else MaterialTheme.colorScheme.outline
    val fill by animateColorAsState(
        targetValue = if (checked) success else MaterialTheme.colorScheme.surface.copy(alpha = 0f),
        label = "checkFill"
    )
    val border by animateColorAsState(targetValue = borderColor, label = "checkBorder")
    val tickScale by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = if (motion) MotionSprings.Bouncy else snap(),
        label = "checkTick"
    )

    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(fill)
            .border(1.6.dp, border, CircleShape)
            .toggleable(
                value = checked,
                onValueChange = { onToggle() },
                role = Role.Checkbox
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .size(18.dp)
                .scale(tickScale)
        )
    }
}

@Composable
private fun PriorityPill(priority: Priority, modifier: Modifier = Modifier) {
    val color = priorityColor(priority)
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = priority.label,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}
