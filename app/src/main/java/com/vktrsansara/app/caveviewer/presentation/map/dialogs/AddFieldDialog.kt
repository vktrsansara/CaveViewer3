package com.vktrsansara.app.caveviewer.presentation.map.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldDateTimeUtils
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldDefinition
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldType
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.presentation.components.DialogSaveButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import java.util.Locale

@Composable
fun AddFieldDialog(
    initialField: LayerFieldDefinition? = null,
    onSave: (field: LayerFieldDefinition) -> Unit,
    onCancel: () -> Unit
) {
    val isEditing = initialField != null
    var name by remember(initialField) { mutableStateOf(initialField?.name ?: "") }
    var type by remember(initialField) { mutableStateOf(initialField?.type ?: LayerFieldType.TEXT) }
    var defaultValue by remember(initialField) { mutableStateOf(initialField?.defaultValue ?: "") }
    var booleanDefault by remember(initialField) {
        mutableStateOf(initialField?.defaultValue.equals("true", ignoreCase = true))
    }
    var includeDateDefault by remember(initialField) {
        mutableStateOf(
            if (initialField != null && initialField.type == LayerFieldType.DATETIME) {
                initialField.defaultValue == LayerFieldDateTimeUtils.DEFAULT_NOW_DATETIME ||
                initialField.defaultValue == LayerFieldDateTimeUtils.DEFAULT_NOW_DATE
            } else true
        )
    }
    var includeTimeDefault by remember(initialField) {
        mutableStateOf(
            if (initialField != null && initialField.type == LayerFieldType.DATETIME) {
                initialField.defaultValue == LayerFieldDateTimeUtils.DEFAULT_NOW_DATETIME ||
                initialField.defaultValue == LayerFieldDateTimeUtils.DEFAULT_NOW_TIME
            } else true
        )
    }
    var optionsRaw by remember(initialField) {
        mutableStateOf(initialField?.options?.joinToString(", ") ?: "")
    }
    val initialSelectedDefaults = remember(initialField) {
        if (initialField != null && initialField.type == LayerFieldType.SELECT) {
            initialField.defaultValue.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        } else {
            emptySet()
        }
    }
    var selectedDefaultOptions by remember(initialField) {
        mutableStateOf(initialSelectedDefaults)
    }
    var isHelpOpen by remember { mutableStateOf(false) }

    val options = remember(optionsRaw) {
        optionsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    val isValid = name.isNotBlank() && (type != LayerFieldType.SELECT || options.isNotEmpty())

    if (isHelpOpen) {
        AddFieldHelpDialog(onDismiss = { isHelpOpen = false })
    }

    AppDialogContainer(
        title = if (isEditing) "Редактирование поля" else "Новое поле слоя",
        onDismissRequest = onCancel,
        onInfoClick = { isHelpOpen = true },
        buttons = {
            DialogCancelButton(
                text = "Отмена",
                onClick = onCancel
            )
            Spacer(modifier = Modifier.width(8.dp))
            DialogSaveButton(
                text = if (isEditing) "Сохранить" else "Добавить",
                enabled = isValid,
                onClick = {
                    val safeKey = if (isEditing) {
                        initialField!!.key
                    } else {
                        name.trim().lowercase(Locale.US)
                            .replace(Regex("[^a-z0-9а-я_]"), "_")
                            .ifBlank { "field_${System.currentTimeMillis()}" }
                    }

                    val finalDefault = when (type) {
                        LayerFieldType.BOOLEAN -> if (booleanDefault) "true" else "false"
                        LayerFieldType.SELECT -> options.filter { selectedDefaultOptions.contains(it) }.joinToString(", ")
                        LayerFieldType.DATETIME -> when {
                            includeDateDefault && includeTimeDefault -> LayerFieldDateTimeUtils.DEFAULT_NOW_DATETIME
                            includeDateDefault -> LayerFieldDateTimeUtils.DEFAULT_NOW_DATE
                            includeTimeDefault -> LayerFieldDateTimeUtils.DEFAULT_NOW_TIME
                            else -> ""
                        }
                        else -> defaultValue.trim()
                    }

                    onSave(
                        LayerFieldDefinition(
                            key = safeKey,
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Field Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название поля", fontSize = 12.5.sp) },
                placeholder = { Text("Например: Опасно, Глубина колодца, Дата замера", fontSize = 11.5.sp) },
                singleLine = true,
                textStyle = TextStyle(fontSize = 13.5.sp),
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

            // 2. Field Type Selection (2 rows of 2 + 1 full row for DateTime)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Тип данных:",
                    fontSize = 12.5.sp,
                    color = AppColors.textSecondary,
                    fontWeight = FontWeight.Medium
                )

                val types = LayerFieldType.entries
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TypeSelectionCard(
                        title = types[0].title,
                        isSelected = type == types[0],
                        modifier = Modifier.weight(1f),
                        onClick = { type = types[0] }
                    )
                    TypeSelectionCard(
                        title = types[1].title,
                        isSelected = type == types[1],
                        modifier = Modifier.weight(1f),
                        onClick = { type = types[1] }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TypeSelectionCard(
                        title = types[2].title,
                        isSelected = type == types[2],
                        modifier = Modifier.weight(1f),
                        onClick = { type = types[2] }
                    )
                    TypeSelectionCard(
                        title = types[3].title,
                        isSelected = type == types[3],
                        modifier = Modifier.weight(1f),
                        onClick = { type = types[3] }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TypeSelectionCard(
                        title = types[4].title,
                        isSelected = type == types[4],
                        modifier = Modifier.weight(1f),
                        onClick = { type = types[4] }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // 3. Options input for SELECT type
            if (type == LayerFieldType.SELECT) {
                OutlinedTextField(
                    value = optionsRaw,
                    onValueChange = { optionsRaw = it },
                    label = { Text("Варианты списка (через запятую)", fontSize = 12.5.sp) },
                    placeholder = { Text("Например: Шлямбур, Спит, Трос, Крюк", fontSize = 11.5.sp) },
                    textStyle = TextStyle(fontSize = 13.5.sp),
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

            // 4. Default Value input depending on selected type
            when (type) {
                LayerFieldType.DATETIME -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Значение по умолчанию (для новых точек):",
                            fontSize = 12.sp,
                            color = AppColors.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // [Чекбокс] Дата
                            DateTimeOptionCard(
                                title = "Дата",
                                isChecked = includeDateDefault,
                                modifier = Modifier.weight(1f),
                                onToggle = { includeDateDefault = !includeDateDefault }
                            )
                            // [Чекбокс] Время
                            DateTimeOptionCard(
                                title = "Время",
                                isChecked = includeTimeDefault,
                                modifier = Modifier.weight(1f),
                                onToggle = { includeTimeDefault = !includeTimeDefault }
                            )
                        }

                        // Live preview
                        val previewText = remember(includeDateDefault, includeTimeDefault) {
                            val nowFormatted = LayerFieldDateTimeUtils.formatNow(includeDateDefault, includeTimeDefault)
                            if (nowFormatted.isNotEmpty()) "Автоподстановка: «$nowFormatted»" else "Без значения по умолчанию (поле будет пустым)"
                        }
                        Text(
                            text = previewText,
                            fontSize = 11.5.sp,
                            color = AccentSkyBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                LayerFieldType.BOOLEAN -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppColors.bgSurface)
                            .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = AppColors.pressedColor),
                                onClick = { booleanDefault = !booleanDefault }
                            )
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "По умолчанию: ${if (booleanDefault) "Да (True)" else "Нет (False)"}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
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
                            modifier = Modifier.scale(0.8f)
                        )
                    }
                }
                LayerFieldType.SELECT -> {
                    if (options.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Значения по умолчанию (выберите нужные):",
                                fontSize = 12.sp,
                                color = AppColors.textSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                options.forEach { opt ->
                                    val isChecked = selectedDefaultOptions.contains(opt)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(34.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isChecked) AccentSkyBlue.copy(alpha = 0.12f) else AppColors.bgSurface)
                                            .border(1.dp, if (isChecked) AccentSkyBlue else AppColors.borderColor, RoundedCornerShape(6.dp))
                                            .clickable {
                                                selectedDefaultOptions = if (isChecked) {
                                                    selectedDefaultOptions - opt
                                                } else {
                                                    selectedDefaultOptions + opt
                                                }
                                            }
                                            .padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                selectedDefaultOptions = if (checked) {
                                                    selectedDefaultOptions + opt
                                                } else {
                                                    selectedDefaultOptions - opt
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = AccentSkyBlue,
                                                uncheckedColor = AppColors.borderColor,
                                                checkmarkColor = Color.Black
                                            ),
                                            modifier = Modifier.scale(0.75f)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = opt, fontSize = 12.sp, color = AppColors.textPrimary)
                                    }
                                }
                            }

                            // Live preview of selected defaults
                            val defaultsPreview = options.filter { selectedDefaultOptions.contains(it) }.joinToString(", ")
                            if (defaultsPreview.isNotEmpty()) {
                                Text(
                                    text = "По умолчанию: «$defaultsPreview»",
                                    fontSize = 11.5.sp,
                                    color = AccentSkyBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                LayerFieldType.NUMBER, LayerFieldType.TEXT -> {
                    OutlinedTextField(
                        value = defaultValue,
                        onValueChange = { defaultValue = it },
                        label = { Text("Значение по умолчанию (опционально)", fontSize = 12.5.sp) },
                        placeholder = { Text(if (type == LayerFieldType.NUMBER) "0" else "Текст по умолчанию", fontSize = 11.5.sp) },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 13.5.sp),
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

@Composable
private fun TypeSelectionCard(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) AccentSkyBlue.copy(alpha = 0.15f) else AppColors.bgSurface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) AccentSkyBlue else AppColors.borderColor,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = AppColors.pressedColor),
                onClick = onClick
            )
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = AccentSkyBlue,
                unselectedColor = AppColors.borderColor
            ),
            modifier = Modifier.scale(0.75f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            color = if (isSelected) AppColors.textPrimary else AppColors.textSecondary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun DateTimeOptionCard(
    title: String,
    isChecked: Boolean,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit
) {
    Row(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isChecked) AccentSkyBlue.copy(alpha = 0.12f) else AppColors.bgSurface)
            .border(
                width = if (isChecked) 1.5.dp else 1.dp,
                color = if (isChecked) AccentSkyBlue else AppColors.borderColor,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = AppColors.pressedColor),
                onClick = onToggle
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = AccentSkyBlue,
                uncheckedColor = AppColors.borderColor,
                checkmarkColor = Color(0xFF121212)
            ),
            modifier = Modifier.scale(0.8f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            fontSize = 12.5.sp,
            color = if (isChecked) AppColors.textPrimary else AppColors.textSecondary,
            fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

/**
 * Help dialog explaining custom layer fields and data types.
 */
@Composable
fun AddFieldHelpDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Новое поле слоя",
        onDismissRequest = onDismiss,
        buttons = {
            DialogCancelButton(
                text = "Закрыть",
                onClick = onDismiss
            )
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            HelpItem(
                title = "Кастомные поля слоя:",
                description = "Позволяют сохранять дополнительные спелеологические характеристики в точках (например, дату навески, глубину колодцев, состояние снаряжения, автора)."
            )

            HelpItem(
                title = "Тип «Текст»:",
                description = "Универсальное текстовое поле (название, комментарий, примечание к прохождению, контакты исследователя)."
            )

            HelpItem(
                title = "Тип «Число»:",
                description = "Числовой ввод для измерений и параметров (глубина колодца в метрах, диаметр хода, высота уступа)."
            )

            HelpItem(
                title = "Тип «Флаг (Да/Нет)»:",
                description = "Логический переключатель для бинарных свойств (признак опасности, обводнённости, наличия готового шлямбура или станции)."
            )

            HelpItem(
                title = "Тип «Список»:",
                description = "Набор вариантов, заданных через запятую. В форме точки можно выбирать один или сразу несколько пунктов с помощью чекбоксов (например: Вода, Глина, Песок)."
            )

            HelpItem(
                title = "Тип «Дата / Время»:",
                description = "Фиксирует дату и/или время замера. В качестве значения по умолчанию можно выбрать автоматическую подстановку Даты, Времени или Даты и Времени одновременно."
            )

            HelpItem(
                title = "Значение по умолчанию:",
                description = "Автоматически подставляется в форму при создании каждой новой точки данного слоя."
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

