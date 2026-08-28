package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.domain.model.LineEnvironmentType
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Topographic vector tick and hatch renderer for cave passage environment types (UIS / Therion speleo standard).
 */
object LinePatternRenderer {

    /**
     * Draws vector topographic environment pattern/ticks along a sequence of projected screen points.
     */
    fun drawEnvironmentPattern(
        drawScope: DrawScope,
        screenPoints: List<Offset>,
        environmentType: LineEnvironmentType,
        patternColor: Color,
        lineWidthPx: Float
    ) {
        if (screenPoints.size < 2 || environmentType == LineEnvironmentType.NONE) return

        val stepPx = drawScope.run { 28.dp.toPx() }      // Distance between ticks along segment
        val tickSizePx = drawScope.run { 5.5.dp.toPx() } // Tick half-size / radius

        for (i in 0 until screenPoints.size - 1) {
            val p1 = screenPoints[i]
            val p2 = screenPoints[i + 1]
            val dx = p2.x - p1.x
            val dy = p2.y - p1.y
            val segmentLen = sqrt(dx * dx + dy * dy)
            if (segmentLen < 12f) continue

            val angle = atan2(dy, dx)
            val normalAngle = angle + (Math.PI / 2.0).toFloat()
            val cosNorm = cos(normalAngle)
            val sinNorm = sin(normalAngle)

            var currentDist = stepPx / 2f
            while (currentDist < segmentLen) {
                val t = currentDist / segmentLen
                val cx = p1.x + dx * t
                val cy = p1.y + dy * t

                when (environmentType) {
                    // 1. Водоток / Ручей: стрелочка по направлению течения хода >
                    LineEnvironmentType.WATER -> {
                        val arrowLen = tickSizePx * 1.15f
                        val arrowAngle = Math.toRadians(35.0).toFloat()
                        val path = Path().apply {
                            moveTo(
                                cx - arrowLen * cos(angle - arrowAngle),
                                cy - arrowLen * sin(angle - arrowAngle)
                            )
                            lineTo(cx, cy)
                            lineTo(
                                cx - arrowLen * cos(angle + arrowAngle),
                                cy - arrowLen * sin(angle + arrowAngle)
                            )
                        }
                        drawScope.drawPath(
                            path = path,
                            color = patternColor,
                            style = Stroke(width = lineWidthPx.coerceAtLeast(2.4f))
                        )
                    }

                    // 2. Завал / Глыбы: перпендикулярная засечка-гребенка ┼
                    LineEnvironmentType.BOULDER -> {
                        drawScope.drawLine(
                            color = patternColor,
                            start = Offset(cx - cosNorm * tickSizePx, cy - sinNorm * tickSizePx),
                            end = Offset(cx + cosNorm * tickSizePx, cy + sinNorm * tickSizePx),
                            strokeWidth = lineWidthPx.coerceAtLeast(2.2f)
                        )
                    }

                    // 3. Глина / Грязь: узловая точка-крапинка •
                    LineEnvironmentType.CLAY -> {
                        drawScope.drawCircle(
                            color = patternColor,
                            radius = tickSizePx * 0.65f,
                            center = Offset(cx, cy)
                        )
                    }

                    // 4. Сифон / Полусифон: двойной наклонный штрих //
                    LineEnvironmentType.SUMP -> {
                        val slashAngle = angle + Math.toRadians(60.0).toFloat()
                        val sx = cos(slashAngle) * tickSizePx
                        val sy = sin(slashAngle) * tickSizePx
                        val offsetDx = cos(angle) * (tickSizePx * 0.45f)
                        val offsetDy = sin(angle) * (tickSizePx * 0.45f)
                        drawScope.drawLine(
                            color = patternColor,
                            start = Offset(cx - sx - offsetDx, cy - sy - offsetDy),
                            end = Offset(cx + sx - offsetDx, cy + sy - offsetDy),
                            strokeWidth = lineWidthPx.coerceAtLeast(2.0f)
                        )
                        drawScope.drawLine(
                            color = patternColor,
                            start = Offset(cx - sx + offsetDx, cy - sy + offsetDy),
                            end = Offset(cx + sx + offsetDx, cy + sy + offsetDy),
                            strokeWidth = lineWidthPx.coerceAtLeast(2.0f)
                        )
                    }

                    // 5. Лед / Наледь: ромбик вдоль оси хода ◇
                    LineEnvironmentType.ICE -> {
                        val r = tickSizePx * 0.85f
                        val path = Path().apply {
                            moveTo(cx - cos(angle) * r, cy - sin(angle) * r)
                            lineTo(cx - cosNorm * r, cy - sinNorm * r)
                            lineTo(cx + cos(angle) * r, cy + sin(angle) * r)
                            lineTo(cx + cosNorm * r, cy + sinNorm * r)
                            close()
                        }
                        drawScope.drawPath(
                            path = path,
                            color = patternColor,
                            style = Stroke(width = 1.8f)
                        )
                    }

                    // 6. Загазованность (CO₂): тройные косые насечки ///
                    LineEnvironmentType.GAS -> {
                        val slashAngle = angle + Math.toRadians(60.0).toFloat()
                        val sx = cos(slashAngle) * tickSizePx
                        val sy = sin(slashAngle) * tickSizePx
                        val spacing = tickSizePx * 0.5f
                        for (k in -1..1) {
                            val offX = cos(angle) * (k * spacing)
                            val offY = sin(angle) * (k * spacing)
                            drawScope.drawLine(
                                color = patternColor,
                                start = Offset(cx - sx + offX, cy - sy + offY),
                                end = Offset(cx + sx + offX, cy + sy + offY),
                                strokeWidth = lineWidthPx.coerceAtLeast(1.8f)
                            )
                        }
                    }

                    // 7. Свой цвет / Пользовательский
                    LineEnvironmentType.CUSTOM -> {
                        drawScope.drawCircle(
                            color = patternColor,
                            radius = tickSizePx * 0.55f,
                            center = Offset(cx, cy)
                        )
                    }

                    LineEnvironmentType.NONE -> {}
                }

                currentDist += stepPx
            }
        }
    }
}
