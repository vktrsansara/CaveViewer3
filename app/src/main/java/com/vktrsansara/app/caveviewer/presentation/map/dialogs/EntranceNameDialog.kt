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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.presentation.components.DialogSaveButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import java.util.Locale

/**
 * Modal dialog for assigning a name to an OpenStreetMap entrance GPS coordinate.
 */
@Composable
fun EntranceNameDialog(
    lat: Double,
    lon: Double,
    defaultName: String = "Точка входа",
    onSave: (name: String) -> Unit,
    onCancel: () -> Unit
) {
    var nameText by remember(defaultName) { mutableStateOf(defaultName) }
    val isNameValid = nameText.trim().isNotEmpty()

    val latFormatted = remember(lat) { String.format(Locale.US, "%.6f", lat) }
    val lonFormatted = remember(lon) { String.format(Locale.US, "%.6f", lon) }

    Dialog(onDismissRequest = onCancel) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.bgCard)
                .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Точка входа на местности",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Широта: $latFormatted°, Долгота: $lonFormatted°",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.textPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Название точки входа:",
                fontSize = 12.5.sp,
                color = AppColors.textSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = nameText,
                onValueChange = { nameText = it },
                placeholder = {
                    Text("Например: Главный вход", color = AppColors.textSecondary.copy(alpha = 0.5f), fontSize = 13.sp)
                },
                singleLine = true,
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

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                DialogCancelButton(
                    text = "Отмена",
                    onClick = onCancel
                )
                Spacer(modifier = Modifier.width(8.dp))
                DialogSaveButton(
                    text = "Сохранить",
                    enabled = isNameValid,
                    onClick = {
                        if (isNameValid) {
                            onSave(nameText.trim())
                        }
                    }
                )
            }
        }
    }
}
