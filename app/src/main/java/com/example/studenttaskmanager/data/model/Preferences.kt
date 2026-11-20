package com.example.studenttaskmanager.data.model

import androidx.compose.runtime.Immutable

/**
 * Light/dark/system preference (see documentation pack section 5.8).
 */
enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

/**
 * Accent choices offered in Settings. [DYNAMIC] maps to Material You when the
 * platform supports it (Android 12+), otherwise it falls back to Indigo.
 */
enum class Accent {
    INDIGO, TEAL, ROSE, AMBER, FOREST, DYNAMIC
}

/**
 * Everything that lives in DataStore: onboarding, session, appearance and
 * reminder defaults. One snapshot keeps the UI reactive.
 */
@Immutable
data class AppSettings(
    val onboardingDone: Boolean = false,
    val sessionUserId: Long = -1L,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accent: Accent = Accent.INDIGO,
    val reduceMotion: Boolean = false,
    val reminderLeadMinutes: Int = 60
)
