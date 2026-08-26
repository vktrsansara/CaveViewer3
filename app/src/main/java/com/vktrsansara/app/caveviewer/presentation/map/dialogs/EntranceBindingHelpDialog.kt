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
 * Onboarding dialog explaining Cave Entrance GPS binding via OpenStreetMap in standard app dialog style.
 */
@Composable
fun EntranceBindingHelpDialog(
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
                text = "Справка: Привязка точки входа",
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
                    title = "Шаг 1. Выбор на плане пещеры:",
                    description = "Наведите перекрестие курсора на вход на схеме пещеры и коснитесь экрана."
                )

                HelpItem(
                    title = "Шаг 2. Поиск места на карте:",
                    description = "Используйте строку поиска на карте OpenStreetMap для быстрого перехода к нужному региону, горе или урочищу."
                )

                HelpItem(
                    title = "Шаг 3. Фиксация на местности:",
                    description = "Наведите перекрестие курсора на точное положение входа на карте OSM и коснитесь экрана."
                )

                HelpItem(
                    title = "Шаг 4. Название и сохранение:",
                    description = "В появившемся окне задайте название точки входа и сохраните ее GPS-координаты."
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
