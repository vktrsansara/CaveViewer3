package com.vktrsansara.app.caveviewer.presentation.map.filters

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import com.vktrsansara.app.caveviewer.domain.model.MapFilterMode

/**
 * GPU hardware color matrix filters for raster map post-processing.
 */
object MapFilterUtils {
    fun getColorFilter(mode: MapFilterMode): ColorFilter? {
        return when (mode) {
            MapFilterMode.NONE -> null
            MapFilterMode.HIGH_CONTRAST -> {
                // Contrast enhancement by 60%
                val contrast = 1.6f
                val translate = (-0.5f * contrast + 0.5f) * 255f
                ColorFilter.colorMatrix(
                    ColorMatrix(
                        floatArrayOf(
                            contrast, 0f, 0f, 0f, translate,
                            0f, contrast, 0f, 0f, translate,
                            0f, 0f, contrast, 0f, translate,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                )
            }
            MapFilterMode.INVERT_COLORS -> {
                // Color Inversion (Night / Underground OLED Mode)
                ColorFilter.colorMatrix(
                    ColorMatrix(
                        floatArrayOf(
                            -1f,  0f,  0f, 0f, 255f,
                             0f, -1f,  0f, 0f, 255f,
                             0f,  0f, -1f, 0f, 255f,
                             0f,  0f,  0f, 1f,   0f
                        )
                    )
                )
            }
            MapFilterMode.GRAYSCALE -> {
                // Monochromatic grayscale (desaturation)
                ColorFilter.colorMatrix(
                    ColorMatrix().apply { setToSaturation(0f) }
                )
            }
            MapFilterMode.INVERT_GRAYSCALE -> {
                // Inverted monochrome grayscale with high contrast
                ColorFilter.colorMatrix(
                    ColorMatrix(
                        floatArrayOf(
                            -0.299f, -0.587f, -0.114f, 0f, 255f,
                            -0.299f, -0.587f, -0.114f, 0f, 255f,
                            -0.299f, -0.587f, -0.114f, 0f, 255f,
                             0f,      0f,      0f,     1f,   0f
                        )
                    )
                )
            }
        }
    }
}
