package com.vktrsansara.app.caveviewer.presentation.main

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vktrsansara.app.caveviewer.domain.model.CompassTapMode
import com.vktrsansara.app.caveviewer.domain.model.MapCameraPosition
import com.vktrsansara.app.caveviewer.domain.model.ToolType
import com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds
import com.vktrsansara.app.caveviewer.presentation.components.FloatingBottomBar
import com.vktrsansara.app.caveviewer.presentation.components.FloatingDockAnchorLayout
import com.vktrsansara.app.caveviewer.presentation.components.MenuPopover
import com.vktrsansara.app.caveviewer.presentation.main.components.NoProjectPlaceholder
import com.vktrsansara.app.caveviewer.presentation.map.MapLibreViewer
import com.vktrsansara.app.caveviewer.presentation.map.OsmEntranceBindingViewer
import com.vktrsansara.app.caveviewer.presentation.map.components.AngleMeasureOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.AreaMeasureOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.AzimuthOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.BindingSideControl
import com.vktrsansara.app.caveviewer.presentation.map.components.CompassWidget
import com.vktrsansara.app.caveviewer.presentation.map.components.DeltaOffsetOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.FaultLineOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.MapCursorOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.MultiToolSideBar
import com.vktrsansara.app.caveviewer.presentation.map.components.NorthBindingOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.RadiusMeasureOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.RulerOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.getBindingSideControlCloseOffset
import com.vktrsansara.app.caveviewer.presentation.map.components.getMultiToolSideBarCloseOffset
import com.vktrsansara.app.caveviewer.presentation.map.components.ScaleBarWidget
import com.vktrsansara.app.caveviewer.presentation.map.components.ScaleBindingOverlay
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.DeltaOffsetHelpDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.EntranceBindingHelpDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.EntranceNameDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.MapFilterDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.MapFilterHelpDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.MultiToolDockHelpDialog
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

    // Entrance Binding Help Dialog
    if (uiState.isEntranceBindingHelpVisible) {
        EntranceBindingHelpDialog(
            onDismiss = { viewModel.handleIntent(MainUiIntent.DismissEntranceBindingHelp) }
        )
    }

    // Entrance GPS Name Dialog
    if (uiState.isEntranceNameDialogVisible && uiState.pendingEntranceGps != null) {
        val nextIndex = uiState.activeProjectEntrances.size + 1
        EntranceNameDialog(
            lat = uiState.pendingEntranceGps!!.latitude,
            lon = uiState.pendingEntranceGps!!.longitude,
            defaultName = "Точка входа #$nextIndex",
            onSave = { name ->
                viewModel.handleIntent(
                    MainUiIntent.SaveEntranceCoordinate(
                        name = name,
                        lat = uiState.pendingEntranceGps!!.latitude,
                        lon = uiState.pendingEntranceGps!!.longitude
                    )
                )
            },
            onCancel = {
                viewModel.handleIntent(MainUiIntent.DismissEntranceNameDialog)
            }
        )
    }

    // Map Filter Dialog
    if (uiState.isMapFilterDialogVisible) {
        MapFilterDialog(
            currentFilter = uiState.settings.mapFilter,
            onFilterSelected = { mode ->
                viewModel.handleIntent(MainUiIntent.SetMapFilterMode(mode))
            },
            onOpenHelp = {
                viewModel.handleIntent(MainUiIntent.OpenMapFilterHelpDialog)
            },
            onDismiss = {
                viewModel.handleIntent(MainUiIntent.DismissMapFilterDialog)
            }
        )
    }

    // Map Filter Help Dialog
    if (uiState.isMapFilterHelpDialogVisible) {
        MapFilterHelpDialog(
            onDismiss = {
                viewModel.handleIntent(MainUiIntent.DismissMapFilterHelpDialog)
            }
        )
    }

    // Delta Offset Help Dialog
    if (uiState.isDeltaOffsetHelpVisible) {
        DeltaOffsetHelpDialog(
            onDismiss = {
                viewModel.handleIntent(MainUiIntent.DismissDeltaOffsetHelp)
            }
        )
    }

    // Multi-Tool Dock Help Dialog
    if (uiState.isDockHelpVisible) {
        MultiToolDockHelpDialog(
            onDismiss = {
                viewModel.handleIntent(MainUiIntent.DismissDockHelp)
            }
        )
    }

    // Handle system back gesture
    BackHandler(
        enabled = uiState.currentScreen != AppScreen.MAIN ||
                uiState.isScaleBindingMode ||
                uiState.isNorthBindingMode ||
                uiState.isEntranceCavePickMode ||
                uiState.isOsmEntranceBindingMode ||
                uiState.dockedTools.isNotEmpty()
    ) {
        when {
            uiState.isOsmEntranceBindingMode -> viewModel.handleIntent(MainUiIntent.CloseOsmEntranceBinding)
            uiState.isEntranceCavePickMode -> viewModel.handleIntent(MainUiIntent.CancelEntranceCavePick)
            uiState.isScaleBindingMode -> viewModel.handleIntent(MainUiIntent.CancelScaleBinding)
            uiState.isNorthBindingMode -> viewModel.handleIntent(MainUiIntent.CancelNorthBinding)
            uiState.dockedTools.isNotEmpty() -> viewModel.handleIntent(MainUiIntent.HandleDockCloseClick)
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
                    onCompassTapModeChanged = { viewModel.handleIntent(MainUiIntent.UpdateCompassTapMode(it)) },
                    onShowScaleBarChanged = { viewModel.handleIntent(MainUiIntent.OnShowScaleBarChanged(it)) },
                    onNavigateBack = { viewModel.handleIntent(MainUiIntent.NavigateBack) }
                )
            }
            AppScreen.TOOLS_SETTINGS -> {
                ToolsSettingsScreen(
                    settings = uiState.settings,
                    activeMetadata = uiState.activeProjectMetadata,
                    onCursorShowChanged = { viewModel.handleIntent(MainUiIntent.UpdateCursorShow(it)) },
                    onCursorTypeChanged = { viewModel.handleIntent(MainUiIntent.UpdateCursorType(it)) },
                    onCursorColorChanged = { viewModel.handleIntent(MainUiIntent.UpdateCursorColor(it)) },
                    onGridSizeModeChanged = { viewModel.handleIntent(MainUiIntent.UpdateGridSizeMode(it)) },
                    onGridCustomSizeChanged = { viewModel.handleIntent(MainUiIntent.UpdateGridCustomSize(it)) },
                    onGridColorChanged = { viewModel.handleIntent(MainUiIntent.UpdateGridColor(it)) },
                    onColorPaletteModeChanged = { viewModel.handleIntent(MainUiIntent.UpdateColorPaletteMode(it)) },
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
    // If in OpenStreetMap entrance binding mode, show full-screen OSM viewer
    if (uiState.isOsmEntranceBindingMode) {
        OsmEntranceBindingViewer(
            entrances = uiState.activeProjectEntrances,
            cursorType = uiState.settings.cursorType,
            cursorColor = uiState.settings.cursorColor,
            onEntranceTapped = { latLng ->
                onIntent(MainUiIntent.OnOsmEntranceTapped(latLng))
            },
            onClose = {
                onIntent(MainUiIntent.CloseOsmEntranceBinding)
            },
            modifier = modifier
        )
        return
    }

    val initialPos = uiState.activeProjectCameraPosition
    var mapBearing by remember(initialPos) { mutableDoubleStateOf(initialPos?.bearing ?: 0.0) }
    var currentZoom by remember(initialPos) { mutableDoubleStateOf(initialPos?.zoom ?: 0.0) }
    var currentTargetLat by remember(initialPos) { mutableDoubleStateOf(initialPos?.targetLat ?: 0.0) }
    var currentTargetLon by remember(initialPos) { mutableDoubleStateOf(initialPos?.targetLon ?: 0.0) }
    var resetBearingAction by remember { mutableStateOf<((Double) -> Unit)?>(null) }
    var mapMetadata by remember(uiState.activeProjectMetadata) { mutableStateOf(uiState.activeProjectMetadata) }
    var bindingScreenPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var projector by remember { mutableStateOf<((LatLng) -> Offset)?>(null) }

    val isCalibrationMode = uiState.isScaleBindingMode || uiState.isNorthBindingMode || uiState.isEntranceCavePickMode
    val isAnyToolActive = uiState.activeTool != null
    val hasDockedTools = uiState.dockedTools.isNotEmpty()

    val activeBindingPoints = when {
        uiState.isNorthBindingMode -> uiState.northBindingPoints
        uiState.isScaleBindingMode -> uiState.scaleBindingPoints
        else -> emptyList()
    }

    // Screen offsets projected for each tool
    val rulerScreenPoints = remember(uiState.rulerPoints, projector, currentZoom, currentTargetLat, currentTargetLon, mapBearing) {
        uiState.rulerPoints.mapNotNull { pt -> projector?.invoke(pt.latLng) }
    }
    val areaScreenPoints = remember(uiState.areaPoints, projector, currentZoom, currentTargetLat, currentTargetLon, mapBearing) {
        uiState.areaPoints.mapNotNull { pt -> projector?.invoke(pt.latLng) }
    }
    val angleScreenPoints = remember(uiState.anglePoints, projector, currentZoom, currentTargetLat, currentTargetLon, mapBearing) {
        uiState.anglePoints.mapNotNull { pt -> projector?.invoke(pt.latLng) }
    }
    val azimuthScreenPoint = remember(uiState.azimuthOriginPoint, projector, currentZoom, currentTargetLat, currentTargetLon, mapBearing) {
        uiState.azimuthOriginPoint?.latLng?.let { projector?.invoke(it) }
    }
    val faultLineScreenPoints = remember(uiState.faultLinePoints, projector, currentZoom, currentTargetLat, currentTargetLon, mapBearing) {
        uiState.faultLinePoints.mapNotNull { pt -> projector?.invoke(pt.latLng) }
    }
    val radiusScreenPoint = remember(uiState.radiusCenterPoint, projector, currentZoom, currentTargetLat, currentTargetLon, mapBearing) {
        uiState.radiusCenterPoint?.latLng?.let { projector?.invoke(it) }
    }
    val deltaOffsetScreenPoint = remember(uiState.deltaOffsetOriginPoint, projector, currentZoom, currentTargetLat, currentTargetLon, mapBearing) {
        uiState.deltaOffsetOriginPoint?.latLng?.let { projector?.invoke(it) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bgMain)
    ) {
        // Main content: active MapLibreViewer or NoProjectPlaceholder
        val activeDir = uiState.activeProjectDir
        if (activeDir != null && uiState.hasActiveProject) {
            MapLibreViewer(
                projectDir = activeDir,
                initialCameraPosition = initialPos,
                settings = uiState.settings,
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
                onProjectorReady = { proj ->
                    projector = proj
                },
                onMapCenterClick = { centerLatLng ->
                    when {
                        uiState.isEntranceCavePickMode -> onIntent(MainUiIntent.OnEntrancePlanPicked(centerLatLng))
                        uiState.isScaleBindingMode -> onIntent(MainUiIntent.AddScaleBindingPoint(centerLatLng))
                        uiState.isNorthBindingMode -> onIntent(MainUiIntent.AddNorthBindingPoint(centerLatLng))
                        uiState.activeTool == ToolType.RULER -> onIntent(MainUiIntent.AddRulerPoint(centerLatLng))
                        uiState.activeTool == ToolType.AREA -> onIntent(MainUiIntent.AddAreaPoint(centerLatLng))
                        uiState.activeTool == ToolType.ANGLE -> onIntent(MainUiIntent.AddAnglePoint(centerLatLng))
                        uiState.activeTool == ToolType.AZIMUTH -> onIntent(MainUiIntent.SetAzimuthOriginPoint(centerLatLng))
                        uiState.activeTool == ToolType.FAULT_LINE -> onIntent(MainUiIntent.AddFaultLinePoint(centerLatLng))
                        uiState.activeTool == ToolType.RADIUS -> onIntent(MainUiIntent.SetRadiusCenterPoint(centerLatLng))
                        uiState.activeTool == ToolType.DELTA_OFFSET -> onIntent(MainUiIntent.SetDeltaOffsetOriginPoint(centerLatLng))
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
            val centerPx = if (meta != null) {
                CaveMapBounds.latLngToImagePixels(
                    latLng = LatLng(currentTargetLat, currentTargetLon),
                    imageWidth = meta.imageWidth,
                    imageHeight = meta.imageHeight,
                    maxZoom = meta.zoomMax
                )
            } else null

            // Scale Calibration Overlay
            if (uiState.isScaleBindingMode && meta != null && centerPx != null) {
                ScaleBindingOverlay(
                    points = uiState.scaleBindingPoints,
                    screenPoints = bindingScreenPoints,
                    currentCenterPx = centerPx
                )
            }

            // North Calibration Overlay
            if (uiState.isNorthBindingMode && meta != null && centerPx != null) {
                NorthBindingOverlay(
                    points = uiState.northBindingPoints,
                    screenPoints = bindingScreenPoints,
                    currentCenterPx = centerPx
                )
            }

            // Ruler Overlay
            if (ToolType.RULER in uiState.dockedTools && meta != null) {
                RulerOverlay(
                    points = uiState.rulerPoints,
                    screenPoints = rulerScreenPoints,
                    currentCenterPx = centerPx,
                    ppm = meta.pixelsPerMeter,
                    isActive = (uiState.activeTool == ToolType.RULER)
                )
            }

            // Area Measure Overlay
            if (ToolType.AREA in uiState.dockedTools && meta != null) {
                AreaMeasureOverlay(
                    points = uiState.areaPoints,
                    screenPoints = areaScreenPoints,
                    currentCenterPx = centerPx,
                    ppm = meta.pixelsPerMeter,
                    isActive = (uiState.activeTool == ToolType.AREA)
                )
            }

            // Angle Measure Overlay
            if (ToolType.ANGLE in uiState.dockedTools && meta != null) {
                AngleMeasureOverlay(
                    points = uiState.anglePoints,
                    screenPoints = angleScreenPoints,
                    currentCenterPx = centerPx,
                    ppm = meta.pixelsPerMeter,
                    isActive = (uiState.activeTool == ToolType.ANGLE)
                )
            }

            // Azimuth Overlay
            if (ToolType.AZIMUTH in uiState.dockedTools && meta != null) {
                AzimuthOverlay(
                    originPoint = uiState.azimuthOriginPoint,
                    originScreenPoint = azimuthScreenPoint,
                    currentCenterPx = centerPx,
                    angleNorth = meta.angleNorth,
                    ppm = meta.pixelsPerMeter,
                    isActive = (uiState.activeTool == ToolType.AZIMUTH)
                )
            }

            // Fault Line Overlay
            if (ToolType.FAULT_LINE in uiState.dockedTools && meta != null) {
                FaultLineOverlay(
                    points = uiState.faultLinePoints,
                    screenPoints = faultLineScreenPoints,
                    infiniteEndPoints = null,
                    currentCenterPx = centerPx,
                    angleNorth = meta.angleNorth,
                    ppm = meta.pixelsPerMeter,
                    isActive = (uiState.activeTool == ToolType.FAULT_LINE)
                )
            }

            // Radius Measure Overlay
            if (ToolType.RADIUS in uiState.dockedTools && meta != null) {
                RadiusMeasureOverlay(
                    centerPoint = uiState.radiusCenterPoint,
                    centerScreenPoint = radiusScreenPoint,
                    currentCenterPx = centerPx,
                    ppm = meta.pixelsPerMeter,
                    isActive = (uiState.activeTool == ToolType.RADIUS)
                )
            }

            // Delta Offset Overlay
            if (ToolType.DELTA_OFFSET in uiState.dockedTools && meta != null) {
                DeltaOffsetOverlay(
                    originPoint = uiState.deltaOffsetOriginPoint,
                    originScreenPoint = deltaOffsetScreenPoint,
                    currentCenterPx = centerPx,
                    angleNorth = meta.angleNorth,
                    ppm = meta.pixelsPerMeter,
                    isActive = (uiState.activeTool == ToolType.DELTA_OFFSET)
                )
            }

            // Step 1: Cave Entrance Pick Banner
            if (uiState.isEntranceCavePickMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp, start = 30.dp, end = 30.dp)
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.bgCard.copy(alpha = 0.95f))
                        .border(1.dp, AppColors.borderColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Наведите курсор на вход на плане пещеры и коснитесь экрана",
                        color = AppColors.textPrimary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Central Cursor Overlay (Strictly centered on screen; always visible in calibration or when active tool is open)
            MapCursorOverlay(
                cursorShow = isCalibrationMode || isAnyToolActive || uiState.settings.cursorShow,
                cursorType = uiState.settings.cursorType,
                cursorColor = uiState.settings.cursorColor
            )

            // 1. Compass Widget (Top-Start: top = 15.dp, start = 15.dp)
            if (uiState.settings.showCompass && meta != null && !isCalibrationMode && !isAnyToolActive) {
                CompassWidget(
                    angleNorth = meta.angleNorth.toFloat(),
                    mapBearing = mapBearing,
                    onResetBearing = {
                        val targetBearing = when (uiState.settings.compassTapMode) {
                            CompassTapMode.HORIZONTAL -> 0.0 // Горизонтальное выравнивание растра
                            CompassTapMode.SCREEN_NORTH -> {
                                // Поворот карты так, чтобы стрелка севера смотрела строго в верх экрана
                                (meta.angleNorth % 360.0 + 360.0) % 360.0
                            }
                        }
                        resetBearingAction?.invoke(targetBearing)
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 15.dp, start = 15.dp)
                )
            }

            // 2. Scale Bar Widget (Top-End: top = 15.dp, end = 15.dp)
            if (uiState.settings.showScaleBar && meta != null && meta.pixelsPerMeter > 0.0 && meta.scaleMeters > 0.0 && !isCalibrationMode && !isAnyToolActive) {
                ScaleBarWidget(
                    pixelsPerMeter = meta.pixelsPerMeter,
                    zoomMax = meta.zoomMax,
                    currentZoom = currentZoom,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 15.dp, end = 15.dp)
                )
            }

            // 3. Multi-Tool SideBar (Floating dock on the right) or Single Tool BindingSideControl or Calibration Side Control
            val dockedTools = uiState.dockedTools
            val activeTool = uiState.activeTool

            // 1. Если в баре 2 и более инструментов (или открыт избранный пресет "Мои инструменты")
            if (dockedTools.size >= 2 || (uiState.isDockFavorite && dockedTools.isNotEmpty())) {
                FloatingDockAnchorLayout(
                    closeButtonTopInBar = getMultiToolSideBarCloseOffset(dockedTools.size),
                    modifier = Modifier.fillMaxSize()
                ) {
                    MultiToolSideBar(
                        dockedTools = dockedTools,
                        activeTool = activeTool,
                        isFavorite = uiState.isDockFavorite,
                        onSelectTool = { tool -> onIntent(MainUiIntent.SelectDockTool(tool)) },
                        onUndoActiveToolPoint = { onIntent(MainUiIntent.UndoActiveToolPoint) },
                        onCloseClick = { onIntent(MainUiIntent.HandleDockCloseClick) },
                        onCloseAllLongClick = { onIntent(MainUiIntent.HandleDockCloseAllLongClick) },
                        onToggleFavorite = { onIntent(MainUiIntent.ToggleFavoriteToolPreset) },
                        onOpenHelp = { onIntent(MainUiIntent.OpenDockHelp) }
                    )
                }
            }
            // 2. Если в работе только 1 инструмент — показываем компактную одиночную кнопку
            else if (dockedTools.size == 1) {
                val singleTool = dockedTools.first()
                val pointsCount = when (singleTool) {
                    ToolType.RULER -> uiState.rulerPoints.size
                    ToolType.AREA -> uiState.areaPoints.size
                    ToolType.ANGLE -> uiState.anglePoints.size
                    ToolType.AZIMUTH -> if (uiState.azimuthOriginPoint != null) 1 else 0
                    ToolType.FAULT_LINE -> uiState.faultLinePoints.size
                    ToolType.DELTA_OFFSET -> if (uiState.deltaOffsetOriginPoint != null) 1 else 0
                    ToolType.RADIUS -> if (uiState.radiusCenterPoint != null) 1 else 0
                }
                FloatingDockAnchorLayout(
                    closeButtonTopInBar = getBindingSideControlCloseOffset(pointsCount),
                    modifier = Modifier.fillMaxSize()
                ) {
                    BindingSideControl(
                        pointsCount = pointsCount,
                        onClose = { onIntent(MainUiIntent.HandleDockCloseAllLongClick) },
                        onUndo = { onIntent(MainUiIntent.UndoActiveToolPoint) },
                        onHelp = when (singleTool) {
                            ToolType.DELTA_OFFSET -> { { onIntent(MainUiIntent.OpenDeltaOffsetHelp) } }
                            else -> null
                        }
                    )
                }
            } else if (isCalibrationMode) {
                val pointsCount = when {
                    uiState.isEntranceCavePickMode -> 0
                    uiState.isScaleBindingMode -> uiState.scaleBindingPoints.size
                    uiState.isNorthBindingMode -> uiState.northBindingPoints.size
                    else -> 0
                }
                FloatingDockAnchorLayout(
                    closeButtonTopInBar = getBindingSideControlCloseOffset(pointsCount),
                    modifier = Modifier.fillMaxSize()
                ) {
                    BindingSideControl(
                        pointsCount = pointsCount,
                        onUndo = {
                            when {
                                uiState.isScaleBindingMode -> onIntent(MainUiIntent.UndoScaleBindingPoint)
                                uiState.isNorthBindingMode -> onIntent(MainUiIntent.UndoNorthBindingPoint)
                            }
                        },
                        onClose = {
                            when {
                                uiState.isEntranceCavePickMode -> onIntent(MainUiIntent.CancelEntranceCavePick)
                                uiState.isScaleBindingMode -> onIntent(MainUiIntent.CancelScaleBinding)
                                uiState.isNorthBindingMode -> onIntent(MainUiIntent.CancelNorthBinding)
                            }
                        },
                        onHelp = null
                    )
                }
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
                isGridEnabled = uiState.settings.gridEnabled,
                dockedTools = uiState.dockedTools,
                favoriteTools = uiState.settings.favoriteToolPreset,
                mapFilter = uiState.settings.mapFilter,
                onToggleGrid = { onIntent(MainUiIntent.ToggleGrid) },
                onStartRulerClick = { onIntent(MainUiIntent.StartRulerMode) },
                onStartAreaMeasureClick = { onIntent(MainUiIntent.StartAreaMeasureMode) },
                onStartAngleMeasureClick = { onIntent(MainUiIntent.StartAngleMeasureMode) },
                onStartAzimuthClick = { onIntent(MainUiIntent.StartAzimuthMode) },
                onStartFaultLineClick = { onIntent(MainUiIntent.StartFaultLineMode) },
                onStartDeltaOffsetClick = { onIntent(MainUiIntent.StartDeltaOffsetMode) },
                onStartRadiusMeasureClick = { onIntent(MainUiIntent.StartRadiusMeasureMode) },
                onOpenMapFiltersClick = { onIntent(MainUiIntent.OpenMapFilterDialog) },
                onOpenFavoriteToolsPreset = { onIntent(MainUiIntent.OpenFavoriteToolsPreset) },
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
                onEntranceBindingClick = { onIntent(MainUiIntent.StartEntranceBinding) },
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
