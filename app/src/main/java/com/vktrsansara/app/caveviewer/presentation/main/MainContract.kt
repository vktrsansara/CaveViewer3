package com.vktrsansara.app.caveviewer.presentation.main

import com.vktrsansara.app.caveviewer.core.mvi.UiEffect
import com.vktrsansara.app.caveviewer.core.mvi.UiIntent
import com.vktrsansara.app.caveviewer.core.mvi.UiState
import com.vktrsansara.app.caveviewer.domain.model.AppSettings
import com.vktrsansara.app.caveviewer.domain.model.AppTheme

enum class AppScreen {
    MAIN,
    APP_SETTINGS
}

data class MainUiState(
    val currentScreen: AppScreen = AppScreen.MAIN,
    val isMenuExpanded: Boolean = false,
    val settings: AppSettings = AppSettings()
) : UiState

sealed interface MainUiIntent : UiIntent {
    data object ToggleMenu : MainUiIntent
    data object DismissMenu : MainUiIntent
    data object ExitAppClicked : MainUiIntent
    data object OpenAppSettings : MainUiIntent
    data object NavigateBack : MainUiIntent
    data class UpdateTheme(val theme: AppTheme) : MainUiIntent
    data class UpdateFullscreen(val enabled: Boolean) : MainUiIntent
}

sealed interface MainUiEffect : UiEffect {
    data object ExitApp : MainUiEffect
}
