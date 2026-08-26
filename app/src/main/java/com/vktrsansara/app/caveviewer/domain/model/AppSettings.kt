package com.vktrsansara.app.caveviewer.domain.model

enum class ThemeMode {
    AUTO,
    LIGHT,
    DARK
}

data class AppSettings(
    val theme: ThemeMode = ThemeMode.AUTO,
    val isFullscreen: Boolean = false,
    val showCompass: Boolean = true,
    val showScaleBar: Boolean = true,
    val cursorShow: Boolean = true,
    val cursorType: Int = 1,              // 1..6 cursor presets
    val cursorColor: Long = 0xFFEF4444L,  // Cursor color (with alpha)
    val gridEnabled: Boolean = false,
    val gridSizeMode: String = "metadata", // "metadata" or "custom"
    val gridCustomSize: Double = 10.0,     // 10m or 100px
    val gridColor: Long = 0x9973FF00L,     // Grid color (with alpha)
    val colorPaletteMode: String = "standard", // "standard" or "muted"
    val mapFilter: MapFilterMode = MapFilterMode.NONE
)
