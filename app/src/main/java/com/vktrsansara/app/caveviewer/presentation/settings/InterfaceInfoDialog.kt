package com.vktrsansara.app.caveviewer.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

/**
 * Information dialog for the "Interface" settings section.
 */
@Composable
fun InterfaceInfoDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Интерфейс",
        onDismissRequest = onDismiss,
        buttons = {
            DialogCancelButton(
                text = "Закрыть",
                onClick = onDismiss
            )
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // 1. Во весь экран
            Column {
                Text(
                    text = "Во весь экран",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentSkyBlue
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Скрывает верхнюю системную шторку и нижнюю панель навигации Android для максимального увеличения рабочей области карты.",
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    color = AppColors.textSecondary
                )
            }

            // 2. Показывать компас
            Column {
                Text(
                    text = "Показывать компас",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentSkyBlue
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Отображает в левом верхнем углу виджет компаса с азимутом на север. Клик по компасу плавно возвращает карту в положение на север.",
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    color = AppColors.textSecondary
                )
            }

            // 3. Полоска масштаба
            Column {
                Text(
                    text = "Полоска масштаба",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentSkyBlue
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Отображает в правом верхнем углу линейку реального масштаба в метрах с динамическим пересчетом при приближении и отдалении карты.",
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    color = AppColors.textSecondary
                )
            }
        }
    }
}

@Preview
@Composable
private fun InterfaceInfoDialogPreview() {
    InterfaceInfoDialog(onDismiss = {})
}
