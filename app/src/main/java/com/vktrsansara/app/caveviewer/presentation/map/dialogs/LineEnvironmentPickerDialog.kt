package com.vktrsansara.app.caveviewer.presentation.map.dialogs

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.measure.LineColorUtils
import com.vktrsansara.app.caveviewer.domain.model.LineEnvironmentType
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.presentation.map.components.LinePatternRenderer
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

/**
 * Modal selection dialog for Topographic Environment Textures (UIS / Therion speleological standard).
 * Displays a clean vertical list of options with a live line pattern sample [Пример линии] and [Название].
 */
@Composable
fun LineEnvironmentPickerDialog(
    selectedEnvironment: LineEnvironmentType,
    customHaloColor: Long? = null,
    showCustom: Boolean = true,
    onEnvironmentSelected: (LineEnvironmentType) -> Unit,
    onDismiss: () -> Unit
) {
    val options = remember(showCustom) {
        if (showCustom) {
            LineEnvironmentType.entries
        } else {
            LineEnvironmentType.entries.filter { it != LineEnvironmentType.CUSTOM }
        }
    }

    AppDialogContainer(
        title = "Текстура хода (Среда)",
        onDismissRequest = onDismiss,
        buttons = {
            DialogCancelButton(
                text = "Закрыть",
                onClick = onDismiss
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { env ->
                val isSelected = (selectedEnvironment == env)
                val patternColor = LineColorUtils.getHaloColor(env, customHaloColor) ?: Color.White

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AccentSkyBlue.copy(alpha = 0.15f) else AppColors.bgSurface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) AccentSkyBlue else AppColors.borderColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.pressedColor),
                            onClick = {
                                onEnvironmentSelected(env)
                                onDismiss()
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Line Sample Preview
                    LineEnvironmentSampleBox(
                        environmentType = env,
                        patternColor = patternColor,
                        modifier = Modifier
                            .width(58.dp)
                            .height(28.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // 2. Title and Subtitle / Symbol
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = env.title,
                            fontSize = 13.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) AccentSkyBlue else AppColors.textPrimary
                        )
                        if (env.symbol.isNotBlank()) {
                            Text(
                                text = env.symbol,
                                fontSize = 11.sp,
                                color = AppColors.textSecondary
                            )
                        }
                    }

                    // 3. Selection Indicator
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Выбрано",
                            tint = AccentSkyBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Miniature Canvas box showing a single line segment with vector topographic environment markings.
 */
@Composable
fun LineEnvironmentSampleBox(
    environmentType: LineEnvironmentType,
    patternColor: Color,
    lineColor: Color = AccentSkyBlue,
    modifier: Modifier = Modifier
        .width(58.dp)
        .height(28.dp)
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(AppColors.bgCard)
            .border(1.dp, AppColors.borderColor, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp)
        ) {
            val midY = size.height / 2f
            val start = Offset(0f, midY)
            val end = Offset(size.width, midY)
            val strokePx = 2.dp.toPx()

            // 1. Draw core line segment
            drawLine(
                color = lineColor,
                start = start,
                end = end,
                strokeWidth = strokePx,
                cap = StrokeCap.Round
            )

            // 2. Draw topographic pattern
            if (environmentType != LineEnvironmentType.NONE) {
                LinePatternRenderer.drawEnvironmentPattern(
                    drawScope = this,
                    screenPoints = listOf(start, end),
                    environmentType = environmentType,
                    patternColor = patternColor,
                    lineWidthPx = strokePx
                )
            }
        }
    }
}
