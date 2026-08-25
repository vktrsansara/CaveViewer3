package com.vktrsansara.app.caveviewer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
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
            AppSettings(
                theme = theme,
                isFullscreen = isFullscreen,
                showCompass = showCompass,
                showScaleBar = showScaleBar
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
}
