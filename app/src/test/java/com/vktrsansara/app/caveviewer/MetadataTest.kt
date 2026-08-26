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

    @Test
    fun testSubpixelScaleMeasurement() {
        val p1 = Pair(100.1234, 200.5678)
        val p2 = Pair(242.5088, 200.5678)
        val dx = p2.first - p1.first
        val dy = p2.second - p1.second
        val measuredPixels = kotlin.math.sqrt(dx * dx + dy * dy)
        val formattedPx = String.format(java.util.Locale.US, "%.4f", measuredPixels)
        assertEquals("142.3854", formattedPx)

        val meters = 10.0
        val ppm = measuredPixels / meters
        val formattedPpm = String.format(java.util.Locale.US, "%.4f", ppm)
        assertEquals("14.2385", formattedPpm)
    }

    @Test
    fun testNorthAngleCalculation() {
        // Pointing straight UP -> North (0°)
        val pSouth = Pair(100.0, 200.0)
        val pNorth = Pair(100.0, 100.0)
        val angleNorth = com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds.calculateNorthAngle(pSouth, pNorth)
        assertEquals(0.0, angleNorth, 0.01)

        // Pointing RIGHT -> East (90°)
        val pEast = Pair(200.0, 200.0)
        val angleEast = com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds.calculateNorthAngle(pSouth, pEast)
        assertEquals(90.0, angleEast, 0.01)

        // Pointing DOWN -> South (180°)
        val angleSouth = com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds.calculateNorthAngle(pNorth, pSouth)
        assertEquals(180.0, angleSouth, 0.01)

        // Pointing LEFT -> West (270°)
        val pWest = Pair(0.0, 200.0)
        val angleWest = com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds.calculateNorthAngle(pSouth, pWest)
        assertEquals(270.0, angleWest, 0.01)

        // Pointing UP-RIGHT -> North-East (45°)
        val pNorthEast = Pair(200.0, 100.0)
        val angleNE = com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds.calculateNorthAngle(pSouth, pNorthEast)
        assertEquals(45.0, angleNE, 0.01)
    }

    @Test
    fun testEntranceCoordinateCreationAndFormatting() {
        val entrance = com.vktrsansara.app.caveviewer.domain.model.EntranceCoordinate(
            pointIndex = 0,
            name = "Главный вход",
            lat = 43.123456,
            lon = 40.654321,
            alt = 1350.0
        )
        assertEquals("Главный вход", entrance.name)
        assertEquals("43.123456", String.format(java.util.Locale.US, "%.6f", entrance.lat!!))
        assertEquals("40.654321", String.format(java.util.Locale.US, "%.6f", entrance.lon!!))
    }

    @Test
    fun testGridStepAndCoordinateTransforms() {
        val meta = MapMetadata(
            projectName = "Снежная",
            imageWidth = 2000,
            imageHeight = 4000,
            zoomMin = 1,
            zoomMax = 5,
            zoomDefault = 3,
            pixelsPerMeter = 12.5,
            scaleMeters = 10.0
        )

        // Metadata grid step: 10m * 12.5 px/m = 125 px
        val stepMetadata = meta.scaleMeters * meta.pixelsPerMeter
        assertEquals(125.0, stepMetadata, 0.001)

        // Custom grid step: 50m * 12.5 px/m = 625 px
        val customMeters = 50.0
        val stepCustom = customMeters * meta.pixelsPerMeter
        assertEquals(625.0, stepCustom, 0.001)

        // Top-left pixel (0, 0) conversion to LatLng and back
        val latLngTL = com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds.imagePixelsToLatLng(
            0.0, 0.0, meta.imageWidth, meta.imageHeight, meta.zoomMax
        )
        val pixelTL = com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds.latLngToImagePixels(
            latLngTL, meta.imageWidth, meta.imageHeight, meta.zoomMax
        )
        assertEquals(0.0, pixelTL.first, 0.001)
        assertEquals(0.0, pixelTL.second, 0.001)

        // Bottom-right pixel (2000, 4000) conversion to LatLng and back
        val latLngBR = com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds.imagePixelsToLatLng(
            2000.0, 4000.0, meta.imageWidth, meta.imageHeight, meta.zoomMax
        )
        val pixelBR = com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds.latLngToImagePixels(
            latLngBR, meta.imageWidth, meta.imageHeight, meta.zoomMax
        )
        assertEquals(2000.0, pixelBR.first, 0.001)
        assertEquals(4000.0, pixelBR.second, 0.001)
    }

    @Test
    fun testMeasureUtilsDistanceAndArea() {
        val ppm = 10.0 // 10 px = 1 m

        // 1. Distance tests
        val p1 = Pair(0.0, 0.0)
        val p2 = Pair(30.0, 40.0)
        val distPx = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.distancePx(p1, p2)
        assertEquals(50.0, distPx, 0.001)

        val formattedDist = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.formatDistance(distPx, ppm)
        assertEquals("5.00 м", formattedDist)

        val formattedDistKm = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.formatDistance(15000.0, ppm)
        assertEquals("1.50 км", formattedDistKm)

        val formattedDistUncalibrated = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.formatDistance(50.0, 0.0)
        assertEquals("50 px", formattedDistUncalibrated)

        // 2. Polygon Area tests (100x100 square)
        val squarePoints = listOf(
            Pair(0.0, 0.0),
            Pair(100.0, 0.0),
            Pair(100.0, 100.0),
            Pair(0.0, 100.0)
        )
        val areaPx = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.calculatePolygonAreaPx(squarePoints)
        assertEquals(10000.0, areaPx, 0.001)

        // Area in m2: 10000 px2 / 100 = 100 m2
        val formattedArea = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.formatArea(areaPx, ppm)
        assertEquals("100.00 м²", formattedArea)

        // Area in hectares: 200,000 m2 = 20.00 ha
        val largeAreaPx = 20_000_000.0
        val formattedHa = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.formatArea(largeAreaPx, ppm)
        assertEquals("20.00 га", formattedHa)

        // Perimeter of 100x100 square = 400 px = 40.00 m
        val perimPx = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.calculatePolygonPerimeterPx(squarePoints)
        assertEquals(400.0, perimPx, 0.001)
        val formattedPerim = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.formatDistance(perimPx, ppm)
        assertEquals("40.00 м", formattedPerim)

        // 3. Angle tests
        // Right 90° angle at vertex (0, 0) between (100, 0) and (0, 100)
        val vertex = Pair(0.0, 0.0)
        val ptA = Pair(100.0, 0.0)
        val ptB = Pair(0.0, 100.0)
        val angle90 = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.calculateAngleDegrees(ptA, vertex, ptB)
        assertEquals(90.0, angle90, 0.001)

        // Straight 180° angle
        val ptC = Pair(-100.0, 0.0)
        val angle180 = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.calculateAngleDegrees(ptA, vertex, ptC)
        assertEquals(180.0, angle180, 0.001)

        // 45° angle
        val ptD = Pair(100.0, 100.0)
        val angle45 = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.calculateAngleDegrees(ptA, vertex, ptD)
        assertEquals(45.0, angle45, 0.001)

        // 4. Azimuth & Rumb tests
        val origin = Pair(100.0, 100.0)
        // Straight North on raster (dx = 0, dy = -100): azimuth 0° (when angleNorth = 0)
        val northPt = Pair(100.0, 0.0)
        val azNorth = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.calculateAzimuthDegrees(origin, northPt, 0.0)
        assertEquals(0.0, azNorth, 0.001)

        // East on raster (dx = 100, dy = 0): azimuth 90°
        val eastPt = Pair(200.0, 100.0)
        val azEast = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.calculateAzimuthDegrees(origin, eastPt, 0.0)
        assertEquals(90.0, azEast, 0.001)

        // South on raster (dx = 0, dy = 100): azimuth 180°
        val southPt = Pair(100.0, 200.0)
        val azSouth = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.calculateAzimuthDegrees(origin, southPt, 0.0)
        assertEquals(180.0, azSouth, 0.001)

        // West on raster (dx = -100, dy = 0): azimuth 270°
        val westPt = Pair(0.0, 100.0)
        val azWest = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.calculateAzimuthDegrees(origin, westPt, 0.0)
        assertEquals(270.0, azWest, 0.001)

        // Azimuth with angleNorth correction (map angleNorth = 45°)
        val azNorthCorrected = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.calculateAzimuthDegrees(origin, northPt, 45.0)
        assertEquals(315.0, azNorthCorrected, 0.001)

        // Back azimuth
        assertEquals(180.0, com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.calculateBackAzimuth(0.0), 0.001)
        assertEquals(270.0, com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.calculateBackAzimuth(90.0), 0.001)
        assertEquals(45.0, com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.calculateBackAzimuth(225.0), 0.001)

        // Rumb tests
        val rumbNE = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.calculateRumb(42.5)
        assertEquals("СВ", rumbNE.first)
        assertEquals(42.5, rumbNE.second, 0.001)

        val rumbSE = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.calculateRumb(135.0)
        assertEquals("ЮВ", rumbSE.first)
        assertEquals(45.0, rumbSE.second, 0.001)

        val rumbSW = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.calculateRumb(215.0)
        assertEquals("ЮЗ", rumbSW.first)
        assertEquals(35.0, rumbSW.second, 0.001)

        val rumbNW = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.calculateRumb(310.0)
        assertEquals("СЗ", rumbNW.first)
        assertEquals(50.0, rumbNW.second, 0.001)

        // 5. Infinite Line Bounds tests
        // Line passing through (50, 50) and (150, 150) diagonal across 200x200 map
        val lineBounds = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.calculateInfiniteLineBounds(
            p1 = Pair(50.0, 50.0),
            p2 = Pair(150.0, 150.0),
            mapWidth = 200.0,
            mapHeight = 200.0
        )
        // Should intersect at (0, 0) and (200, 200)
        assertEquals(0.0, lineBounds.first.first, 0.001)
        assertEquals(0.0, lineBounds.first.second, 0.001)
        assertEquals(200.0, lineBounds.second.first, 0.001)
        assertEquals(200.0, lineBounds.second.second, 0.001)

        // 6. Circle Metrics tests
        // Radius = 100 px with ppm = 10.0 px/m => R = 10.00 m, D = 20.00 m, S = pi * 100 = 314.16 m2, C = 2 * pi * 10 = 62.83 m
        val circleMetrics = com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils.calculateCircleMetrics(
            radiusPx = 100.0,
            ppm = 10.0
        )
        assertEquals("10.00 м", circleMetrics.radiusText)
        assertEquals("20.00 м", circleMetrics.diameterText)
        assertEquals("314.16 м²", circleMetrics.areaText)
        assertEquals("62.83 м", circleMetrics.perimeterText)
    }
}
