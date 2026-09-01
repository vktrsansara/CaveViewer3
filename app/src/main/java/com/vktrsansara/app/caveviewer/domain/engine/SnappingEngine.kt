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
 * Pre-cached vertex with image pixel coordinates and static geographic LatLng.
 */
data class CachedSnapVertex(
    val pixel: Pair<Double, Double>,
    val latLng: LatLng
)

/**
 * Pre-cached line holding vertices with LatLng and image-space bounding box for zero-cost spatial culling.
 */
data class CachedSnapLine(
    val line: LayerLine,
    val vertices: List<CachedSnapVertex>,
    val minPixelX: Double,
    val maxPixelX: Double,
    val minPixelY: Double,
    val maxPixelY: Double
)

/**
 * Pre-cached point holding image-space coordinates and static geographic LatLng.
 */
data class CachedSnapPoint(
    val point: LayerPoint,
    val latLng: LatLng
)

/**
 * Pre-computed line intersection point (O(N^2) computed ONCE upon dataset update, NOT on camera animation).
 */
data class CachedIntersection(
    val point: Pair<Double, Double>,
    val line1Name: String,
    val line2Name: String,
    val latLng: LatLng
)

object SnappingEngine {
    const val CURSOR_SNAP_RADIUS_DP = 12f // 12 dp для курсорных режимов
    const val TOUCH_SNAP_RADIUS_DP = 24f  // 24 dp для режима FREE_TAP (прямое касание пальцем)

    /**
     * Builds cached lines with pre-computed geographic LatLng and image bounding box.
     * Must be called ONCE per layer dataset change, not per frame.
     */
    fun buildCachedLines(
        visibleLines: List<LayerLine>,
        imageWidth: Int,
        imageHeight: Int,
        zoomMax: Int
    ): List<CachedSnapLine> {
        return visibleLines.map { line ->
            var minX = Double.MAX_VALUE
            var maxX = -Double.MAX_VALUE
            var minY = Double.MAX_VALUE
            var maxY = -Double.MAX_VALUE
            val vertices = line.points.map { pt ->
                if (pt.first < minX) minX = pt.first
                if (pt.first > maxX) maxX = pt.first
                if (pt.second < minY) minY = pt.second
                if (pt.second > maxY) maxY = pt.second
                val latLng = CaveMapBounds.imagePixelsToLatLng(
                    pixelX = pt.first,
                    pixelY = pt.second,
                    imageWidth = imageWidth,
                    imageHeight = imageHeight,
                    maxZoom = zoomMax
                )
                CachedSnapVertex(pt, latLng)
            }
            CachedSnapLine(
                line = line,
                vertices = vertices,
                minPixelX = if (minX != Double.MAX_VALUE) minX else 0.0,
                maxPixelX = if (maxX != -Double.MAX_VALUE) maxX else 0.0,
                minPixelY = if (minY != Double.MAX_VALUE) minY else 0.0,
                maxPixelY = if (maxY != -Double.MAX_VALUE) maxY else 0.0
            )
        }
    }

    /**
     * Builds cached points with pre-computed geographic LatLng.
     * Must be called ONCE per layer dataset change, not per frame.
     */
    fun buildCachedPoints(
        visiblePoints: List<LayerPoint>,
        imageWidth: Int,
        imageHeight: Int,
        zoomMax: Int
    ): List<CachedSnapPoint> {
        return visiblePoints.map { p ->
            val latLng = CaveMapBounds.imagePixelsToLatLng(
                pixelX = p.x,
                pixelY = p.y,
                imageWidth = imageWidth,
                imageHeight = imageHeight,
                maxZoom = zoomMax
            )
            CachedSnapPoint(p, latLng)
        }
    }

    /**
     * Pre-computes all segment-segment intersections between visible lines.
     * O(N^2) complexity runs ONLY when layers change, eliminating 120 FPS UI-thread freezing.
     */
    fun buildCachedIntersections(
        visibleLines: List<LayerLine>,
        imageWidth: Int,
        imageHeight: Int,
        zoomMax: Int
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
                            val latLng = CaveMapBounds.imagePixelsToLatLng(
                                pixelX = ix.first,
                                pixelY = ix.second,
                                imageWidth = imageWidth,
                                imageHeight = imageHeight,
                                maxZoom = zoomMax
                            )
                            result.add(
                                CachedIntersection(
                                    point = ix,
                                    line1Name = l1.name,
                                    line2Name = l2.name,
                                    latLng = latLng
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
     * Ultra-fast sub-millisecond snapping target search for real-time 120 FPS camera animation.
     * Uses:
     * - Image-space spatial bounding box culling (rejects 99% of off-cursor geometry instantly)
     * - Pre-computed LatLng vertices (eliminates all trigonometric calculations during render)
     * - Pre-computed static line intersections (eliminates O(N^2) nested loops during render)
     */
    fun findSnapTargetFast(
        cursorScreenOffset: Offset,
        cursorPixelX: Double,
        cursorPixelY: Double,
        currentZoom: Double,
        zoomMax: Int,
        cachedLines: List<CachedSnapLine>,
        cachedPoints: List<CachedSnapPoint>,
        cachedIntersections: List<CachedIntersection>,
        projector: (LatLng) -> Offset,
        settings: SnappingSettings,
        snapRadiusScreenPx: Float,
        forPointCreation: Boolean = false
    ): SnapTarget? {
        if (!settings.isEnabled || snapRadiusScreenPx <= 0f) return null
        val radiusSq = snapRadiusScreenPx * snapRadiusScreenPx

        val allowLines = !forPointCreation || settings.snapPointsToLines

        val effZoom = if (currentZoom > 0.0) currentZoom else zoomMax.toDouble()
        val pixelScale = 2.0.pow((zoomMax - effZoom).coerceAtLeast(0.0))
        val searchMarginPx = (snapRadiusScreenPx * pixelScale * 2.0).coerceAtLeast(snapRadiusScreenPx.toDouble())

        val minX = cursorPixelX - searchMarginPx
        val maxX = cursorPixelX + searchMarginPx
        val minY = cursorPixelY - searchMarginPx
        val maxY = cursorPixelY + searchMarginPx

        // 1. Приоритет 1: Привязка к вершинам линий
        if (settings.snapToVertices && allowLines) {
            var closestVertex: Pair<Double, Double>? = null
            var closestScreenOffset: Offset? = null
            var lineName = ""
            var minDistanceSq = Float.MAX_VALUE

            for (cachedLine in cachedLines) {
                // Bounding Box Culling на уровне всей линии
                if (cachedLine.maxPixelX < minX || cachedLine.minPixelX > maxX ||
                    cachedLine.maxPixelY < minY || cachedLine.minPixelY > maxY) {
                    continue
                }

                for (v in cachedLine.vertices) {
                    // Culling на уровне вершины в растровом пространстве
                    if (v.pixel.first < minX || v.pixel.first > maxX ||
                        v.pixel.second < minY || v.pixel.second > maxY) {
                        continue
                    }

                    val ptScreen = projector(v.latLng)
                    val dx = cursorScreenOffset.x - ptScreen.x
                    val dy = cursorScreenOffset.y - ptScreen.y
                    val distSq = dx * dx + dy * dy
                    if (distSq <= radiusSq && distSq < minDistanceSq) {
                        minDistanceSq = distSq
                        closestVertex = v.pixel
                        closestScreenOffset = ptScreen
                        lineName = cachedLine.line.name
                    }
                }
            }
            if (closestVertex != null && closestScreenOffset != null) {
                return SnapTarget.Vertex(closestVertex, closestScreenOffset, lineName)
            }
        }

        // 2. Приоритет 2: Привязка к перекресткам существующих линий (пересечениям отрезков)
        if (allowLines && settings.intersectionMode != IntersectionMode.NO) {
            var closestIntersection: Pair<Double, Double>? = null
            var closestScreenOffset: Offset? = null
            var line1Name = ""
            var line2Name = ""
            var minDistanceSq = Float.MAX_VALUE

            for (ix in cachedIntersections) {
                // Culling в растровом пространстве
                if (ix.point.first < minX || ix.point.first > maxX ||
                    ix.point.second < minY || ix.point.second > maxY) {
                    continue
                }

                val ptScreen = projector(ix.latLng)
                val dx = cursorScreenOffset.x - ptScreen.x
                val dy = cursorScreenOffset.y - ptScreen.y
                val distSq = dx * dx + dy * dy
                if (distSq <= radiusSq && distSq < minDistanceSq) {
                    minDistanceSq = distSq
                    closestIntersection = ix.point
                    closestScreenOffset = ptScreen
                    line1Name = ix.line1Name
                    line2Name = ix.line2Name
                }
            }

            if (closestIntersection != null && closestScreenOffset != null) {
                return SnapTarget.Intersection(closestIntersection, closestScreenOffset, line1Name, line2Name)
            }
        }

        // 3. Приоритет 3: Привязка к точкам видимых слоев
        if (settings.snapToPoints) {
            var closestPoint: LayerPoint? = null
            var closestScreenOffset: Offset? = null
            var minDistanceSq = Float.MAX_VALUE

            for (cp in cachedPoints) {
                if (cp.point.x < minX || cp.point.x > maxX ||
                    cp.point.y < minY || cp.point.y > maxY) {
                    continue
                }

                val ptScreen = projector(cp.latLng)
                val dx = cursorScreenOffset.x - ptScreen.x
                val dy = cursorScreenOffset.y - ptScreen.y
                val distSq = dx * dx + dy * dy
                if (distSq <= radiusSq && distSq < minDistanceSq) {
                    minDistanceSq = distSq
                    closestPoint = cp.point
                    closestScreenOffset = ptScreen
                }
            }
            if (closestPoint != null && closestScreenOffset != null) {
                return SnapTarget.Point(Pair(closestPoint.x, closestPoint.y), closestScreenOffset, closestPoint.name)
            }
        }

        // 4. Приоритет 4: Привязка к ребрам (проекция на отрезок)
        if (settings.snapToEdges && allowLines) {
            var closestEdgePt: Pair<Double, Double>? = null
            var closestScreenOffset: Offset? = null
            var lineName = ""
            var minDistanceSq = Float.MAX_VALUE

            for (cachedLine in cachedLines) {
                if (cachedLine.maxPixelX < minX || cachedLine.minPixelX > maxX ||
                    cachedLine.maxPixelY < minY || cachedLine.minPixelY > maxY) {
                    continue
                }

                val verts = cachedLine.vertices
                for (i in 0 until verts.size - 1) {
                    val v1 = verts[i]
                    val v2 = verts[i + 1]

                    val segMinX = minOf(v1.pixel.first, v2.pixel.first)
                    val segMaxX = maxOf(v1.pixel.first, v2.pixel.first)
                    val segMinY = minOf(v1.pixel.second, v2.pixel.second)
                    val segMaxY = maxOf(v1.pixel.second, v2.pixel.second)

                    if (segMaxX < minX || segMinX > maxX || segMaxY < minY || segMinY > maxY) {
                        continue
                    }

                    val p1Screen = projector(v1.latLng)
                    val p2Screen = projector(v2.latLng)

                    val projResult = projectPointToSegmentScreen(cursorScreenOffset, p1Screen, p2Screen)
                    val projScreen = projResult.first
                    val t = projResult.second

                    val dx = cursorScreenOffset.x - projScreen.x
                    val dy = cursorScreenOffset.y - projScreen.y
                    val distSq = dx * dx + dy * dy

                    if (distSq <= radiusSq && distSq < minDistanceSq) {
                        minDistanceSq = distSq
                        closestScreenOffset = projScreen
                        closestEdgePt = Pair(
                            v1.pixel.first + t * (v2.pixel.first - v1.pixel.first),
                            v1.pixel.second + t * (v2.pixel.second - v1.pixel.second)
                        )
                        lineName = cachedLine.line.name
                    }
                }
            }
            if (closestEdgePt != null && closestScreenOffset != null) {
                return SnapTarget.Edge(closestEdgePt, closestScreenOffset, lineName)
            }
        }

        return null
    }

    /**
     * Legacy backward-compatible signature.
     */
    fun findSnapTarget(
        cursorScreenOffset: Offset,
        visibleLines: List<LayerLine>,
        visiblePoints: List<LayerPoint>,
        imageWidth: Int,
        imageHeight: Int,
        zoomMax: Int,
        projector: (LatLng) -> Offset,
        settings: SnappingSettings,
        snapRadiusScreenPx: Float,
        forPointCreation: Boolean = false
    ): SnapTarget? {
        val cachedLines = buildCachedLines(visibleLines, imageWidth, imageHeight, zoomMax)
        val cachedPoints = buildCachedPoints(visiblePoints, imageWidth, imageHeight, zoomMax)
        val cachedIntersections = if (settings.intersectionMode != IntersectionMode.NO) {
            buildCachedIntersections(visibleLines, imageWidth, imageHeight, zoomMax)
        } else emptyList()

        return findSnapTargetFast(
            cursorScreenOffset = cursorScreenOffset,
            cursorPixelX = (imageWidth / 2).toDouble(),
            cursorPixelY = (imageHeight / 2).toDouble(),
            currentZoom = 0.0,
            zoomMax = zoomMax,
            cachedLines = cachedLines,
            cachedPoints = cachedPoints,
            cachedIntersections = cachedIntersections,
            projector = projector,
            settings = settings,
            snapRadiusScreenPx = snapRadiusScreenPx,
            forPointCreation = forPointCreation
        )
    }

    private fun projectPointToSegmentScreen(
        p: Offset,
        a: Offset,
        b: Offset
    ): Pair<Offset, Double> {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val lenSq = dx * dx + dy * dy
        if (lenSq == 0f) return Pair(a, 0.0)
        val t = (((p.x - a.x) * dx + (p.y - a.y) * dy) / lenSq).coerceIn(0f, 1f).toDouble()
        val proj = Offset(
            (a.x + t * dx).toFloat(),
            (a.y + t * dy).toFloat()
        )
        return Pair(proj, t)
    }
}
