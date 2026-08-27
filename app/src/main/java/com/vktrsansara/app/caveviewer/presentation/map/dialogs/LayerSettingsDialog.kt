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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.PointLayer
import com.vktrsansara.app.caveviewer.domain.model.PointShape
import com.vktrsansara.app.caveviewer.presentation.components.AppColorPickerDialog
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.presentation.components.DialogSaveButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import kotlin.math.roundToInt

@Composable
fun LayerSettingsDialog(
    layer: PointLayer,
    existingNames: List<String> = emptyList(),
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

    if (isColorPickerOpen) {
        AppColorPickerDialog(
            initialColor = color,
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Layer Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название слоя") },
                singleLine = true,
                isError = isDuplicate,
                supportingText = if (isDuplicate) {
                    { Text("Слой с таким названием уже существует", color = Color(0xFFEF4444)) }
                } else null,
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

            // 2. Default shape (7 buttons, 40x40 dp)
            Column {
                Text(
                    text = "Форма маркера по умолчанию:",
                    fontSize = 12.5.sp,
                    color = AppColors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PointShape.entries.forEach { pointShape ->
                        val isSelected = shape == pointShape
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) AccentSkyBlue.copy(alpha = 0.15f) else AppColors.bgSurface)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) AccentSkyBlue else AppColors.borderColor,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = AppColors.pressedColor),
                                    onClick = { shape = pointShape }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            PointShapeMarker(
                                shape = pointShape,
                                color = Color(color.toInt()),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 3. Layer Color
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(AppColors.bgSurface)
                    .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = AppColors.pressedColor),
                        onClick = { isColorPickerOpen = true }
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Цвет слоя",
                        fontSize = 13.5.sp,
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Нажмите для выбора цвета",
                        fontSize = 11.5.sp,
                        color = AppColors.textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(color.toInt()))
                        .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                )
            }

            // 4. Point size on map
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
                        fontSize = 13.sp,
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

            // 5. Labels on map
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
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Отображать названия точек на карте",
                    fontSize = 13.sp,
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
                    )
                )
            }
        }
    }
}
