package com.vktrsansara.app.caveviewer.domain.engine

import androidx.compose.ui.geometry.Offset
import com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils
import com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds
import com.vktrsansara.app.caveviewer.domain.model.IntersectionMode
import com.vktrsansara.app.caveviewer.domain.model.LayerLine
import com.vktrsansara.app.caveviewer.domain.model.LayerPoint
import com.vktrsansara.app.caveviewer.domain.model.SnappingSettings
import org.maplibre.android.geometry.LatLng
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

object SnappingEngine {
    const val CURSOR_SNAP_RADIUS_DP = 12f // 12 dp для курсорных режимов
    const val TOUCH_SNAP_RADIUS_DP = 24f  // 24 dp для режима FREE_TAP (прямое касание пальцем)

    /**
     * Ищет ближайшую точку привязки (вершину, точку или ребро) среди всех видимых слоев.
     * Расстояние рассчитывается в экранных пикселях (Screen Space) от точки визира/касания.
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
        snapRadiusScreenPx: Float, // Точный радиус захвата в экранных пикселях (например, 4 dp * density)
        forPointCreation: Boolean = false
    ): SnapTarget? {
        if (!settings.isEnabled || snapRadiusScreenPx <= 0f) return null
        val radiusSq = snapRadiusScreenPx * snapRadiusScreenPx

        val allowLines = !forPointCreation || settings.snapPointsToLines

        // 1. Приоритет 1: Привязка к вершинам линий
        if (settings.snapToVertices && allowLines) {
            var closestVertex: Pair<Double, Double>? = null
            var closestScreenOffset: Offset? = null
            var lineName = ""
            var minDistanceSq = Float.MAX_VALUE

            for (line in visibleLines) {
                for (pt in line.points) {
                    val latLng = CaveMapBounds.imagePixelsToLatLng(
                        pixelX = pt.first,
                        pixelY = pt.second,
                        imageWidth = imageWidth,
                        imageHeight = imageHeight,
                        maxZoom = zoomMax
                    )
                    val ptScreen = projector(latLng)
                    val dx = cursorScreenOffset.x - ptScreen.x
                    val dy = cursorScreenOffset.y - ptScreen.y
                    val distSq = dx * dx + dy * dy
                    if (distSq <= radiusSq && distSq < minDistanceSq) {
                        minDistanceSq = distSq
                        closestVertex = pt
                        closestScreenOffset = ptScreen
                        lineName = line.name
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
                                val ptScreen = projector(latLng)
                                val dx = cursorScreenOffset.x - ptScreen.x
                                val dy = cursorScreenOffset.y - ptScreen.y
                                val distSq = dx * dx + dy * dy
                                if (distSq <= radiusSq && distSq < minDistanceSq) {
                                    minDistanceSq = distSq
                                    closestIntersection = ix
                                    closestScreenOffset = ptScreen
                                    line1Name = l1.name
                                    line2Name = l2.name
                                }
                            }
                        }
                    }
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

            for (p in visiblePoints) {
                val latLng = CaveMapBounds.imagePixelsToLatLng(
                    pixelX = p.x,
                    pixelY = p.y,
                    imageWidth = imageWidth,
                    imageHeight = imageHeight,
                    maxZoom = zoomMax
                )
                val ptScreen = projector(latLng)
                val dx = cursorScreenOffset.x - ptScreen.x
                val dy = cursorScreenOffset.y - ptScreen.y
                val distSq = dx * dx + dy * dy
                if (distSq <= radiusSq && distSq < minDistanceSq) {
                    minDistanceSq = distSq
                    closestPoint = p
                    closestScreenOffset = ptScreen
                }
            }
            if (closestPoint != null && closestScreenOffset != null) {
                return SnapTarget.Point(Pair(closestPoint.x, closestPoint.y), closestScreenOffset, closestPoint.name)
            }
        }

        // 3. Приоритет 3: Привязка к ребрам (проекция на отрезок)
        if (settings.snapToEdges && allowLines) {
            var closestEdgePt: Pair<Double, Double>? = null
            var closestScreenOffset: Offset? = null
            var lineName = ""
            var minDistanceSq = Float.MAX_VALUE

            for (line in visibleLines) {
                val pts = line.points
                for (i in 0 until pts.size - 1) {
                    val p1 = pts[i]
                    val p2 = pts[i + 1]
                    val p1Screen = projector(
                        CaveMapBounds.imagePixelsToLatLng(
                            pixelX = p1.first,
                            pixelY = p1.second,
                            imageWidth = imageWidth,
                            imageHeight = imageHeight,
                            maxZoom = zoomMax
                        )
                    )
                    val p2Screen = projector(
                        CaveMapBounds.imagePixelsToLatLng(
                            pixelX = p2.first,
                            pixelY = p2.second,
                            imageWidth = imageWidth,
                            imageHeight = imageHeight,
                            maxZoom = zoomMax
                        )
                    )

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
                            p1.first + t * (p2.first - p1.first),
                            p1.second + t * (p2.second - p1.second)
                        )
                        lineName = line.name
                    }
                }
            }
            if (closestEdgePt != null && closestScreenOffset != null) {
                return SnapTarget.Edge(closestEdgePt, closestScreenOffset, lineName)
            }
        }

        return null
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
