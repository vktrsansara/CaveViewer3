package com.vktrsansara.app.caveviewer.domain.model

/**
 * Hardware raster post-processing filter modes applied over the map.
 */
enum class MapFilterMode(val title: String, val description: String) {
    NONE("Нет", "Исходные цвета карты"),
    HIGH_CONTRAST("Повыш. контрастности", "Режим повышенной контрастности для слабых линий"),
    INVERT_COLORS("Инвер. цвета", "Инвертирование цветов (ночной режим)"),
    GRAYSCALE("Черно-белый", "Обесцвечивание растра (монохромный режим)"),
    INVERT_GRAYSCALE("Инверсия", "Черно-белый высококонтрастный негатив")
}
