package com.vktrsansara.app.caveviewer.presentation.settings

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

/**
 * Information dialog for the "Interface" settings section with standardized red close button.
 */
@Composable
fun InterfaceInfoDialog(
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
            // H1: Dialog Title
            Text(
                text = "Справка: Интерфейс",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Во весь экран
                Text(
                    text = "Во весь экран",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Скрывает верхнюю системную шторку и нижнюю панель навигации Android для максимального увеличения рабочей области карты.",
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    color = AppColors.textSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Показывать компас
                Text(
                    text = "Показывать компас",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Отображает в левом верхнем углу виджет компаса с азимутом на север. Клик по компасу плавно возвращает карту в положение на север.",
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    color = AppColors.textSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Полоска масштаба
                Text(
                    text = "Полоска масштаба",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Отображает в правом верхнем углу линейку реального масштаба в метрах с динамическим пересчетом при приближении и отдалении карты.",
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    color = AppColors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Close button with red border
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

@Preview
@Composable
private fun InterfaceInfoDialogPreview() {
    InterfaceInfoDialog(onDismiss = {})
}
