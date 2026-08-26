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
fun MultiToolDockHelpDialog(onDismiss: () -> Unit) {
    AppDialogContainer(
        title = "Панель инструментов",
        onDismissRequest = onDismiss,
        buttons = { DialogCancelButton(text = "Закрыть", onClick = onDismiss) }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Плавающий док позволяет работать с несколькими инструментами одновременно (до 4 в наборе).",
                fontSize = 13.sp,
                color = AppColors.textPrimary,
                lineHeight = 18.sp
            )
            HelpFeatureItem(
                title = "Переключение инструментов",
                desc = "Нажимайте на иконки инструментов в баре для переключения. Активный инструмент подсвечивается цветной рамкой и принимает нажатия по экрану."
            )
            HelpFeatureItem(
                title = "Кнопка «Отмена»",
                desc = "Отменяет последнюю точку текущего выбранного инструмента."
            )
            HelpFeatureItem(
                title = "Кнопка «Закрыть»",
                desc = "• Короткое нажатие при активном инструменте выключает его замер.\n• Короткое нажатие без активного инструмента удаляет инструменты из бара по очереди.\n• Долгое нажатие мгновенно закрывает всю панель и очищает замеры."
            )
            HelpFeatureItem(
                title = "Кнопка «Избранное»",
                desc = "Сохраняет текущий набор инструментов. В главном меню появится пункт «Мои инструменты» для быстрого вызова этой коллекции в один клик."
            )
        }
    }
}

@Composable
private fun HelpFeatureItem(title: String, desc: String) {
    Column {
        Text(text = title, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = AccentSkyBlue)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, fontSize = 12.sp, lineHeight = 17.sp, color = AppColors.textSecondary)
    }
}
