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
import androidx.compose.material.icons.rounded.LocationSearching
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
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

private val IndigoDeltaColor = Color(0xFF6366F1)
private val AmberWarningColor = Color(0xFFF59E0B)

/**
 * Visual canvas overlay for Delta Offset (ΔX, ΔY) local coordinate tool:
 * - 1 Origin datum point (survey station, entrance, camp)
 * - Orthogonal L-shaped projection triangle (East/West dX and North/South dY)
 * - Direct hypotenuse ray and directional azimuth
 */
@Composable
fun DeltaOffsetOverlay(
    originPoint: ScaleBindingPoint?,
    originScreenPoint: Offset?,
    currentCenterPx: Pair<Double, Double>?,
    angleNorth: Double,
    ppm: Double,
    isActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    val metrics = remember(originPoint, currentCenterPx, angleNorth, ppm, isActive) {
        if (isActive && originPoint != null && currentCenterPx != null) {
            MeasureUtils.calculateDeltaOffset(originPoint.imagePx, currentCenterPx, angleNorth, ppm)
        } else {
            null
        }
    }

    Box(modifier = modifier.fillMaxSize().clipToBounds()) {
        Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {
            val centerScreen = Offset(size.width / 2f, size.height / 2f)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)

            if (originPoint != null && originScreenPoint != null) {
                if (isActive) {
                    // 1. Direct line (hypotenuse) to cursor
                    drawLine(
                        color = IndigoDeltaColor,
                        start = originScreenPoint,
                        end = centerScreen,
                        strokeWidth = 2.dp.toPx()
                    )

                    // 2. Orthogonal projection legs forming right-angled triangle
                    val intermediateScreen = Offset(centerScreen.x, originScreenPoint.y)

                    // Horizontal leg
                    drawLine(
                        color = IndigoDeltaColor.copy(alpha = 0.8f),
                        start = originScreenPoint,
                        end = intermediateScreen,
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = dashEffect
                    )

                    // Vertical leg
                    drawLine(
                        color = IndigoDeltaColor.copy(alpha = 0.8f),
                        start = intermediateScreen,
                        end = centerScreen,
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }

                // 3. Origin Point Marker
                drawCircle(
                    color = IndigoDeltaColor,
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
                    .zIndex(10f)
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp)
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.bgCard.copy(alpha = 0.95f))
                    .border(1.dp, IndigoDeltaColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
            if (originPoint == null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.LocationSearching,
                        contentDescription = "Смещение",
                        tint = IndigoDeltaColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Наведите курсор на опорный репер/вход и коснитесь экрана",
                        color = AppColors.textPrimary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (metrics != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Line 1: Axis offsets East/West and North/South
                    Text(
                        text = "ΔX: ${metrics.deltaXText}   •   ΔY: ${metrics.deltaYText}",
                        color = AppColors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Line 2: Direct distance and azimuth
                    Text(
                        text = "Прямая: ${metrics.directDistanceText}   •   Азимут: ${metrics.azimuthText}",
                        color = IndigoDeltaColor,
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
