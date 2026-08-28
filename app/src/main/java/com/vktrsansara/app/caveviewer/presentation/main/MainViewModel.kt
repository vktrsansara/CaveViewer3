package com.vktrsansara.app.caveviewer.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vktrsansara.app.caveviewer.domain.model.EntranceCoordinate
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldDateTimeUtils
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldDefinition
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldType
import com.vktrsansara.app.caveviewer.domain.model.LayerLine
import com.vktrsansara.app.caveviewer.domain.model.LayerPoint
import com.vktrsansara.app.caveviewer.domain.model.LineLayer
import com.vktrsansara.app.caveviewer.domain.model.MapCameraPosition
import com.vktrsansara.app.caveviewer.domain.model.MapLocation
import com.vktrsansara.app.caveviewer.domain.model.MapMetadata
import com.vktrsansara.app.caveviewer.domain.model.PointLayer
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
                        val layers = projectRepository.getPointLayers(activeName)
                        val pointCounts = mutableMapOf<Long, Int>()
                        layers.forEach { layer ->
                            val points = projectRepository.getPointsForLayer(activeName, layer.id)
                            pointCounts[layer.id] = points.size
                        }
                        val allPoints = projectRepository.getAllVisiblePoints(activeName)
                        val lineLayersList = projectRepository.getLineLayers(activeName)
                        val lineCounts = mutableMapOf<Long, Int>()
                        lineLayersList.forEach { layer ->
                            val lines = projectRepository.getLinesForLayer(activeName, layer.id)
                            lineCounts[layer.id] = lines.size
                        }
                        val allLines = projectRepository.getAllVisibleLines(activeName)
                        _uiState.update {
                            it.copy(
                                hasActiveProject = true,
                                activeProjectName = activeName,
                                activeProjectDir = dir,
                                activeProjectMetadata = meta,
                                activeProjectEntrances = entrances,
                                activeProjectLocation = location,
                                activeProjectCadastralData = cadastral,
                                activeProjectCameraPosition = savedPos,
                                pointLayers = layers,
                                layerPointCounts = pointCounts,
                                allVisiblePoints = allPoints,
                                lineLayers = lineLayersList,
                                layerLineCounts = lineCounts,
                                allVisibleLines = allLines
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
                                activeProjectCameraPosition = null,
                                pointLayers = emptyList(),
                                layerPointCounts = emptyMap(),
                                allVisiblePoints = emptyList(),
                                isLayerManagerOpen = false,
                                isCreateLayerOpen = false,
                                lineLayers = emptyList(),
                                layerLineCounts = emptyMap(),
                                allVisibleLines = emptyList(),
                                isLineLayerManagerOpen = false,
                                isCreateLineLayerOpen = false
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
                            activeProjectCameraPosition = null,
                            pointLayers = emptyList(),
                            layerPointCounts = emptyMap(),
                            allVisiblePoints = emptyList(),
                            isLayerManagerOpen = false,
                            isCreateLayerOpen = false,
                            lineLayers = emptyList(),
                            layerLineCounts = emptyMap(),
                            allVisibleLines = emptyList(),
                            isLineLayerManagerOpen = false,
                            isCreateLineLayerOpen = false
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
            is MainUiIntent.UpdateCompassTapMode -> {
                viewModelScope.launch {
                    settingsRepository.setCompassTapMode(intent.mode)
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
                            pointLayers = emptyList(),
                            layerPointCounts = emptyMap(),
                            allVisiblePoints = emptyList(),
                            selectedPointForDetails = null,
                            editingPointLayer = null,
                            editingPoint = null,
                            isEditPointDialogOpen = false,
                            isPointPlacementControlOpen = false,
                            isPointEditorHelpOpen = false,
                            isPointLayersModeActive = false,
                            lineLayers = emptyList(),
                            layerLineCounts = emptyMap(),
                            allVisibleLines = emptyList(),
                            isLineLayerManagerOpen = false,
                            isCreateLineLayerOpen = false,
                            selectedLineLayerForSettings = null,
                            selectedLineLayerForProperties = null,
                            isAddLineFieldDialogOpen = false,
                            editingLineFieldDefinition = null,
                            editingLineLayer = null,
                            drawingLinePoints = emptyList(),
                            editingLine = null,
                            isEditLineDialogOpen = false,
                            selectedLineForDetails = null,
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
                    val layers = projectRepository.getPointLayers(intent.projectName)
                    val pointCounts = mutableMapOf<Long, Int>()
                    layers.forEach { layer ->
                        val points = projectRepository.getPointsForLayer(intent.projectName, layer.id)
                        pointCounts[layer.id] = points.size
                    }
                    val allPoints = projectRepository.getAllVisiblePoints(intent.projectName)
                    val lineLayersList = projectRepository.getLineLayers(intent.projectName)
                    val lineCounts = mutableMapOf<Long, Int>()
                    lineLayersList.forEach { layer ->
                        val lines = projectRepository.getLinesForLayer(intent.projectName, layer.id)
                        lineCounts[layer.id] = lines.size
                    }
                    val allLines = projectRepository.getAllVisibleLines(intent.projectName)
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
                            pointLayers = layers,
                            layerPointCounts = pointCounts,
                            allVisiblePoints = allPoints,
                            lineLayers = lineLayersList,
                            layerLineCounts = lineCounts,
                            allVisibleLines = allLines,
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
                        if (isActive) {
                            it.copy(
                                projectsList = updatedList,
                                hasActiveProject = false,
                                activeProjectName = null,
                                activeProjectDir = null,
                                activeProjectMetadata = null,
                                activeProjectEntrances = emptyList(),
                                activeProjectLocation = MapLocation(),
                                activeProjectCadastralData = emptyMap(),
                                activeProjectCameraPosition = null,
                                pointLayers = emptyList(),
                                layerPointCounts = emptyMap(),
                                allVisiblePoints = emptyList(),
                                selectedPointForDetails = null,
                                editingPointLayer = null,
                                editingPoint = null,
                                isEditPointDialogOpen = false,
                                isPointPlacementControlOpen = false,
                                isPointEditorHelpOpen = false,
                                isPointLayersModeActive = false,
                                lineLayers = emptyList(),
                                layerLineCounts = emptyMap(),
                                allVisibleLines = emptyList(),
                                isLineLayerManagerOpen = false,
                                isCreateLineLayerOpen = false,
                                selectedLineLayerForSettings = null,
                                selectedLineLayerForProperties = null,
                                isAddLineFieldDialogOpen = false,
                                editingLineFieldDefinition = null,
                                editingLineLayer = null,
                                drawingLinePoints = emptyList(),
                                editingLine = null,
                                isEditLineDialogOpen = false,
                                selectedLineForDetails = null
                            )
                        } else {
                            it.copy(projectsList = updatedList)
                        }
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
            is MainUiIntent.TogglePointLayersMode -> {
                val newActive = !_uiState.value.isPointLayersModeActive
                _uiState.update {
                    it.copy(
                        isPointLayersModeActive = newActive,
                        isMenuExpanded = false,
                        editingPointLayer = if (!newActive) null else it.editingPointLayer,
                        isLayerManagerOpen = if (!newActive) false else it.isLayerManagerOpen
                    )
                }
            }
            is MainUiIntent.DisablePointLayersMode -> {
                _uiState.update {
                    it.copy(
                        isPointLayersModeActive = false,
                        isLayerManagerOpen = false,
                        editingPointLayer = null,
                        editingPoint = null,
                        isEditPointDialogOpen = false
                    )
                }
            }
            is MainUiIntent.OpenLayerManager -> {
                val activeName = _uiState.value.activeProjectName
                if (activeName != null) {
                    viewModelScope.launch {
                        loadPointLayers(activeName)
                        _uiState.update {
                            it.copy(
                                isLayerManagerOpen = true,
                                isMenuExpanded = false
                            )
                        }
                    }
                }
            }
            is MainUiIntent.DismissLayerManager -> {
                _uiState.update { it.copy(isLayerManagerOpen = false) }
            }
            is MainUiIntent.OpenCreateLayerDialog -> {
                _uiState.update { it.copy(isCreateLayerOpen = true) }
            }
            is MainUiIntent.DismissCreateLayerDialog -> {
                _uiState.update { it.copy(isCreateLayerOpen = false) }
            }
            is MainUiIntent.CreatePointLayer -> {
                val activeName = _uiState.value.activeProjectName
                if (activeName != null) {
                    val trimmed = intent.name.trim()
                    val isDuplicate = _uiState.value.pointLayers.any { it.name.equals(trimmed, ignoreCase = true) }
                    if (isDuplicate) {
                        viewModelScope.launch {
                            _effect.send(MainUiEffect.ShowToast("Слой с названием «$trimmed» уже существует"))
                        }
                    } else {
                        viewModelScope.launch {
                            val newLayer = PointLayer(name = trimmed)
                            projectRepository.insertPointLayer(activeName, newLayer)
                            loadPointLayers(activeName)
                            _uiState.update { it.copy(isCreateLayerOpen = false) }
                        }
                    }
                }
            }
            is MainUiIntent.ToggleLayerVisibility -> {
                val activeName = _uiState.value.activeProjectName
                if (activeName != null) {
                    viewModelScope.launch {
                        val currentLayer = _uiState.value.pointLayers.find { it.id == intent.layerId }
                        val currentVis = currentLayer?.isVisible ?: true
                        projectRepository.toggleLayerVisibility(activeName, intent.layerId, !currentVis)
                        loadPointLayers(activeName)
                    }
                }
            }
            is MainUiIntent.DeletePointLayer -> {
                val activeName = _uiState.value.activeProjectName
                if (activeName != null) {
                    viewModelScope.launch {
                        projectRepository.deletePointLayer(activeName, intent.layerId)
                        loadPointLayers(activeName)
                        _uiState.update {
                            it.copy(
                                editingPointLayer = if (it.editingPointLayer?.id == intent.layerId) null else it.editingPointLayer,
                                selectedPointForDetails = if (it.selectedPointForDetails?.layerId == intent.layerId) null else it.selectedPointForDetails,
                                selectedLayerForSettings = if (it.selectedLayerForSettings?.id == intent.layerId) null else it.selectedLayerForSettings,
                                selectedLayerForProperties = if (it.selectedLayerForProperties?.id == intent.layerId) null else it.selectedLayerForProperties,
                                editingPoint = if (it.editingPoint?.layerId == intent.layerId) null else it.editingPoint,
                                isEditPointDialogOpen = if (it.editingPoint?.layerId == intent.layerId) false else it.isEditPointDialogOpen
                            )
                        }
                    }
                }
            }
            is MainUiIntent.OpenLayerSettings -> {
                _uiState.update { it.copy(selectedLayerForSettings = intent.layer) }
            }
            is MainUiIntent.DismissLayerSettings -> {
                _uiState.update { it.copy(selectedLayerForSettings = null) }
            }
            is MainUiIntent.SaveLayerSettings -> {
                val activeName = _uiState.value.activeProjectName
                if (activeName != null) {
                    val trimmed = intent.updatedLayer.name.trim()
                    val isDuplicate = _uiState.value.pointLayers.any {
                        it.id != intent.updatedLayer.id && it.name.equals(trimmed, ignoreCase = true)
                    }
                    if (isDuplicate) {
                        viewModelScope.launch {
                            _effect.send(MainUiEffect.ShowToast("Слой с названием «$trimmed» уже существует"))
                        }
                    } else {
                        viewModelScope.launch {
                            projectRepository.updatePointLayer(activeName, intent.updatedLayer.copy(name = trimmed))
                            loadPointLayers(activeName)
                            _uiState.update { it.copy(selectedLayerForSettings = null) }
                        }
                    }
                }
            }
            is MainUiIntent.OpenLayerProperties -> {
                _uiState.update { it.copy(selectedLayerForProperties = intent.layer) }
            }
            is MainUiIntent.DismissLayerProperties -> {
                _uiState.update { it.copy(selectedLayerForProperties = null, isAddFieldDialogOpen = false, editingFieldDefinition = null) }
            }
            is MainUiIntent.OpenAddFieldDialog -> {
                _uiState.update { it.copy(isAddFieldDialogOpen = true, editingFieldDefinition = null) }
            }
            is MainUiIntent.OpenEditFieldDialog -> {
                _uiState.update { it.copy(isAddFieldDialogOpen = true, editingFieldDefinition = intent.field) }
            }
            is MainUiIntent.DismissAddFieldDialog -> {
                _uiState.update { it.copy(isAddFieldDialogOpen = false, editingFieldDefinition = null) }
            }
            is MainUiIntent.AddLayerField -> {
                val activeName = _uiState.value.activeProjectName
                if (activeName != null) {
                    viewModelScope.launch {
                        val currentLayer = _uiState.value.pointLayers.find { it.id == intent.layerId }
                            ?: _uiState.value.selectedLayerForProperties
                        if (currentLayer != null) {
                            val updatedSchema = currentLayer.fieldsSchema + intent.field
                            val updatedLayer = currentLayer.copy(fieldsSchema = updatedSchema)
                            projectRepository.updatePointLayer(activeName, updatedLayer)
                            loadPointLayers(activeName)
                            _uiState.update {
                                it.copy(
                                    selectedLayerForProperties = updatedLayer,
                                    isAddFieldDialogOpen = false,
                                    editingFieldDefinition = null
                                )
                            }
                        }
                    }
                }
            }
            is MainUiIntent.UpdateLayerField -> {
                val activeName = _uiState.value.activeProjectName
                if (activeName != null) {
                    viewModelScope.launch {
                        val currentLayer = _uiState.value.pointLayers.find { it.id == intent.layerId }
                            ?: _uiState.value.selectedLayerForProperties
                        if (currentLayer != null) {
                            val updatedSchema = currentLayer.fieldsSchema.map { existing ->
                                if (existing.key == intent.field.key) intent.field else existing
                            }
                            val updatedLayer = currentLayer.copy(fieldsSchema = updatedSchema)
                            projectRepository.updatePointLayer(activeName, updatedLayer)
                            loadPointLayers(activeName)
                            _uiState.update {
                                it.copy(
                                    selectedLayerForProperties = updatedLayer,
                                    isAddFieldDialogOpen = false,
                                    editingFieldDefinition = null
                                )
                            }
                        }
                    }
                }
            }
            is MainUiIntent.DeleteLayerField -> {
                val activeName = _uiState.value.activeProjectName
                if (activeName != null) {
                    viewModelScope.launch {
                        val currentLayer = _uiState.value.pointLayers.find { it.id == intent.layerId }
                            ?: _uiState.value.selectedLayerForProperties
                        if (currentLayer != null) {
                            val updatedSchema = currentLayer.fieldsSchema.filterNot { it.key == intent.fieldKey }
                            val updatedLayer = currentLayer.copy(fieldsSchema = updatedSchema)
                            projectRepository.updatePointLayer(activeName, updatedLayer)
                            loadPointLayers(activeName)
                            _uiState.update {
                                it.copy(selectedLayerForProperties = updatedLayer)
                            }
                        }
                    }
                }
            }
            is MainUiIntent.StartPointEditorMode -> {
                _uiState.update {
                    it.copy(
                        editingPointLayer = intent.layer,
                        isLayerManagerOpen = false,
                        isEditPointDialogOpen = false,
                        editingPoint = null
                    )
                }
            }
            is MainUiIntent.ExitPointEditorMode -> {
                _uiState.update {
                    it.copy(
                        editingPointLayer = null,
                        editingPoint = null,
                        isEditPointDialogOpen = false
                    )
                }
            }
            is MainUiIntent.OpenCreatePointDialog -> {
                val currentLayer = _uiState.value.editingPointLayer
                if (currentLayer != null) {
                    val defaultCustomValues = currentLayer.fieldsSchema.associate { field ->
                        val defVal = if (field.type == LayerFieldType.DATETIME) {
                            LayerFieldDateTimeUtils.resolveDefaultValue(field.defaultValue)
                        } else {
                            field.defaultValue
                        }
                        field.key to defVal
                    }
                    val newPoint = LayerPoint(
                        id = 0L,
                        layerId = currentLayer.id,
                        name = "",
                        x = intent.cursorPx.first,
                        y = intent.cursorPx.second,
                        shape = currentLayer.defaultShape,
                        color = currentLayer.defaultColor,
                        customValues = defaultCustomValues
                    )
                    _uiState.update {
                        it.copy(
                            editingPoint = newPoint,
                            isEditPointDialogOpen = true
                        )
                    }
                }
            }
            is MainUiIntent.OpenEditPointDialog -> {
                _uiState.update {
                    it.copy(
                        editingPoint = intent.point,
                        isEditPointDialogOpen = true
                    )
                }
            }
            is MainUiIntent.DismissEditPointDialog -> {
                _uiState.update {
                    it.copy(
                        editingPoint = null,
                        isEditPointDialogOpen = false
                    )
                }
            }
            is MainUiIntent.SaveLayerPoint -> {
                val activeName = _uiState.value.activeProjectName
                if (activeName != null) {
                    viewModelScope.launch {
                        if (intent.point.id == 0L) {
                            projectRepository.insertLayerPoint(activeName, intent.point)
                        } else {
                            projectRepository.updateLayerPoint(activeName, intent.point)
                        }
                        loadPointLayers(activeName)
                        _uiState.update {
                            it.copy(
                                editingPoint = null,
                                isEditPointDialogOpen = false,
                                selectedPointForDetails = if (it.selectedPointForDetails?.id == intent.point.id) intent.point else it.selectedPointForDetails
                            )
                        }
                    }
                }
            }
            is MainUiIntent.DeleteLayerPoint -> {
                val activeName = _uiState.value.activeProjectName
                if (activeName != null) {
                    viewModelScope.launch {
                        projectRepository.deleteLayerPoint(activeName, intent.pointId)
                        loadPointLayers(activeName)
                        _uiState.update {
                            it.copy(
                                editingPoint = null,
                                isEditPointDialogOpen = false,
                                selectedPointForDetails = if (it.selectedPointForDetails?.id == intent.pointId) null else it.selectedPointForDetails
                            )
                        }
                    }
                }
            }
            is MainUiIntent.SelectPoint -> {
                _uiState.update { it.copy(selectedPointForDetails = intent.point) }
            }
            is MainUiIntent.DismissPointDetails -> {
                _uiState.update { it.copy(selectedPointForDetails = null) }
            }
            is MainUiIntent.CenterOnPoint -> {
                val meta = _uiState.value.activeProjectMetadata
                if (meta != null) {
                    val targetLatLng = CaveMapBounds.imagePixelsToLatLng(
                        pixelX = intent.point.x,
                        pixelY = intent.point.y,
                        imageWidth = meta.imageWidth,
                        imageHeight = meta.imageHeight,
                        maxZoom = meta.zoomMax
                    )
                    _uiState.update {
                        it.copy(
                            activeProjectCameraPosition = MapCameraPosition(
                                targetLat = targetLatLng.latitude,
                                targetLon = targetLatLng.longitude,
                                zoom = it.activeProjectCameraPosition?.zoom ?: (meta.zoomMax.toDouble() - 1.0),
                                bearing = it.activeProjectCameraPosition?.bearing ?: 0.0
                            )
                        )
                    }
                }
            }
            is MainUiIntent.OpenPointPlacementControl -> {
                _uiState.update { it.copy(isPointPlacementControlOpen = true) }
            }
            is MainUiIntent.DismissPointPlacementControl -> {
                _uiState.update { it.copy(isPointPlacementControlOpen = false) }
            }
            is MainUiIntent.SavePointPlacementMode -> {
                viewModelScope.launch {
                    settingsRepository.setPointPlacementMode(intent.mode)
                    _uiState.update { it.copy(isPointPlacementControlOpen = false) }
                }
            }
            is MainUiIntent.OpenPointEditorHelp -> {
                _uiState.update { it.copy(isPointEditorHelpOpen = true) }
            }
            is MainUiIntent.DismissPointEditorHelp -> {
                _uiState.update { it.copy(isPointEditorHelpOpen = false) }
            }
            is MainUiIntent.OpenLineLayerManager -> {
                val activeName = _uiState.value.activeProjectName
                if (activeName != null) {
                    viewModelScope.launch {
                        loadLineLayers(activeName)
                        _uiState.update {
                            it.copy(
                                isLineLayerManagerOpen = true,
                                isMenuExpanded = false
                            )
                        }
                    }
                }
            }
            is MainUiIntent.DismissLineLayerManager -> {
                _uiState.update { it.copy(isLineLayerManagerOpen = false) }
            }
            is MainUiIntent.OpenCreateLineLayerDialog -> {
                _uiState.update { it.copy(isCreateLineLayerOpen = true) }
            }
            is MainUiIntent.DismissCreateLineLayerDialog -> {
                _uiState.update { it.copy(isCreateLineLayerOpen = false) }
            }
            is MainUiIntent.CreateLineLayer -> {
                val activeName = _uiState.value.activeProjectName
                if (activeName != null) {
                    val trimmed = intent.name.trim()
                    val isDuplicate = _uiState.value.lineLayers.any { it.name.equals(trimmed, ignoreCase = true) }
                    if (isDuplicate) {
                        viewModelScope.launch {
                            _effect.send(MainUiEffect.ShowToast("Слой линий с названием «$trimmed» уже существует"))
                        }
                    } else {
                        viewModelScope.launch {
                            val newLayer = LineLayer(name = trimmed)
                            projectRepository.insertLineLayer(activeName, newLayer)
                            loadLineLayers(activeName)
                            _uiState.update { it.copy(isCreateLineLayerOpen = false) }
                        }
                    }
                }
            }
            is MainUiIntent.ToggleLineLayerVisibility -> {
                val activeName = _uiState.value.activeProjectName
                if (activeName != null) {
                    viewModelScope.launch {
                        val currentLayer = _uiState.value.lineLayers.find { it.id == intent.layerId }
                        val currentVis = currentLayer?.isVisible ?: true
                        projectRepository.toggleLineLayerVisibility(activeName, intent.layerId, !currentVis)
                        loadLineLayers(activeName)
                    }
                }
            }
            is MainUiIntent.DeleteLineLayer -> {
                val activeName = _uiState.value.activeProjectName
                if (activeName != null) {
                    viewModelScope.launch {
                        projectRepository.deleteLineLayer(activeName, intent.layerId)
                        loadLineLayers(activeName)
                        _uiState.update {
                            it.copy(
                                editingLineLayer = if (it.editingLineLayer?.id == intent.layerId) null else it.editingLineLayer,
                                selectedLineLayerForSettings = if (it.selectedLineLayerForSettings?.id == intent.layerId) null else it.selectedLineLayerForSettings,
                                selectedLineLayerForProperties = if (it.selectedLineLayerForProperties?.id == intent.layerId) null else it.selectedLineLayerForProperties
                            )
                        }
                    }
                }
            }
            is MainUiIntent.OpenLineLayerSettings -> {
                _uiState.update { it.copy(selectedLineLayerForSettings = intent.layer) }
            }
            is MainUiIntent.DismissLineLayerSettings -> {
                _uiState.update { it.copy(selectedLineLayerForSettings = null) }
            }
            is MainUiIntent.SaveLineLayerSettings -> {
                val activeName = _uiState.value.activeProjectName
                if (activeName != null) {
                    val trimmed = intent.updatedLayer.name.trim()
                    val isDuplicate = _uiState.value.lineLayers.any {
                        it.id != intent.updatedLayer.id && it.name.equals(trimmed, ignoreCase = true)
                    }
                    if (isDuplicate) {
                        viewModelScope.launch {
                            _effect.send(MainUiEffect.ShowToast("Слой с названием «$trimmed» уже существует"))
                        }
                    } else {
                        viewModelScope.launch {
                            projectRepository.updateLineLayer(activeName, intent.updatedLayer.copy(name = trimmed))
                            loadLineLayers(activeName)
                            _uiState.update { it.copy(selectedLineLayerForSettings = null) }
                        }
                    }
                }
            }
            is MainUiIntent.OpenLineLayerProperties -> {
                _uiState.update { it.copy(selectedLineLayerForProperties = intent.layer) }
            }
            is MainUiIntent.DismissLineLayerProperties -> {
                _uiState.update { it.copy(selectedLineLayerForProperties = null, isAddLineFieldDialogOpen = false, editingLineFieldDefinition = null) }
            }
            is MainUiIntent.OpenAddLineFieldDialog -> {
                _uiState.update { it.copy(isAddLineFieldDialogOpen = true, editingLineFieldDefinition = null) }
            }
            is MainUiIntent.OpenEditLineFieldDialog -> {
                _uiState.update { it.copy(isAddLineFieldDialogOpen = true, editingLineFieldDefinition = intent.field) }
            }
            is MainUiIntent.DismissAddLineFieldDialog -> {
                _uiState.update { it.copy(isAddLineFieldDialogOpen = false, editingLineFieldDefinition = null) }
            }
            is MainUiIntent.AddLineLayerField -> {
                val activeName = _uiState.value.activeProjectName
                if (activeName != null) {
                    viewModelScope.launch {
                        val currentLayer = _uiState.value.lineLayers.find { it.id == intent.layerId }
                            ?: _uiState.value.selectedLineLayerForProperties
                        if (currentLayer != null) {
                            val updatedSchema = currentLayer.fieldsSchema + intent.field
                            val updatedLayer = currentLayer.copy(fieldsSchema = updatedSchema)
                            projectRepository.updateLineLayer(activeName, updatedLayer)
                            loadLineLayers(activeName)
                            _uiState.update {
                                it.copy(
                                    selectedLineLayerForProperties = updatedLayer,
                                    isAddLineFieldDialogOpen = false,
                                    editingLineFieldDefinition = null
                                )
                            }
                        }
                    }
                }
            }
            is MainUiIntent.UpdateLineLayerField -> {
                val activeName = _uiState.value.activeProjectName
                if (activeName != null) {
                    viewModelScope.launch {
                        val currentLayer = _uiState.value.lineLayers.find { it.id == intent.layerId }
                            ?: _uiState.value.selectedLineLayerForProperties
                        if (currentLayer != null) {
                            val updatedSchema = currentLayer.fieldsSchema.map { existing ->
                                if (existing.key == intent.field.key) intent.field else existing
                            }
                            val updatedLayer = currentLayer.copy(fieldsSchema = updatedSchema)
                            projectRepository.updateLineLayer(activeName, updatedLayer)
                            loadLineLayers(activeName)
                            _uiState.update {
                                it.copy(
                                    selectedLineLayerForProperties = updatedLayer,
                                    isAddLineFieldDialogOpen = false,
                                    editingLineFieldDefinition = null
                                )
                            }
                        }
                    }
                }
            }
            is MainUiIntent.DeleteLineLayerField -> {
                val activeName = _uiState.value.activeProjectName
                if (activeName != null) {
                    viewModelScope.launch {
                        val currentLayer = _uiState.value.lineLayers.find { it.id == intent.layerId }
                            ?: _uiState.value.selectedLineLayerForProperties
                        if (currentLayer != null) {
                            val updatedSchema = currentLayer.fieldsSchema.filter { it.key != intent.fieldKey }
                            val updatedLayer = currentLayer.copy(fieldsSchema = updatedSchema)
                            projectRepository.updateLineLayer(activeName, updatedLayer)
                            loadLineLayers(activeName)
                            _uiState.update {
                                it.copy(
                                    selectedLineLayerForProperties = updatedLayer
                                )
                            }
                        }
                    }
                }
            }
            is MainUiIntent.StartLineDrawingMode -> {
                _uiState.update {
                    it.copy(
                        editingLineLayer = intent.layer,
                        drawingLinePoints = emptyList(),
                        editingLine = null,
                        isEditLineDialogOpen = false,
                        isLineLayerManagerOpen = false,
                        isMenuExpanded = false,
                        editingPointLayer = null,
                        editingPoint = null
                    )
                }
            }
            is MainUiIntent.ExitLineDrawingMode -> {
                _uiState.update {
                    it.copy(
                        editingLineLayer = null,
                        drawingLinePoints = emptyList(),
                        editingLine = null,
                        isEditLineDialogOpen = false
                    )
                }
            }
            is MainUiIntent.AddDrawingLineVertex -> {
                _uiState.update {
                    it.copy(
                        drawingLinePoints = it.drawingLinePoints + ScaleBindingPoint(intent.latLng, intent.pointPx)
                    )
                }
            }
            is MainUiIntent.UndoDrawingLineVertex -> {
                _uiState.update {
                    it.copy(
                        drawingLinePoints = if (it.drawingLinePoints.isNotEmpty()) it.drawingLinePoints.dropLast(1) else emptyList()
                    )
                }
            }
            is MainUiIntent.CompleteLineDrawing -> {
                val layer = _uiState.value.editingLineLayer
                val pts = _uiState.value.drawingLinePoints.map { it.imagePx }
                if (layer != null && pts.size >= 2) {
                    var lenPx = 0.0
                    for (i in 0 until pts.size - 1) {
                        val p1 = pts[i]
                        val p2 = pts[i + 1]
                        val dx = p2.first - p1.first
                        val dy = p2.second - p1.second
                        lenPx += kotlin.math.sqrt(dx * dx + dy * dy)
                    }
                    val ppm = _uiState.value.activeProjectMetadata?.pixelsPerMeter ?: 0.0
                    val lenMeters = if (ppm > 0.0) lenPx / ppm else 0.0
                    val defaultName = "Линия ${(_uiState.value.layerLineCounts[layer.id] ?: 0) + 1}"
                    val newLine = LayerLine(
                        layerId = layer.id,
                        name = defaultName,
                        points = pts,
                        lengthPx = lenPx,
                        lengthMeters = lenMeters,
                        environmentType = layer.defaultEnvironment,
                        haloColor = if (layer.defaultEnvironment == com.vktrsansara.app.caveviewer.domain.model.LineEnvironmentType.CUSTOM) 0xFFEAB308 else layer.defaultEnvironment.defaultHaloColor
                    )
                    _uiState.update {
                        it.copy(
                            editingLine = newLine,
                            isEditLineDialogOpen = true
                        )
                    }
                }
            }
            is MainUiIntent.DismissEditLineDialog -> {
                _uiState.update { it.copy(isEditLineDialogOpen = false, editingLine = null) }
            }
            is MainUiIntent.OpenEditLineDialog -> {
                _uiState.update { it.copy(isEditLineDialogOpen = true, editingLine = intent.line) }
            }
            is MainUiIntent.SaveLayerLine -> {
                val activeName = _uiState.value.activeProjectName
                if (activeName != null) {
                    val pts = intent.line.points
                    var lenPx = 0.0
                    for (i in 0 until pts.size - 1) {
                        val p1 = pts[i]
                        val p2 = pts[i + 1]
                        val dx = p2.first - p1.first
                        val dy = p2.second - p1.second
                        lenPx += kotlin.math.sqrt(dx * dx + dy * dy)
                    }
                    val ppm = _uiState.value.activeProjectMetadata?.pixelsPerMeter ?: 0.0
                    val lenMeters = if (ppm > 0.0) lenPx / ppm else 0.0
                    val lineToSave = intent.line.copy(
                        name = intent.line.name.trim(),
                        lengthPx = lenPx,
                        lengthMeters = lenMeters
                    )

                    viewModelScope.launch {
                        if (lineToSave.id == 0L) {
                            projectRepository.insertLayerLine(activeName, lineToSave)
                        } else {
                            projectRepository.updateLayerLine(activeName, lineToSave)
                        }
                        loadLineLayers(activeName)
                        _uiState.update {
                            it.copy(
                                drawingLinePoints = emptyList(),
                                editingLine = null,
                                isEditLineDialogOpen = false,
                                selectedLineForDetails = null
                            )
                        }
                    }
                }
            }
            is MainUiIntent.DeleteLayerLine -> {
                val activeName = _uiState.value.activeProjectName
                if (activeName != null) {
                    viewModelScope.launch {
                        projectRepository.deleteLayerLine(activeName, intent.lineId)
                        loadLineLayers(activeName)
                        _uiState.update {
                            it.copy(
                                selectedLineForDetails = if (it.selectedLineForDetails?.id == intent.lineId) null else it.selectedLineForDetails
                            )
                        }
                    }
                }
            }
            is MainUiIntent.SelectLine -> {
                _uiState.update { it.copy(selectedLineForDetails = intent.line, selectedPointForDetails = null) }
            }
            is MainUiIntent.DismissLineDetails -> {
                _uiState.update { it.copy(selectedLineForDetails = null) }
            }
            is MainUiIntent.CenterOnLine -> {
                // Focus camera handled in UI
            }
        }
    }

    private suspend fun loadPointLayers(projectName: String) {
        val layers = projectRepository.getPointLayers(projectName)
        val pointCounts = mutableMapOf<Long, Int>()
        layers.forEach { layer ->
            val points = projectRepository.getPointsForLayer(projectName, layer.id)
            pointCounts[layer.id] = points.size
        }
        val allPoints = projectRepository.getAllVisiblePoints(projectName)
        _uiState.update {
            it.copy(
                pointLayers = layers,
                layerPointCounts = pointCounts,
                allVisiblePoints = allPoints
            )
        }
    }

    private suspend fun loadLineLayers(projectName: String) {
        val layers = projectRepository.getLineLayers(projectName)
        val lineCounts = mutableMapOf<Long, Int>()
        layers.forEach { layer ->
            val lines = projectRepository.getLinesForLayer(projectName, layer.id)
            lineCounts[layer.id] = lines.size
        }
        val allLines = projectRepository.getAllVisibleLines(projectName)
        _uiState.update {
            it.copy(
                lineLayers = layers,
                layerLineCounts = lineCounts,
                allVisibleLines = allLines
            )
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
