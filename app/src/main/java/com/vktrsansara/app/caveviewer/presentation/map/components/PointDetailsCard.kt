package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldType
import com.vktrsansara.app.caveviewer.domain.model.LayerPoint
import com.vktrsansara.app.caveviewer.domain.model.PointLayer
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import java.util.Locale

@Composable
fun PointDetailsCard(
    point: LayerPoint,
    layer: PointLayer,
    isSimpleCrs: Boolean = true,
    canEdit: Boolean = false,
    onEditClick: () -> Unit,
    onCenterMapClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val maxCardHeight = configuration.screenHeightDp.dp * 0.5f
    var isConfirmingDelete by remember(point.id) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth(0.88f)
            .heightIn(max = maxCardHeight)
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.bgCard.copy(alpha = 0.95f))
            .border(1.dp, AppColors.borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. [ЗАГОЛОВОК]: Shape icon + Point Name + Close button (Red)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Canvas(modifier = Modifier.size(18.dp)) {
                        drawPointShape(
                            shape = point.shape,
                            center = Offset(size.width / 2, size.height / 2),
                            sizePx = 7.5.dp.toPx(),
                            fillColor = Color(point.color.toInt())
                        )
                    }
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = point.name.ifEmpty { "Точка без названия" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!canEdit) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(AppColors.bgSurface)
                                .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = AppColors.pressedColor),
                                    onClick = onCenterMapClick
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MyLocation,
                                contentDescription = "Фокусировка на точке",
                                tint = AccentSkyBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Закрыть",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            // 2. [РАЗДЕЛИТЕЛЬ 1]
            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
            Spacer(modifier = Modifier.height(6.dp))

            // 3. [АТРИБУТЫ]
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Слой
                Row(
                    modifier = Modifier.padding(vertical = 0.5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Слой: ",
                        fontSize = 12.sp,
                        color = AppColors.textSecondary
                    )
                    Text(
                        text = layer.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentSkyBlue
                    )
                }

                // Координаты (если система координат не Simple)
                if (!isSimpleCrs) {
                    Row(
                        modifier = Modifier.padding(vertical = 0.5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Координаты: ",
                            fontSize = 12.sp,
                            color = AppColors.textSecondary
                        )
                        Text(
                            text = "X: ${String.format(Locale.US, "%.1f", point.x)}, Y: ${String.format(Locale.US, "%.1f", point.y)} px",
                            fontSize = 11.5.sp,
                            color = AppColors.textPrimary
                        )
                    }
                }

                // Пользовательские атрибуты слоя
                layer.fieldsSchema.forEach { field ->
                    val value = point.customValues[field.key] ?: field.defaultValue
                    if (field.type == LayerFieldType.BOOLEAN) {
                        val isTrue = value.equals("true", ignoreCase = true)
                        Row(
                            modifier = Modifier.padding(vertical = 0.5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${field.name}: ",
                                fontSize = 12.sp,
                                color = AppColors.textSecondary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isTrue) Color(0x33EF4444) else Color(0x3310B981))
                                    .padding(horizontal = 5.dp, vertical = 1.5.dp)
                            ) {
                                Text(
                                    text = if (isTrue) "Да" else "Нет",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTrue) Color(0xFFEF4444) else Color(0xFF10B981)
                                )
                            }
                        }
                    } else if (value.isNotBlank()) {
                        Row(
                            modifier = Modifier.padding(vertical = 0.5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${field.name}: ",
                                fontSize = 12.sp,
                                color = AppColors.textSecondary
                            )
                            Text(
                                text = value,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.textPrimary
                            )
                        }
                    }
                }
            }

            if (canEdit) {
                Spacer(modifier = Modifier.height(6.dp))
                // 4. [РАЗДЕЛИТЕЛЬ 2]
                HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
                Spacer(modifier = Modifier.height(6.dp))

                // 5. [КНОПКИ]: [Удалить] (Left) [Фокусировка] (Strict Center) [Изменить] (Right)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                ) {
                    // 1. Delete (Left) - Inline confirmation logic
                    Box(modifier = Modifier.align(Alignment.CenterStart)) {
                        if (!isConfirmingDelete) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AppColors.bgSurface)
                                    .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(color = AppColors.pressedColor),
                                        onClick = { isConfirmingDelete = true }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = "Удалить точку",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Confirm Delete (Checkmark Red)
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                        .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(6.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(color = AppColors.pressedColor),
                                            onClick = {
                                                isConfirmingDelete = false
                                                onDeleteClick()
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Подтвердить удаление",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Cancel Delete (Cross Blue)
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AccentSkyBlue.copy(alpha = 0.15f))
                                        .border(1.dp, AccentSkyBlue, RoundedCornerShape(6.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(color = AppColors.pressedColor),
                                            onClick = { isConfirmingDelete = false }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Отмена удаления",
                                        tint = AccentSkyBlue,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Focus / Center on Map (Strictly anchored at exact Center of the card)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppColors.bgSurface)
                            .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = AppColors.pressedColor),
                                onClick = onCenterMapClick
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MyLocation,
                            contentDescription = "Центрировать на карте",
                            tint = AccentSkyBlue,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // 3. Edit (Right) - Pencil Icon
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.CenterEnd)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppColors.bgSurface)
                            .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = AppColors.pressedColor),
                                onClick = onEditClick
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Редактировать точку",
                            tint = AccentSkyBlue,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

