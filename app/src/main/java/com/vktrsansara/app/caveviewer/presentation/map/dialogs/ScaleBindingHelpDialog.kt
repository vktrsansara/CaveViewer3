package com.vktrsansara.app.caveviewer.presentation.map.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vktrsansara.app.caveviewer.presentation.components.DialogSaveButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

/**
 * Onboarding dialog explaining the scale binding process in 3 clear steps.
 */
@Composable
fun ScaleBindingHelpDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.bgCard)
                .border(1.dp, AppColors.borderColor, RoundedCornerShape(8.dp))
                .padding(18.dp)
        ) {
            Text(
                text = "Справка: Привязка к масштабу",
                color = AppColors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(14.dp))

            HelpStepItem(
                step = "1",
                text = "Наведите перекрестие курсора на начало шкалы масштаба и коснитесь экрана."
            )

            Spacer(modifier = Modifier.height(10.dp))

            HelpStepItem(
                step = "2",
                text = "Наведите перекрестие курсора на отметку деления шкалы (например, 10м) и коснитесь экрана."
            )

            Spacer(modifier = Modifier.height(10.dp))

            HelpStepItem(
                step = "3",
                text = "В появившемся окне введите длину этого отрезка в метрах."
            )

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                DialogSaveButton(
                    text = "Понятно",
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
private fun HelpStepItem(
    step: String,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(AccentSkyBlue.copy(alpha = 0.15f))
                .border(1.dp, AccentSkyBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                color = AccentSkyBlue,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            color = AppColors.textSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}
