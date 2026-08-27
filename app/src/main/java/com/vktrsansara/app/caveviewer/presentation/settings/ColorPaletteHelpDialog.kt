package com.vktrsansara.app.caveviewer.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

/**
 * Help dialog explaining color picker palette options (Standard vs Muted).
 */
@Composable
fun ColorPaletteHelpDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Цвета колорпикера",
        onDismissRequest = onDismiss,
        buttons = {
            DialogCancelButton(
                text = "Закрыть",
                onClick = onDismiss
            )
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Выбор таблицы цветов определяет 15 базовых оттенков в диалоге выбора цвета (для курсора, сетки и будущих инструментов):",
                fontSize = 12.sp,
                color = AppColors.textSecondary,
                lineHeight = 17.sp
            )

            // 1. Standard Palette Section
            Column {
                Text(
                    text = "1. Стандартная палитра:",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentSkyBlue
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Классические спектральные цвета с максимальной контрастностью.",
                    fontSize = 12.sp,
                    color = AppColors.textSecondary,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val standardExamples = listOf(
                        Color(0xFFFF0000), // Красный
                        Color(0xFFFF6600), // Оранжевый
                        Color(0xFFFFBF00), // Янтарный
                        Color(0xFF008000), // Зеленый
                        Color(0xFF0000FF), // Синий
                        Color(0xFFFF00FF)  // Маджента
                    )
                    standardExamples.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                        )
                    }
                }
            }

            // 2. Muted Palette Section
            Column {
                Text(
                    text = "2. Приглушенные цвета:",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentSkyBlue
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Мягкие природные и пастельные тона, комфортные для глаз в темноте.",
                    fontSize = 12.sp,
                    color = AppColors.textSecondary,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val mutedExamples = listOf(
                        Color(0xFFE57373), // Мягкий красный
                        Color(0xFFFFB74D), // Мягкий оранжевый
                        Color(0xFFFFF176), // Мягкий желтый
                        Color(0xFF81C784), // Мягкий зеленый
                        Color(0xFF64B5F6), // Мягкий синий
                        Color(0xFFBA68C8)  // Мягкий фиолетовый
                    )
                    mutedExamples.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                        )
                    }
                }
            }
        }
    }
}
