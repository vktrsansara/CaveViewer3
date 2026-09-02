package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.TextLayoutResult
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
import org.maplibre.android.geometry.LatLngBounds

/**
 * Pre-cached structure holding unchanging geographic LatLng and hazard state for each point.
 * Computed ONLY once when point list or raster dimensions change, avoiding trigonometrical
 * calculations (pow, atan, exp) on every frame.
 */
private data class CachedPoint(
    val point: LayerPoint,
    val latLng: LatLng,
    val lat: Double,
    val lon: Double,
    val isHazard: Boolean
)

private data class RenderedPoint(
    val point: LayerPoint,
    val layer: PointLayer,
    val screenOffset: Offset,
    val isHazard: Boolean
)

@Composable
fun PointLayersOverlay(
    pointLayers: List<PointLayer>,
    allPoints: List<LayerPoint>,
    imageWidth: Int,
    imageHeight: Int,
    zoomMax: Int,
    projector: ((LatLng) -> Offset)?,
    visibleBoundsProvider: (() -> LatLngBounds?)? = null,
    currentTargetLat: Double,
    currentTargetLon: Double,
    currentZoom: Double,
    mapBearing: Double,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val layerMap = remember(pointLayers) { pointLayers.associateBy { it.id } }

    val textPrimaryColor = AppColors.textPrimary
    val textStyle = remember(textPrimaryColor) {
        TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textPrimaryColor
        )
    }

    // 1. Pre-cache LatLng and hazard flag (1 calculation per point, NOT 60fps)
    val cachedPoints = remember(allPoints, imageWidth, imageHeight, zoomMax) {
        if (imageWidth <= 0 || imageHeight <= 0 || zoomMax <= 0) {
            emptyList()
        } else {
            allPoints.map { pt ->
                val isHazard = pt.customValues.entries.any { (k, v) ->
                    (k.contains("hazard", ignoreCase = true) ||
                     k.contains("опасн", ignoreCase = true) ||
                     k.contains("danger", ignoreCase = true)) &&
                    v.equals("true", ignoreCase = true)
                }
                val latLng = CaveMapBounds.imagePixelsToLatLng(
                    pixelX = pt.x,
                    pixelY = pt.y,
                    imageWidth = imageWidth,
                    imageHeight = imageHeight,
                    maxZoom = zoomMax
                )
                CachedPoint(
                    point = pt,
                    latLng = latLng,
                    lat = latLng.latitude,
                    lon = latLng.longitude,
                    isHazard = isHazard
                )
            }
        }
    }

    // 2. Pre-measure distinct point label layouts (avoid re-measuring text in Canvas loop)
    val labelLayoutMap = remember(allPoints, textStyle) {
        val map = HashMap<String, TextLayoutResult>(allPoints.size)
        allPoints.forEach { pt ->
            if (pt.name.isNotBlank() && !map.containsKey(pt.name)) {
                map[pt.name] = textMeasurer.measure(
                    text = pt.name,
                    style = textStyle
                )
            }
        }
        map
    }

    // 3. Fast screen projection per frame with Geo Culling before JNI
    val renderedPoints = remember(
        cachedPoints,
        layerMap,
        projector,
        visibleBoundsProvider,
        currentTargetLat,
        currentTargetLon,
        currentZoom,
        mapBearing
    ) {
        if (projector == null || cachedPoints.isEmpty()) {
            emptyList()
        } else {
            val visibleBounds = visibleBoundsProvider?.invoke()
            val (minVisLat, maxVisLat, minVisLon, maxVisLon) = if (visibleBounds != null) {
                val latMargin = (visibleBounds.latitudeNorth - visibleBounds.latitudeSouth).coerceAtLeast(0.0001) * 0.20
                val lonMargin = (visibleBounds.longitudeEast - visibleBounds.longitudeWest).coerceAtLeast(0.0001) * 0.20
                arrayOf(
                    visibleBounds.latitudeSouth - latMargin,
                    visibleBounds.latitudeNorth + latMargin,
                    visibleBounds.longitudeWest - lonMargin,
                    visibleBounds.longitudeEast + lonMargin
                )
            } else {
                arrayOf(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
            }

            val result = ArrayList<RenderedPoint>(cachedPoints.size.coerceAtMost(100))
            for (i in cachedPoints.indices) {
                val cp = cachedPoints[i]
                val layer = layerMap[cp.point.layerId]
                if (layer != null && layer.isVisible) {
                    // Fast Geo Culling before JNI
                    if (cp.lat < minVisLat || cp.lat > maxVisLat ||
                        cp.lon < minVisLon || cp.lon > maxVisLon) {
                        continue
                    }

                    val screenOffset = projector(cp.latLng)
                    if (screenOffset.x.isFinite() && screenOffset.y.isFinite()) {
                        result.add(RenderedPoint(cp.point, layer, screenOffset, cp.isHazard))
                    }
                }
            }
            result
        }
    }

    val bgCardColor = AppColors.bgCard
    val borderColor = AppColors.borderColor

    Canvas(modifier = modifier.fillMaxSize().clipToBounds()) {
        clipRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height
        ) {
            val screenW = size.width
            val screenH = size.height
            val margin = 80.dp.toPx()

            val badgePaddingX = 5.dp.toPx()
            val badgePaddingY = 2.dp.toPx()
            val cornerRadiusPx = CornerRadius(4.dp.toPx())
            val strokeWidthPx = Stroke(width = 1.dp.toPx())

            for (i in renderedPoints.indices) {
                val item = renderedPoints[i]
                val screenOffset = item.screenOffset

                // Viewport Culling: skip points outside visible screen with margin
                if (screenOffset.x < -margin || screenOffset.x > screenW + margin ||
                    screenOffset.y < -margin || screenOffset.y > screenH + margin) {
                    continue
                }

                val point = item.point
                val layer = item.layer
                val markerSizePx = layer.defaultSize.dp.toPx()

                // 1. Draw shape marker
                drawPointShape(
                    shape = point.shape,
                    center = screenOffset,
                    sizePx = markerSizePx,
                    fillColor = Color(point.color.toInt()),
                    strokeColor = Color.Black,
                    isHazard = item.isHazard
                )

                // 2. Draw pre-measured label badge if enabled for layer
                if (layer.showLabels && point.name.isNotBlank()) {
                    val textLayoutResult = labelLayoutMap[point.name]
                    if (textLayoutResult != null) {
                        val textW = textLayoutResult.size.width.toFloat()
                        val textH = textLayoutResult.size.height.toFloat()

                        val badgeTopLeft = Offset(
                            x = screenOffset.x + markerSizePx + 6.dp.toPx(),
                            y = screenOffset.y - (textH / 2f) - badgePaddingY
                        )

                        val badgeSize = Size(
                            width = textW + badgePaddingX * 2,
                            height = textH + badgePaddingY * 2
                        )

                        // Background badge
                        drawRoundRect(
                            color = bgCardColor.copy(alpha = 0.85f),
                            topLeft = badgeTopLeft,
                            size = badgeSize,
                            cornerRadius = cornerRadiusPx
                        )

                        // Border
                        drawRoundRect(
                            color = borderColor.copy(alpha = 0.6f),
                            topLeft = badgeTopLeft,
                            size = badgeSize,
                            cornerRadius = cornerRadiusPx,
                            style = strokeWidthPx
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
    }
}

