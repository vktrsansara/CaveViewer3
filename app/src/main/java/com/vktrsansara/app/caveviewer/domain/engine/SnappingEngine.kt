package com.vktrsansara.app.caveviewer.domain.engine

import androidx.compose.ui.geometry.Offset
import com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils
import com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds
import com.vktrsansara.app.caveviewer.domain.model.IntersectionMode
import com.vktrsansara.app.caveviewer.domain.model.LayerLine
import com.vktrsansara.app.caveviewer.domain.model.LayerPoint
import com.vktrsansara.app.caveviewer.domain.model.SnappingSettings
import org.maplibre.android.geometry.LatLng
import kotlin.math.pow
import kotlin.math.sqrt

sealed class SnapTarget {
    abstract val px: Pair<Double, Double>
    abstract val screenOffset: Offset
    abstract val title: String

    data class Vertex(
        override val px: Pair<Double, Double>,
        override val screenOffset: Offset,
        val lineName: String
    ) : SnapTarget() {
        override val title: String get() = if (lineName.isNotBlank()) "Вершина: $lineName" else "Вершина линии"
    }

    data class Edge(
        override val px: Pair<Double, Double>,
        override val screenOffset: Offset,
        val lineName: String
    ) : SnapTarget() {
        override val title: String get() = if (lineName.isNotBlank()) "Ребро: $lineName" else "Ребро линии"
    }

    data class Point(
        override val px: Pair<Double, Double>,
        override val screenOffset: Offset,
        val pointName: String
    ) : SnapTarget() {
        override val title: String get() = if (pointName.isNotBlank()) "Точка: $pointName" else "Точка слоя"
    }

    data class Intersection(
        override val px: Pair<Double, Double>,
        override val screenOffset: Offset,
        val line1Name: String = "",
        val line2Name: String = ""
    ) : SnapTarget() {
        override val title: String get() = if (line1Name.isNotBlank() && line2Name.isNotBlank()) {
            "Перекресток: $line1Name × $line2Name"
        } else {
            "Перекресток линий"
        }
    }
}

/**
 * Pre-cached line holding image-space bounding box for zero-cost spatial culling.
 */
data class CachedSnapLine(
    val line: LayerLine,
    val minPixelX: Double,
    val maxPixelX: Double,
    val minPixelY: Double,
    val maxPixelY: Double
)

/**
 * Pre-cached point wrapper.
 */
data class CachedSnapPoint(
    val point: LayerPoint
)

/**
 * Pre-computed line intersection in World Pixel Space.
 */
data class CachedIntersection(
    val point: Pair<Double, Double>,
    val line1Name: String,
    val line2Name: String
)

object SnappingEngine {
    const val CURSOR_SNAP_RADIUS_DP = 12f // 12 dp для курсорных режимов
    const val TOUCH_SNAP_RADIUS_DP = 24f  // 24 dp для режима FREE_TAP (прямое касание пальцем)

    /**
     * Converts screen snapping radius (in DP) to raster image pixels based on current camera zoom and density.
     * In MapLibre: scale = 2^(currentZoom - zoomMax)
     * snapRadiusImagePx = snapRadiusScreenPx / 2^(currentZoom - zoomMax)
     */
    fun calculateSnapRadiusImagePx(
        snapRadiusDp: Float,
        density: Float,
        currentZoom: Double,
        zoomMax: Int
    ): Double {
        val snapRadiusScreenPx = snapRadiusDp * density
        val scale = 2.0.pow(currentZoom - zoomMax)
        return if (scale > 0.0) snapRadiusScreenPx / scale else snapRadiusScreenPx.toDouble()
    }

    /**
     * Builds cached lines with pre-computed image bounding box.
     * Must be called ONCE per layer dataset change, not per frame.
     */
    fun buildCachedLines(
        visibleLines: List<LayerLine>,
        imageWidth: Int = 0,
        imageHeight: Int = 0,
        zoomMax: Int = 0
    ): List<CachedSnapLine> {
        return visibleLines.map { line ->
            var minX = Double.MAX_VALUE
            var maxX = -Double.MAX_VALUE
            var minY = Double.MAX_VALUE
            var maxY = -Double.MAX_VALUE
            for (pt in line.points) {
                if (pt.first < minX) minX = pt.first
                if (pt.first > maxX) maxX = pt.first
                if (pt.second < minY) minY = pt.second
                if (pt.second > maxY) maxY = pt.second
            }
            CachedSnapLine(
                line = line,
                minPixelX = if (minX != Double.MAX_VALUE) minX else 0.0,
                maxPixelX = if (maxX != -Double.MAX_VALUE) maxX else 0.0,
                minPixelY = if (minY != Double.MAX_VALUE) minY else 0.0,
                maxPixelY = if (maxY != -Double.MAX_VALUE) maxY else 0.0
            )
        }
    }

    /**
     * Builds cached points.
     * Must be called ONCE per layer dataset change, not per frame.
     */
    fun buildCachedPoints(
        visiblePoints: List<LayerPoint>,
        imageWidth: Int = 0,
        imageHeight: Int = 0,
        zoomMax: Int = 0
    ): List<CachedSnapPoint> {
        return visiblePoints.map { CachedSnapPoint(it) }
    }

    /**
     * Pre-computes all segment-segment intersections between visible lines in World Pixel Space.
     * O(N^2) complexity runs ONLY when layers change, eliminating 120 FPS UI-thread freezing.
     */
    fun buildCachedIntersections(
        visibleLines: List<LayerLine>,
        imageWidth: Int = 0,
        imageHeight: Int = 0,
        zoomMax: Int = 0
    ): List<CachedIntersection> {
        val result = mutableListOf<CachedIntersection>()
        for (i in 0 until visibleLines.size) {
            val l1 = visibleLines[i]
            val pts1 = l1.points
            for (si in 0 until pts1.size - 1) {
                val a1 = pts1[si]
                val a2 = pts1[si + 1]

                for (j in i until visibleLines.size) {
                    val l2 = visibleLines[j]
                    val pts2 = l2.points
                    val startSj = if (i == j) si + 2 else 0
                    for (sj in startSj until pts2.size - 1) {
                        val b1 = pts2[sj]
                        val b2 = pts2[sj + 1]

                        val ix = MeasureUtils.findSegmentIntersection(a1, a2, b1, b2)
                        if (ix != null) {
                            result.add(
                                CachedIntersection(
                                    point = ix,
                                    line1Name = l1.name,
                                    line2Name = l2.name
                                )
                            )
                        }
                    }
                }
            }
        }
        return result
    }

    /**
     * Ultra-fast magnetic snapping target search in World Pixel Space.
     * All Euclidean distance checks, bounding box culling, and projections are performed in 2D raster pixels.
     * ZERO calls to [CaveMapBounds.imagePixelsToLatLng] or [projector] occur inside the search loops.
     * [projector] is called AT MOST ONCE at the very end to compute screenOffset for the winning target.
     */
    fun findSnapTarget(
        cursorImagePx: Pair<Double, Double>,
        snapRadiusImagePx: Double,
        cachedLines: List<CachedSnapLine>,
        cachedPoints: List<CachedSnapPoint>,
        cachedIntersections: List<CachedIntersection>,
        settings: SnappingSettings,
        imageWidth: Int,
        imageHeight: Int,
        zoomMax: Int,
        projector: ((LatLng) -> Offset)? = null,
        forPointCreation: Boolean = false
    ): SnapTarget? {
        if (!settings.isEnabled || snapRadiusImagePx <= 0.0) return null
        val radiusSq = snapRadiusImagePx * snapRadiusImagePx

        val allowLines = !forPointCreation || settings.snapPointsToLines

        val minX = cursorImagePx.first - snapRadiusImagePx
        val maxX = cursorImagePx.first + snapRadiusImagePx
        val minY = cursorImagePx.second - snapRadiusImagePx
        val maxY = cursorImagePx.second + snapRadiusImagePx

        // 1. Приоритет 1: Привязка к вершинам линий
        if (settings.snapToVertices && allowLines) {
            var closestVertex: Pair<Double, Double>? = null
            var lineName = ""
            var minDistanceSq = Double.MAX_VALUE

            for (cachedLine in cachedLines) {
                // Bounding Box Culling на уровне всей линии в растровом пространстве
                if (cachedLine.maxPixelX < minX || cachedLine.minPixelX > maxX ||
                    cachedLine.maxPixelY < minY || cachedLine.minPixelY > maxY) {
                    continue
                }

                for (pt in cachedLine.line.points) {
                    if (pt.first < minX || pt.first > maxX ||
                        pt.second < minY || pt.second > maxY) {
                        continue
                    }
                    val dx = cursorImagePx.first - pt.first
                    val dy = cursorImagePx.second - pt.second
                    val distSq = dx * dx + dy * dy
                    if (distSq <= radiusSq && distSq < minDistanceSq) {
                        minDistanceSq = distSq
                        closestVertex = pt
                        lineName = cachedLine.line.name
                    }
                }
            }

            if (closestVertex != null) {
                val screenOffset = projectWinnerToScreen(closestVertex, imageWidth, imageHeight, zoomMax, projector)
                return SnapTarget.Vertex(closestVertex, screenOffset, lineName)
            }
        }

        // 2. Приоритет 2: Привязка к перекресткам существующих линий (пересечениям отрезков)
        if (allowLines && settings.intersectionMode != IntersectionMode.NO) {
            var closestIntersection: Pair<Double, Double>? = null
            var line1Name = ""
            var line2Name = ""
            var minDistanceSq = Double.MAX_VALUE

            for (ix in cachedIntersections) {
                if (ix.point.first < minX || ix.point.first > maxX ||
                    ix.point.second < minY || ix.point.second > maxY) {
                    continue
                }
                val dx = cursorImagePx.first - ix.point.first
                val dy = cursorImagePx.second - ix.point.second
                val distSq = dx * dx + dy * dy
                if (distSq <= radiusSq && distSq < minDistanceSq) {
                    minDistanceSq = distSq
                    closestIntersection = ix.point
                    line1Name = ix.line1Name
                    line2Name = ix.line2Name
                }
            }

            if (closestIntersection != null) {
                val screenOffset = projectWinnerToScreen(closestIntersection, imageWidth, imageHeight, zoomMax, projector)
                return SnapTarget.Intersection(closestIntersection, screenOffset, line1Name, line2Name)
            }
        }

        // 3. Приоритет 3: Привязка к точкам видимых слоев
        if (settings.snapToPoints) {
            var closestPoint: LayerPoint? = null
            var minDistanceSq = Double.MAX_VALUE

            for (cp in cachedPoints) {
                val p = cp.point
                if (p.x < minX || p.x > maxX ||
                    p.y < minY || p.y > maxY) {
                    continue
                }
                val dx = cursorImagePx.first - p.x
                val dy = cursorImagePx.second - p.y
                val distSq = dx * dx + dy * dy
                if (distSq <= radiusSq && distSq < minDistanceSq) {
                    minDistanceSq = distSq
                    closestPoint = p
                }
            }

            if (closestPoint != null) {
                val ptPx = Pair(closestPoint.x, closestPoint.y)
                val screenOffset = projectWinnerToScreen(ptPx, imageWidth, imageHeight, zoomMax, projector)
                return SnapTarget.Point(ptPx, screenOffset, closestPoint.name)
            }
        }

        // 4. Приоритет 4: Привязка к ребрам (проекция на отрезок)
        if (settings.snapToEdges && allowLines) {
            var closestEdgePt: Pair<Double, Double>? = null
            var lineName = ""
            var minDistanceSq = Double.MAX_VALUE

            for (cachedLine in cachedLines) {
                if (cachedLine.maxPixelX < minX || cachedLine.minPixelX > maxX ||
                    cachedLine.maxPixelY < minY || cachedLine.minPixelY > maxY) {
                    continue
                }

                val verts = cachedLine.line.points
                for (i in 0 until verts.size - 1) {
                    val v1 = verts[i]
                    val v2 = verts[i + 1]

                    val segMinX = minOf(v1.first, v2.first)
                    val segMaxX = maxOf(v1.first, v2.first)
                    val segMinY = minOf(v1.second, v2.second)
                    val segMaxY = maxOf(v1.second, v2.second)

                    if (segMaxX < minX || segMinX > maxX || segMaxY < minY || segMinY > maxY) {
                        continue
                    }

                    val projResult = projectPointToSegmentPixel(cursorImagePx, v1, v2)
                    val projPt = projResult.first

                    val dx = cursorImagePx.first - projPt.first
                    val dy = cursorImagePx.second - projPt.second
                    val distSq = dx * dx + dy * dy

                    if (distSq <= radiusSq && distSq < minDistanceSq) {
                        minDistanceSq = distSq
                        closestEdgePt = projPt
                        lineName = cachedLine.line.name
                    }
                }
            }

            if (closestEdgePt != null) {
                val screenOffset = projectWinnerToScreen(closestEdgePt, imageWidth, imageHeight, zoomMax, projector)
                return SnapTarget.Edge(closestEdgePt, screenOffset, lineName)
            }
        }

        return null
    }

    /**
     * Alias for findSnapTarget for backward compatibility.
     */
    fun findSnapTargetFast(
        cursorImagePx: Pair<Double, Double>,
        snapRadiusImagePx: Double,
        cachedLines: List<CachedSnapLine>,
        cachedPoints: List<CachedSnapPoint>,
        cachedIntersections: List<CachedIntersection>,
        settings: SnappingSettings,
        imageWidth: Int,
        imageHeight: Int,
        zoomMax: Int,
        projector: ((LatLng) -> Offset)? = null,
        forPointCreation: Boolean = false
    ): SnapTarget? = findSnapTarget(
        cursorImagePx = cursorImagePx,
        snapRadiusImagePx = snapRadiusImagePx,
        cachedLines = cachedLines,
        cachedPoints = cachedPoints,
        cachedIntersections = cachedIntersections,
        settings = settings,
        imageWidth = imageWidth,
        imageHeight = imageHeight,
        zoomMax = zoomMax,
        projector = projector,
        forPointCreation = forPointCreation
    )

    private fun projectWinnerToScreen(
        px: Pair<Double, Double>,
        imageWidth: Int,
        imageHeight: Int,
        zoomMax: Int,
        projector: ((LatLng) -> Offset)?
    ): Offset {
        if (projector == null || imageWidth <= 0 || imageHeight <= 0) return Offset.Zero
        val latLng = CaveMapBounds.imagePixelsToLatLng(
            pixelX = px.first,
            pixelY = px.second,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            maxZoom = zoomMax
        )
        return projector(latLng)
    }

    private fun projectPointToSegmentPixel(
        p: Pair<Double, Double>,
        a: Pair<Double, Double>,
        b: Pair<Double, Double>
    ): Pair<Pair<Double, Double>, Double> {
        val dx = b.first - a.first
        val dy = b.second - a.second
        val lenSq = dx * dx + dy * dy
        if (lenSq == 0.0) return Pair(a, 0.0)
        val t = (((p.first - a.first) * dx + (p.second - a.second) * dy) / lenSq).coerceIn(0.0, 1.0)
        val proj = Pair(
            a.first + t * dx,
            a.second + t * dy
        )
        return Pair(proj, t)
    }
}
