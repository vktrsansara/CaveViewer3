package com.vktrsansara.app.caveviewer.domain.engine

import com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils
import com.vktrsansara.app.caveviewer.domain.model.IntersectionMode
import com.vktrsansara.app.caveviewer.domain.model.LayerLine
import com.vktrsansara.app.caveviewer.domain.model.LayerPoint
import kotlin.math.floor

/**
 * Lightweight 2D vertex representation in World Pixel Space.
 */
data class IndexedVertex(
    val px: Pair<Double, Double>,
    val lineName: String
)

/**
 * Line segment with line metadata and segment index in World Pixel Space.
 */
data class IndexedSegment(
    val p1: Pair<Double, Double>,
    val p2: Pair<Double, Double>,
    val lineId: Long,
    val lineName: String,
    val segmentIndex: Int
)

/**
 * Pre-computed line intersection in World Pixel Space.
 */
data class IndexedIntersection(
    val px: Pair<Double, Double>,
    val line1Name: String,
    val line2Name: String
)

/**
 * Spatial cell holding elements within a grid region of [cellSize] x [cellSize] pixels.
 */
class SpatialCell {
    val vertices = mutableListOf<IndexedVertex>()
    val points = mutableListOf<LayerPoint>()
    val segments = mutableListOf<IndexedSegment>()
    val intersections = mutableListOf<IndexedIntersection>()
}

/**
 * Candidate elements returned from querying nearby spatial cells around a cursor location.
 */
data class CellCandidates(
    val vertices: List<IndexedVertex>,
    val points: List<LayerPoint>,
    val segments: List<IndexedSegment>,
    val intersections: List<IndexedIntersection>
)

/**
 * High-performance 2D Spatial Hash Grid in World Pixel Space.
 * Partitions the raster plan into uniform square cells of [cellSize] pixels.
 * Accelerates magnetic snapping target searches from O(N) to O(1) / O(K) and
 * reduces intersection detection from global O(N^2) to cell-local checks.
 */
class SpatialHashGrid(val cellSize: Double = 256.0) {
    private val cells = HashMap<Long, SpatialCell>()

    /**
     * Packs 2D cell grid coordinates (cx, cy) into a single 64-bit integer key.
     */
    fun cellKey(cx: Int, cy: Int): Long = (cx.toLong() shl 32) or (cy.toLong() and 0xFFFFFFFFL)

    private fun getOrCreateCell(cx: Int, cy: Int): SpatialCell {
        val key = cellKey(cx, cy)
        var cell = cells[key]
        if (cell == null) {
            cell = SpatialCell()
            cells[key] = cell
        }
        return cell
    }

    /**
     * Rebuilds the spatial index from the provided lines and points.
     * Must be called ONLY when layers change, never on camera movement or zoom.
     */
    fun build(
        lines: List<LayerLine>,
        points: List<LayerPoint>,
        intersectionMode: IntersectionMode
    ) {
        cells.clear()

        // 1. Index Layer Points
        for (p in points) {
            val cx = floor(p.x / cellSize).toInt()
            val cy = floor(p.y / cellSize).toInt()
            getOrCreateCell(cx, cy).points.add(p)
        }

        // 2. Index Line Vertices & Segments
        for (line in lines) {
            val pts = line.points
            if (pts.isEmpty()) continue

            // Index vertices
            for (pt in pts) {
                val cx = floor(pt.first / cellSize).toInt()
                val cy = floor(pt.second / cellSize).toInt()
                getOrCreateCell(cx, cy).vertices.add(IndexedVertex(pt, line.name))
            }

            // Index segments into overlapping grid cells
            for (si in 0 until pts.size - 1) {
                val p1 = pts[si]
                val p2 = pts[si + 1]

                val minX = minOf(p1.first, p2.first)
                val maxX = maxOf(p1.first, p2.first)
                val minY = minOf(p1.second, p2.second)
                val maxY = maxOf(p1.second, p2.second)

                val minCX = floor(minX / cellSize).toInt()
                val maxCX = floor(maxX / cellSize).toInt()
                val minCY = floor(minY / cellSize).toInt()
                val maxCY = floor(maxY / cellSize).toInt()

                val seg = IndexedSegment(
                    p1 = p1,
                    p2 = p2,
                    lineId = line.id,
                    lineName = line.name,
                    segmentIndex = si
                )

                for (cx in minCX..maxCX) {
                    for (cy in minCY..maxCY) {
                        getOrCreateCell(cx, cy).segments.add(seg)
                    }
                }
            }
        }

        // 3. Compute Intersections Cell-Locally (only if intersectionMode != NO)
        if (intersectionMode != IntersectionMode.NO) {
            val testedPairs = HashSet<String>()

            for (cell in cells.values) {
                val segs = cell.segments
                if (segs.size < 2) continue

                for (i in 0 until segs.size) {
                    val segA = segs[i]
                    for (j in i + 1 until segs.size) {
                        val segB = segs[j]

                        // Skip consecutive segments of the same line
                        if (segA.lineId == segB.lineId && kotlin.math.abs(segA.segmentIndex - segB.segmentIndex) < 2) {
                            continue
                        }

                        // Deduplicate segment pair test across shared cells
                        val pairKey = if (segA.lineId < segB.lineId || (segA.lineId == segB.lineId && segA.segmentIndex < segB.segmentIndex)) {
                            "${segA.lineId}_${segA.segmentIndex}_${segB.lineId}_${segB.segmentIndex}"
                        } else {
                            "${segB.lineId}_${segB.segmentIndex}_${segA.lineId}_${segA.segmentIndex}"
                        }

                        if (!testedPairs.add(pairKey)) {
                            continue
                        }

                        val ix = MeasureUtils.findSegmentIntersection(segA.p1, segA.p2, segB.p1, segB.p2)
                        if (ix != null) {
                            val ixCX = floor(ix.first / cellSize).toInt()
                            val ixCY = floor(ix.second / cellSize).toInt()
                            getOrCreateCell(ixCX, ixCY).intersections.add(
                                IndexedIntersection(ix, segA.lineName, segB.lineName)
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Queries all candidate vertices, points, segments, and intersections in cells
     * overlapping the bounding box of [cursorPx] +/- [radiusPx].
     */
    fun queryCandidates(cursorPx: Pair<Double, Double>, radiusPx: Double): CellCandidates {
        if (cells.isEmpty() || radiusPx <= 0.0) {
            return CellCandidates(emptyList(), emptyList(), emptyList(), emptyList())
        }

        val minCX = floor((cursorPx.first - radiusPx) / cellSize).toInt()
        val maxCX = floor((cursorPx.first + radiusPx) / cellSize).toInt()
        val minCY = floor((cursorPx.second - radiusPx) / cellSize).toInt()
        val maxCY = floor((cursorPx.second + radiusPx) / cellSize).toInt()

        val candidateVertices = mutableListOf<IndexedVertex>()
        val candidatePoints = mutableListOf<LayerPoint>()
        val candidateSegments = mutableListOf<IndexedSegment>()
        val candidateIntersections = mutableListOf<IndexedIntersection>()

        val seenSegments = HashSet<String>()

        for (cx in minCX..maxCX) {
            for (cy in minCY..maxCY) {
                val cell = cells[cellKey(cx, cy)] ?: continue
                candidateVertices.addAll(cell.vertices)
                candidatePoints.addAll(cell.points)
                candidateIntersections.addAll(cell.intersections)

                for (seg in cell.segments) {
                    val segKey = "${seg.lineId}_${seg.segmentIndex}"
                    if (seenSegments.add(segKey)) {
                        candidateSegments.add(seg)
                    }
                }
            }
        }

        return CellCandidates(
            vertices = candidateVertices,
            points = candidatePoints,
            segments = candidateSegments,
            intersections = candidateIntersections
        )
    }

    /**
     * Clears all cells in the grid.
     */
    fun clear() {
        cells.clear()
    }
}
