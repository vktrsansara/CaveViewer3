package com.vktrsansara.app.caveviewer.presentation.map.filters

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import com.vktrsansara.app.caveviewer.domain.model.MapFilterMode

/**
 * GPU ColorMatrix filter generators for real-time raster map post-processing.
 */
object MapFilterUtils {
    fun getColorFilter(mode: MapFilterMode): ColorFilter? {
        return when (mode) {
            MapFilterMode.NONE -> null

            // 1. Инверсия (Ночной ч/б)
            MapFilterMode.INVERT_GRAYSCALE -> {
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

            // 2. Красный ночной
            MapFilterMode.RED_NIGHT -> {
                ColorFilter.colorMatrix(
                    ColorMatrix(
                        floatArrayOf(
                            -0.35f, -0.65f, -0.15f, 0f, 280f,
                             0f,     0f,     0f,    0f,   0f,
                             0f,     0f,     0f,    0f,   0f,
                             0f,     0f,     0f,    1f,   0f
                        )
                    )
                )
            }

            // 3. Инвер. цвета (RGB Негатив)
            MapFilterMode.INVERT_COLORS -> {
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

            // 4. Повыш. контрастности (+60%)
            MapFilterMode.HIGH_CONTRAST -> {
                val c = 1.6f
                val t = (-0.5f * c + 0.5f) * 255f
                ColorFilter.colorMatrix(
                    ColorMatrix(
                        floatArrayOf(
                            c,  0f, 0f, 0f, t,
                            0f, c,  0f, 0f, t,
                            0f, 0f, c,  0f, t,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                )
            }

            // 5. Теплый / Защита глаз (Янтарь/Сепия)
            MapFilterMode.WARM_AMBER -> {
                ColorFilter.colorMatrix(
                    ColorMatrix(
                        floatArrayOf(
                            0.393f, 0.769f, 0.189f, 0f, 10f,
                            0.349f, 0.686f, 0.168f, 0f, 5f,
                            0.272f, 0.534f, 0.131f, 0f, -10f,
                            0f,     0f,     0f,     1f, 0f
                        )
                    )
                )
            }

            // 6. Чертёж (Синька / Blueprint)
            MapFilterMode.BLUEPRINT -> {
                ColorFilter.colorMatrix(
                    ColorMatrix(
                        floatArrayOf(
                            -0.05f, -0.08f, -0.02f, 0f, 25f,
                            -0.20f, -0.38f, -0.08f, 0f, 170f,
                            -0.30f, -0.58f, -0.12f, 0f, 255f,
                             0f,     0f,     0f,    1f, 0f
                        )
                    )
                )
            }

            // 7. Черно-белый
            MapFilterMode.GRAYSCALE -> {
                ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
            }

            // 8. Макс. резкость / Бинаризация
            MapFilterMode.BINARIZATION -> {
                val c = 3.5f
                val t = (-0.5f * c + 0.5f) * 255f
                ColorFilter.colorMatrix(
                    ColorMatrix(
                        floatArrayOf(
                            c,  0f, 0f, 0f, t,
                            0f, c,  0f, 0f, t,
                            0f, 0f, c,  0f, t,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                )
            }

            // 9. Очистка фона (Белый лист)
            MapFilterMode.CLEAN_BACKGROUND -> {
                val c = 1.35f
                val t = 25f
                ColorFilter.colorMatrix(
                    ColorMatrix(
                        floatArrayOf(
                            c,  0f, 0f, 0f, t,
                            0f, c,  0f, 0f, t,
                            0f, 0f, c,  0f, t,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                )
            }

            // 10. Усиление цвета (+120%)
            MapFilterMode.COLOR_BOOST -> {
                ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(2.2f) })
            }
        }
    }
}
