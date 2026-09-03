package com.vktrsansara.app.caveviewer.domain.model

enum class NavAlgorithm(val title: String) {
    ASTAR("A* (По умолчанию)"),
    BIDIRECTIONAL_ASTAR("Bidirectional A*"),
    YEN("Алгоритм Йена"),
    DIJKSTRA("Dijkstra (Волновой обход)")
}

data class NavigationConfig(
    val isEnabled: Boolean = false,
    val algorithm: NavAlgorithm = NavAlgorithm.ASTAR,
    val quality: Float = 1.5f,                           // Точность (1.0..2.0)
    val isAlternativeRouteEnabled: Boolean = false
) {
    val accuracyQuality: Float get() = quality
}

/**
 * Individual passage segment of a route with its difficulty rating and length.
 */
data class RouteSegment(
    val points: List<Pair<Double, Double>>, // Raster coordinates (px)
    val difficulty: Float,
    val lengthMeters: Double
)

/**
 * Represents a computed route along the cave passages graph.
 */
data class CaveRoute(
    val points: List<Pair<Double, Double>>, // Raster coordinates (px)
    val segments: List<RouteSegment> = emptyList(),
    val lengthMeters: Double,
    val averageDifficulty: Float,
    val isAlternative: Boolean = false
)

/**
 * Result of routing between Point A and Point B.
 */
data class NavigationResult(
    val primaryRoute: CaveRoute?,
    val alternativeRoute: CaveRoute? = null,
    val errorMessage: String? = null
)
