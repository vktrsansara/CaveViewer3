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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldDefinition
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldType
import com.vktrsansara.app.caveviewer.domain.model.LayerPoint
import com.vktrsansara.app.caveviewer.domain.model.PointLayer
import com.vktrsansara.app.caveviewer.domain.model.PointShape
import com.vktrsansara.app.caveviewer.presentation.components.AppColorPickerDialog
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.presentation.components.DialogSaveButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import java.util.Locale

/**
 * Universal dynamic point creation and editing dialog based on layer's schema.
 */
@Composable
fun EditPointDialog(
    point: LayerPoint,
    layer: PointLayer,
    onSave: (LayerPoint) -> Unit,
    onDismiss: () -> Unit
) {
    val isNew = point.id == 0L
    var name by remember(point) { mutableStateOf(point.name) }
    var shape by remember(point) { mutableStateOf(point.shape) }
    var color by remember(point) { mutableLongStateOf(point.color) }
    var isColorPickerOpen by remember { mutableStateOf(false) }

    // Dynamic field values map
    val customValues = remember(point, layer) {
        mutableStateMapOf<String, String>().apply {
            // Seed from point or schema default
            layer.fieldsSchema.forEach { field ->
                val existing = point.customValues[field.key]
                if (existing != null) {
                    put(field.key, existing)
                } else if (field.defaultValue.isNotEmpty()) {
                    put(field.key, field.defaultValue)
                }
            }
        }
    }

    if (isColorPickerOpen) {
        AppColorPickerDialog(
            initialColor = color,
            title = "Цвет точки",
            onColorSelected = { selected ->
                color = selected
                isColorPickerOpen = false
            },
            onDismiss = { isColorPickerOpen = false }
        )
    }

    AppDialogContainer(
        title = if (isNew) "Новая точка" else "Редактирование точки",
        onDismissRequest = onDismiss,
        buttons = {
            DialogCancelButton(
                text = "Отмена",
                onClick = onDismiss
            )
            Spacer(modifier = Modifier.width(8.dp))
            DialogSaveButton(
                text = "Сохранить",
                enabled = name.isNotBlank(),
                onClick = {
                    val updatedPoint = point.copy(
                        name = name.trim(),
                        shape = shape,
                        color = color,
                        customValues = customValues.toMap(),
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(updatedPoint)
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Point Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название точки *") },
                placeholder = { Text("Например: ПК-0, Навеска 12м, Осыпь") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentSkyBlue,
                    unfocusedBorderColor = AppColors.borderColor,
                    focusedLabelColor = AccentSkyBlue,
                    unfocusedLabelColor = AppColors.textSecondary,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    cursorColor = AccentSkyBlue
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // 2. Coordinates label
            Text(
                text = "Координаты на растре: X = ${String.format(Locale.US, "%.1f", point.x)} px, Y = ${String.format(Locale.US, "%.1f", point.y)} px",
                fontSize = 11.5.sp,
                color = AppColors.textSecondary
            )

            // 3. Point Shape (7 buttons, 40x40 dp)
            Column {
                Text(
                    text = "Форма маркера:",
                    fontSize = 12.5.sp,
                    color = AppColors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
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

            // 4. Point Color
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
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Индивидуальный цвет",
                        fontSize = 13.sp,
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Нажмите для изменения цвета",
                        fontSize = 11.sp,
                        color = AppColors.textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(color.toInt()))
                        .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                )
            }

            // 5. Dynamic fields based on layer.fieldsSchema
            if (layer.fieldsSchema.isNotEmpty()) {
                HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
                Text(
                    text = "Дополнительные свойства слоя:",
                    fontSize = 12.5.sp,
                    color = AccentSkyBlue,
                    fontWeight = FontWeight.SemiBold
                )

                layer.fieldsSchema.forEach { field ->
                    DynamicFieldInput(
                        field = field,
                        currentValue = customValues[field.key] ?: "",
                        onValueChange = { customValues[field.key] = it }
                    )
                }
            }
        }
    }
}

/**
 * Dynamic input renderer for a single LayerFieldDefinition.
 */
@Composable
private fun DynamicFieldInput(
    field: LayerFieldDefinition,
    currentValue: String,
    onValueChange: (String) -> Unit
) {
    when (field.type) {
        LayerFieldType.TEXT -> {
            OutlinedTextField(
                value = currentValue,
                onValueChange = onValueChange,
                label = { Text(field.name) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentSkyBlue,
                    unfocusedBorderColor = AppColors.borderColor,
                    focusedLabelColor = AccentSkyBlue,
                    unfocusedLabelColor = AppColors.textSecondary,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    cursorColor = AccentSkyBlue
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        LayerFieldType.NUMBER -> {
            OutlinedTextField(
                value = currentValue,
                onValueChange = onValueChange,
                label = { Text(field.name) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentSkyBlue,
                    unfocusedBorderColor = AppColors.borderColor,
                    focusedLabelColor = AccentSkyBlue,
                    unfocusedLabelColor = AppColors.textSecondary,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    cursorColor = AccentSkyBlue
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        LayerFieldType.BOOLEAN -> {
            val isChecked = currentValue.toBooleanStrictOrNull() ?: false
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(AppColors.bgSurface)
                    .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = AppColors.pressedColor),
                        onClick = { onValueChange((!isChecked).toString()) }
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = field.name,
                    fontSize = 13.sp,
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = isChecked,
                    onCheckedChange = { onValueChange(it.toString()) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentSkyBlue,
                        checkedTrackColor = AccentSkyBlue.copy(alpha = 0.5f),
                        uncheckedThumbColor = AppColors.textSecondary,
                        uncheckedTrackColor = AppColors.bgCard
                    ),
                    modifier = Modifier.scale(0.85f)
                )
            }
        }
        LayerFieldType.SELECT -> {
            var isDropdownExpanded by remember { mutableStateOf(false) }
            val displayValue = currentValue.ifEmpty { field.options.firstOrNull() ?: "" }

            Column {
                Text(
                    text = field.name,
                    fontSize = 12.sp,
                    color = AppColors.textSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(AppColors.bgSurface)
                        .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                        .clickable { isDropdownExpanded = true }
                        .padding(horizontal = 12.dp, vertical = 11.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayValue.ifEmpty { "Выберите вариант" },
                            fontSize = 13.5.sp,
                            color = if (displayValue.isNotEmpty()) AppColors.textPrimary else AppColors.textSecondary
                        )
                        Icon(
                            imageVector = Icons.Rounded.ArrowDropDown,
                            contentDescription = "Выбрать вариант",
                            tint = AccentSkyBlue
                        )
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier
                            .background(AppColors.bgCard)
                            .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                    ) {
                        field.options.forEach { opt ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = opt,
                                        color = if (opt == displayValue) AccentSkyBlue else AppColors.textPrimary,
                                        fontWeight = if (opt == displayValue) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onValueChange(opt)
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
