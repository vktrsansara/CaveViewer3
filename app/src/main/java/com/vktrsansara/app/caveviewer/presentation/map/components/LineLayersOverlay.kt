package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import com.vktrsansara.app.caveviewer.domain.measure.LineColorUtils
import com.vktrsansara.app.caveviewer.domain.model.LayerLine
import com.vktrsansara.app.caveviewer.domain.model.LineEnvironmentType
import com.vktrsansara.app.caveviewer.domain.model.LineLayer
import com.vktrsansara.app.caveviewer.domain.model.LineStyle
import com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import androidx.compose.ui.unit.dp
import org.maplibre.android.geometry.LatLng

/**
 * Data class representing projected polyline ready for fast Canvas drawing.
 */
private data class ProjectedLine(
    val line: LayerLine,
    val layer: LineLayer,
    val path: Path,
    val screenPoints: List<Offset>,
    val isSelected: Boolean
)

/**
 * High-performance vector overlay for displaying all visible cave line layers and polylines.
 * Supports:
 * - Real-time camera transformation with per-frame recalculation
 * - Layer 1: Selection Glow outline
 * - Layer 2: Core Difficulty Stroke (Heatmap 0.0..8.0 or layer default color, styles: Solid, Dashed, Dotted)
 * - Layer 3: Topographic vector ticks & hatches (UIS/Therion speleological cartography standard)
 */
@Composable
fun LineLayersOverlay(
    lineLayers: List<LineLayer>,
    allLines: List<LayerLine>,
    selectedLineId: Long? = null,
    imageWidth: Int,
    imageHeight: Int,
    zoomMax: Int,
    projector: ((LatLng) -> Offset)?,
    currentTargetLat: Double,
    currentTargetLon: Double,
    currentZoom: Double,
    mapBearing: Double,
    modifier: Modifier = Modifier
) {
    val visibleLayersMap = remember(lineLayers) {
        lineLayers.filter { it.isVisible }.associateBy { it.id }
    }

    val projectedLines = remember(
        allLines,
        visibleLayersMap,
        projector,
        currentTargetLat,
        currentTargetLon,
        currentZoom,
        mapBearing,
        selectedLineId
    ) {
        if (projector == null || imageWidth <= 0 || imageHeight <= 0) {
            emptyList()
        } else {
            allLines.mapNotNull { line ->
                val layer = visibleLayersMap[line.layerId] ?: return@mapNotNull null
                if (line.points.size < 2) return@mapNotNull null

                val screenPoints = line.points.map { pt ->
                    val latLng = CaveMapBounds.imagePixelsToLatLng(
                        pixelX = pt.first,
                        pixelY = pt.second,
                        imageWidth = imageWidth,
                        imageHeight = imageHeight,
                        maxZoom = zoomMax
                    )
                    projector(latLng)
                }

                val path = Path().apply {
                    moveTo(screenPoints[0].x, screenPoints[0].y)
                    for (i in 1 until screenPoints.size) {
                        lineTo(screenPoints[i].x, screenPoints[i].y)
                    }
                }

                ProjectedLine(
                    line = line,
                    layer = layer,
                    path = path,
                    screenPoints = screenPoints,
                    isSelected = (line.id == selectedLineId)
                )
            }
        }
    }

    if (projectedLines.isEmpty()) return

    Canvas(modifier = modifier.fillMaxSize().clipToBounds()) {
        clipRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height
        ) {
            // 1. Draw Selection Glow on selected line
            projectedLines.forEach { item ->
                if (item.isSelected) {
                    val selectionWidth = (item.layer.defaultWidth + 8f) * density
                    drawPath(
                        path = item.path,
                        color = AccentSkyBlue.copy(alpha = 0.35f),
                        style = Stroke(
                            width = selectionWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // 2. Draw Core Line Stroke
            projectedLines.forEach { item ->
                val coreColor = if (item.line.colorOverride != null) {
                    Color(item.line.colorOverride.toInt())
                } else if (item.layer.isHeatmapEnabled) {
                    LineColorUtils.getDifficultyColor(item.line.difficulty)
                } else {
                    Color(item.layer.defaultColor.toInt())
                }

                val strokeWidth = item.layer.defaultWidth * density
                val pathEffect = when (item.line.style) {
                    LineStyle.SOLID -> null
                    LineStyle.DASHED -> {
                        val dashLen = (item.layer.defaultWidth * 2.5f + 6.dp.toPx()).coerceAtLeast(18f)
                        val dashGap = (item.layer.defaultWidth * 1.5f + 6.dp.toPx()).coerceAtLeast(16f)
                        PathEffect.dashPathEffect(floatArrayOf(dashLen, dashGap), 0f)
                    }
                    LineStyle.DOTTED -> {
                        val dotLen = 1f
                        val dotGap = (strokeWidth + 3.5.dp.toPx()).coerceAtLeast(10f)
                        PathEffect.dashPathEffect(floatArrayOf(dotLen, dotGap), 0f)
                    }
                }

                drawPath(
                    path = item.path,
                    color = coreColor,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = pathEffect
                    )
                )
            }

            // 3. Draw Vector Topographic Ticks / Hatches (UIS / Therion Standard)
            projectedLines.forEach { item ->
                if (item.line.environmentType != LineEnvironmentType.NONE) {
                    val patternColor = LineColorUtils.getHaloColor(item.line.environmentType, item.line.haloColor) ?: Color.White
                    val strokeWidth = item.layer.defaultWidth * density
                    LinePatternRenderer.drawEnvironmentPattern(
                        drawScope = this,
                        screenPoints = item.screenPoints,
                        environmentType = item.line.environmentType,
                        patternColor = patternColor,
                        lineWidthPx = strokeWidth
                    )
                }
            }
        }
    }
}
