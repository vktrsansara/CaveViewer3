package com.vktrsansara.app.caveviewer.presentation.metadata

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
fun CadastralHelpDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Кадастровая запись",
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
                title = "Кадастровая запись:",
                description = "Научно-справочный и исследовательский паспорт пещеры. Включает 7 подразделов:\n" +
                        "• Классификация: Генезис, тип полости, категория сложности.\n" +
                        "• Топология: Пространственная ориентация, форма ходов и залов.\n" +
                        "• Морфология: Формы рельефа, натечные образования, отложения.\n" +
                        "• Климат: Температурный режим, тяга воздуха, микроклимат.\n" +
                        "• Гидрология: Водотоки, озера, сифоны, паводковый режим.\n" +
                        "• Биота: Пещерная флора и фауна, летучие мыши.\n" +
                        "• Дополнительно: Археология, история исследований, литература."
            )

            HelpItem(
                title = "Управление записями:",
                description = "• Кнопка «+»: Создает новую карточку с заголовком и описанием в текущем подразделе.\n" +
                        "• Кнопка «Список»: Открывает быстрое оглавление всех записей текущего подраздела для мгновенного перехода."
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
