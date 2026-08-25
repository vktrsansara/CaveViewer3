package com.vktrsansara.app.caveviewer.domain.repository

import com.vktrsansara.app.caveviewer.domain.model.AppSettings
import com.vktrsansara.app.caveviewer.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settingsFlow: Flow<AppSettings>
    suspend fun setTheme(theme: ThemeMode)
    suspend fun setFullscreen(enabled: Boolean)
    suspend fun updateShowCompass(show: Boolean)
    suspend fun updateShowScaleBar(show: Boolean)
    suspend fun setCursorShow(show: Boolean)
    suspend fun setCursorType(type: Int)
    suspend fun setCursorColor(color: Long)
}
