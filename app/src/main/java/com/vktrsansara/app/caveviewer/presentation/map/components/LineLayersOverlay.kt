package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.domain.model.LayerLine
import com.vktrsansara.app.caveviewer.domain.model.LineLayer
import com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import org.maplibre.android.geometry.LatLng

/**
 * Lightweight interactive overlay for lines in Compose Canvas.
 * Passive saved lines are rendered directly on the GPU by MapLibre Native SDK (LineLayer).
 * This Compose overlay is responsible ONLY for drawing the Selection Glow around the currently selected line.
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
    if (selectedLineId == null || projector == null || imageWidth <= 0 || imageHeight <= 0 || zoomMax <= 0) {
        return
    }

    val visibleLayersMap = remember(lineLayers) {
        lineLayers.filter { it.isVisible }.associateBy { it.id }
    }

    val selectedLine = remember(allLines, selectedLineId) {
        allLines.firstOrNull { it.id == selectedLineId && it.points.size >= 2 }
    } ?: return

    val layer = visibleLayersMap[selectedLine.layerId] ?: return

    // Pre-cache LatLng points for the selected line
    val latLngPoints = remember(selectedLine, imageWidth, imageHeight, zoomMax) {
        selectedLine.points.map { pt ->
            CaveMapBounds.imagePixelsToLatLng(
                pixelX = pt.first,
                pixelY = pt.second,
                imageWidth = imageWidth,
                imageHeight = imageHeight,
                maxZoom = zoomMax
            )
        }
    }

    // Fast screen projection per frame only for the single selected line
    val screenPath = remember(latLngPoints, projector, currentTargetLat, currentTargetLon, currentZoom, mapBearing) {
        if (latLngPoints.isEmpty()) return@remember null
        val screenPoints = latLngPoints.map { projector(it) }
        Path().apply {
            moveTo(screenPoints[0].x, screenPoints[0].y)
            for (i in 1 until screenPoints.size) {
                lineTo(screenPoints[i].x, screenPoints[i].y)
            }
        }
    } ?: return

    Canvas(modifier = modifier.fillMaxSize().clipToBounds()) {
        clipRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height
        ) {
            val selectionWidth = (layer.defaultWidth + 8f) * density
            drawPath(
                path = screenPath,
                color = AccentSkyBlue.copy(alpha = 0.35f),
                style = Stroke(
                    width = selectionWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
