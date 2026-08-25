package com.vktrsansara.app.caveviewer.presentation.projects

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import com.vktrsansara.app.caveviewer.domain.model.ProjectInfo
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentRed
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import com.vktrsansara.app.caveviewer.ui.theme.CaveViewerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen displaying the list of all available projects with open and delete actions.
 */
@Composable
fun ProjectsListScreen(
    projects: List<ProjectInfo>,
    activeProjectName: String?,
    onSelectProject: (String) -> Unit,
    onDeleteProject: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var projectToOpen by remember { mutableStateOf<ProjectInfo?>(null) }
    var projectToDelete by remember { mutableStateOf<ProjectInfo?>(null) }
    var isInfoDialogVisible by remember { mutableStateOf(false) }

    BackHandler(onBack = onNavigateBack)

    // Open project confirmation dialog
    if (projectToOpen != null) {
        val proj = projectToOpen!!
        OpenProjectConfirmDialog(
            projectName = proj.name,
            onConfirm = {
                projectToOpen = null
                onSelectProject(proj.name)
            },
            onDismiss = { projectToOpen = null }
        )
    }

    // Delete project confirmation dialog
    if (projectToDelete != null) {
        val proj = projectToDelete!!
        DeleteProjectConfirmDialog(
            projectName = proj.name,
            onConfirm = {
                projectToDelete = null
                onDeleteProject(proj.name)
            },
            onDismiss = { projectToDelete = null }
        )
    }

    // Projects list help dialog
    if (isInfoDialogVisible) {
        ProjectsListHelpDialog(onDismiss = { isInfoDialogVisible = false })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bgMain)
    ) {
        // 1. Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.bgSurface)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (32x32 dp)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AppColors.bgCard)
                        .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.pressedColor),
                            onClick = onNavigateBack
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = AccentSkyBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Список проектов",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textPrimary,
                    modifier = Modifier.weight(1f)
                )

                // Info Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AppColors.bgCard)
                        .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.pressedColor),
                            onClick = { isInfoDialogVisible = true }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "Справка",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
        }

        // 2. List Content
        if (projects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Проекты не найдены",
                    fontSize = 14.sp,
                    color = AppColors.textSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(projects, key = { it.name }) { project ->
                    ProjectItemCard(
                        project = project,
                        isActive = project.name == activeProjectName,
                        onClick = { projectToOpen = project },
                        onDeleteClick = { projectToDelete = project }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectItemCard(
    project: ProjectInfo,
    isActive: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val formattedDate = remember(project.lastModified) {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(project.lastModified))
    }
    val formattedSize = remember(project.sizeBytes) {
        formatBytes(project.sizeBytes)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.bgCard)
            .border(
                width = 1.dp,
                color = if (isActive) AppColors.accent else AppColors.borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = AppColors.pressedColor),
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Project Icon
        Icon(
            imageVector = Icons.Rounded.Folder,
            contentDescription = null,
            tint = if (isActive) AccentSkyBlue else Color(0xFFF59E0B),
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Info Column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = project.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "$formattedDate  •  $formattedSize",
                fontSize = 11.sp,
                color = AppColors.textSecondary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Delete button
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = AccentRed.copy(alpha = 0.2f)),
                    onClick = onDeleteClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = "Удалить",
                tint = AccentRed,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun OpenProjectConfirmDialog(
    projectName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.bgCard)
                .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Открыть проект?",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Загрузить проект «$projectName» на карту?",
                fontSize = 13.5.sp,
                color = AppColors.textSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DialogCancelButton(
                    text = "Отмена",
                    onClick = onDismiss
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AppColors.accent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.bgMain.copy(alpha = 0.3f)),
                            onClick = onConfirm
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Открыть",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.bgMain
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteProjectConfirmDialog(
    projectName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.bgCard)
                .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Удаление проекта",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccentRed
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Вы действительно хотите удалить проект «$projectName»? Все данные и карта будут удалены без возможности восстановления.",
                fontSize = 13.sp,
                color = AppColors.textSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DialogCancelButton(
                    text = "Отмена",
                    onClick = onDismiss
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentRed)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.bgMain.copy(alpha = 0.3f)),
                            onClick = onConfirm
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Удалить",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectsListHelpDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.bgCard)
                .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Справка: Список проектов",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "В списке отображаются все созданные и импортированные проекты из папки Documents / CaveViewer / Projects.",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = AppColors.textSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Нажмите на проект, чтобы загрузить его на карту, или на иконку корзины для удаления.",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = AppColors.textSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                DialogCancelButton(
                    text = "Закрыть",
                    onClick = onDismiss
                )
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(Locale.getDefault(), "%.1f KB", kb)
    val mb = kb / 1024.0
    return String.format(Locale.getDefault(), "%.1f MB", mb)
}

@Preview
@Composable
private fun ProjectsListScreenPreview() {
    CaveViewerTheme(darkTheme = true) {
        ProjectsListScreen(
            projects = listOf(
                ProjectInfo("Красная пещера", "/path", System.currentTimeMillis(), 4500000L, true),
                ProjectInfo("Мраморная", "/path", System.currentTimeMillis() - 86400000L, 2100000L, true)
            ),
            activeProjectName = "Красная пещера",
            onSelectProject = {},
            onDeleteProject = {},
            onNavigateBack = {}
        )
    }
}
