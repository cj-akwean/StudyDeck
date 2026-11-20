package com.example.studenttaskmanager.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * iOS-style continuous corner (squircle).
 *
 * A circular corner uses cubic control points at 0.5523 × radius along each
 * tangent. Pushing those control points further out makes the curvature
 * continuous instead of snapping from straight line to circle, which is what
 * gives the reference UI its soft, "pebble" feel.
 *
 * [smoothness] 0 = plain rounded corner, 1 = very square. The design system
 * uses 0.6. Extending [CornerBasedShape] keeps the shape compatible with
 * `MaterialTheme.shapes` and component defaults.
 */
class SmoothCornerShape(
    topStart: CornerSize = CornerSize(0.dp),
    topEnd: CornerSize = topStart,
    bottomEnd: CornerSize = topStart,
    bottomStart: CornerSize = topStart,
    private val smoothness: Float = DEFAULT_SMOOTHNESS
) : CornerBasedShape(topStart, topEnd, bottomEnd, bottomStart) {

    constructor(radius: Dp, smoothness: Float = DEFAULT_SMOOTHNESS) : this(
        CornerSize(radius), CornerSize(radius), CornerSize(radius), CornerSize(radius), smoothness
    )

    override fun createOutline(
        size: Size,
        topStart: Float,
        topEnd: Float,
        bottomEnd: Float,
        bottomStart: Float,
        layoutDirection: LayoutDirection
    ): Outline {
        val limit = min(size.width, size.height) / 2f
        val tl = topStart.coerceIn(0f, limit)
        val tr = topEnd.coerceIn(0f, limit)
        val br = bottomEnd.coerceIn(0f, limit)
        val bl = bottomStart.coerceIn(0f, limit)

        if (tl < 0.5f && tr < 0.5f && br < 0.5f && bl < 0.5f) {
            return Outline.Rectangle(Rect(Offset.Zero, size))
        }

        // 0.5523 is the circular-arc constant; extra smoothing pushes outward.
        val f = 0.5523f + 0.30f * smoothness.coerceIn(0f, 1f)
        val ktl = f * tl
        val ktr = f * tr
        val kbr = f * br
        val kbl = f * bl

        val w = size.width
        val h = size.height
        val path = Path()

        path.moveTo(tl, 0f)
        // Top edge → top-right corner.
        path.lineTo(w - tr, 0f)
        path.cubicTo(w - tr + ktr, 0f, w, tr - ktr, w, tr)
        // Right edge → bottom-right corner.
        path.lineTo(w, h - br)
        path.cubicTo(w, h - br + kbr, w - br + kbr, h, w - br, h)
        // Bottom edge → bottom-left corner.
        path.lineTo(bl, h)
        path.cubicTo(bl - kbl, h, 0f, h - bl + kbl, 0f, h - bl)
        // Left edge → top-left corner.
        path.lineTo(0f, tl)
        path.cubicTo(0f, tl - ktl, tl - ktl, 0f, tl, 0f)
        path.close()

        return Outline.Generic(path)
    }

    override fun copy(
        topStart: CornerSize,
        topEnd: CornerSize,
        bottomEnd: CornerSize,
        bottomStart: CornerSize
    ): CornerBasedShape = SmoothCornerShape(topStart, topEnd, bottomEnd, bottomStart, smoothness)

    override fun toString(): String =
        "SmoothCornerShape($topStart, $topEnd, $bottomEnd, $bottomStart, smoothness=$smoothness)"

    companion object {
        const val DEFAULT_SMOOTHNESS = 0.6f
    }
}

/** Density-resolved variant for code that already knows its pixel radius. */
class AbsoluteSmoothCornerShape(
    private val radiusPx: Float,
    private val smoothness: Float = SmoothCornerShape.DEFAULT_SMOOTHNESS
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline = SmoothCornerShape(
        CornerSize(with(density) { radiusPx.toDp() }),
        smoothness = smoothness
    ).createOutline(size, layoutDirection, density)
}

/** Rounded top corners only — used for bottom sheets. */
fun SmoothTopShape(
    radius: Dp,
    smoothness: Float = SmoothCornerShape.DEFAULT_SMOOTHNESS
): CornerBasedShape = SmoothCornerShape(
    topStart = CornerSize(radius),
    topEnd = CornerSize(radius),
    bottomEnd = CornerSize(0.dp),
    bottomStart = CornerSize(0.dp),
    smoothness = smoothness
)

/**
 * The radii the UI actually uses. Creating a shape instance is cheap, but the
 * Outline path is not: keeping one instance per radius lets Compose reuse its
 * cached outline across every list row.
 */
object ShapeCache {
    val smooth8 = SmoothCornerShape(8.dp)
    val smooth10 = SmoothCornerShape(10.dp)
    val smooth12 = SmoothCornerShape(12.dp)
    val smooth14 = SmoothCornerShape(14.dp)
    val smooth16 = SmoothCornerShape(16.dp)
    val smooth20 = SmoothCornerShape(20.dp)
    val smooth24 = SmoothCornerShape(24.dp)
    val smooth28 = SmoothCornerShape(28.dp)
    val smooth32 = SmoothCornerShape(32.dp)
    val smoothPill = SmoothCornerShape(50.dp)

    fun of(radius: Dp) = when (radius) {
        8.dp -> smooth8
        10.dp -> smooth10
        12.dp -> smooth12
        14.dp -> smooth14
        16.dp -> smooth16
        20.dp -> smooth20
        24.dp -> smooth24
        28.dp -> smooth28
        32.dp -> smooth32
        else -> SmoothCornerShape(radius)
    }
}
