package com.vktrsansara.app.caveviewer.domain.tile

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import com.vktrsansara.app.caveviewer.data.database.ProjectDatabase
import com.vktrsansara.app.caveviewer.domain.model.MapMetadata
import com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds
import com.vktrsansara.app.caveviewer.engine.maplibre.TileRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.coroutineContext
import kotlin.math.pow
import kotlin.math.roundToInt

data class TileCutProgress(
    val currentTile: Int,
    val totalTiles: Int,
    val currentZoom: Int,
    val zoomMin: Int,
    val zoomMax: Int,
    val progressFraction: Float
)

object TileCutter {

    fun calculateZoomLevels(width: Int, height: Int): Triple<Int, Int, Int> {
        val maxDim = maxOf(width, height)
        val (zoomMin, zoomMax) = when {
            maxDim > 15000 -> 2 to 7
            maxDim > 8000  -> 2 to 6
            maxDim > 4000  -> 2 to 5
            maxDim > 2000  -> 1 to 4
            else           -> 0 to 3
        }
        val zoomDefault = (zoomMin + zoomMax) / 2
        return Triple(zoomMin, zoomMax, zoomDefault)
    }

    suspend fun cutTiles(
        projectName: String,
        projectDir: File,
        sourceBitmap: Bitmap,
        onProgress: (TileCutProgress) -> Unit
    ) = withContext(Dispatchers.IO) {
        val width = sourceBitmap.width
        val height = sourceBitmap.height
        val tileSize = CaveMapBounds.TILE_SIZE

        val (zoomMin, zoomMax, zoomDefault) = calculateZoomLevels(width, height)

        val tilesDir = File(projectDir, "tiles")
        if (tilesDir.exists()) {
            tilesDir.deleteRecursively()
        }
        tilesDir.mkdirs()

        // 1. Calculate total tiles across all zoom levels with global quadtree alignment
        var totalTiles = 0
        val zoomRangeMap = mutableMapOf<Int, Pair<TileRange, Pair<Double, Double>>>()

        for (z in zoomMin..zoomMax) {
            val rangeData = CaveMapBounds.calculateTileRange(width, height, zoomMax, z)
            zoomRangeMap[z] = rangeData
            totalTiles += rangeData.first.totalTiles
        }

        var currentTileCount = 0
        val paint = Paint().apply {
            isFilterBitmap = true
            isAntiAlias = true
            isDither = true
        }

        // 2. High-Quality Progressive Downsampling (Pyramid scaling)
        var currentBitmap = sourceBitmap

        for (z in zoomMax downTo zoomMin) {
            coroutineContext.ensureActive()

            val (range, imageTopLeft) = zoomRangeMap[z]
                ?: CaveMapBounds.calculateTileRange(width, height, zoomMax, z)
            val (imageLeftPx, imageTopPx) = imageTopLeft

            val scale = 2.0.pow((z - zoomMax).toDouble())
            val targetScaledW = (width * scale).roundToInt().coerceAtLeast(1)
            val targetScaledH = (height * scale).roundToInt().coerceAtLeast(1)

            val levelBitmap = if (z == zoomMax) {
                sourceBitmap
            } else {
                Bitmap.createScaledBitmap(currentBitmap, targetScaledW, targetScaledH, true)
            }

            if (currentBitmap != sourceBitmap && currentBitmap != levelBitmap) {
                currentBitmap.recycle()
            }
            currentBitmap = levelBitmap

            for (tileX in range.minTileX..range.maxTileX) {
                val tileDir = File(tilesDir, "$z/$tileX")
                if (!tileDir.exists()) tileDir.mkdirs()

                for (tileY in range.minTileY..range.maxTileY) {
                    coroutineContext.ensureActive()

                    val tileFile = File(tileDir, "$tileY.png")

                    // Create 256x256 ARGB_8888 tile with transparent background
                    val tileBitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(tileBitmap)
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

                    // Calculate the pixel rect inside levelBitmap corresponding to this tile
                    val tileWorldLeft = tileX * tileSize
                    val tileWorldTop = tileY * tileSize

                    val srcX = (tileWorldLeft - imageLeftPx).roundToInt()
                    val srcY = (tileWorldTop - imageTopPx).roundToInt()

                    // Source and Destination clipping with transparent edge padding
                    val srcLeft = srcX.coerceAtLeast(0)
                    val srcTop = srcY.coerceAtLeast(0)
                    val srcRight = (srcX + tileSize).coerceAtMost(levelBitmap.width)
                    val srcBottom = (srcY + tileSize).coerceAtMost(levelBitmap.height)

                    if (srcRight > srcLeft && srcBottom > srcTop) {
                        val dstLeft = (srcLeft - srcX).coerceAtLeast(0)
                        val dstTop = (srcTop - srcY).coerceAtLeast(0)
                        val dstRight = dstLeft + (srcRight - srcLeft)
                        val dstBottom = dstTop + (srcBottom - srcTop)

                        val srcRect = Rect(srcLeft, srcTop, srcRight, srcBottom)
                        val dstRect = Rect(dstLeft, dstTop, dstRight, dstBottom)
                        canvas.drawBitmap(levelBitmap, srcRect, dstRect, paint)
                    }

                    FileOutputStream(tileFile).use { out ->
                        tileBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    tileBitmap.recycle()

                    currentTileCount++
                    val fraction = if (totalTiles > 0) currentTileCount.toFloat() / totalTiles.toFloat() else 1f
                    onProgress(
                        TileCutProgress(
                            currentTile = currentTileCount,
                            totalTiles = totalTiles,
                            currentZoom = z,
                            zoomMin = zoomMin,
                            zoomMax = zoomMax,
                            progressFraction = fraction
                        )
                    )
                }
            }
        }

        if (currentBitmap != sourceBitmap) {
            currentBitmap.recycle()
        }

        // 3. Save metadata into thismap.sqlite
        val dbFile = File(projectDir, "thismap.sqlite")
        val db = ProjectDatabase(dbFile)
        db.saveMetadata(
            MapMetadata(
                projectName = projectName,
                imageWidth = width,
                imageHeight = height,
                tileSize = tileSize,
                zoomMin = zoomMin,
                zoomMax = zoomMax,
                zoomDefault = zoomDefault,
                createdAt = System.currentTimeMillis()
            )
        )
        File(tilesDir, ".v2_aligned").createNewFile()
    }
}
