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

@Composable
fun PointEditorHelpDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Редактор точек",
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
                title = "Назначение режима:",
                description = "Предназначен для интерактивного нанесения пикетов, навесок, колодцев, лагерей и спелеологических объектов на растровый план пещеры."
            )

            HelpItem(
                title = "Позиционирование по курсору:",
                description = "Перемещайте карту под визирным перекрестием для точной фиксации пикета в исходных координатах растра."
            )

            HelpItem(
                title = "Режимы установки (меню «Управление»):",
                description = "Вы можете настроить установку через кнопку «+», быстрый тап по экрану или свободное касание нужного места плана без наведения курсора."
            )

            HelpItem(
                title = "Просмотр и изменение точек:",
                description = "Нажатие на любой существующий маркер на плане открывает карточку деталей, где можно отредактировать атрибуты, сфокусировать карту или удалить точку."
            )

            HelpItem(
                title = "Символика UIS и кастомные свойства:",
                description = "Каждая точка поддерживает выбор цвета, размера, знаков международной спелеологической спецификации UIS и заполнение схемы атрибутов слоя."
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
