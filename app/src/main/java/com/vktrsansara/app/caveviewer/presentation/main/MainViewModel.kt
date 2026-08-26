package com.vktrsansara.app.caveviewer.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vktrsansara.app.caveviewer.domain.model.EntranceCoordinate
import com.vktrsansara.app.caveviewer.domain.model.MapCameraPosition
import com.vktrsansara.app.caveviewer.domain.model.MapLocation
import com.vktrsansara.app.caveviewer.domain.model.ScaleBindingPoint
import com.vktrsansara.app.caveviewer.domain.repository.ProjectRepository
import com.vktrsansara.app.caveviewer.domain.repository.SettingsRepository
import com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds
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

        // Observe active project from DataStore
        projectRepository.activeProjectNameFlow
            .distinctUntilChanged()
            .onEach { activeName ->
                if (activeName != null) {
                    val dir = projectRepository.getProjectDir(activeName)
                    val sqliteFile = dir?.let { File(it, "thismap.sqlite") }
                    val mapFile = dir?.let { File(it, "map/image.png") }
                    if (dir != null && (sqliteFile?.exists() == true || mapFile?.exists() == true)) {
                        val savedPos = projectCameraPositions[activeName]
                        val meta = projectRepository.getProjectMetadata(activeName)
                        _uiState.update {
                            it.copy(
                                hasActiveProject = true,
                                activeProjectName = activeName,
                                activeProjectDir = dir,
                                activeProjectMetadata = meta,
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
            is MainUiIntent.OpenToolsSettings -> {
                _uiState.update {
                    it.copy(
                        currentScreen = AppScreen.TOOLS_SETTINGS,
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
            is MainUiIntent.OnShowCompassChanged -> {
                viewModelScope.launch {
                    settingsRepository.updateShowCompass(intent.enabled)
                }
            }
            is MainUiIntent.OnShowScaleBarChanged -> {
                viewModelScope.launch {
                    settingsRepository.updateShowScaleBar(intent.enabled)
                }
            }
            is MainUiIntent.UpdateCursorShow -> {
                viewModelScope.launch {
                    settingsRepository.setCursorShow(intent.show)
                }
            }
            is MainUiIntent.UpdateCursorType -> {
                viewModelScope.launch {
                    settingsRepository.setCursorType(intent.type)
                }
            }
            is MainUiIntent.UpdateCursorColor -> {
                viewModelScope.launch {
                    settingsRepository.setCursorColor(intent.color)
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
                        currentScreen = AppScreen.FEATURE_UNDER_DEVELOPMENT,
                        underDevelopmentFeatureName = "Импорт проекта",
                        isMenuExpanded = false
                    )
                }
            }
            is MainUiIntent.ExportProjectClicked -> {
                _uiState.update {
                    it.copy(
                        currentScreen = AppScreen.FEATURE_UNDER_DEVELOPMENT,
                        underDevelopmentFeatureName = "Экспорт проекта",
                        isMenuExpanded = false
                    )
                }
            }
            is MainUiIntent.CloseActiveProject -> {
                viewModelScope.launch {
                    val activeName = _uiState.value.activeProjectName
                    if (activeName != null) {
                        projectCameraPositions.remove(activeName)
                    }
                    projectRepository.setActiveProjectName(null)
                    _uiState.update {
                        it.copy(
                            hasActiveProject = false,
                            activeProjectName = null,
                            activeProjectDir = null,
                            activeProjectMetadata = null,
                            activeProjectCameraPosition = null,
                            isScaleBindingMode = false,
                            scaleBindingPoints = emptyList(),
                            isScaleBindingHelpVisible = false,
                            isScaleBindingInputVisible = false,
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
                    val savedPos = projectCameraPositions[intent.projectName]
                    _uiState.update {
                        it.copy(
                            hasActiveProject = true,
                            activeProjectName = intent.projectName,
                            activeProjectDir = dir,
                            activeProjectMetadata = meta,
                            activeProjectCameraPosition = savedPos,
                            isScaleBindingMode = false,
                            scaleBindingPoints = emptyList(),
                            isScaleBindingHelpVisible = false,
                            isScaleBindingInputVisible = false,
                            currentScreen = AppScreen.MAIN
                        )
                    }
                }
            }
            is MainUiIntent.DeleteProject -> {
                viewModelScope.launch {
                    val isActive = _uiState.value.activeProjectName == intent.projectName
                    projectRepository.deleteProject(intent.projectName)
                    projectCameraPositions.remove(intent.projectName)
                    if (isActive) {
                        projectRepository.setActiveProjectName(null)
                    }
                    val updatedList = projectRepository.getProjectsList()
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
                    _effect.send(MainUiEffect.ShowToast("Проект «${intent.projectName}» удален"))
                }
            }
            is MainUiIntent.UpdateMapCameraPosition -> {
                val activeName = _uiState.value.activeProjectName
                if (activeName != null) {
                    projectCameraPositions[activeName] = intent.position
                }
                _uiState.update { it.copy(activeProjectCameraPosition = intent.position) }
            }
            is MainUiIntent.OnMetadataLoaded -> {
                _uiState.update { it.copy(activeProjectMetadata = intent.metadata) }
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
                            _uiState.update {
                                it.copy(
                                    activeProjectName = cleanName,
                                    activeProjectMetadata = updatedMeta,
                                    activeProjectLocation = intent.location ?: it.activeProjectLocation,
                                    activeProjectEntrances = intent.entrances ?: it.activeProjectEntrances,
                                    activeProjectCadastralData = intent.cadastralData ?: it.activeProjectCadastralData,
                                    currentScreen = AppScreen.MAIN
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
            is MainUiIntent.StartScaleBinding -> {
                viewModelScope.launch {
                    val activeName = _uiState.value.activeProjectName
                    val meta = if (activeName != null) projectRepository.getProjectMetadata(activeName) else _uiState.value.activeProjectMetadata
                    _uiState.update {
                        it.copy(
                            activeProjectMetadata = meta ?: it.activeProjectMetadata,
                            isScaleBindingMode = true,
                            scaleBindingPoints = emptyList(),
                            isScaleBindingHelpVisible = true,
                            isMenuExpanded = false
                        )
                    }
                }
            }
            is MainUiIntent.DismissScaleBindingHelp -> {
                _uiState.update { it.copy(isScaleBindingHelpVisible = false) }
            }
            is MainUiIntent.AddScaleBindingPoint -> {
                viewModelScope.launch {
                    val currentPoints = _uiState.value.scaleBindingPoints
                    if (currentPoints.size >= 2) return@launch
                    val meta = _uiState.value.activeProjectMetadata
                        ?: _uiState.value.activeProjectName?.let { projectRepository.getProjectMetadata(it) }
                        ?: return@launch

                    val (pxX, pxY) = CaveMapBounds.latLngToImagePixels(
                        latLng = intent.latLng,
                        imageWidth = meta.imageWidth,
                        imageHeight = meta.imageHeight,
                        maxZoom = meta.zoomMax
                    )
                    val newPoint = ScaleBindingPoint(latLng = intent.latLng, imagePx = Pair(pxX, pxY))
                    val newPoints = currentPoints + newPoint
                    val isInputVisible = newPoints.size == 2

                    _uiState.update {
                        it.copy(
                            scaleBindingPoints = newPoints,
                            isScaleBindingInputVisible = isInputVisible
                        )
                    }
                }
            }
            is MainUiIntent.UndoScaleBindingPoint -> {
                _uiState.update {
                    if (it.scaleBindingPoints.isNotEmpty()) {
                        it.copy(
                            scaleBindingPoints = it.scaleBindingPoints.dropLast(1),
                            isScaleBindingInputVisible = false
                        )
                    } else {
                        it
                    }
                }
            }
            is MainUiIntent.CancelScaleBinding -> {
                _uiState.update {
                    it.copy(
                        isScaleBindingMode = false,
                        scaleBindingPoints = emptyList(),
                        isScaleBindingHelpVisible = false,
                        isScaleBindingInputVisible = false
                    )
                }
            }
            is MainUiIntent.DismissScaleBindingInput -> {
                _uiState.update {
                    it.copy(
                        scaleBindingPoints = it.scaleBindingPoints.dropLast(1),
                        isScaleBindingInputVisible = false
                    )
                }
            }
            is MainUiIntent.SaveScaleBinding -> {
                viewModelScope.launch {
                    val activeName = _uiState.value.activeProjectName
                    if (activeName != null) {
                        val result = projectRepository.saveScaleBinding(
                            projectName = activeName,
                            pixelsPerMeter = intent.pixelsPerMeter,
                            scaleMeters = intent.scaleMeters
                        )
                        result.fold(
                            onSuccess = { updatedMeta ->
                                _uiState.update {
                                    it.copy(
                                        activeProjectMetadata = updatedMeta,
                                        isScaleBindingMode = false,
                                        scaleBindingPoints = emptyList(),
                                        isScaleBindingHelpVisible = false,
                                        isScaleBindingInputVisible = false
                                    )
                                }
                                _effect.send(MainUiEffect.ShowToast("Привязка масштаба сохранена"))
                            },
                            onFailure = { error ->
                                _effect.send(MainUiEffect.ShowToast(error.message ?: "Ошибка сохранения масштаба"))
                            }
                        )
                    }
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
                        underDevelopmentFeatureName = "Проект Therion"
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
                            projectSavingStatusText = "Инициализация проекта..."
                        )
                    }
                    val result = projectRepository.createRasterProject(
                        projectName = intent.projectName,
                        imageUri = intent.imageUri,
                        onProgress = { progress ->
                            _uiState.update { state ->
                                state.copy(
                                    projectSavingProgress = progress.progressFraction,
                                    projectSavingStatusText = "Нарезка тайлов (Zoom ${progress.currentZoom}): ${progress.currentTile}/${progress.totalTiles}"
                                )
                            }
                        }
                    )
                    result.fold(
                        onSuccess = { projectDir ->
                            val cleanName = intent.projectName.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_")
                            projectRepository.setActiveProjectName(cleanName)
                            val meta = projectRepository.getProjectMetadata(cleanName)
                            _uiState.update {
                                it.copy(
                                    isProjectSaving = false,
                                    hasActiveProject = true,
                                    activeProjectName = cleanName,
                                    activeProjectDir = projectDir,
                                    activeProjectMetadata = meta,
                                    activeProjectCameraPosition = null,
                                    isScaleBindingMode = false,
                                    scaleBindingPoints = emptyList(),
                                    isScaleBindingHelpVisible = false,
                                    isScaleBindingInputVisible = false,
                                    currentScreen = AppScreen.MAIN
                                )
                            }
                            _effect.send(MainUiEffect.ShowToast("Проект «$cleanName» успешно создан"))
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
                _uiState.update { it.copy(isProjectSaving = false) }
            }
        }
    }
}
