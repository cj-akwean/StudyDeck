package com.example.studenttaskmanager.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.studenttaskmanager.StudyDeckApplication
import com.example.studenttaskmanager.data.model.AppSettings
import com.example.studenttaskmanager.ui.auth.LoginScreen
import com.example.studenttaskmanager.ui.auth.RegisterScreen
import com.example.studenttaskmanager.ui.main.MainScreen
import com.example.studenttaskmanager.ui.onboarding.OnboardingScreen
import com.example.studenttaskmanager.ui.settings.AboutScreen
import com.example.studenttaskmanager.ui.settings.ProfileScreen
import com.example.studenttaskmanager.ui.tasks.TaskFormScreen
import com.example.studenttaskmanager.ui.theme.EmphasizedEasing
import com.example.studenttaskmanager.ui.theme.MotionDuration
import com.example.studenttaskmanager.ui.theme.motionEnabled
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

private val authRoutes = setOf(Routes.ONBOARDING, Routes.LOGIN, Routes.REGISTER)

/**
 * The single navigation graph. The start destination is resolved once from the
 * settings loaded during the splash, then auth transitions clear the stack so
 * Back never returns to a screen the user has left behind.
 */
@Composable
fun StudyDeckNavHost(
    settings: AppSettings,
    pendingTaskId: Long?,
    onTaskIdConsumed: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val startDestination = remember {
        when {
            !settings.onboardingDone -> Routes.ONBOARDING
            settings.sessionUserId <= 0L -> Routes.LOGIN
            else -> Routes.MAIN
        }
    }

    val context = LocalContext.current
    val application = context.applicationContext as StudyDeckApplication
    val scope = rememberCoroutineScope()

    // Depth transitions: incoming screens rise from 96 % with a clip-like
    // scale, outgoing screens dim and drop behind. Duration collapses to zero
    // when Reduce motion or the system animator scale is off.
    val animate = motionEnabled
    val enterDuration = if (animate) MotionDuration.Standard else 0
    val fadeDuration = if (animate) MotionDuration.Quick else 0
    val enterTransition: EnterTransition = slideInHorizontally(
        animationSpec = tween(enterDuration, easing = EmphasizedEasing)
    ) { fullWidth -> fullWidth / 10 } +
        fadeIn(tween(fadeDuration)) +
        scaleIn(
            initialScale = 0.96f,
            animationSpec = tween(enterDuration, easing = EmphasizedEasing)
        )
    val exitTransition: ExitTransition = fadeOut(tween(fadeDuration)) +
        scaleOut(
            targetScale = 0.98f,
            animationSpec = tween(enterDuration, easing = EmphasizedEasing)
        )
    val popEnterTransition: EnterTransition = slideInHorizontally(
        animationSpec = tween(enterDuration, easing = EmphasizedEasing)
    ) { fullWidth -> -fullWidth / 10 } +
        fadeIn(tween(fadeDuration)) +
        scaleIn(
            initialScale = 0.98f,
            animationSpec = tween(enterDuration, easing = EmphasizedEasing)
        )
    val popExitTransition: ExitTransition = slideOutHorizontally(
        animationSpec = tween(enterDuration, easing = EmphasizedEasing)
    ) { fullWidth -> fullWidth / 16 } +
        fadeOut(tween(fadeDuration)) +
        scaleOut(
            targetScale = 1.02f,
            animationSpec = tween(enterDuration, easing = EmphasizedEasing)
        )

    // Notification tap: open that task's edit form once a session exists and the
    // auth screens are behind us. Navigating while LOGIN is still on top would be
    // undone by the login callback's popUpTo(LOGIN), so wait for a real app route.
    LaunchedEffect(pendingTaskId, settings.sessionUserId) {
        val taskId = pendingTaskId ?: return@LaunchedEffect
        if (settings.sessionUserId <= 0L) return@LaunchedEffect
        navController.currentBackStackEntryFlow
            .mapNotNull { it.destination.route }
            .first { it !in authRoutes }
        navController.navigate(Routes.taskForm(taskId))
        onTaskIdConsumed()
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { enterTransition },
        exitTransition = { exitTransition },
        popEnterTransition = { popEnterTransition },
        popExitTransition = { popExitTransition }
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onDone = {
                    scope.launch {
                        application.container.settingsRepository.setOnboardingDone(true)
                    }
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onCreateAccount = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegistered = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MAIN) { entry ->
            val savedMessage by entry.savedStateHandle
                .getStateFlow("task_saved", "")
                .collectAsStateWithLifecycle()

            MainScreen(
                message = savedMessage.takeIf { it.isNotBlank() },
                onMessageShown = { entry.savedStateHandle["task_saved"] = "" },
                onAddTask = { navController.navigate(Routes.taskForm()) },
                onOpenTask = { taskId -> navController.navigate(Routes.taskForm(taskId)) },
                onOpenProfile = { navController.navigate(Routes.PROFILE) },
                onOpenAbout = { navController.navigate(Routes.ABOUT) },
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.TASK_FORM,
            arguments = listOf(
                navArgument(Routes.TASK_FORM_ARG) {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { entry ->
            val taskId = entry.arguments?.getLong(Routes.TASK_FORM_ARG) ?: 0L
            TaskFormScreen(
                taskId = taskId,
                onClose = { navController.popBackStack() },
                onSaved = { isEdit ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("task_saved", if (isEdit) "Task updated" else "Task added")
                    navController.popBackStack()
                }
            )
        }
    }
}
