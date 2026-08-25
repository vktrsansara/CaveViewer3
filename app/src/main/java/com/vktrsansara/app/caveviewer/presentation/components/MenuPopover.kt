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
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Folder
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

enum class MenuLevel {
    MAIN,
    PROJECTS,
    SETTINGS
}

/**
 * Popover menu appearing above the bottom control bar with submenus for Project and Settings.
 *
 * @param isOpen Whether the menu is currently visible.
 * @param hasActiveProject Whether an active project is currently loaded.
 * @param onOpenAppSettings Callback invoked when clicking "Application" inside Settings submenu.
 * @param onExitApp Callback invoked when clicking "Exit".
 * @param onProjectListClick Callback for "List" inside Projects submenu.
 * @param onNewProjectClick Callback for "New" inside Projects submenu.
 * @param onImportProjectClick Callback for "Import" inside Projects submenu.
 * @param onExportProjectClick Callback for "Export" inside Projects submenu.
 * @param onCloseProject Callback for "Close" inside Projects submenu (unloads current project).
 * @param modifier Custom layout modifier.
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
                            onProjectsClick = { currentLevel = MenuLevel.PROJECTS },
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
    onProjectsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onExitClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        MenuHeader(title = "Меню")

        HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)

        // Item 1: Projects
        MenuItem(
            icon = Icons.Rounded.Folder,
            title = "Проект",
            onClick = onProjectsClick
        )

        // Item 2: Settings
        MenuItem(
            icon = Icons.Rounded.Settings,
            title = "Настройки",
            onClick = onSettingsClick
        )

        // Item 3: Exit
        MenuItem(
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            title = "Выход",
            onClick = onExitClick
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

        // Item: Список
        MenuItem(
            icon = Icons.AutoMirrored.Rounded.List,
            title = "Список",
            onClick = onListClick
        )

        // Item: Новый
        MenuItem(
            icon = Icons.Rounded.CreateNewFolder,
            title = "Новый",
            onClick = onNewClick
        )

        // Item: Импорт
        MenuItem(
            icon = Icons.Rounded.FileDownload,
            title = "Импорт",
            onClick = onImportClick
        )

        // Item: Экспорт
        MenuItem(
            icon = Icons.Rounded.FileUpload,
            title = "Экспорт",
            onClick = onExportClick
        )

        // Item: Закрыть (only when active project is open)
        if (hasActiveProject) {
            MenuItem(
                icon = Icons.Rounded.Close,
                title = "Закрыть",
                onClick = onCloseClick
            )
        }

        // Item: Назад
        MenuItem(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
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

        // Item: App Settings
        MenuItem(
            icon = Icons.Rounded.AppSettingsAlt,
            title = "Приложение",
            onClick = onAppSettingsClick
        )

        // Item: Back
        MenuItem(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
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
            tint = AppColors.textPrimary,
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
