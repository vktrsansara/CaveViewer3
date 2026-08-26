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
 * Onboarding dialog explaining North / Compass calibration.
 */
@Composable
fun NorthBindingHelpDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Привязка к Северу",
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
                title = "Шаг 1. Ориентир на карте:",
                description = "Найдите на плане или схеме розу ветров, стрелку направления на север или ось координат."
            )

            HelpItem(
                title = "Шаг 2. Основание стрелки (Юг):",
                description = "Наведите перекрестие курсора на основание стрелки (Юг) и коснитесь экрана для установки Точки 1."
            )

            HelpItem(
                title = "Шаг 3. Острие стрелки (Север):",
                description = "Наведите перекрестие курсора на острие стрелки (Север) и коснитесь экрана для установки Точки 2."
            )

            HelpItem(
                title = "Шаг 4. Сохранение азимута:",
                description = "В появившемся окне проверьте вычисленный угол и сохраните его для калибровки компаса."
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
