package com.vktrsansara.app.caveviewer.presentation.map

import android.graphics.BitmapFactory
import android.net.Uri
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.vktrsansara.app.caveviewer.data.database.ProjectDatabase
import com.vktrsansara.app.caveviewer.domain.model.MapCameraPosition
import com.vktrsansara.app.caveviewer.domain.model.MapMetadata
import com.vktrsansara.app.caveviewer.domain.model.ScaleBindingPoint
import com.vktrsansara.app.caveviewer.domain.tile.TileCutter
import com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import java.io.File

/**
 * Professional map viewer powered by MapLibre Native Android SDK in TextureView mode.
 * Renders Equator-centered 256x256 raster tile pyramid with free 360-degree panning, strict center-stopping bounds,
 * seamless viewport state restoration, and interactive calibration / scale binding.
 */
@Composable
fun MapLibreViewer(
    projectDir: File,
    initialCameraPosition: MapCameraPosition? = null,
    bindingPoints: List<ScaleBindingPoint> = emptyList(),
    onCameraPositionChanged: (targetLat: Double, targetLon: Double, zoom: Double, bearing: Double) -> Unit = { _, _, _, _ -> },
    onBindingScreenPointsChanged: (List<Offset>) -> Unit = {},
    onMapCenterClick: (LatLng) -> Unit = {},
    onResetBearingReady: (() -> Unit) -> Unit = {},
    onMetadataLoaded: (MapMetadata) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Ensure MapLibre instance is initialized
    remember {
        try {
            MapLibre.getInstance(context)
        } catch (_: Exception) {}
    }

    var metadata by remember(projectDir) { mutableStateOf<MapMetadata?>(null) }
    var isLoading by remember(projectDir) { mutableStateOf(true) }
    var loadingStatus by remember(projectDir) { mutableStateOf("Загрузка карты...") }
    var loadError by remember(projectDir) { mutableStateOf<String?>(null) }

    // Load or generate project metadata and tiles
    LaunchedEffect(projectDir) {
        isLoading = true
        loadError = null
        withContext(Dispatchers.IO) {
            try {
                val dbFile = File(projectDir, "thismap.sqlite")
                val db = ProjectDatabase(dbFile)
                var meta = db.getMetadata()

                val tilesDir = File(projectDir, "tiles")
                val mapFile = File(projectDir, "map/image.png")

                val v2Marker = File(tilesDir, ".v2_aligned")
                if (meta == null || !tilesDir.exists() || !v2Marker.exists()) {
                    if (mapFile.exists()) {
                        loadingStatus = "Оптимизация четкости тайлов карты..."
                        val bitmap = BitmapFactory.decodeFile(mapFile.absolutePath)
                        if (bitmap != null) {
                            TileCutter.cutTiles(
                                projectName = projectDir.name,
                                projectDir = projectDir,
                                sourceBitmap = bitmap,
                                onProgress = {}
                            )
                            bitmap.recycle()
                            meta = db.getMetadata()
                        }
                    }
                }

                if (meta != null) {
                    metadata = meta
                    onMetadataLoaded(meta)
                } else {
                    loadError = "Файлы карты не найдены в проекте"
                }
            } catch (e: Exception) {
                loadError = e.message ?: "Ошибка инициализации карты"
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bgMain)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AppColors.accent)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = loadingStatus,
                            color = AppColors.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            loadError != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = loadError ?: "Ошибка загрузки",
                        color = AppColors.textSecondary,
                        fontSize = 14.sp
                    )
                }
            }
            metadata != null -> {
                val meta = metadata!!
                val tilesDir = File(projectDir, "tiles")

                MapLibreMapViewContainer(
                    meta = meta,
                    tilesDir = tilesDir,
                    initialCameraPosition = initialCameraPosition,
                    bindingPoints = bindingPoints,
                    lifecycleOwner = lifecycleOwner,
                    onCameraPositionChanged = onCameraPositionChanged,
                    onBindingScreenPointsChanged = onBindingScreenPointsChanged,
                    onMapCenterClick = onMapCenterClick,
                    onResetBearingReady = onResetBearingReady,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun MapLibreMapViewContainer(
    meta: MapMetadata,
    tilesDir: File,
    initialCameraPosition: MapCameraPosition?,
    bindingPoints: List<ScaleBindingPoint>,
    lifecycleOwner: LifecycleOwner,
    onCameraPositionChanged: (targetLat: Double, targetLon: Double, zoom: Double, bearing: Double) -> Unit,
    onBindingScreenPointsChanged: (List<Offset>) -> Unit,
    onMapCenterClick: (LatLng) -> Unit,
    onResetBearingReady: (() -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var maplibreMapInstance by remember { mutableStateOf<MapLibreMap?>(null) }

    val currentOnMapCenterClick by rememberUpdatedState(onMapCenterClick)
    val currentOnCameraPositionChanged by rememberUpdatedState(onCameraPositionChanged)
    val currentOnBindingScreenPointsChanged by rememberUpdatedState(onBindingScreenPointsChanged)
    val currentOnResetBearingReady by rememberUpdatedState(onResetBearingReady)
    val currentBindingPoints by rememberUpdatedState(bindingPoints)

    // 1. Calculate Bounds and Map Center using CaveMapBounds (Equator centered)
    val bounds = remember(meta) {
        CaveMapBounds.calculateBounds(
            imageWidth = meta.imageWidth,
            imageHeight = meta.imageHeight,
            tileSize = meta.tileSize,
            maxZoom = meta.zoomMax
        )
    }
    val mapCenter = remember(bounds) {
        CaveMapBounds.getCenter(bounds)
    }

    // 2. Properly URI-encode file path for MapLibre C++ engine
    val baseEncodedUri = remember(tilesDir) {
        Uri.fromFile(tilesDir).toString().trimEnd('/')
    }
    val tilesUrl = "$baseEncodedUri/{z}/{x}/{y}.png"

    // 3. Style JSON with clean raster layer
    val styleJson = remember(tilesUrl, meta.zoomMin, meta.zoomMax) {
        """
        {
          "version": 8,
          "sources": {
            "cave-raster-source": {
              "type": "raster",
              "tiles": [
                "$tilesUrl"
              ],
              "tileSize": 256,
              "minzoom": ${meta.zoomMin},
              "maxzoom": ${meta.zoomMax}
            }
          },
          "layers": [
            {
              "id": "background-layer",
              "type": "background",
              "paint": {
                "background-color": "#202020"
              }
            },
            {
              "id": "cave-raster-layer",
              "type": "raster",
              "source": "cave-raster-source"
            }
          ]
        }
        """.trimIndent()
    }

    // Helper to calculate screen offsets of active binding points
    fun calculateScreenPoints(map: MapLibreMap) {
        if (currentBindingPoints.isNotEmpty()) {
            val offsets = currentBindingPoints.map { pt ->
                val p = map.projection.toScreenLocation(pt.latLng)
                Offset(p.x, p.y)
            }
            currentOnBindingScreenPointsChanged(offsets)
        } else {
            currentOnBindingScreenPointsChanged(emptyList())
        }
    }

    // Update screen offsets when bindingPoints change
    LaunchedEffect(bindingPoints, maplibreMapInstance) {
        maplibreMapInstance?.let { map ->
            calculateScreenPoints(map)
        }
    }

    val mapView = remember(tilesDir) {
        val mapOptions = MapLibreMapOptions.createFromAttributes(context).apply {
            textureMode(true)
        }
        MapView(context, mapOptions).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            onCreate(null)

            getMapAsync { maplibreMap ->
                maplibreMapInstance = maplibreMap

                // Provide callback to reset rotation bearing smoothly to 0°
                currentOnResetBearingReady {
                    val currentCam = maplibreMap.cameraPosition
                    val targetCam = CameraPosition.Builder(currentCam).bearing(0.0).build()
                    maplibreMap.easeCamera(CameraUpdateFactory.newCameraPosition(targetCam), 300)
                }

                // Handle single tap click on map -> provides map center (under cursor)
                maplibreMap.addOnMapClickListener { _ ->
                    val center = maplibreMap.cameraPosition.target
                    if (center != null) {
                        currentOnMapCenterClick(center)
                    }
                    true
                }

                // Track camera moves with strict boundary clamping:
                // The center of the screen (cursor) cannot leave the raster rectangle [latitudeSouth..latitudeNorth, longitudeWest..longitudeEast]
                maplibreMap.addOnCameraMoveListener {
                    val cam = maplibreMap.cameraPosition
                    val target = cam.target
                    if (target != null) {
                        val clampedLat = target.latitude.coerceIn(bounds.latitudeSouth, bounds.latitudeNorth)
                        val clampedLon = target.longitude.coerceIn(bounds.longitudeWest, bounds.longitudeEast)
                        if (clampedLat != target.latitude || clampedLon != target.longitude) {
                            maplibreMap.moveCamera(
                                CameraUpdateFactory.newLatLng(LatLng(clampedLat, clampedLon))
                            )
                        } else {
                            currentOnCameraPositionChanged(target.latitude, target.longitude, cam.zoom, cam.bearing)
                        }
                    }
                    calculateScreenPoints(maplibreMap)
                }

                // UI & Gesture Settings
                maplibreMap.uiSettings.apply {
                    isLogoEnabled = false
                    isAttributionEnabled = false
                    isCompassEnabled = false      // Custom compass on top-left
                    isRotateGesturesEnabled = true // 360 rotation
                    isTiltGesturesEnabled = false  // 3D tilt disabled
                    isZoomGesturesEnabled = true   // Pinch-to-zoom & double tap
                    isQuickZoomGesturesEnabled = true
                }

                // Apply style
                maplibreMap.setStyle(Style.Builder().fromJson(styleJson)) { _ ->
                    maplibreMap.setMinZoomPreference(meta.zoomMin.toDouble())
                    maplibreMap.setMaxZoomPreference(meta.zoomMax.toDouble() + 4.0)

                    // Restore saved camera position (clamped) or start at default map center
                    if (initialCameraPosition != null && initialCameraPosition.zoom > 0.0) {
                        val clampedLat = initialCameraPosition.targetLat.coerceIn(bounds.latitudeSouth, bounds.latitudeNorth)
                        val clampedLon = initialCameraPosition.targetLon.coerceIn(bounds.longitudeWest, bounds.longitudeEast)
                        maplibreMap.moveCamera(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder()
                                    .target(LatLng(clampedLat, clampedLon))
                                    .zoom(initialCameraPosition.zoom)
                                    .bearing(initialCameraPosition.bearing)
                                    .build()
                            )
                        )
                        currentOnCameraPositionChanged(
                            clampedLat,
                            clampedLon,
                            initialCameraPosition.zoom,
                            initialCameraPosition.bearing
                        )
                    } else {
                        maplibreMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                mapCenter,
                                meta.zoomDefault.toDouble()
                            )
                        )
                        currentOnCameraPositionChanged(
                            mapCenter.latitude,
                            mapCenter.longitude,
                            meta.zoomDefault.toDouble(),
                            0.0
                        )
                    }
                    calculateScreenPoints(maplibreMap)
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}
