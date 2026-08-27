package com.vktrsansara.app.caveviewer.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import kotlin.math.roundToInt

/**
 * High-contrast Black & White Checkerboard background for transparency preview.
 */
@Composable
fun CheckerboardBox(
    modifier: Modifier = Modifier,
    squareSizeDp: Float = 4f,
    color1: Color = Color.White,
    color2: Color = Color.Black,
    content: @Composable () -> Unit = {}
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val squarePx = squareSizeDp.dp.toPx()
            val numCols = (size.width / squarePx).toInt() + 1
            val numRows = (size.height / squarePx).toInt() + 1
            for (row in 0 until numRows) {
                for (col in 0 until numCols) {
                    val color = if ((row + col) % 2 == 0) color1 else color2
                    drawRect(
                        color = color,
                        topLeft = Offset(col * squarePx, row * squarePx),
                        size = Size(squarePx, squarePx)
                    )
                }
            }
        }
        content()
    }
}

/**
 * Universal color picker dialog with 16 preset buttons (4x4), custom spectrum picker,
 * and alpha transparency slider with black/white checkerboard preview.
 */
@Composable
fun AppColorPickerDialog(
    initialColor: Long,
    paletteMode: String = "standard",
    title: String = "Выбор цвета",
    onColorSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val presetColors = remember(paletteMode) {
        if (paletteMode == "muted") {
            listOf(
                0xFF1A1A1AL, // Черный (угольный)
                0xFF4A4A4AL, // Темно-серый
                0xFF8B0000L, // Бордовый
                0xFFCC5500L, // Терракотовый
                0xFFD4A017L, // Горчичный
                0xFF556B2FL, // Оливковый
                0xFF228B22L, // Травяной
                0xFF3EB489L, // Мятный
                0xFF40E0D0L, // Бирюзовый
                0xFF4682B4L, // Стальной синий
                0xFF0047ABL, // Кобальтовый
                0xFF967BB6L, // Лавандовый
                0xFFE52B50L, // Амарантовый
                0xFFFF7F50L, // Коралловый
                0xFF7B3F00L  // Шоколадный
            )
        } else {
            listOf(
                0xFF000000L, // Черный
                0xFF808080L, // Серый
                0xFFFF0000L, // Красный
                0xFFFF6600L, // Оранжевый
                0xFFFFBF00L, // Янтарный (Желтый)
                0xFFAADD00L, // Желто-зеленый
                0xFF008000L, // Зеленый
                0xFF50C878L, // Изумрудный
                0xFF00FFFFL, // Циан (Голубой)
                0xFF007FFFL, // Лазурный
                0xFF0000FFL, // Синий
                0xFF800080L, // Фиолетовый
                0xFFFF00FFL, // Пурпурный (Маджента)
                0xFFFF1493L, // Розовый
                0xFFA52A2AL  // Коричневый
            )
        }
    }

    var selectedRgb by remember { mutableLongStateOf(initialColor or 0xFF000000L) }
    var alphaFraction by remember {
        val initialAlpha = ((initialColor ushr 24) and 0xFF).toFloat() / 255f
        mutableFloatStateOf(if (initialAlpha <= 0f) 0.6f else initialAlpha)
    }
    var isCustomPickerOpen by remember { mutableStateOf(false) }

    // Custom spectrum HSV state
    var hue by remember { mutableFloatStateOf(120f) }

    val finalColorLong = remember(selectedRgb, alphaFraction) {
        val alphaInt = (alphaFraction * 255f).roundToInt().coerceIn(0, 255)
        (alphaInt.toLong() shl 24) or (selectedRgb and 0x00FFFFFFL)
    }

    AppDialogContainer(
        title = title,
        onDismissRequest = onDismiss,
        buttons = {
            DialogCancelButton(
                text = "Отмена",
                onClick = onDismiss
            )
            Spacer(modifier = Modifier.width(8.dp))
            DialogSaveButton(
                text = "Применить",
                onClick = {
                    onColorSelected(finalColorLong)
                    onDismiss()
                }
            )
        }
    ) {
        // 1. Grid of 16 Color Buttons (4x4)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val allItems: List<Long?> = presetColors.take(15) + listOf(null)
            allItems.chunked(4).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { colorValue ->
                        if (colorValue != null) {
                            val isSelected = (selectedRgb and 0x00FFFFFFL) == (colorValue and 0x00FFFFFFL)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.2f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(colorValue.toInt()))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) AccentSkyBlue else Color.White.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            selectedRgb = colorValue
                                            isCustomPickerOpen = false
                                        }
                                    )
                            )
                        } else {
                            // 16th Button (+) for Custom HSV Spectrum
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.2f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isCustomPickerOpen) AccentSkyBlue.copy(alpha = 0.2f) else AppColors.bgSurface)
                                    .border(
                                        width = if (isCustomPickerOpen) 2.dp else 1.dp,
                                        color = if (isCustomPickerOpen) AccentSkyBlue else AppColors.borderColor,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { isCustomPickerOpen = !isCustomPickerOpen }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Другой цвет",
                                    tint = AccentSkyBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Custom Hue Spectrum Bar when (+) is active
        AnimatedVisibility(
            visible = isCustomPickerOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Text(
                    text = "Спектр оттенка:",
                    color = AppColors.textSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Rainbow Gradient Slider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Red, Color.Yellow, Color.Green,
                                    Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                                )
                            )
                        )
                        .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                    hue = fraction * 360f
                                    val hsvColor = android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
                                    selectedRgb = (hsvColor.toLong() and 0xFFFFFFFFL) or 0xFF000000L
                                },
                                onDrag = { change, _ ->
                                    val fraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                                    hue = fraction * 360f
                                    val hsvColor = android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
                                    selectedRgb = (hsvColor.toLong() and 0xFFFFFFFFL) or 0xFF000000L
                                }
                            )
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
        Spacer(modifier = Modifier.height(12.dp))

        // 2. Alpha Transparency Slider with Black/White Checkerboard Preview
        Text(
            text = "Прозрачность: ${(alphaFraction * 100).roundToInt()}%",
            color = AppColors.textSecondary,
            fontSize = 12.5.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                value = alphaFraction,
                onValueChange = { alphaFraction = it },
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = AccentSkyBlue,
                    activeTrackColor = AccentSkyBlue,
                    inactiveTrackColor = AppColors.bgSurface
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Black & White Checkerboard Preview Box with Colored Inner Circle
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
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(finalColorLong.toInt()))
                            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    )
                }
            }
        }
    }
}
