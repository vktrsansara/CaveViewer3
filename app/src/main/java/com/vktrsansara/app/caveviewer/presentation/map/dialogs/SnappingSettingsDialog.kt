package com.vktrsansara.app.caveviewer.presentation.map.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.IntersectionMode
import com.vktrsansara.app.caveviewer.domain.model.SnappingSettings
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.presentation.components.DialogSaveButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import kotlin.math.roundToInt

@Composable
fun SnappingSettingsDialog(
    currentSettings: SnappingSettings,
    onApply: (SnappingSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var isEnabled by remember(currentSettings) { mutableStateOf(currentSettings.isEnabled) }
    var snapRadiusDp by remember(currentSettings) { mutableFloatStateOf(currentSettings.snapRadiusDp) }
    var snapToVertices by remember(currentSettings) { mutableStateOf(currentSettings.snapToVertices) }
    var snapToEdges by remember(currentSettings) { mutableStateOf(currentSettings.snapToEdges) }
    var snapToPoints by remember(currentSettings) { mutableStateOf(currentSettings.snapToPoints) }
    var snapPointsToLines by remember(currentSettings) { mutableStateOf(currentSettings.snapPointsToLines) }
    var intersectionMode by remember(currentSettings) { mutableStateOf(currentSettings.intersectionMode) }

    var isHelpOpen by remember { mutableStateOf(false) }

    if (isHelpOpen) {
        SnappingHelpDialog(onDismiss = { isHelpOpen = false })
    }

    AppDialogContainer(
        title = "Настройки привязки",
        onDismissRequest = onDismiss,
        onInfoClick = { isHelpOpen = true },
        buttons = {
            DialogCancelButton(
                text = "Отмена",
                onClick = onDismiss
            )
            Spacer(modifier = Modifier.width(8.dp))
            DialogSaveButton(
                text = "Применить",
                onClick = {
                    val updated = currentSettings.copy(
                        isEnabled = isEnabled,
                        snapRadiusDp = snapRadiusDp,
                        snapToVertices = snapToVertices,
                        snapToEdges = snapToEdges,
                        snapToPoints = snapToPoints,
                        snapPointsToLines = snapPointsToLines,
                        intersectionMode = intersectionMode
                    )
                    onApply(updated)
                    onDismiss()
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Главный переключатель магнита
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(AppColors.bgSurface)
                    .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Магнитная привязка",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textPrimary
                    )
                    Text(
                        text = "Притягивать курсор к вершинам и объектам",
                        fontSize = 11.sp,
                        color = AppColors.textSecondary
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { isEnabled = it },
                    modifier = Modifier.scale(0.8f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentSkyBlue,
                        checkedTrackColor = AccentSkyBlue.copy(alpha = 0.3f),
                        uncheckedThumbColor = AppColors.textSecondary,
                        uncheckedTrackColor = AppColors.bgMain
                    )
                )
            }

            // Настройка радиуса захвата (2..20 px)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(AppColors.bgSurface)
                    .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Радиус захвата привязки:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isEnabled) AccentSkyBlue else AppColors.textSecondary
                    )
                    Text(
                        text = "${snapRadiusDp.roundToInt()} px",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) AccentSkyBlue else AppColors.textSecondary
                    )
                }
                Slider(
                    value = snapRadiusDp,
                    onValueChange = { snapRadiusDp = it.roundToInt().toFloat() },
                    valueRange = 2f..20f,
                    steps = 17,
                    enabled = isEnabled,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentSkyBlue,
                        activeTrackColor = AccentSkyBlue,
                        inactiveTrackColor = AppColors.borderColor,
                        disabledThumbColor = AppColors.textSecondary,
                        disabledActiveTrackColor = AppColors.borderColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "2 px (вплотную)",
                        fontSize = 10.5.sp,
                        color = AppColors.textSecondary
                    )
                    Text(
                        text = "20 px (широкий)",
                        fontSize = 10.5.sp,
                        color = AppColors.textSecondary
                    )
                }
            }

            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)

            // Секция 1: Привязка к:
            Text(
                text = "Привязка к:",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccentSkyBlue
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SnapCheckboxRow(
                    title = "К вершинам линий",
                    subtitle = "Примагничивание к углам и поворотам ходов",
                    isChecked = snapToVertices,
                    enabled = isEnabled,
                    onCheckedChange = { snapToVertices = it }
                )

                SnapCheckboxRow(
                    title = "К ребрам (линиям)",
                    subtitle = "Проекция на отрезок между вершинами",
                    isChecked = snapToEdges,
                    enabled = isEnabled,
                    onCheckedChange = { snapToEdges = it }
                )

                SnapCheckboxRow(
                    title = "К точкам слоев",
                    subtitle = "Примагничивание к пикетам и объектам",
                    isChecked = snapToPoints,
                    enabled = isEnabled,
                    onCheckedChange = { snapToPoints = it }
                )

                SnapCheckboxRow(
                    title = "Привязывать точки к линиям",
                    subtitle = "Примагничивание меток и пикетов к ходам и вершинам",
                    isChecked = snapPointsToLines,
                    enabled = isEnabled,
                    onCheckedChange = { snapPointsToLines = it }
                )
            }

            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)

            // Секция 2: Связывать перекрестки?:
            Text(
                text = "Связывать перекрестки?:",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccentSkyBlue
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                IntersectionOptionRow(
                    title = "Нет",
                    subtitle = "Пересекающиеся линии остаются независимыми",
                    isSelected = intersectionMode == IntersectionMode.NO,
                    enabled = isEnabled,
                    onClick = { intersectionMode = IntersectionMode.NO }
                )

                IntersectionOptionRow(
                    title = "Да",
                    subtitle = "Автоматически создавать общую вершину на пересечении линий",
                    isSelected = intersectionMode == IntersectionMode.YES,
                    enabled = isEnabled,
                    onClick = { intersectionMode = IntersectionMode.YES }
                )

                IntersectionOptionRow(
                    title = "Спрашивать",
                    subtitle = "Запрашивать подтверждение при пересечении",
                    isSelected = intersectionMode == IntersectionMode.ASK,
                    enabled = isEnabled,
                    onClick = { intersectionMode = IntersectionMode.ASK }
                )
            }
        }
    }
}

@Composable
private fun SnapCheckboxRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val alpha = if (enabled) 1f else 0.4f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isChecked && enabled) AccentSkyBlue.copy(alpha = 0.08f) else AppColors.bgSurface)
            .border(
                width = 1.dp,
                color = if (isChecked && enabled) AccentSkyBlue.copy(alpha = 0.5f) else AppColors.borderColor,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(enabled = enabled) { onCheckedChange(!isChecked) }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = CheckboxDefaults.colors(
                checkedColor = AccentSkyBlue,
                uncheckedColor = AppColors.textSecondary
            ),
            modifier = Modifier.scale(0.8f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isChecked && enabled) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isChecked && enabled) AccentSkyBlue else AppColors.textPrimary.copy(alpha = alpha)
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = AppColors.textSecondary.copy(alpha = alpha),
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun IntersectionOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.4f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected && enabled) AccentSkyBlue.copy(alpha = 0.08f) else AppColors.bgSurface)
            .border(
                width = 1.dp,
                color = if (isSelected && enabled) AccentSkyBlue else AppColors.borderColor,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(
                selectedColor = AccentSkyBlue,
                unselectedColor = AppColors.textSecondary
            ),
            modifier = Modifier.scale(0.8f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected && enabled) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected && enabled) AccentSkyBlue else AppColors.textPrimary.copy(alpha = alpha)
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = AppColors.textSecondary.copy(alpha = alpha),
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun SnappingHelpDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Магнитная привязка",
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
                title = "Назначение магнита:",
                description = "Позволяет точно состыковывать ходы пещеры, соединять ответвления и привязывать линии к существующим пикетам без погрешности позиционирования."
            )

            HelpItem(
                title = "Радиус захвата (2–20 px):",
                description = "Определяет дистанцию срабатывания магнита. Значения 2–4 px позволяют комфортно чертить густые сети ходов пещеры без ложных притяжений, примагничиваясь только при точном наведении центра визира вплотную к вершине или ребру."
            )

            HelpItem(
                title = "Привязка к вершинам:",
                description = "При приближении курсора к любой вершине видимых слоев линий, визир автоматически захватывает точные координаты угла или стыка хода."
            )

            HelpItem(
                title = "Привязка к ребрам:",
                description = "Курсор проецируется на ближайшую линию хода под прямым углом, позволяя аккуратно начать примыкающий ход прямо от стены или осевой линии."
            )

            HelpItem(
                title = "Привязка к точкам:",
                description = "Обеспечивает стыковку линий ходов с пикетами съемочной сети, станциями навески и реперами."
            )

            HelpItem(
                title = "Привязка точек к линиям:",
                description = "При установке новых пикетов и меток в слоях точек, курсор точно захватывает вершины и стены ходов пещеры."
            )

            HelpItem(
                title = "Связывание перекрестков:",
                description = "При пересечении двух ходов система может автоматически создать общую топологическую вершину (узел сети), предотвращая топологические разрывы."
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
