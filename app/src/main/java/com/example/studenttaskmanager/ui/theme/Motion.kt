package com.example.studenttaskmanager.ui.theme

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Motion tokens derived from the PixelPlayer design report (§3.6 and §6).
 *
 * Three duration tiers keep the app coherent: small state changes settle
 * quickly, screens take the standard beat, and hero/large surfaces breathe.
 * Springs are named so components do not invent their own physics.
 */
object MotionDuration {
    const val Quick = 225
    const val Standard = 350
    const val Large = 450
}

val EmphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
val StandardEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

object MotionSprings {
    /** Slight overshoot for playful feedback (tab icons, checkboxes). */
    val Bouncy: SpringSpec<Float> = spring(dampingRatio = 0.65f, stiffness = 380f)

    /** Calm settle for cards and surfaces. */
    val Soft: SpringSpec<Float> = spring(dampingRatio = 0.85f, stiffness = 300f)

    /** Fast, near-critical spring for layout shifts. */
    val Snappy: SpringSpec<Float> = spring(dampingRatio = 0.9f, stiffness = 500f)
}

object MotionSpringOffsets {
    val Slide: SpringSpec<Offset> = spring(dampingRatio = 0.85f, stiffness = 300f)
}

/**
 * True when animation should run. Combines the in-app Reduce motion setting
 * with the platform animator duration scale, re-read on every resume so a
 * developer-options change is picked up without a restart.
 */
val LocalMotionEnabled = staticCompositionLocalOf { true }

val motionEnabled: Boolean
    @Composable get() = LocalMotionEnabled.current

/** Animator scale (0 when animations are disabled system-wide). */
@Composable
fun rememberSystemAnimatorScale(): Float {
    val context = LocalContext.current
    var scale by remember {
        mutableFloatStateOf(readAnimatorScale(context))
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scale = readAnimatorScale(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return scale
}

private fun readAnimatorScale(context: Context): Float = runCatching {
    Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    )
}.getOrDefault(1f)

/**
 * Tween that collapses to a snap when motion is disabled. Use for every
 * duration-based animation in the app.
 */
@Composable
fun <T> motionTween(
    durationMillis: Int,
    easing: androidx.compose.animation.core.Easing = EmphasizedEasing
): androidx.compose.animation.core.FiniteAnimationSpec<T> =
    if (LocalMotionEnabled.current) tween(durationMillis, easing = easing) else snap()

/** Spring that collapses to a snap when motion is disabled. */
@Composable
fun <T> motionSpring(spec: SpringSpec<T>): androidx.compose.animation.core.FiniteAnimationSpec<T> =
    if (LocalMotionEnabled.current) spec else snap()
