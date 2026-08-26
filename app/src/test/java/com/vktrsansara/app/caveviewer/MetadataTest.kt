package com.vktrsansara.app.caveviewer

import com.vktrsansara.app.caveviewer.domain.model.AppSettings
import com.vktrsansara.app.caveviewer.domain.model.CadastralItem
import com.vktrsansara.app.caveviewer.domain.model.CadastralSection
import com.vktrsansara.app.caveviewer.domain.model.EntranceCoordinate
import com.vktrsansara.app.caveviewer.domain.model.MapLocation
import com.vktrsansara.app.caveviewer.domain.model.MapMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

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

    @Test
    fun testCadastralModels() {
        assertEquals(7, CadastralSection.entries.size)
        assertEquals("classification", CadastralSection.CLASSIFICATION.key)
        assertEquals("topology", CadastralSection.TOPOLOGY.key)
        assertEquals("morphology", CadastralSection.MORPHOLOGY.key)
        assertEquals("climate", CadastralSection.CLIMATE.key)
        assertEquals("hydrology", CadastralSection.HYDROLOGY.key)
        assertEquals("biota", CadastralSection.BIOTA.key)
        assertEquals("description", CadastralSection.DESCRIPTION.key)

        val item = CadastralItem(
            id = 1,
            section = CadastralSection.CLASSIFICATION.key,
            title = "Генезис полости",
            content = "Карстовая полость коррозионно-эрозионного типа"
        )
        assertEquals("classification", item.section)
        assertEquals("Генезис полости", item.title)
        assertEquals("Карстовая полость коррозионно-эрозионного типа", item.content)
    }

    @Test
    fun testScaleBarMath() {
        val pixelsPerMeter = 9.0
        val zoomMax = 5
        val currentZoom = 5.0 // at zoomMax

        // At zoomMax in MapLibre with 256px tiles, 1 image pixel = 2.0 dp
        val dpPerMeter = pixelsPerMeter * 2.0.pow(currentZoom - zoomMax.toDouble() + 1.0)
        assertEquals(18.0, dpPerMeter, 0.0001)

        // 10 meters on the map image = 90 pixels = 180 DP on screen
        val tenMetersDp = 10.0 * dpPerMeter
        assertEquals(180.0, tenMetersDp, 0.0001)

        // At zoomMax - 1 (zoomed out by 1 level)
        val zoomedOutDpPerMeter = pixelsPerMeter * 2.0.pow((zoomMax - 1.0) - zoomMax.toDouble() + 1.0)
        assertEquals(9.0, zoomedOutDpPerMeter, 0.0001)
        assertEquals(90.0, 10.0 * zoomedOutDpPerMeter, 0.0001)
    }

    @Test
    fun testAppSettingsCursorDefaults() {
        val settings = AppSettings()
        assertTrue(settings.cursorShow)
        assertEquals(1, settings.cursorType)
        assertEquals(0xFFEF4444L, settings.cursorColor)

        val custom = settings.copy(
            cursorShow = false,
            cursorType = 5,
            cursorColor = 0xFF10B981L
        )
        assertEquals(false, custom.cursorShow)
        assertEquals(5, custom.cursorType)
        assertEquals(0xFF10B981L, custom.cursorColor)
    }

    @Test
    fun testLatLngToImagePixelsConversion() {
        val width = 4000
        val height = 2000
        val maxZoom = 5
        val (pxX, pxY) = com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds.latLngToImagePixels(
            latLng = org.maplibre.android.geometry.LatLng(0.0, 0.0),
            imageWidth = width,
            imageHeight = height,
            maxZoom = maxZoom
        )
        // Center of map at (0, 0) should be exactly half-width and half-height
        assertEquals(2000.0, pxX, 0.01)
        assertEquals(1000.0, pxY, 0.01)
    }
}
