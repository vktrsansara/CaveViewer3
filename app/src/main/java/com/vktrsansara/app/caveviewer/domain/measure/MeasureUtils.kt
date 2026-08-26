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

    /**
     * Calculates angle (0.00..180.00°) at vertex between base point p1 and current cursorPx.
     */
    fun calculateAngleDegrees(
        p1: Pair<Double, Double>,
        vertex: Pair<Double, Double>,
        cursorPx: Pair<Double, Double>
    ): Double {
        val v1x = p1.first - vertex.first
        val v1y = p1.second - vertex.second
        val v2x = cursorPx.first - vertex.first
        val v2y = cursorPx.second - vertex.second
        val len1 = sqrt(v1x * v1x + v1y * v1y)
        val len2 = sqrt(v2x * v2x + v2y * v2y)
        if (len1 == 0.0 || len2 == 0.0) return 0.0
        val angle1 = kotlin.math.atan2(v1y, v1x)
        val angle2 = kotlin.math.atan2(v2y, v2x)
        var diffDeg = Math.toDegrees(abs(angle1 - angle2))
        if (diffDeg > 180.0) {
            diffDeg = 360.0 - diffDeg
        }
        return diffDeg
    }

    /**
     * Calculates direct azimuth (0.00..360.00°) from p1 to p2 taking into account map's angleNorth.
     */
    fun calculateAzimuthDegrees(
        p1: Pair<Double, Double>,
        p2: Pair<Double, Double>,
        angleNorth: Double
    ): Double {
        val dx = p2.first - p1.first
        val dy = p1.second - p2.second // Y raster axis points downwards
        var rawAngleDeg = Math.toDegrees(kotlin.math.atan2(dx, dy))
        rawAngleDeg = (rawAngleDeg + 360.0) % 360.0
        return (rawAngleDeg - angleNorth + 360.0) % 360.0
    }

    /**
     * Calculates rumb (horizon quadrant) from azimuth.
     * Returns a pair of quadrant name ("СВ", "ЮВ", "ЮЗ", "СЗ") and inner angle (0..90°).
     */
    fun calculateRumb(azimuth: Double): Pair<String, Double> {
        val az = (azimuth % 360.0 + 360.0) % 360.0
        return when {
            az in 0.0..90.0 -> Pair("СВ", az)
            az in 90.0..180.0 -> Pair("ЮВ", 180.0 - az)
            az in 180.0..270.0 -> Pair("ЮЗ", az - 180.0)
            else -> Pair("СЗ", 360.0 - az)
        }
    }

    /**
     * Calculates back (reverse) azimuth (+180°).
     */
    fun calculateBackAzimuth(azimuth: Double): Double {
        return ((azimuth + 180.0) % 360.0 + 360.0) % 360.0
    }

    /**
     * Calculates intersection points of infinite line (p1 -> p2) with a bounding rectangle (0..mapWidth, 0..mapHeight).
     */
    fun calculateInfiniteLineBounds(
        p1: Pair<Double, Double>,
        p2: Pair<Double, Double>,
        mapWidth: Double,
        mapHeight: Double
    ): Pair<Pair<Double, Double>, Pair<Double, Double>> {
        val dx = p2.first - p1.first
        val dy = p2.second - p1.second
        if (dx == 0.0 && dy == 0.0) return Pair(p1, p2)
        val intersections = mutableListOf<Pair<Double, Double>>()
        // Intersection with x = 0 and x = mapWidth
        if (dx != 0.0) {
            val tLeft = (0.0 - p1.first) / dx
            val yLeft = p1.second + tLeft * dy
            if (yLeft in 0.0..mapHeight) intersections.add(Pair(0.0, yLeft))
            val tRight = (mapWidth - p1.first) / dx
            val yRight = p1.second + tRight * dy
            if (yRight in 0.0..mapHeight) intersections.add(Pair(mapWidth, yRight))
        }
        // Intersection with y = 0 and y = mapHeight
        if (dy != 0.0) {
            val tTop = (0.0 - p1.second) / dy
            val xTop = p1.first + tTop * dx
            if (xTop in 0.0..mapWidth) intersections.add(Pair(xTop, 0.0))
            val tBottom = (mapHeight - p1.second) / dy
            val xBottom = p1.first + tBottom * dx
            if (xBottom in 0.0..mapWidth) intersections.add(Pair(xBottom, mapHeight))
        }
        return if (intersections.size >= 2) {
            Pair(intersections[0], intersections[1])
        } else {
            // Fallback: extend line by large factor
            val factor = 10000.0
            Pair(
                Pair(p1.first - dx * factor, p1.second - dy * factor),
                Pair(p1.first + dx * factor, p1.second + dy * factor)
            )
        }
    }

    /**
     * Metrics for circular zone (radius, diameter, area, circumference).
     */
    data class CircleMetrics(
        val radiusText: String,
        val diameterText: String,
        val areaText: String,
        val perimeterText: String
    )

    /**
     * Calculates circle metrics in meters/km or px depending on ppm calibration.
     */
    fun calculateCircleMetrics(radiusPx: Double, ppm: Double): CircleMetrics {
        val diameterPx = radiusPx * 2.0
        val areaPx = Math.PI * radiusPx * radiusPx
        val circumferencePx = 2.0 * Math.PI * radiusPx
        return if (ppm > 0.0) {
            val rMeters = radiusPx / ppm
            val dMeters = diameterPx / ppm
            val areaM2 = areaPx / (ppm * ppm)
            val cMeters = circumferencePx / ppm
            val rStr = if (rMeters < 1000.0) String.format(Locale.US, "%.2f м", rMeters) else String.format(Locale.US, "%.2f км", rMeters / 1000.0)
            val dStr = if (dMeters < 1000.0) String.format(Locale.US, "%.2f м", dMeters) else String.format(Locale.US, "%.2f км", dMeters / 1000.0)
            val cStr = if (cMeters < 1000.0) String.format(Locale.US, "%.2f м", cMeters) else String.format(Locale.US, "%.2f км", cMeters / 1000.0)
            val sStr = when {
                areaM2 < 10_000.0 -> String.format(Locale.US, "%.2f м²", areaM2)
                areaM2 < 1_000_000.0 -> String.format(Locale.US, "%.2f га", areaM2 / 10_000.0)
                else -> String.format(Locale.US, "%.2f км²", areaM2 / 1_000_000.0)
            }
            CircleMetrics(rStr, dStr, sStr, cStr)
        } else {
            CircleMetrics(
                radiusText = String.format(Locale.US, "%.0f px", radiusPx),
                diameterText = String.format(Locale.US, "%.0f px", diameterPx),
                areaText = String.format(Locale.US, "%.0f px²", areaPx),
                perimeterText = String.format(Locale.US, "%.0f px", circumferencePx)
            )
        }
    }
}
