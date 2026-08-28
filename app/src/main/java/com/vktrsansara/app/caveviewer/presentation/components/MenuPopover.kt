package com.vktrsansara.app.caveviewer.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.AddLocation
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.AppSettingsAlt
import androidx.compose.material.icons.rounded.Architecture
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.DesignServices
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocationSearching
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Polyline
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SquareFoot
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.MapFilterMode
import com.vktrsansara.app.caveviewer.domain.model.ToolType
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import com.vktrsansara.app.caveviewer.ui.theme.CaveViewerTheme

// Palette of semantic icon colors
private val IconFolderColor = Color(0xFFF59E0B) // Amber
private val IconEditColor = Color(0xFF818CF8)   // Indigo
private val IconToolsColor = Color(0xFF10B981)  // Emerald Green
private val IconSettingsColor = Color(0xFF38BDF8) // Sky Blue
private val IconExitColor = Color(0xFFEF4444)   // Red
private val IconListColor = Color(0xFFA78BFA)   // Purple
private val IconNewColor = Color(0xFF10B981)    // Green
private val IconImportColor = Color(0xFF06B6D4) // Cyan
private val IconExportColor = Color(0xFFFB923C) // Orange
private val IconCloseColor = Color(0xFFEF4444)  // Red
private val IconBackArrowColor = AccentSkyBlue
private val IconBindingColor = Color(0xFFF59E0B) // Amber

enum class MenuLevel {
    MAIN,
    PROJECTS,
    EDIT,
    BINDING,
    TOOLS,
    SETTINGS
}

/**
 * Popover menu appearing above the bottom control bar with submenus for Project, Edit, Binding, Tools, and Settings.
 */
@Composable
fun MenuPopover(
    isOpen: Boolean,
    hasActiveProject: Boolean = false,
    dockedTools: List<ToolType> = emptyList(),
    favoriteTools: List<String> = emptyList(),
    mapFilter: MapFilterMode = MapFilterMode.NONE,
    onOpenFavoriteToolsPreset: () -> Unit = {},
    isGridEnabled: Boolean = false,
    onToggleGrid: () -> Unit = {},
    onStartRulerClick: () -> Unit = {},
    onStartAreaMeasureClick: () -> Unit = {},
    onStartAngleMeasureClick: () -> Unit = {},
    onStartAzimuthClick: () -> Unit = {},
    onStartFaultLineClick: () -> Unit = {},
    onStartDeltaOffsetClick: () -> Unit = {},
    onStartRadiusMeasureClick: () -> Unit = {},
    onOpenMapFiltersClick: () -> Unit = {},
    onOpenAppSettings: () -> Unit,
    onOpenToolsSettings: () -> Unit = {},
    onExitApp: () -> Unit,
    onProjectListClick: () -> Unit = {},
    onNewProjectClick: () -> Unit = {},
    onImportProjectClick: () -> Unit = {},
    onExportProjectClick: () -> Unit = {},
    onCloseProject: () -> Unit = {},
    onEditMetadataClick: () -> Unit = {},
    onScaleBindingClick: () -> Unit = {},
    onNorthBindingClick: () -> Unit = {},
    onEntranceBindingClick: () -> Unit = {},
    isPointLayersModeActive: Boolean = false,
    onTogglePointLayersMode: () -> Unit = {},
    onOpenLineLayerManagerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentLevel by remember { mutableStateOf(MenuLevel.MAIN) }

    // Reset submenu to MAIN whenever popover is closed
    LaunchedEffect(isOpen) {
        if (!isOpen) {
            currentLevel = MenuLevel.MAIN
        }
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn() + slideInVertically { it / 2 } + scaleIn(initialScale = 0.95f),
        exit = fadeOut() + slideOutVertically { it / 2 } + scaleOut(targetScale = 0.95f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .width(190.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.bgCard)
                .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
        ) {
            AnimatedContent(
                targetState = currentLevel,
                transitionSpec = {
                    if (targetState != MenuLevel.MAIN) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "MenuLevelTransition"
            ) { level ->
                when (level) {
                    MenuLevel.MAIN -> {
                        MainMenuContent(
                            hasActiveProject = hasActiveProject,
                            favoriteTools = favoriteTools,
                            onOpenFavoriteToolsPreset = onOpenFavoriteToolsPreset,
                            onProjectsClick = { currentLevel = MenuLevel.PROJECTS },
                            onEditClick = { currentLevel = MenuLevel.EDIT },
                            onOpenLineLayerManagerClick = onOpenLineLayerManagerClick,
                            onToolsClick = { currentLevel = MenuLevel.TOOLS },
                            onSettingsClick = { currentLevel = MenuLevel.SETTINGS },
                            onExitClick = onExitApp
                        )
                    }
                    MenuLevel.PROJECTS -> {
                        ProjectsSubmenuContent(
                            hasActiveProject = hasActiveProject,
                            onListClick = onProjectListClick,
                            onNewClick = onNewProjectClick,
                            onImportClick = onImportProjectClick,
                            onExportClick = onExportProjectClick,
                            onCloseClick = onCloseProject,
                            onBackClick = { currentLevel = MenuLevel.MAIN }
                        )
                    }
                    MenuLevel.EDIT -> {
                        EditSubmenuContent(
                            isPointLayersModeActive = isPointLayersModeActive,
                            onMetadataClick = onEditMetadataClick,
                            onBindingClick = { currentLevel = MenuLevel.BINDING },
                            onTogglePointLayersMode = onTogglePointLayersMode,
                            onBackClick = { currentLevel = MenuLevel.MAIN }
                        )
                    }
                    MenuLevel.BINDING -> {
                        BindingSubmenuContent(
                            onScaleClick = onScaleBindingClick,
                            onNorthClick = onNorthBindingClick,
                            onEntranceClick = onEntranceBindingClick,
                            onBackClick = { currentLevel = MenuLevel.EDIT }
                        )
                    }
                    MenuLevel.TOOLS -> {
                        ToolsSubmenuContent(
                            dockedTools = dockedTools,
                            isGridEnabled = isGridEnabled,
                            isMapFilterActive = (mapFilter != MapFilterMode.NONE),
                            onToggleGrid = onToggleGrid,
                            onStartRulerClick = onStartRulerClick,
                            onStartAreaMeasureClick = onStartAreaMeasureClick,
                            onStartAngleMeasureClick = onStartAngleMeasureClick,
                            onStartAzimuthClick = onStartAzimuthClick,
                            onStartFaultLineClick = onStartFaultLineClick,
                            onStartDeltaOffsetClick = onStartDeltaOffsetClick,
                            onStartRadiusMeasureClick = onStartRadiusMeasureClick,
                            onOpenMapFiltersClick = onOpenMapFiltersClick,
                            onBackClick = { currentLevel = MenuLevel.MAIN }
                        )
                    }
                    MenuLevel.SETTINGS -> {
                        SettingsSubmenuContent(
                            onAppSettingsClick = onOpenAppSettings,
                            onToolsSettingsClick = onOpenToolsSettings,
                            onBackClick = { currentLevel = MenuLevel.MAIN }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainMenuContent(
    hasActiveProject: Boolean,
    favoriteTools: List<String>,
    onOpenFavoriteToolsPreset: () -> Unit,
    onProjectsClick: () -> Unit,
    onEditClick: () -> Unit,
    onOpenLineLayerManagerClick: () -> Unit,
    onToolsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onExitClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        MenuHeader(title = "Меню")

        HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)

        // Item 1: Проект (Amber Folder)
        MenuItem(
            icon = Icons.Rounded.Folder,
            iconTint = IconFolderColor,
            title = "Проект",
            onClick = onProjectsClick
        )

        // Item 2: Редактировать (Indigo Design/Edit) - active only if project is loaded
        if (hasActiveProject) {
            MenuItem(
                icon = Icons.Rounded.DesignServices,
                iconTint = IconEditColor,
                title = "Редактировать",
                onClick = onEditClick
            )
        }

        // Item 2.5: Слои линий (Emerald Green Polyline) - active only if project is loaded
        if (hasActiveProject) {
            MenuItem(
                icon = Icons.Rounded.Polyline,
                iconTint = Color(0xFF10B981), // Emerald Green
                title = "Слои линий",
                onClick = onOpenLineLayerManagerClick
            )
        }

        // Item 3: Инструменты (Emerald Green Build)
        MenuItem(
            icon = Icons.Rounded.Build,
            iconTint = IconToolsColor,
            title = "Инструменты",
            onClick = onToolsClick
        )

        // Item 3.5: Мои инструменты (показывается, только если есть сохраненное Избранное)
        if (favoriteTools.isNotEmpty()) {
            MenuItem(
                icon = Icons.Rounded.Star,
                iconTint = Color(0xFFF59E0B), // Gold
                title = "Мои инструменты",
                onClick = onOpenFavoriteToolsPreset
            )
        }

        // Item 4: Настройки (Sky Blue Settings)
        MenuItem(
            icon = Icons.Rounded.Settings,
            iconTint = IconSettingsColor,
            title = "Настройки",
            onClick = onSettingsClick
        )

        // Item 5: Выход (Red Exit)
        MenuItem(
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            iconTint = IconExitColor,
            title = "Выход",
            onClick = onExitClick
        )
    }
}

@Composable
private fun ToolsSubmenuContent(
    dockedTools: List<ToolType>,
    isGridEnabled: Boolean,
    isMapFilterActive: Boolean = false,
    onToggleGrid: () -> Unit,
    onStartRulerClick: () -> Unit,
    onStartAreaMeasureClick: () -> Unit,
    onStartAngleMeasureClick: () -> Unit,
    onStartAzimuthClick: () -> Unit,
    onStartFaultLineClick: () -> Unit,
    onStartDeltaOffsetClick: () -> Unit,
    onStartRadiusMeasureClick: () -> Unit,
    onOpenMapFiltersClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val showToolChecks = dockedTools.size > 1

    Column(modifier = Modifier.fillMaxWidth()) {
        MenuHeader(title = "Инструменты")
        HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)

        // 1. Сетка с галочкой справа
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = AppColors.pressedColor),
                    onClick = onToggleGrid
                )
                .padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.GridOn,
                    contentDescription = "Сетка",
                    tint = Color(0xFF84CC16), // Lime
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Сетка",
                    color = AppColors.textPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Normal
                )
            }
            // Галочка справа, если сетка активна
            if (isGridEnabled) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Включено",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        // 2. Линейка (Фиолетовая иконка Straighten)
        MenuItem(
            icon = Icons.Rounded.Straighten,
            iconTint = Color(0xFF8A2BE2), // BlueViolet
            title = "Линейка",
            isChecked = showToolChecks && ToolType.RULER in dockedTools,
            onClick = onStartRulerClick
        )

        // 3. Площадь (Иконка SquareFoot)
        MenuItem(
            icon = Icons.Rounded.SquareFoot,
            iconTint = Color(0xFFA855F7), // Purple
            title = "Площадь",
            isChecked = showToolChecks && ToolType.AREA in dockedTools,
            onClick = onStartAreaMeasureClick
        )

        // 4. Угол (Иконка Architecture)
        MenuItem(
            icon = Icons.Rounded.Architecture,
            iconTint = Color(0xFFF59E0B), // Amber Gold
            title = "Угол",
            isChecked = showToolChecks && ToolType.ANGLE in dockedTools,
            onClick = onStartAngleMeasureClick
        )

        // 5. Азимут (Иконка Explore / Cyan)
        MenuItem(
            icon = Icons.Rounded.Explore,
            iconTint = Color(0xFF06B6D4), // Cyan
            title = "Азимут",
            isChecked = showToolChecks && ToolType.AZIMUTH in dockedTools,
            onClick = onStartAzimuthClick
        )

        // 6. Ось разломов (Иконка Timeline / Pink)
        MenuItem(
            icon = Icons.Rounded.Timeline,
            iconTint = Color(0xFFEC4899), // Pink
            title = "Ось разломов",
            isChecked = showToolChecks && ToolType.FAULT_LINE in dockedTools,
            onClick = onStartFaultLineClick
        )

        // 7. Смещение (ΔX, ΔY) (Иконка LocationSearching / Indigo)
        MenuItem(
            icon = Icons.Rounded.LocationSearching,
            iconTint = Color(0xFF6366F1), // Indigo
            title = "Смещение (ΔX, ΔY)",
            isChecked = showToolChecks && ToolType.DELTA_OFFSET in dockedTools,
            onClick = onStartDeltaOffsetClick
        )

        // 8. Радиус (Иконка RadioButtonUnchecked / Emerald)
        MenuItem(
            icon = Icons.Rounded.RadioButtonUnchecked,
            iconTint = Color(0xFF10B981), // Emerald
            title = "Радиус",
            isChecked = showToolChecks && ToolType.RADIUS in dockedTools,
            onClick = onStartRadiusMeasureClick
        )

        // 9. Фильтры (Иконка ColorLens / Sky Blue)
        MenuItem(
            icon = Icons.Rounded.ColorLens,
            iconTint = Color(0xFF38BDF8), // Sky Blue
            title = "Фильтры",
            isChecked = isMapFilterActive,
            onClick = onOpenMapFiltersClick
        )

        // 10. Назад
        MenuItem(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            iconTint = AccentSkyBlue,
            title = "Назад",
            onClick = onBackClick
        )
    }
}

@Composable
private fun EditSubmenuContent(
    isPointLayersModeActive: Boolean,
    onMetadataClick: () -> Unit,
    onBindingClick: () -> Unit,
    onTogglePointLayersMode: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        MenuHeader(title = "Редактировать")

        HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)

        // Item 1: Метаданные (Sky Blue Map)
        MenuItem(
            icon = Icons.Rounded.Map,
            iconTint = AccentSkyBlue,
            title = "Метаданные",
            onClick = onMetadataClick
        )

        // Item 2: Привязка (Amber Straighten)
        MenuItem(
            icon = Icons.Rounded.Straighten,
            iconTint = IconBindingColor,
            title = "Привязка",
            onClick = onBindingClick
        )

        // Item 3: Слои точек (Sky Blue AddLocationAlt) с галочкой активности
        MenuItem(
            icon = Icons.Rounded.AddLocationAlt,
            iconTint = AccentSkyBlue,
            title = "Слои точек",
            isChecked = isPointLayersModeActive,
            onClick = onTogglePointLayersMode
        )

        // Item 4: Назад (Sky Blue Arrow)
        MenuItem(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            iconTint = IconBackArrowColor,
            title = "Назад",
            onClick = onBackClick
        )
    }
}

@Composable
private fun BindingSubmenuContent(
    onScaleClick: () -> Unit,
    onNorthClick: () -> Unit,
    onEntranceClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        MenuHeader(title = "Привязка")

        HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)

        // 1. Масштаб
        MenuItem(
            icon = Icons.Rounded.Straighten,
            iconTint = Color(0xFFF59E0B), // Amber
            title = "Масштаб",
            onClick = onScaleClick
        )

        // 2. Север (Compass)
        MenuItem(
            icon = Icons.Rounded.Explore,
            iconTint = Color(0xFF38BDF8), // Sky Blue
            title = "Север",
            onClick = onNorthClick
        )

        // 3. Точка входа (Entrance)
        MenuItem(
            icon = Icons.Rounded.AddLocation,
            iconTint = Color(0xFF10B981), // Emerald
            title = "Точка входа",
            onClick = onEntranceClick
        )

        // 4. Назад
        MenuItem(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            iconTint = AccentSkyBlue,
            title = "Назад",
            onClick = onBackClick
        )
    }
}

@Composable
private fun ProjectsSubmenuContent(
    hasActiveProject: Boolean,
    onListClick: () -> Unit,
    onNewClick: () -> Unit,
    onImportClick: () -> Unit,
    onExportClick: () -> Unit,
    onCloseClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        MenuHeader(title = "Проект")

        HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)

        // Item: Список (Purple List)
        MenuItem(
            icon = Icons.AutoMirrored.Rounded.List,
            iconTint = IconListColor,
            title = "Список",
            onClick = onListClick
        )

        // Item: Новый (Green Folder)
        MenuItem(
            icon = Icons.Rounded.CreateNewFolder,
            iconTint = IconNewColor,
            title = "Новый",
            onClick = onNewClick
        )

        // Item: Импорт (Cyan Download)
        MenuItem(
            icon = Icons.Rounded.FileDownload,
            iconTint = IconImportColor,
            title = "Импорт",
            onClick = onImportClick
        )

        // Item: Экспорт (Orange Upload)
        MenuItem(
            icon = Icons.Rounded.FileUpload,
            iconTint = IconExportColor,
            title = "Экспорт",
            onClick = onExportClick
        )

        // Item: Закрыть (Red Close)
        if (hasActiveProject) {
            MenuItem(
                icon = Icons.Rounded.Close,
                iconTint = IconCloseColor,
                title = "Закрыть",
                onClick = onCloseClick
            )
        }

        // Item: Назад
        MenuItem(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            iconTint = IconBackArrowColor,
            title = "Назад",
            onClick = onBackClick
        )
    }
}

@Composable
private fun SettingsSubmenuContent(
    onAppSettingsClick: () -> Unit,
    onToolsSettingsClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        MenuHeader(title = "Настройки")

        HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)

        // Item: App Settings (Indigo Settings)
        MenuItem(
            icon = Icons.Rounded.AppSettingsAlt,
            iconTint = IconEditColor,
            title = "Приложение",
            onClick = onAppSettingsClick
        )

        // Item: Tools Settings (Green Build)
        MenuItem(
            icon = Icons.Rounded.Build,
            iconTint = Color(0xFF10B981),
            title = "Инструменты",
            onClick = onToolsSettingsClick
        )

        // Item: Back
        MenuItem(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            iconTint = IconBackArrowColor,
            title = "Назад",
            onClick = onBackClick
        )
    }
}

@Composable
private fun MenuHeader(
    title: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = title,
            color = AppColors.textPrimary,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    isChecked: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = AppColors.pressedColor),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                color = AppColors.textPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Normal
            )
        }
        if (isChecked) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Включено",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Preview
@Composable
private fun MenuPopoverPreview() {
    CaveViewerTheme(darkTheme = true) {
        MenuPopover(
            isOpen = true,
            hasActiveProject = true,
            onOpenAppSettings = {},
            onExitApp = {}
        )
    }
}
