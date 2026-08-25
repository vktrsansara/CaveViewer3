package com.vktrsansara.app.caveviewer.data.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
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
                    created_at INTEGER NOT NULL
                );
                """.trimIndent()
            )
        }
    }

    fun saveMetadata(metadata: MapMetadata) {
        openDatabase().use { db ->
            val values = ContentValues().apply {
                put("project_name", metadata.projectName)
                put("image_width", metadata.imageWidth)
                put("image_height", metadata.imageHeight)
                put("tile_size", metadata.tileSize)
                put("zoom_min", metadata.zoomMin)
                put("zoom_max", metadata.zoomMax)
                put("zoom_default", metadata.zoomDefault)
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
                    "SELECT id, project_name, image_width, image_height, tile_size, zoom_min, zoom_max, zoom_default, created_at FROM map_metadata ORDER BY id DESC LIMIT 1",
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
                            createdAt = c.getLong(8)
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
}
