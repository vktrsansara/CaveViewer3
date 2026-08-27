package com.vktrsansara.app.caveviewer.presentation.map.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldDefinition
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldType
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.presentation.components.DialogSaveButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

@Composable
fun AddFieldDialog(
    onAdd: (field: LayerFieldDefinition) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(LayerFieldType.TEXT) }
    var defaultValue by remember { mutableStateOf("") }
    var booleanDefault by remember { mutableStateOf(false) }
    var optionsRaw by remember { mutableStateOf("") }

    val options = remember(optionsRaw) {
        optionsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    val isValid = name.isNotBlank() && (type != LayerFieldType.SELECT || options.isNotEmpty())

    AppDialogContainer(
        title = "Новое поле слоя",
        onDismissRequest = onCancel,
        buttons = {
            DialogCancelButton(
                text = "Отмена",
                onClick = onCancel
            )
            Spacer(modifier = Modifier.width(8.dp))
            DialogSaveButton(
                text = "Добавить",
                enabled = isValid,
                onClick = {
                    val sanitizedKey = name.trim().lowercase()
                        .replace(Regex("[^a-z0-9а-я_]"), "_")
                        .ifEmpty { "field_${System.currentTimeMillis()}" }

                    val finalDefault = when (type) {
                        LayerFieldType.BOOLEAN -> if (booleanDefault) "true" else "false"
                        LayerFieldType.SELECT -> if (defaultValue.isNotBlank()) defaultValue else options.firstOrNull() ?: ""
                        else -> defaultValue.trim()
                    }

                    onAdd(
                        LayerFieldDefinition(
                            key = sanitizedKey,
                            name = name.trim(),
                            type = type,
                            defaultValue = finalDefault,
                            options = if (type == LayerFieldType.SELECT) options else emptyList()
                        )
                    )
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Field Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название поля") },
                placeholder = { Text("Например: Опасно, Глубина, Район") },
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

            // Field Type Selection
            Text(
                text = "Тип данных:",
                fontSize = 12.5.sp,
                color = AppColors.textSecondary,
                fontWeight = FontWeight.Medium
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LayerFieldType.entries.forEach { fieldType ->
                    val isSelected = type == fieldType
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) AccentSkyBlue.copy(alpha = 0.1f) else AppColors.bgSurface)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) AccentSkyBlue.copy(alpha = 0.5f) else AppColors.borderColor,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = AppColors.pressedColor),
                                onClick = { type = fieldType }
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { type = fieldType },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AccentSkyBlue,
                                unselectedColor = AppColors.borderColor
                            ),
                            modifier = Modifier.scale(0.85f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = fieldType.title,
                            fontSize = 13.sp,
                            color = if (isSelected) AppColors.textPrimary else AppColors.textSecondary,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            // Options input for SELECT
            if (type == LayerFieldType.SELECT) {
                OutlinedTextField(
                    value = optionsRaw,
                    onValueChange = { optionsRaw = it },
                    label = { Text("Варианты (через запятую)") },
                    placeholder = { Text("Шлямбур, Спит, Трос, Крюк") },
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

            // Default Value input
            when (type) {
                LayerFieldType.BOOLEAN -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppColors.bgSurface)
                            .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Значение по умолчанию: ${if (booleanDefault) "Да (True)" else "Нет (False)"}",
                            fontSize = 12.5.sp,
                            color = AppColors.textPrimary
                        )
                        Switch(
                            checked = booleanDefault,
                            onCheckedChange = { booleanDefault = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AccentSkyBlue,
                                checkedTrackColor = AccentSkyBlue.copy(alpha = 0.5f),
                                uncheckedThumbColor = AppColors.textSecondary,
                                uncheckedTrackColor = AppColors.bgCard
                            ),
                            modifier = Modifier.scale(0.85f)
                        )
                    }
                }
                LayerFieldType.SELECT -> {
                    if (options.isNotEmpty()) {
                        Text(
                            text = "Значение по умолчанию:",
                            fontSize = 12.5.sp,
                            color = AppColors.textSecondary
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            options.forEach { opt ->
                                val isSelected = defaultValue == opt || (defaultValue.isEmpty() && opt == options.first())
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) AccentSkyBlue.copy(alpha = 0.1f) else AppColors.bgSurface)
                                        .border(1.dp, if (isSelected) AccentSkyBlue else AppColors.borderColor, RoundedCornerShape(6.dp))
                                        .clickable { defaultValue = opt }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { defaultValue = opt },
                                        colors = RadioButtonDefaults.colors(selectedColor = AccentSkyBlue),
                                        modifier = Modifier.scale(0.8f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = opt, fontSize = 12.5.sp, color = AppColors.textPrimary)
                                }
                            }
                        }
                    }
                }
                LayerFieldType.NUMBER, LayerFieldType.TEXT -> {
                    OutlinedTextField(
                        value = defaultValue,
                        onValueChange = { defaultValue = it },
                        label = { Text("Значение по умолчанию (опционально)") },
                        placeholder = { Text(if (type == LayerFieldType.NUMBER) "0" else "Текст по умолчанию") },
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
        }
    }
}
