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
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Warning
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
import kotlin.math.cos
import kotlin.math.sin

private val CyanAzimuthColor = Color(0xFF06B6D4) // Cyan
private val AmberWarningColor = Color(0xFFF59E0B)

/**
 * Visual canvas overlay for Azimuth & Rumb measurement mode:
 * - 1 Origin point (station / survey peg)
 * - Live ray with arrowhead continuously following the screen center cursor
 * - Calculates direct/back azimuth, rumb quadrant, and real-time distance
 */
@Composable
fun AzimuthOverlay(
    originPoint: ScaleBindingPoint?,
    originScreenPoint: Offset?,
    currentCenterPx: Pair<Double, Double>?,
    angleNorth: Double,
    ppm: Double,
    isActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Dynamic calculations
    val liveDistancePx = remember(originPoint, currentCenterPx, isActive) {
        if (isActive && originPoint != null && currentCenterPx != null) {
            MeasureUtils.distancePx(originPoint.imagePx, currentCenterPx)
        } else {
            0.0
        }
    }

    val liveAzimuth = remember(originPoint, currentCenterPx, angleNorth, isActive) {
        if (isActive && originPoint != null && currentCenterPx != null) {
            MeasureUtils.calculateAzimuthDegrees(originPoint.imagePx, currentCenterPx, angleNorth)
        } else {
            0.0
        }
    }

    val backAzimuth = remember(liveAzimuth) {
        MeasureUtils.calculateBackAzimuth(liveAzimuth)
    }

    val rumb = remember(liveAzimuth) {
        MeasureUtils.calculateRumb(liveAzimuth)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerScreen = Offset(size.width / 2f, size.height / 2f)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)

            if (originPoint != null && originScreenPoint != null) {
                // 1. Dashed ray from origin to center cursor (only if active)
                if (isActive) {
                    drawLine(
                        color = CyanAzimuthColor,
                        start = originScreenPoint,
                        end = centerScreen,
                        strokeWidth = 2.5.dp.toPx(),
                        pathEffect = dashEffect
                    )

                    // 2. Arrowhead at cursor pointing along the ray
                    val dx = centerScreen.x - originScreenPoint.x
                    val dy = centerScreen.y - originScreenPoint.y
                    val angleRad = kotlin.math.atan2(dy, dx)
                    val arrowLen = 14.dp.toPx()
                    val arrowAngle = Math.toRadians(25.0)
                    val arrowP1 = Offset(
                        centerScreen.x - (arrowLen * cos(angleRad - arrowAngle)).toFloat(),
                        centerScreen.y - (arrowLen * sin(angleRad - arrowAngle)).toFloat()
                    )
                    val arrowP2 = Offset(
                        centerScreen.x - (arrowLen * cos(angleRad + arrowAngle)).toFloat(),
                        centerScreen.y - (arrowLen * sin(angleRad + arrowAngle)).toFloat()
                    )

                    drawLine(
                        color = CyanAzimuthColor,
                        start = centerScreen,
                        end = arrowP1,
                        strokeWidth = 2.5.dp.toPx()
                    )
                    drawLine(
                        color = CyanAzimuthColor,
                        start = centerScreen,
                        end = arrowP2,
                        strokeWidth = 2.5.dp.toPx()
                    )
                }

                // 3. Origin Point Marker
                drawCircle(
                    color = CyanAzimuthColor,
                    radius = 7.dp.toPx(),
                    center = originScreenPoint
                )
                drawCircle(
                    color = Color.White,
                    radius = 7.dp.toPx(),
                    center = originScreenPoint,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5.dp.toPx(),
                    center = originScreenPoint
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
                    .border(1.dp, CyanAzimuthColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
            if (originPoint == null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Explore,
                        contentDescription = "Азимут",
                        tint = CyanAzimuthColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Наведите курсор на исходную точку и коснитесь экрана",
                        color = AppColors.textPrimary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Line 1: Direct & Back Azimuth
                    val azStr = String.format(Locale.US, "%.2f°", liveAzimuth)
                    val backAzStr = String.format(Locale.US, "%.2f°", backAzimuth)
                    Text(
                        text = "🧭 Азимут: $azStr   •   Обратный: $backAzStr",
                        color = AppColors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Line 2: Rumb & Distance
                    val rumbStr = "${rumb.first} ${String.format(Locale.US, "%.2f°", rumb.second)}"
                    val distStr = MeasureUtils.formatDistance(liveDistancePx, ppm)
                    Text(
                        text = "Румб: $rumbStr   •   Дистанция: $distStr",
                        color = CyanAzimuthColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (ppm <= 0.0) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = "Внимание",
                                tint = AmberWarningColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Карта без привязки к метрам (px)",
                                color = AmberWarningColor,
                                fontSize = 11.sp,
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
