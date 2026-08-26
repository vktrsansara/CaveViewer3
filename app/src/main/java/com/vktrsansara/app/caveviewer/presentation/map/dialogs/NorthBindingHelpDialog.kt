package com.vktrsansara.app.caveviewer.presentation.map.dialogs

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

/**
 * Onboarding dialog explaining North / Compass calibration in standard app dialog style.
 */
@Composable
fun NorthBindingHelpDialog(
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
            // Dialog Title
            Text(
                text = "Справка: Привязка к Северу",
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

            Spacer(modifier = Modifier.height(16.dp))

            // Standard close button matching other help dialogs
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
