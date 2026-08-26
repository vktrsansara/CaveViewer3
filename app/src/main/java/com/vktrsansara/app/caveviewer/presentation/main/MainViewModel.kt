package com.vktrsansara.app.caveviewer.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vktrsansara.app.caveviewer.domain.model.EntranceCoordinate
import com.vktrsansara.app.caveviewer.domain.model.MapCameraPosition
import com.vktrsansara.app.caveviewer.domain.model.MapLocation
import com.vktrsansara.app.caveviewer.domain.model.ScaleBindingPoint
import com.vktrsansara.app.caveviewer.domain.model.ToolType
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
                        val entrances = projectRepository.getProjectEntrances(activeName)
                        val location = projectRepository.getProjectLocation(activeName)
                        val cadastral = projectRepository.getProjectCadastralData(activeName)
                        _uiState.update {
                            it.copy(
                                hasActiveProject = true,
                                activeProjectName = activeName,
                                activeProjectDir = dir,
                                activeProjectMetadata = meta,
                                activeProjectEntrances = entrances,
                                activeProjectLocation = location,
                                activeProjectCadastralData = cadastral,
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
                                activeProjectEntrances = emptyList(),
                                activeProjectLocation = MapLocation(),
                                activeProjectCadastralData = emptyMap(),
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
                            activeProjectEntrances = emptyList(),
                            activeProjectLocation = MapLocation(),
                            activeProjectCadastralData = emptyMap(),
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
            is MainUiIntent.ToggleGrid -> {
                viewModelScope.launch {
                    val currentEnabled = _uiState.value.settings.gridEnabled
                    settingsRepository.setGridEnabled(!currentEnabled)
                }
            }
            is MainUiIntent.UpdateGridEnabled -> {
                viewModelScope.launch {
                    settingsRepository.setGridEnabled(intent.enabled)
                }
            }
            is MainUiIntent.UpdateGridSizeMode -> {
                viewModelScope.launch {
                    settingsRepository.setGridSizeMode(intent.mode)
                }
            }
            is MainUiIntent.UpdateGridCustomSize -> {
                viewModelScope.launch {
                    settingsRepository.setGridCustomSize(intent.size)
                }
            }
            is MainUiIntent.UpdateGridColor -> {
                viewModelScope.launch {
                    settingsRepository.setGridColor(intent.color)
                }
            }
            is MainUiIntent.UpdateColorPaletteMode -> {
                viewModelScope.launch {
                    settingsRepository.setColorPaletteMode(intent.mode)
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
                            activeProjectEntrances = emptyList(),
                            activeProjectLocation = MapLocation(),
                            activeProjectCadastralData = emptyMap(),
                            activeProjectCameraPosition = null,
                            isScaleBindingMode = false,
                            scaleBindingPoints = emptyList(),
                            isScaleBindingHelpVisible = false,
                            isScaleBindingInputVisible = false,
                            isNorthBindingMode = false,
                            northBindingPoints = emptyList(),
                            isNorthBindingHelpVisible = false,
                            isNorthBindingInputVisible = false,
                            isEntranceCavePickMode = false,
                            isOsmEntranceBindingMode = false,
                            pendingEntrancePlanPx = null,
                            isEntranceNameDialogVisible = false,
                            pendingEntranceGps = null,
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
                    val entrances = projectRepository.getProjectEntrances(intent.projectName)
                    val location = projectRepository.getProjectLocation(intent.projectName)
                    val cadastral = projectRepository.getProjectCadastralData(intent.projectName)
                    val savedPos = projectCameraPositions[intent.projectName]
                    _uiState.update {
                        it.copy(
                            hasActiveProject = true,
                            activeProjectName = intent.projectName,
                            activeProjectDir = dir,
                            activeProjectMetadata = meta,
                            activeProjectEntrances = entrances,
                            activeProjectLocation = location,
                            activeProjectCadastralData = cadastral,
                            activeProjectCameraPosition = savedPos,
                            isScaleBindingMode = false,
                            scaleBindingPoints = emptyList(),
                            isScaleBindingHelpVisible = false,
                            isScaleBindingInputVisible = false,
                            isNorthBindingMode = false,
                            northBindingPoints = emptyList(),
                            isNorthBindingHelpVisible = false,
                            isNorthBindingInputVisible = false,
                            isEntranceCavePickMode = false,
                            isOsmEntranceBindingMode = false,
                            pendingEntrancePlanPx = null,
                            isEntranceNameDialogVisible = false,
                            pendingEntranceGps = null,
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
                            isNorthBindingMode = false,
                            northBindingPoints = emptyList(),
                            isNorthBindingHelpVisible = false,
                            isNorthBindingInputVisible = false,
                            isEntranceCavePickMode = false,
                            isOsmEntranceBindingMode = false,
                            pendingEntrancePlanPx = null,
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
            is MainUiIntent.StartNorthBinding -> {
                viewModelScope.launch {
                    val activeName = _uiState.value.activeProjectName
                    val meta = if (activeName != null) projectRepository.getProjectMetadata(activeName) else _uiState.value.activeProjectMetadata
                    _uiState.update {
                        it.copy(
                            activeProjectMetadata = meta ?: it.activeProjectMetadata,
                            isNorthBindingMode = true,
                            northBindingPoints = emptyList(),
                            isNorthBindingHelpVisible = true,
                            isScaleBindingMode = false,
                            scaleBindingPoints = emptyList(),
                            isScaleBindingHelpVisible = false,
                            isScaleBindingInputVisible = false,
                            isEntranceCavePickMode = false,
                            isOsmEntranceBindingMode = false,
                            pendingEntrancePlanPx = null,
                            isMenuExpanded = false
                        )
                    }
                }
            }
            is MainUiIntent.DismissNorthBindingHelp -> {
                _uiState.update { it.copy(isNorthBindingHelpVisible = false) }
            }
            is MainUiIntent.AddNorthBindingPoint -> {
                viewModelScope.launch {
                    val currentPoints = _uiState.value.northBindingPoints
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
                            northBindingPoints = newPoints,
                            isNorthBindingInputVisible = isInputVisible
                        )
                    }
                }
            }
            is MainUiIntent.UndoNorthBindingPoint -> {
                _uiState.update {
                    if (it.northBindingPoints.isNotEmpty()) {
                        it.copy(
                            northBindingPoints = it.northBindingPoints.dropLast(1),
                            isNorthBindingInputVisible = false
                        )
                    } else {
                        it
                    }
                }
            }
            is MainUiIntent.CancelNorthBinding -> {
                _uiState.update {
                    it.copy(
                        isNorthBindingMode = false,
                        northBindingPoints = emptyList(),
                        isNorthBindingHelpVisible = false,
                        isNorthBindingInputVisible = false
                    )
                }
            }
            is MainUiIntent.DismissNorthBindingInput -> {
                _uiState.update {
                    it.copy(
                        northBindingPoints = it.northBindingPoints.dropLast(1),
                        isNorthBindingInputVisible = false
                    )
                }
            }
            is MainUiIntent.SaveNorthBinding -> {
                viewModelScope.launch {
                    val activeName = _uiState.value.activeProjectName
                    if (activeName != null) {
                        val result = projectRepository.saveNorthBinding(
                            projectName = activeName,
                            angleNorth = intent.angle
                        )
                        result.fold(
                            onSuccess = { updatedMeta ->
                                _uiState.update {
                                    it.copy(
                                        activeProjectMetadata = updatedMeta,
                                        isNorthBindingMode = false,
                                        northBindingPoints = emptyList(),
                                        isNorthBindingHelpVisible = false,
                                        isNorthBindingInputVisible = false
                                    )
                                }
                                _effect.send(MainUiEffect.ShowToast("Привязка направления севера сохранена"))
                            },
                            onFailure = { error ->
                                _effect.send(MainUiEffect.ShowToast(error.message ?: "Ошибка сохранения направления севера"))
                            }
                        )
                    }
                }
            }
            is MainUiIntent.StartEntranceBinding -> {
                _uiState.update {
                    it.copy(
                        isEntranceCavePickMode = true,
                        isOsmEntranceBindingMode = false,
                        isEntranceBindingHelpVisible = true,
                        pendingEntrancePlanPx = null,
                        isScaleBindingMode = false,
                        isNorthBindingMode = false,
                        isMenuExpanded = false
                    )
                }
            }
            is MainUiIntent.DismissEntranceBindingHelp -> {
                _uiState.update { it.copy(isEntranceBindingHelpVisible = false) }
            }
            is MainUiIntent.OnEntrancePlanPicked -> {
                viewModelScope.launch {
                    val meta = _uiState.value.activeProjectMetadata
                        ?: _uiState.value.activeProjectName?.let { projectRepository.getProjectMetadata(it) }
                    val planPx = if (meta != null) {
                        CaveMapBounds.latLngToImagePixels(
                            latLng = intent.latLng,
                            imageWidth = meta.imageWidth,
                            imageHeight = meta.imageHeight,
                            maxZoom = meta.zoomMax
                        )
                    } else {
                        Pair(0.0, 0.0)
                    }
                    _uiState.update {
                        it.copy(
                            pendingEntrancePlanPx = planPx,
                            isEntranceCavePickMode = false,
                            isOsmEntranceBindingMode = true
                        )
                    }
                }
            }
            is MainUiIntent.CancelEntranceCavePick -> {
                _uiState.update {
                    it.copy(
                        isEntranceCavePickMode = false,
                        isEntranceBindingHelpVisible = false,
                        pendingEntrancePlanPx = null
                    )
                }
            }
            is MainUiIntent.OnOsmEntranceTapped -> {
                _uiState.update {
                    it.copy(
                        pendingEntranceGps = intent.latLng,
                        isEntranceNameDialogVisible = true
                    )
                }
            }
            is MainUiIntent.DismissEntranceNameDialog -> {
                _uiState.update {
                    it.copy(
                        isEntranceNameDialogVisible = false,
                        pendingEntranceGps = null
                    )
                }
            }
            is MainUiIntent.SaveEntranceCoordinate -> {
                viewModelScope.launch {
                    val activeName = _uiState.value.activeProjectName
                    if (activeName != null) {
                        val currentEntrances = _uiState.value.activeProjectEntrances
                        val nextIndex = currentEntrances.size
                        val newEntrance = EntranceCoordinate(
                            pointIndex = nextIndex,
                            name = intent.name,
                            lat = intent.lat,
                            lon = intent.lon,
                            alt = null
                        )
                        val result = projectRepository.addProjectEntrance(activeName, newEntrance)
                        result.fold(
                            onSuccess = { updatedEntrances ->
                                _uiState.update {
                                    it.copy(
                                        activeProjectEntrances = updatedEntrances,
                                        isEntranceNameDialogVisible = false,
                                        pendingEntranceGps = null
                                    )
                                }
                                _effect.send(MainUiEffect.ShowToast("Точка входа «${intent.name}» добавлена"))
                            },
                            onFailure = { error ->
                                _effect.send(MainUiEffect.ShowToast(error.message ?: "Ошибка сохранения точки входа"))
                            }
                        )
                    }
                }
            }
            is MainUiIntent.CloseOsmEntranceBinding -> {
                _uiState.update {
                    it.copy(
                        isOsmEntranceBindingMode = false,
                        isEntranceCavePickMode = false,
                        pendingEntrancePlanPx = null,
                        pendingEntranceGps = null,
                        isEntranceNameDialogVisible = false
                    )
                }
            }

            // Ruler Handlers
            is MainUiIntent.StartRulerMode -> {
                addOrActivateTool(ToolType.RULER)
            }
            is MainUiIntent.AddRulerPoint -> {
                viewModelScope.launch {
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
                    _uiState.update {
                        it.copy(rulerPoints = it.rulerPoints + newPoint)
                    }
                }
            }
            is MainUiIntent.UndoRulerPoint -> {
                _uiState.update {
                    if (it.rulerPoints.isNotEmpty()) {
                        it.copy(rulerPoints = it.rulerPoints.dropLast(1))
                    } else {
                        it.copy(rulerPoints = emptyList())
                    }
                }
            }
            is MainUiIntent.CloseRulerMode -> {
                val newDock = _uiState.value.dockedTools - ToolType.RULER
                val newActive = if (_uiState.value.activeTool == ToolType.RULER) newDock.firstOrNull() else _uiState.value.activeTool
                _uiState.update {
                    it.copy(
                        dockedTools = newDock,
                        activeTool = newActive,
                        isRulerMode = false,
                        rulerPoints = emptyList()
                    )
                }
            }

            // Area Measure Handlers
            is MainUiIntent.StartAreaMeasureMode -> {
                addOrActivateTool(ToolType.AREA)
            }
            is MainUiIntent.AddAreaPoint -> {
                viewModelScope.launch {
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
                    _uiState.update {
                        it.copy(areaPoints = it.areaPoints + newPoint)
                    }
                }
            }
            is MainUiIntent.UndoAreaPoint -> {
                _uiState.update {
                    if (it.areaPoints.isNotEmpty()) {
                        it.copy(areaPoints = it.areaPoints.dropLast(1))
                    } else {
                        it.copy(areaPoints = emptyList())
                    }
                }
            }
            is MainUiIntent.CloseAreaMeasureMode -> {
                val newDock = _uiState.value.dockedTools - ToolType.AREA
                val newActive = if (_uiState.value.activeTool == ToolType.AREA) newDock.firstOrNull() else _uiState.value.activeTool
                _uiState.update {
                    it.copy(
                        dockedTools = newDock,
                        activeTool = newActive,
                        isAreaMeasureMode = false,
                        areaPoints = emptyList()
                    )
                }
            }

            // Angle Measure Handlers
            is MainUiIntent.StartAngleMeasureMode -> {
                addOrActivateTool(ToolType.ANGLE)
            }
            is MainUiIntent.AddAnglePoint -> {
                viewModelScope.launch {
                    val currentPoints = _uiState.value.anglePoints
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
                    _uiState.update {
                        it.copy(anglePoints = it.anglePoints + newPoint)
                    }
                }
            }
            is MainUiIntent.UndoAnglePoint -> {
                _uiState.update {
                    if (it.anglePoints.isNotEmpty()) {
                        it.copy(anglePoints = it.anglePoints.dropLast(1))
                    } else {
                        it.copy(anglePoints = emptyList())
                    }
                }
            }
            is MainUiIntent.CloseAngleMeasureMode -> {
                val newDock = _uiState.value.dockedTools - ToolType.ANGLE
                val newActive = if (_uiState.value.activeTool == ToolType.ANGLE) newDock.firstOrNull() else _uiState.value.activeTool
                _uiState.update {
                    it.copy(
                        dockedTools = newDock,
                        activeTool = newActive,
                        isAngleMeasureMode = false,
                        anglePoints = emptyList()
                    )
                }
            }

            // Azimuth Handlers
            is MainUiIntent.StartAzimuthMode -> {
                addOrActivateTool(ToolType.AZIMUTH)
            }
            is MainUiIntent.SetAzimuthOriginPoint -> {
                viewModelScope.launch {
                    val meta = _uiState.value.activeProjectMetadata
                        ?: _uiState.value.activeProjectName?.let { projectRepository.getProjectMetadata(it) }
                        ?: return@launch

                    val (pxX, pxY) = CaveMapBounds.latLngToImagePixels(
                        latLng = intent.latLng,
                        imageWidth = meta.imageWidth,
                        imageHeight = meta.imageHeight,
                        maxZoom = meta.zoomMax
                    )
                    val originPoint = ScaleBindingPoint(latLng = intent.latLng, imagePx = Pair(pxX, pxY))
                    _uiState.update {
                        it.copy(azimuthOriginPoint = originPoint)
                    }
                }
            }
            is MainUiIntent.ResetAzimuthOriginPoint -> {
                _uiState.update {
                    it.copy(azimuthOriginPoint = null)
                }
            }
            is MainUiIntent.CloseAzimuthMode -> {
                val newDock = _uiState.value.dockedTools - ToolType.AZIMUTH
                val newActive = if (_uiState.value.activeTool == ToolType.AZIMUTH) newDock.firstOrNull() else _uiState.value.activeTool
                _uiState.update {
                    it.copy(
                        dockedTools = newDock,
                        activeTool = newActive,
                        isAzimuthMode = false,
                        azimuthOriginPoint = null
                    )
                }
            }

            // Fault Line Handlers
            is MainUiIntent.StartFaultLineMode -> {
                addOrActivateTool(ToolType.FAULT_LINE)
            }
            is MainUiIntent.AddFaultLinePoint -> {
                viewModelScope.launch {
                    val currentPoints = _uiState.value.faultLinePoints
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
                    _uiState.update {
                        it.copy(faultLinePoints = it.faultLinePoints + newPoint)
                    }
                }
            }
            is MainUiIntent.UndoFaultLinePoint -> {
                _uiState.update {
                    if (it.faultLinePoints.isNotEmpty()) {
                        it.copy(faultLinePoints = it.faultLinePoints.dropLast(1))
                    } else {
                        it.copy(faultLinePoints = emptyList())
                    }
                }
            }
            is MainUiIntent.CloseFaultLineMode -> {
                val newDock = _uiState.value.dockedTools - ToolType.FAULT_LINE
                val newActive = if (_uiState.value.activeTool == ToolType.FAULT_LINE) newDock.firstOrNull() else _uiState.value.activeTool
                _uiState.update {
                    it.copy(
                        dockedTools = newDock,
                        activeTool = newActive,
                        isFaultLineMode = false,
                        faultLinePoints = emptyList()
                    )
                }
            }

            // Radius Measure Handlers
            is MainUiIntent.StartRadiusMeasureMode -> {
                addOrActivateTool(ToolType.RADIUS)
            }
            is MainUiIntent.SetRadiusCenterPoint -> {
                viewModelScope.launch {
                    val meta = _uiState.value.activeProjectMetadata
                        ?: _uiState.value.activeProjectName?.let { projectRepository.getProjectMetadata(it) }
                        ?: return@launch

                    val (pxX, pxY) = CaveMapBounds.latLngToImagePixels(
                        latLng = intent.latLng,
                        imageWidth = meta.imageWidth,
                        imageHeight = meta.imageHeight,
                        maxZoom = meta.zoomMax
                    )
                    val centerPoint = ScaleBindingPoint(latLng = intent.latLng, imagePx = Pair(pxX, pxY))
                    _uiState.update {
                        it.copy(radiusCenterPoint = centerPoint)
                    }
                }
            }
            is MainUiIntent.ResetRadiusCenterPoint -> {
                _uiState.update {
                    it.copy(radiusCenterPoint = null)
                }
            }
            is MainUiIntent.CloseRadiusMeasureMode -> {
                val newDock = _uiState.value.dockedTools - ToolType.RADIUS
                val newActive = if (_uiState.value.activeTool == ToolType.RADIUS) newDock.firstOrNull() else _uiState.value.activeTool
                _uiState.update {
                    it.copy(
                        dockedTools = newDock,
                        activeTool = newActive,
                        isRadiusMeasureMode = false,
                        radiusCenterPoint = null
                    )
                }
            }

            // Delta Offset Handlers
            is MainUiIntent.StartDeltaOffsetMode -> {
                addOrActivateTool(ToolType.DELTA_OFFSET)
            }
            is MainUiIntent.SetDeltaOffsetOriginPoint -> {
                viewModelScope.launch {
                    val meta = _uiState.value.activeProjectMetadata
                        ?: _uiState.value.activeProjectName?.let { projectRepository.getProjectMetadata(it) }
                        ?: return@launch

                    val (pxX, pxY) = CaveMapBounds.latLngToImagePixels(
                        latLng = intent.latLng,
                        imageWidth = meta.imageWidth,
                        imageHeight = meta.imageHeight,
                        maxZoom = meta.zoomMax
                    )
                    val originPoint = ScaleBindingPoint(latLng = intent.latLng, imagePx = Pair(pxX, pxY))
                    _uiState.update {
                        it.copy(deltaOffsetOriginPoint = originPoint)
                    }
                }
            }
            is MainUiIntent.ResetDeltaOffsetOriginPoint -> {
                _uiState.update {
                    it.copy(deltaOffsetOriginPoint = null)
                }
            }
            is MainUiIntent.CloseDeltaOffsetMode -> {
                val newDock = _uiState.value.dockedTools - ToolType.DELTA_OFFSET
                val newActive = if (_uiState.value.activeTool == ToolType.DELTA_OFFSET) newDock.firstOrNull() else _uiState.value.activeTool
                _uiState.update {
                    it.copy(
                        dockedTools = newDock,
                        activeTool = newActive,
                        isDeltaOffsetMode = false,
                        deltaOffsetOriginPoint = null,
                        isDeltaOffsetHelpVisible = false
                    )
                }
            }
            is MainUiIntent.OpenDeltaOffsetHelp -> {
                _uiState.update {
                    it.copy(isDeltaOffsetHelpVisible = true)
                }
            }
            is MainUiIntent.DismissDeltaOffsetHelp -> {
                _uiState.update {
                    it.copy(isDeltaOffsetHelpVisible = false)
                }
            }

            // Multi-Tool SideBar Handlers
            is MainUiIntent.SelectDockTool -> {
                _uiState.update { state ->
                    val newActive = if (state.activeTool == intent.tool) null else intent.tool
                    state.copy(activeTool = newActive)
                }
            }
            is MainUiIntent.UndoActiveToolPoint -> {
                when (_uiState.value.activeTool) {
                    ToolType.RULER -> {
                        _uiState.update {
                            if (it.rulerPoints.isNotEmpty()) it.copy(rulerPoints = it.rulerPoints.dropLast(1)) else it
                        }
                    }
                    ToolType.AREA -> {
                        _uiState.update {
                            if (it.areaPoints.isNotEmpty()) it.copy(areaPoints = it.areaPoints.dropLast(1)) else it
                        }
                    }
                    ToolType.ANGLE -> {
                        _uiState.update {
                            if (it.anglePoints.isNotEmpty()) it.copy(anglePoints = it.anglePoints.dropLast(1)) else it
                        }
                    }
                    ToolType.AZIMUTH -> {
                        _uiState.update { it.copy(azimuthOriginPoint = null) }
                    }
                    ToolType.FAULT_LINE -> {
                        _uiState.update {
                            if (it.faultLinePoints.isNotEmpty()) it.copy(faultLinePoints = it.faultLinePoints.dropLast(1)) else it
                        }
                    }
                    ToolType.DELTA_OFFSET -> {
                        _uiState.update { it.copy(deltaOffsetOriginPoint = null) }
                    }
                    ToolType.RADIUS -> {
                        _uiState.update { it.copy(radiusCenterPoint = null) }
                    }
                    null -> {}
                }
            }
            is MainUiIntent.HandleDockCloseClick -> {
                val state = _uiState.value
                if (state.activeTool != null) {
                    // Deactivate active tool, stays in dock
                    _uiState.update { it.copy(activeTool = null) }
                } else if (state.isDockFavorite) {
                    // If it is Favorite preset, hide entire dock without destroying preset
                    _uiState.update {
                        it.copy(
                            dockedTools = emptyList(),
                            activeTool = null,
                            isRulerMode = false,
                            isAreaMeasureMode = false,
                            isAngleMeasureMode = false,
                            isAzimuthMode = false,
                            isFaultLineMode = false,
                            isDeltaOffsetMode = false,
                            isRadiusMeasureMode = false
                        )
                    }
                } else if (state.dockedTools.isNotEmpty()) {
                    // Remove from last (LIFO)
                    val removedTool = state.dockedTools.last()
                    val remainingTools = state.dockedTools.dropLast(1)
                    val favPreset = state.settings.favoriteToolPreset
                    val isFav = favPreset.isNotEmpty() && (remainingTools.map { it.name } == favPreset)
                    _uiState.update {
                        val s = it.copy(
                            dockedTools = remainingTools,
                            activeTool = remainingTools.lastOrNull(),
                            isDockFavorite = isFav
                        )
                        clearToolData(s, removedTool)
                    }
                }
            }
            is MainUiIntent.HandleDockCloseAllLongClick -> {
                _uiState.update {
                    it.copy(
                        dockedTools = emptyList(),
                        activeTool = null,
                        rulerPoints = emptyList(),
                        isRulerMode = false,
                        areaPoints = emptyList(),
                        isAreaMeasureMode = false,
                        anglePoints = emptyList(),
                        isAngleMeasureMode = false,
                        azimuthOriginPoint = null,
                        isAzimuthMode = false,
                        faultLinePoints = emptyList(),
                        isFaultLineMode = false,
                        deltaOffsetOriginPoint = null,
                        isDeltaOffsetMode = false,
                        radiusCenterPoint = null,
                        isRadiusMeasureMode = false
                    )
                }
                viewModelScope.launch {
                    _effect.send(MainUiEffect.ShowToast("Панель инструментов закрыта"))
                }
            }
            is MainUiIntent.ToggleFavoriteToolPreset -> {
                val state = _uiState.value
                val currentDock = state.dockedTools
                if (currentDock.isNotEmpty()) {
                    viewModelScope.launch {
                        if (state.isDockFavorite) {
                            settingsRepository.setFavoriteToolPreset(emptyList())
                            _uiState.update { it.copy(isDockFavorite = false) }
                            _effect.send(MainUiEffect.ShowToast("Пресет удален из Моих инструментов"))
                        } else {
                            val presetNames = currentDock.map { it.name }
                            settingsRepository.setFavoriteToolPreset(presetNames)
                            _uiState.update { it.copy(isDockFavorite = true) }
                            _effect.send(MainUiEffect.ShowToast("Набор сохранен в «Мои инструменты» ⭐"))
                        }
                    }
                }
            }
            is MainUiIntent.OpenFavoriteToolsPreset -> {
                val favPreset = _uiState.value.settings.favoriteToolPreset
                if (favPreset.isNotEmpty()) {
                    val tools = favPreset.mapNotNull { name ->
                        try {
                            ToolType.valueOf(name)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (tools.isNotEmpty()) {
                        _uiState.update {
                            it.copy(
                                dockedTools = tools,
                                activeTool = tools.firstOrNull(),
                                isDockFavorite = true,
                                isMenuExpanded = false,
                                isScaleBindingMode = false,
                                isNorthBindingMode = false,
                                isEntranceCavePickMode = false,
                                isOsmEntranceBindingMode = false,
                                isRulerMode = ToolType.RULER in tools,
                                isAreaMeasureMode = ToolType.AREA in tools,
                                isAngleMeasureMode = ToolType.ANGLE in tools,
                                isAzimuthMode = ToolType.AZIMUTH in tools,
                                isFaultLineMode = ToolType.FAULT_LINE in tools,
                                isDeltaOffsetMode = ToolType.DELTA_OFFSET in tools,
                                isRadiusMeasureMode = ToolType.RADIUS in tools
                            )
                        }
                        viewModelScope.launch {
                            _effect.send(MainUiEffect.ShowToast("Загружен набор «Мои инструменты»"))
                        }
                    }
                }
            }
            is MainUiIntent.OpenDockHelp -> {
                _uiState.update { it.copy(isDockHelpVisible = true) }
            }
            is MainUiIntent.DismissDockHelp -> {
                _uiState.update { it.copy(isDockHelpVisible = false) }
            }

            // Map Filter Dialog Handlers
            is MainUiIntent.OpenMapFilterDialog -> {
                _uiState.update {
                    it.copy(isMapFilterDialogVisible = true, isMenuExpanded = false)
                }
            }
            is MainUiIntent.DismissMapFilterDialog -> {
                _uiState.update {
                    it.copy(isMapFilterDialogVisible = false)
                }
            }
            is MainUiIntent.OpenMapFilterHelpDialog -> {
                _uiState.update {
                    it.copy(isMapFilterHelpDialogVisible = true)
                }
            }
            is MainUiIntent.DismissMapFilterHelpDialog -> {
                _uiState.update {
                    it.copy(isMapFilterHelpDialogVisible = false)
                }
            }
            is MainUiIntent.SetMapFilterMode -> {
                viewModelScope.launch {
                    settingsRepository.setMapFilter(intent.mode)
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
                            val entrances = projectRepository.getProjectEntrances(cleanName)
                            val location = projectRepository.getProjectLocation(cleanName)
                            val cadastral = projectRepository.getProjectCadastralData(cleanName)
                            _uiState.update {
                                it.copy(
                                    isProjectSaving = false,
                                    hasActiveProject = true,
                                    activeProjectName = cleanName,
                                    activeProjectDir = projectDir,
                                    activeProjectMetadata = meta,
                                    activeProjectEntrances = entrances,
                                    activeProjectLocation = location,
                                    activeProjectCadastralData = cadastral,
                                    activeProjectCameraPosition = null,
                                    isScaleBindingMode = false,
                                    scaleBindingPoints = emptyList(),
                                    isScaleBindingHelpVisible = false,
                                    isScaleBindingInputVisible = false,
                                    isNorthBindingMode = false,
                                    northBindingPoints = emptyList(),
                                    isNorthBindingHelpVisible = false,
                                    isNorthBindingInputVisible = false,
                                    isEntranceCavePickMode = false,
                                    isOsmEntranceBindingMode = false,
                                    pendingEntrancePlanPx = null,
                                    isEntranceNameDialogVisible = false,
                                    pendingEntranceGps = null,
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

    private fun addOrActivateTool(tool: ToolType) {
        val currentDock = _uiState.value.dockedTools
        if (tool in currentDock) {
            _uiState.update {
                it.copy(
                    activeTool = tool,
                    isScaleBindingMode = false,
                    isNorthBindingMode = false,
                    isEntranceCavePickMode = false,
                    isOsmEntranceBindingMode = false,
                    isMenuExpanded = false
                )
            }
            return
        }
        if (currentDock.size >= 4) {
            viewModelScope.launch {
                _effect.send(MainUiEffect.ShowToast("Максимум 4 инструмента в панели"))
            }
            _uiState.update { it.copy(isMenuExpanded = false) }
            return
        }
        val newDock = currentDock + tool
        val favPreset = _uiState.value.settings.favoriteToolPreset
        val isFav = favPreset.isNotEmpty() && (newDock.map { it.name } == favPreset)
        _uiState.update {
            it.copy(
                dockedTools = newDock,
                activeTool = tool,
                isDockFavorite = isFav,
                isScaleBindingMode = false,
                isNorthBindingMode = false,
                isEntranceCavePickMode = false,
                isOsmEntranceBindingMode = false,
                isMenuExpanded = false,
                isRulerMode = ToolType.RULER in newDock,
                isAreaMeasureMode = ToolType.AREA in newDock,
                isAngleMeasureMode = ToolType.ANGLE in newDock,
                isAzimuthMode = ToolType.AZIMUTH in newDock,
                isFaultLineMode = ToolType.FAULT_LINE in newDock,
                isDeltaOffsetMode = ToolType.DELTA_OFFSET in newDock,
                isRadiusMeasureMode = ToolType.RADIUS in newDock
            )
        }
    }

    private fun clearToolData(state: MainUiState, tool: ToolType): MainUiState {
        return when (tool) {
            ToolType.RULER -> state.copy(rulerPoints = emptyList(), isRulerMode = false)
            ToolType.AREA -> state.copy(areaPoints = emptyList(), isAreaMeasureMode = false)
            ToolType.ANGLE -> state.copy(anglePoints = emptyList(), isAngleMeasureMode = false)
            ToolType.AZIMUTH -> state.copy(azimuthOriginPoint = null, isAzimuthMode = false)
            ToolType.FAULT_LINE -> state.copy(faultLinePoints = emptyList(), isFaultLineMode = false)
            ToolType.DELTA_OFFSET -> state.copy(deltaOffsetOriginPoint = null, isDeltaOffsetMode = false)
            ToolType.RADIUS -> state.copy(radiusCenterPoint = null, isRadiusMeasureMode = false)
        }
    }
}
