package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.domain.model.AppSettings
import com.vktrsansara.app.caveviewer.domain.model.MapMetadata
import com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds
import org.maplibre.android.maps.MapLibreMap
import kotlin.math.sqrt

/**
 * Visual canvas overlay rendering coordinate grid lines locked to the cave map raster coordinate system.
 * The grid rotates, scales, and translates synchronously with the map, starting from top-left (0, 0),
 * and is strictly clipped to the canvas viewport.
 */
@Composable
fun MapGridOverlay(
    map: MapLibreMap?,
    metadata: MapMetadata,
    settings: AppSettings,
    cameraVersion: Long,
    modifier: Modifier = Modifier
) {
    if (!settings.gridEnabled || map == null) return

    val gridColor = remember(settings.gridColor) {
        Color(settings.gridColor.toInt())
    }

    // Step size in raster image pixels
    val stepPx = remember(settings.gridSizeMode, settings.gridCustomSize, metadata.pixelsPerMeter, metadata.scaleMeters) {
        when {
            settings.gridSizeMode == "custom" -> {
                if (metadata.pixelsPerMeter > 0.0) {
                    settings.gridCustomSize * metadata.pixelsPerMeter
                } else {
                    settings.gridCustomSize.coerceAtLeast(10.0)
                }
            }
            else -> { // "metadata"
                if (metadata.pixelsPerMeter > 0.0 && metadata.scaleMeters > 0.0) {
                    metadata.scaleMeters * metadata.pixelsPerMeter
                } else {
                    100.0 // Default 100px for uncalibrated maps
                }
            }
        }.coerceAtLeast(10.0)
    }

    val imgW = metadata.imageWidth.toDouble()
    val imgH = metadata.imageHeight.toDouble()
    val maxZoom = metadata.zoomMax

    Canvas(modifier = modifier.fillMaxSize().clipToBounds()) {
        // Read cameraVersion to trigger recomposition during camera pan / zoom / rotation
        val version = cameraVersion
        val projection = map.projection ?: return@Canvas

        // Calculate step size in screen pixels to avoid rendering overly dense lines on extreme zoom-out
        val latLng0 = CaveMapBounds.imagePixelsToLatLng(0.0, 0.0, metadata.imageWidth, metadata.imageHeight, maxZoom)
        val latLngStep = CaveMapBounds.imagePixelsToLatLng(stepPx, 0.0, metadata.imageWidth, metadata.imageHeight, maxZoom)
        val pt0 = projection.toScreenLocation(latLng0)
        val ptStep = projection.toScreenLocation(latLngStep)
        val dx = (ptStep.x - pt0.x).toDouble()
        val dy = (ptStep.y - pt0.y).toDouble()
        val stepScreenPx = sqrt(dx * dx + dy * dy)

        if (stepScreenPx < 8.0) return@Canvas // Avoid performance drop at extreme zoom-out

        val strokeWidthPx = 1.dp.toPx()

        clipRect(0f, 0f, size.width, size.height) {
            // 1. Vertical Grid Lines (X = curX from Y=0 to Y=imgH)
            var curX = 0.0
            while (curX <= imgW) {
                val topLatLng = CaveMapBounds.imagePixelsToLatLng(curX, 0.0, metadata.imageWidth, metadata.imageHeight, maxZoom)
                val botLatLng = CaveMapBounds.imagePixelsToLatLng(curX, imgH, metadata.imageWidth, metadata.imageHeight, maxZoom)
                val ptTop = projection.toScreenLocation(topLatLng)
                val ptBot = projection.toScreenLocation(botLatLng)

                drawLine(
                    color = gridColor,
                    start = Offset(ptTop.x, ptTop.y),
                    end = Offset(ptBot.x, ptBot.y),
                    strokeWidth = strokeWidthPx
                )
                curX += stepPx
            }

            // 2. Horizontal Grid Lines (Y = curY from X=0 to X=imgW)
            var curY = 0.0
            while (curY <= imgH) {
                val leftLatLng = CaveMapBounds.imagePixelsToLatLng(0.0, curY, metadata.imageWidth, metadata.imageHeight, maxZoom)
                val rightLatLng = CaveMapBounds.imagePixelsToLatLng(imgW, curY, metadata.imageWidth, metadata.imageHeight, maxZoom)
                val ptLeft = projection.toScreenLocation(leftLatLng)
                val ptRight = projection.toScreenLocation(rightLatLng)

                drawLine(
                    color = gridColor,
                    start = Offset(ptLeft.x, ptLeft.y),
                    end = Offset(ptRight.x, ptRight.y),
                    strokeWidth = strokeWidthPx
                )
                curY += stepPx
            }
        }
    }
}
