package com.example.studenttaskmanager.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.studenttaskmanager.data.model.TaskFilter
import com.example.studenttaskmanager.ui.home.DashboardScreen
import com.example.studenttaskmanager.ui.settings.SettingsScreen
import com.example.studenttaskmanager.ui.tasks.TaskListScreen
import com.example.studenttaskmanager.ui.common.ProvideNow
import com.example.studenttaskmanager.ui.common.StudyDeckSnackbar
import com.example.studenttaskmanager.ui.theme.MotionDuration
import com.example.studenttaskmanager.ui.theme.MotionSprings
import com.example.studenttaskmanager.ui.theme.ShapeCache
import com.example.studenttaskmanager.ui.theme.motionEnabled
import com.example.studenttaskmanager.ui.theme.motionTween

enum class MainTab(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    HOME("Home", Icons.Outlined.Home, Icons.Rounded.Home),
    TASKS("Tasks", Icons.Outlined.Checklist, Icons.Rounded.Checklist),
    SETTINGS("Settings", Icons.Outlined.Settings, Icons.Rounded.Settings)
}

/**
 * The signed-in shell: three tabs behind a floating squircle bar, a shared
 * snackbar host (used for Undo and save confirmations) and the add-task FAB.
 * Dashboard stat cards deep-link into the task list via [pendingFilter].
 */
@Composable
fun MainScreen(
    message: String?,
    onMessageShown: () -> Unit,
    onAddTask: () -> Unit,
    onOpenTask: (Long) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenAbout: () -> Unit,
    onLoggedOut: () -> Unit
) {
    var selectedTabName by rememberSaveable { mutableStateOf(MainTab.HOME.name) }
    val selectedTab = MainTab.entries.firstOrNull { it.name == selectedTabName } ?: MainTab.HOME
    var pendingFilter by rememberSaveable { mutableStateOf<TaskFilter?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val stateHolder = rememberSaveableStateHolder()
    val animate = motionEnabled
    val tabFade = if (animate) 140 else 0

    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                StudyDeckSnackbar(data)
            }
        },
        bottomBar = {
            FloatingBottomBar(
                selected = selectedTab,
                onSelect = { selectedTabName = it.name }
            )
        },
        floatingActionButton = {
            if (selectedTab != MainTab.SETTINGS) {
                AddTaskFab(onAddTask)
            }
        }
    ) { _ ->
        // The screens own their large top app bars and apply status-bar insets,
        // so the shell must not add them again. Bottom space for the floating
        // bar is reserved by each screen's list content padding.
        ProvideNow {
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn(tween(tabFade)) togetherWith fadeOut(tween(tabFade))
                    },
                    label = "mainTab"
                ) { tab ->
                    stateHolder.SaveableStateProvider(tab.name) {
                        when (tab) {
                            MainTab.HOME -> DashboardScreen(
                                onOpenTasks = { filter ->
                                    pendingFilter = filter
                                    selectedTabName = MainTab.TASKS.name
                                },
                                onOpenProfile = onOpenProfile,
                                onOpenTask = onOpenTask
                            )

                            MainTab.TASKS -> TaskListScreen(
                                snackbarHostState = snackbarHostState,
                                pendingFilter = pendingFilter,
                                onFilterConsumed = { pendingFilter = null },
                                onOpenTask = onOpenTask,
                                onAddTask = onAddTask
                            )

                            MainTab.SETTINGS -> SettingsScreen(
                                onOpenProfile = onOpenProfile,
                                onOpenAbout = onOpenAbout,
                                onLoggedOut = onLoggedOut
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddTaskFab(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val motion = motionEnabled
    val fabScale by animateFloatAsState(
        targetValue = if (motion && pressed) 0.94f else 1f,
        animationSpec = if (motion) MotionSprings.Snappy else snap(),
        label = "fabScale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(58.dp)
            .graphicsLayer {
                scaleX = fabScale
                scaleY = fabScale
            },
        shape = ShapeCache.smooth20,
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 12.dp,
        interactionSource = interactionSource
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Add task",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

/**
 * Floating bar with a capsule that slides between tabs. The capsule width and
 * offset are derived from the measured bar width, so it works on every screen
 * size without subcomposition.
 */
@Composable
private fun FloatingBottomBar(
    selected: MainTab,
    onSelect: (MainTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Surface(
            shape = ShapeCache.smooth28,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 0.dp,
            shadowElevation = 14.dp,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
            )
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp)
            ) {
                val motion = motionEnabled
                val itemWidth = maxWidth / MainTab.entries.size
                val capsuleWidth = itemWidth - 12.dp
                val density = LocalDensity.current
                val targetOffsetPx = with(density) {
                    (itemWidth * selected.ordinal.toFloat() + 6.dp).toPx()
                }
                val capsuleOffset by animateFloatAsState(
                    targetValue = targetOffsetPx,
                    animationSpec = if (motion) {
                        androidx.compose.animation.core.spring(
                            dampingRatio = 0.85f,
                            stiffness = 300f
                        )
                    } else {
                        snap()
                    },
                    label = "tabCapsule"
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .graphicsLayer { translationX = capsuleOffset }
                        .width(capsuleWidth)
                        .height(54.dp)
                        .clip(ShapeCache.smoothPill)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MainTab.entries.forEach { tab ->
                        TabItem(
                            tab = tab,
                            selected = tab == selected,
                            onClick = { onSelect(tab) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabItem(
    tab: MainTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = if (motionEnabled) MotionSprings.Bouncy else snap(),
        label = "tabIconScale"
    )
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val labelAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.62f,
        animationSpec = if (motionEnabled) motionTween(MotionDuration.Quick) else snap(),
        label = "tabLabelAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) tab.selectedIcon else tab.icon,
            contentDescription = tab.label,
            tint = contentColor,
            modifier = Modifier
                .size(23.dp)
                .scale(iconScale)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.alpha(labelAlpha)
        )
    }
}
