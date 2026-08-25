package com.vktrsansara.app.caveviewer.engine.maplibre

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.pow

data class TileRange(
    val minTileX: Int,
    val maxTileX: Int,
    val minTileY: Int,
    val maxTileY: Int
) {
    val countX: Int get() = (maxTileX - minTileX + 1).coerceAtLeast(1)
    val countY: Int get() = (maxTileY - minTileY + 1).coerceAtLeast(1)
    val totalTiles: Int get() = countX * countY
}

/**
 * High-precision mathematical engine for Web Mercator tile pyramids and boundary calculations.
 * Ensures seamless parent-child quadtree alignment, eliminating double images and edge artifacts.
 */
object CaveMapBounds {
    const val TILE_SIZE = 256

    /**
     * Calculates the tile index range for a given zoom level, aligned to the global Mercator quadtree.
     */
    fun calculateTileRange(
        imageWidth: Int,
        imageHeight: Int,
        maxZoom: Int,
        zoom: Int
    ): Pair<TileRange, Pair<Double, Double>> {
        val totalWorldPixelsMax = (2.0.pow(maxZoom.toDouble())) * TILE_SIZE.toDouble()
        val imageLeftPxMax = (totalWorldPixelsMax / 2.0) - (imageWidth.toDouble() / 2.0)
        val imageTopPxMax = (totalWorldPixelsMax / 2.0) - (imageHeight.toDouble() / 2.0)

        // Scale factor to current zoom level
        val scale = 2.0.pow((zoom - maxZoom).toDouble())
        val imageLeftPx = imageLeftPxMax * scale
        val imageTopPx = imageTopPxMax * scale
        val scaledW = imageWidth.toDouble() * scale
        val scaledH = imageHeight.toDouble() * scale

        val minTileX = floor(imageLeftPx / TILE_SIZE.toDouble()).toInt()
        val minTileY = floor(imageTopPx / TILE_SIZE.toDouble()).toInt()
        val maxTileX = floor((imageLeftPx + scaledW - 0.001) / TILE_SIZE.toDouble()).toInt()
        val maxTileY = floor((imageTopPx + scaledH - 0.001) / TILE_SIZE.toDouble()).toInt()

        return Pair(
            TileRange(minTileX, maxTileX, minTileY, maxTileY),
            Pair(imageLeftPx, imageTopPx)
        )
    }

    /**
     * Calculates the exact LatLngBounds of the raster image centered at Equator (0, 0).
     */
    fun calculateBounds(
        imageWidth: Int,
        imageHeight: Int,
        tileSize: Int = TILE_SIZE,
        maxZoom: Int
    ): LatLngBounds {
        val totalWorldPixels = (2.0.pow(maxZoom.toDouble())) * tileSize.toDouble()
        val halfW = (imageWidth.toDouble() / (2.0 * totalWorldPixels))
        val halfH = (imageHeight.toDouble() / (2.0 * totalWorldPixels))

        val west = (-halfW * 360.0).coerceIn(-180.0, 180.0)
        val east = (halfW * 360.0).coerceIn(-180.0, 180.0)

        val yNorthFraction = (0.5 - halfH).coerceIn(0.0001, 0.9999)
        val ySouthFraction = (0.5 + halfH).coerceIn(0.0001, 0.9999)

        val north = mercatorYToLat(yNorthFraction)
        val south = mercatorYToLat(ySouthFraction)

        return LatLngBounds.Builder()
            .include(LatLng(north, west))
            .include(LatLng(south, east))
            .build()
    }

    /**
     * Center is strictly (0.0, 0.0) at the Equator.
     */
    fun getCenter(bounds: LatLngBounds): LatLng {
        return LatLng(0.0, 0.0)
    }

    private fun mercatorYToLat(yFraction: Double): Double {
        val yMerc = yFraction * 2.0 * Math.PI
        val latRad = 2.0 * atan(exp(Math.PI - yMerc)) - Math.PI / 2.0
        return Math.toDegrees(latRad)
    }
}
