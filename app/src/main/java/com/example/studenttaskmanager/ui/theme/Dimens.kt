package com.example.studenttaskmanager.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Spacing and sizing tokens (Design Brief section 4.5).
 */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

val ScreenPadding = 16.dp
val CardPadding = 16.dp
val CardGap = 8.dp
val MinTouchTarget = 48.dp
val FabSize = 56.dp
val BottomBarHeight = 64.dp

/**
 * Space reserved at the end of scrollable tab content so the floating bottom
 * bar and the add-task FAB never cover the last row. Generous enough for
 * three-button navigation, where the system bar adds another 48dp.
 */
val BottomContentPadding = 168.dp
