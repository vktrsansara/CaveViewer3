package com.vktrsansara.app.caveviewer.domain.repository

import com.vktrsansara.app.caveviewer.domain.model.AppSettings
import com.vktrsansara.app.caveviewer.domain.model.CompassTapMode
import com.vktrsansara.app.caveviewer.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settingsFlow: Flow<AppSettings>
    suspend fun setTheme(theme: ThemeMode)
    suspend fun setFullscreen(enabled: Boolean)
    suspend fun updateShowCompass(show: Boolean)
    suspend fun setCompassTapMode(mode: CompassTapMode)
    suspend fun updateShowScaleBar(show: Boolean)
    suspend fun setCursorShow(show: Boolean)
    suspend fun setCursorType(type: Int)
    suspend fun setCursorColor(color: Long)
    suspend fun setGridEnabled(enabled: Boolean)
    suspend fun setGridSizeMode(mode: String)
    suspend fun setGridCustomSize(size: Double)
    suspend fun setGridColor(color: Long)
    suspend fun setColorPaletteMode(mode: String)
    suspend fun setMapFilter(mode: com.vktrsansara.app.caveviewer.domain.model.MapFilterMode)
    suspend fun setFavoriteToolPreset(preset: List<String>)
    suspend fun setPointPlacementMode(mode: com.vktrsansara.app.caveviewer.domain.model.PointPlacementMode)
    suspend fun setLinePlacementMode(mode: com.vktrsansara.app.caveviewer.domain.model.LinePlacementMode)
    suspend fun setSnappingSettings(settings: com.vktrsansara.app.caveviewer.domain.model.SnappingSettings)
}
