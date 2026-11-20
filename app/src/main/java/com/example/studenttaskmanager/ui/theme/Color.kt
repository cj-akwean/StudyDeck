package com.example.studenttaskmanager.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.example.studenttaskmanager.data.model.Accent
import com.example.studenttaskmanager.data.model.Priority

/**
 * PixelPlayer palette (docs/design.md section 3.1).
 *
 * The measured roles are primary, secondary, tertiary, background, surface,
 * onSurface and error. The remaining container/outline roles are derived in the
 * same hue family so the app still has a complete Material 3 scheme.
 *
 * There are deliberately no gradient helpers here: depth comes from surface
 * steps, corners and scale, never from a decorative Brush.
 */
private val LightSchemeBase: ColorScheme = lightColorScheme(
    primary = Color(0xFF6C4FF5),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE3DBFF),
    onPrimaryContainer = Color(0xFF23005C),
    secondary = Color(0xFFF06292),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD9E4),
    onSecondaryContainer = Color(0xFF3F001D),
    tertiary = Color(0xFFFF8A65),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFFFFDBCF),
    onTertiaryContainer = Color(0xFF3A0B00),
    background = Color(0xFFF7F2FF),
    onBackground = Color(0xFF1E1237),
    surface = Color(0xFFFBF8FF),
    onSurface = Color(0xFF1E1237),
    surfaceVariant = Color(0xFFE8DEF9),
    onSurfaceVariant = Color(0xFF4D4165),
    surfaceTint = Color(0xFF6C4FF5),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFCF9FF),
    surfaceContainer = Color(0xFFF3EDFF),
    surfaceContainerHigh = Color(0xFFEDE5FC),
    surfaceContainerHighest = Color(0xFFE7DDF8),
    outline = Color(0xFF78659A),
    outlineVariant = Color(0xFFC9BDD9),
    inverseSurface = Color(0xFF332A45),
    inverseOnSurface = Color(0xFFF5EEFF),
    inversePrimary = Color(0xFFCBBEFF),
    error = Color(0xFFD32F2F),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkSchemeBase: ColorScheme = darkColorScheme(
    primary = Color(0xFFAB47BC),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF4A2A52),
    onPrimaryContainer = Color(0xFFF6D9FA),
    secondary = Color(0xFFF06292),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF5C2A3D),
    onSecondaryContainer = Color(0xFFFFD9E4),
    tertiary = Color(0xFFFF8A65),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF5A2E1E),
    onTertiaryContainer = Color(0xFFFFDBCF),
    background = Color(0xFF1E1234),
    onBackground = Color(0xFFE1BEE7),
    surface = Color(0xFF2A1F40),
    onSurface = Color(0xFFE1BEE7),
    surfaceVariant = Color(0xFF3A2E52),
    onSurfaceVariant = Color(0xFFCDBFD9),
    surfaceTint = Color(0xFFAB47BC),
    surfaceContainerLowest = Color(0xFF170D2A),
    surfaceContainerLow = Color(0xFF241936),
    surfaceContainer = Color(0xFF2A1F40),
    surfaceContainerHigh = Color(0xFF332747),
    surfaceContainerHighest = Color(0xFF3D3053),
    outline = Color(0xFF9A86B5),
    outlineVariant = Color(0xFF4A3B60),
    inverseSurface = Color(0xFFE1BEE7),
    inverseOnSurface = Color(0xFF332A45),
    inversePrimary = Color(0xFF6C4FF5),
    error = Color(0xFFFF5252),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

val LightScheme: ColorScheme = LightSchemeBase
val DarkScheme: ColorScheme = DarkSchemeBase

// ---------------------------------------------------------------------------
// Accent presets. The default (INDIGO) is the PixelPlayer palette; the other
// presets only replace the primary role family. All of them are flat colours.
// ---------------------------------------------------------------------------

private data class AccentRoles(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color
)

private val TealLightRoles = AccentRoles(
    Color(0xFF00695C), Color(0xFFFFFFFF), Color(0xFFB2F0E6), Color(0xFF00201B)
)
private val TealDarkRoles = AccentRoles(
    Color(0xFF6FD8C6), Color(0xFF00382F), Color(0xFF005045), Color(0xFF8EF5E2)
)
private val RoseLightRoles = AccentRoles(
    Color(0xFFA8325C), Color(0xFFFFFFFF), Color(0xFFFFD9E2), Color(0xFF3F001D)
)
private val RoseDarkRoles = AccentRoles(
    Color(0xFFFFB1C8), Color(0xFF5F1130), Color(0xFF7D2949), Color(0xFFFFD9E2)
)
private val AmberLightRoles = AccentRoles(
    Color(0xFF8A5A00), Color(0xFFFFFFFF), Color(0xFFFFDEA8), Color(0xFF2B1A00)
)
private val AmberDarkRoles = AccentRoles(
    Color(0xFFFFC061), Color(0xFF482A00), Color(0xFF663E00), Color(0xFFFFDEA8)
)
private val ForestLightRoles = AccentRoles(
    Color(0xFF2E6B3A), Color(0xFFFFFFFF), Color(0xFFB7F2BC), Color(0xFF002108)
)
private val ForestDarkRoles = AccentRoles(
    Color(0xFF9BD69F), Color(0xFF00390F), Color(0xFF15522A), Color(0xFFB7F2BC)
)

fun accentColorScheme(accent: Accent, dark: Boolean): ColorScheme {
    val base = if (dark) DarkSchemeBase else LightSchemeBase
    val roles = when (accent) {
        Accent.INDIGO, Accent.DYNAMIC -> return base
        Accent.TEAL -> if (dark) TealDarkRoles else TealLightRoles
        Accent.ROSE -> if (dark) RoseDarkRoles else RoseLightRoles
        Accent.AMBER -> if (dark) AmberDarkRoles else AmberLightRoles
        Accent.FOREST -> if (dark) ForestDarkRoles else ForestLightRoles
    }
    return base.copy(
        primary = roles.primary,
        onPrimary = roles.onPrimary,
        primaryContainer = roles.primaryContainer,
        onPrimaryContainer = roles.onPrimaryContainer,
        surfaceTint = roles.primary
    )
}

// ---------------------------------------------------------------------------
// Status colours. Meaning is always repeated with an icon or label, never by
// colour alone.
// ---------------------------------------------------------------------------

val SuccessGreenLight = Color(0xFF1F8A4C)
val SuccessGreenDark = Color(0xFF7BD99A)
val WarningAmberLight = Color(0xFFB26A00)
val WarningAmberDark = Color(0xFFFFC061)
val DangerRedLight = Color(0xFFBA1A1A)
val DangerRedDark = Color(0xFFFFB4AB)

fun priorityColor(priority: Priority, dark: Boolean): Color = when (priority) {
    Priority.HIGH -> if (dark) DangerRedDark else DangerRedLight
    Priority.MEDIUM -> if (dark) WarningAmberDark else WarningAmberLight
    Priority.LOW -> if (dark) SuccessGreenDark else SuccessGreenLight
}

fun successColor(dark: Boolean): Color = if (dark) SuccessGreenDark else SuccessGreenLight

fun warningColor(dark: Boolean): Color = if (dark) WarningAmberDark else WarningAmberLight

fun dangerColor(dark: Boolean): Color = if (dark) DangerRedDark else DangerRedLight

/**
 * Subject tag colours: a tinted container with text pushed toward the surface
 * colour so contrast stays readable on any of the eight hues.
 */
fun subjectContainerColor(argb: Int, dark: Boolean): Color {
    val base = Color(argb)
    return base.copy(alpha = if (dark) 0.24f else 0.14f)
}

fun subjectContentColor(argb: Int, dark: Boolean): Color {
    val base = Color(argb)
    return if (dark) lerp(base, Color.White, 0.55f) else lerp(base, Color.Black, 0.30f)
}
