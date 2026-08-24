package com.vktrsansara.app.caveviewer.domain.model

enum class AppTheme {
    AUTO,
    LIGHT,
    DARK
}

data class AppSettings(
    val theme: AppTheme = AppTheme.AUTO,
    val isFullscreen: Boolean = false
)
