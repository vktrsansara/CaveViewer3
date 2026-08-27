package com.vktrsansara.app.caveviewer.presentation.map.dialogs

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.presentation.components.DialogSaveButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

@Composable
fun CreateLayerDialog(
    onSave: (name: String) -> Unit,
    onCancel: () -> Unit
) {
    var layerName by remember { mutableStateOf("") }

    AppDialogContainer(
        title = "Новый слой",
        onDismissRequest = onCancel,
        buttons = {
            DialogCancelButton(
                text = "Отмена",
                onClick = onCancel
            )
            Spacer(modifier = Modifier.width(8.dp))
            DialogSaveButton(
                text = "Создать",
                enabled = layerName.isNotBlank(),
                onClick = { onSave(layerName.trim()) }
            )
        }
    ) {
        OutlinedTextField(
            value = layerName,
            onValueChange = { layerName = it },
            label = { Text("Название слоя") },
            placeholder = { Text("Например: Опасности и навеска") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentSkyBlue,
                unfocusedBorderColor = AppColors.borderColor,
                focusedLabelColor = AccentSkyBlue,
                unfocusedLabelColor = AppColors.textSecondary,
                focusedTextColor = AppColors.textPrimary,
                unfocusedTextColor = AppColors.textPrimary,
                cursorColor = AccentSkyBlue
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
