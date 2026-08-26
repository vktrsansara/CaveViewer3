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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.presentation.components.DialogSaveButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import java.util.Locale

/**
 * Input modal dialog for measured scale calibration parameters with subpixel (%.4f) precision.
 */
@Composable
fun ScaleBindingInputDialog(
    measuredPixels: Double,
    onSave: (pixelsPerMeter: Double, scaleMeters: Double) -> Unit,
    onCancel: () -> Unit
) {
    var metersText by remember { mutableStateOf("") }
    val metersVal = metersText.replace(',', '.').toDoubleOrNull()
    val isMetersValid = metersVal != null && metersVal > 0.0

    val calculatedPpm = if (isMetersValid && metersVal != null) {
        measuredPixels / metersVal
    } else {
        0.0
    }

    val measuredPxFormatted = remember(measuredPixels) {
        String.format(Locale.US, "%.4f", measuredPixels)
    }

    Dialog(onDismissRequest = onCancel) {
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
                text = "Параметры масштаба",
                color = AppColors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Measured pixels info with 4 decimal places
            Text(
                text = "Длина отрезка на карте: $measuredPxFormatted px",
                color = AppColors.textPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Real-world meters input
            Text(
                text = "Чему равен отрезок в метрах (м):",
                color = AppColors.textSecondary,
                fontSize = 12.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = metersText,
                onValueChange = { metersText = it },
                placeholder = {
                    Text("Например: 10", color = AppColors.textSecondary.copy(alpha = 0.5f), fontSize = 13.sp)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    focusedBorderColor = AccentSkyBlue,
                    unfocusedBorderColor = AppColors.borderColor,
                    cursorColor = AccentSkyBlue,
                    focusedContainerColor = AppColors.bgMain,
                    unfocusedContainerColor = AppColors.bgMain
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Dynamic scale ratio hint with 4 decimal places
            val ppmString = if (isMetersValid) {
                String.format(Locale.US, "%.4f", calculatedPpm)
            } else {
                "—"
            }
            Text(
                text = "Рассчитанный масштаб: 1 м = $ppmString px",
                color = if (isMetersValid) AccentSkyBlue else AppColors.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                DialogCancelButton(
                    text = "Отмена",
                    onClick = onCancel
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                DialogSaveButton(
                    text = "Сохранить",
                    enabled = isMetersValid,
                    onClick = {
                        if (isMetersValid && metersVal != null) {
                            onSave(calculatedPpm, metersVal)
                        }
                    }
                )
            }
        }
    }
}
