package com.vktrsansara.app.caveviewer.presentation.map.dialogs

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.presentation.components.DialogSaveButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import java.util.Locale

/**
 * Input modal dialog for measured scale calibration parameters.
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

    AppDialogContainer(
        title = "Параметры масштаба",
        onDismissRequest = onCancel,
        buttons = {
            DialogCancelButton(
                text = "Отмена",
                onClick = onCancel
            )
            Spacer(modifier = Modifier.width(8.dp))
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
    ) {
        // Measured pixels info
        Text(
            text = "Длина отрезка на карте: $measuredPxFormatted px",
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Real-world meters input label
        Text(
            text = "Чему равен отрезок в метрах (м):",
            fontSize = 12.5.sp,
            color = AppColors.textSecondary
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

        // Dynamic scale ratio hint
        val ppmString = if (isMetersValid) {
            String.format(Locale.US, "%.4f", calculatedPpm)
        } else {
            "—"
        }
        Text(
            text = "Рассчитанный масштаб: 1 м = $ppmString px",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isMetersValid) AccentSkyBlue else AppColors.textSecondary
        )
    }
}
