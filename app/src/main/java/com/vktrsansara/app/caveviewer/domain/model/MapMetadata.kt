package com.vktrsansara.app.caveviewer.domain.model

/**
 * Metadata stored in thismap.sqlite for each project.
 */
data class MapMetadata(
    val id: Long = 0,
    val projectName: String,
    val imageWidth: Int,
    val imageHeight: Int,
    val tileSize: Int = 256,
    val zoomMin: Int,
    val zoomMax: Int,
    val zoomDefault: Int,
    val pixelsPerMeter: Double = 0.0,
    val scaleMeters: Double = 0.0,
    val angleNorth: Double = 0.0,
    val crs: String = "Simple",
    val createdAt: Long = System.currentTimeMillis(),
    val pointsSearchConfig: LayerSearchConfig = LayerSearchConfig(),
    val linesSearchConfig: LayerSearchConfig = LayerSearchConfig(),
    val navigationConfig: NavigationConfig = NavigationConfig()
)
