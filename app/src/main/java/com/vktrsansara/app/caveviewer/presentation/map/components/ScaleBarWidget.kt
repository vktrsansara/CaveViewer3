package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import kotlin.math.abs
import kotlin.math.pow

private val ScaleBarBgDark = Color(0x8C121820)
private val ScaleBarBgLight = Color(0xD9FFFFFF)

/**
 * Динамическая шкала масштаба (Scale Bar).
 * Точно рассчитывает физическое расстояние на карте в единицах Dp с учетом 2x проекции MapLibre для тайлов 256x256.
 */
@Composable
fun ScaleBarWidget(
    pixelsPerMeter: Double,
    zoomMax: Int,
    currentZoom: Double,
    modifier: Modifier = Modifier
) {
    if (pixelsPerMeter <= 0.0) return

    val isDark = AppColors.isDark
    val bgColor = if (isDark) ScaleBarBgDark else ScaleBarBgLight
    val contentColor = AppColors.textPrimary
    val borderColor = AppColors.borderColor

    // Точный расчет в DP: с учетом того, что тайлы 256x256 в проекции MapLibre (базовый тайл 512)
    // отображаются с масштабом 2 DP на 1 пиксель исходного растра при zoom = zoomMax
    val (bestDistance, rulerWidthDp) = remember(pixelsPerMeter, zoomMax, currentZoom) {
        val dpPerMeter = pixelsPerMeter * 2.0.pow(currentZoom - zoomMax.toDouble() + 1.0)
        val niceDistances = listOf(1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000)
        val targetWidthDp = 90.0 // Комфортная ширина плашки в DP

        var chosen = niceDistances.first()
        var minDiff = abs(chosen * dpPerMeter - targetWidthDp)
        for (d in niceDistances) {
            val diff = abs(d * dpPerMeter - targetWidthDp)
            if (diff < minDiff) {
                minDiff = diff
                chosen = d
            }
        }

        // Физическая длина скобы на экране в DP
        val calculatedWidthDp = (chosen * dpPerMeter).dp.coerceIn(30.dp, 180.dp)
        Pair(chosen, calculatedWidthDp)
    }

    val distanceText = if (bestDistance >= 1000) "${bestDistance / 1000} км" else "$bestDistance м"

    Box(
        modifier = modifier
            .background(bgColor, shape = RoundedCornerShape(6.dp))
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Графическая скоба линейки
            Canvas(modifier = Modifier.size(width = rulerWidthDp, height = 7.dp)) {
                val strokeWidth = 2.dp.toPx()
                val h = size.height
                val w = size.width

                // Левая вертикальная засечка
                drawLine(
                    color = contentColor,
                    start = Offset(x = strokeWidth / 2, y = 0f),
                    end = Offset(x = strokeWidth / 2, y = h),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Square
                )

                // Нижняя горизонтальная линия
                drawLine(
                    color = contentColor,
                    start = Offset(x = strokeWidth / 2, y = h - strokeWidth / 2),
                    end = Offset(x = w - strokeWidth / 2, y = h - strokeWidth / 2),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Square
                )

                // Правая вертикальная засечка
                drawLine(
                    color = contentColor,
                    start = Offset(x = w - strokeWidth / 2, y = 0f),
                    end = Offset(x = w - strokeWidth / 2, y = h),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Square
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Текст масштаба
            Text(
                text = distanceText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                lineHeight = 12.sp
            )
        }
    }
}
