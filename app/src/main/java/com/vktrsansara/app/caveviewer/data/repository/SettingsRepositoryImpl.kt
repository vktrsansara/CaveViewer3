package com.vktrsansara.app.caveviewer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vktrsansara.app.caveviewer.domain.model.AppSettings
import com.vktrsansara.app.caveviewer.domain.model.CompassTapMode
import com.vktrsansara.app.caveviewer.domain.model.ThemeMode
import com.vktrsansara.app.caveviewer.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "caveviewer_settings")

class SettingsRepositoryImpl(
    private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FULLSCREEN_ENABLED = booleanPreferencesKey("fullscreen_enabled")
        val SHOW_COMPASS = booleanPreferencesKey("show_compass")
        val COMPASS_TAP_MODE = stringPreferencesKey("compass_tap_mode")
        val SHOW_SCALE_BAR = booleanPreferencesKey("show_scale_bar")
        val CURSOR_SHOW = booleanPreferencesKey("cursor_show")
        val CURSOR_TYPE = intPreferencesKey("cursor_type")
        val CURSOR_COLOR = longPreferencesKey("cursor_color")
        val GRID_ENABLED = booleanPreferencesKey("grid_enabled")
        val GRID_SIZE_MODE = stringPreferencesKey("grid_size_mode")
        val GRID_CUSTOM_SIZE = doublePreferencesKey("grid_custom_size")
        val GRID_COLOR = longPreferencesKey("grid_color")
        val COLOR_PALETTE_MODE = stringPreferencesKey("color_palette_mode")
        val MAP_FILTER = stringPreferencesKey("map_filter")
        val FAVORITE_TOOL_PRESET = stringPreferencesKey("favorite_tool_preset")
        val POINT_PLACEMENT_MODE = stringPreferencesKey("point_placement_mode")
        val LINE_PLACEMENT_MODE = stringPreferencesKey("line_placement_mode")
        val SNAP_ENABLED = booleanPreferencesKey("snap_enabled")
        val SNAP_TO_VERTICES = booleanPreferencesKey("snap_to_vertices")
        val SNAP_TO_EDGES = booleanPreferencesKey("snap_to_edges")
        val SNAP_TO_POINTS = booleanPreferencesKey("snap_to_points")
        val SNAP_POINTS_TO_LINES = booleanPreferencesKey("snap_points_to_lines")
        val SNAP_RADIUS_DP = doublePreferencesKey("snap_radius_dp")
        val SNAP_INTERSECTION_MODE = stringPreferencesKey("snap_intersection_mode")
    }

    override val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeName = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.AUTO.name
            val theme = try {
                ThemeMode.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                ThemeMode.AUTO
            }
            val isFullscreen = preferences[PreferencesKeys.FULLSCREEN_ENABLED] ?: false
            val showCompass = preferences[PreferencesKeys.SHOW_COMPASS] ?: true
            val compassTapModeName = preferences[PreferencesKeys.COMPASS_TAP_MODE] ?: CompassTapMode.HORIZONTAL.name
            val compassTapMode = try {
                CompassTapMode.valueOf(compassTapModeName)
            } catch (e: IllegalArgumentException) {
                CompassTapMode.HORIZONTAL
            }
            val showScaleBar = preferences[PreferencesKeys.SHOW_SCALE_BAR] ?: true
            val cursorShow = preferences[PreferencesKeys.CURSOR_SHOW] ?: true
            val cursorType = preferences[PreferencesKeys.CURSOR_TYPE] ?: 1
            val cursorColor = preferences[PreferencesKeys.CURSOR_COLOR] ?: 0xFFEF4444L
            val gridEnabled = preferences[PreferencesKeys.GRID_ENABLED] ?: false
            val gridSizeMode = preferences[PreferencesKeys.GRID_SIZE_MODE] ?: "metadata"
            val gridCustomSize = preferences[PreferencesKeys.GRID_CUSTOM_SIZE] ?: 10.0
            val gridColor = preferences[PreferencesKeys.GRID_COLOR] ?: 0x9973FF00L
            val colorPaletteMode = preferences[PreferencesKeys.COLOR_PALETTE_MODE] ?: "standard"
            val filterName = preferences[PreferencesKeys.MAP_FILTER] ?: com.vktrsansara.app.caveviewer.domain.model.MapFilterMode.NONE.name
            val mapFilter = try {
                com.vktrsansara.app.caveviewer.domain.model.MapFilterMode.valueOf(filterName)
            } catch (e: IllegalArgumentException) {
                com.vktrsansara.app.caveviewer.domain.model.MapFilterMode.NONE
            }
            val favoritePresetStr = preferences[PreferencesKeys.FAVORITE_TOOL_PRESET] ?: ""
            val favoriteToolPreset = if (favoritePresetStr.isBlank()) emptyList() else favoritePresetStr.split(",").filter { it.isNotBlank() }
            val placementModeStr = preferences[PreferencesKeys.POINT_PLACEMENT_MODE] ?: com.vktrsansara.app.caveviewer.domain.model.PointPlacementMode.CURSOR_BUTTON_AND_TAP.name
            val pointPlacementMode = try {
                com.vktrsansara.app.caveviewer.domain.model.PointPlacementMode.valueOf(placementModeStr)
            } catch (e: IllegalArgumentException) {
                com.vktrsansara.app.caveviewer.domain.model.PointPlacementMode.CURSOR_BUTTON_AND_TAP
            }
            val linePlacementModeStr = preferences[PreferencesKeys.LINE_PLACEMENT_MODE] ?: com.vktrsansara.app.caveviewer.domain.model.LinePlacementMode.CURSOR_BUTTON_AND_TAP.name
            val linePlacementMode = try {
                com.vktrsansara.app.caveviewer.domain.model.LinePlacementMode.valueOf(linePlacementModeStr)
            } catch (e: IllegalArgumentException) {
                com.vktrsansara.app.caveviewer.domain.model.LinePlacementMode.CURSOR_BUTTON_AND_TAP
            }

            val snapEnabled = preferences[PreferencesKeys.SNAP_ENABLED] ?: true
            val snapToVertices = preferences[PreferencesKeys.SNAP_TO_VERTICES] ?: true
            val snapToEdges = preferences[PreferencesKeys.SNAP_TO_EDGES] ?: false
            val snapToPoints = preferences[PreferencesKeys.SNAP_TO_POINTS] ?: true
            val snapPointsToLines = preferences[PreferencesKeys.SNAP_POINTS_TO_LINES] ?: true
            val snapRadiusDp = (preferences[PreferencesKeys.SNAP_RADIUS_DP] ?: 12.0).toFloat()
            val snapInterModeStr = preferences[PreferencesKeys.SNAP_INTERSECTION_MODE] ?: com.vktrsansara.app.caveviewer.domain.model.IntersectionMode.NO.name
            val snapIntersectionMode = try {
                com.vktrsansara.app.caveviewer.domain.model.IntersectionMode.valueOf(snapInterModeStr)
            } catch (e: IllegalArgumentException) {
                com.vktrsansara.app.caveviewer.domain.model.IntersectionMode.NO
            }
            val snappingSettings = com.vktrsansara.app.caveviewer.domain.model.SnappingSettings(
                isEnabled = snapEnabled,
                snapToVertices = snapToVertices,
                snapToEdges = snapToEdges,
                snapToPoints = snapToPoints,
                snapPointsToLines = snapPointsToLines,
                snapRadiusDp = snapRadiusDp,
                intersectionMode = snapIntersectionMode
            )

            AppSettings(
                theme = theme,
                isFullscreen = isFullscreen,
                showCompass = showCompass,
                showScaleBar = showScaleBar,
                compassTapMode = compassTapMode,
                cursorShow = cursorShow,
                cursorType = cursorType,
                cursorColor = cursorColor,
                gridEnabled = gridEnabled,
                gridSizeMode = gridSizeMode,
                gridCustomSize = gridCustomSize,
                gridColor = gridColor,
                colorPaletteMode = colorPaletteMode,
                mapFilter = mapFilter,
                favoriteToolPreset = favoriteToolPreset,
                pointPlacementMode = pointPlacementMode,
                linePlacementMode = linePlacementMode,
                snappingSettings = snappingSettings
            )
        }

    override suspend fun setTheme(theme: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = theme.name
        }
    }

    override suspend fun setFullscreen(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FULLSCREEN_ENABLED] = enabled
        }
    }

    override suspend fun updateShowCompass(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_COMPASS] = show
        }
    }

    override suspend fun setCompassTapMode(mode: CompassTapMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COMPASS_TAP_MODE] = mode.name
        }
    }

    override suspend fun updateShowScaleBar(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_SCALE_BAR] = show
        }
    }

    override suspend fun setCursorShow(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURSOR_SHOW] = show
        }
    }

    override suspend fun setCursorType(type: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURSOR_TYPE] = type
        }
    }

    override suspend fun setCursorColor(color: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURSOR_COLOR] = color
        }
    }

    override suspend fun setGridEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GRID_ENABLED] = enabled
        }
    }

    override suspend fun setGridSizeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GRID_SIZE_MODE] = mode
        }
    }

    override suspend fun setGridCustomSize(size: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GRID_CUSTOM_SIZE] = size
        }
    }

    override suspend fun setGridColor(color: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GRID_COLOR] = color
        }
    }

    override suspend fun setColorPaletteMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COLOR_PALETTE_MODE] = mode
        }
    }

    override suspend fun setMapFilter(mode: com.vktrsansara.app.caveviewer.domain.model.MapFilterMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MAP_FILTER] = mode.name
        }
    }

    override suspend fun setFavoriteToolPreset(preset: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FAVORITE_TOOL_PRESET] = preset.joinToString(",")
        }
    }

    override suspend fun setPointPlacementMode(mode: com.vktrsansara.app.caveviewer.domain.model.PointPlacementMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.POINT_PLACEMENT_MODE] = mode.name
        }
    }

    override suspend fun setLinePlacementMode(mode: com.vktrsansara.app.caveviewer.domain.model.LinePlacementMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LINE_PLACEMENT_MODE] = mode.name
        }
    }

    override suspend fun setSnappingSettings(settings: com.vktrsansara.app.caveviewer.domain.model.SnappingSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SNAP_ENABLED] = settings.isEnabled
            preferences[PreferencesKeys.SNAP_TO_VERTICES] = settings.snapToVertices
            preferences[PreferencesKeys.SNAP_TO_EDGES] = settings.snapToEdges
            preferences[PreferencesKeys.SNAP_TO_POINTS] = settings.snapToPoints
            preferences[PreferencesKeys.SNAP_POINTS_TO_LINES] = settings.snapPointsToLines
            preferences[PreferencesKeys.SNAP_RADIUS_DP] = settings.snapRadiusDp.toDouble()
            preferences[PreferencesKeys.SNAP_INTERSECTION_MODE] = settings.intersectionMode.name
        }
    }
}
