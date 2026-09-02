package com.vktrsansara.app.caveviewer.domain.model

/**
 * Settings configuration for cave passages route finder (A*).
 */
data class NavigationConfig(
    val isEnabled: Boolean = false,
    val accuracyQuality: Float = 1.5f, // 1.0f (Быстро / кратчайший путь) .. 2.0f (Точно / учет сложности)
    val isAlternativeRouteEnabled: Boolean = false
)

/**
 * Represents a computed route along the cave passages graph.
 */
data class CaveRoute(
    val points: List<Pair<Double, Double>>, // Raster coordinates (px)
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
