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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.vktrsansara.app.caveviewer.data.database.ProjectDatabase
import com.vktrsansara.app.caveviewer.domain.model.MapMetadata
import com.vktrsansara.app.caveviewer.domain.tile.TileCutter
import com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import java.io.File

/**
 * Professional map viewer powered by MapLibre Native Android SDK in TextureView mode.
 * Renders Equator-centered 256x256 raster tile pyramid with free 360-degree panning and strict bounds.
 */
@Composable
fun MapLibreViewer(
    projectDir: File,
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
                    lifecycleOwner = lifecycleOwner,
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
    lifecycleOwner: LifecycleOwner,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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

    // 3. Style JSON with clean raster layer (without artificial polygon bounds clipping)
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

                // Apply style and enforce native camera bounds
                maplibreMap.setStyle(Style.Builder().fromJson(styleJson)) { _ ->
                    maplibreMap.setMinZoomPreference(meta.zoomMin.toDouble())
                    maplibreMap.setMaxZoomPreference(meta.zoomMax.toDouble() + 4.0)

                    // Native bounds clamping (screen center cannot escape map rectangle)
                    maplibreMap.setLatLngBoundsForCameraTarget(bounds)

                    // Position camera in center at default zoom
                    maplibreMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            mapCenter,
                            meta.zoomDefault.toDouble()
                        )
                    )
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
