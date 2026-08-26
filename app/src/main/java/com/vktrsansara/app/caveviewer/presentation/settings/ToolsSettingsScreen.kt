package com.vktrsansara.app.caveviewer.presentation.settings

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.AppSettings
import com.vktrsansara.app.caveviewer.domain.model.MapMetadata
import com.vktrsansara.app.caveviewer.presentation.components.AppColorPickerDialog
import com.vktrsansara.app.caveviewer.presentation.components.CheckerboardBox
import com.vktrsansara.app.caveviewer.presentation.map.dialogs.GridHelpDialog
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

private val GreenInfoColor = Color(0xFF10B981)

/**
 * Screen for configuring map tools, cursor, grid overlay, and color picker palette.
 */
@Composable
fun ToolsSettingsScreen(
    settings: AppSettings,
    activeMetadata: MapMetadata? = null,
    onCursorShowChanged: (Boolean) -> Unit,
    onCursorTypeChanged: (Int) -> Unit,
    onCursorColorChanged: (Long) -> Unit,
    onGridSizeModeChanged: (String) -> Unit = {},
    onGridCustomSizeChanged: (Double) -> Unit = {},
    onGridColorChanged: (Long) -> Unit = {},
    onColorPaletteModeChanged: (String) -> Unit = {},
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isCursorHelpDialogVisible by remember { mutableStateOf(false) }
    var isCursorColorPickerVisible by remember { mutableStateOf(false) }
    var isGridHelpDialogVisible by remember { mutableStateOf(false) }
    var isGridColorPickerVisible by remember { mutableStateOf(false) }
    var isColorPaletteHelpDialogVisible by remember { mutableStateOf(false) }

    if (isCursorHelpDialogVisible) {
        CursorHelpDialog(onDismiss = { isCursorHelpDialogVisible = false })
    }

    if (isCursorColorPickerVisible) {
        AppColorPickerDialog(
            initialColor = settings.cursorColor,
            paletteMode = settings.colorPaletteMode,
            title = "Цвет курсора",
            onColorSelected = onCursorColorChanged,
            onDismiss = { isCursorColorPickerVisible = false }
        )
    }

    if (isGridHelpDialogVisible) {
        GridHelpDialog(onDismiss = { isGridHelpDialogVisible = false })
    }

    if (isGridColorPickerVisible) {
        AppColorPickerDialog(
            initialColor = settings.gridColor,
            paletteMode = settings.colorPaletteMode,
            title = "Цвет сетки",
            onColorSelected = onGridColorChanged,
            onDismiss = { isGridColorPickerVisible = false }
        )
    }

    if (isColorPaletteHelpDialogVisible) {
        ColorPaletteHelpDialog(onDismiss = { isColorPaletteHelpDialogVisible = false })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bgMain)
    ) {
        // Screen Header
        ToolsSettingsHeader(
            title = "Инструменты",
            onNavigateBack = onNavigateBack
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Cursor Settings
            SettingsSectionCard {
                // Header of Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Курсор",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textPrimary
                    )

                    IconButton(
                        onClick = { isCursorHelpDialogVisible = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Справка по курсору",
                            tint = GreenInfoColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                val switchColors = SwitchDefaults.colors(
                    checkedThumbColor = AppColors.textPrimary,
                    checkedTrackColor = AppColors.accent,
                    uncheckedThumbColor = AppColors.textSecondary,
                    uncheckedTrackColor = AppColors.bgSurface,
                    uncheckedBorderColor = AppColors.borderColor
                )

                // Switch "Always show"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.pressedColor),
                            onClick = { onCursorShowChanged(!settings.cursorShow) }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Всегда отображать",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.textPrimary
                    )

                    Switch(
                        checked = settings.cursorShow,
                        onCheckedChange = onCursorShowChanged,
                        modifier = Modifier.scale(0.85f),
                        colors = switchColors
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
                Spacer(modifier = Modifier.height(10.dp))

                // Subtitle "Cursor Appearance"
                Text(
                    text = "Вид курсора",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.textPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Row of 6 cursor presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (type in 1..6) {
                        CursorTypeButton(
                            type = type,
                            isSelected = settings.cursorType == type,
                            onClick = { onCursorTypeChanged(type) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
                Spacer(modifier = Modifier.height(10.dp))

                // Row "Выбор цвета:" with Live Cursor Preview block + Color Picker button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Выбор цвета:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.textPrimary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Live Selected Cursor Preview on Black/White Checkerboard Background
                        CheckerboardBox(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp)),
                            squareSizeDp = 4f
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CursorPreviewCanvas(
                                    type = settings.cursorType,
                                    cursorColor = Color(settings.cursorColor),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // 2. Color Picker Button on Black/White Checkerboard
                        CheckerboardBox(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = AppColors.pressedColor),
                                    onClick = { isCursorColorPickerVisible = true }
                                ),
                            squareSizeDp = 4f
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Color(settings.cursorColor))
                                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            // Card 2: Grid Settings
            SettingsSectionCard {
                // Header of Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Сетка",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textPrimary
                    )

                    IconButton(
                        onClick = { isGridHelpDialogVisible = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Справка по сетке",
                            tint = GreenInfoColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                val radioButtonColors = RadioButtonDefaults.colors(
                    selectedColor = AccentSkyBlue,
                    unselectedColor = AppColors.textSecondary
                )

                val suffix = if (activeMetadata != null && activeMetadata.pixelsPerMeter > 0.0) "м" else "px"

                var customSizeInput by remember(settings.gridCustomSize) {
                    val sizeVal = settings.gridCustomSize
                    val formatted = if (sizeVal % 1.0 == 0.0) sizeVal.toInt().toString() else sizeVal.toString()
                    mutableStateOf(formatted)
                }

                // Radio 1: "Из метаданных карты" (clean label without subtitle)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.pressedColor),
                            onClick = { onGridSizeModeChanged("metadata") }
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = settings.gridSizeMode == "metadata",
                        onClick = { onGridSizeModeChanged("metadata") },
                        colors = radioButtonColors,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Из метаданных карты",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Radio 2: "Свое значение"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.pressedColor),
                            onClick = { onGridSizeModeChanged("custom") }
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        RadioButton(
                            selected = settings.gridSizeMode == "custom",
                            onClick = { onGridSizeModeChanged("custom") },
                            colors = radioButtonColors,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Свое значение",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.textPrimary
                        )
                    }

                    // Numeric Input Box
                    Row(
                        modifier = Modifier
                            .width(100.dp)
                            .height(34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (settings.gridSizeMode == "custom") AppColors.bgSurface else AppColors.bgSurface.copy(alpha = 0.5f))
                            .border(
                                width = 1.dp,
                                color = if (settings.gridSizeMode == "custom") AccentSkyBlue else AppColors.borderColor,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = customSizeInput,
                            onValueChange = { newVal ->
                                customSizeInput = newVal
                                val parsed = newVal.replace(',', '.').toDoubleOrNull()
                                if (parsed != null && parsed > 0.0) {
                                    onGridCustomSizeChanged(parsed)
                                }
                            },
                            enabled = settings.gridSizeMode == "custom",
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (settings.gridSizeMode == "custom") AppColors.textPrimary else AppColors.textSecondary,
                                textAlign = TextAlign.End
                            ),
                            cursorBrush = SolidColor(AccentSkyBlue),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = suffix,
                            fontSize = 12.sp,
                            color = AppColors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
                Spacer(modifier = Modifier.height(10.dp))

                // Section "Выбор цвета:"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Выбор цвета:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.textPrimary
                    )

                    // Black & White Checkerboard Button with Live Circle
                    CheckerboardBox(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = AppColors.pressedColor),
                                onClick = { isGridColorPickerVisible = true }
                            ),
                        squareSizeDp = 4f
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(settings.gridColor))
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            )
                        }
                    }
                }
            }

            // Card 3: Color Picker Palette Settings
            SettingsSectionCard {
                // Header of Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Цвета Колорпикера:",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textPrimary
                    )

                    IconButton(
                        onClick = { isColorPaletteHelpDialogVisible = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Справка по цветам колорпикера",
                            tint = GreenInfoColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                val radioButtonColors = RadioButtonDefaults.colors(
                    selectedColor = AccentSkyBlue,
                    unselectedColor = AppColors.textSecondary
                )

                // Radio 1: "Стандартная" (clean label without subtitle)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.pressedColor),
                            onClick = { onColorPaletteModeChanged("standard") }
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = settings.colorPaletteMode == "standard",
                        onClick = { onColorPaletteModeChanged("standard") },
                        colors = radioButtonColors,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Стандартная",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Radio 2: "Приглушенные" (clean label without subtitle)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.pressedColor),
                            onClick = { onColorPaletteModeChanged("muted") }
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = settings.colorPaletteMode == "muted",
                        onClick = { onColorPaletteModeChanged("muted") },
                        colors = radioButtonColors,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Приглушенные",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.textPrimary
                    )
                }
            }
        }
    }
}

/**
 * Top bar header for Tools Settings screen.
 */
@Composable
private fun ToolsSettingsHeader(
    title: String,
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(52.dp)
            .background(AppColors.bgMain)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = AccentSkyBlue
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.textPrimary
        )
    }
}

/**
 * Card container with standard border and background styling.
 */
@Composable
private fun SettingsSectionCard(
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.bgCard)
            .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        content()
    }
}

/**
 * Cursor shape preview button in the preset list.
 */
@Composable
private fun CursorTypeButton(
    type: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) AccentSkyBlue else AppColors.borderColor
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val shapeColor = if (isSelected) AccentSkyBlue else AppColors.textPrimary

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(AppColors.bgSurface)
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(6.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = AppColors.pressedColor),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        CursorShapeCanvas(
            type = type,
            color = shapeColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Canvas drawing the cursor shape in neutral/accent colors for preset buttons.
 */
@Composable
private fun CursorShapeCanvas(
    type: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val strokePx = 1.5.dp.toPx()

        when (type) {
            1 -> {
                // Type 1: Solid Cross
                drawLine(color, Offset(cx, 2f), Offset(cx, size.height - 2f), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(color, Offset(2f, cy), Offset(size.width - 2f, cy), strokeWidth = strokePx, cap = StrokeCap.Round)
            }
            2 -> {
                // Type 2: Dashed Plus with Center Dot
                drawLine(color, Offset(cx, 2f), Offset(cx, cy - 4f), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(color, Offset(cx, cy + 4f), Offset(cx, size.height - 2f), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(color, Offset(2f, cy), Offset(cx - 4f, cy), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(color, Offset(cx + 4f, cy), Offset(size.width - 2f, cy), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawCircle(color, radius = 2.dp.toPx(), center = Offset(cx, cy))
            }
            3 -> {
                // Type 3: Center Dot
                drawCircle(color, radius = 3.5.dp.toPx(), center = Offset(cx, cy))
            }
            4 -> {
                // Type 4: Diagonal X
                drawLine(color, Offset(4f, 4f), Offset(size.width - 4f, size.height - 4f), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width - 4f, 4f), Offset(4f, size.height - 4f), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawCircle(color, radius = 1.8.dp.toPx(), center = Offset(cx, cy))
            }
            5 -> {
                // Type 5: Circle with Center Dot
                drawCircle(color, radius = 7.dp.toPx(), center = Offset(cx, cy), style = Stroke(width = strokePx))
                drawCircle(color, radius = 2.dp.toPx(), center = Offset(cx, cy))
            }
            6 -> {
                // Type 6: Circle Target with Crosshairs
                drawCircle(color, radius = 6.5.dp.toPx(), center = Offset(cx, cy), style = Stroke(width = strokePx))
                drawCircle(color, radius = 1.8.dp.toPx(), center = Offset(cx, cy))
                drawLine(color, Offset(cx, 1f), Offset(cx, cy - 6.5.dp.toPx()), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(color, Offset(cx, cy + 6.5.dp.toPx()), Offset(cx, size.height - 1f), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(color, Offset(1f, cy), Offset(cx - 6.5.dp.toPx(), cy), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(color, Offset(cx + 6.5.dp.toPx(), cy), Offset(size.width - 1f, cy), strokeWidth = strokePx, cap = StrokeCap.Round)
            }
        }
    }
}

/**
 * Live canvas drawing the selected cursor with user's selected color and transparency on checkerboard.
 */
@Composable
private fun CursorPreviewCanvas(
    type: Int,
    cursorColor: Color,
    modifier: Modifier = Modifier
) {
    val shadowAlpha = 0.35f * cursorColor.alpha
    val shadowColor = Color.Black.copy(alpha = shadowAlpha)

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val strokePx = 1.5.dp.toPx()

        when (type) {
            1 -> {
                if (shadowAlpha > 0f) {
                    drawLine(shadowColor, Offset(cx, 1f), Offset(cx, size.height - 1f), strokeWidth = strokePx + 1f, cap = StrokeCap.Round)
                    drawLine(shadowColor, Offset(1f, cy), Offset(size.width - 1f, cy), strokeWidth = strokePx + 1f, cap = StrokeCap.Round)
                }
                drawLine(cursorColor, Offset(cx, 2f), Offset(cx, size.height - 2f), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(cursorColor, Offset(2f, cy), Offset(size.width - 2f, cy), strokeWidth = strokePx, cap = StrokeCap.Round)
            }
            2 -> {
                if (shadowAlpha > 0f) {
                    drawCircle(shadowColor, radius = 2.5.dp.toPx(), center = Offset(cx, cy))
                }
                drawLine(cursorColor, Offset(cx, 2f), Offset(cx, cy - 4f), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(cursorColor, Offset(cx, cy + 4f), Offset(cx, size.height - 2f), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(cursorColor, Offset(2f, cy), Offset(cx - 4f, cy), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(cursorColor, Offset(cx + 4f, cy), Offset(size.width - 2f, cy), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawCircle(cursorColor, radius = 2.dp.toPx(), center = Offset(cx, cy))
            }
            3 -> {
                if (shadowAlpha > 0f) {
                    drawCircle(shadowColor, radius = 4.2.dp.toPx(), center = Offset(cx, cy))
                }
                drawCircle(cursorColor, radius = 3.5.dp.toPx(), center = Offset(cx, cy))
            }
            4 -> {
                if (shadowAlpha > 0f) {
                    drawCircle(shadowColor, radius = 2.4.dp.toPx(), center = Offset(cx, cy))
                }
                drawLine(cursorColor, Offset(4f, 4f), Offset(size.width - 4f, size.height - 4f), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(cursorColor, Offset(size.width - 4f, 4f), Offset(4f, size.height - 4f), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawCircle(cursorColor, radius = 1.8.dp.toPx(), center = Offset(cx, cy))
            }
            5 -> {
                if (shadowAlpha > 0f) {
                    drawCircle(shadowColor, radius = 7.dp.toPx(), center = Offset(cx, cy), style = Stroke(width = strokePx + 1f))
                    drawCircle(shadowColor, radius = 2.5.dp.toPx(), center = Offset(cx, cy))
                }
                drawCircle(cursorColor, radius = 7.dp.toPx(), center = Offset(cx, cy), style = Stroke(width = strokePx))
                drawCircle(cursorColor, radius = 2.dp.toPx(), center = Offset(cx, cy))
            }
            6 -> {
                if (shadowAlpha > 0f) {
                    drawCircle(shadowColor, radius = 6.5.dp.toPx(), center = Offset(cx, cy), style = Stroke(width = strokePx + 1f))
                    drawCircle(shadowColor, radius = 2.4.dp.toPx(), center = Offset(cx, cy))
                }
                drawCircle(cursorColor, radius = 6.5.dp.toPx(), center = Offset(cx, cy), style = Stroke(width = strokePx))
                drawCircle(cursorColor, radius = 1.8.dp.toPx(), center = Offset(cx, cy))
                drawLine(cursorColor, Offset(cx, 1f), Offset(cx, cy - 6.5.dp.toPx()), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(cursorColor, Offset(cx, cy + 6.5.dp.toPx()), Offset(cx, size.height - 1f), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(cursorColor, Offset(1f, cy), Offset(cx - 6.5.dp.toPx(), cy), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(cursorColor, Offset(cx + 6.5.dp.toPx(), cy), Offset(size.width - 1f, cy), strokeWidth = strokePx, cap = StrokeCap.Round)
            }
        }
    }
}
