package com.vktrsansara.app.caveviewer.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vktrsansara.app.caveviewer.domain.repository.ProjectRepository
import com.vktrsansara.app.caveviewer.domain.repository.SettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(
    private val settingsRepository: SettingsRepository,
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _effect = Channel<MainUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var projectCreationJob: Job? = null

    init {
        observeSettings()
        observeActiveProject()
    }

    private fun observeSettings() {
        settingsRepository.settingsFlow
            .onEach { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeActiveProject() {
        projectRepository.activeProjectNameFlow
            .onEach { activeName ->
                if (activeName != null && activeName.isNotBlank()) {
                    val dir = projectRepository.getProjectDir(activeName)
                    val sqliteFile = dir?.let { File(it, "thismap.sqlite") }
                    val mapFile = dir?.let { File(it, "map/image.png") }
                    if (dir != null && (sqliteFile?.exists() == true || mapFile?.exists() == true)) {
                        _uiState.update {
                            it.copy(
                                hasActiveProject = true,
                                activeProjectName = activeName,
                                activeProjectDir = dir
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                hasActiveProject = false,
                                activeProjectName = null,
                                activeProjectDir = null
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            hasActiveProject = false,
                            activeProjectName = null,
                            activeProjectDir = null
                        )
                    }
                }
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
            is MainUiIntent.ProjectListClicked -> {
                viewModelScope.launch {
                    val list = projectRepository.getProjectsList()
                    _uiState.update {
                        it.copy(
                            projectsList = list,
                            currentScreen = AppScreen.PROJECTS_LIST,
                            isMenuExpanded = false
                        )
                    }
                }
            }
            is MainUiIntent.SelectProject -> {
                viewModelScope.launch {
                    projectRepository.setActiveProjectName(intent.projectName)
                    val dir = projectRepository.getProjectDir(intent.projectName)
                    _uiState.update {
                        it.copy(
                            hasActiveProject = dir != null,
                            activeProjectName = intent.projectName,
                            activeProjectDir = dir,
                            currentScreen = AppScreen.MAIN
                        )
                    }
                    _effect.send(MainUiEffect.ShowToast("Проект «${intent.projectName}» открыт"))
                }
            }
            is MainUiIntent.DeleteProject -> {
                // 1. Optimistic UI update: remove from list instantly (0 ms delay)
                _uiState.update { state ->
                    val isActive = state.activeProjectName == intent.projectName
                    state.copy(
                        projectsList = state.projectsList.filter { it.name != intent.projectName },
                        hasActiveProject = if (isActive) false else state.hasActiveProject,
                        activeProjectName = if (isActive) null else state.activeProjectName,
                        activeProjectDir = if (isActive) null else state.activeProjectDir
                    )
                }

                // 2. Perform background deletion
                viewModelScope.launch {
                    val result = projectRepository.deleteProject(intent.projectName)
                    result.fold(
                        onSuccess = {
                            if (_uiState.value.activeProjectName == intent.projectName) {
                                projectRepository.setActiveProjectName(null)
                            }
                            _effect.send(MainUiEffect.ShowToast("Проект «${intent.projectName}» удален"))
                        },
                        onFailure = { error ->
                            // Rollback if deletion failed
                            val actualList = projectRepository.getProjectsList()
                            _uiState.update { it.copy(projectsList = actualList) }
                            _effect.send(MainUiEffect.ShowToast(error.message ?: "Ошибка удаления"))
                        }
                    )
                }
            }
            is MainUiIntent.CloseActiveProject -> {
                viewModelScope.launch {
                    projectRepository.setActiveProjectName(null)
                    _uiState.update {
                        it.copy(
                            hasActiveProject = false,
                            activeProjectName = null,
                            activeProjectDir = null,
                            isMenuExpanded = false
                        )
                    }
                    _effect.send(MainUiEffect.ShowToast("Проект закрыт"))
                }
            }
            is MainUiIntent.NewProjectClicked -> {
                _uiState.update {
                    it.copy(
                        isMenuExpanded = false,
                        isProjectTypeDialogVisible = true
                    )
                }
            }
            is MainUiIntent.ImportProjectClicked -> {
                _uiState.update { it.copy(isMenuExpanded = false) }
                viewModelScope.launch {
                    _effect.send(MainUiEffect.ShowToast("Раздел «Импорт проекта» в разработке"))
                }
            }
            is MainUiIntent.ExportProjectClicked -> {
                _uiState.update { it.copy(isMenuExpanded = false) }
                viewModelScope.launch {
                    _effect.send(MainUiEffect.ShowToast("Раздел «Экспорт проекта» в разработке"))
                }
            }
            is MainUiIntent.DismissProjectTypeDialog -> {
                _uiState.update { it.copy(isProjectTypeDialogVisible = false) }
            }
            is MainUiIntent.SelectRasterProjectType -> {
                _uiState.update {
                    it.copy(
                        isProjectTypeDialogVisible = false,
                        currentScreen = AppScreen.CREATE_RASTER_PROJECT
                    )
                }
            }
            is MainUiIntent.SelectTopographyProjectType -> {
                _uiState.update {
                    it.copy(
                        isProjectTypeDialogVisible = false,
                        currentScreen = AppScreen.FEATURE_UNDER_DEVELOPMENT,
                        underDevelopmentFeatureName = "Топосъемка"
                    )
                }
            }
            is MainUiIntent.SelectTherionProjectType -> {
                _uiState.update {
                    it.copy(
                        isProjectTypeDialogVisible = false,
                        currentScreen = AppScreen.FEATURE_UNDER_DEVELOPMENT,
                        underDevelopmentFeatureName = "Therion"
                    )
                }
            }
            is MainUiIntent.CreateRasterProject -> {
                projectCreationJob?.cancel()
                projectCreationJob = viewModelScope.launch {
                    _uiState.update {
                        it.copy(
                            isProjectSaving = true,
                            projectSavingName = intent.projectName.trim(),
                            projectSavingProgress = 0f,
                            projectSavingStatusText = "Подготовка файлов карты..."
                        )
                    }
                    val result = projectRepository.createRasterProject(
                        projectName = intent.projectName,
                        imageUri = intent.imageUri,
                        onProgress = { progress ->
                            val percent = (progress.progressFraction * 100).toInt()
                            _uiState.update { state ->
                                state.copy(
                                    projectSavingProgress = progress.progressFraction,
                                    projectSavingStatusText = "Нарезка тайлов (уровень ${progress.currentZoom}, ${progress.currentTile} из ${progress.totalTiles})... $percent%"
                                )
                            }
                        }
                    )
                    result.fold(
                        onSuccess = { projectDir ->
                            _uiState.update {
                                it.copy(
                                    isProjectSaving = false,
                                    hasActiveProject = true,
                                    activeProjectName = intent.projectName.trim(),
                                    activeProjectDir = projectDir,
                                    currentScreen = AppScreen.MAIN
                                )
                            }
                            _effect.send(MainUiEffect.ShowToast("Проект успешно создан"))
                        },
                        onFailure = { error ->
                            if (error !is CancellationException) {
                                _uiState.update { it.copy(isProjectSaving = false) }
                                _effect.send(MainUiEffect.ShowToast(error.message ?: "Ошибка создания проекта"))
                            }
                        }
                    )
                }
            }
            is MainUiIntent.CancelProjectCreation -> {
                projectCreationJob?.cancel()
                projectCreationJob = null
                _uiState.update {
                    it.copy(
                        isProjectSaving = false,
                        projectSavingProgress = 0f,
                        projectSavingStatusText = ""
                    )
                }
                viewModelScope.launch {
                    _effect.send(MainUiEffect.ShowToast("Создание проекта отменено"))
                }
            }
        }
    }
}
