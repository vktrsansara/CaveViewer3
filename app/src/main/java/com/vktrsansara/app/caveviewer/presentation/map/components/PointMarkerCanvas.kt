package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.domain.model.PointShape

/**
 * Renders 7 speleological marker shapes with optional hazard indicator glow.
 */
fun DrawScope.drawPointShape(
    shape: PointShape,
    center: Offset,
    sizePx: Float,
    fillColor: Color,
    strokeColor: Color = Color.White,
    isHazard: Boolean = false
) {
    // 1. Hazard glow
    if (isHazard) {
        drawCircle(
            color = Color(0xFFEF4444).copy(alpha = 0.25f),
            radius = sizePx * 1.8f,
            center = center
        )
    }
    val half = sizePx
    val path = Path()
    when (shape) {
        PointShape.CIRCLE -> {
            drawCircle(color = fillColor, radius = half, center = center)
            drawCircle(color = strokeColor, radius = half, center = center, style = Stroke(width = 1.5.dp.toPx()))
            return
        }
        PointShape.SQUARE -> {
            drawRoundRect(
                color = fillColor,
                topLeft = Offset(center.x - half, center.y - half),
                size = Size(half * 2, half * 2),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
            drawRoundRect(
                color = strokeColor,
                topLeft = Offset(center.x - half, center.y - half),
                size = Size(half * 2, half * 2),
                cornerRadius = CornerRadius(2.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )
            return
        }
        PointShape.TRIANGLE_UP -> {
            path.moveTo(center.x, center.y - half * 1.15f)
            path.lineTo(center.x + half, center.y + half * 0.85f)
            path.lineTo(center.x - half, center.y + half * 0.85f)
            path.close()
        }
        PointShape.TRIANGLE_DOWN -> {
            path.moveTo(center.x, center.y + half * 1.15f)
            path.lineTo(center.x + half, center.y - half * 0.85f)
            path.lineTo(center.x - half, center.y - half * 0.85f)
            path.close()
        }
        PointShape.DIAMOND -> {
            path.moveTo(center.x, center.y - half * 1.2f)
            path.lineTo(center.x + half * 1.2f, center.y)
            path.lineTo(center.x, center.y + half * 1.2f)
            path.lineTo(center.x - half * 1.2f, center.y)
            path.close()
        }
        PointShape.CROSS -> {
            val strokeW = 3.dp.toPx()
            drawLine(strokeColor, Offset(center.x - half, center.y), Offset(center.x + half, center.y), strokeW + 2.dp.toPx(), StrokeCap.Round)
            drawLine(strokeColor, Offset(center.x, center.y - half), Offset(center.x, center.y + half), strokeW + 2.dp.toPx(), StrokeCap.Round)
            drawLine(fillColor, Offset(center.x - half, center.y), Offset(center.x + half, center.y), strokeW, StrokeCap.Round)
            drawLine(fillColor, Offset(center.x, center.y - half), Offset(center.x, center.y + half), strokeW, StrokeCap.Round)
            return
        }
        PointShape.STAR -> {
            // 4-pointed star
            path.moveTo(center.x, center.y - half * 1.3f)
            path.lineTo(center.x + half * 0.35f, center.y - half * 0.35f)
            path.lineTo(center.x + half * 1.3f, center.y)
            path.lineTo(center.x + half * 0.35f, center.y + half * 0.35f)
            path.lineTo(center.x, center.y + half * 1.3f)
            path.lineTo(center.x - half * 0.35f, center.y + half * 0.35f)
            path.lineTo(center.x - half * 1.3f, center.y)
            path.lineTo(center.x - half * 0.35f, center.y - half * 0.35f)
            path.close()
        }
    }
    drawPath(path = path, color = fillColor)
    drawPath(path = path, color = strokeColor, style = Stroke(width = 1.5.dp.toPx()))
}
