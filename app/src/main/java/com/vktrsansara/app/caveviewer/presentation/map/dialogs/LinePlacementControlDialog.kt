package com.vktrsansara.app.caveviewer.presentation.map.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.LinePlacementMode
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.presentation.components.DialogSaveButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

@Composable
fun LinePlacementControlDialog(
    currentMode: LinePlacementMode,
    onApply: (LinePlacementMode) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMode by remember(currentMode) { mutableStateOf(currentMode) }
    var isHelpOpen by remember { mutableStateOf(false) }

    if (isHelpOpen) {
        LinePlacementControlHelpDialog(onDismiss = { isHelpOpen = false })
    }

    AppDialogContainer(
        title = "Управление рисованием линий",
        onDismissRequest = onDismiss,
        onInfoClick = { isHelpOpen = true },
        buttons = {
            DialogCancelButton(
                text = "Отмена",
                onClick = onDismiss
            )
            Spacer(modifier = Modifier.width(8.dp))
            DialogSaveButton(
                text = "Применить",
                onClick = {
                    onApply(selectedMode)
                    onDismiss()
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Section 1: Cursor-based placement
            Text(
                text = "Установка вершин от курсора:",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccentSkyBlue
            )

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                ModeOptionRow(
                    title = LinePlacementMode.CURSOR_BUTTON_AND_TAP.title,
                    subtitle = "Вершина ставится в центре курсора по кнопке или тапу",
                    isSelected = selectedMode == LinePlacementMode.CURSOR_BUTTON_AND_TAP,
                    onClick = { selectedMode = LinePlacementMode.CURSOR_BUTTON_AND_TAP }
                )
                ModeOptionRow(
                    title = LinePlacementMode.CURSOR_BUTTON_ONLY.title,
                    subtitle = "Касания карты перемещают план, исключая случайные добавления",
                    isSelected = selectedMode == LinePlacementMode.CURSOR_BUTTON_ONLY,
                    onClick = { selectedMode = LinePlacementMode.CURSOR_BUTTON_ONLY }
                )
                ModeOptionRow(
                    title = LinePlacementMode.CURSOR_TAP_ONLY.title,
                    subtitle = "Вершина ставится тапом по экрану в позиции курсора",
                    isSelected = selectedMode == LinePlacementMode.CURSOR_TAP_ONLY,
                    onClick = { selectedMode = LinePlacementMode.CURSOR_TAP_ONLY }
                )
            }

            Spacer(modifier = Modifier.height(2.dp))
            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
            Spacer(modifier = Modifier.height(2.dp))

            // Section 2: Free placement
            Text(
                text = "Свободная установка:",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccentSkyBlue
            )

            ModeOptionRow(
                title = LinePlacementMode.FREE_TAP.title,
                subtitle = "Вершина ставится прямо в точку касания пальцем на карте",
                isSelected = selectedMode == LinePlacementMode.FREE_TAP,
                onClick = { selectedMode = LinePlacementMode.FREE_TAP }
            )
        }
    }
}

@Composable
private fun ModeOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) AccentSkyBlue.copy(alpha = 0.12f) else AppColors.bgSurface)
            .border(
                width = 1.dp,
                color = if (isSelected) AccentSkyBlue else AppColors.borderColor,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = AccentSkyBlue,
                unselectedColor = AppColors.textSecondary
            ),
            modifier = Modifier.scale(0.8f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) AccentSkyBlue else AppColors.textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = AppColors.textSecondary,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun LinePlacementControlHelpDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Управление рисованием",
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
                title = "Установка вершин от курсора:",
                description = "Центральное перекрестие экрана служит визиром для точной геодезической привязки. План пещеры перемещается под курсор."
            )

            HelpItem(
                title = "Кнопка «+» и Тап:",
                description = "Универсальный режим. Вершина добавляется в перекрестии курсора как по нажатию кнопки «+» на боковой панели, так и быстрым тапом в любом месте экрана."
            )

            HelpItem(
                title = "Только Кнопка «+»:",
                description = "Вершина добавляется строго по нажатию кнопки «+». Тапы по экрану только панорамируют карту, исключая случайные добавления лишних точек."
            )

            HelpItem(
                title = "Только Тап:",
                description = "Вершина добавляется по тапу на экран в месте перекрестия курсора. Верхняя кнопка на панели переключается в режим информационного индикатора."
            )

            HelpItem(
                title = "Тап в нужном месте (Свободная):",
                description = "Вершина ставится непосредственно в координатах касания экрана пальцем без привязки к центральному курсору."
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
