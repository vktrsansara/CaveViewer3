package com.vktrsansara.app.caveviewer.domain.model

enum class ThemeMode {
    AUTO,
    LIGHT,
    DARK
}

data class AppSettings(
    val theme: ThemeMode = ThemeMode.AUTO,
    val isFullscreen: Boolean = false
)
