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

            AppSettings(
                theme = theme,
                isFullscreen = isFullscreen,
                showCompass = showCompass,
                showScaleBar = showScaleBar,
                cursorShow = cursorShow,
                cursorType = cursorType,
                cursorColor = cursorColor,
                gridEnabled = gridEnabled,
                gridSizeMode = gridSizeMode,
                gridCustomSize = gridCustomSize,
                gridColor = gridColor,
                colorPaletteMode = colorPaletteMode,
                mapFilter = mapFilter,
                favoriteToolPreset = favoriteToolPreset
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
}
