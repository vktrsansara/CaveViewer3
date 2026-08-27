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
import androidx.compose.material.icons.rounded.Architecture
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

private val AmberAngleColor = Color(0xFFF59E0B) // Amber
private val VertexRedColor = Color(0xFFEF4444)  // Red

/**
 * Visual canvas overlay for Angle measurement mode:
 * - Point 1: Base line start
 * - Point 2: Angle vertex (stays fixed after 2nd tap)
 * - Live ray continuously stretches from Vertex (Point 2) to center screen cursor
 */
@Composable
fun AngleMeasureOverlay(
    points: List<ScaleBindingPoint>,
    screenPoints: List<Offset>,
    currentCenterPx: Pair<Double, Double>?,
    ppm: Double,
    isActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Dynamic angle and ray lengths
    val liveAngle: Double? = remember(points, currentCenterPx, isActive) {
        if (isActive && points.size >= 2 && currentCenterPx != null) {
            MeasureUtils.calculateAngleDegrees(points[0].imagePx, points[1].imagePx, currentCenterPx)
        } else {
            null
        }
    }

    val baseDistancePx = remember(points) {
        if (points.size >= 2) {
            MeasureUtils.distancePx(points[0].imagePx, points[1].imagePx)
        } else {
            0.0
        }
    }

    val cursorDistancePx = remember(points, currentCenterPx, isActive) {
        if (isActive && points.size >= 2 && currentCenterPx != null) {
            MeasureUtils.distancePx(points[1].imagePx, currentCenterPx)
        } else {
            0.0
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerScreen = Offset(size.width / 2f, size.height / 2f)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            val strokeWidthPx = 2.5.dp.toPx()

            val pointCount = minOf(screenPoints.size, points.size)
            val validScreenPoints = if (pointCount > 0) screenPoints.take(pointCount) else emptyList()

            if (pointCount >= 1) {
                val p1 = validScreenPoints[0]

                // Line 1: From Point 1 to Point 2 (or to center if Point 2 not placed yet and active)
                if (pointCount >= 2) {
                    val p2 = validScreenPoints[1]
                    drawLine(
                        color = AmberAngleColor,
                        start = p1,
                        end = p2,
                        strokeWidth = strokeWidthPx
                    )
                } else if (isActive) {
                    drawLine(
                        color = AmberAngleColor,
                        start = p1,
                        end = centerScreen,
                        strokeWidth = strokeWidthPx,
                        pathEffect = dashEffect
                    )
                }

                // Line 2: From Vertex (Point 2) to center screen cursor (only if active)
                if (isActive && pointCount >= 2) {
                    val p2 = validScreenPoints[1]
                    drawLine(
                        color = AmberAngleColor,
                        start = p2,
                        end = centerScreen,
                        strokeWidth = strokeWidthPx,
                        pathEffect = dashEffect
                    )

                    // Arc indicator around vertex
                    val arcRadius = 24.dp.toPx()
                    drawCircle(
                        color = AmberAngleColor.copy(alpha = 0.18f),
                        radius = arcRadius,
                        center = p2
                    )
                    drawCircle(
                        color = AmberAngleColor,
                        radius = arcRadius,
                        center = p2,
                        style = Stroke(width = 1.5.dp.toPx(), pathEffect = dashEffect)
                    )
                }

                // Point markers
                validScreenPoints.forEachIndexed { index, pt ->
                    val isVertex = index == 1
                    drawCircle(
                        color = if (isVertex) VertexRedColor else AmberAngleColor,
                        radius = if (isVertex) 7.5.dp.toPx() else 5.5.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = if (isVertex) 7.5.dp.toPx() else 5.5.dp.toPx(),
                        center = pt,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
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
                    .border(1.dp, AmberAngleColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
            when (points.size) {
                0 -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Architecture,
                            contentDescription = "Угол",
                            tint = AmberAngleColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Наведите курсор на начало базовой линии (Точка 1)",
                            color = AppColors.textPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                1 -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Architecture,
                            contentDescription = "Угол",
                            tint = AmberAngleColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Наведите курсор на вершину угла (Точка 2)",
                            color = AppColors.textPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                else -> {
                    val angleStr = String.format(Locale.US, "%.2f°", liveAngle ?: 0.0)
                    val baseLenStr = MeasureUtils.formatDistance(baseDistancePx, ppm)
                    val curLenStr = MeasureUtils.formatDistance(cursorDistancePx, ppm)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Угол: $angleStr   •   База: $baseLenStr   •   Луч: $curLenStr",
                            color = AppColors.textPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (ppm <= 0.0) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Карта без масштаба (px)",
                                color = AmberAngleColor,
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
