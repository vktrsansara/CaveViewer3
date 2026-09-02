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
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.ScaleBindingPoint
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import java.util.Locale
import kotlin.math.sqrt

/**
 * Visual canvas overlay for scale calibration mode:
 * - Top instruction banner with high-precision (%.4f) pixel distance
 * - Markers for Point 1 and Point 2
 * - Dynamic dashed line between Point 1 and map center / Point 2
 */
@Composable
fun ScaleBindingOverlay(
    points: List<ScaleBindingPoint>,
    screenPoints: List<Offset>,
    currentCenterPx: Pair<Double, Double>?,
    modifier: Modifier = Modifier
) {
    // Calculate current live distance in map image pixels with subpixel precision (Double)
    val distancePx: Double = if (points.isNotEmpty() && currentCenterPx != null) {
        val p1 = points[0].imagePx
        val dx = currentCenterPx.first - p1.first
        val dy = currentCenterPx.second - p1.second
        sqrt(dx * dx + dy * dy)
    } else {
        0.0
    }
    val distanceText = String.format(Locale.US, "%.4f", distancePx)

    Box(modifier = modifier.fillMaxSize().clipToBounds()) {
        // Canvas rendering markers and dashed line
        Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {
            val centerScreen = Offset(size.width / 2f, size.height / 2f)
            val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)

            if (screenPoints.isNotEmpty()) {
                val p1Screen = screenPoints[0]

                // Draw connecting dashed line
                val targetEnd = if (screenPoints.size >= 2) screenPoints[1] else centerScreen
                drawLine(
                    color = Color(0xFFEF4444),
                    start = p1Screen,
                    end = targetEnd,
                    strokeWidth = 3.dp.toPx(),
                    pathEffect = dashPathEffect
                )

                // Draw Point 1 Marker
                drawCircle(
                    color = Color(0xFFEF4444),
                    radius = 8.dp.toPx(),
                    center = p1Screen
                )
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = p1Screen,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5.dp.toPx(),
                    center = p1Screen
                )

                // Draw Point 2 Marker if available
                if (screenPoints.size >= 2) {
                    val p2Screen = screenPoints[1]
                    drawCircle(
                        color = Color(0xFF10B981),
                        radius = 8.dp.toPx(),
                        center = p2Screen
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 8.dp.toPx(),
                        center = p2Screen,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.5.dp.toPx(),
                        center = p2Screen
                    )
                }
            }
        }

        // Top Informational Banner
        val bannerText = when (points.size) {
            0 -> "Наведите курсор на начало отрезка и коснитесь экрана"
            1 -> "Наведите курсор на конец отрезка ($distanceText px) и коснитесь экрана"
            else -> "Отрезок зафиксирован"
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(10f)
                .padding(top = 16.dp, start = 60.dp, end = 60.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.bgCard.copy(alpha = 0.95f))
                .border(1.dp, AppColors.borderColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = bannerText,
                color = AppColors.textPrimary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
