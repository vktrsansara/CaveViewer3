package com.vktrsansara.app.caveviewer.domain.model

/**
 * GPS coordinates and altitude for a specific cave entrance or pit.
 */
data class EntranceCoordinate(
    val pointIndex: Int = 0,
    val name: String = "",
    val lat: Double? = null,
    val lon: Double? = null,
    val alt: Double? = null
)
