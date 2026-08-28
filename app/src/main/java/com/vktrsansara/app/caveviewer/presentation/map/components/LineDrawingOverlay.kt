package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.LineLayer
import com.vktrsansara.app.caveviewer.domain.model.ScaleBindingPoint
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import java.util.Locale
import kotlin.math.sqrt

/**
 * Visual canvas overlay for interactive line drawing mode:
 * - Top informative banner with real-time length and vertex count
 * - Fixed segments between accumulated vertices
 * - Dynamic dashed ray from the last vertex to current center cursor
 * - Distinct vertex circular markers (5.dp radius)
 */
@Composable
fun LineDrawingOverlay(
    layer: LineLayer,
    points: List<ScaleBindingPoint>,
    screenPoints: List<Offset>,
    currentCenterPx: Pair<Double, Double>?,
    ppm: Double,
    modifier: Modifier = Modifier
) {
    // 1. Calculate live cumulative distance in map image pixels
    var segmentsPx = 0.0
    for (i in 0 until points.size - 1) {
        val p1 = points[i].imagePx
        val p2 = points[i + 1].imagePx
        val dx = p2.first - p1.first
        val dy = p2.second - p1.second
        segmentsPx += sqrt(dx * dx + dy * dy)
    }

    val liveRayPx = if (points.isNotEmpty() && currentCenterPx != null) {
        val last = points.last().imagePx
        val dx = currentCenterPx.first - last.first
        val dy = currentCenterPx.second - last.second
        val dist = sqrt(dx * dx + dy * dy)
        if (dist >= 1.0) dist else 0.0
    } else {
        0.0
    }

    val totalPx = segmentsPx + liveRayPx
    val totalVertices = points.size + (if (liveRayPx > 0.0 && points.isNotEmpty()) 1 else 0)

    val infoText = if (points.isEmpty()) {
        "Рисование линии: ${layer.name} • Нажмите [+] для установки первой вершины"
    } else {
        if (ppm > 0.0) {
            val meters = totalPx / ppm
            "Рисование линии: ${layer.name} • Длина: ${String.format(Locale.US, "%.2f", meters)} м ($totalVertices верш.)"
        } else {
            "Рисование линии: ${layer.name} • Длина: ${String.format(Locale.US, "%.1f", totalPx)} px ($totalVertices верш.) • Без масштаба (px)"
        }
    }

    val layerColor = if (layer.isHeatmapEnabled) Color(0xFF10B981) else Color(layer.defaultColor.toInt())

    Box(modifier = modifier.fillMaxSize().clipToBounds()) {
        // Canvas rendering lines and markers
        Canvas(modifier = Modifier.fillMaxSize()) {
            clipRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height
            ) {
                val centerScreen = Offset(size.width / 2f, size.height / 2f)
                val strokeWidth = layer.defaultWidth.coerceIn(1.5f, 6.0f).dp.toPx()
                val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(16.dp.toPx(), 10.dp.toPx()), 0f)

                // 1. Draw solid fixed segments
                if (screenPoints.size >= 2) {
                    for (i in 0 until screenPoints.size - 1) {
                        drawLine(
                            color = layerColor,
                            start = screenPoints[i],
                            end = screenPoints[i + 1],
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // 2. Draw live dashed ray to center cursor (if moved from last vertex)
                if (screenPoints.isNotEmpty() && liveRayPx > 0.0) {
                    val lastScreen = screenPoints.last()
                    val dxScreen = centerScreen.x - lastScreen.x
                    val dyScreen = centerScreen.y - lastScreen.y
                    if (sqrt(dxScreen * dxScreen + dyScreen * dyScreen) >= 3f) {
                        drawLine(
                            color = layerColor.copy(alpha = 0.85f),
                            start = lastScreen,
                            end = centerScreen,
                            strokeWidth = strokeWidth,
                            pathEffect = dashPathEffect,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // 3. Draw vertex circular markers
                screenPoints.forEach { pt ->
                    // Outer glow / background
                    drawCircle(
                        color = layerColor,
                        radius = 5.dp.toPx(),
                        center = pt
                    )
                    // White border
                    drawCircle(
                        color = Color.White,
                        radius = 5.dp.toPx(),
                        center = pt,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    // Dark center dot
                    drawCircle(
                        color = Color.Black,
                        radius = 1.5.dp.toPx(),
                        center = pt
                    )
                }
            }
        }

        // Top Information Banner
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.bgCard)
                .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Text(
                text = infoText,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.textPrimary
            )
        }
    }
}
