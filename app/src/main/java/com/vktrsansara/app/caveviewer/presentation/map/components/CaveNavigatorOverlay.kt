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
import com.vktrsansara.app.caveviewer.domain.model.CaveRoute
import com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds
import org.maplibre.android.geometry.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Overlay rendering the active Cave Passage Route (A*), animated direction arrows,
 * alternative dashed path, and start/finish markers (🟢 Point A, 🔴 Point B).
 */
@Composable
fun CaveNavigatorOverlay(
    startPoint: Pair<Double, Double>?,
    endPoint: Pair<Double, Double>?,
    primaryRoute: CaveRoute?,
    alternativeRoute: CaveRoute?,
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

    // Преобразуем точки растра в экранные координаты
    val startScreen = remember(startPoint, projector, currentTargetLat, currentTargetLon, currentZoom, mapBearing) {
        startPoint?.let { (x, y) ->
            val ll = CaveMapBounds.imagePixelsToLatLng(x, y, imageWidth, imageHeight, zoomMax)
            val sp = projector.invoke(ll)
            if (sp.x.isFinite() && sp.y.isFinite()) sp else null
        }
    }

    val endScreen = remember(endPoint, projector, currentTargetLat, currentTargetLon, currentZoom, mapBearing) {
        endPoint?.let { (x, y) ->
            val ll = CaveMapBounds.imagePixelsToLatLng(x, y, imageWidth, imageHeight, zoomMax)
            val sp = projector.invoke(ll)
            if (sp.x.isFinite() && sp.y.isFinite()) sp else null
        }
    }

    val primaryScreenPoints = remember(primaryRoute, projector, currentTargetLat, currentTargetLon, currentZoom, mapBearing) {
        primaryRoute?.points?.mapNotNull { (x, y) ->
            val ll = CaveMapBounds.imagePixelsToLatLng(x, y, imageWidth, imageHeight, zoomMax)
            val sp = projector.invoke(ll)
            if (sp.x.isFinite() && sp.y.isFinite()) sp else null
        } ?: emptyList()
    }

    val altScreenPoints = remember(alternativeRoute, projector, currentTargetLat, currentTargetLon, currentZoom, mapBearing) {
        alternativeRoute?.points?.mapNotNull { (x, y) ->
            val ll = CaveMapBounds.imagePixelsToLatLng(x, y, imageWidth, imageHeight, zoomMax)
            val sp = projector.invoke(ll)
            if (sp.x.isFinite() && sp.y.isFinite()) sp else null
        } ?: emptyList()
    }

    val textPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textSize = 34f
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
        // 1. Альтернативный маршрут (пунктирная оранжевая линия 3.5 dp)
        if (altScreenPoints.size >= 2) {
            val altPath = Path().apply {
                moveTo(altScreenPoints.first().x, altScreenPoints.first().y)
                for (i in 1 until altScreenPoints.size) {
                    lineTo(altScreenPoints[i].x, altScreenPoints[i].y)
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

        // 2. Основной маршрут (неоново-бирюзовая светящаяся линия 5 dp)
        if (primaryScreenPoints.size >= 2) {
            val routePath = Path().apply {
                moveTo(primaryScreenPoints.first().x, primaryScreenPoints.first().y)
                for (i in 1 until primaryScreenPoints.size) {
                    lineTo(primaryScreenPoints[i].x, primaryScreenPoints[i].y)
                }
            }

            // Внешнее неоновое свечение (9 dp)
            drawPath(
                path = routePath,
                color = Color(0xFF06B6D4).copy(alpha = 0.35f),
                style = Stroke(
                    width = 9.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Основная линия маршрута (5 dp)
            drawPath(
                path = routePath,
                color = Color(0xFF06B6D4),
                style = Stroke(
                    width = 5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Анимированные шевроны/стрелки направления
            drawFlowArrows(primaryScreenPoints, flowPhase)
        }

        // 3. Точка А (Старт 🟢)
        if (startScreen != null) {
            drawRouteMarker(
                center = startScreen,
                color = Color(0xFF10B981), // Emerald Green
                letter = "A",
                textPaint = textPaint
            )
        }

        // 4. Точка Б (Финиш 🔴)
        if (endScreen != null) {
            drawRouteMarker(
                center = endScreen,
                color = Color(0xFFEF4444), // Coral Red
                letter = "Б",
                textPaint = textPaint
            )
        }
    }
}

private fun DrawScope.drawRouteMarker(
    center: Offset,
    color: Color,
    letter: String,
    textPaint: Paint
) {
    val radiusPx = 15.dp.toPx()

    // Внешняя тень/свечение
    drawCircle(
        color = Color.Black.copy(alpha = 0.35f),
        radius = radiusPx + 3.dp.toPx(),
        center = center
    )

    // Белая кайма
    drawCircle(
        color = Color.White,
        radius = radiusPx,
        center = center
    )

    // Основной цветной круг
    drawCircle(
        color = color,
        radius = radiusPx - 2.dp.toPx(),
        center = center
    )

    // Буква по центру
    val fontMetrics = textPaint.fontMetrics
    val yOffset = (fontMetrics.descent + fontMetrics.ascent) / 2f
    drawContext.canvas.nativeCanvas.drawText(
        letter,
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

    // Расчет полной длины ломаной на экране
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
        // Находим сегмент для nextArrowDist
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

                // Рисуем стрелку-шеврон >
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
