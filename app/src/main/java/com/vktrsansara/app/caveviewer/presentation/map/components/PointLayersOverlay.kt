package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.domain.model.LayerPoint
import com.vktrsansara.app.caveviewer.domain.model.PointLayer
import com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import org.maplibre.android.geometry.LatLng

/**
 * Lightweight interactive overlay for points in Compose Canvas.
 * Passive saved points and their labels are rendered directly on the GPU by MapLibre Native SDK (CircleLayer, SymbolLayer).
 * This Compose overlay is responsible ONLY for drawing the Selection Glow around the currently selected point.
 */
@Composable
fun PointLayersOverlay(
    pointLayers: List<PointLayer>,
    allPoints: List<LayerPoint>,
    selectedPointId: Long? = null,
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
    if (selectedPointId == null || projector == null || imageWidth <= 0 || imageHeight <= 0 || zoomMax <= 0) {
        return
    }

    val layerMap = remember(pointLayers) { pointLayers.associateBy { it.id } }

    val selectedPoint = remember(allPoints, selectedPointId) {
        allPoints.firstOrNull { it.id == selectedPointId }
    } ?: return

    val layer = layerMap[selectedPoint.layerId] ?: return

    val pointLatLng = remember(selectedPoint, imageWidth, imageHeight, zoomMax) {
        CaveMapBounds.imagePixelsToLatLng(
            pixelX = selectedPoint.x,
            pixelY = selectedPoint.y,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            maxZoom = zoomMax
        )
    }

    val screenOffset = remember(pointLatLng, projector, currentTargetLat, currentTargetLon, currentZoom, mapBearing) {
        val sp = projector(pointLatLng)
        if (sp.x.isFinite() && sp.y.isFinite()) sp else null
    } ?: return

    Canvas(modifier = modifier.fillMaxSize().clipToBounds()) {
        clipRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height
        ) {
            val markerRadiusPx = (layer.defaultSize.dp.toPx() / 2f) + 6.dp.toPx()

            // Outer soft glow
            drawCircle(
                color = AccentSkyBlue.copy(alpha = 0.35f),
                radius = markerRadiusPx + 4.dp.toPx(),
                center = screenOffset
            )

            // Crisp selection ring
            drawCircle(
                color = AccentSkyBlue,
                radius = markerRadiusPx,
                center = screenOffset,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
