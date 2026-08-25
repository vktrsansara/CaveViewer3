package com.vktrsansara.app.caveviewer.data.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.vktrsansara.app.caveviewer.domain.model.CadastralItem
import com.vktrsansara.app.caveviewer.domain.model.EntranceCoordinate
import com.vktrsansara.app.caveviewer.domain.model.MapLocation
import com.vktrsansara.app.caveviewer.domain.model.MapMetadata
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

            // Safe column additions for existing databases
            try { db.execSQL("ALTER TABLE map_metadata ADD COLUMN pixels_per_meter REAL NOT NULL DEFAULT 0.0") } catch (_: Exception) {}
            try { db.execSQL("ALTER TABLE map_metadata ADD COLUMN scale_meters REAL NOT NULL DEFAULT 0.0") } catch (_: Exception) {}
            try { db.execSQL("ALTER TABLE map_metadata ADD COLUMN angle_north REAL NOT NULL DEFAULT 0.0") } catch (_: Exception) {}
            try { db.execSQL("ALTER TABLE map_metadata ADD COLUMN crs TEXT NOT NULL DEFAULT 'Simple'") } catch (_: Exception) {}
        }
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
}
