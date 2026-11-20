package com.example.studenttaskmanager.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.studenttaskmanager.data.model.Accent
import com.example.studenttaskmanager.data.model.AppSettings
import com.example.studenttaskmanager.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.studyDeckDataStore: DataStore<Preferences> by preferencesDataStore(name = "studydeck_settings")

/**
 * Lightweight key/value storage (documentation pack section 5.8): onboarding,
 * session, theme, accent, reduce motion and the default reminder lead time.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val onboardingDone = booleanPreferencesKey("onboarding_done")
        val sessionUserId = longPreferencesKey("session_user_id")
        val themeMode = stringPreferencesKey("theme_mode")
        val accent = stringPreferencesKey("accent")
        val reduceMotion = booleanPreferencesKey("reduce_motion")
        val reminderLead = intPreferencesKey("reminder_default_minutes")
    }

    val settings: Flow<AppSettings> = context.studyDeckDataStore.data
        .catch { throwable ->
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }
        .map { prefs ->
            AppSettings(
                onboardingDone = prefs[Keys.onboardingDone] ?: false,
                sessionUserId = prefs[Keys.sessionUserId] ?: -1L,
                themeMode = enumOrDefault(prefs[Keys.themeMode], ThemeMode.SYSTEM),
                accent = enumOrDefault(prefs[Keys.accent], Accent.INDIGO),
                reduceMotion = prefs[Keys.reduceMotion] ?: false,
                reminderLeadMinutes = prefs[Keys.reminderLead] ?: 60
            )
        }

    suspend fun setOnboardingDone(done: Boolean = true) {
        context.studyDeckDataStore.edit { it[Keys.onboardingDone] = done }
    }

    suspend fun setSessionUser(userId: Long) {
        context.studyDeckDataStore.edit { it[Keys.sessionUserId] = userId }
    }

    suspend fun clearSession() {
        context.studyDeckDataStore.edit { it[Keys.sessionUserId] = -1L }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.studyDeckDataStore.edit { it[Keys.themeMode] = mode.name }
    }

    suspend fun setAccent(accent: Accent) {
        context.studyDeckDataStore.edit { it[Keys.accent] = accent.name }
    }

    suspend fun setReduceMotion(enabled: Boolean) {
        context.studyDeckDataStore.edit { it[Keys.reduceMotion] = enabled }
    }

    suspend fun setReminderLeadMinutes(minutes: Int) {
        context.studyDeckDataStore.edit { it[Keys.reminderLead] = minutes }
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(name: String?, fallback: T): T =
        name?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: fallback
}
