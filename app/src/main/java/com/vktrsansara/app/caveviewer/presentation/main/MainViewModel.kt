package com.vktrsansara.app.caveviewer.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vktrsansara.app.caveviewer.domain.repository.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _effect = Channel<MainUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        settingsRepository.settingsFlow
            .onEach { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
            .launchIn(viewModelScope)
    }

    fun handleIntent(intent: MainUiIntent) {
        when (intent) {
            is MainUiIntent.ToggleMenu -> {
                _uiState.update { it.copy(isMenuExpanded = !it.isMenuExpanded) }
            }
            is MainUiIntent.DismissMenu -> {
                _uiState.update { it.copy(isMenuExpanded = false) }
            }
            is MainUiIntent.ExitAppClicked -> {
                _uiState.update { it.copy(isMenuExpanded = false) }
                viewModelScope.launch {
                    _effect.send(MainUiEffect.ExitApp)
                }
            }
            is MainUiIntent.OpenAppSettings -> {
                _uiState.update {
                    it.copy(
                        currentScreen = AppScreen.APP_SETTINGS,
                        isMenuExpanded = false
                    )
                }
            }
            is MainUiIntent.NavigateBack -> {
                _uiState.update { it.copy(currentScreen = AppScreen.MAIN) }
            }
            is MainUiIntent.UpdateTheme -> {
                viewModelScope.launch {
                    settingsRepository.setTheme(intent.theme)
                }
            }
            is MainUiIntent.UpdateFullscreen -> {
                viewModelScope.launch {
                    settingsRepository.setFullscreen(intent.enabled)
                }
            }
        }
    }
}
