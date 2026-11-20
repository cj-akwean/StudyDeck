package com.example.studenttaskmanager.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * One shared source of "what time is it" for reactive queries.
 *
 * Room only re-runs a Flow when its tables change, so date-based queries need a
 * clock edge of their own. Emitting on the minute boundary keeps due-time
 * comparisons fresh without polling aggressively.
 */
object TimeProvider {

    fun nowFlow(): Flow<LocalDateTime> = flow {
        while (true) {
            val now = LocalDateTime.now()
            emit(now)
            delay(millisUntilNextMinute(now))
        }
    }

    fun dateFlow(): Flow<LocalDate> = nowFlow()
        .map { it.toLocalDate() }
        .distinctUntilChanged()

    fun millisUntilNextMinute(from: LocalDateTime = LocalDateTime.now()): Long {
        val next = from.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1).plusSeconds(1)
        val millis = java.time.Duration.between(from, next).toMillis()
        return millis.coerceAtLeast(250L)
    }
}
