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
 * Input modal dialog for North / Compass calibration azimuth parameters.
 */
@Composable
fun NorthBindingInputDialog(
    measuredAngle: Double,
    onSave: (angleNorth: Double) -> Unit,
    onCancel: () -> Unit
) {
    var angleText by remember(measuredAngle) {
        mutableStateOf(String.format(Locale.US, "%.2f", measuredAngle))
    }
    val angleVal = angleText.replace(',', '.').toDoubleOrNull()
    val isAngleValid = angleVal != null && angleVal in 0.0..360.0

    val measuredAngleFormatted = remember(measuredAngle) {
        String.format(Locale.US, "%.2f", measuredAngle)
    }

    AppDialogContainer(
        title = "Параметры компаса",
        onDismissRequest = onCancel,
        buttons = {
            DialogCancelButton(
                text = "Отмена",
                onClick = onCancel
            )
            Spacer(modifier = Modifier.width(8.dp))
            DialogSaveButton(
                text = "Сохранить",
                enabled = isAngleValid,
                onClick = {
                    if (isAngleValid && angleVal != null) {
                        onSave(angleVal)
                    }
                }
            )
        }
    ) {
        // Measured angle info
        Text(
            text = "Измеренный угол севера: $measuredAngleFormatted°",
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Angle input field label
        Text(
            text = "Угол направления на север (0..360°):",
            fontSize = 12.5.sp,
            color = AppColors.textSecondary
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = angleText,
            onValueChange = { angleText = it },
            placeholder = {
                Text("0.00", color = AppColors.textSecondary.copy(alpha = 0.5f), fontSize = 13.sp)
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

        Spacer(modifier = Modifier.height(8.dp))

        // Orientation hint
        Text(
            text = "0° — верх карты, 90° — восток, 180° — юг, 270° — запад.",
            fontSize = 11.5.sp,
            lineHeight = 16.sp,
            color = AppColors.textSecondary
        )
    }
}
