package com.vktrsansara.app.caveviewer.domain.model

/**
 * Saved camera viewport position and rotation for MapLibreViewer.
 */
data class MapCameraPosition(
    val targetLat: Double = 0.0,
    val targetLon: Double = 0.0,
    val zoom: Double = 0.0,
    val bearing: Double = 0.0
)
