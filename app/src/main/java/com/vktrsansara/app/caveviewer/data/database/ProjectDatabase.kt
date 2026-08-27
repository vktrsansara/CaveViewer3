package com.vktrsansara.app.caveviewer.data.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.vktrsansara.app.caveviewer.domain.model.CadastralItem
import com.vktrsansara.app.caveviewer.domain.model.EntranceCoordinate
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldDefinition
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldType
import com.vktrsansara.app.caveviewer.domain.model.LayerPoint
import com.vktrsansara.app.caveviewer.domain.model.MapLocation
import com.vktrsansara.app.caveviewer.domain.model.MapMetadata
import com.vktrsansara.app.caveviewer.domain.model.PointLayer
import com.vktrsansara.app.caveviewer.domain.model.PointShape
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * SQLite database manager for the project's thismap.sqlite file.
 */
class ProjectDatabase(private val dbFile: File) {

    init {
        initTable()
    }

    private fun openDatabase(): SQLiteDatabase {
        dbFile.parentFile?.let {
            if (!it.exists()) it.mkdirs()
        }
        val db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        try {
            db.rawQuery("PRAGMA journal_mode=MEMORY", null).close()
            db.rawQuery("PRAGMA synchronous=OFF", null).close()
            db.execSQL("PRAGMA foreign_keys=ON")
        } catch (_: Exception) {}
        return db
    }

    private fun initTable() {
        openDatabase().use { db ->
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS map_metadata (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    project_name TEXT NOT NULL,
                    image_width INTEGER NOT NULL,
                    image_height INTEGER NOT NULL,
                    tile_size INTEGER NOT NULL DEFAULT 256,
                    zoom_min INTEGER NOT NULL,
                    zoom_max INTEGER NOT NULL,
                    zoom_default INTEGER NOT NULL,
                    pixels_per_meter REAL NOT NULL DEFAULT 0.0,
                    scale_meters REAL NOT NULL DEFAULT 0.0,
                    angle_north REAL NOT NULL DEFAULT 0.0,
                    crs TEXT NOT NULL DEFAULT 'Simple',
                    created_at INTEGER NOT NULL
                );
                """.trimIndent()
            )

            // General cave location table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS map_location (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    country TEXT,
                    region TEXT,
                    district TEXT,
                    description TEXT
                );
                """.trimIndent()
            )

            // Entrance GPS coordinates table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS entrance_coordinates (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    point_index INTEGER NOT NULL,
                    name TEXT,
                    lat REAL,
                    lon REAL,
                    alt REAL
                );
                """.trimIndent()
            )

            // Cadastral documentation records table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS cadastral_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    section TEXT NOT NULL,
                    record_order INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    content TEXT NOT NULL
                );
                """.trimIndent()
            )

            // Point layers table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS point_layers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    is_visible INTEGER NOT NULL DEFAULT 1,
                    default_shape TEXT NOT NULL DEFAULT 'CIRCLE',
                    default_color INTEGER NOT NULL DEFAULT -13058824,
                    default_size REAL NOT NULL DEFAULT 6.0,
                    show_labels INTEGER NOT NULL DEFAULT 1,
                    fields_schema_json TEXT NOT NULL DEFAULT '[]',
                    created_at INTEGER NOT NULL
                );
                """.trimIndent()
            )

            // Layer points table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS layer_points (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    layer_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    x REAL NOT NULL,
                    y REAL NOT NULL,
                    shape TEXT NOT NULL DEFAULT 'CIRCLE',
                    color INTEGER NOT NULL DEFAULT -13058824,
                    type_category TEXT,
                    custom_values_json TEXT NOT NULL DEFAULT '{}',
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    FOREIGN KEY(layer_id) REFERENCES point_layers(id) ON DELETE CASCADE
                );
                """.trimIndent()
            )

            // Safe column additions for existing databases
            try { db.execSQL("ALTER TABLE map_metadata ADD COLUMN pixels_per_meter REAL NOT NULL DEFAULT 0.0") } catch (_: Exception) {}
            try { db.execSQL("ALTER TABLE map_metadata ADD COLUMN scale_meters REAL NOT NULL DEFAULT 0.0") } catch (_: Exception) {}
            try { db.execSQL("ALTER TABLE map_metadata ADD COLUMN angle_north REAL NOT NULL DEFAULT 0.0") } catch (_: Exception) {}
            try { db.execSQL("ALTER TABLE map_metadata ADD COLUMN crs TEXT NOT NULL DEFAULT 'Simple'") } catch (_: Exception) {}
        }
    }

    fun updateScaleBinding(pixelsPerMeter: Double, scaleMeters: Double): MapMetadata? {
        val current = getMetadata() ?: return null
        val updated = current.copy(
            pixelsPerMeter = pixelsPerMeter,
            scaleMeters = scaleMeters
        )
        saveMetadata(updated)
        return updated
    }

    fun updateNorthBinding(angleNorth: Double): MapMetadata? {
        val current = getMetadata() ?: return null
        val updated = current.copy(
            angleNorth = angleNorth
        )
        saveMetadata(updated)
        return updated
    }

    fun saveMetadata(metadata: MapMetadata) {
        openDatabase().use { db ->
            val values = ContentValues().apply {
                if (metadata.id > 0) {
                    put("id", metadata.id)
                }
                put("project_name", metadata.projectName)
                put("image_width", metadata.imageWidth)
                put("image_height", metadata.imageHeight)
                put("tile_size", metadata.tileSize)
                put("zoom_min", metadata.zoomMin)
                put("zoom_max", metadata.zoomMax)
                put("zoom_default", metadata.zoomDefault)
                put("pixels_per_meter", metadata.pixelsPerMeter)
                put("scale_meters", metadata.scaleMeters)
                put("angle_north", metadata.angleNorth)
                put("crs", metadata.crs)
                put("created_at", metadata.createdAt)
            }
            db.insertWithOnConflict("map_metadata", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    fun getMetadata(): MapMetadata? {
        if (!dbFile.exists()) return null
        return try {
            openDatabase().use { db ->
                val cursor = db.rawQuery(
                    "SELECT id, project_name, image_width, image_height, tile_size, zoom_min, zoom_max, zoom_default, pixels_per_meter, scale_meters, angle_north, crs, created_at FROM map_metadata ORDER BY id DESC LIMIT 1",
                    null
                )
                cursor.use { c ->
                    if (c.moveToFirst()) {
                        MapMetadata(
                            id = c.getLong(0),
                            projectName = c.getString(1),
                            imageWidth = c.getInt(2),
                            imageHeight = c.getInt(3),
                            tileSize = c.getInt(4),
                            zoomMin = c.getInt(5),
                            zoomMax = c.getInt(6),
                            zoomDefault = c.getInt(7),
                            pixelsPerMeter = c.getDouble(8),
                            scaleMeters = c.getDouble(9),
                            angleNorth = c.getDouble(10),
                            crs = c.getString(11) ?: "Simple",
                            createdAt = c.getLong(12)
                        )
                    } else {
                        null
                    }
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    fun saveLocation(location: MapLocation) {
        openDatabase().use { db ->
            db.execSQL("DELETE FROM map_location")
            val values = ContentValues().apply {
                put("country", location.country)
                put("region", location.region)
                put("district", location.district)
                put("description", location.description)
            }
            db.insert("map_location", null, values)
        }
    }

    fun getLocation(): MapLocation {
        if (!dbFile.exists()) return MapLocation()
        return try {
            openDatabase().use { db ->
                val cursor = db.rawQuery("SELECT country, region, district, description FROM map_location LIMIT 1", null)
                cursor.use { c ->
                    if (c.moveToFirst()) {
                        MapLocation(
                            country = c.getString(0) ?: "",
                            region = c.getString(1) ?: "",
                            district = c.getString(2) ?: "",
                            description = c.getString(3) ?: ""
                        )
                    } else {
                        MapLocation()
                    }
                }
            }
        } catch (_: Exception) {
            MapLocation()
        }
    }

    fun saveEntrances(list: List<EntranceCoordinate>) {
        openDatabase().use { db ->
            db.beginTransaction()
            try {
                db.execSQL("DELETE FROM entrance_coordinates")
                list.forEachIndexed { index, item ->
                    val values = ContentValues().apply {
                        put("point_index", index)
                        put("name", item.name)
                        if (item.lat != null) put("lat", item.lat) else putNull("lat")
                        if (item.lon != null) put("lon", item.lon) else putNull("lon")
                        if (item.alt != null) put("alt", item.alt) else putNull("alt")
                    }
                    db.insert("entrance_coordinates", null, values)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    fun getEntrances(): List<EntranceCoordinate> {
        if (!dbFile.exists()) return emptyList()
        return try {
            openDatabase().use { db ->
                val cursor = db.rawQuery("SELECT point_index, name, lat, lon, alt FROM entrance_coordinates ORDER BY point_index ASC", null)
                val results = mutableListOf<EntranceCoordinate>()
                cursor.use { c ->
                    while (c.moveToNext()) {
                        results.add(
                            EntranceCoordinate(
                                pointIndex = c.getInt(0),
                                name = c.getString(1) ?: "",
                                lat = if (c.isNull(2)) null else c.getDouble(2),
                                lon = if (c.isNull(3)) null else c.getDouble(3),
                                alt = if (c.isNull(4)) null else c.getDouble(4)
                            )
                        )
                    }
                }
                results
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveCadastralData(data: Map<String, List<CadastralItem>>) {
        openDatabase().use { db ->
            db.beginTransaction()
            try {
                db.execSQL("DELETE FROM cadastral_records")
                data.forEach { (section, items) ->
                    items.forEachIndexed { index, item ->
                        val values = ContentValues().apply {
                            put("section", section)
                            put("record_order", index)
                            put("title", item.title)
                            put("content", item.content)
                        }
                        db.insert("cadastral_records", null, values)
                    }
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    fun getCadastralData(): Map<String, List<CadastralItem>> {
        if (!dbFile.exists()) return emptyMap()
        return try {
            openDatabase().use { db ->
                val cursor = db.rawQuery("SELECT id, section, record_order, title, content FROM cadastral_records ORDER BY section ASC, record_order ASC", null)
                val map = mutableMapOf<String, MutableList<CadastralItem>>()
                cursor.use { c ->
                    while (c.moveToNext()) {
                        val id = c.getLong(0)
                        val section = c.getString(1) ?: ""
                        val title = c.getString(3) ?: ""
                        val content = c.getString(4) ?: ""
                        val list = map.getOrPut(section) { mutableListOf() }
                        list.add(CadastralItem(id = id, section = section, title = title, content = content))
                    }
                }
                map
            }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    // ==========================================
    // Point Layers & Layer Points CRUD
    // ==========================================

    private fun serializeFieldsSchema(schema: List<LayerFieldDefinition>): String {
        val array = JSONArray()
        for (field in schema) {
            val obj = JSONObject().apply {
                put("key", field.key)
                put("name", field.name)
                put("type", field.type.name)
                put("defaultValue", field.defaultValue)
                val optionsArray = JSONArray()
                field.options.forEach { optionsArray.put(it) }
                put("options", optionsArray)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun deserializeFieldsSchema(jsonStr: String?): List<LayerFieldDefinition> {
        if (jsonStr.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<LayerFieldDefinition>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val typeStr = obj.optString("type", LayerFieldType.TEXT.name)
                val type = try { LayerFieldType.valueOf(typeStr) } catch (_: Exception) { LayerFieldType.TEXT }
                val optionsArray = obj.optJSONArray("options")
                val options = mutableListOf<String>()
                if (optionsArray != null) {
                    for (j in 0 until optionsArray.length()) {
                        options.add(optionsArray.getString(j))
                    }
                }
                list.add(
                    LayerFieldDefinition(
                        key = obj.optString("key", ""),
                        name = obj.optString("name", ""),
                        type = type,
                        defaultValue = obj.optString("defaultValue", ""),
                        options = options
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun serializeCustomValues(values: Map<String, String>): String {
        val obj = JSONObject()
        values.forEach { (k, v) -> obj.put(k, v) }
        return obj.toString()
    }

    private fun deserializeCustomValues(jsonStr: String?): Map<String, String> {
        if (jsonStr.isNullOrBlank()) return emptyMap()
        return try {
            val obj = JSONObject(jsonStr)
            val map = mutableMapOf<String, String>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = obj.optString(key, "")
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    // --- Point Layers ---

    fun getPointLayers(): List<PointLayer> {
        if (!dbFile.exists()) return emptyList()
        return try {
            openDatabase().use { db ->
                val cursor = db.rawQuery(
                    "SELECT id, name, is_visible, default_shape, default_color, default_size, show_labels, fields_schema_json, created_at FROM point_layers ORDER BY id ASC",
                    null
                )
                val results = mutableListOf<PointLayer>()
                cursor.use { c ->
                    while (c.moveToNext()) {
                        results.add(
                            PointLayer(
                                id = c.getLong(0),
                                name = c.getString(1) ?: "",
                                isVisible = c.getInt(2) != 0,
                                defaultShape = try { PointShape.valueOf(c.getString(3)) } catch (_: Exception) { PointShape.CIRCLE },
                                defaultColor = c.getLong(4),
                                defaultSize = c.getFloat(5),
                                showLabels = c.getInt(6) != 0,
                                fieldsSchema = deserializeFieldsSchema(c.getString(7)),
                                createdAt = c.getLong(8)
                            )
                        )
                    }
                }
                results
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun insertPointLayer(layer: PointLayer): Long {
        return openDatabase().use { db ->
            val values = ContentValues().apply {
                put("name", layer.name)
                put("is_visible", if (layer.isVisible) 1 else 0)
                put("default_shape", layer.defaultShape.name)
                put("default_color", layer.defaultColor)
                put("default_size", layer.defaultSize)
                put("show_labels", if (layer.showLabels) 1 else 0)
                put("fields_schema_json", serializeFieldsSchema(layer.fieldsSchema))
                put("created_at", layer.createdAt)
            }
            db.insert("point_layers", null, values)
        }
    }

    fun updatePointLayer(layer: PointLayer) {
        openDatabase().use { db ->
            val values = ContentValues().apply {
                put("name", layer.name)
                put("is_visible", if (layer.isVisible) 1 else 0)
                put("default_shape", layer.defaultShape.name)
                put("default_color", layer.defaultColor)
                put("default_size", layer.defaultSize)
                put("show_labels", if (layer.showLabels) 1 else 0)
                put("fields_schema_json", serializeFieldsSchema(layer.fieldsSchema))
            }
            db.update("point_layers", values, "id = ?", arrayOf(layer.id.toString()))
        }
    }

    fun deletePointLayer(layerId: Long) {
        openDatabase().use { db ->
            db.delete("layer_points", "layer_id = ?", arrayOf(layerId.toString()))
            db.delete("point_layers", "id = ?", arrayOf(layerId.toString()))
        }
    }

    fun toggleLayerVisibility(layerId: Long, isVisible: Boolean) {
        openDatabase().use { db ->
            val values = ContentValues().apply {
                put("is_visible", if (isVisible) 1 else 0)
            }
            db.update("point_layers", values, "id = ?", arrayOf(layerId.toString()))
        }
    }

    // --- Layer Points ---

    fun getPointsForLayer(layerId: Long): List<LayerPoint> {
        if (!dbFile.exists()) return emptyList()
        return try {
            openDatabase().use { db ->
                val cursor = db.rawQuery(
                    "SELECT id, layer_id, name, x, y, shape, color, type_category, custom_values_json, created_at, updated_at FROM layer_points WHERE layer_id = ? ORDER BY id ASC",
                    arrayOf(layerId.toString())
                )
                val results = mutableListOf<LayerPoint>()
                cursor.use { c ->
                    while (c.moveToNext()) {
                        results.add(
                            LayerPoint(
                                id = c.getLong(0),
                                layerId = c.getLong(1),
                                name = c.getString(2) ?: "",
                                x = c.getDouble(3),
                                y = c.getDouble(4),
                                shape = try { PointShape.valueOf(c.getString(5)) } catch (_: Exception) { PointShape.CIRCLE },
                                color = c.getLong(6),
                                typeCategory = c.getString(7),
                                customValues = deserializeCustomValues(c.getString(8)),
                                createdAt = c.getLong(9),
                                updatedAt = c.getLong(10)
                            )
                        )
                    }
                }
                results
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getAllVisiblePoints(): List<LayerPoint> {
        if (!dbFile.exists()) return emptyList()
        return try {
            openDatabase().use { db ->
                val cursor = db.rawQuery(
                    """
                    SELECT p.id, p.layer_id, p.name, p.x, p.y, p.shape, p.color, p.type_category, p.custom_values_json, p.created_at, p.updated_at 
                    FROM layer_points p 
                    INNER JOIN point_layers l ON p.layer_id = l.id 
                    WHERE l.is_visible = 1 
                    ORDER BY p.id ASC
                    """.trimIndent(),
                    null
                )
                val results = mutableListOf<LayerPoint>()
                cursor.use { c ->
                    while (c.moveToNext()) {
                        results.add(
                            LayerPoint(
                                id = c.getLong(0),
                                layerId = c.getLong(1),
                                name = c.getString(2) ?: "",
                                x = c.getDouble(3),
                                y = c.getDouble(4),
                                shape = try { PointShape.valueOf(c.getString(5)) } catch (_: Exception) { PointShape.CIRCLE },
                                color = c.getLong(6),
                                typeCategory = c.getString(7),
                                customValues = deserializeCustomValues(c.getString(8)),
                                createdAt = c.getLong(9),
                                updatedAt = c.getLong(10)
                            )
                        )
                    }
                }
                results
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun insertLayerPoint(point: LayerPoint): Long {
        return openDatabase().use { db ->
            val values = ContentValues().apply {
                put("layer_id", point.layerId)
                put("name", point.name)
                put("x", point.x)
                put("y", point.y)
                put("shape", point.shape.name)
                put("color", point.color)
                put("type_category", point.typeCategory)
                put("custom_values_json", serializeCustomValues(point.customValues))
                put("created_at", point.createdAt)
                put("updated_at", point.updatedAt)
            }
            db.insert("layer_points", null, values)
        }
    }

    fun updateLayerPoint(point: LayerPoint) {
        openDatabase().use { db ->
            val values = ContentValues().apply {
                put("name", point.name)
                put("x", point.x)
                put("y", point.y)
                put("shape", point.shape.name)
                put("color", point.color)
                put("type_category", point.typeCategory)
                put("custom_values_json", serializeCustomValues(point.customValues))
                put("updated_at", System.currentTimeMillis())
            }
            db.update("layer_points", values, "id = ?", arrayOf(point.id.toString()))
        }
    }

    fun deleteLayerPoint(pointId: Long) {
        openDatabase().use { db ->
            db.delete("layer_points", "id = ?", arrayOf(pointId.toString()))
        }
    }
}

