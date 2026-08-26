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
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.domain.model.AppSettings
import com.vktrsansara.app.caveviewer.domain.model.MapMetadata
import com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds
import org.maplibre.android.maps.MapLibreMap
import kotlin.math.sqrt

/**
 * Visual canvas overlay rendering coordinate grid lines over the cave map image.
 * The grid remains horizontal/vertical relative to the screen (does not rotate with camera bearing),
 * is anchored to top-left corner (0, 0), and is strictly clipped to the map image boundary.
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
        Color(settings.gridColor)
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

        // 1. Calculate the 4 image corners in screen coordinates to create a clip boundary
        val latLngTL = CaveMapBounds.imagePixelsToLatLng(0.0, 0.0, metadata.imageWidth, metadata.imageHeight, maxZoom)
        val latLngTR = CaveMapBounds.imagePixelsToLatLng(imgW, 0.0, metadata.imageWidth, metadata.imageHeight, maxZoom)
        val latLngBR = CaveMapBounds.imagePixelsToLatLng(imgW, imgH, metadata.imageWidth, metadata.imageHeight, maxZoom)
        val latLngBL = CaveMapBounds.imagePixelsToLatLng(0.0, imgH, metadata.imageWidth, metadata.imageHeight, maxZoom)

        val ptTL = projection.toScreenLocation(latLngTL)
        val ptTR = projection.toScreenLocation(latLngTR)
        val ptBR = projection.toScreenLocation(latLngBR)
        val ptBL = projection.toScreenLocation(latLngBL)

        val clipBoundary = Path().apply {
            moveTo(ptTL.x, ptTL.y)
            lineTo(ptTR.x, ptTR.y)
            lineTo(ptBR.x, ptBR.y)
            lineTo(ptBL.x, ptBL.y)
            close()
        }

        // Calculate step size in screen pixels
        val latLngStep = CaveMapBounds.imagePixelsToLatLng(stepPx, 0.0, metadata.imageWidth, metadata.imageHeight, maxZoom)
        val ptStep = projection.toScreenLocation(latLngStep)
        val dx = (ptStep.x - ptTL.x).toDouble()
        val dy = (ptStep.y - ptTL.y).toDouble()
        val stepScreenPx = sqrt(dx * dx + dy * dy)

        if (stepScreenPx < 8.0) return@Canvas // Avoid excessive line drawing at extreme zoom-out

        val strokeWidthPx = 1.dp.toPx()

        clipRect(0f, 0f, size.width, size.height) {
            clipPath(clipBoundary) {
                // Anchor grid phase to the top-left corner of the map on screen
                val startX = ((ptTL.x % stepScreenPx) + stepScreenPx) % stepScreenPx
                val startY = ((ptTL.y % stepScreenPx) + stepScreenPx) % stepScreenPx

                // Draw non-rotating vertical lines across screen
                var curX = startX.toFloat()
                while (curX <= size.width + stepScreenPx.toFloat()) {
                    drawLine(
                        color = gridColor,
                        start = Offset(curX, 0f),
                        end = Offset(curX, size.height),
                        strokeWidth = strokeWidthPx
                    )
                    curX += stepScreenPx.toFloat()
                }

                // Draw non-rotating horizontal lines across screen
                var curY = startY.toFloat()
                while (curY <= size.height + stepScreenPx.toFloat()) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, curY),
                        end = Offset(size.width, curY),
                        strokeWidth = strokeWidthPx
                    )
                    curY += stepScreenPx.toFloat()
                }
            }
        }
    }
}
