package com.example.studenttaskmanager.ui.theme

import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape hierarchy from the PixelPlayer design report (§3.3): 8 / 16 / 24 with
 * a 32 dp tier for dialogs and sheets. The corners are continuous squircles
 * rather than plain arcs, which is where the softness comes from.
 */
val StudyDeckShapes = Shapes(
    extraSmall = ShapeCache.smooth8,
    small = ShapeCache.smooth8,
    medium = ShapeCache.smooth16,
    large = ShapeCache.smooth24,
    extraLarge = ShapeCache.smooth32
)

val CardShape = ShapeCache.smooth20
val ButtonShape = ShapeCache.smoothPill
val ChipShape = ShapeCache.smoothPill
val SheetShape = SmoothTopShape(32.dp)
