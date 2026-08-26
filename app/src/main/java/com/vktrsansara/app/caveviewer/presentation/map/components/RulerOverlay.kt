package com.vktrsansara.app.caveviewer.presentation.map.components

import android.graphics.Paint
import android.graphics.Rect
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
import androidx.compose.material.icons.rounded.Straighten
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils
import com.vktrsansara.app.caveviewer.domain.model.ScaleBindingPoint
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

private val RulerColor = Color(0xFF8A2BE2) // BlueViolet
private val WarningAmber = Color(0xFFF59E0B)

/**
 * Overlay for Ruler (distance measurement) mode.
 */
@Composable
fun RulerOverlay(
    points: List<ScaleBindingPoint>,
    screenPoints: List<Offset>,
    currentCenterPx: Pair<Double, Double>?,
    ppm: Double,
    modifier: Modifier = Modifier
) {
    // 1. Calculate total polyline length (fixed points + dynamic segment to center)
    val totalLengthPx: Double = remember(points, currentCenterPx) {
        if (points.isEmpty()) return@remember 0.0
        var total = 0.0
        for (i in 0 until points.size - 1) {
            total += MeasureUtils.distancePx(points[i].imagePx, points[i + 1].imagePx)
        }
        if (currentCenterPx != null) {
            total += MeasureUtils.distancePx(points.last().imagePx, currentCenterPx)
        }
        total
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Canvas rendering polyline, points, dynamic ray, and segment labels
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerScreen = Offset(size.width / 2f, size.height / 2f)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
            val strokeWidthPx = 2.5.dp.toPx()

            // Paint for segment distance badges
            val textPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 11.dp.toPx()
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            val badgeBgPaint = Paint().apply {
                color = RulerColor.copy(alpha = 0.9f).toArgb()
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            val badgeStrokePaint = Paint().apply {
                color = android.graphics.Color.WHITE
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = 1.dp.toPx()
            }

            val pointCount = minOf(screenPoints.size, points.size)
            if (pointCount > 0) {
                val validScreenPoints = screenPoints.take(pointCount)

                // 1. Draw fixed segments between placed points
                for (i in 0 until pointCount - 1) {
                    val p1 = validScreenPoints[i]
                    val p2 = validScreenPoints[i + 1]

                    drawLine(
                        color = RulerColor,
                        start = p1,
                        end = p2,
                        strokeWidth = strokeWidthPx,
                        pathEffect = dashEffect
                    )

                    // Segment length badge at midpoint
                    val midX = (p1.x + p2.x) / 2f
                    val midY = (p1.y + p2.y) / 2f
                    val segDistPx = MeasureUtils.distancePx(points[i].imagePx, points[i + 1].imagePx)
                    val segText = MeasureUtils.formatDistance(segDistPx, ppm)

                    val textBounds = Rect()
                    textPaint.getTextBounds(segText, 0, segText.length, textBounds)
                    val paddingH = 6.dp.toPx()
                    val paddingV = 3.dp.toPx()
                    val badgeRect = android.graphics.RectF(
                        midX - textBounds.width() / 2f - paddingH,
                        midY - textBounds.height() / 2f - paddingV,
                        midX + textBounds.width() / 2f + paddingH,
                        midY + textBounds.height() / 2f + paddingV
                    )

                    drawContext.canvas.nativeCanvas.drawRoundRect(badgeRect, 4.dp.toPx(), 4.dp.toPx(), badgeBgPaint)
                    drawContext.canvas.nativeCanvas.drawRoundRect(badgeRect, 4.dp.toPx(), 4.dp.toPx(), badgeStrokePaint)
                    drawContext.canvas.nativeCanvas.drawText(
                        segText,
                        midX,
                        midY + textBounds.height() / 2f - 1.dp.toPx(),
                        textPaint
                    )
                }

                // 2. Draw dynamic ray from last point to center cursor
                val lastScreenPoint = validScreenPoints.last()
                drawLine(
                    color = RulerColor,
                    start = lastScreenPoint,
                    end = centerScreen,
                    strokeWidth = strokeWidthPx,
                    pathEffect = dashEffect
                )

                // 3. Draw vertices markers
                validScreenPoints.forEachIndexed { index, screenPt ->
                    if (index == 0) {
                        // First point: White fill with BlueViolet border
                        drawCircle(
                            color = Color.White,
                            radius = 6.dp.toPx(),
                            center = screenPt
                        )
                        drawCircle(
                            color = RulerColor,
                            radius = 6.dp.toPx(),
                            center = screenPt,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        drawCircle(
                            color = RulerColor,
                            radius = 2.dp.toPx(),
                            center = screenPt
                        )
                    } else {
                        // Subsequent points: BlueViolet fill with White border
                        drawCircle(
                            color = RulerColor,
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

        // Top Info Banner
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
            if (points.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Straighten,
                        contentDescription = "Линейка",
                        tint = RulerColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Наведите курсор на начальную точку и коснитесь экрана",
                        color = AppColors.textPrimary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📏 Длина: ",
                            color = AppColors.textSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = MeasureUtils.formatDistance(totalLengthPx, ppm),
                            color = AppColors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(Точек: ${points.size + 1})",
                            color = AppColors.textSecondary,
                            fontSize = 12.sp
                        )
                    }

                    if (ppm <= 0.0) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = "Внимание",
                                tint = WarningAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Карта без привязки к метрам (px)",
                                color = WarningAmber,
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
