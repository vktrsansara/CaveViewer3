package com.vktrsansara.app.caveviewer.presentation.main

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.presentation.map.components.AreaMeasureOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.AzimuthOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.BindingSideControl
import com.vktrsansara.app.caveviewer.presentation.map.components.CompassWidget
import com.vktrsansara.app.caveviewer.presentation.map.components.DeltaOffsetOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.FaultLineOverlay
import com.vktrsansara.app.caveviewer.domain.model.LayerLine
import com.vktrsansara.app.caveviewer.presentation.map.components.LineDetailsCard
import com.vktrsansara.app.caveviewer.presentation.map.components.LineDrawingOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.LineDrawingSideControl
import com.vktrsansara.app.caveviewer.presentation.map.components.LineLayersOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.MapCursorOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.MultiToolSideBar
import com.vktrsansara.app.caveviewer.presentation.map.components.NorthBindingOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.PointDetailsCard
import com.vktrsansara.app.caveviewer.presentation.map.components.PointEditorSideControl
import com.vktrsansara.app.caveviewer.presentation.map.components.PointLayersOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.RadiusMeasureOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.RulerOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.getBindingSideControlCloseOffset
import com.vktrsansara.app.caveviewer.presentation.map.components.getMultiToolSideBarCloseOffset
import com.vktrsansara.app.caveviewer.presentation.map.components.ScaleBarWidget
import com.vktrsansara.app.caveviewer.presentation.map.components.ScaleBindingOverlay
import com.vktrsansara.app.caveviewer.presentation.map.components.SnappingIndicatorOverlay
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.AddFieldDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.CreateLayerDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.CreateLineLayerDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.DeltaOffsetHelpDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.EditLineDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.EditPointDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.EntranceBindingHelpDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.EntranceNameDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.IntersectionConfirmDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.LayerManagerDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.LayerPropertiesDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.LayerSettingsDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.LineLayerManagerDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.LineLayerPropertiesDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.LineLayerSettingsDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.MapFilterDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.MapFilterHelpDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.MultiToolDockHelpDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.NorthBindingHelpDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.NorthBindingInputDialog
import androidx.compose.ui.platform.LocalDensity
import com.vktrsansara.app.caveviewer.domain.engine.SnapTarget
import com.vktrsansara.app.caveviewer.domain.engine.SnappingEngine
import com.vktrsansara.app.caveviewer.domain.model.PointPlacementMode
import com.vktrsansara.app.caveviewer.domain.model.LinePlacementMode
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.LineDrawingHelpDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.LinePlacementControlDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.PointEditorHelpDialog
import com.vktrsansara.app.caveviewer.presentation.projects.components.TileGenerationProgressDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.PointPlacementControlDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.ScaleBindingHelpDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.ScaleBindingInputDialog
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.SnappingSettingsDialog
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
import kotlin.math.pow
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

    val importProjectLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.handleIntent(MainUiIntent.ImportProject(uri))
        }
    }

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

    // Layer Manager Dialog
    if (uiState.isLayerManagerOpen) {
        LayerManagerDialog(
            layers = uiState.pointLayers,
            pointCounts = uiState.layerPointCounts,
            onCreateLayerClick = { viewModel.handleIntent(MainUiIntent.OpenCreateLayerDialog) },
            onStartEditingPoints = { layer ->
                viewModel.handleIntent(MainUiIntent.StartPointEditorMode(layer))
            },
            onToggleVisibility = { layerId, _ ->
                viewModel.handleIntent(MainUiIntent.ToggleLayerVisibility(layerId))
            },
            onEditStyle = { layer ->
                viewModel.handleIntent(MainUiIntent.OpenLayerSettings(layer))
            },
            onEditSchema = { layer ->
                viewModel.handleIntent(MainUiIntent.OpenLayerProperties(layer))
            },
            onDeleteLayer = { layerId ->
                viewModel.handleIntent(MainUiIntent.DeletePointLayer(layerId))
            },
            onDismiss = { viewModel.handleIntent(MainUiIntent.DismissLayerManager) }
        )
    }

    // Create Layer Dialog
    if (uiState.isCreateLayerOpen) {
        CreateLayerDialog(
            existingNames = uiState.pointLayers.map { it.name },
            onSave = { name ->
                viewModel.handleIntent(MainUiIntent.CreatePointLayer(name))
            },
            onCancel = {
                viewModel.handleIntent(MainUiIntent.DismissCreateLayerDialog)
            }
        )
    }

    // Line Layer Manager Dialog
    if (uiState.isLineLayerManagerOpen) {
        LineLayerManagerDialog(
            layers = uiState.lineLayers,
            lineCounts = uiState.layerLineCounts,
            onCreateLayerClick = { viewModel.handleIntent(MainUiIntent.OpenCreateLineLayerDialog) },
            onStartEditingLines = { layer ->
                viewModel.handleIntent(MainUiIntent.StartLineDrawingMode(layer))
            },
            onToggleVisibility = { layerId, _ ->
                viewModel.handleIntent(MainUiIntent.ToggleLineLayerVisibility(layerId))
            },
            onEditStyle = { layer ->
                viewModel.handleIntent(MainUiIntent.OpenLineLayerSettings(layer))
            },
            onEditSchema = { layer ->
                viewModel.handleIntent(MainUiIntent.OpenLineLayerProperties(layer))
            },
            onDeleteLayer = { layerId ->
                viewModel.handleIntent(MainUiIntent.DeleteLineLayer(layerId))
            },
            onDismiss = { viewModel.handleIntent(MainUiIntent.DismissLineLayerManager) }
        )
    }

    // Create Line Layer Dialog
    if (uiState.isCreateLineLayerOpen) {
        CreateLineLayerDialog(
            existingNames = uiState.lineLayers.map { it.name },
            onSave = { name ->
                viewModel.handleIntent(MainUiIntent.CreateLineLayer(name))
            },
            onCancel = {
                viewModel.handleIntent(MainUiIntent.DismissCreateLineLayerDialog)
            }
        )
    }

    // Line Layer Settings Dialog (Line Width, Heatmap, Color, Environment)
    if (uiState.selectedLineLayerForSettings != null) {
        val currentSelected = uiState.selectedLineLayerForSettings!!
        LineLayerSettingsDialog(
            layer = currentSelected,
            existingNames = uiState.lineLayers.filter { it.id != currentSelected.id }.map { it.name },
            paletteMode = uiState.settings.colorPaletteMode,
            onSave = { updated ->
                viewModel.handleIntent(MainUiIntent.SaveLineLayerSettings(updated))
            },
            onDismiss = {
                viewModel.handleIntent(MainUiIntent.DismissLineLayerSettings)
            }
        )
    }

    // Line Layer Properties Dialog (Custom Fields Schema)
    if (uiState.selectedLineLayerForProperties != null) {
        val currentSelected = uiState.selectedLineLayerForProperties!!
        LineLayerPropertiesDialog(
            layer = currentSelected,
            onAddCustomFieldClick = {
                viewModel.handleIntent(MainUiIntent.OpenAddLineFieldDialog)
            },
            onEditFieldClick = { field ->
                viewModel.handleIntent(MainUiIntent.OpenEditLineFieldDialog(field))
            },
            onDeleteField = { key ->
                viewModel.handleIntent(
                    MainUiIntent.DeleteLineLayerField(
                        layerId = currentSelected.id,
                        fieldKey = key
                    )
                )
            },
            onDismiss = {
                viewModel.handleIntent(MainUiIntent.DismissLineLayerProperties)
            }
        )
    }

    // Add / Edit Line Field Dialog
    if (uiState.isAddLineFieldDialogOpen && uiState.selectedLineLayerForProperties != null) {
        val targetLayer = uiState.selectedLineLayerForProperties!!
        AddFieldDialog(
            initialField = uiState.editingLineFieldDefinition,
            onSave = { field ->
                if (uiState.editingLineFieldDefinition != null) {
                    viewModel.handleIntent(
                        MainUiIntent.UpdateLineLayerField(
                            layerId = targetLayer.id,
                            field = field
                        )
                    )
                } else {
                    viewModel.handleIntent(
                        MainUiIntent.AddLineLayerField(
                            layerId = targetLayer.id,
                            field = field
                        )
                    )
                }
            },
            onCancel = {
                viewModel.handleIntent(MainUiIntent.DismissAddLineFieldDialog)
            }
        )
    }

    // Edit / Save Line Dialog
    if (uiState.isEditLineDialogOpen && uiState.editingLine != null) {
        val targetLayer = uiState.lineLayers.find { it.id == uiState.editingLine!!.layerId }
            ?: uiState.editingLineLayer
        if (targetLayer != null) {
            EditLineDialog(
                line = uiState.editingLine!!,
                layer = targetLayer,
                ppm = uiState.activeProjectMetadata?.pixelsPerMeter ?: 0.0,
                paletteMode = uiState.settings.colorPaletteMode,
                onSave = { updatedLine ->
                    viewModel.handleIntent(MainUiIntent.SaveLayerLine(updatedLine))
                },
                onDismiss = {
                    viewModel.handleIntent(MainUiIntent.DismissEditLineDialog)
                }
            )
        }
    }

    // Layer Settings Dialog (Marker Shape, Color, Size, Labels)
    if (uiState.selectedLayerForSettings != null) {
        val currentSelected = uiState.selectedLayerForSettings!!
        LayerSettingsDialog(
            layer = currentSelected,
            existingNames = uiState.pointLayers.filter { it.id != currentSelected.id }.map { it.name },
            paletteMode = uiState.settings.colorPaletteMode,
            onSave = { updated ->
                viewModel.handleIntent(MainUiIntent.SaveLayerSettings(updated))
            },
            onDismiss = {
                viewModel.handleIntent(MainUiIntent.DismissLayerSettings)
            }
        )
    }

    // Layer Properties Dialog (Custom Fields Schema)
    if (uiState.selectedLayerForProperties != null) {
        LayerPropertiesDialog(
            layer = uiState.selectedLayerForProperties!!,
            onAddCustomFieldClick = {
                viewModel.handleIntent(MainUiIntent.OpenAddFieldDialog)
            },
            onEditFieldClick = { field ->
                viewModel.handleIntent(MainUiIntent.OpenEditFieldDialog(field))
            },
            onDeleteField = { key ->
                viewModel.handleIntent(
                    MainUiIntent.DeleteLayerField(
                        layerId = uiState.selectedLayerForProperties!!.id,
                        fieldKey = key
                    )
                )
            },
            onDismiss = {
                viewModel.handleIntent(MainUiIntent.DismissLayerProperties)
            }
        )
    }

    // Add / Edit Field Dialog (New or existing custom field definition)
    if (uiState.isAddFieldDialogOpen && uiState.selectedLayerForProperties != null) {
        AddFieldDialog(
            initialField = uiState.editingFieldDefinition,
            onSave = { field ->
                if (uiState.editingFieldDefinition != null) {
                    viewModel.handleIntent(
                        MainUiIntent.UpdateLayerField(
                            layerId = uiState.selectedLayerForProperties!!.id,
                            field = field
                        )
                    )
                } else {
                    viewModel.handleIntent(
                        MainUiIntent.AddLayerField(
                            layerId = uiState.selectedLayerForProperties!!.id,
                            field = field
                        )
                    )
                }
            },
            onCancel = {
                viewModel.handleIntent(MainUiIntent.DismissAddFieldDialog)
            }
        )
    }

    // Edit / Create Point Dialog
    if (uiState.isEditPointDialogOpen && uiState.editingPoint != null) {
        val targetLayer = uiState.pointLayers.find { it.id == uiState.editingPoint!!.layerId }
            ?: uiState.editingPointLayer
        if (targetLayer != null) {
            EditPointDialog(
                point = uiState.editingPoint!!,
                layer = targetLayer,
                paletteMode = uiState.settings.colorPaletteMode,
                onSave = { savedPoint ->
                    viewModel.handleIntent(MainUiIntent.SaveLayerPoint(savedPoint))
                },
                onDismiss = {
                    viewModel.handleIntent(MainUiIntent.DismissEditPointDialog)
                }
            )
        }
    }

    // Point Placement Control Dialog («Управление»)
    if (uiState.isPointPlacementControlOpen) {
        PointPlacementControlDialog(
            currentMode = uiState.settings.pointPlacementMode,
            onApply = { newMode ->
                viewModel.handleIntent(MainUiIntent.SavePointPlacementMode(newMode))
            },
            onDismiss = {
                viewModel.handleIntent(MainUiIntent.DismissPointPlacementControl)
            }
        )
    }

    // Point Editor Help Dialog («Справка: Редактор точек»)
    if (uiState.isPointEditorHelpOpen) {
        PointEditorHelpDialog(
            onDismiss = {
                viewModel.handleIntent(MainUiIntent.DismissPointEditorHelp)
            }
        )
    }

    // Line Placement Control Dialog («Управление»)
    if (uiState.isLinePlacementControlOpen) {
        LinePlacementControlDialog(
            currentMode = uiState.settings.linePlacementMode,
            onApply = { newMode ->
                viewModel.handleIntent(MainUiIntent.SaveLinePlacementMode(newMode))
            },
            onDismiss = {
                viewModel.handleIntent(MainUiIntent.DismissLinePlacementControl)
            }
        )
    }

    // Line Drawing Help Dialog («Справка: Рисование линий»)
    if (uiState.isLineDrawingHelpOpen) {
        LineDrawingHelpDialog(
            onDismiss = {
                viewModel.handleIntent(MainUiIntent.DismissLineDrawingHelp)
            }
        )
    }

    // Snapping Settings Dialog («Настройки привязки»)
    if (uiState.isSnappingSettingsDialogOpen) {
        SnappingSettingsDialog(
            currentSettings = uiState.settings.snappingSettings,
            onApply = { newSettings ->
                viewModel.handleIntent(MainUiIntent.SaveSnappingSettings(newSettings))
            },
            onDismiss = {
                viewModel.handleIntent(MainUiIntent.DismissSnappingSettingsDialog)
            }
        )
    }

    // Intersection Confirmation Dialog («Связывание перекрестка»)
    if (uiState.pendingIntersection != null) {
        val pending = uiState.pendingIntersection!!
        IntersectionConfirmDialog(
            lineName = pending.intersectedLine?.name ?: "",
            onConfirm = {
                viewModel.handleIntent(MainUiIntent.ConfirmIntersection(createNode = true))
            },
            onDecline = {
                viewModel.handleIntent(MainUiIntent.ConfirmIntersection(createNode = false))
            }
        )
    }

    // Tile / Project Import Progress Dialog
    if (uiState.isProjectSaving && uiState.currentScreen != AppScreen.CREATE_RASTER_PROJECT) {
        TileGenerationProgressDialog(
            projectName = uiState.projectSavingName,
            progressFraction = uiState.projectSavingProgress,
            statusText = uiState.projectSavingStatusText,
            onCancel = { viewModel.handleIntent(MainUiIntent.CancelProjectCreation) }
        )
    }

    // Handle system back gesture
    BackHandler(
        enabled = uiState.currentScreen != AppScreen.MAIN ||
                uiState.selectedPointForDetails != null ||
                uiState.selectedLineForDetails != null ||
                uiState.editingPointLayer != null ||
                uiState.editingLineLayer != null ||
                uiState.isScaleBindingMode ||
                uiState.isNorthBindingMode ||
                uiState.isEntranceCavePickMode ||
                uiState.isOsmEntranceBindingMode ||
                uiState.dockedTools.isNotEmpty()
    ) {
        when {
            uiState.selectedPointForDetails != null -> viewModel.handleIntent(MainUiIntent.DismissPointDetails)
            uiState.selectedLineForDetails != null -> viewModel.handleIntent(MainUiIntent.DismissLineDetails)
            uiState.editingPointLayer != null -> viewModel.handleIntent(MainUiIntent.ExitPointEditorMode)
            uiState.editingLineLayer != null -> viewModel.handleIntent(MainUiIntent.ExitLineDrawingMode)
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
                    onIntent = viewModel::handleIntent,
                    onImportProject = {
                        importProjectLauncher.launch(
                            arrayOf(
                                "application/zip",
                                "application/x-zip-compressed",
                                "application/octet-stream",
                                "*/*"
                            )
                        )
                    }
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
                    onImportProject = {
                        importProjectLauncher.launch(
                            arrayOf(
                                "application/zip",
                                "application/x-zip-compressed",
                                "application/octet-stream",
                                "*/*"
                            )
                        )
                    },
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
    onImportProject: () -> Unit = {},
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
    var mapBearing by remember { mutableDoubleStateOf(initialPos?.bearing ?: 0.0) }
    var currentZoom by remember { mutableDoubleStateOf(initialPos?.zoom ?: 0.0) }
    var currentTargetLat by remember { mutableDoubleStateOf(initialPos?.targetLat ?: 0.0) }
    var currentTargetLon by remember { mutableDoubleStateOf(initialPos?.targetLon ?: 0.0) }
    var resetBearingAction by remember { mutableStateOf<((Double) -> Unit)?>(null) }
    var moveCameraAction by remember { mutableStateOf<((LatLng, Double?) -> Unit)?>(null) }
    var getMapCenter by remember { mutableStateOf<(() -> LatLng)?>(null) }
    var mapMetadata by remember(uiState.activeProjectMetadata) { mutableStateOf(uiState.activeProjectMetadata) }
    var bindingScreenPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var projector by remember { mutableStateOf<((LatLng) -> Offset)?>(null) }
    val density = LocalDensity.current

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
    val lineDrawingScreenPoints = remember(uiState.drawingLinePoints, projector, currentZoom, currentTargetLat, currentTargetLon, mapBearing) {
        uiState.drawingLinePoints.mapNotNull { pt -> projector?.invoke(pt.latLng) }
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
                onGetMapCenterReady = { getter ->
                    getMapCenter = getter
                },
                onMapClick = { clickedLatLng ->
                    var pointHit = false
                    val curMeta = mapMetadata ?: uiState.activeProjectMetadata
                    val isMeasuringTools = isCalibrationMode || isAnyToolActive
                    if (!isMeasuringTools && curMeta != null && projector != null && uiState.allVisiblePoints.isNotEmpty()) {
                        try {
                            val clickedScreen = projector!!.invoke(clickedLatLng)
                            val hitRadiusPx = 28 * density.density
                            val hitRadiusSq = hitRadiusPx * hitRadiusPx
                            val layerMap = uiState.pointLayers.associateBy { it.id }

                            val hit = uiState.allVisiblePoints.lastOrNull { point ->
                                val layer = layerMap[point.layerId]
                                if (layer != null && layer.isVisible) {
                                    val pointLatLng = CaveMapBounds.imagePixelsToLatLng(
                                        pixelX = point.x,
                                        pixelY = point.y,
                                        imageWidth = curMeta.imageWidth,
                                        imageHeight = curMeta.imageHeight,
                                        maxZoom = curMeta.zoomMax
                                    )
                                    val pointScreen = projector!!.invoke(pointLatLng)
                                    val dx = clickedScreen.x - pointScreen.x
                                    val dy = clickedScreen.y - pointScreen.y
                                    (dx * dx + dy * dy) <= hitRadiusSq
                                } else false
                            }

                            if (hit != null) {
                                onIntent(MainUiIntent.SelectPoint(hit))
                                pointHit = true
                            }
                        } catch (_: Exception) {
                            // Safe fallback
                        }
                    }

                    // Line Hit-Test (Tap on any line segment)
                    var lineHit = false
                    if (!pointHit && !isMeasuringTools && curMeta != null && projector != null && uiState.editingLineLayer == null && uiState.editingPointLayer == null && uiState.allVisibleLines.isNotEmpty()) {
                        try {
                            val clickedScreen = projector!!.invoke(clickedLatLng)
                            val lineHitThresholdPx = 20 * density.density
                            val lineHitThresholdSq = lineHitThresholdPx * lineHitThresholdPx
                            val lineLayerMap = uiState.lineLayers.associateBy { it.id }

                            var closestLine: LayerLine? = null
                            var minDistanceSq = Float.MAX_VALUE

                            for (line in uiState.allVisibleLines) {
                                val layer = lineLayerMap[line.layerId]
                                if (layer != null && layer.isVisible && line.points.size >= 2) {
                                    val screenPts = line.points.map { pt ->
                                        val latLng = CaveMapBounds.imagePixelsToLatLng(
                                            pixelX = pt.first,
                                            pixelY = pt.second,
                                            imageWidth = curMeta.imageWidth,
                                            imageHeight = curMeta.imageHeight,
                                            maxZoom = curMeta.zoomMax
                                        )
                                        projector!!.invoke(latLng)
                                    }

                                    for (i in 0 until screenPts.size - 1) {
                                        val p1 = screenPts[i]
                                        val p2 = screenPts[i + 1]
                                        val distSq = distanceToSegmentSq(clickedScreen.x, clickedScreen.y, p1.x, p1.y, p2.x, p2.y)
                                        if (distSq <= lineHitThresholdSq && distSq < minDistanceSq) {
                                            minDistanceSq = distSq
                                            closestLine = line
                                        }
                                    }
                                }
                            }

                            if (closestLine != null) {
                                onIntent(MainUiIntent.SelectLine(closestLine))
                                lineHit = true
                                pointHit = true
                            }
                        } catch (_: Exception) {
                            // Safe fallback
                        }
                    }

                    if (!pointHit && !lineHit) {
                        if (uiState.selectedPointForDetails != null) {
                            onIntent(MainUiIntent.DismissPointDetails)
                        } else if (uiState.selectedLineForDetails != null) {
                            onIntent(MainUiIntent.DismissLineDetails)
                        } else if (uiState.editingLineLayer != null && curMeta != null) {
                            when (uiState.settings.linePlacementMode) {
                                LinePlacementMode.CURSOR_BUTTON_AND_TAP,
                                LinePlacementMode.CURSOR_TAP_ONLY -> {
                                    val liveCenterLatLng = getMapCenter?.invoke() ?: LatLng(currentTargetLat, currentTargetLon)
                                    val liveCenterPx = CaveMapBounds.latLngToImagePixels(
                                        latLng = liveCenterLatLng,
                                        imageHeight = curMeta.imageHeight,
                                        imageWidth = curMeta.imageWidth,
                                        maxZoom = curMeta.zoomMax
                                    )
                                    val effectiveTarget = if (uiState.settings.snappingSettings.isEnabled && projector != null) {
                                        val cursorScreenOffset = projector?.invoke(liveCenterLatLng)
                                        val snap = if (cursorScreenOffset != null) {
                                            val snapRadiusScreenPx = with(density) {
                                                uiState.settings.snappingSettings.snapRadiusDp.dp.toPx()
                                            }
                                            SnappingEngine.findSnapTarget(
                                                cursorScreenOffset = cursorScreenOffset,
                                                visibleLines = uiState.allVisibleLines,
                                                visiblePoints = uiState.allVisiblePoints,
                                                imageWidth = curMeta.imageWidth,
                                                imageHeight = curMeta.imageHeight,
                                                zoomMax = curMeta.zoomMax,
                                                projector = projector!!,
                                                settings = uiState.settings.snappingSettings,
                                                snapRadiusScreenPx = snapRadiusScreenPx
                                            )
                                        } else null

                                        if (snap != null) {
                                            val snapLatLng = CaveMapBounds.imagePixelsToLatLng(
                                                pixelX = snap.px.first,
                                                pixelY = snap.px.second,
                                                imageWidth = curMeta.imageWidth,
                                                imageHeight = curMeta.imageHeight,
                                                maxZoom = curMeta.zoomMax
                                            )
                                            Pair(snapLatLng, snap.px)
                                        } else {
                                            Pair(liveCenterLatLng, liveCenterPx)
                                        }
                                    } else {
                                        Pair(liveCenterLatLng, liveCenterPx)
                                    }
                                    onIntent(MainUiIntent.AddDrawingLineVertex(effectiveTarget.first, effectiveTarget.second))
                                    pointHit = true
                                }
                                LinePlacementMode.FREE_TAP -> {
                                    val clickedPx = CaveMapBounds.latLngToImagePixels(
                                        latLng = clickedLatLng,
                                        imageWidth = curMeta.imageWidth,
                                        imageHeight = curMeta.imageHeight,
                                        maxZoom = curMeta.zoomMax
                                    )
                                    val effectiveTarget = if (uiState.settings.snappingSettings.isEnabled && projector != null) {
                                        val clickedScreenOffset = projector?.invoke(clickedLatLng)
                                        val snap = if (clickedScreenOffset != null) {
                                            val snapRadiusScreenPx = with(density) {
                                                SnappingEngine.TOUCH_SNAP_RADIUS_DP.dp.toPx()
                                            }
                                            SnappingEngine.findSnapTarget(
                                                cursorScreenOffset = clickedScreenOffset,
                                                visibleLines = uiState.allVisibleLines,
                                                visiblePoints = uiState.allVisiblePoints,
                                                imageWidth = curMeta.imageWidth,
                                                imageHeight = curMeta.imageHeight,
                                                zoomMax = curMeta.zoomMax,
                                                projector = projector!!,
                                                settings = uiState.settings.snappingSettings,
                                                snapRadiusScreenPx = snapRadiusScreenPx
                                            )
                                        } else null

                                        if (snap != null) {
                                            val snapLatLng = CaveMapBounds.imagePixelsToLatLng(
                                                pixelX = snap.px.first,
                                                pixelY = snap.px.second,
                                                imageWidth = curMeta.imageWidth,
                                                imageHeight = curMeta.imageHeight,
                                                maxZoom = curMeta.zoomMax
                                            )
                                            Pair(snapLatLng, snap.px)
                                        } else {
                                            Pair(clickedLatLng, clickedPx)
                                        }
                                    } else {
                                        Pair(clickedLatLng, clickedPx)
                                    }
                                    onIntent(MainUiIntent.AddDrawingLineVertex(effectiveTarget.first, effectiveTarget.second))
                                    pointHit = true
                                }
                                LinePlacementMode.CURSOR_BUTTON_ONLY -> {
                                    // Do nothing on tap (only via [+] button)
                                }
                            }
                        } else if (uiState.editingPointLayer != null && curMeta != null) {
                            when (uiState.settings.pointPlacementMode) {
                                PointPlacementMode.CURSOR_BUTTON_AND_TAP,
                                PointPlacementMode.CURSOR_TAP_ONLY -> {
                                    val liveCenterLatLng = getMapCenter?.invoke() ?: LatLng(currentTargetLat, currentTargetLon)
                                    val liveCenterPx = CaveMapBounds.latLngToImagePixels(
                                        latLng = liveCenterLatLng,
                                        imageWidth = curMeta.imageWidth,
                                        imageHeight = curMeta.imageHeight,
                                        maxZoom = curMeta.zoomMax
                                    )
                                    val effectivePx = if (uiState.settings.snappingSettings.isEnabled && projector != null) {
                                        val cursorScreenOffset = projector?.invoke(liveCenterLatLng)
                                        val snap = if (cursorScreenOffset != null) {
                                            val snapRadiusScreenPx = with(density) {
                                                uiState.settings.snappingSettings.snapRadiusDp.dp.toPx()
                                            }
                                            SnappingEngine.findSnapTarget(
                                                cursorScreenOffset = cursorScreenOffset,
                                                visibleLines = uiState.allVisibleLines,
                                                visiblePoints = uiState.allVisiblePoints,
                                                imageWidth = curMeta.imageWidth,
                                                imageHeight = curMeta.imageHeight,
                                                zoomMax = curMeta.zoomMax,
                                                projector = projector!!,
                                                settings = uiState.settings.snappingSettings,
                                                snapRadiusScreenPx = snapRadiusScreenPx,
                                                forPointCreation = true
                                            )
                                        } else null
                                        snap?.px ?: liveCenterPx
                                    } else {
                                        liveCenterPx
                                    }
                                    onIntent(MainUiIntent.OpenCreatePointDialog(effectivePx))
                                    pointHit = true
                                }
                                PointPlacementMode.FREE_TAP -> {
                                    val clickedPx = CaveMapBounds.latLngToImagePixels(
                                        latLng = clickedLatLng,
                                        imageWidth = curMeta.imageWidth,
                                        imageHeight = curMeta.imageHeight,
                                        maxZoom = curMeta.zoomMax
                                    )
                                    val effectivePx = if (uiState.settings.snappingSettings.isEnabled && projector != null) {
                                        val clickedScreenOffset = projector?.invoke(clickedLatLng)
                                        val snap = if (clickedScreenOffset != null) {
                                            val snapRadiusScreenPx = with(density) {
                                                SnappingEngine.TOUCH_SNAP_RADIUS_DP.dp.toPx()
                                            }
                                            SnappingEngine.findSnapTarget(
                                                cursorScreenOffset = clickedScreenOffset,
                                                visibleLines = uiState.allVisibleLines,
                                                visiblePoints = uiState.allVisiblePoints,
                                                imageWidth = curMeta.imageWidth,
                                                imageHeight = curMeta.imageHeight,
                                                zoomMax = curMeta.zoomMax,
                                                projector = projector!!,
                                                settings = uiState.settings.snappingSettings,
                                                snapRadiusScreenPx = snapRadiusScreenPx,
                                                forPointCreation = true
                                            )
                                        } else null
                                        snap?.px ?: clickedPx
                                    } else {
                                        clickedPx
                                    }
                                    onIntent(MainUiIntent.OpenCreatePointDialog(effectivePx))
                                    pointHit = true
                                }
                                PointPlacementMode.CURSOR_BUTTON_ONLY -> {
                                    // Do nothing on tap
                                }
                            }
                        }
                    }

                    pointHit
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
                onMoveCameraReady = { action ->
                    moveCameraAction = action
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

            val isFreeTapLineMode = uiState.editingLineLayer != null && uiState.settings.linePlacementMode == LinePlacementMode.FREE_TAP
            val isFreeTapPointMode = uiState.editingPointLayer != null && uiState.settings.pointPlacementMode == PointPlacementMode.FREE_TAP
            val isFreeTapActive = isFreeTapLineMode || isFreeTapPointMode
            val isSnappingActiveMode = uiState.editingLineLayer != null || uiState.editingPointLayer != null

            val snapTarget = if (meta != null && projector != null && uiState.settings.snappingSettings.isEnabled && isSnappingActiveMode && !isFreeTapActive) {
                val cursorScreenOffset = projector?.invoke(LatLng(currentTargetLat, currentTargetLon))
                if (cursorScreenOffset != null) {
                    val snapRadiusScreenPx = with(density) {
                        uiState.settings.snappingSettings.snapRadiusDp.dp.toPx()
                    }
                    SnappingEngine.findSnapTarget(
                        cursorScreenOffset = cursorScreenOffset,
                        visibleLines = uiState.allVisibleLines,
                        visiblePoints = uiState.allVisiblePoints,
                        imageWidth = meta.imageWidth,
                        imageHeight = meta.imageHeight,
                        zoomMax = meta.zoomMax,
                        projector = projector!!,
                        settings = uiState.settings.snappingSettings,
                        snapRadiusScreenPx = snapRadiusScreenPx,
                        forPointCreation = (uiState.editingPointLayer != null)
                    )
                } else null
            } else null

            val snapScreenPoint = snapTarget?.screenOffset

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

            // Line Layers Vector Overlay (Outer Halo + Core Stroke + Selection Glow)
            if (meta != null && uiState.lineLayers.isNotEmpty() && uiState.allVisibleLines.isNotEmpty()) {
                LineLayersOverlay(
                    lineLayers = uiState.lineLayers,
                    allLines = uiState.allVisibleLines,
                    selectedLineId = uiState.selectedLineForDetails?.id,
                    imageWidth = meta.imageWidth,
                    imageHeight = meta.imageHeight,
                    zoomMax = meta.zoomMax,
                    projector = projector,
                    currentTargetLat = currentTargetLat,
                    currentTargetLon = currentTargetLon,
                    currentZoom = currentZoom,
                    mapBearing = mapBearing,
                    modifier = Modifier.fillMaxSize().clipToBounds()
                )
            }

            // Point Layers Vector Overlay (Markers & Labels)
            if (meta != null && uiState.pointLayers.isNotEmpty() && uiState.allVisiblePoints.isNotEmpty()) {
                PointLayersOverlay(
                    pointLayers = uiState.pointLayers,
                    allPoints = uiState.allVisiblePoints,
                    imageWidth = meta.imageWidth,
                    imageHeight = meta.imageHeight,
                    zoomMax = meta.zoomMax,
                    projector = projector,
                    currentTargetLat = currentTargetLat,
                    currentTargetLon = currentTargetLon,
                    currentZoom = currentZoom,
                    mapBearing = mapBearing,
                    modifier = Modifier.fillMaxSize().clipToBounds()
                )
            }

            // Line Drawing Overlay (Real-time vertices, live dashed ray, and magnetic snapping)
            if (uiState.editingLineLayer != null) {
                LineDrawingOverlay(
                    layer = uiState.editingLineLayer!!,
                    points = uiState.drawingLinePoints,
                    screenPoints = lineDrawingScreenPoints,
                    currentCenterPx = centerPx,
                    ppm = meta?.pixelsPerMeter ?: 0.0,
                    placementMode = uiState.settings.linePlacementMode,
                    snapTarget = snapTarget,
                    snapScreenPoint = snapScreenPoint,
                    modifier = Modifier.fillMaxSize().clipToBounds()
                )
            }

            // Point Editor Snapping Target Overlay (shows pink QGIS target at cursor)
            if (uiState.editingPointLayer != null && snapTarget != null && snapScreenPoint != null) {
                SnappingIndicatorOverlay(
                    snapTarget = snapTarget,
                    snapScreenPoint = snapScreenPoint,
                    modifier = Modifier.fillMaxSize().clipToBounds()
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

            // Point Editor Top Banner
            if (uiState.editingPointLayer != null) {
                val bannerText = buildString {
                    append("Редактор точек: ${uiState.editingPointLayer!!.name}")
                    if (snapTarget != null) {
                        append(" • [🧲 ${snapTarget.title}]")
                    } else if (uiState.settings.pointPlacementMode == PointPlacementMode.FREE_TAP) {
                        append(" • Коснитесь экрана для установки точки")
                    } else {
                        append(" • Наведите курсор на объект и нажмите [+]")
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp, start = 20.dp, end = 20.dp)
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.bgCard.copy(alpha = 0.95f))
                        .border(1.dp, if (snapTarget != null) Color(0xFFF43F5E) else AccentSkyBlue, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = bannerText,
                        color = AppColors.textPrimary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Central Cursor Overlay (Strictly centered on screen; always visible in calibration or when active tool is open or in editors)
            MapCursorOverlay(
                cursorShow = isCalibrationMode || isAnyToolActive || uiState.editingPointLayer != null || uiState.editingLineLayer != null || uiState.settings.cursorShow,
                cursorType = uiState.settings.cursorType,
                cursorColor = uiState.settings.cursorColor
            )

            // 1. Compass Widget (Top-Start: top = 15.dp, start = 15.dp)
            if (uiState.settings.showCompass && meta != null && !isCalibrationMode && !isAnyToolActive && uiState.editingPointLayer == null && uiState.editingLineLayer == null) {
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
            if (uiState.settings.showScaleBar && meta != null && meta.pixelsPerMeter > 0.0 && meta.scaleMeters > 0.0 && !isCalibrationMode && !isAnyToolActive && uiState.editingPointLayer == null && uiState.editingLineLayer == null) {
                ScaleBarWidget(
                    pixelsPerMeter = meta.pixelsPerMeter,
                    zoomMax = meta.zoomMax,
                    currentZoom = currentZoom,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 15.dp, end = 15.dp)
                )
            }

            // 3. Multi-Tool SideBar (Floating dock on the right) or Single Tool BindingSideControl or Calibration Side Control or Point Editor Side Control
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
            } else if (uiState.editingPointLayer != null) {
                val layer = uiState.editingPointLayer!!
                FloatingDockAnchorLayout(
                    closeButtonTopInBar = 46.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    PointEditorSideControl(
                        layer = layer,
                        placementMode = uiState.settings.pointPlacementMode,
                        onAddPoint = {
                            val curMeta = mapMetadata ?: uiState.activeProjectMetadata
                            if (curMeta != null) {
                                val liveCenterLatLng = getMapCenter?.invoke() ?: LatLng(currentTargetLat, currentTargetLon)
                                val liveCenterPx = CaveMapBounds.latLngToImagePixels(
                                    latLng = liveCenterLatLng,
                                    imageWidth = curMeta.imageWidth,
                                    imageHeight = curMeta.imageHeight,
                                    maxZoom = curMeta.zoomMax
                                )
                                val effectivePx = if (uiState.settings.snappingSettings.isEnabled && projector != null) {
                                    val cursorScreenOffset = projector?.invoke(liveCenterLatLng)
                                    val snap = if (cursorScreenOffset != null) {
                                        val snapRadiusScreenPx = with(density) {
                                            uiState.settings.snappingSettings.snapRadiusDp.dp.toPx()
                                        }
                                        SnappingEngine.findSnapTarget(
                                            cursorScreenOffset = cursorScreenOffset,
                                            visibleLines = uiState.allVisibleLines,
                                            visiblePoints = uiState.allVisiblePoints,
                                            imageWidth = curMeta.imageWidth,
                                            imageHeight = curMeta.imageHeight,
                                            zoomMax = curMeta.zoomMax,
                                            projector = projector!!,
                                            settings = uiState.settings.snappingSettings,
                                            snapRadiusScreenPx = snapRadiusScreenPx,
                                            forPointCreation = true
                                        )
                                    } else null
                                    snap?.px ?: liveCenterPx
                                } else {
                                    liveCenterPx
                                }
                                onIntent(MainUiIntent.OpenCreatePointDialog(effectivePx))
                            }
                        },
                        onClose = {
                            onIntent(MainUiIntent.ExitPointEditorMode)
                        },
                        onSettingsClick = {
                            onIntent(MainUiIntent.OpenPointPlacementControl)
                        },
                        onHelpClick = {
                            onIntent(MainUiIntent.OpenPointEditorHelp)
                        }
                    )
                }
            } else if (uiState.editingLineLayer != null) {
                val layer = uiState.editingLineLayer!!
                FloatingDockAnchorLayout(
                    closeButtonTopInBar = 114.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LineDrawingSideControl(
                        layer = layer,
                        placementMode = uiState.settings.linePlacementMode,
                        pointsCount = uiState.drawingLinePoints.size,
                        onAddVertex = {
                            val curMeta = mapMetadata ?: uiState.activeProjectMetadata
                            if (curMeta != null) {
                                val rawCenterLatLng = getMapCenter?.invoke() ?: LatLng(currentTargetLat, currentTargetLon)
                                val rawCenterPx = CaveMapBounds.latLngToImagePixels(
                                    latLng = rawCenterLatLng,
                                    imageWidth = curMeta.imageWidth,
                                    imageHeight = curMeta.imageHeight,
                                    maxZoom = curMeta.zoomMax
                                )
                                val effectiveTarget = if (uiState.settings.snappingSettings.isEnabled && projector != null) {
                                    val cursorScreenOffset = projector?.invoke(rawCenterLatLng)
                                    val snap = if (cursorScreenOffset != null) {
                                        val snapRadiusScreenPx = with(density) {
                                            uiState.settings.snappingSettings.snapRadiusDp.dp.toPx()
                                        }
                                        SnappingEngine.findSnapTarget(
                                            cursorScreenOffset = cursorScreenOffset,
                                            visibleLines = uiState.allVisibleLines,
                                            visiblePoints = uiState.allVisiblePoints,
                                            imageWidth = curMeta.imageWidth,
                                            imageHeight = curMeta.imageHeight,
                                            zoomMax = curMeta.zoomMax,
                                            projector = projector!!,
                                            settings = uiState.settings.snappingSettings,
                                            snapRadiusScreenPx = snapRadiusScreenPx
                                        )
                                    } else null

                                    if (snap != null) {
                                        val snapLatLng = CaveMapBounds.imagePixelsToLatLng(
                                            pixelX = snap.px.first,
                                            pixelY = snap.px.second,
                                            imageWidth = curMeta.imageWidth,
                                            imageHeight = curMeta.imageHeight,
                                            maxZoom = curMeta.zoomMax
                                        )
                                        Pair(snapLatLng, snap.px)
                                    } else {
                                        Pair(rawCenterLatLng, rawCenterPx)
                                    }
                                } else {
                                    Pair(rawCenterLatLng, rawCenterPx)
                                }
                                onIntent(MainUiIntent.AddDrawingLineVertex(effectiveTarget.first, effectiveTarget.second))
                            }
                        },
                        onUndo = {
                            onIntent(MainUiIntent.UndoDrawingLineVertex)
                        },
                        onComplete = {
                            onIntent(MainUiIntent.CompleteLineDrawing)
                        },
                        onClose = {
                            onIntent(MainUiIntent.ExitLineDrawingMode)
                        },
                        onSettingsClick = {
                            onIntent(MainUiIntent.OpenLinePlacementControl)
                        },
                        onHelpClick = {
                            onIntent(MainUiIntent.OpenLineDrawingHelp)
                        }
                    )
                }
            }

            // Selected Point Details Card
            if (uiState.selectedPointForDetails != null && !isCalibrationMode) {
                val selPoint = uiState.selectedPointForDetails!!
                val parentLayer = uiState.pointLayers.find { it.id == selPoint.layerId }
                if (parentLayer != null) {
                    val curMeta = mapMetadata ?: uiState.activeProjectMetadata
                    val isSimpleCrs = curMeta?.crs.equals("Simple", ignoreCase = true) || curMeta?.crs.isNullOrBlank()
                    PointDetailsCard(
                        point = selPoint,
                        layer = parentLayer,
                        isSimpleCrs = isSimpleCrs,
                        onEditClick = {
                            onIntent(MainUiIntent.OpenEditPointDialog(selPoint))
                        },
                        onCenterMapClick = {
                            if (curMeta != null) {
                                val targetLatLng = CaveMapBounds.imagePixelsToLatLng(
                                    pixelX = selPoint.x,
                                    pixelY = selPoint.y,
                                    imageWidth = curMeta.imageWidth,
                                    imageHeight = curMeta.imageHeight,
                                    maxZoom = curMeta.zoomMax
                                )
                                moveCameraAction?.invoke(targetLatLng, null)
                            }
                            onIntent(MainUiIntent.CenterOnPoint(selPoint))
                        },
                        onDeleteClick = {
                            onIntent(MainUiIntent.DeleteLayerPoint(selPoint.id))
                        },
                        onDismiss = {
                            onIntent(MainUiIntent.DismissPointDetails)
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = 75.dp)
                    )
                }
            }

            // Selected Line Details Card
            if (uiState.selectedLineForDetails != null && !isCalibrationMode && uiState.editingLineLayer == null) {
                val selLine = uiState.selectedLineForDetails!!
                val parentLayer = uiState.lineLayers.find { it.id == selLine.layerId }
                if (parentLayer != null) {
                    val curMeta = mapMetadata ?: uiState.activeProjectMetadata
                    val ppm = curMeta?.pixelsPerMeter ?: 0.0
                    LineDetailsCard(
                        line = selLine,
                        layer = parentLayer,
                        ppm = ppm,
                        onEditClick = {
                            onIntent(MainUiIntent.OpenEditLineDialog(selLine))
                        },
                        onCenterMapClick = {
                            if (curMeta != null && selLine.points.isNotEmpty()) {
                                val midIndex = selLine.points.size / 2
                                val centerPt = selLine.points[midIndex]
                                val targetLatLng = CaveMapBounds.imagePixelsToLatLng(
                                    pixelX = centerPt.first,
                                    pixelY = centerPt.second,
                                    imageWidth = curMeta.imageWidth,
                                    imageHeight = curMeta.imageHeight,
                                    maxZoom = curMeta.zoomMax
                                )
                                moveCameraAction?.invoke(targetLatLng, null)
                            }
                            onIntent(MainUiIntent.CenterOnLine(selLine))
                        },
                        onDeleteClick = {
                            onIntent(MainUiIntent.DeleteLayerLine(selLine.id))
                        },
                        onDismiss = {
                            onIntent(MainUiIntent.DismissLineDetails)
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = 75.dp)
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
                onImportProjectClick = {
                    onIntent(MainUiIntent.DismissMenu)
                    onImportProject()
                },
                onExportProjectClick = { onIntent(MainUiIntent.ExportProjectClicked) },
                onCloseProject = { onIntent(MainUiIntent.CloseActiveProject) },
                onEditMetadataClick = { onIntent(MainUiIntent.OpenMetadataEditor) },
                onScaleBindingClick = { onIntent(MainUiIntent.StartScaleBinding) },
                onNorthBindingClick = { onIntent(MainUiIntent.StartNorthBinding) },
                onEntranceBindingClick = { onIntent(MainUiIntent.StartEntranceBinding) },
                isPointLayersModeActive = uiState.isPointLayersModeActive,
                onTogglePointLayersMode = { onIntent(MainUiIntent.TogglePointLayersMode) },
                isLineLayersModeActive = uiState.isLineLayersModeActive,
                onToggleLineLayersMode = { onIntent(MainUiIntent.ToggleLineLayersMode) },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Floating bottom bar
            FloatingBottomBar(
                onMenuClick = { onIntent(MainUiIntent.ToggleMenu) },
                showMagnetButton = (uiState.isLineLayersModeActive || uiState.editingLineLayer != null || uiState.isPointLayersModeActive || uiState.editingPointLayer != null),
                isMagnetEnabled = uiState.settings.snappingSettings.isEnabled,
                onMagnetClick = { onIntent(MainUiIntent.OpenSnappingSettingsDialog) },
                isPointLayersModeActive = uiState.isPointLayersModeActive,
                onPointLayersClick = { onIntent(MainUiIntent.OpenLayerManager) },
                onClosePointLayersClick = { onIntent(MainUiIntent.DisablePointLayersMode) },
                isLineLayersModeActive = uiState.isLineLayersModeActive,
                onLineLayersClick = { onIntent(MainUiIntent.OpenLineLayerManager) },
                onCloseLineLayersClick = { onIntent(MainUiIntent.DisableLineLayersMode) },
                onCloseAllLayersClick = {
                    onIntent(MainUiIntent.DisablePointLayersMode)
                    onIntent(MainUiIntent.DisableLineLayersMode)
                }
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

/**
 * Calculates squared distance from a 2D point (px, py) to a line segment (x1, y1) - (x2, y2).
 */
private fun distanceToSegmentSq(px: Float, py: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val dx = x2 - x1
    val dy = y2 - y1
    val l2 = dx * dx + dy * dy
    if (l2 == 0f) {
        val dpx = px - x1
        val dpy = py - y1
        return dpx * dpx + dpy * dpy
    }
    val t = ((px - x1) * dx + (py - y1) * dy) / l2
    val clampedT = t.coerceIn(0f, 1f)
    val projX = x1 + clampedT * dx
    val projY = y1 + clampedT * dy
    val diffX = px - projX
    val diffY = py - projY
    return diffX * diffX + diffY * diffY
}
