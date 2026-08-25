package com.vktrsansara.app.caveviewer.presentation.main

import android.net.Uri
import com.vktrsansara.app.caveviewer.core.mvi.UiEffect
import com.vktrsansara.app.caveviewer.core.mvi.UiIntent
import com.vktrsansara.app.caveviewer.core.mvi.UiState
import com.vktrsansara.app.caveviewer.domain.model.AppSettings
import com.vktrsansara.app.caveviewer.domain.model.EntranceCoordinate
import com.vktrsansara.app.caveviewer.domain.model.MapLocation
import com.vktrsansara.app.caveviewer.domain.model.MapMetadata
import com.vktrsansara.app.caveviewer.domain.model.ProjectInfo
import com.vktrsansara.app.caveviewer.domain.model.ThemeMode
import java.io.File

enum class AppScreen {
    MAIN,
    APP_SETTINGS,
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
    val projectsList: List<ProjectInfo> = emptyList(),
    val isProjectSaving: Boolean = false,
    val projectSavingName: String = "",
    val projectSavingProgress: Float = 0f,
    val projectSavingStatusText: String = "",
    val settings: AppSettings = AppSettings()
) : UiState

sealed interface MainUiIntent : UiIntent {
    data object ToggleMenu : MainUiIntent
    data object DismissMenu : MainUiIntent
    data object ExitAppClicked : MainUiIntent
    data object OpenAppSettings : MainUiIntent
    data object NavigateBack : MainUiIntent
    data class UpdateTheme(val theme: ThemeMode) : MainUiIntent
    data class UpdateFullscreen(val enabled: Boolean) : MainUiIntent

    // Project Menu actions
    data object ProjectListClicked : MainUiIntent
    data object NewProjectClicked : MainUiIntent
    data object ImportProjectClicked : MainUiIntent
    data object ExportProjectClicked : MainUiIntent
    data object CloseActiveProject : MainUiIntent

    // Edit Menu actions
    data object OpenMetadataEditor : MainUiIntent
    data class SaveMetadata(
        val updatedMetadata: MapMetadata,
        val originalProjectName: String,
        val location: MapLocation? = null,
        val entrances: List<EntranceCoordinate>? = null
    ) : MainUiIntent

    // Projects List actions
    data class SelectProject(val projectName: String) : MainUiIntent
    data class DeleteProject(val projectName: String) : MainUiIntent

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
