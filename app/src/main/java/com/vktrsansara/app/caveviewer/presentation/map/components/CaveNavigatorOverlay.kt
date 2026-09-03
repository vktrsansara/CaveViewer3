package com.vktrsansara.app.caveviewer.presentation.map.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.domain.measure.LineColorUtils
import com.vktrsansara.app.caveviewer.domain.model.CaveRoute
import com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds
import org.maplibre.android.geometry.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Overlay rendering the active Cave Passage Route (A*), animated direction arrows,
 * alternative dashed path, and waypoint markers (1, 2, 3...).
 * Active route renders passage difficulty heat colors; inactive route renders dashed line.
 */
@Composable
fun CaveNavigatorOverlay(
    waypoints: List<Pair<Double, Double>>,
    primaryRoute: CaveRoute?,
    alternativeRoute: CaveRoute?,
    isAlternativeActive: Boolean = false,
    imageWidth: Int,
    imageHeight: Int,
    zoomMax: Int,
    projector: (((LatLng) -> Offset)?),
    currentTargetLat: Double,
    currentTargetLon: Double,
    currentZoom: Double,
    mapBearing: Double,
    modifier: Modifier = Modifier
) {
    if (projector == null || imageWidth <= 0 || imageHeight <= 0 || zoomMax <= 0) return

    val infiniteTransition = rememberInfiniteTransition(label = "CaveRouteFlow")
    val flowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RouteFlowPhase"
    )

    val activeRoute = if (isAlternativeActive) alternativeRoute ?: primaryRoute else primaryRoute
    val inactiveRoute = if (isAlternativeActive) primaryRoute else alternativeRoute

    // Преобразуем точки растра в экранные координаты
    val screenWaypoints = remember(waypoints, projector, currentTargetLat, currentTargetLon, currentZoom, mapBearing) {
        waypoints.mapNotNull { (x, y) ->
            val ll = CaveMapBounds.imagePixelsToLatLng(x, y, imageWidth, imageHeight, zoomMax)
            val sp = projector.invoke(ll)
            if (sp.x.isFinite() && sp.y.isFinite()) sp else null
        }
    }

    val activeScreenPoints = remember(activeRoute, projector, currentTargetLat, currentTargetLon, currentZoom, mapBearing) {
        activeRoute?.points?.mapNotNull { (x, y) ->
            val ll = CaveMapBounds.imagePixelsToLatLng(x, y, imageWidth, imageHeight, zoomMax)
            val sp = projector.invoke(ll)
            if (sp.x.isFinite() && sp.y.isFinite()) sp else null
        } ?: emptyList()
    }

    val inactiveScreenPoints = remember(inactiveRoute, projector, currentTargetLat, currentTargetLon, currentZoom, mapBearing) {
        inactiveRoute?.points?.mapNotNull { (x, y) ->
            val ll = CaveMapBounds.imagePixelsToLatLng(x, y, imageWidth, imageHeight, zoomMax)
            val sp = projector.invoke(ll)
            if (sp.x.isFinite() && sp.y.isFinite()) sp else null
        } ?: emptyList()
    }

    // Сегменты активного маршрута со своими цветами сложности
    val activeScreenSegments = remember(activeRoute, projector, currentTargetLat, currentTargetLon, currentZoom, mapBearing) {
        if (activeRoute == null) return@remember emptyList<ScreenSegment>()
        if (activeRoute.segments.isNotEmpty()) {
            activeRoute.segments.mapNotNull { seg ->
                val pts = seg.points.mapNotNull { (x, y) ->
                    val ll = CaveMapBounds.imagePixelsToLatLng(x, y, imageWidth, imageHeight, zoomMax)
                    val sp = projector.invoke(ll)
                    if (sp.x.isFinite() && sp.y.isFinite()) sp else null
                }
                if (pts.size >= 2) {
                    ScreenSegment(pts, LineColorUtils.getDifficultyColor(seg.difficulty))
                } else null
            }
        } else if (activeScreenPoints.size >= 2) {
            listOf(ScreenSegment(activeScreenPoints, LineColorUtils.getDifficultyColor(activeRoute.averageDifficulty)))
        } else {
            emptyList()
        }
    }

    val textPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        // 1. Неактивный маршрут (пунктирная оранжевая линия 3.5 dp)
        if (inactiveScreenPoints.size >= 2) {
            val altPath = Path().apply {
                moveTo(inactiveScreenPoints.first().x, inactiveScreenPoints.first().y)
                for (i in 1 until inactiveScreenPoints.size) {
                    lineTo(inactiveScreenPoints[i].x, inactiveScreenPoints[i].y)
                }
            }
            drawPath(
                path = altPath,
                color = Color(0xFFF59E0B),
                style = Stroke(
                    width = 3.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 16f), 0f),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // 2. Активный маршрут (тепловая линия сложности по отрезкам + анимированные шевроны)
        if (activeScreenSegments.isNotEmpty()) {
            val glowWidth = 9.dp.toPx()
            val coreWidth = 5.dp.toPx()

            // 1-й проход: внешний ореол
            for (seg in activeScreenSegments) {
                val segPath = Path().apply {
                    moveTo(seg.points.first().x, seg.points.first().y)
                    for (i in 1 until seg.points.size) {
                        lineTo(seg.points[i].x, seg.points[i].y)
                    }
                }
                drawPath(
                    path = segPath,
                    color = seg.color.copy(alpha = 0.35f),
                    style = Stroke(
                        width = glowWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // 2-й проход: ядро линии цвета сложности хода
            for (seg in activeScreenSegments) {
                val segPath = Path().apply {
                    moveTo(seg.points.first().x, seg.points.first().y)
                    for (i in 1 until seg.points.size) {
                        lineTo(seg.points[i].x, seg.points[i].y)
                    }
                }
                drawPath(
                    path = segPath,
                    color = seg.color,
                    style = Stroke(
                        width = coreWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // 3-й проход: анимированные шевроны направления движения
            if (activeScreenPoints.size >= 2) {
                drawFlowArrows(activeScreenPoints, flowPhase)
            }
        }

        // 3. Маркеры точек маршрута (1, 2, 3...)
        for (i in screenWaypoints.indices) {
            val center = screenWaypoints[i]
            val color = when {
                i == 0 -> Color(0xFF10B981) // Точка 1: Зеленый (Старт)
                i == screenWaypoints.size - 1 && screenWaypoints.size >= 2 -> Color(0xFFEF4444) // Последняя точка: Красный (Финиш)
                else -> Color(0xFFF59E0B) // Промежуточные точки: Оранжевый с темным центром
            }
            drawRouteMarker(
                center = center,
                color = color,
                number = "${i + 1}",
                textPaint = textPaint
            )
        }
    }
}

private data class ScreenSegment(
    val points: List<Offset>,
    val color: Color
)

private fun DrawScope.drawRouteMarker(
    center: Offset,
    color: Color,
    number: String,
    textPaint: Paint
) {
    val radius = 9.dp.toPx()
    val outerRingRadius = 12.dp.toPx()

    // Внешнее полупрозрачное кольцо
    drawCircle(
        color = color.copy(alpha = 0.28f),
        radius = outerRingRadius,
        center = center
    )

    // Темная подложка
    drawCircle(
        color = Color(0xFF1E293B),
        radius = radius,
        center = center
    )

    // Цветная окантовка маркера
    drawCircle(
        color = color,
        radius = radius,
        style = Stroke(width = 2.dp.toPx()),
        center = center
    )

    // Номер по центру
    val fontMetrics = textPaint.fontMetrics
    val yOffset = (fontMetrics.descent + fontMetrics.ascent) / 2f
    drawContext.canvas.nativeCanvas.drawText(
        number,
        center.x,
        center.y - yOffset,
        textPaint
    )
}

private fun DrawScope.drawFlowArrows(
    points: List<Offset>,
    flowPhase: Float
) {
    val arrowSpacingPx = 48.dp.toPx()
    val arrowSizePx = 7.dp.toPx()
    val arrowColor = Color.White.copy(alpha = 0.95f)

    var totalLen = 0f
    for (i in 0 until points.size - 1) {
        totalLen += hypot(points[i + 1].x - points[i].x, points[i + 1].y - points[i].y)
    }
    if (totalLen < 15f) return

    val startOffset = flowPhase * arrowSpacingPx

    var accumulatedDist = 0f
    var currSegmentIdx = 0
    var distAlongSegment = 0f

    var nextArrowDist = startOffset
    while (nextArrowDist < totalLen) {
        while (currSegmentIdx < points.size - 1) {
            val p1 = points[currSegmentIdx]
            val p2 = points[currSegmentIdx + 1]
            val segLen = hypot(p2.x - p1.x, p2.y - p1.y)

            if (accumulatedDist + segLen >= nextArrowDist) {
                distAlongSegment = nextArrowDist - accumulatedDist
                val t = if (segLen > 0f) distAlongSegment / segLen else 0f
                val ax = p1.x + t * (p2.x - p1.x)
                val ay = p1.y + t * (p2.y - p1.y)
                val angle = atan2(p2.y - p1.y, p2.x - p1.x)

                val leftWingX = ax - arrowSizePx * cos(angle - 0.55f)
                val leftWingY = ay - arrowSizePx * sin(angle - 0.55f)
                val rightWingX = ax - arrowSizePx * cos(angle + 0.55f)
                val rightWingY = ay - arrowSizePx * sin(angle + 0.55f)

                drawLine(
                    color = arrowColor,
                    start = Offset(leftWingX, leftWingY),
                    end = Offset(ax, ay),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = arrowColor,
                    start = Offset(rightWingX, rightWingY),
                    end = Offset(ax, ay),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                break
            } else {
                accumulatedDist += segLen
                currSegmentIdx++
            }
        }
        nextArrowDist += arrowSpacingPx
    }
}
