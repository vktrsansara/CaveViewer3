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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldDateTimeUtils
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldDefinition
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldType
import com.vktrsansara.app.caveviewer.domain.model.LayerPoint
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
    var isShapePickerOpen by remember { mutableStateOf(false) }

    // Dynamic field values map
    val customValues = remember(point, layer) {
        mutableStateMapOf<String, String>().apply {
            // Seed from point or schema default
            layer.fieldsSchema.forEach { field ->
                val existing = point.customValues[field.key]
                if (existing != null) {
                    put(field.key, existing)
                } else if (field.defaultValue.isNotEmpty()) {
                    val resolvedDefault = if (field.type == LayerFieldType.DATETIME) {
                        LayerFieldDateTimeUtils.resolveDefaultValue(field.defaultValue)
                    } else {
                        field.defaultValue
                    }
                    put(field.key, resolvedDefault)
                }
            }
        }
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Point Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название точки", fontSize = 13.sp) },
                placeholder = { Text("Например: Пикет 1, Колодец 15м") },
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

            // 3. Point Shape and Color in one compact row
            Column {
                Text(
                    text = "Форма и цвет маркера:",
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
                    // Shape Button
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

                    // Color Button
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
                            imageVector = Icons.Rounded.ArrowDropDown,
                            contentDescription = "Выбрать цвет",
                            tint = AccentSkyBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
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
            val selectedSet = remember(currentValue) {
                currentValue.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
            }
            val displayValue = if (selectedSet.isEmpty()) "Не выбрано" else field.options.filter { selectedSet.contains(it) }.joinToString(", ")

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
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayValue,
                            fontSize = 13.sp,
                            color = if (selectedSet.isNotEmpty()) AppColors.textPrimary else AppColors.textSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Rounded.ArrowDropDown,
                            contentDescription = "Выбрать варианты",
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
                            val isChecked = selectedSet.contains(opt)
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = null,
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = AccentSkyBlue,
                                                uncheckedColor = AppColors.borderColor,
                                                checkmarkColor = Color.Black
                                            ),
                                            modifier = Modifier.scale(0.8f)
                                        )
                                        Text(
                                            text = opt,
                                            color = if (isChecked) AccentSkyBlue else AppColors.textPrimary,
                                            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    }
                                },
                                onClick = {
                                    val newSet = if (isChecked) selectedSet - opt else selectedSet + opt
                                    val newString = field.options.filter { newSet.contains(it) }.joinToString(", ")
                                    onValueChange(newString)
                                }
                            )
                        }
                    }
                }
            }
        }
        LayerFieldType.DATETIME -> {
            OutlinedTextField(
                value = currentValue,
                onValueChange = onValueChange,
                label = { Text(field.name) },
                placeholder = { Text("ДД.ММ.ГГГГ ЧЧ:ММ") },
                singleLine = true,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val nowVal = LayerFieldDateTimeUtils.resolveDefaultValue(
                                field.defaultValue.ifEmpty { LayerFieldDateTimeUtils.DEFAULT_NOW_DATETIME }
                            )
                            onValueChange(nowVal)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = "Вставить текущее время",
                            tint = AccentSkyBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
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
    }
}
