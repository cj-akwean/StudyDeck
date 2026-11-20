package com.example.studenttaskmanager.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.studenttaskmanager.util.TimeProvider
import kotlinx.coroutines.delay
import java.time.LocalDateTime

/** The current minute, shared by every relative date label in the shell. */
val LocalNow = staticCompositionLocalOf { LocalDateTime.now() }

/**
 * Re-emits on the minute boundary so "Today", due groups, overdue labels and
 * the dashboard greeting cannot go stale while the app stays open.
 */
@Composable
fun ProvideNow(content: @Composable () -> Unit) {
    val now by produceState(initialValue = LocalDateTime.now()) {
        while (true) {
            value = LocalDateTime.now()
            delay(TimeProvider.millisUntilNextMinute())
        }
    }
    CompositionLocalProvider(LocalNow provides now, content = content)
}
