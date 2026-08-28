package com.vktrsansara.app.caveviewer.domain.model

enum class ThemeMode {
    AUTO,
    LIGHT,
    DARK
}

enum class CompassTapMode {
    HORIZONTAL,   // Выравнивание по горизонтали (растр 0°)
    SCREEN_NORTH  // Выравнивание по экрану (Север строго вверх)
}

enum class PointPlacementMode(val title: String) {
    CURSOR_BUTTON_AND_TAP("Кнопка «+» и Тап"),
    CURSOR_BUTTON_ONLY("Только Кнопка «+»"),
    CURSOR_TAP_ONLY("Только Тап"),
    FREE_TAP("Тап в нужном месте")
}

enum class LinePlacementMode(val title: String) {
    CURSOR_BUTTON_AND_TAP("Кнопка «+» и Тап"),
    CURSOR_BUTTON_ONLY("Только Кнопка «+»"),
    CURSOR_TAP_ONLY("Только Тап"),
    FREE_TAP("Тап в нужном месте")
}

data class AppSettings(
    val theme: ThemeMode = ThemeMode.AUTO,
    val isFullscreen: Boolean = false,
    val showScaleBar: Boolean = true,
    val showCompass: Boolean = true,
    val compassTapMode: CompassTapMode = CompassTapMode.HORIZONTAL,
    val cursorShow: Boolean = true,
    val cursorType: Int = 1,              // 1..6 cursor presets
    val cursorColor: Long = 0xFFEF4444L,  // Cursor color (with alpha)
    val gridEnabled: Boolean = false,
    val gridSizeMode: String = "metadata", // "metadata" or "custom"
    val gridCustomSize: Double = 10.0,     // 10m or 100px
    val gridColor: Long = 0x9973FF00L,     // Grid color (with alpha)
    val colorPaletteMode: String = "standard", // "standard" or "muted"
    val mapFilter: MapFilterMode = MapFilterMode.NONE,
    val favoriteToolPreset: List<String> = emptyList(), // ToolType.name list
    val pointPlacementMode: PointPlacementMode = PointPlacementMode.CURSOR_BUTTON_AND_TAP,
    val linePlacementMode: LinePlacementMode = LinePlacementMode.CURSOR_BUTTON_AND_TAP,
    val snappingSettings: SnappingSettings = SnappingSettings()
)
