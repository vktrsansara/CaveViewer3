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
 * Onboarding dialog explaining the scale binding process.
 */
@Composable
fun ScaleBindingHelpDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Привязка к масштабу",
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
                title = "Шаг 1. Начало отрезка:",
                description = "Наведите перекрестие курсора на начало шкалы масштаба и коснитесь экрана для установки Точки 1."
            )

            HelpItem(
                title = "Шаг 2. Конец отрезка:",
                description = "Наведите перекрестие курсора на отметку деления шкалы (например, 10 м) и коснитесь экрана для установки Точки 2."
            )

            HelpItem(
                title = "Шаг 3. Длина в метрах:",
                description = "В появившемся окне введите длину этого отрезка в реальных метрах и сохраните значение."
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
