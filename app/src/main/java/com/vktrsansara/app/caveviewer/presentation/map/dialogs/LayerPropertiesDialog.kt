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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldDateTimeUtils
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldDefinition
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldType
import com.vktrsansara.app.caveviewer.domain.model.PointLayer
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.presentation.components.DialogSaveButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

@Composable
fun LayerPropertiesDialog(
    layer: PointLayer,
    onAddCustomFieldClick: () -> Unit,
    onEditFieldClick: (field: LayerFieldDefinition) -> Unit,
    onDeleteField: (fieldKey: String) -> Unit,
    onDismiss: () -> Unit
) {
    var fieldPendingDelete by remember { mutableStateOf<LayerFieldDefinition?>(null) }
    var isHelpOpen by remember { mutableStateOf(false) }

    if (isHelpOpen) {
        LayerPropertiesHelpDialog(onDismiss = { isHelpOpen = false })
    }

    if (fieldPendingDelete != null) {
        val target = fieldPendingDelete!!
        AppDialogContainer(
            title = "Удаление поля",
            onDismissRequest = { fieldPendingDelete = null },
            buttons = {
                DialogCancelButton(
                    text = "Отмена",
                    onClick = { fieldPendingDelete = null }
                )
                Spacer(modifier = Modifier.width(8.dp))
                DialogSaveButton(
                    text = "Удалить",
                    onClick = {
                        onDeleteField(target.key)
                        fieldPendingDelete = null
                    }
                )
            }
        ) {
            Text(
                text = "Удалить поле «${target.name}» из схемы слоя? Значения этого поля в сохраненных точках будут удалены.",
                fontSize = 13.5.sp,
                color = AppColors.textPrimary,
                lineHeight = 19.sp
            )
        }
    }

    AppDialogContainer(
        title = "Свойства слоя: ${layer.name}",
        onDismissRequest = onDismiss,
        onInfoClick = { isHelpOpen = true },
        buttons = {
            DialogCancelButton(
                text = "Закрыть",
                onClick = onDismiss
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header with "+ Добавить поле" button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Кастомные поля (${layer.fieldsSchema.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.textSecondary
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AppColors.bgSurface)
                        .border(1.dp, AccentSkyBlue.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.pressedColor),
                            onClick = onAddCustomFieldClick
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Добавить поле",
                        tint = AccentSkyBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Добавить поле",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentSkyBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
            Spacer(modifier = Modifier.height(10.dp))

            // Fields List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (layer.fieldsSchema.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "У этого слоя пока нет дополнительных полей. Нажмите «Добавить поле», чтобы создать кастомные атрибуты (например, «Глубина», «Опасно», «Описание»).",
                            fontSize = 13.sp,
                            color = AppColors.textSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                } else {
                    layer.fieldsSchema.forEach { field ->
                        FieldItemCard(
                            field = field,
                            onEdit = { onEditFieldClick(field) },
                            onDelete = { fieldPendingDelete = field }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldItemCard(
    field: LayerFieldDefinition,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(AppColors.bgSurface)
            .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = field.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )
                // Type badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AccentSkyBlue.copy(alpha = 0.15f))
                        .border(1.dp, AccentSkyBlue.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = field.type.title,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentSkyBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val defaultDisplay = when (field.type) {
                LayerFieldType.BOOLEAN -> if (field.defaultValue == "true") "Да" else "Нет"
                LayerFieldType.SELECT -> {
                    val optsStr = field.options.joinToString(", ")
                    if (field.defaultValue.isNotEmpty()) "${field.defaultValue} (из: $optsStr)" else optsStr
                }
                LayerFieldType.DATETIME -> LayerFieldDateTimeUtils.getDisplayDefaultLabel(field.defaultValue)
                else -> if (field.defaultValue.isNotEmpty()) field.defaultValue else "не задано"
            }

            Text(
                text = "По умолчанию: $defaultDisplay",
                fontSize = 11.5.sp,
                color = AppColors.textSecondary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Edit button
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AppColors.bgCard)
                    .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = AppColors.pressedColor),
                        onClick = onEdit
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Редактировать поле",
                    tint = AccentSkyBlue,
                    modifier = Modifier.size(15.dp)
                )
            }

            // Delete button
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AppColors.bgCard)
                    .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = AppColors.pressedColor),
                        onClick = onDelete
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Удалить поле",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

/**
 * Help dialog explaining layer schema properties and custom attributes.
 */
@Composable
fun LayerPropertiesHelpDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Свойства слоя",
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
                title = "Схема свойств слоя:",
                description = "Задает структуру данных и набор настраиваемых полей (атрибутов), общих для всех точек текущего слоя."
            )

            HelpItem(
                title = "Добавление полей:",
                description = "Нажмите «+ Добавить поле», чтобы ввести новые характеристики (текст, число, дата/время, список снаряжения, флаг опасности)."
            )

            HelpItem(
                title = "Редактирование полей:",
                description = "Нажмите на иконку карандаша на карточке поля, чтобы изменить его название, тип данных, список вариантов или значение по умолчанию."
            )

            HelpItem(
                title = "Форма создания и редактирования:",
                description = "Все созданные поля автоматически отображаются при добавлении новых точек и в окне их редактирования."
            )

            HelpItem(
                title = "Удаление полей:",
                description = "При удалении поля оно удаляется из схемы слоя, а сохраненные значения этого поля очищаются во всех точках."
            )

            HelpItem(
                title = "Отображение в карточке точки:",
                description = "Заполненные свойства выводятся в подробной информационной карточке точки при тапе по ее маркеру на плане пещеры."
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

