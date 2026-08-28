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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.presentation.components.DialogSaveButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

/**
 * Compact modal dialog for creating a new line layer.
 */
@Composable
fun CreateLineLayerDialog(
    existingNames: List<String> = emptyList(),
    onSave: (name: String) -> Unit,
    onCancel: () -> Unit
) {
    var layerName by remember { mutableStateOf("") }
    val trimmed = layerName.trim()
    val isDuplicate = remember(trimmed, existingNames) {
        trimmed.isNotEmpty() && existingNames.any { it.equals(trimmed, ignoreCase = true) }
    }

    AppDialogContainer(
        title = "Новый слой линий",
        onDismissRequest = onCancel,
        buttons = {
            DialogCancelButton("Отмена", onClick = onCancel)
            Spacer(modifier = Modifier.width(8.dp))
            DialogSaveButton(
                text = "Создать",
                enabled = trimmed.isNotBlank() && !isDuplicate,
                onClick = { onSave(trimmed) }
            )
        }
    ) {
        OutlinedTextField(
            value = layerName,
            onValueChange = { layerName = it },
            label = { Text("Название слоя") },
            placeholder = { Text("Например: Основные ходы") },
            singleLine = true,
            isError = isDuplicate,
            supportingText = if (isDuplicate) {
                { Text("Слой линий с таким названием уже существует", color = Color(0xFFEF4444)) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isDuplicate) Color(0xFFEF4444) else AccentSkyBlue,
                unfocusedBorderColor = if (isDuplicate) Color(0xFFEF4444) else AppColors.borderColor,
                focusedLabelColor = if (isDuplicate) Color(0xFFEF4444) else AccentSkyBlue,
                unfocusedLabelColor = if (isDuplicate) Color(0xFFEF4444) else AppColors.textSecondary,
                errorBorderColor = Color(0xFFEF4444),
                errorLabelColor = Color(0xFFEF4444),
                errorSupportingTextColor = Color(0xFFEF4444),
                cursorColor = AccentSkyBlue,
                focusedTextColor = AppColors.textPrimary,
                unfocusedTextColor = AppColors.textPrimary
            )
        )
    }
}
