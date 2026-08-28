package com.vktrsansara.app.caveviewer.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MagnetIcon(
    size: Dp = 20.dp,
    isEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val redColor = if (isEnabled) Color(0xFFEF4444) else Color.Gray.copy(alpha = 0.5f)
    val blueColor = if (isEnabled) Color(0xFF38BDF8) else Color.Gray.copy(alpha = 0.5f)
    val bodyColor = if (isEnabled) Color(0xFFE2E8F0) else Color.Gray.copy(alpha = 0.3f)

    Canvas(modifier = modifier.size(size)) {
        val stroke = size.toPx() * 0.22f
        val radius = (size.toPx() - stroke) / 2f
        val center = Offset(size.toPx() / 2f, size.toPx() / 2f + stroke * 0.2f)

        // Дуга основания магнита
        drawArc(
            color = bodyColor,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )

        // Левый полюс (Красный)
        drawLine(
            color = redColor,
            start = Offset(center.x - radius, center.y),
            end = Offset(center.x - radius, center.y - radius * 0.9f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        // Правый полюс (Синий)
        drawLine(
            color = blueColor,
            start = Offset(center.x + radius, center.y),
            end = Offset(center.x + radius, center.y - radius * 0.9f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}
