package com.example.studenttaskmanager.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.studenttaskmanager.data.model.AppSettings
import com.example.studenttaskmanager.data.model.Priority
import com.example.studenttaskmanager.data.model.ThemeMode
import androidx.compose.ui.graphics.Color

val LocalDarkTheme = staticCompositionLocalOf { false }
val LocalReduceMotion = staticCompositionLocalOf { false }

val isDarkTheme: Boolean
    @Composable get() = LocalDarkTheme.current

val reduceMotion: Boolean
    @Composable get() = LocalReduceMotion.current

@Composable
fun priorityColor(priority: Priority): Color = priorityColor(priority, isDarkTheme)

/**
 * Applies theme mode, accent and reduce-motion preferences to the whole tree.
 */
@Composable
fun StudyDeckTheme(settings: AppSettings, content: @Composable () -> Unit) {
    val darkTheme = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val scheme = if (settings.accent == com.example.studenttaskmanager.data.model.Accent.DYNAMIC) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        accentColorScheme(settings.accent, darkTheme)
    }

    val systemAnimatorScale = rememberSystemAnimatorScale()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalReduceMotion provides settings.reduceMotion,
        LocalMotionEnabled provides (!settings.reduceMotion && systemAnimatorScale > 0f)
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = StudyDeckTypography,
            shapes = StudyDeckShapes,
            content = content
        )
    }
}
