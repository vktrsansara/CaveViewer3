package com.vktrsansara.app.caveviewer.presentation.projects

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PhotoSizeSelectActual
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.MapMetadata
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.presentation.components.DialogSaveButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

private data class CompressionItem(
    val level: Int,
    val title: String,
    val subtitle: String
)

private val CompressionLevels = listOf(
    CompressionItem(0, "Без сжатия (Быстро)", "Запись без сжатия данных"),
    CompressionItem(1, "Быстрое сжатие", "Минимальная нагрузка на процессор"),
    CompressionItem(6, "Стандартное (Рекомендуется)", "Оптимальный баланс размера и времени"),
    CompressionItem(9, "Максимальное сжатие", "Наименьший итоговый размер файла")
)

/**
 * Modal dialog for exporting a cave survey project to the standalone .cvproj format.
 */
@Composable
fun ExportProjectDialog(
    projectName: String,
    metadata: MapMetadata?,
    pointLayersCount: Int,
    lineLayersCount: Int,
    pointsCount: Int,
    linesCount: Int,
    onExport: (outputUri: Uri, compressionLevel: Int, password: String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fileName by remember {
        val sanitized = projectName.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_")
        mutableStateOf(if (sanitized.isNotBlank()) "$sanitized.cvproj" else "Project.cvproj")
    }
    var selectedCompressionLevel by remember { mutableIntStateOf(6) }
    var isCompressionDropdownExpanded by remember { mutableStateOf(false) }

    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isHelpDialogVisible by remember { mutableStateOf(false) }

    // System file creation launcher
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            val pass = password.trim().ifEmpty { null }
            onExport(uri, selectedCompressionLevel, pass)
        }
    }

    if (isHelpDialogVisible) {
        ExportHelpDialog(onDismiss = { isHelpDialogVisible = false })
    }

    AppDialogContainer(
        title = "Экспорт проекта",
        onDismissRequest = onDismiss,
        onInfoClick = { isHelpDialogVisible = true },
        modifier = modifier,
        buttons = {
            DialogCancelButton(
                text = "Отмена",
                onClick = onDismiss
            )
            Spacer(modifier = Modifier.width(8.dp))
            DialogSaveButton(
                text = "Экспортировать",
                onClick = {
                    val finalFileName = fileName.trim().let {
                        if (it.endsWith(".cvproj", ignoreCase = true)) it else "$it.cvproj"
                    }
                    createDocumentLauncher.launch(finalFileName)
                }
            )
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // 1. Информационный блок проекта
            ProjectInfoCard(
                projectName = projectName,
                imageWidth = metadata?.imageWidth ?: 0,
                imageHeight = metadata?.imageHeight ?: 0,
                pointLayersCount = pointLayersCount,
                lineLayersCount = lineLayersCount,
                pointsCount = pointsCount,
                linesCount = linesCount
            )

            // 2. Поле «Имя файла»
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Имя файла",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textSecondary
                )
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppColors.bgSurface,
                        unfocusedContainerColor = AppColors.bgSurface,
                        focusedBorderColor = AccentSkyBlue,
                        unfocusedBorderColor = AppColors.borderColor,
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. Уровень сжатия
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Уровень сжатия",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textSecondary
                )

                val selectedItem = CompressionLevels.firstOrNull { it.level == selectedCompressionLevel }
                    ?: CompressionLevels[2]

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.bgSurface)
                        .border(1.dp, AppColors.borderColor, RoundedCornerShape(8.dp))
                        .clickable { isCompressionDropdownExpanded = !isCompressionDropdownExpanded }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedItem.title,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.textPrimary
                            )
                            Text(
                                text = selectedItem.subtitle,
                                fontSize = 11.5.sp,
                                color = AppColors.textSecondary
                            )
                        }
                        Icon(
                            imageVector = if (isCompressionDropdownExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = AccentSkyBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (isCompressionDropdownExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppColors.bgCard)
                            .border(1.dp, AppColors.borderColor, RoundedCornerShape(8.dp))
                    ) {
                        CompressionLevels.forEachIndexed { index, item ->
                            val isSelected = (item.level == selectedCompressionLevel)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCompressionLevel = item.level
                                        isCompressionDropdownExpanded = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) AccentSkyBlue else AppColors.textPrimary
                                    )
                                    Text(
                                        text = item.subtitle,
                                        fontSize = 11.sp,
                                        color = AppColors.textSecondary
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = AccentSkyBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            if (index < CompressionLevels.lastIndex) {
                                HorizontalDivider(thickness = 0.5.dp, color = AppColors.borderColor.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }

            // 4. Пароль для защиты (Опционально)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Пароль для защиты (Опционально)",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textSecondary
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "Оставьте пустым, если защита не требуется",
                            fontSize = 12.5.sp,
                            color = AppColors.textSecondary.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = if (password.isNotEmpty()) AccentSkyBlue else AppColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = if (isPasswordVisible) "Скрыть пароль" else "Показать пароль",
                                tint = AppColors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppColors.bgSurface,
                        unfocusedContainerColor = AppColors.bgSurface,
                        focusedBorderColor = AccentSkyBlue,
                        unfocusedBorderColor = AppColors.borderColor,
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ProjectInfoCard(
    projectName: String,
    imageWidth: Int,
    imageHeight: Int,
    pointLayersCount: Int,
    lineLayersCount: Int,
    pointsCount: Int,
    linesCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.bgSurface)
            .border(1.dp, AppColors.borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Folder,
                contentDescription = null,
                tint = AccentSkyBlue,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = projectName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
        }

        HorizontalDivider(thickness = 0.5.dp, color = AppColors.borderColor.copy(alpha = 0.5f))

        InfoRow(
            icon = Icons.Rounded.PhotoSizeSelectActual,
            label = "Размеры растра:",
            value = if (imageWidth > 0 && imageHeight > 0) "$imageWidth × $imageHeight px" else "Не определен"
        )

        InfoRow(
            icon = Icons.Rounded.Layers,
            label = "Слои:",
            value = "${pointLayersCount + lineLayersCount} (точек: $pointLayersCount, линий: $lineLayersCount)"
        )

        InfoRow(
            icon = Icons.Rounded.Timeline,
            label = "Объекты:",
            value = "${pointsCount + linesCount} (точек: $pointsCount, линий: $linesCount)"
        )
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.textSecondary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = AppColors.textSecondary
            )
        }
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.textPrimary
        )
    }
}

/**
 * Help dialog explaining .cvproj format and security options without revealing zip internal structure.
 */
@Composable
private fun ExportHelpDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Формат проекта .cvproj",
        onDismissRequest = onDismiss,
        buttons = {
            DialogCancelButton(
                text = "Закрыть",
                onClick = onDismiss
            )
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            HelpItem(
                title = "Формат проекта .cvproj",
                description = "Автономный файл проекта CaveViewer, объединяющий растровый план пещеры, векторные слои точек и линий, привязки масштаба и ориентации, а также полную кадастровую документацию."
            )
            HelpItem(
                title = "Уровни сжатия",
                description = "Позволяют выбрать баланс между скоростью создания файла и его итоговым размером. Стандартное сжатие является оптимальным выбором для большинства карт."
            )
            HelpItem(
                title = "Защита паролем",
                description = "Шифрует содержимое проекта надежным алгоритмом AES-256. Файлы проекта невозможно будет открыть или извлечь без знания установленного пароля."
            )
        }
    }
}

@Composable
private fun HelpItem(
    title: String,
    description: String
) {
    Column {
        Text(
            text = title,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = AccentSkyBlue
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = description,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = AppColors.textSecondary
        )
    }
}
