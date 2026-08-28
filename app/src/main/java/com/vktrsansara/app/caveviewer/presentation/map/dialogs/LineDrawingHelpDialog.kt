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
fun LineDrawingHelpDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Рисование линий",
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
                title = "Рисование ходов пещеры:",
                description = "Предназначено для пошагового нанесения полилиний основных и вспомогательных ходов, колодцев и русел подземных рек на план."
            )

            HelpItem(
                title = "Кнопка [+] / Индикатор:",
                description = "Добавляет очередную вершину полилинии в перекрестии центрального курсора. В режимах тапа иконка меняется на индикатор касания."
            )

            HelpItem(
                title = "Кнопка [←] Отмена вершины:",
                description = "Удаляет последнюю добавленную вершину линии (Undo), позволяя быстро исправить неточный шаг."
            )

            HelpItem(
                title = "Кнопка [✓] Завершить линию:",
                description = "Завершает построение и открывает окно настройки свойств созданной линии. Кнопка активируется при наличии минимум 2 вершин."
            )

            HelpItem(
                title = "Кнопка [✕] Отмена рисования:",
                description = "Прерывает процесс создания линии, сбрасывает набранные вершины и выходит из режима рисования."
            )

            HelpItem(
                title = "Кнопка [⚙️] Управление:",
                description = "Позволяет настроить способ добавления вершин: по кнопке «+», быстрому тапу по экрану от курсора или свободному тапу в любом месте карты."
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
