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
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
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
import kotlin.math.sqrt

private val EmeraldRadiusColor = Color(0xFF10B981)
private val AmberWarningColor = Color(0xFFF59E0B)

/**
 * Visual canvas overlay for Radius / Circular Coverage Zone tool:
 * - 1 Center point (survey station, camp, radio repeater)
 * - Scalable circular zone following cursor
 * - Real-time metrics: Radius, Diameter, Area, Circumference
 */
@Composable
fun RadiusMeasureOverlay(
    centerPoint: ScaleBindingPoint?,
    centerScreenPoint: Offset?,
    currentCenterPx: Pair<Double, Double>?,
    ppm: Double,
    isActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    val liveRadiusPx = remember(centerPoint, currentCenterPx, isActive) {
        if (isActive && centerPoint != null && currentCenterPx != null) {
            MeasureUtils.distancePx(centerPoint.imagePx, currentCenterPx)
        } else {
            0.0
        }
    }

    val metrics = remember(liveRadiusPx, ppm) {
        MeasureUtils.calculateCircleMetrics(liveRadiusPx, ppm)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerScreen = Offset(size.width / 2f, size.height / 2f)
            if (centerPoint != null && centerScreenPoint != null) {
                if (isActive) {
                    val dx = centerScreen.x - centerScreenPoint.x
                    val dy = centerScreen.y - centerScreenPoint.y
                    val screenRadius = sqrt(dx * dx + dy * dy)

                    // 1. Semi-transparent circle fill
                    drawCircle(
                        color = EmeraldRadiusColor.copy(alpha = 0.15f),
                        radius = screenRadius,
                        center = centerScreenPoint
                    )

                    // 2. Circle boundary outline
                    drawCircle(
                        color = EmeraldRadiusColor,
                        radius = screenRadius,
                        center = centerScreenPoint,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // 3. Radius line to screen center cursor
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    drawLine(
                        color = EmeraldRadiusColor,
                        start = centerScreenPoint,
                        end = centerScreen,
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }

                // 4. Center point marker
                drawCircle(
                    color = EmeraldRadiusColor,
                    radius = 7.dp.toPx(),
                    center = centerScreenPoint
                )
                drawCircle(
                    color = Color.White,
                    radius = 7.dp.toPx(),
                    center = centerScreenPoint,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5.dp.toPx(),
                    center = centerScreenPoint
                )
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
                    .border(1.dp, EmeraldRadiusColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
            if (centerPoint == null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = "Радиус",
                        tint = EmeraldRadiusColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Наведите курсор на центр зоны и коснитесь экрана",
                        color = AppColors.textPrimary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Радиус: ${metrics.radiusText} (Диаметр: ${metrics.diameterText})   •   Площадь: ${metrics.areaText}",
                        color = AppColors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Длина окружности: ${metrics.perimeterText}",
                        color = EmeraldRadiusColor,
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
