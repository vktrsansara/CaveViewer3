package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.domain.model.PointShape
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Universal composable for rendering any marker shape inside UI dialogs and lists.
 */
@Composable
fun PointShapeMarker(
    shape: PointShape,
    color: Color,
    modifier: Modifier = Modifier.size(18.dp),
    strokeColor: Color = Color.Black
) {
    Canvas(modifier = modifier) {
        val half = minOf(size.width, size.height) / 2f
        drawPointShape(
            shape = shape,
            center = Offset(size.width / 2f, size.height / 2f),
            sizePx = half * 0.85f,
            fillColor = color,
            strokeColor = strokeColor
        )
    }
}

/**
 * Renders all speleological marker shapes with optional hazard indicator glow.
 */
fun DrawScope.drawPointShape(
    shape: PointShape,
    center: Offset,
    sizePx: Float,
    fillColor: Color,
    strokeColor: Color = Color.Black,
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
    val strokeWidth = 1.5.dp.toPx()
    val path = Path()

    when (shape) {
        // --- 1. Basic Geometry ---
        PointShape.CIRCLE -> {
            drawCircle(color = fillColor, radius = half, center = center)
            drawCircle(color = strokeColor, radius = half, center = center, style = Stroke(width = strokeWidth))
            return
        }
        PointShape.HOLLOW_CIRCLE -> {
            drawCircle(color = fillColor, radius = half * 0.85f, center = center, style = Stroke(width = strokeWidth * 2.2f))
            drawCircle(color = strokeColor, radius = half, center = center, style = Stroke(width = strokeWidth))
            return
        }
        PointShape.RING_DOT -> {
            drawCircle(color = fillColor, radius = half * 0.88f, center = center, style = Stroke(width = strokeWidth * 1.8f))
            drawCircle(color = fillColor, radius = half * 0.32f, center = center)
            drawCircle(color = strokeColor, radius = half, center = center, style = Stroke(width = strokeWidth))
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
                style = Stroke(width = strokeWidth)
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
        PointShape.TRIANGLE_LEFT -> {
            path.moveTo(center.x - half * 1.15f, center.y)
            path.lineTo(center.x + half * 0.85f, center.y - half)
            path.lineTo(center.x + half * 0.85f, center.y + half)
            path.close()
        }
        PointShape.TRIANGLE_RIGHT -> {
            path.moveTo(center.x + half * 1.15f, center.y)
            path.lineTo(center.x - half * 0.85f, center.y - half)
            path.lineTo(center.x - half * 0.85f, center.y + half)
            path.close()
        }
        PointShape.TRIANGLE_DOT -> {
            path.moveTo(center.x, center.y - half * 1.15f)
            path.lineTo(center.x + half, center.y + half * 0.85f)
            path.lineTo(center.x - half, center.y + half * 0.85f)
            path.close()
            drawPath(path = path, color = fillColor)
            drawPath(path = path, color = strokeColor, style = Stroke(width = strokeWidth))
            drawCircle(color = strokeColor, radius = half * 0.28f, center = Offset(center.x, center.y + half * 0.18f))
            return
        }
        PointShape.DIAMOND -> {
            path.moveTo(center.x, center.y - half * 1.2f)
            path.lineTo(center.x + half * 1.2f, center.y)
            path.lineTo(center.x, center.y + half * 1.2f)
            path.lineTo(center.x - half * 1.2f, center.y)
            path.close()
        }
        PointShape.STAR -> {
            val outerR = half * 1.25f
            val innerR = outerR * 0.42f
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) outerR else innerR
                val angle = (i * PI / 5 - PI / 2).toFloat()
                val x = center.x + r * cos(angle)
                val y = center.y + r * sin(angle)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
        }
        PointShape.CROSS -> {
            val strokeW = 3.dp.toPx()
            val d = half * 0.85f
            drawLine(strokeColor, Offset(center.x - d, center.y - d), Offset(center.x + d, center.y + d), strokeW + 2.dp.toPx(), StrokeCap.Round)
            drawLine(strokeColor, Offset(center.x - d, center.y + d), Offset(center.x + d, center.y - d), strokeW + 2.dp.toPx(), StrokeCap.Round)
            drawLine(fillColor, Offset(center.x - d, center.y - d), Offset(center.x + d, center.y + d), strokeW, StrokeCap.Round)
            drawLine(fillColor, Offset(center.x - d, center.y + d), Offset(center.x + d, center.y - d), strokeW, StrokeCap.Round)
            return
        }
        PointShape.PLUS -> {
            val strokeW = 3.dp.toPx()
            drawLine(strokeColor, Offset(center.x, center.y - half), Offset(center.x, center.y + half), strokeW + 2.dp.toPx(), StrokeCap.Round)
            drawLine(strokeColor, Offset(center.x - half, center.y), Offset(center.x + half, center.y), strokeW + 2.dp.toPx(), StrokeCap.Round)
            drawLine(fillColor, Offset(center.x, center.y - half), Offset(center.x, center.y + half), strokeW, StrokeCap.Round)
            drawLine(fillColor, Offset(center.x - half, center.y), Offset(center.x + half, center.y), strokeW, StrokeCap.Round)
            return
        }
        PointShape.PENTAGON -> {
            drawRegularPolygon(path, center, half * 1.1f, 5, -PI.toFloat() / 2f)
        }
        PointShape.HEXAGON -> {
            drawRegularPolygon(path, center, half * 1.1f, 6, 0f)
        }
        PointShape.OCTAGON -> {
            drawRegularPolygon(path, center, half * 1.1f, 8, (PI / 8).toFloat())
        }
        PointShape.SEMICIRCLE_TOP -> {
            drawArc(
                color = fillColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(center.x - half, center.y - half),
                size = Size(half * 2, half * 2)
            )
            drawArc(
                color = strokeColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(center.x - half, center.y - half),
                size = Size(half * 2, half * 2),
                style = Stroke(width = strokeWidth)
            )
            return
        }
        PointShape.SEMICIRCLE_BOTTOM -> {
            drawArc(
                color = fillColor,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(center.x - half, center.y - half),
                size = Size(half * 2, half * 2)
            )
            drawArc(
                color = strokeColor,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(center.x - half, center.y - half),
                size = Size(half * 2, half * 2),
                style = Stroke(width = strokeWidth)
            )
            return
        }
        PointShape.ARCH -> {
            path.moveTo(center.x - half * 0.9f, center.y + half * 0.9f)
            path.lineTo(center.x - half * 0.9f, center.y)
            path.arcTo(Rect(center.x - half * 0.9f, center.y - half * 0.9f, center.x + half * 0.9f, center.y + half * 0.9f), 180f, 180f, false)
            path.lineTo(center.x + half * 0.9f, center.y + half * 0.9f)
            path.close()
        }
        PointShape.INVERTED_ARCH -> {
            path.moveTo(center.x - half * 0.9f, center.y - half * 0.9f)
            path.lineTo(center.x - half * 0.9f, center.y)
            path.arcTo(Rect(center.x - half * 0.9f, center.y - half * 0.9f, center.x + half * 0.9f, center.y + half * 0.9f), 180f, -180f, false)
            path.lineTo(center.x + half * 0.9f, center.y - half * 0.9f)
            path.close()
        }
        PointShape.SPIRAL -> {
            val strokeW = 2.2.dp.toPx()
            path.moveTo(center.x, center.y)
            for (step in 0..36) {
                val theta = step * (PI / 6).toFloat()
                val r = (half * 0.95f) * (step / 36f)
                val x = center.x + r * cos(theta)
                val y = center.y + r * sin(theta)
                path.lineTo(x, y)
            }
            drawPath(path, strokeColor, style = Stroke(width = strokeW + 2.dp.toPx(), cap = StrokeCap.Round))
            drawPath(path, fillColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))
            return
        }
        PointShape.ASTERISK -> {
            val strokeW = 2.5.dp.toPx()
            for (i in 0 until 3) {
                val angle = i * PI / 3
                val dx = (half * cos(angle)).toFloat()
                val dy = (half * sin(angle)).toFloat()
                drawLine(strokeColor, Offset(center.x - dx, center.y - dy), Offset(center.x + dx, center.y + dy), strokeW + 2.dp.toPx(), StrokeCap.Round)
                drawLine(fillColor, Offset(center.x - dx, center.y - dy), Offset(center.x + dx, center.y + dy), strokeW, StrokeCap.Round)
            }
            return
        }
        PointShape.HASH -> {
            val strokeW = 2.dp.toPx()
            val offset = half * 0.4f
            val len = half * 0.9f
            // Vertical lines
            drawLine(strokeColor, Offset(center.x - offset, center.y - len), Offset(center.x - offset, center.y + len), strokeW + 1.5.dp.toPx())
            drawLine(strokeColor, Offset(center.x + offset, center.y - len), Offset(center.x + offset, center.y + len), strokeW + 1.5.dp.toPx())
            // Horizontal lines
            drawLine(strokeColor, Offset(center.x - len, center.y - offset), Offset(center.x + len, center.y - offset), strokeW + 1.5.dp.toPx())
            drawLine(strokeColor, Offset(center.x - len, center.y + offset), Offset(center.x + len, center.y + offset), strokeW + 1.5.dp.toPx())

            drawLine(fillColor, Offset(center.x - offset, center.y - len), Offset(center.x - offset, center.y + len), strokeW)
            drawLine(fillColor, Offset(center.x + offset, center.y - len), Offset(center.x + offset, center.y + len), strokeW)
            drawLine(fillColor, Offset(center.x - len, center.y - offset), Offset(center.x + len, center.y - offset), strokeW)
            drawLine(fillColor, Offset(center.x - len, center.y + offset), Offset(center.x + len, center.y + offset), strokeW)
            return
        }

        // --- 2. Directional Arrows (8 directions) ---
        PointShape.ARROW_UP -> { drawArrow(center, half, -PI.toFloat() / 2f, fillColor, strokeColor, strokeWidth); return }
        PointShape.ARROW_DOWN -> { drawArrow(center, half, PI.toFloat() / 2f, fillColor, strokeColor, strokeWidth); return }
        PointShape.ARROW_LEFT -> { drawArrow(center, half, PI.toFloat(), fillColor, strokeColor, strokeWidth); return }
        PointShape.ARROW_RIGHT -> { drawArrow(center, half, 0f, fillColor, strokeColor, strokeWidth); return }
        PointShape.ARROW_NE -> { drawArrow(center, half, -PI.toFloat() / 4f, fillColor, strokeColor, strokeWidth); return }
        PointShape.ARROW_NW -> { drawArrow(center, half, -3f * PI.toFloat() / 4f, fillColor, strokeColor, strokeWidth); return }
        PointShape.ARROW_SE -> { drawArrow(center, half, PI.toFloat() / 4f, fillColor, strokeColor, strokeWidth); return }
        PointShape.ARROW_SW -> { drawArrow(center, half, 3f * PI.toFloat() / 4f, fillColor, strokeColor, strokeWidth); return }

        // --- 3. Speleological & Therion Icons ---
        PointShape.ENTRANCE -> {
            // Cave Entrance portal: triangular black mouth with arched entrance frame
            path.moveTo(center.x - half, center.y + half * 0.8f)
            path.lineTo(center.x, center.y - half * 0.9f)
            path.lineTo(center.x + half, center.y + half * 0.8f)
            path.close()
            drawPath(path, color = fillColor)
            drawPath(path, color = strokeColor, style = Stroke(width = strokeWidth))
            // Inner dark entry
            val innerPath = Path().apply {
                moveTo(center.x - half * 0.5f, center.y + half * 0.8f)
                lineTo(center.x, center.y - half * 0.1f)
                lineTo(center.x + half * 0.5f, center.y + half * 0.8f)
                close()
            }
            drawPath(innerPath, color = strokeColor)
            return
        }
        PointShape.PITCH -> {
            // Pitch / Drop shaft: square frame with depth lines
            drawRect(color = fillColor, topLeft = Offset(center.x - half, center.y - half), size = Size(half * 2, half * 2))
            drawRect(color = strokeColor, topLeft = Offset(center.x - half, center.y - half), size = Size(half * 2, half * 2), style = Stroke(strokeWidth))
            // Central drop hatch
            drawLine(strokeColor, Offset(center.x - half * 0.5f, center.y - half * 0.5f), Offset(center.x + half * 0.5f, center.y + half * 0.5f), strokeWidth)
            drawLine(strokeColor, Offset(center.x - half * 0.5f, center.y + half * 0.5f), Offset(center.x + half * 0.5f, center.y - half * 0.5f), strokeWidth)
            return
        }
        PointShape.CHIMNEY -> {
            // Chimney / Avon: square frame with upward arrow
            drawRect(color = fillColor, topLeft = Offset(center.x - half, center.y - half), size = Size(half * 2, half * 2))
            drawRect(color = strokeColor, topLeft = Offset(center.x - half, center.y - half), size = Size(half * 2, half * 2), style = Stroke(strokeWidth))
            // Arrow up
            path.moveTo(center.x, center.y - half * 0.6f)
            path.lineTo(center.x + half * 0.45f, center.y)
            path.lineTo(center.x - half * 0.45f, center.y)
            path.close()
            drawPath(path, color = strokeColor)
            drawLine(strokeColor, Offset(center.x, center.y), Offset(center.x, center.y + half * 0.6f), strokeWidth * 1.5f)
            return
        }
        PointShape.WATER_SPRING -> {
            // Water Spring / Droplet
            path.moveTo(center.x, center.y - half * 1.15f)
            path.cubicTo(
                center.x + half * 0.9f, center.y,
                center.x + half * 0.8f, center.y + half * 0.9f,
                center.x, center.y + half * 0.9f
            )
            path.cubicTo(
                center.x - half * 0.8f, center.y + half * 0.9f,
                center.x - half * 0.9f, center.y,
                center.x, center.y - half * 1.15f
            )
            path.close()
        }
        PointShape.WATER_FLOW -> {
            // Flowing water stream waves with arrow
            val strokeW = 2.dp.toPx()
            val wavePath = Path().apply {
                moveTo(center.x - half, center.y - half * 0.2f)
                quadraticTo(center.x - half * 0.5f, center.y - half * 0.7f, center.x, center.y - half * 0.2f)
                quadraticTo(center.x + half * 0.5f, center.y + half * 0.3f, center.x + half, center.y - half * 0.2f)
            }
            drawPath(wavePath, strokeColor, style = Stroke(width = strokeW + 2.dp.toPx(), cap = StrokeCap.Round))
            drawPath(wavePath, fillColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))

            val wavePath2 = Path().apply {
                moveTo(center.x - half, center.y + half * 0.3f)
                quadraticTo(center.x - half * 0.5f, center.y - half * 0.2f, center.x, center.y + half * 0.3f)
                quadraticTo(center.x + half * 0.5f, center.y + half * 0.8f, center.x + half, center.y + half * 0.3f)
            }
            drawPath(wavePath2, strokeColor, style = Stroke(width = strokeW + 2.dp.toPx(), cap = StrokeCap.Round))
            drawPath(wavePath2, fillColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))
            return
        }
        PointShape.LAKE -> {
            // Sump / Lake: circle with dual wave lines
            drawCircle(color = fillColor, radius = half, center = center)
            drawCircle(color = strokeColor, radius = half, center = center, style = Stroke(strokeWidth))
            val wave1 = Path().apply {
                moveTo(center.x - half * 0.6f, center.y - half * 0.2f)
                quadraticTo(center.x - half * 0.3f, center.y - half * 0.5f, center.x, center.y - half * 0.2f)
                quadraticTo(center.x + half * 0.3f, center.y + half * 0.1f, center.x + half * 0.6f, center.y - half * 0.2f)
            }
            drawPath(wave1, strokeColor, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
            val wave2 = Path().apply {
                moveTo(center.x - half * 0.6f, center.y + half * 0.25f)
                quadraticTo(center.x - half * 0.3f, center.y - half * 0.05f, center.x, center.y + half * 0.25f)
                quadraticTo(center.x + half * 0.3f, center.y + half * 0.55f, center.x + half * 0.6f, center.y + half * 0.25f)
            }
            drawPath(wave2, strokeColor, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
            return
        }
        PointShape.DANGER -> {
            // Hazard triangle with exclamation mark (!)
            path.moveTo(center.x, center.y - half * 1.15f)
            path.lineTo(center.x + half * 1.1f, center.y + half * 0.85f)
            path.lineTo(center.x - half * 1.1f, center.y + half * 0.85f)
            path.close()
            drawPath(path, color = fillColor)
            drawPath(path, color = strokeColor, style = Stroke(width = strokeWidth))
            // Exclamation line & dot
            drawLine(strokeColor, Offset(center.x, center.y - half * 0.45f), Offset(center.x, center.y + half * 0.15f), strokeWidth * 1.8f, StrokeCap.Round)
            drawCircle(strokeColor, radius = half * 0.12f, center = Offset(center.x, center.y + half * 0.5f))
            return
        }
        PointShape.COLLAPSE -> {
            // Rockfall / collapse: 3 stacked angular rock shards
            val rock1 = Path().apply {
                moveTo(center.x - half * 0.8f, center.y + half * 0.8f)
                lineTo(center.x - half * 0.3f, center.y)
                lineTo(center.x + half * 0.2f, center.y + half * 0.8f)
                close()
            }
            drawPath(rock1, color = fillColor)
            drawPath(rock1, color = strokeColor, style = Stroke(strokeWidth))

            val rock2 = Path().apply {
                moveTo(center.x - half * 0.2f, center.y + half * 0.8f)
                lineTo(center.x + half * 0.4f, center.y - half * 0.1f)
                lineTo(center.x + half * 0.9f, center.y + half * 0.8f)
                close()
            }
            drawPath(rock2, color = fillColor)
            drawPath(rock2, color = strokeColor, style = Stroke(strokeWidth))

            val topRock = Path().apply {
                moveTo(center.x - half * 0.4f, center.y + half * 0.1f)
                lineTo(center.x, center.y - half * 0.85f)
                lineTo(center.x + half * 0.4f, center.y + half * 0.1f)
                close()
            }
            drawPath(topRock, color = fillColor)
            drawPath(topRock, color = strokeColor, style = Stroke(strokeWidth))
            return
        }
        PointShape.BOULDER -> {
            // Cluster of rounded boulders
            drawCircle(fillColor, half * 0.55f, Offset(center.x - half * 0.35f, center.y + half * 0.25f))
            drawCircle(strokeColor, half * 0.55f, Offset(center.x - half * 0.35f, center.y + half * 0.25f), style = Stroke(strokeWidth))

            drawCircle(fillColor, half * 0.5f, Offset(center.x + half * 0.35f, center.y + half * 0.3f))
            drawCircle(strokeColor, half * 0.5f, Offset(center.x + half * 0.35f, center.y + half * 0.3f), style = Stroke(strokeWidth))

            drawCircle(fillColor, half * 0.45f, Offset(center.x, center.y - half * 0.35f))
            drawCircle(strokeColor, half * 0.45f, Offset(center.x, center.y - half * 0.35f), style = Stroke(strokeWidth))
            return
        }
        PointShape.CAMP -> {
            // Camping / Bivouac tent
            path.moveTo(center.x, center.y - half * 0.9f)
            path.lineTo(center.x + half, center.y + half * 0.85f)
            path.lineTo(center.x - half, center.y + half * 0.85f)
            path.close()
            drawPath(path, color = fillColor)
            drawPath(path, color = strokeColor, style = Stroke(strokeWidth))
            // Tent flap line
            drawLine(strokeColor, Offset(center.x, center.y - half * 0.9f), Offset(center.x, center.y + half * 0.85f), strokeWidth)
            return
        }
        PointShape.STATION -> {
            // Survey picket / station: equilateral triangle with center dot
            path.moveTo(center.x, center.y - half * 1.15f)
            path.lineTo(center.x + half, center.y + half * 0.85f)
            path.lineTo(center.x - half, center.y + half * 0.85f)
            path.close()
            drawPath(path, color = fillColor)
            drawPath(path, color = strokeColor, style = Stroke(strokeWidth))
            drawCircle(color = strokeColor, radius = half * 0.22f, center = Offset(center.x, center.y + half * 0.18f))
            return
        }
        PointShape.AIR_DRAFT -> {
            // Air draft / ventilation: 3 curved breeze lines
            val strokeW = 1.8.dp.toPx()
            for (offsetY in listOf(-half * 0.45f, 0f, half * 0.45f)) {
                val breeze = Path().apply {
                    moveTo(center.x - half * 0.8f, center.y + offsetY)
                    quadraticTo(center.x, center.y + offsetY - half * 0.25f, center.x + half * 0.8f, center.y + offsetY)
                }
                drawPath(breeze, strokeColor, style = Stroke(strokeW + 1.5.dp.toPx(), cap = StrokeCap.Round))
                drawPath(breeze, fillColor, style = Stroke(strokeW, cap = StrokeCap.Round))
            }
            return
        }
        PointShape.BAT -> {
            // Bat wings silhouette
            path.moveTo(center.x, center.y - half * 0.3f) // Head
            path.quadraticTo(center.x + half * 0.5f, center.y - half * 0.9f, center.x + half, center.y - half * 0.4f) // Wing tip right
            path.quadraticTo(center.x + half * 0.6f, center.y + half * 0.1f, center.x + half * 0.3f, center.y + half * 0.6f)
            path.lineTo(center.x, center.y + half * 0.3f)
            path.lineTo(center.x - half * 0.3f, center.y + half * 0.6f)
            path.quadraticTo(center.x - half * 0.6f, center.y + half * 0.1f, center.x - half, center.y - half * 0.4f) // Wing tip left
            path.quadraticTo(center.x - half * 0.5f, center.y - half * 0.9f, center.x, center.y - half * 0.3f)
            path.close()
        }
        PointShape.BONES -> {
            // Crossed bone icon (paleontology / bones)
            val strokeW = 2.5.dp.toPx()
            val d = half * 0.75f
            drawLine(strokeColor, Offset(center.x - d, center.y - d), Offset(center.x + d, center.y + d), strokeW + 2.dp.toPx(), StrokeCap.Round)
            drawLine(strokeColor, Offset(center.x - d, center.y + d), Offset(center.x + d, center.y - d), strokeW + 2.dp.toPx(), StrokeCap.Round)
            drawLine(fillColor, Offset(center.x - d, center.y - d), Offset(center.x + d, center.y + d), strokeW, StrokeCap.Round)
            drawLine(fillColor, Offset(center.x - d, center.y + d), Offset(center.x + d, center.y - d), strokeW, StrokeCap.Round)
            // Bone knobs at extremities
            drawCircle(fillColor, radius = half * 0.2f, center = Offset(center.x - d, center.y - d))
            drawCircle(fillColor, radius = half * 0.2f, center = Offset(center.x + d, center.y + d))
            drawCircle(fillColor, radius = half * 0.2f, center = Offset(center.x - d, center.y + d))
            drawCircle(fillColor, radius = half * 0.2f, center = Offset(center.x + d, center.y - d))
            return
        }
    }

    drawPath(path = path, color = fillColor)
    drawPath(path = path, color = strokeColor, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))
}

private fun DrawScope.drawArrow(
    center: Offset,
    half: Float,
    angleRad: Float,
    fillColor: Color,
    strokeColor: Color,
    strokeWidth: Float
) {
    val cosA = cos(angleRad)
    val sinA = sin(angleRad)
    val tip = Offset(center.x + half * 1.15f * cosA, center.y + half * 1.15f * sinA)
    val base = Offset(center.x - half * 0.9f * cosA, center.y - half * 0.9f * sinA)

    // Arrowhead wing points
    val headLen = half * 0.85f
    val wingAngle = 0.5f
    val leftWing = Offset(
        tip.x - headLen * cos(angleRad - wingAngle),
        tip.y - headLen * sin(angleRad - wingAngle)
    )
    val rightWing = Offset(
        tip.x - headLen * cos(angleRad + wingAngle),
        tip.y - headLen * sin(angleRad + wingAngle)
    )

    // Draw Arrow stem
    val stemW = 3.dp.toPx()
    drawLine(strokeColor, base, tip, stemW + 2.dp.toPx(), StrokeCap.Round)
    drawLine(fillColor, base, tip, stemW, StrokeCap.Round)

    // Draw Arrow head
    val headPath = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(leftWing.x, leftWing.y)
        lineTo(rightWing.x, rightWing.y)
        close()
    }
    drawPath(headPath, fillColor)
    drawPath(headPath, strokeColor, style = Stroke(width = strokeWidth))
}

private fun drawRegularPolygon(
    path: Path,
    center: Offset,
    radius: Float,
    sides: Int,
    initialAngle: Float
) {
    for (i in 0 until sides) {
        val angle = initialAngle + (i * 2 * PI / sides).toFloat()
        val x = center.x + radius * cos(angle)
        val y = center.y + radius * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
}
