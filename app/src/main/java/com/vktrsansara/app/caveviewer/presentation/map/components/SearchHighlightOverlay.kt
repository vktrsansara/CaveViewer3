package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds
import com.vktrsansara.app.caveviewer.presentation.main.SearchHighlightTarget
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import kotlinx.coroutines.delay
import org.maplibre.android.geometry.LatLng

/**
 * Renders an animated pulsing cyan ring around the found search target for 3 seconds.
 */
@Composable
fun SearchHighlightOverlay(
    target: SearchHighlightTarget?,
    imageWidth: Int,
    imageHeight: Int,
    zoomMax: Int,
    projector: ((LatLng) -> Offset)?,
    currentTargetLat: Double,
    currentTargetLon: Double,
    currentZoom: Double,
    mapBearing: Double,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (target == null || projector == null || imageWidth <= 0 || imageHeight <= 0 || zoomMax <= 0) {
        return
    }

    // Auto-dismiss after 3 seconds
    LaunchedEffect(target.timestamp) {
        delay(3000L)
        onDismiss()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "SearchHighlightPulse")
    val pulseFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseFraction"
    )

    val targetLatLng = remember(target.pixelX, target.pixelY, imageWidth, imageHeight, zoomMax) {
        CaveMapBounds.imagePixelsToLatLng(
            pixelX = target.pixelX,
            pixelY = target.pixelY,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            maxZoom = zoomMax
        )
    }

    val screenOffset = remember(targetLatLng, projector, currentTargetLat, currentTargetLon, currentZoom, mapBearing) {
        val sp = projector(targetLatLng)
        if (sp.x.isFinite() && sp.y.isFinite()) sp else null
    } ?: return

    Canvas(modifier = modifier.fillMaxSize().clipToBounds()) {
        clipRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height
        ) {
            val minRadius = 14.dp.toPx()
            val maxRadius = 46.dp.toPx()
            val currentRadius = minRadius + (maxRadius - minRadius) * pulseFraction
            val alpha = (1f - pulseFraction).coerceIn(0f, 1f)

            // Expanding wave fill
            drawCircle(
                color = AccentSkyBlue.copy(alpha = alpha * 0.22f),
                radius = currentRadius,
                center = screenOffset
            )

            // Expanding wave ring
            drawCircle(
                color = AccentSkyBlue.copy(alpha = alpha * 0.85f),
                radius = currentRadius,
                center = screenOffset,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Outer fixed glowing halo
            drawCircle(
                color = AccentSkyBlue.copy(alpha = 0.35f),
                radius = minRadius + 4.dp.toPx(),
                center = screenOffset
            )

            // Core anchor ring
            drawCircle(
                color = AccentSkyBlue,
                radius = minRadius,
                center = screenOffset,
                style = Stroke(width = 2.dp.toPx())
            )

            // White center beacon dot
            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = screenOffset
            )
        }
    }
}
