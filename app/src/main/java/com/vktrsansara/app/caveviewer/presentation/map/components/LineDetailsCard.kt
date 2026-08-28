package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Timeline
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.measure.LineColorUtils
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldType
import com.vktrsansara.app.caveviewer.domain.model.LayerLine
import com.vktrsansara.app.caveviewer.domain.model.LineEnvironmentType
import com.vktrsansara.app.caveviewer.domain.model.LineLayer
import com.vktrsansara.app.caveviewer.domain.model.LineStyle
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import java.util.Locale

/**
 * Floating inspection card displaying details of a selected cave polyline (LayerLine):
 * - Length and vertex count
 * - Difficulty badge (0.0..8.0)
 * - Environment halo badge
 * - Custom schema attributes
 * - Action buttons: Delete (with confirmation), Focus/Center, Edit
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LineDetailsCard(
    line: LayerLine,
    layer: LineLayer,
    ppm: Double,
    onEditClick: () -> Unit,
    onCenterMapClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val maxCardHeight = configuration.screenHeightDp.dp * 0.5f
    var isConfirmingDelete by remember(line.id) { mutableStateOf(false) }

    val diffColor = LineColorUtils.getDifficultyColor(line.difficulty)

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
            // 1. [HEADER]: Line icon + Line Name + Close button (Red)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(diffColor.copy(alpha = 0.2f))
                            .border(1.dp, diffColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Timeline,
                            contentDescription = "Линия",
                            tint = diffColor,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = line.name.ifEmpty { "Линия без названия" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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

            Spacer(modifier = Modifier.height(4.dp))
            // 2. Subheader: Layer Name
            Text(
                text = "Слой: ${layer.name}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccentSkyBlue
            )

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
            Spacer(modifier = Modifier.height(6.dp))

            // 3. [ATTRIBUTES & METRICS]
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Length and Vertices
                val lengthFormatted = if (ppm > 0.0) {
                    "${String.format(Locale.US, "%.2f", line.lengthMeters)} м"
                } else {
                    "${String.format(Locale.US, "%.1f", line.lengthPx)} px"
                }

                Row(
                    modifier = Modifier.padding(vertical = 0.5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Длина: ",
                        fontSize = 12.sp,
                        color = AppColors.textSecondary
                    )
                    Text(
                        text = "$lengthFormatted (${line.points.size} верш.)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary
                    )
                }

                // Metric Badges Flow
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Difficulty Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(diffColor.copy(alpha = 0.15f))
                            .border(1.dp, diffColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Сложность: ${String.format(Locale.US, "%.1f", line.difficulty)} / 8.0",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = diffColor
                        )
                    }

                    // Environment Halo Badge
                    if (line.environmentType != LineEnvironmentType.NONE) {
                        val envColor = LineColorUtils.getHaloColor(line.environmentType, line.haloColor) ?: AccentSkyBlue
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(envColor.copy(alpha = 0.15f))
                                .border(1.dp, envColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (line.environmentType == LineEnvironmentType.NONE) line.environmentType.title else "${line.environmentType.title.substringBefore(" /").substringBefore(" (")} ${line.environmentType.symbol}",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = envColor
                            )
                        }
                    }

                    // Line Style Badge
                    if (line.style != LineStyle.SOLID) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AppColors.bgSurface)
                                .border(1.dp, AppColors.borderColor, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = line.style.title,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.textSecondary
                            )
                        }
                    }
                }

                // Custom Schema Fields
                layer.fieldsSchema.forEach { field ->
                    val value = line.customValues[field.key] ?: field.defaultValue
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

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
            Spacer(modifier = Modifier.height(6.dp))

            // 4. [ACTION BUTTONS]: [Удалить] (Left), [Центрировать] (Center), [Изменить] (Right)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                // 1. Delete (Left)
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
                                contentDescription = "Удалить линию",
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
                        contentDescription = "Фокусировка на линии",
                        tint = AccentSkyBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // 3. Edit (Right)
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentSkyBlue.copy(alpha = 0.15f))
                        .border(1.dp, AccentSkyBlue.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.pressedColor),
                            onClick = onEditClick
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Изменить линию",
                        tint = AccentSkyBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Изменить",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentSkyBlue
                    )
                }
            }
        }
    }
}
