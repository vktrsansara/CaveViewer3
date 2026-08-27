package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.SquareFoot
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils
import com.vktrsansara.app.caveviewer.domain.model.ScaleBindingPoint
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

private val AreaFillColor = Color(0x338A2BE2) // 20% alpha BlueViolet
private val AreaStrokeColor = Color(0xFF8A2BE2) // BlueViolet
private val WarningAmber = Color(0xFFF59E0B)

/**
 * Overlay for Polygon Area & Perimeter measurement mode.
 */
@Composable
fun AreaMeasureOverlay(
    points: List<ScaleBindingPoint>,
    screenPoints: List<Offset>,
    currentCenterPx: Pair<Double, Double>?,
    ppm: Double,
    isActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Construct current active polygon image points (fixed + current center if active)
    val activePolygonPoints = remember(points, currentCenterPx, isActive) {
        val list = points.map { it.imagePx }.toMutableList()
        if (isActive && currentCenterPx != null) {
            list.add(currentCenterPx)
        }
        list
    }

    val areaPx = remember(activePolygonPoints) {
        MeasureUtils.calculatePolygonAreaPx(activePolygonPoints)
    }

    val perimeterPx = remember(activePolygonPoints) {
        MeasureUtils.calculatePolygonPerimeterPx(activePolygonPoints)
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Canvas rendering polygon fill, contour lines, dynamic closing lines, and vertices
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerScreen = Offset(size.width / 2f, size.height / 2f)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
            val strokeWidthPx = 2.5.dp.toPx()

            val pointCount = minOf(screenPoints.size, points.size)
            val validScreenPoints = if (pointCount > 0) screenPoints.take(pointCount) else emptyList()
            val allScreenPoints = if (validScreenPoints.isNotEmpty()) {
                if (isActive) validScreenPoints + centerScreen else validScreenPoints
            } else {
                emptyList()
            }

            if (allScreenPoints.size >= 3) {
                // 1. Draw polygon semi-transparent fill
                val fillPath = Path().apply {
                    moveTo(allScreenPoints[0].x, allScreenPoints[0].y)
                    for (i in 1 until allScreenPoints.size) {
                        lineTo(allScreenPoints[i].x, allScreenPoints[i].y)
                    }
                    close()
                }
                drawPath(
                    path = fillPath,
                    color = AreaFillColor,
                    style = Fill
                )
            }

            if (pointCount > 0) {
                // 2. Draw fixed segments between placed vertices
                for (i in 0 until pointCount - 1) {
                    drawLine(
                        color = AreaStrokeColor,
                        start = validScreenPoints[i],
                        end = validScreenPoints[i + 1],
                        strokeWidth = strokeWidthPx,
                        pathEffect = dashEffect
                    )
                }
                if (!isActive && pointCount >= 3) {
                    // Close fixed polygon
                    drawLine(
                        color = AreaStrokeColor,
                        start = validScreenPoints.last(),
                        end = validScreenPoints.first(),
                        strokeWidth = strokeWidthPx,
                        pathEffect = dashEffect
                    )
                }

                // 3. Draw dynamic lines to center cursor (only if active)
                if (isActive) {
                    // From last placed point to center
                    drawLine(
                        color = AreaStrokeColor,
                        start = validScreenPoints.last(),
                        end = centerScreen,
                        strokeWidth = strokeWidthPx,
                        pathEffect = dashEffect
                    )
                    // From center to first placed point (closing line preview)
                    if (pointCount >= 2) {
                        drawLine(
                            color = AreaStrokeColor.copy(alpha = 0.75f),
                            start = centerScreen,
                            end = validScreenPoints.first(),
                            strokeWidth = strokeWidthPx,
                            pathEffect = dashEffect
                        )
                    }
                }

                // 4. Draw vertices markers
                validScreenPoints.forEachIndexed { index, screenPt ->
                    if (index == 0) {
                        // First point: White fill with BlueViolet border
                        drawCircle(
                            color = Color.White,
                            radius = 6.dp.toPx(),
                            center = screenPt
                        )
                        drawCircle(
                            color = AreaStrokeColor,
                            radius = 6.dp.toPx(),
                            center = screenPt,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        drawCircle(
                            color = AreaStrokeColor,
                            radius = 2.dp.toPx(),
                            center = screenPt
                        )
                    } else {
                        // Subsequent vertices: BlueViolet fill with White border
                        drawCircle(
                            color = AreaStrokeColor,
                            radius = 5.5.dp.toPx(),
                            center = screenPt
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 5.5.dp.toPx(),
                            center = screenPt,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }
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
                    .border(1.dp, AppColors.borderColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
            if (activePolygonPoints.size < 3) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.SquareFoot,
                        contentDescription = "Площадь",
                        tint = AreaStrokeColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (points.isEmpty()) {
                            "Наведите курсор на 1-ю вершину и коснитесь экрана"
                        } else {
                            "Поставьте минимум 3 точки для многоугольника (точек: ${points.size})"
                        },
                        color = AppColors.textPrimary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                val areaStr = MeasureUtils.formatArea(areaPx, ppm)
                val perimStr = MeasureUtils.formatDistance(perimeterPx, ppm)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Площадь: $areaStr   •   Периметр: $perimStr",
                        color = AppColors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (ppm <= 0.0) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Карта без масштаба (px²)",
                            color = WarningAmber,
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
