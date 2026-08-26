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
fun GeolocationHelpDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Геолокация",
        onDismissRequest = onDismiss,
        buttons = {
            DialogCancelButton(
                text = "Закрыть",
                onClick = onDismiss
            )
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            HelpItem(
                title = "Расположение:",
                description = "Административная привязка карстового массива или пещеры (государство, субъект/область, район и дополнительное текстовое описание ориентиров на местности)."
            )

            HelpItem(
                title = "Точки входа (Геолокация):",
                description = "Географические координаты входов в пещеру в системе WGS-84 для GPS-навигации:\n" +
                        "• Название: Произвольное наименование конкретного входа, колодца или грота.\n" +
                        "• Широта (Lat) и Долгота (Lon): Десятичные градусы (например: 43.456789 и 40.123456).\n" +
                        "• Высота (Alt): Высота входа над уровнем моря в метрах (например: 1250.0)."
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
