package com.vktrsansara.app.caveviewer.presentation.map.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

/**
 * Detailed help modal dialog explaining the Delta Offset (ΔX, ΔY) local coordinates tool.
 */
@Composable
fun DeltaOffsetHelpDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Смещение (ΔX, ΔY)",
        onDismissRequest = onDismiss,
        buttons = {
            DialogCancelButton(text = "Закрыть", onClick = onDismiss)
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            HelpItem(
                title = "Назначение инструмента:",
                desc = "Позволяет определять локальные ортогональные координаты любой точки или хода относительно выбранного опорного репера, входа или пикета съёмки."
            )
            HelpItem(
                title = "Ориентация по истинному северу:",
                desc = "Оси проекций Восток/Запад и Север/Юг автоматически ориентируются по истинному северу пещеры с учетом калиброванного угла поворота карты (angle_north)."
            )
            HelpItem(
                title = "Шаг 1. Опорный репер (Точка 1):",
                desc = "Наведите центральный курсор на исходную точку (например, Репер 0, базовый лагерь или вход) и коснитесь экрана."
            )
            HelpItem(
                title = "Шаг 2. Живые измерения:",
                desc = "Перемещайте карту к целевому объекту. Инструмент непрерывно строит прямоугольный треугольник проекций и рассчитывает:\n• ΔX: смещение на Восток (+) или Запад (-);\n• ΔY: смещение на Север (+) или Юг (-);\n• Прямая: истинное расстояние по гипотенузе;\n• Азимут: дирекционный угол направления на курсор."
            )
            HelpItem(
                title = "Шаг 3. Сброс и отмена:",
                desc = "Кнопка «Отменить» (или повторный тап) сбрасывает опорный репер для выбора новой точки. Кнопка «Закрыть» завершает работу с инструментом."
            )
        }
    }
}

@Composable
private fun HelpItem(title: String, desc: String) {
    Column {
        Text(text = title, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = AccentSkyBlue)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, fontSize = 12.sp, lineHeight = 17.sp, color = AppColors.textSecondary)
    }
}
