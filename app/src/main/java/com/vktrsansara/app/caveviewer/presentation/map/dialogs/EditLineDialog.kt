package com.vktrsansara.app.caveviewer.presentation.map.dialogs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.measure.LineColorUtils
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldDateTimeUtils
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldDefinition
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldType
import com.vktrsansara.app.caveviewer.domain.model.LayerLine
import com.vktrsansara.app.caveviewer.domain.model.LineEnvironmentType
import com.vktrsansara.app.caveviewer.domain.model.LineLayer
import com.vktrsansara.app.caveviewer.domain.model.LineStyle
import com.vktrsansara.app.caveviewer.presentation.components.AppColorPickerDialog
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.presentation.map.components.LinePatternRenderer
import com.vktrsansara.app.caveviewer.presentation.components.DialogSaveButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Universal dialog for creating or editing a cave polyline (LayerLine) with difficulty scale, environment halo, style, and schema fields.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditLineDialog(
    line: LayerLine,
    layer: LineLayer,
    ppm: Double,
    paletteMode: String = "standard",
    onSave: (LayerLine) -> Unit,
    onDismiss: () -> Unit
) {
    val isNew = line.id == 0L
    var name by remember(line) { mutableStateOf(line.name) }
    var difficulty by remember(line) { mutableFloatStateOf(line.difficulty) }
    var environmentType by remember(line) {
        mutableStateOf(if (line.environmentType != LineEnvironmentType.NONE) line.environmentType else layer.defaultEnvironment)
    }
    var haloColor by remember(line) {
        mutableStateOf<Long?>(
            line.haloColor ?: (if (environmentType == LineEnvironmentType.CUSTOM) 0xFFEAB308 else environmentType.defaultHaloColor)
        )
    }
    var style by remember(line) { mutableStateOf(line.style) }
    var isCustomHaloPickerOpen by remember { mutableStateOf(false) }
    var isEnvironmentPickerOpen by remember { mutableStateOf(false) }
    var isHelpOpen by remember { mutableStateOf(false) }

    // Dynamic schema custom values
    val customValues = remember(line, layer) {
        mutableStateMapOf<String, String>().apply {
            layer.fieldsSchema.forEach { field ->
                val existing = line.customValues[field.key]
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

    if (isHelpOpen) {
        EditLineHelpDialog(onDismiss = { isHelpOpen = false })
    }

    if (isEnvironmentPickerOpen) {
        LineEnvironmentPickerDialog(
            selectedEnvironment = environmentType,
            customHaloColor = haloColor,
            showCustom = true,
            onEnvironmentSelected = { selected ->
                environmentType = selected
                if (selected == LineEnvironmentType.CUSTOM && haloColor == null) {
                    haloColor = 0xFFEAB308
                }
                isEnvironmentPickerOpen = false
            },
            onDismiss = { isEnvironmentPickerOpen = false }
        )
    }

    if (isCustomHaloPickerOpen) {
        AppColorPickerDialog(
            initialColor = haloColor ?: 0xFFEAB308,
            paletteMode = paletteMode,
            title = "Свой цвет ореола",
            onColorSelected = { selected ->
                haloColor = selected
                isCustomHaloPickerOpen = false
            },
            onDismiss = { isCustomHaloPickerOpen = false }
        )
    }

    AppDialogContainer(
        title = if (isNew) "Новая линия" else "Редактирование линии",
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
                enabled = name.isNotBlank() && line.points.size >= 2,
                onClick = {
                    val updatedLine = line.copy(
                        name = name.trim(),
                        difficulty = difficulty,
                        style = style,
                        environmentType = environmentType,
                        haloColor = if (environmentType == LineEnvironmentType.CUSTOM) haloColor else environmentType.defaultHaloColor,
                        customValues = customValues.toMap(),
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(updatedLine)
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Line Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название линии / хода") },
                placeholder = { Text("Например: Главный ход / Ход Метро") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentSkyBlue,
                    unfocusedBorderColor = AppColors.borderColor,
                    focusedLabelColor = AccentSkyBlue,
                    unfocusedLabelColor = AppColors.textSecondary,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    cursorColor = AccentSkyBlue
                )
            )

            // 2. Info Chip: Length and Vertex Count
            val verticesCount = line.points.size
            val lengthDisplay = if (ppm > 0.0) {
                "${String.format(Locale.US, "%.2f", line.lengthMeters)} м ($verticesCount верш.)"
            } else {
                "${String.format(Locale.US, "%.1f", line.lengthPx)} px ($verticesCount верш.)"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(AppColors.bgSurface)
                    .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Timeline,
                    contentDescription = "Длина",
                    tint = AccentSkyBlue,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Длина: $lengthDisplay",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentSkyBlue
                )
            }

            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)

            // 3. Difficulty Scale Slider (0.0 .. 8.0)
            val diffColor = LineColorUtils.getDifficultyColor(difficulty)
            val diffCategory = getDifficultyCategoryTitle(difficulty)

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Сложность хода:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.textPrimary
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", difficulty)} / 8.0 • $diffCategory",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = diffColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 1. Полоска с температурой цвета сверху слайдера
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF10B981),
                                    Color(0xFF84CC16),
                                    Color(0xFFEAB308),
                                    Color(0xFFF97316),
                                    Color(0xFFEA580C),
                                    Color(0xFF8B0000)
                                )
                            )
                        )
                )

                Slider(
                    value = difficulty,
                    onValueChange = { difficulty = (it * 10).roundToInt() / 10f },
                    valueRange = 0.0f..8.0f,
                    steps = 79,
                    colors = SliderDefaults.colors(
                        thumbColor = diffColor,
                        activeTrackColor = diffColor,
                        inactiveTrackColor = AppColors.borderColor
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                // 2. Под слайдером: Образец отображения
                Text(
                    text = "Образец отображения:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.textPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AppColors.bgSurface)
                        .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val effectivePatternColor = LineColorUtils.getHaloColor(environmentType, haloColor) ?: Color.White

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .padding(horizontal = 24.dp)
                    ) {
                        val midY = size.height / 2f
                        val strokeCorePx = layer.defaultWidth.coerceIn(1.5f, 6.0f) * density
                        val startOffset = Offset(0f, midY)
                        val endOffset = Offset(size.width, midY)

                        // 1. Draw Core Line with style
                        val pathEffect = when (style) {
                            LineStyle.SOLID -> null
                            LineStyle.DASHED -> {
                                val dashLen = (layer.defaultWidth * 2.5f + 6.dp.toPx()).coerceAtLeast(18f)
                                val dashGap = (layer.defaultWidth * 1.5f + 6.dp.toPx()).coerceAtLeast(16f)
                                PathEffect.dashPathEffect(floatArrayOf(dashLen, dashGap), 0f)
                            }
                            LineStyle.DOTTED -> {
                                val dotLen = 1f
                                val dotGap = (strokeCorePx + 3.5.dp.toPx()).coerceAtLeast(10f)
                                PathEffect.dashPathEffect(floatArrayOf(dotLen, dotGap), 0f)
                            }
                        }

                        val coreColor = if (layer.isHeatmapEnabled) diffColor else Color(layer.defaultColor.toInt())
                        drawLine(
                            color = coreColor,
                            start = startOffset,
                            end = endOffset,
                            strokeWidth = strokeCorePx,
                            pathEffect = pathEffect,
                            cap = StrokeCap.Round
                        )

                        // 2. Draw Vector Topographic Ticks / Hatches
                        if (environmentType != LineEnvironmentType.NONE) {
                            LinePatternRenderer.drawEnvironmentPattern(
                                drawScope = this,
                                screenPoints = listOf(startOffset, endOffset),
                                environmentType = environmentType,
                                patternColor = effectivePatternColor,
                                lineWidthPx = strokeCorePx
                            )
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)

            // 4. Topographic Environment Pattern
            Text(
                text = "Топографическая текстура хода (Среда):",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.textPrimary
            )

            val currentPatternColor = LineColorUtils.getHaloColor(environmentType, haloColor) ?: Color.White

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(AppColors.bgSurface)
                    .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = AppColors.pressedColor),
                        onClick = { isEnvironmentPickerOpen = true }
                    )
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    LineEnvironmentSampleBox(
                        environmentType = environmentType,
                        patternColor = currentPatternColor,
                        modifier = Modifier
                            .width(54.dp)
                            .height(26.dp)
                    )

                    Text(
                        text = if (environmentType == LineEnvironmentType.NONE) {
                            environmentType.title
                        } else {
                            "${environmentType.title.substringBefore(" /").substringBefore(" (")} ${environmentType.symbol}"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = "Выбрать текстуру среды",
                    tint = AccentSkyBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (environmentType == LineEnvironmentType.CUSTOM) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Цвет засечек:", fontSize = 12.sp, color = AppColors.textSecondary)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppColors.bgSurface)
                            .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = AppColors.pressedColor),
                                onClick = { isCustomHaloPickerOpen = true }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color((haloColor ?: 0xFFEAB308).toInt()))
                        )
                        Text(text = "Выбрать цвет", fontSize = 12.sp, color = AccentSkyBlue)
                    }
                }
            }

            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)

            // 5. Line Style
            Text(
                text = "Стиль линии:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.textPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LineStyle.entries.forEach { st ->
                    val isSelected = (style == st)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) AccentSkyBlue.copy(alpha = 0.15f) else AppColors.bgSurface)
                            .border(
                                1.dp,
                                if (isSelected) AccentSkyBlue else AppColors.borderColor,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = AppColors.pressedColor),
                                onClick = { style = st }
                            )
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = st.title,
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) AccentSkyBlue else AppColors.textPrimary
                        )
                    }
                }
            }

            // 6. Dynamic Custom Schema Fields
            if (layer.fieldsSchema.isNotEmpty()) {
                HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
                Text(
                    text = "Дополнительные параметры:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.textPrimary
                )

                layer.fieldsSchema.forEach { field ->
                    val curVal = customValues[field.key] ?: ""
                    DynamicLineFieldInput(
                        field = field,
                        currentValue = curVal,
                        onValueChange = { customValues[field.key] = it }
                    )
                }
            }
        }
    }
}

private fun getDifficultyCategoryTitle(difficulty: Float): String {
    val diff = difficulty.coerceIn(0.0f, 8.0f)
    return when {
        diff <= 1.0f -> "Просторный ход"
        diff <= 2.0f -> "Удобный ход"
        diff <= 3.5f -> "Низкий ход"
        diff <= 5.0f -> "Шкуродер"
        diff <= 6.5f -> "Узкий калибр"
        else -> "Экстрим / Сифон"
    }
}

/**
 * Dynamic input renderer for a single LayerFieldDefinition inside EditLineDialog.
 */
@Composable
private fun DynamicLineFieldInput(
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
                                                uncheckedColor = AppColors.textSecondary,
                                                checkmarkColor = AppColors.bgMain
                                            ),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = opt,
                                            fontSize = 13.sp,
                                            color = if (isChecked) AccentSkyBlue else AppColors.textPrimary,
                                            fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    val newSet = if (isChecked) selectedSet - opt else selectedSet + opt
                                    val newJoined = field.options.filter { newSet.contains(it) }.joinToString(", ")
                                    onValueChange(newJoined)
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

/**
 * Detailed help dialog for line parameters and settings.
 */
@Composable
fun EditLineHelpDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Параметры линии",
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
                title = "Название линии / хода:",
                description = "Уникальное наименование хода, зала, меандра, галереи или трассы навески для быстрой идентификации на схеме пещеры."
            )

            HelpItem(
                title = "Длина и вершины:",
                description = "Автоматический расчет реальной суммарной протяженности хода (в метрах при наличии калибровки масштаба или в пикселях px) и число зафиксированных вершин."
            )

            HelpItem(
                title = "Шкала сложности (0.0 .. 8.0):",
                description = "Оценка проходимости хода по градиентной тепловой гамме: от просторных галерей (0.0 — изумрудный) через удобные и низкие ходы (1.0..3.5 — салатовый/желтый), шкуродеры (5.0 — оранжевый) до экстремальных узких калибров и сифонов (8.0 — темно-бордовый)."
            )

            HelpItem(
                title = "Образец отображения:",
                description = "Интерактивный живой Canvas-предпросмотр, наглядно отображающий цвет сложности, выбранный стиль штриха и спелеологическую текстуру среды в реальном времени."
            )

            HelpItem(
                title = "Топографическая текстура хода (Среда UIS):",
                description = "Специальные картографические векторные значки спелеостандарта UIS/Therion вдоль сегментов хода:\n• Стоячая вода (──⬭──) — вытянутые овалы стоячей воды/озер вдоль хода\n• Водоток (──>──) — стрелки течения воды\n• Завал / Глыбы (──┼──) — поперечная гребенка\n• Глина / Грязь (──•──) — узловые крапинки\n• Сифон (──//──) — двойные наклонные штрихи\n• Лед / Наледь (──◇──) — ромбические маркеры\n• Загазованность (──///──) — тройные косые штрихи\n• Тяга воздуха (──>>──) — двойной шеврон ветра\n• Песок / Гравий (──⁖──) — три микро-точки треугольником\n• Перила / Навеска (──○──) — полый круг-узел\n• Узость / Калибр (──><──) — смыкающиеся зажимы\n• Уступ / Сброс (──┰──) — Т-образный зубчик уступа\n• Натеки / Кальцит (──⌒──) — дуга-чешуйка\n• Свой цвет — кастомный цвет ореола вокруг линии."
            )

            HelpItem(
                title = "Стиль линии:",
                description = "Сплошная — основной исследованный ход; Пунктир — предполагаемое или перспективное продолжение; Точки — второстепенные ответвления и исследуемые ходы."
            )

            HelpItem(
                title = "Дополнительные параметры:",
                description = "Пользовательские структурированные атрибуты, настроенные для данного слоя ходов (числовые замеры, текстовые описания, списки, флаги и даты)."
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
