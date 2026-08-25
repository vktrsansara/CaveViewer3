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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.AppSettings
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import com.vktrsansara.app.caveviewer.ui.theme.CaveViewerTheme

private val GreenInfoColor = Color(0xFF10B981)

/**
 * Screen for configuring map tools and cursor overlay.
 */
@Composable
fun ToolsSettingsScreen(
    settings: AppSettings,
    onCursorShowChanged: (Boolean) -> Unit,
    onCursorTypeChanged: (Int) -> Unit,
    onCursorColorChanged: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isHelpDialogVisible by remember { mutableStateOf(false) }
    var isColorPickerVisible by remember { mutableStateOf(false) }

    if (isHelpDialogVisible) {
        CursorHelpDialog(onDismiss = { isHelpDialogVisible = false })
    }

    if (isColorPickerVisible) {
        CursorColorPickerDialog(
            selectedColor = settings.cursorColor,
            onColorSelected = onCursorColorChanged,
            onDismiss = { isColorPickerVisible = false }
        )
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
            // Card: Cursor Settings
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
                        onClick = { isHelpDialogVisible = true },
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

                // Grid of 5 cursor types + 1 color picker button (evenly distributed across full width)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val cursorColor = Color(settings.cursorColor)

                    // Cursor Type 1: Solid Cross
                    CursorTypeButton(
                        type = 1,
                        isSelected = settings.cursorType == 1,
                        cursorColor = cursorColor,
                        onClick = { onCursorTypeChanged(1) }
                    )

                    // Cursor Type 2: Dashed Plus
                    CursorTypeButton(
                        type = 2,
                        isSelected = settings.cursorType == 2,
                        cursorColor = cursorColor,
                        onClick = { onCursorTypeChanged(2) }
                    )

                    // Cursor Type 3: Center Dot
                    CursorTypeButton(
                        type = 3,
                        isSelected = settings.cursorType == 3,
                        cursorColor = cursorColor,
                        onClick = { onCursorTypeChanged(3) }
                    )

                    // Cursor Type 4: Diagonal X
                    CursorTypeButton(
                        type = 4,
                        isSelected = settings.cursorType == 4,
                        cursorColor = cursorColor,
                        onClick = { onCursorTypeChanged(4) }
                    )

                    // Cursor Type 5: Circle with Center Dot
                    CursorTypeButton(
                        type = 5,
                        isSelected = settings.cursorType == 5,
                        cursorColor = cursorColor,
                        onClick = { onCursorTypeChanged(5) }
                    )

                    // 6th Button: Color Picker
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppColors.bgSurface)
                            .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = AppColors.pressedColor),
                                onClick = { isColorPickerVisible = true }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(cursorColor)
                                .border(width = 1.5.dp, color = Color.White, shape = CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CursorTypeButton(
    type: Int,
    isSelected: Boolean,
    cursorColor: Color,
    onClick: () -> Unit
) {
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
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            when (type) {
                1 -> {
                    // Solid Cross
                    val lineHalf = 7.dp.toPx()
                    val stroke = 1.5.dp.toPx()
                    drawLine(color = cursorColor, start = Offset(cx - lineHalf, cy), end = Offset(cx + lineHalf, cy), strokeWidth = stroke)
                    drawLine(color = cursorColor, start = Offset(cx, cy - lineHalf), end = Offset(cx, cy + lineHalf), strokeWidth = stroke)
                }
                2 -> {
                    // Dashed Plus with center dot
                    val dotR = 1.5.dp.toPx()
                    val gap = 3.dp.toPx()
                    val len = 4.5.dp.toPx()
                    val stroke = 1.5.dp.toPx()

                    drawCircle(color = cursorColor, radius = dotR, center = Offset(cx, cy))
                    drawLine(color = cursorColor, start = Offset(cx, cy - gap), end = Offset(cx, cy - gap - len), strokeWidth = stroke, cap = StrokeCap.Round)
                    drawLine(color = cursorColor, start = Offset(cx, cy + gap), end = Offset(cx, cy + gap + len), strokeWidth = stroke, cap = StrokeCap.Round)
                    drawLine(color = cursorColor, start = Offset(cx - gap, cy), end = Offset(cx - gap - len, cy), strokeWidth = stroke, cap = StrokeCap.Round)
                    drawLine(color = cursorColor, start = Offset(cx + gap, cy), end = Offset(cx + gap + len, cy), strokeWidth = stroke, cap = StrokeCap.Round)
                }
                3 -> {
                    // Center Dot
                    val dotR = 3.dp.toPx()
                    drawCircle(color = cursorColor, radius = dotR, center = Offset(cx, cy))
                }
                4 -> {
                    // Diagonal X
                    val dotR = 1.2.dp.toPx()
                    val gap = 2.5.dp.toPx()
                    val len = 4.5.dp.toPx()
                    val stroke = 1.5.dp.toPx()
                    val d = 0.7071f

                    drawCircle(color = cursorColor, radius = dotR, center = Offset(cx, cy))
                    val dirs = listOf(Pair(1f, 1f), Pair(-1f, 1f), Pair(1f, -1f), Pair(-1f, -1f))
                    dirs.forEach { (dx, dy) ->
                        drawLine(
                            color = cursorColor,
                            start = Offset(cx + dx * gap * d, cy + dy * gap * d),
                            end = Offset(cx + dx * (gap + len) * d, cy + dy * (gap + len) * d),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                    }
                }
                5 -> {
                    // Circle with Center Dot
                    val dotR = 1.5.dp.toPx()
                    val ringR = 6.5.dp.toPx()
                    val stroke = 1.5.dp.toPx()

                    drawCircle(color = cursorColor, radius = dotR, center = Offset(cx, cy))
                    drawCircle(
                        color = cursorColor,
                        radius = ringR,
                        center = Offset(cx, cy),
                        style = Stroke(width = stroke)
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolsSettingsHeader(
    title: String,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.bgSurface)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button: 32x32 dp
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AppColors.bgCard)
                    .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = AppColors.pressedColor),
                        onClick = onNavigateBack
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = AccentSkyBlue,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary
            )
        }

        HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
    }
}

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
            .padding(12.dp)
    ) {
        content()
    }
}

@Preview(name = "Tools Settings Preview", showBackground = true)
@Composable
private fun ToolsSettingsScreenPreview() {
    CaveViewerTheme(darkTheme = true) {
        ToolsSettingsScreen(
            settings = AppSettings(),
            onCursorShowChanged = {},
            onCursorTypeChanged = {},
            onCursorColorChanged = {},
            onNavigateBack = {}
        )
    }
}
