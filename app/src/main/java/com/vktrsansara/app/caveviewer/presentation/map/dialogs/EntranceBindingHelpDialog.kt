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
 * Onboarding dialog explaining Cave Entrance GPS binding via OpenStreetMap.
 */
@Composable
fun EntranceBindingHelpDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Привязка точки входа",
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
