package com.vktrsansara.app.caveviewer.domain.model

import org.maplibre.android.geometry.LatLng

/**
 * Represents a measured calibration point for scale binding.
 */
data class ScaleBindingPoint(
    val latLng: LatLng,
    val imagePx: Pair<Double, Double>
)
