package com.vktrsansara.app.caveviewer.presentation.metadata

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

@Composable
fun CadastralHelpDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.bgCard)
                .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Справка: Кадастровая запись",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                DialogCancelButton(
                    text = "Закрыть",
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
private fun HelpItem(title: String, description: String) {
    Column {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.textPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            fontSize = 12.5.sp,
            color = AppColors.textSecondary,
            lineHeight = 17.sp
        )
    }
}
