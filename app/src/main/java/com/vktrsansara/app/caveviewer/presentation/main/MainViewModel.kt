package com.vktrsansara.app.caveviewer.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vktrsansara.app.caveviewer.domain.model.EntranceCoordinate
import com.vktrsansara.app.caveviewer.domain.model.MapCameraPosition
import com.vktrsansara.app.caveviewer.domain.model.MapLocation
import com.vktrsansara.app.caveviewer.domain.repository.ProjectRepository
import com.vktrsansara.app.caveviewer.domain.repository.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
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
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<MainUiEffect>()
    val effect = _effect.receiveAsFlow()

    private var projectCreationJob: Job? = null
    private val projectCameraPositions = mutableMapOf<String, MapCameraPosition>()

    init {
        // Collect app settings from DataStore
        settingsRepository.settingsFlow
            .onEach { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
            .launchIn(viewModelScope)

        // Observe active project changes
        projectRepository.activeProjectNameFlow
            .distinctUntilChanged()
            .onEach { activeName ->
                if (activeName != null) {
                    val dir = projectRepository.getProjectDir(activeName)
                    val sqliteFile = dir?.let { File(it, "thismap.sqlite") }
                    val mapFile = dir?.let { File(it, "map/image.png") }
                    if (dir != null && (sqliteFile?.exists() == true || mapFile?.exists() == true)) {
                        val savedPos = projectCameraPositions[activeName]
                        _uiState.update {
                            it.copy(
                                hasActiveProject = true,
                                activeProjectName = activeName,
                                activeProjectDir = dir,
                                activeProjectCameraPosition = savedPos
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                hasActiveProject = false,
                                activeProjectName = null,
                                activeProjectDir = null,
                                activeProjectMetadata = null,
                                activeProjectCameraPosition = null
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            hasActiveProject = false,
                            activeProjectName = null,
                            activeProjectDir = null,
                            activeProjectMetadata = null,
                            activeProjectCameraPosition = null
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
                    val projects = projectRepository.getProjectsList()
                    _uiState.update {
                        it.copy(
                            projectsList = projects,
                            currentScreen = AppScreen.PROJECTS_LIST,
                            isMenuExpanded = false
                        )
                    }
                }
            }
            is MainUiIntent.NewProjectClicked -> {
                _uiState.update {
                    it.copy(
                        isProjectTypeDialogVisible = true,
                        isMenuExpanded = false
                    )
                }
            }
            is MainUiIntent.ImportProjectClicked -> {
                _uiState.update {
                    it.copy(
                        underDevelopmentFeatureName = "Импорт проекта",
                        currentScreen = AppScreen.FEATURE_UNDER_DEVELOPMENT,
                        isMenuExpanded = false
                    )
                }
            }
            is MainUiIntent.ExportProjectClicked -> {
                _uiState.update {
                    it.copy(
                        underDevelopmentFeatureName = "Экспорт проекта",
                        currentScreen = AppScreen.FEATURE_UNDER_DEVELOPMENT,
                        isMenuExpanded = false
                    )
                }
            }
            is MainUiIntent.CloseActiveProject -> {
                viewModelScope.launch {
                    val currentName = _uiState.value.activeProjectName
                    if (currentName != null) {
                        projectCameraPositions.remove(currentName)
                    }
                    projectRepository.setActiveProjectName(null)
                    _uiState.update {
                        it.copy(
                            hasActiveProject = false,
                            activeProjectName = null,
                            activeProjectDir = null,
                            activeProjectMetadata = null,
                            activeProjectLocation = MapLocation(),
                            activeProjectEntrances = emptyList(),
                            activeProjectCadastralData = emptyMap(),
                            activeProjectCameraPosition = null,
                            isMenuExpanded = false
                        )
                    }
                }
            }
            is MainUiIntent.SelectProject -> {
                viewModelScope.launch {
                    projectRepository.setActiveProjectName(intent.projectName)
                    val dir = projectRepository.getProjectDir(intent.projectName)
                    val meta = projectRepository.getProjectMetadata(intent.projectName)
                    val location = projectRepository.getProjectLocation(intent.projectName)
                    val entrances = projectRepository.getProjectEntrances(intent.projectName)
                    val cadastral = projectRepository.getProjectCadastralData(intent.projectName)
                    val savedPos = projectCameraPositions[intent.projectName]
                    _uiState.update {
                        it.copy(
                            hasActiveProject = true,
                            activeProjectName = intent.projectName,
                            activeProjectDir = dir,
                            activeProjectMetadata = meta,
                            activeProjectLocation = location,
                            activeProjectEntrances = entrances,
                            activeProjectCadastralData = cadastral,
                            activeProjectCameraPosition = savedPos,
                            currentScreen = AppScreen.MAIN,
                            isMenuExpanded = false
                        )
                    }
                }
            }
            is MainUiIntent.DeleteProject -> {
                viewModelScope.launch {
                    val result = projectRepository.deleteProject(intent.projectName)
                    result.fold(
                        onSuccess = {
                            projectCameraPositions.remove(intent.projectName)
                            val updatedList = projectRepository.getProjectsList()
                            val isActive = _uiState.value.activeProjectName == intent.projectName
                            _uiState.update {
                                it.copy(
                                    projectsList = updatedList,
                                    hasActiveProject = if (isActive) false else it.hasActiveProject,
                                    activeProjectName = if (isActive) null else it.activeProjectName,
                                    activeProjectDir = if (isActive) null else it.activeProjectDir,
                                    activeProjectMetadata = if (isActive) null else it.activeProjectMetadata,
                                    activeProjectCameraPosition = if (isActive) null else it.activeProjectCameraPosition
                                )
                            }
                            if (isActive) {
                                projectRepository.setActiveProjectName(null)
                            }
                            _effect.send(MainUiEffect.ShowToast("Проект перемещен в корзину"))
                        },
                        onFailure = { error ->
                            _effect.send(MainUiEffect.ShowToast(error.message ?: "Ошибка удаления проекта"))
                        }
                    )
                }
            }
            is MainUiIntent.UpdateMapCameraPosition -> {
                val activeName = _uiState.value.activeProjectName
                if (activeName != null) {
                    projectCameraPositions[activeName] = intent.position
                }
                _uiState.update { it.copy(activeProjectCameraPosition = intent.position) }
            }
            is MainUiIntent.OpenMetadataEditor -> {
                viewModelScope.launch {
                    val activeName = _uiState.value.activeProjectName
                    if (activeName != null) {
                        val meta = projectRepository.getProjectMetadata(activeName)
                        val location = projectRepository.getProjectLocation(activeName)
                        val entrances = projectRepository.getProjectEntrances(activeName)
                        val cadastral = projectRepository.getProjectCadastralData(activeName)
                        _uiState.update {
                            it.copy(
                                activeProjectMetadata = meta,
                                activeProjectLocation = location,
                                activeProjectEntrances = entrances,
                                activeProjectCadastralData = cadastral,
                                currentScreen = AppScreen.METADATA_EDITOR,
                                isMenuExpanded = false
                            )
                        }
                    }
                }
            }
            is MainUiIntent.SaveMetadata -> {
                viewModelScope.launch {
                    val result = projectRepository.updateProjectMetadata(
                        originalProjectName = intent.originalProjectName,
                        metadata = intent.updatedMetadata
                    )
                    result.fold(
                        onSuccess = { updatedMeta ->
                            val cleanName = updatedMeta.projectName
                            if (intent.location != null) {
                                projectRepository.saveProjectLocation(cleanName, intent.location)
                            }
                            if (intent.entrances != null) {
                                projectRepository.saveProjectEntrances(cleanName, intent.entrances)
                            }
                            if (intent.cadastralData != null) {
                                projectRepository.saveProjectCadastralData(cleanName, intent.cadastralData)
                            }
                            if (cleanName != intent.originalProjectName) {
                                val oldPos = projectCameraPositions.remove(intent.originalProjectName)
                                if (oldPos != null) {
                                    projectCameraPositions[cleanName] = oldPos
                                }
                                projectRepository.setActiveProjectName(cleanName)
                            }
                            val updatedDir = projectRepository.getProjectDir(cleanName)
                            val updatedLoc = intent.location ?: projectRepository.getProjectLocation(cleanName)
                            val updatedEntr = intent.entrances ?: projectRepository.getProjectEntrances(cleanName)
                            val updatedCadastral = intent.cadastralData ?: projectRepository.getProjectCadastralData(cleanName)
                            _uiState.update {
                                it.copy(
                                    activeProjectName = cleanName,
                                    activeProjectDir = updatedDir,
                                    activeProjectMetadata = updatedMeta,
                                    activeProjectLocation = updatedLoc,
                                    activeProjectEntrances = updatedEntr,
                                    activeProjectCadastralData = updatedCadastral
                                )
                            }
                            _effect.send(MainUiEffect.ShowToast("Метаданные сохранены"))
                        },
                        onFailure = { error ->
                            _effect.send(MainUiEffect.ShowToast(error.message ?: "Ошибка сохранения метаданных"))
                        }
                    )
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
                        underDevelopmentFeatureName = "Топосъемка",
                        currentScreen = AppScreen.FEATURE_UNDER_DEVELOPMENT
                    )
                }
            }
            is MainUiIntent.SelectTherionProjectType -> {
                _uiState.update {
                    it.copy(
                        isProjectTypeDialogVisible = false,
                        underDevelopmentFeatureName = "Therion",
                        currentScreen = AppScreen.FEATURE_UNDER_DEVELOPMENT
                    )
                }
            }
            is MainUiIntent.CreateRasterProject -> {
                projectCreationJob?.cancel()
                projectCreationJob = viewModelScope.launch {
                    _uiState.update {
                        it.copy(
                            isProjectSaving = true,
                            projectSavingName = intent.projectName,
                            projectSavingProgress = 0f,
                            projectSavingStatusText = "Подготовка к нарезке тайлов..."
                        )
                    }

                    val result = projectRepository.createRasterProject(
                        projectName = intent.projectName,
                        imageUri = intent.imageUri,
                        onProgress = { progress ->
                            _uiState.update {
                                it.copy(
                                    projectSavingProgress = progress.progressFraction,
                                    projectSavingStatusText = "Нарезка тайлов зума ${progress.currentZoom} (${progress.currentTile}/${progress.totalTiles})"
                                )
                            }
                        }
                    )

                    result.fold(
                        onSuccess = { projectDir ->
                            val cleanName = projectDir.name
                            projectRepository.setActiveProjectName(cleanName)
                            projectCameraPositions.remove(cleanName)
                            val meta = projectRepository.getProjectMetadata(cleanName)
                            _uiState.update {
                                it.copy(
                                    isProjectSaving = false,
                                    hasActiveProject = true,
                                    activeProjectName = cleanName,
                                    activeProjectDir = projectDir,
                                    activeProjectMetadata = meta,
                                    activeProjectLocation = MapLocation(),
                                    activeProjectEntrances = emptyList(),
                                    activeProjectCadastralData = emptyMap(),
                                    activeProjectCameraPosition = null,
                                    currentScreen = AppScreen.MAIN
                                )
                            }
                            _effect.send(MainUiEffect.ShowToast("Проект успешно создан"))
                        },
                        onFailure = { error ->
                            _uiState.update { it.copy(isProjectSaving = false) }
                            _effect.send(MainUiEffect.ShowToast(error.message ?: "Ошибка создания проекта"))
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
                        projectSavingStatusText = "",
                        currentScreen = AppScreen.MAIN
                    )
                }
            }
        }
    }
}
