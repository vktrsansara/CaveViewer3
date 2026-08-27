package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils
import com.vktrsansara.app.caveviewer.domain.model.ScaleBindingPoint
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import java.util.Locale

private val PinkFaultColor = Color(0xFFEC4899)
private val AmberWarningColor = Color(0xFFF59E0B)

/**
 * Visual canvas overlay for Fault Line / Tectonic Axis tool:
 * - 2 Reference points defining fracture orientation
 * - Infinitely extended dashed line through map bounds
 * - Dual strike azimuth (e.g., 42.50° ⇄ 222.50°) with angle_north correction
 * - Reference baseline distance
 */
@Composable
fun FaultLineOverlay(
    points: List<ScaleBindingPoint>,
    screenPoints: List<Offset>,
    infiniteEndPoints: Pair<Offset, Offset>?,
    currentCenterPx: Pair<Double, Double>?,
    angleNorth: Double,
    ppm: Double,
    isActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    val strikeAzimuth = remember(points, currentCenterPx, angleNorth, isActive) {
        if (points.size >= 2) {
            MeasureUtils.calculateAzimuthDegrees(points[0].imagePx, points[1].imagePx, angleNorth)
        } else if (isActive && points.size == 1 && currentCenterPx != null) {
            MeasureUtils.calculateAzimuthDegrees(points[0].imagePx, currentCenterPx, angleNorth)
        } else {
            0.0
        }
    }

    val oppositeAzimuth = remember(strikeAzimuth) {
        MeasureUtils.calculateBackAzimuth(strikeAzimuth)
    }

    val baseDistancePx = remember(points, currentCenterPx, isActive) {
        if (points.size >= 2) {
            MeasureUtils.distancePx(points[0].imagePx, points[1].imagePx)
        } else if (isActive && points.size == 1 && currentCenterPx != null) {
            MeasureUtils.distancePx(points[0].imagePx, currentCenterPx)
        } else {
            0.0
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerScreen = Offset(size.width / 2f, size.height / 2f)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
            val pointCount = minOf(screenPoints.size, points.size)
            val validScreenPoints = if (pointCount > 0) screenPoints.take(pointCount) else emptyList()

            // 1. When 1 point is set and active, draw dynamic ray to center screen
            if (isActive && pointCount == 1) {
                drawLine(
                    color = PinkFaultColor,
                    start = validScreenPoints[0],
                    end = centerScreen,
                    strokeWidth = 2.5.dp.toPx(),
                    pathEffect = dashEffect
                )
            }

            // 2. When 2 points are set, draw infinite dashed line across the map
            if (pointCount >= 2) {
                val startPt: Offset
                val endPt: Offset
                if (infiniteEndPoints != null) {
                    startPt = infiniteEndPoints.first
                    endPt = infiniteEndPoints.second
                } else {
                    // Calculate intersection with screen bounds
                    val p1 = validScreenPoints[0]
                    val p2 = validScreenPoints[1]
                    val bounds = MeasureUtils.calculateInfiniteLineBounds(
                        p1 = Pair(p1.x.toDouble(), p1.y.toDouble()),
                        p2 = Pair(p2.x.toDouble(), p2.y.toDouble()),
                        mapWidth = size.width.toDouble(),
                        mapHeight = size.height.toDouble()
                    )
                    startPt = Offset(bounds.first.first.toFloat(), bounds.first.second.toFloat())
                    endPt = Offset(bounds.second.first.toFloat(), bounds.second.second.toFloat())
                }

                drawLine(
                    color = PinkFaultColor,
                    start = startPt,
                    end = endPt,
                    strokeWidth = 2.5.dp.toPx(),
                    pathEffect = dashEffect
                )
            }

            // 3. Markers for reference points
            validScreenPoints.forEach { pt ->
                drawCircle(color = PinkFaultColor, radius = 7.dp.toPx(), center = pt)
                drawCircle(
                    color = Color.White,
                    radius = 7.dp.toPx(),
                    center = pt,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = pt)
            }
        }

        // Top Info Banner (only if active)
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp)
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.bgCard.copy(alpha = 0.95f))
                    .border(1.dp, PinkFaultColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
            when (points.size) {
                0 -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Timeline,
                            contentDescription = "Ось разломов",
                            tint = PinkFaultColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Наведите курсор на начало трещины (Точка 1)",
                            color = AppColors.textPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                1 -> {
                    val azStr = String.format(Locale.US, "%.2f°", strikeAzimuth)
                    val oppStr = String.format(Locale.US, "%.2f°", oppositeAzimuth)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Наведите курсор вдоль трещины (Точка 2)",
                            color = AppColors.textPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Азимут: $azStr ⇄ $oppStr",
                            color = PinkFaultColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                else -> {
                    val azStr = String.format(Locale.US, "%.2f°", strikeAzimuth)
                    val oppStr = String.format(Locale.US, "%.2f°", oppositeAzimuth)
                    val baseLenStr = MeasureUtils.formatDistance(baseDistancePx, ppm)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Ось разлома: $azStr ⇄ $oppStr",
                            color = AppColors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Опорная база: $baseLenStr",
                            color = PinkFaultColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        if (ppm <= 0.0) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Карта без масштаба (px)",
                                color = AmberWarningColor,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
}
