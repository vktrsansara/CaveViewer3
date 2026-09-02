package com.vktrsansara.app.caveviewer.domain.engine

import com.vktrsansara.app.caveviewer.domain.model.CaveRoute
import com.vktrsansara.app.caveviewer.domain.model.LayerLine
import com.vktrsansara.app.caveviewer.domain.model.NavAlgorithm
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
     * on the network of visible cave passages using the specified [algorithm].
     */
    fun findRoute(
        lines: List<LayerLine>,
        startPx: Pair<Double, Double>,
        endPx: Pair<Double, Double>,
        pixelsPerMeter: Double,
        algorithm: NavAlgorithm = NavAlgorithm.ASTAR,
        quality: Float = 1.5f,
        isAlternativeEnabled: Boolean = false
    ): NavigationResult {
        val validLines = lines.filter { it.points.size >= 2 }
        if (validLines.isEmpty()) {
            return NavigationResult(null, null, "В проекте нет видимых линий ходов")
        }

        // 1. Построение графа из линий
        val isDijkstra = (algorithm == NavAlgorithm.DIJKSTRA)
        val graph = buildGraph(validLines, pixelsPerMeter, quality, isDijkstra)
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
            val node = graph.nodes[startNodeId] ?: return NavigationResult(null, null, null)
            val singlePointRoute = CaveRoute(
                points = listOf(Pair(node.x, node.y)),
                lengthMeters = 0.0,
                averageDifficulty = 1.0f
            )
            return NavigationResult(singlePointRoute, null)
        }

        // 3. Выполнение маршрутизации в зависимости от выбранного алгоритма
        var primaryPath: PathResult? = null
        var altPath: PathResult? = null

        when (algorithm) {
            NavAlgorithm.ASTAR -> {
                primaryPath = aStarSearch(graph, startNodeId, endNodeId)
                if (primaryPath != null && isAlternativeEnabled) {
                    val usedEdges = primaryPath.edges.map { it.id }.toSet()
                    altPath = aStarSearch(graph, startNodeId, endNodeId, edgePenalties = usedEdges.associateWith { 3.5 })
                }
            }
            NavAlgorithm.BIDIRECTIONAL_ASTAR -> {
                primaryPath = bidirectionalAStarSearch(graph, startNodeId, endNodeId)
                if (primaryPath != null && isAlternativeEnabled) {
                    val usedEdges = primaryPath.edges.map { it.id }.toSet()
                    altPath = bidirectionalAStarSearch(graph, startNodeId, endNodeId, edgePenalties = usedEdges.associateWith { 3.5 })
                }
            }
            NavAlgorithm.YEN -> {
                if (isAlternativeEnabled) {
                    val pair = yenAlgorithm(graph, startNodeId, endNodeId)
                    primaryPath = pair.first
                    altPath = pair.second
                } else {
                    primaryPath = aStarSearch(graph, startNodeId, endNodeId)
                }
            }
            NavAlgorithm.DIJKSTRA -> {
                primaryPath = aStarSearch(graph, startNodeId, endNodeId, useHeuristic = false)
                if (primaryPath != null && isAlternativeEnabled) {
                    val usedEdges = primaryPath.edges.map { it.id }.toSet()
                    altPath = aStarSearch(
                        graph = graph,
                        startNodeId = startNodeId,
                        endNodeId = endNodeId,
                        edgePenalties = usedEdges.associateWith { 3.5 },
                        useHeuristic = false
                    )
                }
            }
        }

        if (primaryPath == null) {
            return NavigationResult(
                null,
                null,
                "Маршрут не найден: точки находятся в изолированных частях пещеры"
            )
        }

        val primaryRoute = buildCaveRoute(primaryPath, isAlternative = false)

        var alternativeRoute: CaveRoute? = null
        if (altPath != null) {
            val altCandidate = buildCaveRoute(altPath, isAlternative = true)
            val primaryPointsSet = primaryRoute.points.toSet()
            if (altCandidate.points.any { it !in primaryPointsSet }) {
                alternativeRoute = altCandidate
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
        quality: Float,
        isDijkstra: Boolean
    ): CaveGraph {
        val graph = CaveGraph(pixelsPerMeter, quality, isDijkstra)

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

    // --- Алгоритм 1: Стандартный A* / Dijkstra (при useHeuristic = false) ---

    private fun aStarSearch(
        graph: CaveGraph,
        startNodeId: Int,
        endNodeId: Int,
        edgePenalties: Map<Long, Double> = emptyMap(),
        ignoredNodes: Set<Int> = emptySet(),
        ignoredEdges: Set<Long> = emptySet(),
        useHeuristic: Boolean = true
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
        val startH = if (useHeuristic) graph.heuristic(graph.nodes[startNodeId]!!, targetNode) else 0.0
        openSet.add(PriorityItem(startNodeId, 0.0, startH))

        val visited = mutableSetOf<Int>()

        while (openSet.isNotEmpty()) {
            val current = openSet.poll() ?: break
            val currNodeId = current.nodeId

            if (currNodeId == endNodeId) {
                return reconstructPath(startNodeId, endNodeId, cameFromNode, cameFromEdge)
            }

            if (!visited.add(currNodeId)) continue

            val neighbors = graph.adjacency[currNodeId] ?: continue
            for (edge in neighbors) {
                if (edge.id in ignoredEdges) continue
                val neighborId = edge.getOtherNode(currNodeId)
                if (neighborId in visited || neighborId in ignoredNodes) continue

                val penalty = edgePenalties[edge.id] ?: 1.0
                val tentativeG = current.gScore + (edge.weight * penalty)

                if (tentativeG < gScores.getValue(neighborId)) {
                    gScores[neighborId] = tentativeG
                    cameFromNode[neighborId] = currNodeId
                    cameFromEdge[neighborId] = edge

                    val neighborNode = graph.nodes[neighborId] ?: continue
                    val h = if (useHeuristic) graph.heuristic(neighborNode, targetNode) else 0.0
                    val fScore = tentativeG + h
                    openSet.add(PriorityItem(neighborId, tentativeG, fScore))
                }
            }
        }

        return null
    }

    // --- Алгоритм 2: Двунаправленный A* (Bidirectional A*) ---

    private fun bidirectionalAStarSearch(
        graph: CaveGraph,
        startNodeId: Int,
        endNodeId: Int,
        edgePenalties: Map<Long, Double> = emptyMap()
    ): PathResult? {
        val startNode = graph.nodes[startNodeId] ?: return null
        val targetNode = graph.nodes[endNodeId] ?: return null

        data class PriorityItem(
            val nodeId: Int,
            val gScore: Double,
            val fScore: Double
        ) : Comparable<PriorityItem> {
            override fun compareTo(other: PriorityItem): Int = fScore.compareTo(other.fScore)
        }

        val forwardG = mutableMapOf<Int, Double>().withDefault { Double.POSITIVE_INFINITY }
        val backwardG = mutableMapOf<Int, Double>().withDefault { Double.POSITIVE_INFINITY }
        forwardG[startNodeId] = 0.0
        backwardG[endNodeId] = 0.0

        val forwardCameFromNode = mutableMapOf<Int, Int>()
        val forwardCameFromEdge = mutableMapOf<Int, GraphEdge>()
        val backwardCameFromNode = mutableMapOf<Int, Int>()
        val backwardCameFromEdge = mutableMapOf<Int, GraphEdge>()

        val forwardOpen = PriorityQueue<PriorityItem>()
        val backwardOpen = PriorityQueue<PriorityItem>()

        forwardOpen.add(PriorityItem(startNodeId, 0.0, graph.heuristic(startNode, targetNode)))
        backwardOpen.add(PriorityItem(endNodeId, 0.0, graph.heuristic(targetNode, startNode)))

        val forwardVisited = mutableSetOf<Int>()
        val backwardVisited = mutableSetOf<Int>()

        var bestMeetingNode: Int? = null
        var bestPathCost = Double.POSITIVE_INFINITY

        while (forwardOpen.isNotEmpty() && backwardOpen.isNotEmpty()) {
            val forwardMin = forwardOpen.peek()?.fScore ?: Double.POSITIVE_INFINITY
            val backwardMin = backwardOpen.peek()?.fScore ?: Double.POSITIVE_INFINITY

            if (forwardMin + backwardMin >= bestPathCost && bestMeetingNode != null) {
                break
            }

            if (forwardMin <= backwardMin) {
                val curr = forwardOpen.poll() ?: break
                val u = curr.nodeId
                if (!forwardVisited.add(u)) continue

                if (u in backwardVisited) {
                    val cost = (forwardG[u] ?: Double.POSITIVE_INFINITY) + (backwardG[u] ?: Double.POSITIVE_INFINITY)
                    if (cost < bestPathCost) {
                        bestPathCost = cost
                        bestMeetingNode = u
                    }
                }

                val neighbors = graph.adjacency[u] ?: continue
                for (edge in neighbors) {
                    val v = edge.getOtherNode(u)
                    if (v in forwardVisited) continue

                    val penalty = edgePenalties[edge.id] ?: 1.0
                    val tentativeG = curr.gScore + (edge.weight * penalty)

                    if (tentativeG < forwardG.getValue(v)) {
                        forwardG[v] = tentativeG
                        forwardCameFromNode[v] = u
                        forwardCameFromEdge[v] = edge

                        val vNode = graph.nodes[v] ?: continue
                        val fScore = tentativeG + graph.heuristic(vNode, targetNode)
                        forwardOpen.add(PriorityItem(v, tentativeG, fScore))

                        if (v in backwardVisited) {
                            val meetingCost = tentativeG + backwardG.getValue(v)
                            if (meetingCost < bestPathCost) {
                                bestPathCost = meetingCost
                                bestMeetingNode = v
                            }
                        }
                    }
                }
            } else {
                val curr = backwardOpen.poll() ?: break
                val u = curr.nodeId
                if (!backwardVisited.add(u)) continue

                if (u in forwardVisited) {
                    val cost = (forwardG[u] ?: Double.POSITIVE_INFINITY) + (backwardG[u] ?: Double.POSITIVE_INFINITY)
                    if (cost < bestPathCost) {
                        bestPathCost = cost
                        bestMeetingNode = u
                    }
                }

                val neighbors = graph.adjacency[u] ?: continue
                for (edge in neighbors) {
                    val v = edge.getOtherNode(u)
                    if (v in backwardVisited) continue

                    val penalty = edgePenalties[edge.id] ?: 1.0
                    val tentativeG = curr.gScore + (edge.weight * penalty)

                    if (tentativeG < backwardG.getValue(v)) {
                        backwardG[v] = tentativeG
                        backwardCameFromNode[v] = u
                        backwardCameFromEdge[v] = edge

                        val vNode = graph.nodes[v] ?: continue
                        val fScore = tentativeG + graph.heuristic(vNode, startNode)
                        backwardOpen.add(PriorityItem(v, tentativeG, fScore))

                        if (v in forwardVisited) {
                            val meetingCost = tentativeG + forwardG.getValue(v)
                            if (meetingCost < bestPathCost) {
                                bestPathCost = meetingCost
                                bestMeetingNode = v
                            }
                        }
                    }
                }
            }
        }

        val meetingNode = bestMeetingNode ?: return null

        val forwardPathNodes = mutableListOf<Int>()
        val forwardPathEdges = mutableListOf<GraphEdge>()

        var curr = meetingNode
        forwardPathNodes.add(curr)
        while (curr != startNodeId) {
            val prev = forwardCameFromNode[curr] ?: break
            val edge = forwardCameFromEdge[curr] ?: break
            forwardPathEdges.add(edge)
            forwardPathNodes.add(prev)
            curr = prev
        }
        forwardPathNodes.reverse()
        forwardPathEdges.reverse()

        val backwardPathNodes = mutableListOf<Int>()
        val backwardPathEdges = mutableListOf<GraphEdge>()

        curr = meetingNode
        while (curr != endNodeId) {
            val next = backwardCameFromNode[curr] ?: break
            val edge = backwardCameFromEdge[curr] ?: break
            backwardPathEdges.add(edge)
            backwardPathNodes.add(next)
            curr = next
        }

        val allNodes = forwardPathNodes + backwardPathNodes
        val allEdges = forwardPathEdges + backwardPathEdges

        return PathResult(allNodes, allEdges)
    }

    // --- Алгоритм 3: Алгоритм Йена (K-Shortest Paths для K=2) ---

    private fun yenAlgorithm(
        graph: CaveGraph,
        startNodeId: Int,
        endNodeId: Int
    ): Pair<PathResult?, PathResult?> {
        val primaryPath = aStarSearch(graph, startNodeId, endNodeId) ?: return Pair(null, null)

        val candidatePaths = mutableListOf<PathResult>()
        val primaryEdgeIds = primaryPath.edges.map { it.id }.toSet()

        for (i in 0 until primaryPath.nodes.size - 1) {
            val spurNode = primaryPath.nodes[i]
            val rootNodes = primaryPath.nodes.subList(0, i + 1)
            val rootEdges = primaryPath.edges.subList(0, i)

            val ignoredEdges = mutableSetOf<Long>()
            if (i < primaryPath.edges.size) {
                ignoredEdges.add(primaryPath.edges[i].id)
            }

            val ignoredNodes = rootNodes.filter { it != spurNode }.toSet()

            val spurPath = aStarSearch(
                graph = graph,
                startNodeId = spurNode,
                endNodeId = endNodeId,
                edgePenalties = emptyMap(),
                ignoredNodes = ignoredNodes,
                ignoredEdges = ignoredEdges
            )

            if (spurPath != null) {
                val totalNodes = rootNodes + spurPath.nodes.subList(1, spurPath.nodes.size)
                val totalEdges = rootEdges + spurPath.edges
                val candidate = PathResult(totalNodes, totalEdges)

                if (candidate.edges.any { it.id !in primaryEdgeIds }) {
                    candidatePaths.add(candidate)
                }
            }
        }

        val bestAlt = candidatePaths.minByOrNull { path ->
            path.edges.sumOf { it.weight }
        }

        return Pair(primaryPath, bestAlt)
    }

    private fun reconstructPath(
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

            val edgePts = if (edge.fromNodeId == fromNodeId) edge.points else edge.points.reversed()

            if (combinedPoints.isEmpty()) {
                combinedPoints.addAll(edgePts)
            } else {
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
        val quality: Float,
        val isDijkstra: Boolean
    ) {
        private var nextNodeId = 1
        private var nextEdgeId = 1L

        val nodes = mutableMapOf<Int, GraphNode>()
        val edges = mutableListOf<GraphEdge>()
        val adjacency = mutableMapOf<Int, MutableList<GraphEdge>>()

        private val grid = mutableMapOf<Long, MutableList<Int>>()

        private fun gridKey(x: Double, y: Double): Long {
            val gx = floor(x / NODE_SNAP_TOLERANCE_PX).toLong()
            val gy = floor(y / NODE_SNAP_TOLERANCE_PX).toLong()
            return (gx shl 32) or (gy and 0xFFFFFFFFL)
        }

        fun getOrCreateNode(x: Double, y: Double): Int {
            val gx = floor(x / NODE_SNAP_TOLERANCE_PX).toLong()
            val gy = floor(y / NODE_SNAP_TOLERANCE_PX).toLong()

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

            val weight = if (isDijkstra) {
                lenMeters // Дейкстра производит чистый физический волновой поиск
            } else {
                val diffExp = difficulty.toDouble().pow(quality.toDouble())
                lenMeters * diffExp
            }

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
            if (isDijkstra) return 0.0
            val distPx = hypot(to.x - from.x, to.y - from.y)
            val distMeters = if (ppm > 0.0) distPx / ppm else distPx
            val minDiffExp = (0.1).pow(quality.toDouble())
            return distMeters * minDiffExp
        }

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

            val n1 = nodes[targetEdge.fromNodeId]!!
            val n2 = nodes[targetEdge.toNodeId]!!
            if (hypot(bestProjX - n1.x, bestProjY - n1.y) <= NODE_SNAP_TOLERANCE_PX) {
                return n1.id
            }
            if (hypot(bestProjX - n2.x, bestProjY - n2.y) <= NODE_SNAP_TOLERANCE_PX) {
                return n2.id
            }

            val splitNodeId = nextNodeId++
            val splitNode = GraphNode(splitNodeId, bestProjX, bestProjY)
            nodes[splitNodeId] = splitNode

            adjacency[targetEdge.fromNodeId]?.remove(targetEdge)
            adjacency[targetEdge.toNodeId]?.remove(targetEdge)
            edges.remove(targetEdge)

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
