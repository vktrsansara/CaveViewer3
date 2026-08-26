package com.vktrsansara.app.caveviewer.presentation.main

import android.net.Uri
import com.vktrsansara.app.caveviewer.core.mvi.UiEffect
import com.vktrsansara.app.caveviewer.core.mvi.UiIntent
import com.vktrsansara.app.caveviewer.core.mvi.UiState
import com.vktrsansara.app.caveviewer.domain.model.AppSettings
import com.vktrsansara.app.caveviewer.domain.model.CadastralItem
import com.vktrsansara.app.caveviewer.domain.model.EntranceCoordinate
import com.vktrsansara.app.caveviewer.domain.model.MapCameraPosition
import com.vktrsansara.app.caveviewer.domain.model.MapLocation
import com.vktrsansara.app.caveviewer.domain.model.MapMetadata
import com.vktrsansara.app.caveviewer.domain.model.ProjectInfo
import com.vktrsansara.app.caveviewer.domain.model.ScaleBindingPoint
import com.vktrsansara.app.caveviewer.domain.model.ThemeMode
import org.maplibre.android.geometry.LatLng
import java.io.File

enum class AppScreen {
    MAIN,
    APP_SETTINGS,
    TOOLS_SETTINGS,
    PROJECTS_LIST,
    CREATE_RASTER_PROJECT,
    METADATA_EDITOR,
    FEATURE_UNDER_DEVELOPMENT
}

data class MainUiState(
    val currentScreen: AppScreen = AppScreen.MAIN,
    val isMenuExpanded: Boolean = false,
    val isProjectTypeDialogVisible: Boolean = false,
    val underDevelopmentFeatureName: String = "",
    val hasActiveProject: Boolean = false,
    val activeProjectName: String? = null,
    val activeProjectDir: File? = null,
    val activeProjectMetadata: MapMetadata? = null,
    val activeProjectLocation: MapLocation = MapLocation(),
    val activeProjectEntrances: List<EntranceCoordinate> = emptyList(),
    val activeProjectCadastralData: Map<String, List<CadastralItem>> = emptyMap(),
    val activeProjectCameraPosition: MapCameraPosition? = null,
    val projectsList: List<ProjectInfo> = emptyList(),
    val isProjectSaving: Boolean = false,
    val projectSavingName: String = "",
    val projectSavingProgress: Float = 0f,
    val projectSavingStatusText: String = "",
    val settings: AppSettings = AppSettings(),
    // Scale Calibration Mode State
    val isScaleBindingMode: Boolean = false,
    val scaleBindingPoints: List<ScaleBindingPoint> = emptyList(),
    val isScaleBindingHelpVisible: Boolean = false,
    val isScaleBindingInputVisible: Boolean = false,
    // North Calibration Mode State
    val isNorthBindingMode: Boolean = false,
    val northBindingPoints: List<ScaleBindingPoint> = emptyList(),
    val isNorthBindingHelpVisible: Boolean = false,
    val isNorthBindingInputVisible: Boolean = false,
    // Entrance Binding State
    val isEntranceCavePickMode: Boolean = false,
    val isOsmEntranceBindingMode: Boolean = false,
    val isEntranceBindingHelpVisible: Boolean = false,
    val pendingEntrancePlanPx: Pair<Double, Double>? = null,
    val isEntranceNameDialogVisible: Boolean = false,
    val pendingEntranceGps: LatLng? = null,
    // Ruler (Distance Measurement) State
    val isRulerMode: Boolean = false,
    val rulerPoints: List<ScaleBindingPoint> = emptyList(),
    // Area (Polygon Measurement) State
    val isAreaMeasureMode: Boolean = false,
    val areaPoints: List<ScaleBindingPoint> = emptyList(),
    // Angle (Live Dynamic Ray) State
    val isAngleMeasureMode: Boolean = false,
    val anglePoints: List<ScaleBindingPoint> = emptyList(),
    // Azimuth & Rumb (Live Ray & Horizon Direction) State
    val isAzimuthMode: Boolean = false,
    val azimuthOriginPoint: ScaleBindingPoint? = null,
    // Fault Line (Tectonic Axis) State
    val isFaultLineMode: Boolean = false,
    val faultLinePoints: List<ScaleBindingPoint> = emptyList(),
    // Radius (Circular Zone) State
    val isRadiusMeasureMode: Boolean = false,
    val radiusCenterPoint: ScaleBindingPoint? = null
) : UiState

sealed interface MainUiIntent : UiIntent {
    data object ToggleMenu : MainUiIntent
    data object DismissMenu : MainUiIntent
    data object ExitAppClicked : MainUiIntent
    data object OpenAppSettings : MainUiIntent
    data object OpenToolsSettings : MainUiIntent
    data object NavigateBack : MainUiIntent
    data class UpdateTheme(val theme: ThemeMode) : MainUiIntent
    data class UpdateFullscreen(val enabled: Boolean) : MainUiIntent
    data class OnShowCompassChanged(val enabled: Boolean) : MainUiIntent
    data class OnShowScaleBarChanged(val enabled: Boolean) : MainUiIntent
    data class UpdateCursorShow(val show: Boolean) : MainUiIntent
    data class UpdateCursorType(val type: Int) : MainUiIntent
    data class UpdateCursorColor(val color: Long) : MainUiIntent
    data object ToggleGrid : MainUiIntent
    data class UpdateGridEnabled(val enabled: Boolean) : MainUiIntent
    data class UpdateGridSizeMode(val mode: String) : MainUiIntent
    data class UpdateGridCustomSize(val size: Double) : MainUiIntent
    data class UpdateGridColor(val color: Long) : MainUiIntent
    data class UpdateColorPaletteMode(val mode: String) : MainUiIntent

    // Project Menu actions
    data object ProjectListClicked : MainUiIntent
    data object NewProjectClicked : MainUiIntent
    data object ImportProjectClicked : MainUiIntent
    data object ExportProjectClicked : MainUiIntent
    data object CloseActiveProject : MainUiIntent

    // Edit Menu actions
    data class OnMetadataLoaded(val metadata: MapMetadata) : MainUiIntent
    data object OpenMetadataEditor : MainUiIntent
    data class SaveMetadata(
        val updatedMetadata: MapMetadata,
        val originalProjectName: String,
        val location: MapLocation? = null,
        val entrances: List<EntranceCoordinate>? = null,
        val cadastralData: Map<String, List<CadastralItem>>? = null
    ) : MainUiIntent

    // Scale Binding actions
    data object StartScaleBinding : MainUiIntent
    data object DismissScaleBindingHelp : MainUiIntent
    data class AddScaleBindingPoint(val latLng: LatLng) : MainUiIntent
    data object UndoScaleBindingPoint : MainUiIntent
    data object CancelScaleBinding : MainUiIntent
    data object DismissScaleBindingInput : MainUiIntent
    data class SaveScaleBinding(val pixelsPerMeter: Double, val scaleMeters: Double) : MainUiIntent

    // North Binding actions
    data object StartNorthBinding : MainUiIntent
    data object DismissNorthBindingHelp : MainUiIntent
    data class AddNorthBindingPoint(val latLng: LatLng) : MainUiIntent
    data object UndoNorthBindingPoint : MainUiIntent
    data object CancelNorthBinding : MainUiIntent
    data object DismissNorthBindingInput : MainUiIntent
    data class SaveNorthBinding(val angle: Double) : MainUiIntent

    // Entrance Binding actions
    data object StartEntranceBinding : MainUiIntent
    data object DismissEntranceBindingHelp : MainUiIntent
    data class OnEntrancePlanPicked(val latLng: LatLng) : MainUiIntent
    data object CancelEntranceCavePick : MainUiIntent
    data class OnOsmEntranceTapped(val latLng: LatLng) : MainUiIntent
    data object DismissEntranceNameDialog : MainUiIntent
    data class SaveEntranceCoordinate(val name: String, val lat: Double, val lon: Double) : MainUiIntent
    data object CloseOsmEntranceBinding : MainUiIntent

    // Ruler (Distance Measurement) actions
    data object StartRulerMode : MainUiIntent
    data class AddRulerPoint(val latLng: LatLng) : MainUiIntent
    data object UndoRulerPoint : MainUiIntent
    data object CloseRulerMode : MainUiIntent

    // Area (Polygon Measurement) actions
    data object StartAreaMeasureMode : MainUiIntent
    data class AddAreaPoint(val latLng: LatLng) : MainUiIntent
    data object UndoAreaPoint : MainUiIntent
    data object CloseAreaMeasureMode : MainUiIntent

    // Angle (Live Dynamic Ray) actions
    data object StartAngleMeasureMode : MainUiIntent
    data class AddAnglePoint(val latLng: LatLng) : MainUiIntent
    data object UndoAnglePoint : MainUiIntent
    data object CloseAngleMeasureMode : MainUiIntent

    // Azimuth (Live Ray & Horizon Direction) actions
    data object StartAzimuthMode : MainUiIntent
    data class SetAzimuthOriginPoint(val latLng: LatLng) : MainUiIntent
    data object ResetAzimuthOriginPoint : MainUiIntent
    data object CloseAzimuthMode : MainUiIntent

    // Fault Line (Tectonic Axis) actions
    data object StartFaultLineMode : MainUiIntent
    data class AddFaultLinePoint(val latLng: LatLng) : MainUiIntent
    data object UndoFaultLinePoint : MainUiIntent
    data object CloseFaultLineMode : MainUiIntent

    // Radius (Circular Zone) actions
    data object StartRadiusMeasureMode : MainUiIntent
    data class SetRadiusCenterPoint(val latLng: LatLng) : MainUiIntent
    data object ResetRadiusCenterPoint : MainUiIntent
    data object CloseRadiusMeasureMode : MainUiIntent

    // Projects List actions
    data class SelectProject(val projectName: String) : MainUiIntent
    data class DeleteProject(val projectName: String) : MainUiIntent

    // Camera viewport persistence
    data class UpdateMapCameraPosition(val position: MapCameraPosition) : MainUiIntent

    // Project Type Selection
    data object DismissProjectTypeDialog : MainUiIntent
    data object SelectRasterProjectType : MainUiIntent
    data object SelectTopographyProjectType : MainUiIntent
    data object SelectTherionProjectType : MainUiIntent

    // Project Creation
    data class CreateRasterProject(val projectName: String, val imageUri: Uri) : MainUiIntent
    data object CancelProjectCreation : MainUiIntent
}

sealed interface MainUiEffect : UiEffect {
    data object ExitApp : MainUiEffect
    data class ShowToast(val message: String) : MainUiEffect
}
