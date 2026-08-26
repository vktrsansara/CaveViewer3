package com.vktrsansara.app.caveviewer.presentation.main

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vktrsansara.app.caveviewer.domain.model.MapCameraPosition
import com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds
import com.vktrsansara.app.caveviewer.presentation.components.FloatingBottomBar
import com.vktrsansara.app.caveviewer.presentation.components.MenuPopover
import com.vktrsansara.app.caveviewer.presentation.main.components.NoProjectPlaceholder
import com.vktrsansara.app.caveviewer.presentation.map.MapLibreViewer
import com.vktrsansara.app.caveviewer.presentation.map.components.BindingSideControl
import com.vktrsansara.app.caveviewer.presentation.map.components.CompassWidget
import com.vktrsansara.app.caveviewer.presentation.map.components.MapCursorOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.NorthBindingOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.ScaleBarWidget
import com.vktrsansara.app.caveviewer.presentation.map.components.ScaleBindingOverlay
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.NorthBindingHelpDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.NorthBindingInputDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.ScaleBindingHelpDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.ScaleBindingInputDialog
import com.vktrsansara.app.caveviewer.presentation.metadata.MetadataEditorScreen
import com.vktrsansara.app.caveviewer.presentation.projects.CreateRasterProjectScreen
import com.vktrsansara.app.caveviewer.presentation.projects.FeatureUnderDevelopmentScreen
import com.vktrsansara.app.caveviewer.presentation.projects.ProjectTypeDialog
import com.vktrsansara.app.caveviewer.presentation.projects.ProjectsListScreen
import com.vktrsansara.app.caveviewer.presentation.settings.AppSettingsScreen
import com.vktrsansara.app.caveviewer.presentation.settings.ToolsSettingsScreen
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import com.vktrsansara.app.caveviewer.ui.theme.CaveViewerTheme
import org.maplibre.android.geometry.LatLng
import kotlin.math.sqrt

/**
 * Root Composable host that reacts to MVI state and manages screen navigation.
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Effect handling
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MainUiEffect.ExitApp -> {
                    (context as? android.app.Activity)?.finish()
                }
                is MainUiEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Modal dialog for selecting project type
    if (uiState.isProjectTypeDialogVisible) {
        ProjectTypeDialog(
            onSelectRasterProject = { viewModel.handleIntent(MainUiIntent.SelectRasterProjectType) },
            onSelectTopographyProject = { viewModel.handleIntent(MainUiIntent.SelectTopographyProjectType) },
            onSelectTherionProject = { viewModel.handleIntent(MainUiIntent.SelectTherionProjectType) },
            onDismiss = { viewModel.handleIntent(MainUiIntent.DismissProjectTypeDialog) }
        )
    }

    // Scale Binding Help Dialog
    if (uiState.isScaleBindingHelpVisible) {
        ScaleBindingHelpDialog(
            onDismiss = { viewModel.handleIntent(MainUiIntent.DismissScaleBindingHelp) }
        )
    }

    // Scale Binding Input Dialog
    if (uiState.isScaleBindingInputVisible && uiState.scaleBindingPoints.size >= 2) {
        val p1 = uiState.scaleBindingPoints[0].imagePx
        val p2 = uiState.scaleBindingPoints[1].imagePx
        val dx = p2.first - p1.first
        val dy = p2.second - p1.second
        val measuredPixels = sqrt(dx * dx + dy * dy)

        ScaleBindingInputDialog(
            measuredPixels = measuredPixels,
            onSave = { ppm, meters ->
                viewModel.handleIntent(MainUiIntent.SaveScaleBinding(ppm, meters))
            },
            onCancel = {
                viewModel.handleIntent(MainUiIntent.DismissScaleBindingInput)
            }
        )
    }

    // North Binding Help Dialog
    if (uiState.isNorthBindingHelpVisible) {
        NorthBindingHelpDialog(
            onDismiss = { viewModel.handleIntent(MainUiIntent.DismissNorthBindingHelp) }
        )
    }

    // North Binding Input Dialog
    if (uiState.isNorthBindingInputVisible && uiState.northBindingPoints.size >= 2) {
        val p1 = uiState.northBindingPoints[0].imagePx
        val p2 = uiState.northBindingPoints[1].imagePx
        val measuredAngle = CaveMapBounds.calculateNorthAngle(p1, p2)

        NorthBindingInputDialog(
            measuredAngle = measuredAngle,
            onSave = { angle ->
                viewModel.handleIntent(MainUiIntent.SaveNorthBinding(angle))
            },
            onCancel = {
                viewModel.handleIntent(MainUiIntent.DismissNorthBindingInput)
            }
        )
    }

    // Handle system back gesture
    BackHandler(
        enabled = uiState.currentScreen != AppScreen.MAIN ||
                uiState.isScaleBindingMode ||
                uiState.isNorthBindingMode
    ) {
        when {
            uiState.isScaleBindingMode -> viewModel.handleIntent(MainUiIntent.CancelScaleBinding)
            uiState.isNorthBindingMode -> viewModel.handleIntent(MainUiIntent.CancelNorthBinding)
            else -> viewModel.handleIntent(MainUiIntent.NavigateBack)
        }
    }

    AnimatedContent(
        targetState = uiState.currentScreen,
        transitionSpec = { fadeIn().togetherWith(fadeOut()) },
        label = "ScreenTransition",
        modifier = modifier.fillMaxSize()
    ) { screen ->
        when (screen) {
            AppScreen.MAIN -> {
                MainScreenContent(
                    uiState = uiState,
                    onIntent = viewModel::handleIntent
                )
            }
            AppScreen.APP_SETTINGS -> {
                AppSettingsScreen(
                    settings = uiState.settings,
                    onThemeChanged = { viewModel.handleIntent(MainUiIntent.UpdateTheme(it)) },
                    onFullscreenChanged = { viewModel.handleIntent(MainUiIntent.UpdateFullscreen(it)) },
                    onShowCompassChanged = { viewModel.handleIntent(MainUiIntent.OnShowCompassChanged(it)) },
                    onShowScaleBarChanged = { viewModel.handleIntent(MainUiIntent.OnShowScaleBarChanged(it)) },
                    onNavigateBack = { viewModel.handleIntent(MainUiIntent.NavigateBack) }
                )
            }
            AppScreen.TOOLS_SETTINGS -> {
                ToolsSettingsScreen(
                    settings = uiState.settings,
                    onCursorShowChanged = { viewModel.handleIntent(MainUiIntent.UpdateCursorShow(it)) },
                    onCursorTypeChanged = { viewModel.handleIntent(MainUiIntent.UpdateCursorType(it)) },
                    onCursorColorChanged = { viewModel.handleIntent(MainUiIntent.UpdateCursorColor(it)) },
                    onNavigateBack = { viewModel.handleIntent(MainUiIntent.NavigateBack) }
                )
            }
            AppScreen.PROJECTS_LIST -> {
                ProjectsListScreen(
                    projects = uiState.projectsList,
                    activeProjectName = uiState.activeProjectName,
                    onSelectProject = { viewModel.handleIntent(MainUiIntent.SelectProject(it)) },
                    onDeleteProject = { viewModel.handleIntent(MainUiIntent.DeleteProject(it)) },
                    onNavigateBack = { viewModel.handleIntent(MainUiIntent.NavigateBack) }
                )
            }
            AppScreen.CREATE_RASTER_PROJECT -> {
                CreateRasterProjectScreen(
                    isSaving = uiState.isProjectSaving,
                    savingProgress = uiState.projectSavingProgress,
                    savingStatusText = uiState.projectSavingStatusText,
                    onCreateProject = { name, uri ->
                        viewModel.handleIntent(MainUiIntent.CreateRasterProject(name, uri))
                    },
                    onCancelSaving = {
                        viewModel.handleIntent(MainUiIntent.CancelProjectCreation)
                    },
                    onNavigateBack = { viewModel.handleIntent(MainUiIntent.NavigateBack) }
                )
            }
            AppScreen.METADATA_EDITOR -> {
                val metadata = uiState.activeProjectMetadata
                val activeName = uiState.activeProjectName
                if (metadata != null && activeName != null) {
                    MetadataEditorScreen(
                        metadata = metadata,
                        location = uiState.activeProjectLocation,
                        entrances = uiState.activeProjectEntrances,
                        cadastralData = uiState.activeProjectCadastralData,
                        onSaveMetadata = { updatedMeta, updatedLocation, updatedEntrances, updatedCadastral ->
                            viewModel.handleIntent(
                                MainUiIntent.SaveMetadata(
                                    updatedMetadata = updatedMeta,
                                    originalProjectName = activeName,
                                    location = updatedLocation,
                                    entrances = updatedEntrances,
                                    cadastralData = updatedCadastral
                                )
                            )
                        },
                        onNavigateBack = { viewModel.handleIntent(MainUiIntent.NavigateBack) }
                    )
                }
            }
            AppScreen.FEATURE_UNDER_DEVELOPMENT -> {
                FeatureUnderDevelopmentScreen(
                    featureTitle = uiState.underDevelopmentFeatureName,
                    onNavigateBack = { viewModel.handleIntent(MainUiIntent.NavigateBack) }
                )
            }
        }
    }
}

@Composable
fun MainScreenContent(
    uiState: MainUiState,
    onIntent: (MainUiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialPos = uiState.activeProjectCameraPosition
    var mapBearing by remember(initialPos) { mutableDoubleStateOf(initialPos?.bearing ?: 0.0) }
    var currentZoom by remember(initialPos) { mutableDoubleStateOf(initialPos?.zoom ?: 0.0) }
    var currentTargetLat by remember(initialPos) { mutableDoubleStateOf(initialPos?.targetLat ?: 0.0) }
    var currentTargetLon by remember(initialPos) { mutableDoubleStateOf(initialPos?.targetLon ?: 0.0) }
    var resetBearingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var mapMetadata by remember(uiState.activeProjectMetadata) { mutableStateOf(uiState.activeProjectMetadata) }
    var bindingScreenPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    val isAnyCalibrationMode = uiState.isScaleBindingMode || uiState.isNorthBindingMode
    val activeBindingPoints = if (uiState.isNorthBindingMode) uiState.northBindingPoints else uiState.scaleBindingPoints

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bgMain)
    ) {
        val oneThirdFromBottom = maxHeight / 3

        // Main content: active MapLibreViewer or NoProjectPlaceholder
        val activeDir = uiState.activeProjectDir
        if (activeDir != null && uiState.hasActiveProject) {
            MapLibreViewer(
                projectDir = activeDir,
                initialCameraPosition = initialPos,
                bindingPoints = activeBindingPoints,
                onCameraPositionChanged = { lat, lon, zoom, bearing ->
                    currentTargetLat = lat
                    currentTargetLon = lon
                    mapBearing = bearing
                    currentZoom = zoom
                    onIntent(
                        MainUiIntent.UpdateMapCameraPosition(
                            MapCameraPosition(
                                targetLat = lat,
                                targetLon = lon,
                                zoom = zoom,
                                bearing = bearing
                            )
                        )
                    )
                },
                onBindingScreenPointsChanged = { points ->
                    bindingScreenPoints = points
                },
                onMapCenterClick = { centerLatLng ->
                    when {
                        uiState.isScaleBindingMode -> onIntent(MainUiIntent.AddScaleBindingPoint(centerLatLng))
                        uiState.isNorthBindingMode -> onIntent(MainUiIntent.AddNorthBindingPoint(centerLatLng))
                    }
                },
                onResetBearingReady = { action ->
                    resetBearingAction = action
                },
                onMetadataLoaded = { meta ->
                    mapMetadata = meta
                    onIntent(MainUiIntent.OnMetadataLoaded(meta))
                },
                modifier = Modifier.fillMaxSize()
            )

            val meta = mapMetadata ?: uiState.activeProjectMetadata

            // Scale Calibration Overlay
            if (uiState.isScaleBindingMode && meta != null) {
                val centerPx = CaveMapBounds.latLngToImagePixels(
                    latLng = LatLng(currentTargetLat, currentTargetLon),
                    imageWidth = meta.imageWidth,
                    imageHeight = meta.imageHeight,
                    maxZoom = meta.zoomMax
                )
                ScaleBindingOverlay(
                    points = uiState.scaleBindingPoints,
                    screenPoints = bindingScreenPoints,
                    currentCenterPx = centerPx
                )
            }

            // North Calibration Overlay
            if (uiState.isNorthBindingMode && meta != null) {
                val centerPx = CaveMapBounds.latLngToImagePixels(
                    latLng = LatLng(currentTargetLat, currentTargetLon),
                    imageWidth = meta.imageWidth,
                    imageHeight = meta.imageHeight,
                    maxZoom = meta.zoomMax
                )
                NorthBindingOverlay(
                    points = uiState.northBindingPoints,
                    screenPoints = bindingScreenPoints,
                    currentCenterPx = centerPx
                )
            }

            // Central Cursor Overlay (Strictly centered on screen; always visible in calibration mode)
            MapCursorOverlay(
                cursorShow = isAnyCalibrationMode || uiState.settings.cursorShow,
                cursorType = uiState.settings.cursorType,
                cursorColor = uiState.settings.cursorColor
            )

            // 1. Compass Widget (Top-Start: top = 15.dp, start = 15.dp)
            if (uiState.settings.showCompass && meta != null && !isAnyCalibrationMode) {
                CompassWidget(
                    angleNorth = meta.angleNorth.toFloat(),
                    mapBearing = mapBearing,
                    onResetBearing = { resetBearingAction?.invoke() },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 15.dp, start = 15.dp)
                )
            }

            // 2. Scale Bar Widget (Top-End: top = 15.dp, end = 15.dp)
            if (uiState.settings.showScaleBar && meta != null && meta.pixelsPerMeter > 0.0 && meta.scaleMeters > 0.0 && !isAnyCalibrationMode) {
                ScaleBarWidget(
                    pixelsPerMeter = meta.pixelsPerMeter,
                    zoomMax = meta.zoomMax,
                    currentZoom = currentZoom,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 15.dp, end = 15.dp)
                )
            }

            // 3. Side Control Bar for Calibration Mode (positioned at 1/3 height from bottom)
            if (isAnyCalibrationMode) {
                BindingSideControl(
                    pointsCount = activeBindingPoints.size,
                    onClose = {
                        if (uiState.isNorthBindingMode) {
                            onIntent(MainUiIntent.CancelNorthBinding)
                        } else {
                            onIntent(MainUiIntent.CancelScaleBinding)
                        }
                    },
                    onUndo = {
                        if (uiState.isNorthBindingMode) {
                            onIntent(MainUiIntent.UndoNorthBindingPoint)
                        } else {
                            onIntent(MainUiIntent.UndoScaleBindingPoint)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 15.dp, bottom = oneThirdFromBottom)
                )
            }
        } else {
            NoProjectPlaceholder()
        }

        // Fullscreen transparent backdrop to dismiss menu on outside tap
        if (uiState.isMenuExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onIntent(MainUiIntent.DismissMenu)
                    }
            )
        }

        // Bottom control area: popover menu + floating bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Popover menu positioned directly above the bar with 8.dp spacing
            MenuPopover(
                isOpen = uiState.isMenuExpanded,
                hasActiveProject = uiState.hasActiveProject,
                onOpenAppSettings = { onIntent(MainUiIntent.OpenAppSettings) },
                onOpenToolsSettings = { onIntent(MainUiIntent.OpenToolsSettings) },
                onExitApp = { onIntent(MainUiIntent.ExitAppClicked) },
                onProjectListClick = { onIntent(MainUiIntent.ProjectListClicked) },
                onNewProjectClick = { onIntent(MainUiIntent.NewProjectClicked) },
                onImportProjectClick = { onIntent(MainUiIntent.ImportProjectClicked) },
                onExportProjectClick = { onIntent(MainUiIntent.ExportProjectClicked) },
                onCloseProject = { onIntent(MainUiIntent.CloseActiveProject) },
                onEditMetadataClick = { onIntent(MainUiIntent.OpenMetadataEditor) },
                onScaleBindingClick = { onIntent(MainUiIntent.StartScaleBinding) },
                onNorthBindingClick = { onIntent(MainUiIntent.StartNorthBinding) },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Floating bottom bar
            FloatingBottomBar(
                onMenuClick = { onIntent(MainUiIntent.ToggleMenu) }
            )
        }
    }
}

@Preview(name = "Main Screen - Dark", showBackground = true)
@Composable
private fun MainScreenDarkPreview() {
    CaveViewerTheme(darkTheme = true) {
        MainScreenContent(
            uiState = MainUiState(isMenuExpanded = false, hasActiveProject = false),
            onIntent = {}
        )
    }
}

@Preview(name = "Main Screen - Light", showBackground = true)
@Composable
private fun MainScreenLightPreview() {
    CaveViewerTheme(darkTheme = false) {
        MainScreenContent(
            uiState = MainUiState(isMenuExpanded = true, hasActiveProject = false),
            onIntent = {}
        )
    }
}
