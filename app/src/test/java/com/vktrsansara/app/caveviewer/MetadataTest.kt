package com.vktrsansara.app.caveviewer

import com.vktrsansara.app.caveviewer.domain.model.EntranceCoordinate
import com.vktrsansara.app.caveviewer.domain.model.MapLocation
import com.vktrsansara.app.caveviewer.domain.model.MapMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class MetadataTest {

    @Test
    fun testMapMetadataDefaultsAndUpdates() {
        val meta = MapMetadata(
            projectName = "Снежная",
            imageWidth = 5000,
            imageHeight = 3000,
            zoomMin = 1,
            zoomMax = 5,
            zoomDefault = 3
        )

        assertEquals("Снежная", meta.projectName)
        assertEquals(0.0, meta.pixelsPerMeter, 0.0001)
        assertEquals(0.0, meta.scaleMeters, 0.0001)
        assertEquals(0.0, meta.angleNorth, 0.0001)
        assertEquals("Simple", meta.crs)

        val updated = meta.copy(
            projectName = "Снежная_новая",
            pixelsPerMeter = 14.5,
            scaleMeters = 10.0,
            angleNorth = 45.0,
            crs = "Simple"
        )

        assertEquals("Снежная_новая", updated.projectName)
        assertEquals(14.5, updated.pixelsPerMeter, 0.0001)
        assertEquals(10.0, updated.scaleMeters, 0.0001)
        assertEquals(45.0, updated.angleNorth, 0.0001)
        assertEquals("Simple", updated.crs)
        assertNotEquals(meta, updated)
    }

    @Test
    fun testSanitizeProjectName() {
        val rawName = "  Снежная / План * 2026?  "
        val cleanName = rawName.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_")
        assertEquals("Снежная _ План _ 2026_", cleanName)
    }

    @Test
    fun testMapLocationAndEntranceCoordinateModels() {
        val location = MapLocation(
            country = "Россия",
            region = "Краснодарский край",
            district = "Сочинский",
            description = "Хребет Алек, поляна Куйбышева"
        )
        assertEquals("Россия", location.country)
        assertEquals("Краснодарский край", location.region)
        assertEquals("Сочинский", location.district)
        assertEquals("Хребет Алек, поляна Куйбышева", location.description)

        val entrances = listOf(
            EntranceCoordinate(pointIndex = 0, name = "Главный вход", lat = 43.123456, lon = 40.654321, alt = 1350.0),
            EntranceCoordinate(pointIndex = 1, name = "Провал №2", lat = 43.123890, lon = 40.654890, alt = 1380.0)
        )
        assertEquals(2, entrances.size)
        assertEquals("Главный вход", entrances[0].name)
        assertEquals(43.123456, entrances[0].lat!!, 0.000001)
        assertEquals(40.654321, entrances[0].lon!!, 0.000001)
        assertEquals(1350.0, entrances[0].alt!!, 0.1)

        val reindexed = entrances.filter { it.pointIndex != 0 }.mapIndexed { idx, item -> item.copy(pointIndex = idx) }
        assertEquals(1, reindexed.size)
        assertEquals(0, reindexed[0].pointIndex)
        assertEquals("Провал №2", reindexed[0].name)
    }
}
