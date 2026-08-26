package com.vktrsansara.app.caveviewer.domain.measure

import java.util.Locale
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Mathematical utilities for distance, perimeter, and polygon area calculations.
 */
object MeasureUtils {

    /**
     * Euclidean distance between two points in 2D pixels.
     */
    fun distancePx(p1: Pair<Double, Double>, p2: Pair<Double, Double>): Double {
        val dx = p2.first - p1.first
        val dy = p2.second - p1.second
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Total length of a polyline in pixels.
     */
    fun calculatePolylineLengthPx(points: List<Pair<Double, Double>>): Double {
        if (points.size < 2) return 0.0
        var total = 0.0
        for (i in 0 until points.size - 1) {
            total += distancePx(points[i], points[i + 1])
        }
        return total
    }

    /**
     * Formats distance in meters/kilometers (if ppm > 0) or pixels.
     */
    fun formatDistance(pixels: Double, ppm: Double): String {
        return if (ppm > 0.0) {
            val meters = pixels / ppm
            if (meters < 1000.0) {
                String.format(Locale.US, "%.2f м", meters)
            } else {
                String.format(Locale.US, "%.2f км", meters / 1000.0)
            }
        } else {
            String.format(Locale.US, "%.0f px", pixels)
        }
    }

    /**
     * Calculates polygon area in pixels squared using the Gauss shoelace formula.
     */
    fun calculatePolygonAreaPx(points: List<Pair<Double, Double>>): Double {
        if (points.size < 3) return 0.0
        var area = 0.0
        val n = points.size
        for (i in 0 until n) {
            val j = (i + 1) % n
            area += points[i].first * points[j].second
            area -= points[j].first * points[i].second
        }
        return abs(area) / 2.0
    }

    /**
     * Calculates the closed perimeter of a polygon in pixels.
     */
    fun calculatePolygonPerimeterPx(points: List<Pair<Double, Double>>): Double {
        if (points.size < 2) return 0.0
        var perimeter = 0.0
        val n = points.size
        for (i in 0 until n) {
            val j = (i + 1) % n
            perimeter += distancePx(points[i], points[j])
        }
        return perimeter
    }

    /**
     * Formats area in m² / ha / km² (if ppm > 0) or px².
     */
    fun formatArea(areaPx: Double, ppm: Double): String {
        return if (ppm > 0.0) {
            val realAreaM2 = areaPx / (ppm * ppm)
            when {
                realAreaM2 < 10_000.0 -> String.format(Locale.US, "%.2f м²", realAreaM2)
                realAreaM2 < 1_000_000.0 -> String.format(Locale.US, "%.2f га", realAreaM2 / 10_000.0)
                else -> String.format(Locale.US, "%.2f км²", realAreaM2 / 1_000_000.0)
            }
        } else {
            String.format(Locale.US, "%.0f px²", areaPx)
        }
    }
}
