package com.vktrsansara.app.caveviewer.data.importer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.vktrsansara.app.caveviewer.data.database.ProjectDatabase
import com.vktrsansara.app.caveviewer.domain.model.EntranceCoordinate
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldDefinition
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldType
import com.vktrsansara.app.caveviewer.domain.model.LayerLine
import com.vktrsansara.app.caveviewer.domain.model.LayerPoint
import com.vktrsansara.app.caveviewer.domain.model.LineEnvironmentType
import com.vktrsansara.app.caveviewer.domain.model.LineLayer
import com.vktrsansara.app.caveviewer.domain.model.MapLocation
import com.vktrsansara.app.caveviewer.domain.model.MapMetadata
import com.vktrsansara.app.caveviewer.domain.model.PointLayer
import com.vktrsansara.app.caveviewer.domain.model.PointShape
import com.vktrsansara.app.caveviewer.domain.tile.TileCutter
import com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile
import kotlin.math.floor
import kotlin.math.pow

object LegacyCaveViewerImporter {

    /**
     * Триггер определения старого проекта:
     * В архиве есть map_metadata.json или metadata.json и НЕТ thismap.sqlite.
     */
    fun isLegacyProject(entryNames: List<String>): Boolean {
        val hasMetadata = entryNames.any {
            it.endsWith("map_metadata.json", ignoreCase = true) ||
            it.endsWith("metadata.json", ignoreCase = true)
        }
        val hasSqlite = entryNames.any { it.endsWith("thismap.sqlite", ignoreCase = true) }
        return hasMetadata && !hasSqlite
    }

    /**
     * Конвертирует старый ZIP-проект CaveViewer V1 в структуру CaveViewer3
     */
    suspend fun importLegacyZip(
        context: Context,
        zipFile: File,
        targetProjectsBaseDir: File,
        onProgress: (progress: Float, statusText: String) -> Unit = { _, _ -> }
    ): Result<String> = withContext(Dispatchers.IO) {
        val tempExtractDir = File(context.cacheDir, "legacy_import_${System.currentTimeMillis()}").apply { mkdirs() }
        var dbInstance: ProjectDatabase? = null
        try {
            onProgress(0.1f, "Распаковка архива CaveViewer V1...")

            // 1. Распаковываем ZIP во временную папку
            ZipFile(zipFile).use { zip ->
                val total = zip.size()
                var count = 0
                val entries = zip.entries()
                val canonicalDestPath = tempExtractDir.canonicalPath
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    count++
                    val file = File(tempExtractDir, entry.name)
                    val canonicalEntryPath = file.canonicalPath
                    if (!canonicalEntryPath.startsWith(canonicalDestPath + File.separator) && canonicalEntryPath != canonicalDestPath) {
                        throw SecurityException("Небезопасный путь в архиве: ${entry.name}")
                    }

                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile?.mkdirs()
                        zip.getInputStream(entry).use { input ->
                            file.outputStream().use { output -> input.copyTo(output) }
                        }
                    }
                    if (count % 20 == 0 || count == total) {
                        onProgress(0.1f + (count.toFloat() / total) * 0.35f, "Распаковка: $count из $total файлов...")
                    }
                }
            }

            onProgress(0.48f, "Чтение структуры и метаданных проекта...")

            // 2. Находим папку с map_metadata.json / metadata.json (с учетом возможной вложенности)
            val metaFile = tempExtractDir.walkTopDown().firstOrNull {
                it.name.equals("map_metadata.json", ignoreCase = true) ||
                it.name.equals("metadata.json", ignoreCase = true)
            } ?: return@withContext Result.failure(IllegalStateException("Файл метаданных (map_metadata.json / metadata.json) не найден в архиве"))

            // 3. Читаем метаданные и формируем имя проекта
            val metaJson = JSONObject(metaFile.readText(Charsets.UTF_8))

            // Имя проекта (V1: name / map_about.name, V2: features.names):
            val projectName = when {
                metaJson.has("name") && metaJson.optString("name").isNotBlank() -> metaJson.optString("name")
                metaJson.optJSONObject("map_about")?.has("name") == true -> metaJson.optJSONObject("map_about")?.optString("name") ?: ""
                metaJson.has("features") -> {
                    val names = metaJson.optJSONObject("features")?.optJSONObject("names")
                    names?.optString("official")?.takeIf { it.isNotBlank() }
                        ?: names?.optString("aliases")?.takeIf { it.isNotBlank() }
                        ?: zipFile.nameWithoutExtension
                }
                else -> zipFile.nameWithoutExtension
            }.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim().ifBlank { zipFile.nameWithoutExtension }

            // 4. Создаем целевую папку в Documents/CaveViewer/Projects/
            var targetDir = File(targetProjectsBaseDir, projectName)
            var counter = 1
            while (targetDir.exists()) {
                targetDir = File(targetProjectsBaseDir, "$projectName ($counter)")
                counter++
            }
            targetDir.mkdirs()

            onProgress(0.55f, "Создание базы данных...")

            // 5. Инициализируем thismap.sqlite
            val dbFile = File(targetDir, "thismap.sqlite")
            val db = ProjectDatabase(dbFile)
            dbInstance = db

            // Параметры карты (V1: map_character, V2: specifications.map):
            val charObj = metaJson.optJSONObject("map_character")
                ?: metaJson.optJSONObject("specifications")?.optJSONObject("map")

            val rawWidth = charObj?.optInt("width", 0) ?: 0
            val rawHeight = charObj?.optInt("height", 0) ?: 0
            val ppm = charObj?.optDouble("pixels_per_meter", 0.0) ?: 0.0
            val scale = charObj?.optDouble("scale", 10.0) ?: 10.0
            val angleNorth = charObj?.optDouble("angle_north", 0.0) ?: 0.0
            val crs = metaJson.optJSONObject("specifications")?.optJSONObject("crs")?.optJSONObject("properties")?.optString("name", "Simple") ?: "Simple"

            // Проверяем наличие папки тайлов
            val tilesSrcDir = tempExtractDir.walkTopDown().firstOrNull {
                it.isDirectory && it.name.equals("tiles", ignoreCase = true)
            }

            var width = rawWidth
            var height = rawHeight

            if (tilesSrcDir != null && tilesSrcDir.exists() && (width <= 0 || height <= 0)) {
                val zoomDirs = tilesSrcDir.listFiles { f -> f.isDirectory }
                    ?.mapNotNull { it.name.toIntOrNull() }
                    ?: emptyList()
                if (zoomDirs.isNotEmpty()) {
                    val maxZ = zoomDirs.maxOrNull() ?: 6
                    val maxZoomDir = File(tilesSrcDir, maxZ.toString())
                    var maxCol = 0
                    var maxRow = 0
                    maxZoomDir.listFiles()?.forEach { item ->
                        if (item.isDirectory) {
                            val col = item.name.toIntOrNull() ?: return@forEach
                            item.listFiles { f -> f.isFile }?.forEach { f ->
                                val row = f.nameWithoutExtension.toIntOrNull() ?: return@forEach
                                if (col > maxCol) maxCol = col
                                if (row > maxRow) maxRow = row
                            }
                        } else if (item.isFile) {
                            val parts = item.nameWithoutExtension.split("_", "-")
                            if (parts.size == 2) {
                                val col = parts[0].toIntOrNull() ?: return@forEach
                                val row = parts[1].toIntOrNull() ?: return@forEach
                                if (col > maxCol) maxCol = col
                                if (row > maxRow) maxRow = row
                            }
                        }
                    }
                    if (width <= 0) width = (maxCol + 1) * CaveMapBounds.TILE_SIZE
                    if (height <= 0) height = (maxRow + 1) * CaveMapBounds.TILE_SIZE
                }
            }

            // Рассчитываем уровни зума исходя из разрешения карты (единый стандарт CaveViewer3)
            val (zoomMin, zoomMax, zoomDefault) = TileCutter.calculateZoomLevels(width, height)

            // Сохраняем оригинальные размеры и стандартизированные зумы CaveViewer3 в SQLite
            db.saveMetadata(
                MapMetadata(
                    projectName = targetDir.name,
                    imageWidth = width,
                    imageHeight = height,
                    tileSize = CaveMapBounds.TILE_SIZE,
                    zoomMin = zoomMin,
                    zoomMax = zoomMax,
                    zoomDefault = zoomDefault,
                    pixelsPerMeter = ppm,
                    scaleMeters = scale,
                    angleNorth = angleNorth,
                    crs = crs
                )
            )

            // 6. Локация и входы (V1: map_geo + map_about, V2: location + features)
            val geoObj = metaJson.optJSONObject("map_geo")
            val locationObj = metaJson.optJSONObject("location")
            val aboutObj = metaJson.optJSONObject("map_about")
            val featuresObj = metaJson.optJSONObject("features")

            val country = geoObj?.optString("country")?.takeIf { it.isNotBlank() }
                ?: locationObj?.optString("country")?.takeIf { it.isNotBlank() }
                ?: "Россия"
            val region = geoObj?.optString("region")?.takeIf { it.isNotBlank() }
                ?: locationObj?.optString("region")?.takeIf { it.isNotBlank() }
                ?: ""
            val district = geoObj?.optString("district")?.takeIf { it.isNotBlank() }
                ?: locationObj?.optString("district")?.takeIf { it.isNotBlank() }
                ?: ""
            val description = aboutObj?.optString("description")?.takeIf { it.isNotBlank() }
                ?: featuresObj?.optString("description")?.takeIf { it.isNotBlank() }
                ?: ""

            db.saveLocation(
                MapLocation(
                    country = country,
                    region = region,
                    district = district,
                    description = description
                )
            )

            val entrances = mutableListOf<EntranceCoordinate>()

            // V1: GPS координаты входа (map_geo.gps):
            val gpsObj = geoObj?.optJSONObject("gps")
            if (gpsObj != null) {
                val lat = gpsObj.optString("lat").toDoubleOrNull() ?: (if (!gpsObj.isNull("lat")) gpsObj.optDouble("lat") else null)
                val lon = gpsObj.optString("lon").toDoubleOrNull() ?: (if (!gpsObj.isNull("lon")) gpsObj.optDouble("lon") else null)
                val alt = gpsObj.optString("alt").toDoubleOrNull() ?: (if (!gpsObj.isNull("alt")) gpsObj.optDouble("alt") else null)
                if (lat != null && lon != null) {
                    entrances.add(
                        EntranceCoordinate(
                            pointIndex = 1,
                            name = "Главный вход",
                            lat = lat,
                            lon = lon,
                            alt = alt
                        )
                    )
                }
            }

            // V2: массив coordinates:
            val coordsArr = locationObj?.optJSONArray("coordinates")
            if (coordsArr != null) {
                for (i in 0 until coordsArr.length()) {
                    val c = coordsArr.getJSONObject(i)
                    entrances.add(
                        EntranceCoordinate(
                            pointIndex = entrances.size + 1,
                            name = "Вход ${entrances.size + 1}",
                            lat = if (!c.isNull("lat")) c.getDouble("lat") else null,
                            lon = if (!c.isNull("lon")) c.getDouble("lon") else null,
                            alt = if (!c.isNull("alt")) c.getDouble("alt") else null
                        )
                    )
                }
            }

            if (entrances.isNotEmpty()) {
                db.saveEntrances(entrances)
            }

            onProgress(0.60f, "Импорт слоев точек и пикетов...")

            // 7. Конвертируем layer.points.geojson / map_points.geojson -> Point Layers в SQLite
            val pointsFile = tempExtractDir.walkTopDown().firstOrNull {
                it.name.equals("map_points.geojson", ignoreCase = true) ||
                it.name.equals("layer.points.geojson", ignoreCase = true) ||
                it.name.equals("points.geojson", ignoreCase = true)
            }
            if (pointsFile != null && pointsFile.exists()) {
                convertPointsGeoJson(pointsFile, db)
            }

            onProgress(0.68f, "Импорт векторных ходов и линий...")

            // 8. Конвертируем layer.lines.geojson / map_path.geojson -> Line Layers в SQLite
            val linesFile = tempExtractDir.walkTopDown().firstOrNull {
                it.name.equals("map_path.geojson", ignoreCase = true) ||
                it.name.equals("map_lines.geojson", ignoreCase = true) ||
                it.name.equals("layer.lines.geojson", ignoreCase = true) ||
                it.name.equals("lines.geojson", ignoreCase = true) ||
                it.name.equals("path.geojson", ignoreCase = true)
            }
            if (linesFile != null && linesFile.exists()) {
                convertLinesGeoJson(linesFile, db, ppm)
            }

            // 9. Сохраняем исходное изображение (если есть) в map/image.png без загрузки в память
            val imageFile = tempExtractDir.walkTopDown().firstOrNull {
                it.isFile && (it.name.equals("image.png", ignoreCase = true) ||
                             it.name.equals("image.jpg", ignoreCase = true) ||
                             it.name.equals("image.jpeg", ignoreCase = true))
            }
            if (imageFile != null) {
                val mapDstDir = File(targetDir, "map").apply { mkdirs() }
                try {
                    imageFile.copyTo(File(mapDstDir, "image.png"), overwrite = true)
                } catch (_: Exception) {}
            }

            // 10. Субпиксельное потайловое выравнивание Web Mercator без OutOfMemory
            val tilesDstDir = File(targetDir, "tiles")
            if (tilesSrcDir != null && tilesSrcDir.exists() && width > 0 && height > 0) {
                onProgress(0.75f, "Субпиксельное выравнивание тайлов...")
                realignTilesForMercator(
                    srcDir = tilesSrcDir,
                    dstDir = tilesDstDir,
                    width = width,
                    height = height,
                    zoomMin = zoomMin,
                    zoomMax = zoomMax,
                    onProgress = { p, text ->
                        onProgress(0.75f + p * 0.23f, text)
                    }
                )
            } else if (imageFile != null && width > 0 && height > 0) {
                // Если исходных тайлов не было, нарезаем из файла изображения
                onProgress(0.75f, "Генерация тайлов Web Mercator...")
                val bmp = BitmapFactory.decodeFile(imageFile.absolutePath)
                if (bmp != null) {
                    TileCutter.cutTiles(
                        projectName = targetDir.name,
                        projectDir = targetDir,
                        sourceBitmap = bmp,
                        onProgress = { cutProgress ->
                            onProgress(
                                0.75f + cutProgress.progressFraction * 0.23f,
                                "Генерация тайлов: зум ${cutProgress.currentZoom} (${(cutProgress.progressFraction * 100).toInt()}%)..."
                            )
                        }
                    )
                    bmp.recycle()
                }
                File(tilesDstDir, ".v3_aligned").writeText("v3")
            } else {
                throw IllegalStateException("В архиве проекта не найдены ни папка с тайлами tiles/, ни изображение карты")
            }

            onProgress(1.0f, "Конвертация успешно завершена!")

            Result.success(targetDir.name)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            dbInstance?.close()
            // 11. Очистка временных файлов
            try {
                tempExtractDir.deleteRecursively()
            } catch (_: Exception) {}
        }
    }

    private fun realignTilesForMercator(
        srcDir: File,
        dstDir: File,
        width: Int,
        height: Int,
        zoomMin: Int,
        zoomMax: Int,
        onProgress: (progress: Float, statusText: String) -> Unit = { _, _ -> }
    ) {
        dstDir.mkdirs()
        val tileSize = CaveMapBounds.TILE_SIZE.toDouble()
        val totalWorldPixelsMax = (2.0.pow(zoomMax.toDouble())) * tileSize
        val imageLeftPxMax = (totalWorldPixelsMax - width.toDouble()) / 2.0
        val imageTopPxMax = (totalWorldPixelsMax - height.toDouble()) / 2.0
        val paint = Paint().apply {
            isFilterBitmap = true
            isAntiAlias = true
        }

        val oldZoomDirs = srcDir.listFiles { f -> f.isDirectory }
            ?.mapNotNull { it.name.toIntOrNull() }
            ?.sorted() ?: emptyList()

        val oldMaxZoom = oldZoomDirs.maxOrNull() ?: zoomMax
        val oldMinZoom = oldZoomDirs.minOrNull() ?: 2
        val zoomShift = zoomMax - oldMaxZoom

        val totalZooms = (zoomMax - zoomMin + 1).coerceAtLeast(1)

        for ((zoomIdx, z) in (zoomMax downTo zoomMin).withIndex()) {
            val z_old = z - zoomShift
            val srcZoomDir = if (z_old >= oldMinZoom) File(srcDir, z_old.toString()) else null
            val hasOldTiles = srcZoomDir != null && srcZoomDir.exists()

            onProgress(
                zoomIdx.toFloat() / totalZooms,
                "Выравнивание тайлов: зум $z из $zoomMax..."
            )

            val scale = 2.0.pow((z - zoomMax).toDouble())
            val scaledW = width.toDouble() * scale
            val scaledH = height.toDouble() * scale
            val imageLeftPx = imageLeftPxMax * scale
            val imageTopPx = imageTopPxMax * scale
            val minTileX = floor(imageLeftPx / tileSize).toInt()
            val minTileY = floor(imageTopPx / tileSize).toInt()
            val maxTileX = floor((imageLeftPx + scaledW - 0.001) / tileSize).toInt()
            val maxTileY = floor((imageTopPx + scaledH - 0.001) / tileSize).toInt()

            if (hasOldTiles) {
                // Прямой субпиксельный перенос исходных тайлов на сетку Web Mercator
                val maxOldCol = floor((scaledW - 0.001) / tileSize).toInt()
                val maxOldRow = floor((scaledH - 0.001) / tileSize).toInt()

                for (tx in minTileX..maxTileX) {
                    val targetColDir = File(dstDir, "$z/$tx").apply { mkdirs() }
                    val tileWorldLeft = tx * tileSize
                    for (ty in minTileY..maxTileY) {
                        val tileWorldTop = ty * tileSize
                        val srcX0 = tileWorldLeft - imageLeftPx
                        val srcY0 = tileWorldTop - imageTopPx
                        val srcX1 = srcX0 + tileSize
                        val srcY1 = srcY0 + tileSize
                        val oldColMin = maxOf(0, floor(srcX0 / tileSize).toInt())
                        val oldColMax = minOf(maxOldCol, floor((srcX1 - 0.001) / tileSize).toInt())
                        val oldRowMin = maxOf(0, floor(srcY0 / tileSize).toInt())
                        val oldRowMax = minOf(maxOldRow, floor((srcY1 - 0.001) / tileSize).toInt())

                        if (oldColMin > oldColMax || oldRowMin > oldRowMax) {
                            continue
                        }

                        var targetBitmap: Bitmap? = null
                        var canvas: Canvas? = null
                        var hasDrawn = false

                        for (c in oldColMin..oldColMax) {
                            for (r in oldRowMin..oldRowMax) {
                                val oldTileFile = findOldTileFile(srcZoomDir!!, c, r)
                                if (oldTileFile != null) {
                                    val oldBmp = BitmapFactory.decodeFile(oldTileFile.absolutePath)
                                    if (oldBmp != null) {
                                        if (targetBitmap == null) {
                                            targetBitmap = Bitmap.createBitmap(
                                                CaveMapBounds.TILE_SIZE,
                                                CaveMapBounds.TILE_SIZE,
                                                Bitmap.Config.ARGB_8888
                                            )
                                            canvas = Canvas(targetBitmap)
                                        }
                                        val drawX = (c * tileSize + imageLeftPx - tileWorldLeft).toFloat()
                                        val drawY = (r * tileSize + imageTopPx - tileWorldTop).toFloat()
                                        canvas!!.drawBitmap(oldBmp, drawX, drawY, paint)
                                        oldBmp.recycle()
                                        hasDrawn = true
                                    }
                                }
                            }
                        }

                        if (targetBitmap != null && hasDrawn) {
                            val outTileFile = File(targetColDir, "$ty.png")
                            FileOutputStream(outTileFile).use { out ->
                                targetBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                            }
                            targetBitmap.recycle()
                        }
                    }
                }
            } else {
                // Понижающая прогрессивная перенарезка (downsampling) из тайлов уровня (z + 1)
                for (tx in minTileX..maxTileX) {
                    val targetColDir = File(dstDir, "$z/$tx").apply { mkdirs() }
                    for (ty in minTileY..maxTileY) {
                        val children = listOf(
                            Pair(File(dstDir, "${z + 1}/${2 * tx}/${2 * ty}.png"), RectF(0f, 0f, 128f, 128f)),
                            Pair(File(dstDir, "${z + 1}/${2 * tx + 1}/${2 * ty}.png"), RectF(128f, 0f, 256f, 128f)),
                            Pair(File(dstDir, "${z + 1}/${2 * tx}/${2 * ty + 1}.png"), RectF(0f, 128f, 128f, 256f)),
                            Pair(File(dstDir, "${z + 1}/${2 * tx + 1}/${2 * ty + 1}.png"), RectF(128f, 128f, 256f, 256f))
                        )

                        var targetBitmap: Bitmap? = null
                        var canvas: Canvas? = null
                        var hasDrawn = false

                        for ((childFile, dstRect) in children) {
                            if (childFile.exists()) {
                                val childBmp = BitmapFactory.decodeFile(childFile.absolutePath)
                                if (childBmp != null) {
                                    if (targetBitmap == null) {
                                        targetBitmap = Bitmap.createBitmap(
                                            CaveMapBounds.TILE_SIZE,
                                            CaveMapBounds.TILE_SIZE,
                                            Bitmap.Config.ARGB_8888
                                        )
                                        canvas = Canvas(targetBitmap)
                                    }
                                    canvas!!.drawBitmap(childBmp, null, dstRect, paint)
                                    childBmp.recycle()
                                    hasDrawn = true
                                }
                            }
                        }

                        if (targetBitmap != null && hasDrawn) {
                            val outTileFile = File(targetColDir, "$ty.png")
                            FileOutputStream(outTileFile).use { out ->
                                targetBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                            }
                            targetBitmap.recycle()
                        }
                    }
                }
            }
        }
        File(dstDir, ".v3_aligned").writeText("v3")
    }

    private fun findOldTileFile(zoomDir: File, col: Int, row: Int): File? {
        val fPng = File(zoomDir, "$col/$row.png")
        if (fPng.exists()) return fPng
        val fJpg = File(zoomDir, "$col/$row.jpg")
        if (fJpg.exists()) return fJpg
        val fJpeg = File(zoomDir, "$col/$row.jpeg")
        if (fJpeg.exists()) return fJpeg

        val fFlatPng = File(zoomDir, "${col}_${row}.png")
        if (fFlatPng.exists()) return fFlatPng
        val fFlatJpg = File(zoomDir, "${col}_${row}.jpg")
        if (fFlatJpg.exists()) return fFlatJpg
        val fFlatDash = File(zoomDir, "${col}-${row}.png")
        if (fFlatDash.exists()) return fFlatDash

        return null
    }

    private fun convertPointsGeoJson(file: File, db: ProjectDatabase) {
        val root = JSONObject(file.readText(Charsets.UTF_8))
        val features = root.optJSONArray("features") ?: return
        val schema = listOf(
            LayerFieldDefinition("dist", "Район", LayerFieldType.TEXT),
            LayerFieldDefinition("description", "Описание", LayerFieldType.TEXT),
            LayerFieldDefinition("hazard", "Опасно", LayerFieldType.BOOLEAN)
        )
        val picketLayerId = db.insertPointLayer(PointLayer(name = "Пикеты (Станции)", defaultShape = PointShape.CIRCLE, defaultColor = 0xFF38BDF8, fieldsSchema = schema))
        val interestLayerId = db.insertPointLayer(PointLayer(name = "Интересные места", defaultShape = PointShape.STAR, defaultColor = 0xFF10B981, fieldsSchema = schema))
        val entranceLayerId = db.insertPointLayer(PointLayer(name = "Входы", defaultShape = PointShape.TRIANGLE_UP, defaultColor = 0xFFEF4444, fieldsSchema = schema))
        val otherLayerId = db.insertPointLayer(PointLayer(name = "Прочие точки", defaultShape = PointShape.DIAMOND, defaultColor = 0xFFF59E0B, fieldsSchema = schema))

        for (i in 0 until features.length()) {
            val f = features.getJSONObject(i)
            val props = f.optJSONObject("properties") ?: JSONObject()
            val coords = f.optJSONObject("geometry")?.optJSONArray("coordinates") ?: continue
            val name = props.optString("name", "Точка ${i + 1}")
            val type = props.optString("type", "").lowercase()
            val (lid, shape, color) = when {
                type.contains("entrance") || name.lowercase().contains("вход") -> Triple(entranceLayerId, PointShape.TRIANGLE_UP, 0xFFEF4444)
                type.contains("station") || name.all { it.isDigit() } -> Triple(picketLayerId, PointShape.CIRCLE, 0xFF38BDF8)
                type.contains("interest") || name.lowercase().contains("грот") -> Triple(interestLayerId, PointShape.STAR, 0xFF10B981)
                else -> Triple(otherLayerId, PointShape.DIAMOND, 0xFFF59E0B)
            }
            val custom = mutableMapOf<String, String>()
            if (props.has("dist")) custom["dist"] = props.optString("dist")
            if (props.has("description")) custom["description"] = props.optString("description")
            if (props.has("hazard")) custom["hazard"] = props.optBoolean("hazard").toString()
            db.insertLayerPoint(
                LayerPoint(
                    layerId = lid,
                    name = name,
                    x = coords.getDouble(0),
                    y = coords.getDouble(1),
                    shape = shape,
                    color = color,
                    customValues = custom
                )
            )
        }
    }

    private fun convertLinesGeoJson(file: File, db: ProjectDatabase, ppm: Double) {
        val root = JSONObject(file.readText(Charsets.UTF_8))
        val features = root.optJSONArray("features") ?: return
        val schema = listOf(
            LayerFieldDefinition("height", "Высота свода (м)", LayerFieldType.NUMBER),
            LayerFieldDefinition("width", "Ширина хода (м)", LayerFieldType.NUMBER),
            LayerFieldDefinition("description", "Описание", LayerFieldType.TEXT)
        )
        val mainLineLayerId = db.insertLineLayer(
            LineLayer(
                name = "Ходы пещеры",
                defaultWidth = 3.0f,
                defaultHaloWidth = 4.0f,
                isHeatmapEnabled = true,
                fieldsSchema = schema
            )
        )
        for (i in 0 until features.length()) {
            val f = features.getJSONObject(i)
            val props = f.optJSONObject("properties") ?: JSONObject()
            val geom = f.optJSONObject("geometry") ?: continue
            val coordsArr = geom.optJSONArray("coordinates") ?: continue
            val rawList = mutableListOf<Pair<Double, Double>>()
            // Поддержка LineString и MultiLineString
            if (coordsArr.length() > 0 && coordsArr.get(0) is org.json.JSONArray) {
                val firstEl = coordsArr.getJSONArray(0)
                val targetArr = if (firstEl.length() > 0 && firstEl.get(0) is org.json.JSONArray) firstEl else coordsArr
                for (k in 0 until targetArr.length()) {
                    val pt = targetArr.getJSONArray(k)
                    rawList.add(Pair(pt.getDouble(0), pt.getDouble(1)))
                }
            }
            if (rawList.size < 2) continue
            var lenPx = 0.0
            for (k in 0 until rawList.size - 1) {
                val dx = rawList[k+1].first - rawList[k].first
                val dy = rawList[k+1].second - rawList[k].second
                lenPx += Math.sqrt(dx*dx + dy*dy)
            }
            val lenM = if (props.has("length")) props.getDouble("length") else (if (ppm > 0) lenPx / ppm else 0.0)
            val diff = props.optDouble("value", 1.0).toFloat().coerceIn(0.0f, 8.0f)
            val lType = props.optString("type", "").lowercase()
            val env = when {
                lType.contains("water") -> LineEnvironmentType.WATER
                lType.contains("clay") -> LineEnvironmentType.CLAY
                lType.contains("block") || lType.contains("завал") -> LineEnvironmentType.BOULDER
                else -> LineEnvironmentType.NONE
            }
            val custom = mutableMapOf<String, String>()
            if (props.has("height")) custom["height"] = props.optString("height")
            if (props.has("width")) custom["width"] = props.optString("width")
            if (props.has("description")) custom["description"] = props.optString("description")
            db.insertLayerLine(
                LayerLine(
                    layerId = mainLineLayerId,
                    name = props.optString("description", "Ход ${i + 1}"),
                    points = rawList,
                    lengthMeters = lenM,
                    lengthPx = lenPx,
                    difficulty = diff,
                    environmentType = env,
                    customValues = custom
                )
            )
        }
    }
}
