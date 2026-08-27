package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.LayerPoint
import com.vktrsansara.app.caveviewer.domain.model.PointLayer
import com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import org.maplibre.android.geometry.LatLng

private data class RenderedPoint(
    val point: LayerPoint,
    val layer: PointLayer,
    val screenOffset: Offset
)

@Composable
fun PointLayersOverlay(
    pointLayers: List<PointLayer>,
    allPoints: List<LayerPoint>,
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
    val textMeasurer = rememberTextMeasurer()
    val layerMap = remember(pointLayers) { pointLayers.associateBy { it.id } }

    val renderedPoints = remember(
        allPoints,
        layerMap,
        projector,
        imageWidth,
        imageHeight,
        zoomMax,
        currentTargetLat,
        currentTargetLon,
        currentZoom,
        mapBearing
    ) {
        if (projector == null || imageWidth <= 0 || imageHeight <= 0) {
            emptyList()
        } else {
            allPoints.mapNotNull { point ->
                val layer = layerMap[point.layerId]
                if (layer != null && layer.isVisible) {
                    val latLng = CaveMapBounds.imagePixelsToLatLng(
                        pixelX = point.x,
                        pixelY = point.y,
                        imageWidth = imageWidth,
                        imageHeight = imageHeight,
                        maxZoom = zoomMax
                    )
                    val screenOffset = projector(latLng)
                    RenderedPoint(point, layer, screenOffset)
                } else {
                    null
                }
            }
        }
    }

    val bgCardColor = AppColors.bgCard
    val borderColor = AppColors.borderColor

    Canvas(modifier = modifier.fillMaxSize()) {
        renderedPoints.forEach { item ->
            val point = item.point
            val layer = item.layer
            val screenOffset = item.screenOffset

            // Check if point has hazard/danger flag set to true
            val isHazard = point.customValues.entries.any { (k, v) ->
                (k.contains("hazard", ignoreCase = true) ||
                        k.contains("опасн", ignoreCase = true) ||
                        k.contains("danger", ignoreCase = true)) &&
                        v.equals("true", ignoreCase = true)
            }

            val markerSizePx = layer.defaultSize.dp.toPx()

            // 1. Draw shape marker
            drawPointShape(
                shape = point.shape,
                center = screenOffset,
                sizePx = markerSizePx,
                fillColor = Color(point.color.toInt()),
                strokeColor = Color.White,
                isHazard = isHazard
            )

            // 2. Draw label badge if enabled for layer
            if (layer.showLabels && point.name.isNotBlank()) {
                val textLayoutResult = textMeasurer.measure(
                    text = point.name,
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                )

                val badgePaddingX = 5.dp.toPx()
                val badgePaddingY = 2.dp.toPx()
                val badgeTopLeft = Offset(
                    x = screenOffset.x + markerSizePx + 6.dp.toPx(),
                    y = screenOffset.y - (textLayoutResult.size.height / 2f) - badgePaddingY
                )

                val badgeSize = Size(
                    width = textLayoutResult.size.width + badgePaddingX * 2,
                    height = textLayoutResult.size.height + badgePaddingY * 2
                )

                // Background badge
                drawRoundRect(
                    color = bgCardColor.copy(alpha = 0.85f),
                    topLeft = badgeTopLeft,
                    size = badgeSize,
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                // Border
                drawRoundRect(
                    color = borderColor.copy(alpha = 0.6f),
                    topLeft = badgeTopLeft,
                    size = badgeSize,
                    cornerRadius = CornerRadius(4.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Label text
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(badgeTopLeft.x + badgePaddingX, badgeTopLeft.y + badgePaddingY)
                )
            }
        }
    }
}
