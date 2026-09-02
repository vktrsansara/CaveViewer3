package com.vktrsansara.app.caveviewer.domain.engine

import com.vktrsansara.app.caveviewer.domain.model.CaveRoute
import com.vktrsansara.app.caveviewer.domain.model.LayerLine
import com.vktrsansara.app.caveviewer.domain.model.NavigationResult
import java.util.PriorityQueue
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt

object CaveGraphRouter {

    private const val NODE_SNAP_TOLERANCE_PX = 4.0

    /**
     * Finds the optimal route (and optional alternative route) between startPx and endPx
     * on the network of visible cave passages.
     */
    fun findRoute(
        lines: List<LayerLine>,
        startPx: Pair<Double, Double>,
        endPx: Pair<Double, Double>,
        pixelsPerMeter: Double,
        quality: Float = 1.5f,
        isAlternativeEnabled: Boolean = false
    ): NavigationResult {
        val validLines = lines.filter { it.points.size >= 2 }
        if (validLines.isEmpty()) {
            return NavigationResult(null, null, "В проекте нет видимых линий ходов")
        }

        // 1. Построение графа из линий
        val graph = buildGraph(validLines, pixelsPerMeter, quality)
        if (graph.nodes.size < 2 || graph.edges.isEmpty()) {
            return NavigationResult(null, null, "Не удалось построить граф ходов")
        }

        // 2. Привязка точки А (Старт) и точки Б (Финиш) к ближайшим ребрам графа
        val startNodeId = graph.snapPointToGraph(startPx.first, startPx.second)
        val endNodeId = graph.snapPointToGraph(endPx.first, endPx.second)

        if (startNodeId == null || endNodeId == null) {
            return NavigationResult(null, null, "Точки маршрута находятся слишком далеко от ходов")
        }

        if (startNodeId == endNodeId) {
            // Старт и финиш привязались к одной и той же точке
            val node = graph.nodes[startNodeId] ?: return NavigationResult(null, null, null)
            val singlePointRoute = CaveRoute(
                points = listOf(Pair(node.x, node.y)),
                lengthMeters = 0.0,
                averageDifficulty = 1.0f
            )
            return NavigationResult(singlePointRoute, null)
        }

        // 3. Поиск основного маршрута через A*
        val primaryPath = aStarSearch(graph, startNodeId, endNodeId, edgePenalties = emptyMap())
        if (primaryPath == null) {
            return NavigationResult(
                null,
                null,
                "Маршрут не найден: точки находятся в изолированных частях пещеры"
            )
        }

        val primaryRoute = buildCaveRoute(primaryPath, isAlternative = false)

        // 4. Поиск альтернативного маршрута (если включен)
        var alternativeRoute: CaveRoute? = null
        if (isAlternativeEnabled) {
            // Штрафуем ребра основного маршрута в 3.5 раза, чтобы A* исследовал параллельные ходы лабиринта
            val usedEdgeIds = primaryPath.edges.map { it.id }.toSet()
            val penalties = usedEdgeIds.associateWith { 3.5 }
            val altPath = aStarSearch(graph, startNodeId, endNodeId, edgePenalties = penalties)

            if (altPath != null) {
                val altRouteCandidate = buildCaveRoute(altPath, isAlternative = true)
                // Альтернативный маршрут имеет смысл, только если он отличается от основного
                val primaryPointsSet = primaryRoute.points.toSet()
                val hasDistinctPoints = altRouteCandidate.points.any { it !in primaryPointsSet }
                if (hasDistinctPoints) {
                    alternativeRoute = altRouteCandidate
                }
            }
        }

        return NavigationResult(
            primaryRoute = primaryRoute,
            alternativeRoute = alternativeRoute
        )
    }

    private fun buildGraph(
        lines: List<LayerLine>,
        pixelsPerMeter: Double,
        quality: Float
    ): CaveGraph {
        val graph = CaveGraph(pixelsPerMeter, quality)

        for (line in lines) {
            val diff = line.difficulty.coerceIn(0.1f, 8.0f)
            val pts = line.points
            var prevNodeId: Int? = null

            for (i in 0 until pts.size - 1) {
                val p1 = pts[i]
                val p2 = pts[i + 1]

                val n1 = prevNodeId ?: graph.getOrCreateNode(p1.first, p1.second)
                val n2 = graph.getOrCreateNode(p2.first, p2.second)
                prevNodeId = n2

                graph.addEdge(n1, n2, diff, line.id, listOf(p1, p2))
            }
        }

        return graph
    }

    private fun aStarSearch(
        graph: CaveGraph,
        startNodeId: Int,
        endNodeId: Int,
        edgePenalties: Map<Long, Double>
    ): PathResult? {
        val targetNode = graph.nodes[endNodeId] ?: return null

        data class PriorityItem(
            val nodeId: Int,
            val gScore: Double,
            val fScore: Double
        ) : Comparable<PriorityItem> {
            override fun compareTo(other: PriorityItem): Int = fScore.compareTo(other.fScore)
        }

        val gScores = mutableMapOf<Int, Double>().withDefault { Double.POSITIVE_INFINITY }
        gScores[startNodeId] = 0.0

        val cameFromNode = mutableMapOf<Int, Int>()
        val cameFromEdge = mutableMapOf<Int, GraphEdge>()

        val openSet = PriorityQueue<PriorityItem>()
        val startH = graph.heuristic(graph.nodes[startNodeId]!!, targetNode)
        openSet.add(PriorityItem(startNodeId, 0.0, startH))

        val visited = mutableSetOf<Int>()

        while (openSet.isNotEmpty()) {
            val current = openSet.poll() ?: break
            val currNodeId = current.nodeId

            if (currNodeId == endNodeId) {
                // Восстановление пути
                return reconstructPath(graph, startNodeId, endNodeId, cameFromNode, cameFromEdge)
            }

            if (!visited.add(currNodeId)) continue

            val neighbors = graph.adjacency[currNodeId] ?: continue
            for (edge in neighbors) {
                val neighborId = edge.getOtherNode(currNodeId)
                if (neighborId in visited) continue

                val penalty = edgePenalties[edge.id] ?: 1.0
                val tentativeG = current.gScore + (edge.weight * penalty)

                if (tentativeG < gScores.getValue(neighborId)) {
                    gScores[neighborId] = tentativeG
                    cameFromNode[neighborId] = currNodeId
                    cameFromEdge[neighborId] = edge

                    val neighborNode = graph.nodes[neighborId] ?: continue
                    val fScore = tentativeG + graph.heuristic(neighborNode, targetNode)
                    openSet.add(PriorityItem(neighborId, tentativeG, fScore))
                }
            }
        }

        return null
    }

    private fun reconstructPath(
        graph: CaveGraph,
        startNodeId: Int,
        endNodeId: Int,
        cameFromNode: Map<Int, Int>,
        cameFromEdge: Map<Int, GraphEdge>
    ): PathResult {
        val edges = mutableListOf<GraphEdge>()
        val nodes = mutableListOf<Int>()

        var curr = endNodeId
        nodes.add(curr)

        while (curr != startNodeId) {
            val prevNode = cameFromNode[curr] ?: break
            val edge = cameFromEdge[curr] ?: break
            edges.add(edge)
            nodes.add(prevNode)
            curr = prevNode
        }

        edges.reverse()
        nodes.reverse()

        return PathResult(nodes, edges)
    }

    private fun buildCaveRoute(pathResult: PathResult, isAlternative: Boolean): CaveRoute {
        val combinedPoints = mutableListOf<Pair<Double, Double>>()
        var totalLengthMeters = 0.0
        var totalWeightedDifficulty = 0.0

        for (i in pathResult.edges.indices) {
            val edge = pathResult.edges[i]
            val fromNodeId = pathResult.nodes[i]
            val toNodeId = pathResult.nodes[i + 1]

            val edgePts = if (edge.fromNodeId == fromNodeId) edge.points else edge.points.reversed()

            if (combinedPoints.isEmpty()) {
                combinedPoints.addAll(edgePts)
            } else {
                // Добавляем все точки кроме первой, чтобы не дублировать стык
                if (edgePts.size > 1) {
                    combinedPoints.addAll(edgePts.subList(1, edgePts.size))
                }
            }

            totalLengthMeters += edge.lengthMeters
            totalWeightedDifficulty += edge.lengthMeters * edge.difficulty
        }

        val avgDiff = if (totalLengthMeters > 0.0) {
            (totalWeightedDifficulty / totalLengthMeters).toFloat().coerceIn(0.1f, 8.0f)
        } else {
            1.0f
        }

        return CaveRoute(
            points = combinedPoints,
            lengthMeters = (totalLengthMeters * 10.0).roundToInt() / 10.0,
            averageDifficulty = (avgDiff * 10f).roundToInt() / 10f,
            isAlternative = isAlternative
        )
    }

    // --- Внутренние структуры графа ---

    private data class GraphNode(
        val id: Int,
        var x: Double,
        var y: Double
    )

    private data class GraphEdge(
        val id: Long,
        val fromNodeId: Int,
        val toNodeId: Int,
        val lengthMeters: Double,
        val difficulty: Float,
        val weight: Double,
        val lineId: Long,
        val points: List<Pair<Double, Double>>
    ) {
        fun getOtherNode(nodeId: Int): Int = if (nodeId == fromNodeId) toNodeId else fromNodeId
    }

    private data class PathResult(
        val nodes: List<Int>,
        val edges: List<GraphEdge>
    )

    private class CaveGraph(
        val ppm: Double,
        val quality: Float
    ) {
        private var nextNodeId = 1
        private var nextEdgeId = 1L

        val nodes = mutableMapOf<Int, GraphNode>()
        val edges = mutableListOf<GraphEdge>()
        val adjacency = mutableMapOf<Int, MutableList<GraphEdge>>()

        // Пространственный индекс для быстрого поиска и кластеризации узлов
        private val grid = mutableMapOf<Long, MutableList<Int>>()

        private fun gridKey(x: Double, y: Double): Long {
            val gx = floor(x / NODE_SNAP_TOLERANCE_PX).toLong()
            val gy = floor(y / NODE_SNAP_TOLERANCE_PX).toLong()
            return (gx shl 32) or (gy and 0xFFFFFFFFL)
        }

        fun getOrCreateNode(x: Double, y: Double): Int {
            val gx = floor(x / NODE_SNAP_TOLERANCE_PX).toLong()
            val gy = floor(y / NODE_SNAP_TOLERANCE_PX).toLong()

            // Проверяем соседние ячейки (3x3)
            for (dx in -1L..1L) {
                for (dy in -1L..1L) {
                    val key = ((gx + dx) shl 32) or ((gy + dy) and 0xFFFFFFFFL)
                    val bucket = grid[key] ?: continue
                    for (candidateId in bucket) {
                        val node = nodes[candidateId] ?: continue
                        val dist = hypot(node.x - x, node.y - y)
                        if (dist <= NODE_SNAP_TOLERANCE_PX) {
                            return candidateId
                        }
                    }
                }
            }

            // Создаем новый узел
            val id = nextNodeId++
            val newNode = GraphNode(id, x, y)
            nodes[id] = newNode

            val key = gridKey(x, y)
            grid.getOrPut(key) { mutableListOf() }.add(id)

            return id
        }

        fun addEdge(
            fromNode: Int,
            toNode: Int,
            difficulty: Float,
            lineId: Long,
            points: List<Pair<Double, Double>>
        ): GraphEdge {
            val p1 = points.first()
            val p2 = points.last()
            val lenPx = hypot(p2.first - p1.first, p2.second - p1.second)
            val lenMeters = if (ppm > 0.0) lenPx / ppm else lenPx

            // Формула веса A*: weight = distance * difficulty^quality
            val diffExp = difficulty.toDouble().pow(quality.toDouble())
            val weight = lenMeters * diffExp

            val edge = GraphEdge(
                id = nextEdgeId++,
                fromNodeId = fromNode,
                toNodeId = toNode,
                lengthMeters = lenMeters,
                difficulty = difficulty,
                weight = weight,
                lineId = lineId,
                points = points
            )

            edges.add(edge)
            adjacency.getOrPut(fromNode) { mutableListOf() }.add(edge)
            adjacency.getOrPut(toNode) { mutableListOf() }.add(edge)

            return edge
        }

        fun heuristic(from: GraphNode, to: GraphNode): Double {
            val distPx = hypot(to.x - from.x, to.y - from.y)
            val distMeters = if (ppm > 0.0) distPx / ppm else distPx
            // Эвристика допустима (не переоценивает путь), так как минимальная сложность >= 0.1
            val minDiffExp = (0.1).pow(quality.toDouble())
            return distMeters * minDiffExp
        }

        /**
         * Привязывает произвольную точку (например, координаты тапа на карте) к ближайшему отрезку графа.
         * Если точка проецируется на ребро, ребро расщепляется новым узлом.
         */
        fun snapPointToGraph(px: Double, py: Double): Int? {
            if (edges.isEmpty()) return null

            var closestEdge: GraphEdge? = null
            var minDistanceSq = Double.MAX_VALUE
            var bestProjX = px
            var bestProjY = py

            for (edge in edges) {
                val n1 = nodes[edge.fromNodeId] ?: continue
                val n2 = nodes[edge.toNodeId] ?: continue

                val dx = n2.x - n1.x
                val dy = n2.y - n1.y
                val segLenSq = dx * dx + dy * dy

                val t = if (segLenSq > 0.0) {
                    (((px - n1.x) * dx + (py - n1.y) * dy) / segLenSq).coerceIn(0.0, 1.0)
                } else 0.0

                val projX = n1.x + t * dx
                val projY = n1.y + t * dy
                val distSq = (px - projX) * (px - projX) + (py - projY) * (py - projY)

                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq
                    closestEdge = edge
                    bestProjX = projX
                    bestProjY = projY
                }
            }

            val targetEdge = closestEdge ?: return null

            // Если проекция попадает вплотную к одному из концов ребра
            val n1 = nodes[targetEdge.fromNodeId]!!
            val n2 = nodes[targetEdge.toNodeId]!!
            if (hypot(bestProjX - n1.x, bestProjY - n1.y) <= NODE_SNAP_TOLERANCE_PX) {
                return n1.id
            }
            if (hypot(bestProjX - n2.x, bestProjY - n2.y) <= NODE_SNAP_TOLERANCE_PX) {
                return n2.id
            }

            // Иначе создаем новый узел на ребре и расщепляем его
            val splitNodeId = nextNodeId++
            val splitNode = GraphNode(splitNodeId, bestProjX, bestProjY)
            nodes[splitNodeId] = splitNode

            // Удаляем старое ребро из adjacency
            adjacency[targetEdge.fromNodeId]?.remove(targetEdge)
            adjacency[targetEdge.toNodeId]?.remove(targetEdge)
            edges.remove(targetEdge)

            // Добавляем два новых ребра
            addEdge(
                fromNode = targetEdge.fromNodeId,
                toNode = splitNodeId,
                difficulty = targetEdge.difficulty,
                lineId = targetEdge.lineId,
                points = listOf(Pair(n1.x, n1.y), Pair(bestProjX, bestProjY))
            )

            addEdge(
                fromNode = splitNodeId,
                toNode = targetEdge.toNodeId,
                difficulty = targetEdge.difficulty,
                lineId = targetEdge.lineId,
                points = listOf(Pair(bestProjX, bestProjY), Pair(n2.x, n2.y))
            )

            return splitNodeId
        }
    }
}
