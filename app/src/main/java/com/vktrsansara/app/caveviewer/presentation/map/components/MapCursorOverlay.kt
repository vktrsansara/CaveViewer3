package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Overlay rendering the central crosshair/cursor at the geometric center of the screen/map.
 * Transparent to pointer input gestures so map panning and zooming are completely unobstructed.
 */
@Composable
fun MapCursorOverlay(
    cursorShow: Boolean,
    cursorType: Int,
    cursorColor: Long,
    modifier: Modifier = Modifier
) {
    if (!cursorShow) return

    val color = Color(cursorColor)
    val shadowAlpha = 0.35f * color.alpha
    val shadowColor = Color.Black.copy(alpha = shadowAlpha)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(48.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            when (cursorType) {
                1 -> {
                    // Type 1: Solid Cross (+)
                    val lineHalfLength = 8.dp.toPx()
                    val strokeWidth = 1.5.dp.toPx()
                    val shadowStroke = strokeWidth + 1.dp.toPx()

                    if (shadowAlpha > 0f) {
                        drawLine(
                            color = shadowColor,
                            start = Offset(cx - lineHalfLength, cy),
                            end = Offset(cx + lineHalfLength, cy),
                            strokeWidth = shadowStroke,
                            cap = StrokeCap.Square
                        )
                        drawLine(
                            color = shadowColor,
                            start = Offset(cx, cy - lineHalfLength),
                            end = Offset(cx, cy + lineHalfLength),
                            strokeWidth = shadowStroke,
                            cap = StrokeCap.Square
                        )
                    }

                    drawLine(
                        color = color,
                        start = Offset(cx - lineHalfLength, cy),
                        end = Offset(cx + lineHalfLength, cy),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Square
                    )
                    drawLine(
                        color = color,
                        start = Offset(cx, cy - lineHalfLength),
                        end = Offset(cx, cy + lineHalfLength),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Square
                    )
                }

                2 -> {
                    // Type 2: Dashed Plus with center dot
                    val centerDotRadius = 2.dp.toPx()
                    val gap = 4.dp.toPx()
                    val tickLength = 6.dp.toPx()
                    val strokeWidth = 1.5.dp.toPx()

                    if (shadowAlpha > 0f) {
                        drawCircle(color = shadowColor, radius = centerDotRadius + 0.8f, center = Offset(cx, cy))
                    }
                    drawCircle(color = color, radius = centerDotRadius, center = Offset(cx, cy))

                    val ticks = listOf(
                        Pair(Offset(cx, cy - gap), Offset(cx, cy - gap - tickLength)),
                        Pair(Offset(cx, cy + gap), Offset(cx, cy + gap + tickLength)),
                        Pair(Offset(cx - gap, cy), Offset(cx - gap - tickLength, cy)),
                        Pair(Offset(cx + gap, cy), Offset(cx + gap + tickLength, cy))
                    )

                    ticks.forEach { (start, end) ->
                        if (shadowAlpha > 0f) {
                            drawLine(
                                color = shadowColor,
                                start = start,
                                end = end,
                                strokeWidth = strokeWidth + 1f,
                                cap = StrokeCap.Round
                            )
                        }
                        drawLine(
                            color = color,
                            start = start,
                            end = end,
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }

                3 -> {
                    // Type 3: Clean Center Dot
                    val dotRadius = 3.dp.toPx()

                    if (shadowAlpha > 0f) {
                        drawCircle(color = shadowColor, radius = dotRadius + 1f, center = Offset(cx, cy))
                    }
                    drawCircle(color = color, radius = dotRadius, center = Offset(cx, cy))
                }

                4 -> {
                    // Type 4: Diagonal X-cross with center dot
                    val centerDotRadius = 1.8.dp.toPx()
                    val gap = 4.dp.toPx()
                    val tickLength = 6.dp.toPx()
                    val strokeWidth = 1.5.dp.toPx()

                    if (shadowAlpha > 0f) {
                        drawCircle(color = shadowColor, radius = centerDotRadius + 0.8f, center = Offset(cx, cy))
                    }
                    drawCircle(color = color, radius = centerDotRadius, center = Offset(cx, cy))

                    val diagFactor = 0.7071f
                    val directions = listOf(
                        Pair(1f, 1f),
                        Pair(-1f, 1f),
                        Pair(1f, -1f),
                        Pair(-1f, -1f)
                    )

                    directions.forEach { (dx, dy) ->
                        val start = Offset(cx + dx * gap * diagFactor, cy + dy * gap * diagFactor)
                        val end = Offset(cx + dx * (gap + tickLength) * diagFactor, cy + dy * (gap + tickLength) * diagFactor)

                        if (shadowAlpha > 0f) {
                            drawLine(
                                color = shadowColor,
                                start = start,
                                end = end,
                                strokeWidth = strokeWidth + 1f,
                                cap = StrokeCap.Round
                            )
                        }
                        drawLine(
                            color = color,
                            start = start,
                            end = end,
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }

                5 -> {
                    // Type 5: Circle ring with center dot
                    val ringRadius = 7.dp.toPx()
                    val centerDotRadius = 2.dp.toPx()
                    val strokeWidth = 1.5.dp.toPx()

                    if (shadowAlpha > 0f) {
                        drawCircle(color = shadowColor, radius = centerDotRadius + 0.8f, center = Offset(cx, cy))
                        drawCircle(
                            color = shadowColor,
                            radius = ringRadius,
                            center = Offset(cx, cy),
                            style = Stroke(width = strokeWidth + 1f)
                        )
                    }
                    drawCircle(color = color, radius = centerDotRadius, center = Offset(cx, cy))
                    drawCircle(
                        color = color,
                        radius = ringRadius,
                        center = Offset(cx, cy),
                        style = Stroke(width = strokeWidth)
                    )
                }

                6 -> {
                    // Type 6: Circle Target with Crosshairs (Кружок с перекрестием)
                    val ringRadius = 6.5.dp.toPx()
                    val centerDotRadius = 1.8.dp.toPx()
                    val strokeWidth = 1.5.dp.toPx()
                    val crosshairStart = 7.5.dp.toPx()
                    val crosshairEnd = 11.5.dp.toPx()

                    if (shadowAlpha > 0f) {
                        drawCircle(color = shadowColor, radius = centerDotRadius + 0.8f, center = Offset(cx, cy))
                        drawCircle(
                            color = shadowColor,
                            radius = ringRadius,
                            center = Offset(cx, cy),
                            style = Stroke(width = strokeWidth + 1f)
                        )
                    }
                    drawCircle(color = color, radius = centerDotRadius, center = Offset(cx, cy))
                    drawCircle(
                        color = color,
                        radius = ringRadius,
                        center = Offset(cx, cy),
                        style = Stroke(width = strokeWidth)
                    )

                    val crosshairTicks = listOf(
                        Pair(Offset(cx, cy - crosshairStart), Offset(cx, cy - crosshairEnd)),
                        Pair(Offset(cx, cy + crosshairStart), Offset(cx, cy + crosshairEnd)),
                        Pair(Offset(cx - crosshairStart, cy), Offset(cx - crosshairEnd, cy)),
                        Pair(Offset(cx + crosshairStart, cy), Offset(cx + crosshairEnd, cy))
                    )

                    crosshairTicks.forEach { (start, end) ->
                        if (shadowAlpha > 0f) {
                            drawLine(
                                color = shadowColor,
                                start = start,
                                end = end,
                                strokeWidth = strokeWidth + 1f,
                                cap = StrokeCap.Round
                            )
                        }
                        drawLine(
                            color = color,
                            start = start,
                            end = end,
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}
