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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.measure.LineColorUtils
import com.vktrsansara.app.caveviewer.domain.model.LineEnvironmentType
import com.vktrsansara.app.caveviewer.domain.model.LineLayer
import com.vktrsansara.app.caveviewer.presentation.components.AppColorPickerDialog
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.CheckerboardBox
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.presentation.components.DialogSaveButton
import com.vktrsansara.app.caveviewer.presentation.map.components.LinePatternRenderer
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Modal dialog for editing Line Layer appearance and default settings.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LineLayerSettingsDialog(
    layer: LineLayer,
    existingNames: List<String> = emptyList(),
    onSave: (updatedLayer: LineLayer) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(layer.name) }
    val trimmed = name.trim()
    val isDuplicate = remember(trimmed, existingNames) {
        trimmed.isNotEmpty() && existingNames.any { it.equals(trimmed, ignoreCase = true) }
    }

    var width by remember { mutableFloatStateOf(layer.defaultWidth) }
    var isHeatmapEnabled by remember { mutableStateOf(layer.isHeatmapEnabled) }
    var defaultColor by remember { mutableLongStateOf(layer.defaultColor) }
    var environmentType by remember { mutableStateOf(layer.defaultEnvironment) }
    var customHaloColor by remember { mutableLongStateOf(0xFFEAB308) }

    var isColorPickerOpen by remember { mutableStateOf(false) }
    var isCustomHaloPickerOpen by remember { mutableStateOf(false) }
    var isHelpOpen by remember { mutableStateOf(false) }

    if (isHelpOpen) {
        LineLayerSettingsHelpDialog(onDismiss = { isHelpOpen = false })
    }

    if (isColorPickerOpen) {
        AppColorPickerDialog(
            initialColor = defaultColor,
            title = "Цвет слоя",
            onColorSelected = { selected ->
                defaultColor = selected
                isColorPickerOpen = false
            },
            onDismiss = { isColorPickerOpen = false }
        )
    }

    if (isCustomHaloPickerOpen) {
        AppColorPickerDialog(
            initialColor = customHaloColor,
            title = "Свой цвет засечек",
            onColorSelected = { selected ->
                customHaloColor = selected
                isCustomHaloPickerOpen = false
            },
            onDismiss = { isCustomHaloPickerOpen = false }
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
                enabled = trimmed.isNotEmpty() && !isDuplicate,
                onClick = {
                    onSave(
                        layer.copy(
                            name = trimmed,
                            defaultWidth = width,
                            isHeatmapEnabled = isHeatmapEnabled,
                            defaultColor = defaultColor,
                            defaultEnvironment = environmentType
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
                label = { Text("Название слоя") },
                isError = isDuplicate || trimmed.isEmpty(),
                supportingText = {
                    if (isDuplicate) {
                        Text("Слой с таким названием уже существует", color = Color(0xFFEF4444))
                    } else if (trimmed.isEmpty()) {
                        Text("Название не может быть пустым", color = Color(0xFFEF4444))
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentSkyBlue,
                    unfocusedBorderColor = AppColors.borderColor,
                    focusedLabelColor = AccentSkyBlue,
                    unfocusedLabelColor = AppColors.textSecondary,
                    cursorColor = AccentSkyBlue,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary
                )
            )

            // 2. Line Width Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Толщина линии:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.textPrimary
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", width)} dp",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentSkyBlue
                    )
                }
                Slider(
                    value = width,
                    onValueChange = { width = (it * 2).roundToInt() / 2f },
                    valueRange = 1.5f..8.0f,
                    steps = 12,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentSkyBlue,
                        activeTrackColor = AccentSkyBlue,
                        inactiveTrackColor = AppColors.borderColor
                    )
                )
            }

            // 3. Live Line Preview Canvas (Positioned directly under width slider)
            Text(
                text = "Предпросмотр линии слоя:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.textPrimary
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AppColors.bgSurface)
                    .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                val patternColor = LineColorUtils.getHaloColor(environmentType, customHaloColor) ?: Color.White

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .padding(horizontal = 20.dp)
                ) {
                    val midY = size.height / 2f
                    val strokeCorePx = width * density
                    val startOffset = Offset(0f, midY)
                    val endOffset = Offset(size.width, midY)

                    // 1. Draw Core Line (Heatmap gradient or Single Color)
                    if (isHeatmapEnabled) {
                        val gradientBrush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFF10B981), // Green
                                Color(0xFF84CC16), // Lime
                                Color(0xFFEAB308), // Yellow
                                Color(0xFFF97316), // Orange
                                Color(0xFFEA580C), // Orange-Red
                                Color(0xFF8B0000)  // Dark Red
                            )
                        )
                        drawLine(
                            brush = gradientBrush,
                            start = startOffset,
                            end = endOffset,
                            strokeWidth = strokeCorePx,
                            cap = StrokeCap.Round
                        )
                    } else {
                        drawLine(
                            color = Color(defaultColor.toInt()),
                            start = startOffset,
                            end = endOffset,
                            strokeWidth = strokeCorePx,
                            cap = StrokeCap.Round
                        )
                    }

                    // 2. Draw Topographic Vector Ticks / Hatches
                    if (environmentType != LineEnvironmentType.NONE) {
                        LinePatternRenderer.drawEnvironmentPattern(
                            drawScope = this,
                            screenPoints = listOf(startOffset, endOffset),
                            environmentType = environmentType,
                            patternColor = patternColor,
                            lineWidthPx = strokeCorePx
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)

            // 4. Color Mode Options
            Text(
                text = "Режим цвета линии:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.textPrimary
            )

            // Radio 1: Heatmap (0.0..8.0)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isHeatmapEnabled) AccentSkyBlue.copy(alpha = 0.08f) else AppColors.bgSurface)
                    .border(
                        1.dp,
                        if (isHeatmapEnabled) AccentSkyBlue.copy(alpha = 0.5f) else AppColors.borderColor,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = AppColors.pressedColor),
                        onClick = { isHeatmapEnabled = true }
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RadioButton(
                    selected = isHeatmapEnabled,
                    onClick = { isHeatmapEnabled = true },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = AccentSkyBlue,
                        unselectedColor = AppColors.textSecondary
                    ),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Тепловая шкала сложности (0.0 .. 8.0)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isHeatmapEnabled) AccentSkyBlue else AppColors.textPrimary
                )
            }

            // Radio 2: Single Color
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (!isHeatmapEnabled) AccentSkyBlue.copy(alpha = 0.08f) else AppColors.bgSurface)
                    .border(
                        1.dp,
                        if (!isHeatmapEnabled) AccentSkyBlue.copy(alpha = 0.5f) else AppColors.borderColor,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = AppColors.pressedColor),
                        onClick = { isHeatmapEnabled = false }
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RadioButton(
                        selected = !isHeatmapEnabled,
                        onClick = { isHeatmapEnabled = false },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = AccentSkyBlue,
                            unselectedColor = AppColors.textSecondary
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Единый цвет слоя",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (!isHeatmapEnabled) AccentSkyBlue else AppColors.textPrimary
                    )
                }

                // Color picker trigger
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AppColors.bgCard)
                        .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.pressedColor),
                            onClick = { isColorPickerOpen = true }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CheckerboardBox(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .border(1.dp, AppColors.borderColor, CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(defaultColor.toInt()))
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.Palette,
                        contentDescription = "Выбрать цвет",
                        tint = AccentSkyBlue,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)

            // 5. Topographic Environment Pattern Selection (UIS / Therion Standard)
            Text(
                text = "Топографическая текстура хода (Среда):",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.textPrimary
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LineEnvironmentType.entries.filter { it != LineEnvironmentType.CUSTOM }.forEach { env ->
                    val isSelected = (environmentType == env)
                    val envColor = env.defaultHaloColor?.let { Color(it.toInt()) } ?: AccentSkyBlue

                    Row(
                        modifier = Modifier
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
                                onClick = { environmentType = env }
                            )
                            .padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (env != LineEnvironmentType.NONE) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(envColor)
                            )
                        }

                        Text(
                            text = if (env == LineEnvironmentType.NONE) env.title else "${env.title.substringBefore(" /").substringBefore(" (")} ${env.symbol}",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) AccentSkyBlue else AppColors.textPrimary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Help dialog for Line Layer Settings.
 */
@Composable
fun LineLayerSettingsHelpDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Настройки слоя линий",
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
                title = "Толщина линии:",
                description = "Задает базовую ширину отрисовки отрезков ходов этого слоя на плане (от 1.5 до 8.0 dp)."
            )

            HelpItem(
                title = "Тепловая шкала сложности (0.0 .. 8.0):",
                description = "Каждый ход в слое будет автоматически менять цвет от зеленого к бордово-красному в зависимости от указанной сложности прохождения."
            )

            HelpItem(
                title = "Единый цвет слоя:",
                description = "Отключает тепловую шкалу и окрашивает все линии слоя в фиксированный выбранный цвет."
            )

            HelpItem(
                title = "Топографическая текстура хода (Среда UIS):",
                description = "Векторные спелеологические обозначения стандарта UIS/Therion вдоль ходов: водоток, завалы, глина, сифоны, лед, загазованность, тяга воздуха (ветер), песок/осыпь, перила/навеска, узости/калибры, уступы/сбросы и натеки/кальцит."
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
