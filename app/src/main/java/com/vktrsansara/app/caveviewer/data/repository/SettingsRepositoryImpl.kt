package com.vktrsansara.app.caveviewer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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

            AppSettings(
                theme = theme,
                isFullscreen = isFullscreen,
                showCompass = showCompass,
                showScaleBar = showScaleBar,
                cursorShow = cursorShow,
                cursorType = cursorType,
                cursorColor = cursorColor
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
}
