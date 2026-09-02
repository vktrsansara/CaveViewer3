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
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.domain.measure.LineColorUtils
import com.vktrsansara.app.caveviewer.domain.model.LayerLine
import com.vktrsansara.app.caveviewer.domain.model.LineEnvironmentType
import com.vktrsansara.app.caveviewer.domain.model.LineLayer
import com.vktrsansara.app.caveviewer.domain.model.LineStyle
import com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds

/**
 * Pre-cached polyline holding unchanging geographic LatLng vertices and AABB bounds.
 * Computed ONLY once per dataset update, eliminating millions of trigonometrical calculations per second.
 */
private data class CachedLine(
    val line: LayerLine,
    val latLngPoints: List<LatLng>,
    val minLat: Double,
    val maxLat: Double,
    val minLon: Double,
    val maxLon: Double
)

/**
 * Data class representing projected polyline with screen bounding box for sub-millisecond Viewport Culling.
 */
private data class ProjectedLine(
    val line: LayerLine,
    val layer: LineLayer,
    val path: Path,
    val screenPoints: List<Offset>,
    val minX: Float,
    val maxX: Float,
    val minY: Float,
    val maxY: Float,
    val isSelected: Boolean
)

/**
 * High-performance vector overlay for displaying all visible cave line layers and polylines.
 * Supports:
 * - Pre-cached LatLng vertices with fast screen projection
 * - Geo AABB & Screen Viewport Culling (skips off-screen geometry & JNI calls)
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
    visibleBoundsProvider: (() -> LatLngBounds?)? = null,
    currentTargetLat: Double,
    currentTargetLon: Double,
    currentZoom: Double,
    mapBearing: Double,
    modifier: Modifier = Modifier
) {
    val visibleLayersMap = remember(lineLayers) {
        lineLayers.filter { it.isVisible }.associateBy { it.id }
    }

    // 1. Pre-cache LatLng points and Geo Bounding Box for each line (1 computation per line vertex, NOT 60fps)
    val cachedLines = remember(allLines, imageWidth, imageHeight, zoomMax) {
        if (imageWidth <= 0 || imageHeight <= 0 || zoomMax <= 0) {
            emptyList()
        } else {
            allLines.mapNotNull { line ->
                if (line.points.size < 2) return@mapNotNull null
                var minLat = Double.MAX_VALUE
                var maxLat = -Double.MAX_VALUE
                var minLon = Double.MAX_VALUE
                var maxLon = -Double.MAX_VALUE

                val latLngs = line.points.map { pt ->
                    val latLng = CaveMapBounds.imagePixelsToLatLng(
                        pixelX = pt.first,
                        pixelY = pt.second,
                        imageWidth = imageWidth,
                        imageHeight = imageHeight,
                        maxZoom = zoomMax
                    )
                    if (latLng.latitude < minLat) minLat = latLng.latitude
                    if (latLng.latitude > maxLat) maxLat = latLng.latitude
                    if (latLng.longitude < minLon) minLon = latLng.longitude
                    if (latLng.longitude > maxLon) maxLon = latLng.longitude
                    latLng
                }
                CachedLine(
                    line = line,
                    latLngPoints = latLngs,
                    minLat = minLat,
                    maxLat = maxLat,
                    minLon = minLon,
                    maxLon = maxLon
                )
            }
        }
    }

    // 2. Fast screen projection per frame with immediate Geo and Screen Bounding Box culling
    val projectedLines = remember(
        cachedLines,
        visibleLayersMap,
        projector,
        visibleBoundsProvider,
        currentTargetLat,
        currentTargetLon,
        currentZoom,
        mapBearing,
        selectedLineId
    ) {
        if (projector == null || cachedLines.isEmpty()) {
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

            val result = ArrayList<ProjectedLine>(cachedLines.size.coerceAtMost(120))
            for (k in cachedLines.indices) {
                val cl = cachedLines[k]
                val layer = visibleLayersMap[cl.line.layerId] ?: continue

                // Fast Geo Bounding Box Culling: skips off-screen lines without calling JNI!
                if (cl.maxLat < minVisLat || cl.minLat > maxVisLat ||
                    cl.maxLon < minVisLon || cl.minLon > maxVisLon) {
                    continue
                }

                val pts = cl.latLngPoints

                var minX = Float.MAX_VALUE
                var maxX = -Float.MAX_VALUE
                var minY = Float.MAX_VALUE
                var maxY = -Float.MAX_VALUE

                val screenPoints = ArrayList<Offset>(pts.size)
                for (i in pts.indices) {
                    val sp = projector(pts[i])
                    screenPoints.add(sp)
                    if (sp.x < minX) minX = sp.x
                    if (sp.x > maxX) maxX = sp.x
                    if (sp.y < minY) minY = sp.y
                    if (sp.y > maxY) maxY = sp.y
                }

                // Generous screen bounding box check (-150..3500 px) to avoid Path creation for far-off lines
                if (maxX < -150f || minX > 3500f || maxY < -150f || minY > 3500f) {
                    continue
                }

                val path = Path().apply {
                    moveTo(screenPoints[0].x, screenPoints[0].y)
                    for (i in 1 until screenPoints.size) {
                        lineTo(screenPoints[i].x, screenPoints[i].y)
                    }
                }

                result.add(
                    ProjectedLine(
                        line = cl.line,
                        layer = layer,
                        path = path,
                        screenPoints = screenPoints,
                        minX = minX,
                        maxX = maxX,
                        minY = minY,
                        maxY = maxY,
                        isSelected = (cl.line.id == selectedLineId)
                    )
                )
            }
            result
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
            val screenW = size.width
            val screenH = size.height
            val margin = 80.dp.toPx()

            // 1. Draw Selection Glow on selected line
            for (i in projectedLines.indices) {
                val item = projectedLines[i]
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

            // 2. Draw Core Line Strokes with Viewport Culling
            for (i in projectedLines.indices) {
                val item = projectedLines[i]

                // Viewport Culling: skip lines outside visible screen
                if (item.maxX < -margin || item.minX > screenW + margin ||
                    item.maxY < -margin || item.minY > screenH + margin) {
                    continue
                }

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

            // 3. Draw Vector Topographic Ticks / Hatches (UIS / Therion Standard) with Viewport Culling
            for (i in projectedLines.indices) {
                val item = projectedLines[i]
                if (item.line.environmentType != LineEnvironmentType.NONE) {
                    if (item.maxX < -margin || item.minX > screenW + margin ||
                        item.maxY < -margin || item.minY > screenH + margin) {
                        continue
                    }

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

