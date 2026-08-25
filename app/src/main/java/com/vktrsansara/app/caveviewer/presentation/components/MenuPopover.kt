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
import androidx.compose.material.icons.rounded.AppSettingsAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.DesignServices
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Settings
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
import com.vktrsansara.app.caveviewer.ui.theme.AccentRed
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

// Palette of semantic icon colors
private val IconFolderColor = Color(0xFFF59E0B) // Amber
private val IconEditColor = Color(0xFF818CF8)   // Indigo
private val IconSettingsColor = Color(0xFF38BDF8) // Sky Blue
private val IconExitColor = Color(0xFFEF4444)   // Red
private val IconListColor = Color(0xFFA78BFA)   // Purple
private val IconNewColor = Color(0xFF10B981)    // Green
private val IconImportColor = Color(0xFF06B6D4) // Cyan
private val IconExportColor = Color(0xFFFB923C) // Orange
private val IconCloseColor = Color(0xFFEF4444)  // Red
private val IconBackArrowColor = AccentSkyBlue

enum class MenuLevel {
    MAIN,
    PROJECTS,
    EDIT,
    SETTINGS
}

/**
 * Popover menu appearing above the bottom control bar with submenus for Project, Edit, and Settings.
 */
@Composable
fun MenuPopover(
    isOpen: Boolean,
    hasActiveProject: Boolean = false,
    onOpenAppSettings: () -> Unit,
    onExitApp: () -> Unit,
    onProjectListClick: () -> Unit = {},
    onNewProjectClick: () -> Unit = {},
    onImportProjectClick: () -> Unit = {},
    onExportProjectClick: () -> Unit = {},
    onCloseProject: () -> Unit = {},
    onEditMetadataClick: () -> Unit = {},
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
                            onProjectsClick = { currentLevel = MenuLevel.PROJECTS },
                            onEditClick = { currentLevel = MenuLevel.EDIT },
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
                            onMetadataClick = onEditMetadataClick,
                            onBackClick = { currentLevel = MenuLevel.MAIN }
                        )
                    }
                    MenuLevel.SETTINGS -> {
                        SettingsSubmenuContent(
                            onAppSettingsClick = onOpenAppSettings,
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
    onProjectsClick: () -> Unit,
    onEditClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onExitClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        MenuHeader(title = "Меню")

        HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)

        // Item 1: Projects (Amber Folder)
        MenuItem(
            icon = Icons.Rounded.Folder,
            iconTint = IconFolderColor,
            title = "Проект",
            onClick = onProjectsClick
        )

        // Item 2: Edit (Indigo Design - only when project is open)
        if (hasActiveProject) {
            MenuItem(
                icon = Icons.Rounded.DesignServices,
                iconTint = IconEditColor,
                title = "Редактировать",
                onClick = onEditClick
            )
        }

        // Item 3: Settings (Sky Blue Settings)
        MenuItem(
            icon = Icons.Rounded.Settings,
            iconTint = IconSettingsColor,
            title = "Настройки",
            onClick = onSettingsClick
        )

        // Item 4: Exit (Red Exit)
        MenuItem(
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            iconTint = IconExitColor,
            title = "Выход",
            onClick = onExitClick
        )
    }
}

@Composable
private fun EditSubmenuContent(
    onMetadataClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        MenuHeader(title = "Редактировать")

        HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)

        // Item: Метаданные (Sky Blue Map)
        MenuItem(
            icon = Icons.Rounded.Map,
            iconTint = AccentSkyBlue,
            title = "Метаданные",
            onClick = onMetadataClick
        )

        // Item: Назад (Sky Blue Arrow)
        MenuItem(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            iconTint = IconBackArrowColor,
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
private fun MenuHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.bgSurface)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.textSecondary
        )
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
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
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = AppColors.textPrimary
        )
    }
}

@Preview
@Composable
private fun MenuPopoverPreview() {
    MenuPopover(
        isOpen = true,
        hasActiveProject = true,
        onOpenAppSettings = {},
        onExitApp = {}
    )
}
