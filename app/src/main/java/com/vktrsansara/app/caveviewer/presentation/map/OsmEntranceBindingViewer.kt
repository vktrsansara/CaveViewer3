package com.vktrsansara.app.caveviewer.presentation.map

import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.vktrsansara.app.caveviewer.domain.model.EntranceCoordinate
import com.vktrsansara.app.caveviewer.presentation.map.components.BindingSideControl
import com.vktrsansara.app.caveviewer.presentation.map.components.MapCursorOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.OsmSearchBar
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

private const val OSM_STYLE_JSON = """{
  "version": 8,
  "sources": {
    "osm-raster-source": {
      "type": "raster",
      "tiles": [
        "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
      ],
      "tileSize": 256,
      "attribution": "© OpenStreetMap contributors"
    }
  },
  "layers": [
    {
      "id": "osm-raster-layer",
      "type": "raster",
      "source": "osm-raster-source"
    }
  ]
}"""

/**
 * Fullscreen OpenStreetMap viewer for placing and binding cave entrance GPS coordinates.
 */
@Composable
fun OsmEntranceBindingViewer(
    entrances: List<EntranceCoordinate>,
    cursorType: Int = 1,
    cursorColor: Long = 0xFFEF4444L,
    onEntranceTapped: (LatLng) -> Unit,
    onClose: () -> Unit,
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

    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var entranceScreenPositions by remember { mutableStateOf<List<Pair<EntranceCoordinate, Offset>>>(emptyList()) }

    val currentOnEntranceTapped by rememberUpdatedState(onEntranceTapped)
    val currentEntrances by rememberUpdatedState(entrances)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bgMain)
    ) {
        val oneThirdFromBottom = maxHeight / 3

        // 1. AndroidView hosting MapLibre MapView with OpenStreetMap Style
        AndroidView(
            factory = { ctx ->
                val initialTarget = currentEntrances.firstOrNull { it.lat != null && it.lon != null }?.let {
                    LatLng(it.lat!!, it.lon!!)
                } ?: LatLng(55.751244, 37.618423) // Moscow default

                val initialZoom = if (currentEntrances.any { it.lat != null && it.lon != null }) 14.0 else 5.0

                val options = MapLibreMapOptions.createFromAttributes(ctx)
                    .textureMode(true)
                    .attributionEnabled(false)
                    .logoEnabled(false)
                    .compassEnabled(false)
                    .camera(
                        CameraPosition.Builder()
                            .target(initialTarget)
                            .zoom(initialZoom)
                            .build()
                    )

                MapView(ctx, options).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    getMapAsync { maplibreMap ->
                        mapInstance = maplibreMap
                        maplibreMap.setStyle(Style.Builder().fromJson(OSM_STYLE_JSON)) {
                            // Style loaded
                        }

                        // Update entrance projected screen coordinates on camera move
                        fun updateEntranceScreenPositions() {
                            val projection = maplibreMap.projection
                            val newPositions = currentEntrances.mapNotNull { entrance ->
                                val lat = entrance.lat
                                val lon = entrance.lon
                                if (lat != null && lon != null) {
                                    val pointF = projection.toScreenLocation(LatLng(lat, lon))
                                    Pair(entrance, Offset(pointF.x, pointF.y))
                                } else {
                                    null
                                }
                            }
                            entranceScreenPositions = newPositions
                        }

                        maplibreMap.addOnCameraMoveListener {
                            updateEntranceScreenPositions()
                        }

                        maplibreMap.addOnMapClickListener {
                            val target = maplibreMap.cameraPosition.target
                            if (target != null) {
                                currentOnEntranceTapped(target)
                            }
                            true
                        }

                        updateEntranceScreenPositions()
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Entrance Markers Canvas Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            entranceScreenPositions.forEach { (_, screenPos) ->
                // Outer Emerald Circle
                drawCircle(
                    color = Color(0xFF10B981), // Emerald
                    radius = 9.dp.toPx(),
                    center = screenPos
                )
                // White Stroke
                drawCircle(
                    color = Color.White,
                    radius = 9.dp.toPx(),
                    center = screenPos,
                    style = Stroke(width = 2.dp.toPx())
                )
                // Inner White Dot
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = screenPos
                )
            }
        }

        // 3. Central Cursor Overlay
        MapCursorOverlay(
            cursorShow = true,
            cursorType = cursorType,
            cursorColor = cursorColor
        )

        // 4. Top Search Bar and Informational Banner
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 14.dp, start = 14.dp, end = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OsmSearchBar(
                onSelectLocation = { lat, lon ->
                    mapInstance?.easeCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                .target(LatLng(lat, lon))
                                .zoom(15.0)
                                .build()
                        ),
                        800
                    )
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.bgCard.copy(alpha = 0.95f))
                    .border(1.dp, AppColors.borderColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Наведите курсор на вход на местности и коснитесь экрана",
                    color = AppColors.textPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 6. Right Side Bar with Close button (positioned at 1/3 height from bottom)
        BindingSideControl(
            pointsCount = 0,
            onClose = onClose,
            onUndo = {},
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 15.dp, bottom = oneThirdFromBottom)
        )
    }

    // Lifecycle handling for MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {}
                Lifecycle.Event.ON_RESUME -> {}
                Lifecycle.Event.ON_PAUSE -> {}
                Lifecycle.Event.ON_STOP -> {}
                Lifecycle.Event.ON_DESTROY -> {}
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
