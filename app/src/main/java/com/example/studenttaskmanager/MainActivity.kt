package com.example.studenttaskmanager

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.studenttaskmanager.data.model.AppSettings
import com.example.studenttaskmanager.reminder.NotificationHelper
import com.example.studenttaskmanager.ui.navigation.StudyDeckNavHost
import com.example.studenttaskmanager.ui.splash.SplashScreen
import com.example.studenttaskmanager.ui.theme.StudyDeckTheme

/**
 * Single host activity. It installs the platform splash, loads preferences
 * while a matching Compose splash is shown, applies the saved theme and hands
 * off to the navigation graph. Notification taps are turned into a pending task
 * id that the graph opens as the edit form.
 */
class MainActivity : ComponentActivity() {

    private var pendingTaskId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        applyMaxRefreshRate()

        // Only consume a notification id on a fresh launch. On rotation the
        // navigation state is restored, so re-reading the intent would open a
        // duplicate form.
        if (savedInstanceState == null) {
            pendingTaskId = intent
                ?.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1L)
                ?.takeIf { it > 0L }
        }

        val container = (application as StudyDeckApplication).container

        setContent {
            var settings by remember { mutableStateOf<AppSettings?>(null) }

            LaunchedEffect(Unit) {
                container.settingsRepository.settings.collect { settings = it }
            }

            val loaded = settings
            StudyDeckTheme(settings = loaded ?: AppSettings()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (loaded == null) {
                        SplashScreen()
                    } else {
                        StudyDeckNavHost(
                            settings = loaded,
                            pendingTaskId = pendingTaskId,
                            onTaskIdConsumed = { pendingTaskId = null }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1L)
        if (taskId > 0L) {
            pendingTaskId = taskId
        }
    }

    override fun onResume() {
        super.onResume()
        // Some OEM builds reset the display mode after permission dialogs or
        // PiP, so re-apply on every resume.
        applyMaxRefreshRate()
    }

    /**
     * Ask the system for the highest refresh rate at the *current* resolution.
     * Keeping the resolution avoids a visible mode switch, and falling back to
     * the default mode is safer than forcing a lower-resolution mode on
     * devices that expose unusual mode lists.
     */
    private fun applyMaxRefreshRate() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        val display = display ?: return
        val current = display.mode ?: return
        val best = display.supportedModes
            .asSequence()
            .filter {
                it.physicalWidth == current.physicalWidth &&
                    it.physicalHeight == current.physicalHeight
            }
            .maxByOrNull { it.refreshRate }
            ?: return

        val attributes: WindowManager.LayoutParams = window.attributes
        if (attributes.preferredDisplayModeId == best.modeId &&
            attributes.preferredRefreshRate == best.refreshRate
        ) {
            return
        }
        attributes.preferredDisplayModeId = best.modeId
        attributes.preferredRefreshRate = best.refreshRate
        window.attributes = attributes

        if (BuildConfig.DEBUG) {
            Log.d(
                "StudyDeck",
                "Refresh rate: ${best.refreshRate} Hz (mode ${best.modeId})"
            )
        }
    }
}
