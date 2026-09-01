package com.vktrsansara.app.caveviewer.engine.maplibre

import com.vktrsansara.app.caveviewer.domain.measure.LineColorUtils
import com.vktrsansara.app.caveviewer.domain.model.LayerLine
import com.vktrsansara.app.caveviewer.domain.model.LayerPoint
import com.vktrsansara.app.caveviewer.domain.model.LineLayer
import com.vktrsansara.app.caveviewer.domain.model.MapMetadata
import com.vktrsansara.app.caveviewer.domain.model.PointLayer
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer as MapLibreLineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

/**
 * GPU-accelerated vector layer manager for MapLibre Native SDK.
 * Renders saved lines, halos, dashed/dotted styles, point markers, and labels directly on the GPU
 * via GeoJsonSource, completely offloading the UI thread and Compose Canvas.
 */
object MapLibreVectorManager {
    const val SOURCE_CAVE_LINES = "cave-lines-source"
    const val SOURCE_CAVE_POINTS = "cave-points-source"

    const val LAYER_LINES_HALO = "cave-lines-halo-layer"
    const val LAYER_LINES_SOLID = "cave-lines-layer"
    const val LAYER_LINES_DASHED = "cave-lines-dashed-layer"
    const val LAYER_LINES_DOTTED = "cave-lines-dotted-layer"
    const val LAYER_POINTS_CIRCLE = "cave-points-circle-layer"
    const val LAYER_POINTS_LABELS = "cave-points-labels-layer"

    private val ALL_LINE_LAYERS = listOf(
        LAYER_LINES_HALO,
        LAYER_LINES_SOLID,
        LAYER_LINES_DASHED,
        LAYER_LINES_DOTTED
    )

    private val ALL_POINT_LAYERS = listOf(
        LAYER_POINTS_CIRCLE,
        LAYER_POINTS_LABELS
    )

    /**
     * Initializes GeoJson sources and GPU layers in MapLibre Style if not already present.
     */
    fun setupVectorLayers(
        style: Style,
        isLineLayersVisible: Boolean,
        isPointLayersVisible: Boolean,
        initialLines: FeatureCollection? = null,
        initialPoints: FeatureCollection? = null
    ) {
        val emptyFc = FeatureCollection.fromFeatures(emptyList())

        // 1. Sources
        if (style.getSource(SOURCE_CAVE_LINES) == null) {
            style.addSource(GeoJsonSource(SOURCE_CAVE_LINES, initialLines ?: emptyFc))
        } else if (initialLines != null) {
            style.getSourceAs<GeoJsonSource>(SOURCE_CAVE_LINES)?.setGeoJson(initialLines)
        }

        if (style.getSource(SOURCE_CAVE_POINTS) == null) {
            style.addSource(GeoJsonSource(SOURCE_CAVE_POINTS, initialPoints ?: emptyFc))
        } else if (initialPoints != null) {
            style.getSourceAs<GeoJsonSource>(SOURCE_CAVE_POINTS)?.setGeoJson(initialPoints)
        }

        val lineVisibility = if (isLineLayersVisible) Property.VISIBLE else Property.NONE
        val pointVisibility = if (isPointLayersVisible) Property.VISIBLE else Property.NONE

        // 2. Halo Layer (renders below main lines)
        if (style.getLayer(LAYER_LINES_HALO) == null) {
            val haloLayer = MapLibreLineLayer(LAYER_LINES_HALO, SOURCE_CAVE_LINES).apply {
                setFilter(Expression.has("halo-color"))
                setProperties(
                    PropertyFactory.lineColor(Expression.toColor(Expression.get("halo-color"))),
                    PropertyFactory.lineWidth(Expression.toNumber(Expression.get("halo-width"))),
                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                    PropertyFactory.visibility(lineVisibility)
                )
            }
            style.addLayer(haloLayer)
        }

        // 3. Solid Line Layer
        if (style.getLayer(LAYER_LINES_SOLID) == null) {
            val solidLayer = MapLibreLineLayer(LAYER_LINES_SOLID, SOURCE_CAVE_LINES).apply {
                setFilter(
                    Expression.all(
                        Expression.neq(Expression.get("style"), Expression.literal("DASHED")),
                        Expression.neq(Expression.get("style"), Expression.literal("DOTTED"))
                    )
                )
                setProperties(
                    PropertyFactory.lineColor(Expression.toColor(Expression.get("line-color"))),
                    PropertyFactory.lineWidth(Expression.toNumber(Expression.get("line-width"))),
                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                    PropertyFactory.visibility(lineVisibility)
                )
            }
            style.addLayer(solidLayer)
        }

        // 4. Dashed Line Layer
        if (style.getLayer(LAYER_LINES_DASHED) == null) {
            val dashedLayer = MapLibreLineLayer(LAYER_LINES_DASHED, SOURCE_CAVE_LINES).apply {
                setFilter(Expression.eq(Expression.get("style"), Expression.literal("DASHED")))
                setProperties(
                    PropertyFactory.lineColor(Expression.toColor(Expression.get("line-color"))),
                    PropertyFactory.lineWidth(Expression.toNumber(Expression.get("line-width"))),
                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                    PropertyFactory.lineDasharray(arrayOf(2.5f, 1.5f)),
                    PropertyFactory.visibility(lineVisibility)
                )
            }
            style.addLayer(dashedLayer)
        }

        // 5. Dotted Line Layer
        if (style.getLayer(LAYER_LINES_DOTTED) == null) {
            val dottedLayer = MapLibreLineLayer(LAYER_LINES_DOTTED, SOURCE_CAVE_LINES).apply {
                setFilter(Expression.eq(Expression.get("style"), Expression.literal("DOTTED")))
                setProperties(
                    PropertyFactory.lineColor(Expression.toColor(Expression.get("line-color"))),
                    PropertyFactory.lineWidth(Expression.toNumber(Expression.get("line-width"))),
                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                    PropertyFactory.lineDasharray(arrayOf(0.2f, 1.8f)),
                    PropertyFactory.visibility(lineVisibility)
                )
            }
            style.addLayer(dottedLayer)
        }

        // 6. Point Marker (Circle) Layer
        if (style.getLayer(LAYER_POINTS_CIRCLE) == null) {
            val circleLayer = CircleLayer(LAYER_POINTS_CIRCLE, SOURCE_CAVE_POINTS).apply {
                setProperties(
                    PropertyFactory.circleColor(Expression.toColor(Expression.get("circle-color"))),
                    PropertyFactory.circleRadius(Expression.toNumber(Expression.get("circle-radius"))),
                    PropertyFactory.circleStrokeWidth(1.5f),
                    PropertyFactory.circleStrokeColor(Expression.toColor(Expression.literal("#000000"))),
                    PropertyFactory.visibility(pointVisibility)
                )
            }
            style.addLayer(circleLayer)
        }

        // 7. Point Labels Layer
        if (style.getLayer(LAYER_POINTS_LABELS) == null) {
            val labelsLayer = SymbolLayer(LAYER_POINTS_LABELS, SOURCE_CAVE_POINTS).apply {
                setFilter(Expression.has("name"))
                setProperties(
                    PropertyFactory.textField(Expression.get("name")),
                    PropertyFactory.textSize(11f),
                    PropertyFactory.textColor(Expression.toColor(Expression.literal("#FFFFFF"))),
                    PropertyFactory.textHaloColor(Expression.toColor(Expression.literal("#1E1E1E"))),
                    PropertyFactory.textHaloWidth(1.5f),
                    PropertyFactory.textOffset(arrayOf(1.2f, 0.0f)),
                    PropertyFactory.textAnchor(Property.TEXT_ANCHOR_LEFT),
                    PropertyFactory.textAllowOverlap(false),
                    PropertyFactory.visibility(pointVisibility)
                )
            }
            style.addLayer(labelsLayer)
        }
    }

    /**
     * Updates visibility of native MapLibre line and point layers.
     */
    fun updateVisibility(
        style: Style,
        isLineLayersVisible: Boolean,
        isPointLayersVisible: Boolean
    ) {
        val lineVisibility = if (isLineLayersVisible) Property.VISIBLE else Property.NONE
        ALL_LINE_LAYERS.forEach { layerId ->
            style.getLayer(layerId)?.setProperties(PropertyFactory.visibility(lineVisibility))
        }

        val pointVisibility = if (isPointLayersVisible) Property.VISIBLE else Property.NONE
        ALL_POINT_LAYERS.forEach { layerId ->
            style.getLayer(layerId)?.setProperties(PropertyFactory.visibility(pointVisibility))
        }
    }

    /**
     * Updates the GeoJSON data for lines on the GPU.
     */
    fun updateLinesSource(style: Style, featureCollection: FeatureCollection) {
        style.getSourceAs<GeoJsonSource>(SOURCE_CAVE_LINES)?.setGeoJson(featureCollection)
    }

    /**
     * Updates the GeoJSON data for points on the GPU.
     */
    fun updatePointsSource(style: Style, featureCollection: FeatureCollection) {
        style.getSourceAs<GeoJsonSource>(SOURCE_CAVE_POINTS)?.setGeoJson(featureCollection)
    }

    /**
     * Converts a list of [LayerLine] into a GeoJSON [FeatureCollection].
     */
    fun linesToFeatureCollection(
        lines: List<LayerLine>,
        lineLayers: List<LineLayer>,
        meta: MapMetadata
    ): FeatureCollection {
        if (lines.isEmpty() || meta.imageWidth <= 0 || meta.imageHeight <= 0 || meta.zoomMax <= 0) {
            return FeatureCollection.fromFeatures(emptyList())
        }

        val layerMap = lineLayers.associateBy { it.id }
        val features = ArrayList<Feature>(lines.size)

        for (line in lines) {
            if (line.points.size < 2) continue
            val layer = layerMap[line.layerId] ?: continue
            if (!layer.isVisible) continue

            val lngLatPoints = line.points.map { pt ->
                val latLng = CaveMapBounds.imagePixelsToLatLng(
                    pixelX = pt.first,
                    pixelY = pt.second,
                    imageWidth = meta.imageWidth,
                    imageHeight = meta.imageHeight,
                    maxZoom = meta.zoomMax
                )
                Point.fromLngLat(latLng.longitude, latLng.latitude)
            }

            val lineString = LineString.fromLngLats(lngLatPoints)
            val feature = Feature.fromGeometry(lineString)

            val coreColorInt = if (line.colorOverride != null) {
                line.colorOverride.toInt()
            } else if (layer.isHeatmapEnabled) {
                val c = LineColorUtils.getDifficultyColor(line.difficulty)
                val r = (c.red * 255).toInt().coerceIn(0, 255)
                val g = (c.green * 255).toInt().coerceIn(0, 255)
                val b = (c.blue * 255).toInt().coerceIn(0, 255)
                (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            } else {
                layer.defaultColor.toInt()
            }
            val coreColorHex = String.format("#%06X", 0xFFFFFF and coreColorInt)

            val haloColor = LineColorUtils.getHaloColor(line.environmentType, line.haloColor)
            if (haloColor != null) {
                val hr = (haloColor.red * 255).toInt().coerceIn(0, 255)
                val hg = (haloColor.green * 255).toInt().coerceIn(0, 255)
                val hb = (haloColor.blue * 255).toInt().coerceIn(0, 255)
                val haloColorInt = (0xFF shl 24) or (hr shl 16) or (hg shl 8) or hb
                val haloHex = String.format("#%06X", 0xFFFFFF and haloColorInt)
                feature.addStringProperty("halo-color", haloHex)
                feature.addNumberProperty("halo-width", layer.defaultWidth + 4.0f)
            }

            feature.addStringProperty("style", line.style.name)
            feature.addStringProperty("line-color", coreColorHex)
            feature.addNumberProperty("line-width", layer.defaultWidth.coerceAtLeast(1f))
            feature.addStringProperty("id", line.id.toString())
            feature.addStringProperty("name", line.name)
            features.add(feature)
        }

        return FeatureCollection.fromFeatures(features)
    }

    /**
     * Converts a list of [LayerPoint] into a GeoJSON [FeatureCollection].
     */
    fun pointsToFeatureCollection(
        points: List<LayerPoint>,
        pointLayers: List<PointLayer>,
        meta: MapMetadata
    ): FeatureCollection {
        if (points.isEmpty() || meta.imageWidth <= 0 || meta.imageHeight <= 0 || meta.zoomMax <= 0) {
            return FeatureCollection.fromFeatures(emptyList())
        }

        val layerMap = pointLayers.associateBy { it.id }
        val features = ArrayList<Feature>(points.size)

        for (point in points) {
            val layer = layerMap[point.layerId] ?: continue
            if (!layer.isVisible) continue

            val latLng = CaveMapBounds.imagePixelsToLatLng(
                pixelX = point.x,
                pixelY = point.y,
                imageWidth = meta.imageWidth,
                imageHeight = meta.imageHeight,
                maxZoom = meta.zoomMax
            )

            val ptGeom = Point.fromLngLat(latLng.longitude, latLng.latitude)
            val feature = Feature.fromGeometry(ptGeom)

            val colorHex = String.format("#%06X", 0xFFFFFF and point.color.toInt())
            feature.addStringProperty("circle-color", colorHex)
            feature.addNumberProperty("circle-radius", (layer.defaultSize / 2f).coerceAtLeast(3f))
            feature.addStringProperty("id", point.id.toString())

            if (layer.showLabels && point.name.isNotBlank()) {
                feature.addStringProperty("name", point.name)
            }

            features.add(feature)
        }

        return FeatureCollection.fromFeatures(features)
    }
}
