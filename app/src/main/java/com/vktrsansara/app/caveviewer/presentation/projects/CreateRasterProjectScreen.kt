package com.vktrsansara.app.caveviewer.presentation.projects

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.presentation.projects.components.TileGenerationProgressDialog
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import com.vktrsansara.app.caveviewer.ui.theme.CaveViewerTheme

/**
 * Screen for creating a "Raster + Layers" project with live tile generation progress and abort capability.
 */
@Composable
fun CreateRasterProjectScreen(
    isSaving: Boolean,
    savingProgress: Float = 0f,
    savingStatusText: String = "",
    onCreateProject: (projectName: String, imageUri: Uri) -> Unit,
    onCancelSaving: () -> Unit = {},
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var projectName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageName by remember { mutableStateOf<String?>(null) }
    var selectedImageResolution by remember { mutableStateOf<String?>(null) }

    var isHelpDialogVisible by remember { mutableStateOf(false) }
    var isDiscardDialogVisible by remember { mutableStateOf(false) }

    val hasData = projectName.isNotBlank() || selectedImageUri != null
    val canCreate = projectName.isNotBlank() && selectedImageUri != null && !isSaving

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            // Сохраняем постоянное разрешение на чтение URI (SAF)
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            selectedImageUri = uri
            selectedImageName = getFileNameFromUri(context, uri)
            selectedImageResolution = getImageResolutionFromUri(context, uri)
        }
    }

    val handleBackPress = {
        if (!isSaving) {
            if (hasData) {
                isDiscardDialogVisible = true
            } else {
                onNavigateBack()
            }
        }
    }

    BackHandler(onBack = handleBackPress)

    // 1. Tile Generation Progress Dialog (with cancel action)
    if (isSaving) {
        TileGenerationProgressDialog(
            projectName = projectName.ifEmpty { "Новый проект" },
            progressFraction = savingProgress,
            statusText = savingStatusText,
            onCancel = onCancelSaving
        )
    }

    // 2. Help Dialog
    if (isHelpDialogVisible) {
        ProjectHelpDialog(onDismiss = { isHelpDialogVisible = false })
    }

    // 3. Discard Changes Confirmation Dialog
    if (isDiscardDialogVisible) {
        DiscardChangesDialog(
            onConfirmDiscard = {
                isDiscardDialogVisible = false
                onNavigateBack()
            },
            onDismiss = { isDiscardDialogVisible = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bgMain)
    ) {
        // 1. Screen Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.bgSurface)
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
                            onClick = handleBackPress
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

                // Title
                Text(
                    text = "Новый проект: Растр + Слои",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Help Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AppColors.bgCard)
                        .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.pressedColor),
                            onClick = { isHelpDialogVisible = true }
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

                Spacer(modifier = Modifier.width(8.dp))

                // Create Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AppColors.bgCard)
                        .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                        .alpha(if (canCreate) 1f else 0.4f)
                        .clickable(
                            enabled = canCreate,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.pressedColor),
                            onClick = {
                                val uri = selectedImageUri
                                if (uri != null && projectName.isNotBlank()) {
                                    onCreateProject(projectName, uri)
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = AppColors.accent,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Создать",
                            tint = if (canCreate) Color(0xFF10B981) else AppColors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
        }

        // 2. Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Block 1: Project Name
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.bgCard)
                    .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Название проекта:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Input field
                BasicTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    textStyle = TextStyle(
                        fontSize = 13.sp,
                        color = AppColors.textPrimary
                    ),
                    cursorBrush = SolidColor(AppColors.accent),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(AppColors.bgSurface)
                                .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (projectName.isEmpty()) {
                                Text(
                                    text = "Введите название проекта...",
                                    fontSize = 13.sp,
                                    color = AppColors.textSecondary
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "По названию создается папка проекта в /Documents/CaveViewer/Projects/",
                    fontSize = 11.sp,
                    color = AppColors.textSecondary,
                    lineHeight = 15.sp
                )
            }

            // Block 2: Map File
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.bgCard)
                    .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Файл карты:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // File picker row button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(AppColors.bgSurface)
                        .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.pressedColor),
                            onClick = {
                                imagePickerLauncher.launch(
                                    arrayOf(
                                        "image/*",
                                        "image/png",
                                        "image/jpeg",
                                        "image/jpg",
                                        "image/webp"
                                    )
                                )
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FolderOpen,
                        contentDescription = "Выбрать файл",
                        tint = if (selectedImageUri != null) Color(0xFFF59E0B) else AppColors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = selectedImageName ?: "Файл не выбран",
                        fontSize = 13.sp,
                        color = if (selectedImageUri != null) AppColors.textPrimary else AppColors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (selectedImageResolution != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Разрешение: $selectedImageResolution",
                        fontSize = 11.sp,
                        color = AppColors.accent
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Поддерживаются любые форматы изображений (PNG, JPG, WebP и др.). Создается пирамида тайлов 256×256 px и база данных thismap.sqlite.",
                    fontSize = 11.sp,
                    color = AppColors.textSecondary,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun DiscardChangesDialog(
    onConfirmDiscard: () -> Unit,
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Отменить создание?",
        onDismissRequest = onDismiss,
        buttons = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = AppColors.pressedColor),
                        onClick = onDismiss
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Продолжить",
                    fontSize = 13.sp,
                    color = AppColors.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            DialogCancelButton(
                text = "Отменить",
                onClick = onConfirmDiscard
            )
        }
    ) {
        Text(
            text = "Введенные данные проекта будут потеряны.",
            fontSize = 13.sp,
            color = AppColors.textSecondary,
            lineHeight = 18.sp
        )
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String {
    var name: String? = null
    if (uri.scheme == "content") {
        try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        name = cursor.getString(index)
                    }
                }
            }
        } catch (_: Exception) {}
    }
    return name ?: uri.lastPathSegment ?: "image.png"
}

private fun getImageResolutionFromUri(context: Context, uri: Uri): String? {
    return try {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
        if (options.outWidth > 0 && options.outHeight > 0) {
            "${options.outWidth} × ${options.outHeight} px"
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}

@Preview
@Composable
private fun CreateRasterProjectScreenPreview() {
    CaveViewerTheme(darkTheme = true) {
        CreateRasterProjectScreen(
            isSaving = false,
            onCreateProject = { _, _ -> },
            onNavigateBack = {}
        )
    }
}
