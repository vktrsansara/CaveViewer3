package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.domain.engine.SnapTarget

fun DrawScope.drawSnapIndicator(
    snapTarget: SnapTarget,
    snapScreenPoint: Offset
) {
    val snapPink = Color(0xFFF43F5E) // Ярко-розовая мишень QGIS / CAD

    when (snapTarget) {
        is SnapTarget.Vertex, is SnapTarget.Point -> {
            // Стиль ⊙ (Вершина / Точка): розовая точка с микро-кольцом
            // 1. Тонкое внешнее микро-кольцо (6.5 dp, обводка 1.2 dp)
            drawCircle(
                color = snapPink.copy(alpha = 0.85f),
                radius = 6.5.dp.toPx(),
                center = snapScreenPoint,
                style = Stroke(width = 1.2.dp.toPx())
            )
            // 2. Центральная плотная розовая точка (3.5 dp)
            drawCircle(
                color = snapPink,
                radius = 3.5.dp.toPx(),
                center = snapScreenPoint
            )
            // 3. Белая контрастная сердцевинка (1.2 dp)
            drawCircle(
                color = Color.White,
                radius = 1.2.dp.toPx(),
                center = snapScreenPoint
            )
        }
        is SnapTarget.Edge -> {
            // Стиль ◇ (Ребро): микро-ромбик на линии
            val diamondHalf = 5.5.dp.toPx()
            val diamondPath = Path().apply {
                moveTo(snapScreenPoint.x, snapScreenPoint.y - diamondHalf)
                lineTo(snapScreenPoint.x + diamondHalf, snapScreenPoint.y)
                lineTo(snapScreenPoint.x, snapScreenPoint.y + diamondHalf)
                lineTo(snapScreenPoint.x - diamondHalf, snapScreenPoint.y)
                close()
            }
            // Тонкая обводка ромбика
            drawPath(
                path = diamondPath,
                color = snapPink,
                style = Stroke(width = 1.4.dp.toPx())
            )
            // Центральная розовая точка
            drawCircle(
                color = snapPink,
                radius = 2.2.dp.toPx(),
                center = snapScreenPoint
            )
            // Белая микро-сердцевинка
            drawCircle(
                color = Color.White,
                radius = 1.0.dp.toPx(),
                center = snapScreenPoint
            )
        }
        is SnapTarget.Intersection -> {
            // Стиль ✕ (Перекресток): микро-крестик
            val crossHalf = 4.5.dp.toPx()
            drawLine(
                color = snapPink,
                start = Offset(snapScreenPoint.x - crossHalf, snapScreenPoint.y - crossHalf),
                end = Offset(snapScreenPoint.x + crossHalf, snapScreenPoint.y + crossHalf),
                strokeWidth = 1.6.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = snapPink,
                start = Offset(snapScreenPoint.x + crossHalf, snapScreenPoint.y - crossHalf),
                end = Offset(snapScreenPoint.x - crossHalf, snapScreenPoint.y + crossHalf),
                strokeWidth = 1.6.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun SnappingIndicatorOverlay(
    snapTarget: SnapTarget?,
    snapScreenPoint: Offset?,
    modifier: Modifier = Modifier
) {
    if (snapTarget == null || snapScreenPoint == null) return

    Canvas(modifier = modifier) {
        drawSnapIndicator(snapTarget, snapScreenPoint)
    }
}
