package com.vktrsansara.app.caveviewer.domain.model

/**
 * General geographic location and administrative region of the cave.
 */
data class MapLocation(
    val country: String = "",
    val region: String = "",
    val district: String = "",
    val description: String = ""
)
