package com.vktrsansara.app.caveviewer.presentation.map.dialogs

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.PointLayer
import com.vktrsansara.app.caveviewer.domain.model.PointShape
import com.vktrsansara.app.caveviewer.presentation.components.AppColorPickerDialog
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.CheckerboardBox
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.presentation.components.DialogSaveButton
import com.vktrsansara.app.caveviewer.presentation.map.components.PointShapeMarker
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import kotlin.math.roundToInt

@Composable
fun LayerSettingsDialog(
    layer: PointLayer,
    existingNames: List<String> = emptyList(),
    paletteMode: String = "standard",
    onSave: (updatedLayer: PointLayer) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(layer.name) }
    val trimmed = name.trim()
    val isDuplicate = remember(trimmed, existingNames) {
        trimmed.isNotEmpty() && existingNames.any { it.equals(trimmed, ignoreCase = true) }
    }
    var shape by remember { mutableStateOf(layer.defaultShape) }
    var color by remember { mutableLongStateOf(layer.defaultColor) }
    var size by remember { mutableFloatStateOf(layer.defaultSize) }
    var showLabels by remember { mutableStateOf(layer.showLabels) }
    var isColorPickerOpen by remember { mutableStateOf(false) }
    var isShapePickerOpen by remember { mutableStateOf(false) }
    var isHelpOpen by remember { mutableStateOf(false) }

    if (isHelpOpen) {
        LayerSettingsHelpDialog(onDismiss = { isHelpOpen = false })
    }

    if (isShapePickerOpen) {
        PointShapePickerDialog(
            selectedShape = shape,
            markerColor = color,
            onShapeSelected = { selected ->
                shape = selected
                isShapePickerOpen = false
            },
            onDismiss = { isShapePickerOpen = false }
        )
    }

    if (isColorPickerOpen) {
        AppColorPickerDialog(
            initialColor = color,
            paletteMode = paletteMode,
            title = "Цвет слоя",
            onColorSelected = { selected ->
                color = selected
                isColorPickerOpen = false
            },
            onDismiss = { isColorPickerOpen = false }
        )
    }

    AppDialogContainer(
        title = "Настройки слоя: ${layer.name}",
        onDismissRequest = onDismiss,
        onInfoClick = { isHelpOpen = true },
        buttons = {
            DialogCancelButton(
                text = "Отмена",
                onClick = onDismiss
            )
            Spacer(modifier = Modifier.width(8.dp))
            DialogSaveButton(
                text = "Сохранить",
                enabled = trimmed.isNotBlank() && !isDuplicate,
                onClick = {
                    onSave(
                        layer.copy(
                            name = trimmed,
                            defaultShape = shape,
                            defaultColor = color,
                            defaultSize = size,
                            showLabels = showLabels
                        )
                    )
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Layer Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название слоя", fontSize = 13.sp) },
                singleLine = true,
                isError = isDuplicate,
                supportingText = if (isDuplicate) {
                    { Text("Слой с таким названием уже существует", color = Color(0xFFEF4444), fontSize = 11.sp) }
                } else null,
                textStyle = TextStyle(fontSize = 14.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isDuplicate) Color(0xFFEF4444) else AccentSkyBlue,
                    unfocusedBorderColor = if (isDuplicate) Color(0xFFEF4444) else AppColors.borderColor,
                    focusedLabelColor = if (isDuplicate) Color(0xFFEF4444) else AccentSkyBlue,
                    unfocusedLabelColor = if (isDuplicate) Color(0xFFEF4444) else AppColors.textSecondary,
                    errorBorderColor = Color(0xFFEF4444),
                    errorLabelColor = Color(0xFFEF4444),
                    errorSupportingTextColor = Color(0xFFEF4444),
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    cursorColor = AccentSkyBlue
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // 2. Default shape and color in one compact horizontal row
            Column {
                Text(
                    text = "Форма и цвет маркеров по умолчанию:",
                    fontSize = 12.5.sp,
                    color = AppColors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 2a. Shape Button (40 dp height)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppColors.bgSurface)
                            .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = AppColors.pressedColor),
                                onClick = { isShapePickerOpen = true }
                            )
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PointShapeMarker(
                            shape = shape,
                            color = Color(color.toInt()),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = shape.title,
                            fontSize = 12.5.sp,
                            color = AppColors.textPrimary,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Rounded.ArrowDropDown,
                            contentDescription = "Выбрать форму",
                            tint = AppColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // 2b. Color Button (40 dp height with checkerboard preview)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppColors.bgSurface)
                            .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = AppColors.pressedColor),
                                onClick = { isColorPickerOpen = true }
                            )
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CheckerboardBox(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                            squareSizeDp = 3f
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(color.toInt()))
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Цвет",
                            fontSize = 12.5.sp,
                            color = AppColors.textPrimary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Rounded.Palette,
                            contentDescription = "Выбрать цвет",
                            tint = AccentSkyBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 3. Point size on map
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Размер точек на карте:",
                        fontSize = 12.5.sp,
                        color = AppColors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${size.roundToInt()} dp",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentSkyBlue
                    )
                }
                Slider(
                    value = size,
                    onValueChange = { size = it },
                    valueRange = 4f..12f,
                    steps = 7,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentSkyBlue,
                        activeTrackColor = AccentSkyBlue,
                        inactiveTrackColor = AppColors.bgSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 4. Labels on map compact row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(AppColors.bgSurface)
                    .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = AppColors.pressedColor),
                        onClick = { showLabels = !showLabels }
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Отображать названия точек на карте",
                    fontSize = 12.5.sp,
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Checkbox(
                    checked = showLabels,
                    onCheckedChange = { showLabels = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AccentSkyBlue,
                        uncheckedColor = AppColors.borderColor,
                        checkmarkColor = Color.Black
                    ),
                    modifier = Modifier.scale(0.85f)
                )
            }
        }
    }
}

/**
 * Help dialog explaining layer style settings.
 */
@Composable
fun LayerSettingsHelpDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Настройки слоя",
        onDismissRequest = onDismiss,
        buttons = {
            DialogCancelButton(
                text = "Закрыть",
                onClick = onDismiss
            )
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            HelpItem(
                title = "Название слоя:",
                description = "Имя слоя для группировки точек по типам объектов (например, «Пикеты», «Опасности», «Навеска», «Вода»)."
            )

            HelpItem(
                title = "Форма маркера по умолчанию:",
                description = "Символ или спелеологический знак, который будет автоматически присваиваться каждой новой точке этого слоя."
            )

            HelpItem(
                title = "Цвет и прозрачность:",
                description = "Индивидуальный цвет и уровень прозрачности для маркеров слоя. Поддерживается расширенная палитра оттенков."
            )

            HelpItem(
                title = "Размер маркеров:",
                description = "Базовый экранный размер значков на карте (от 4 до 12 dp)."
            )

            HelpItem(
                title = "Подписи точек на карте:",
                description = "Включение или отключение текстовых названий рядом с маркерами на плане пещеры."
            )
        }
    }
}

@Composable
private fun HelpItem(title: String, description: String) {
    Column {
        Text(
            text = title,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = AccentSkyBlue
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            fontSize = 12.sp,
            color = AppColors.textSecondary,
            lineHeight = 17.sp
        )
    }
}

