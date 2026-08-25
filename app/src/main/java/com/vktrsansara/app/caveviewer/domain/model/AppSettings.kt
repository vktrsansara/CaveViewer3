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
    val cursorType: Int = 1,              // 1: Крестик, 2: Пунктирный плюс с точкой, 3: Точка, 4: Х-образный, 5: Кружок с точкой
    val cursorColor: Long = 0xFFEF4444L   // Цвет курсора (по умолчанию ярко-красный #EF4444)
)
