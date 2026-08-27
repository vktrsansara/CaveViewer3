package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldType
import com.vktrsansara.app.caveviewer.domain.model.LayerPoint
import com.vktrsansara.app.caveviewer.domain.model.PointLayer
import com.vktrsansara.app.caveviewer.presentation.components.DialogSaveButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import java.util.Locale

@Composable
fun PointDetailsCard(
    point: LayerPoint,
    layer: PointLayer,
    onEditClick: () -> Unit,
    onCenterMapClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(AppColors.bgCard.copy(alpha = 0.95f))
            .border(1.dp, AppColors.borderColor, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column {
            // Header: Point Shape icon + Point Name + Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Canvas(modifier = Modifier.size(20.dp)) {
                        drawPointShape(
                            shape = point.shape,
                            center = Offset(size.width / 2, size.height / 2),
                            sizePx = 8.dp.toPx(),
                            fillColor = Color(point.color.toInt())
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = point.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Закрыть",
                        tint = AppColors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Слой: ${layer.name}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AccentSkyBlue
                )
                Text(
                    text = "X: ${String.format(Locale.US, "%.1f", point.x)}, Y: ${String.format(Locale.US, "%.1f", point.y)} px",
                    fontSize = 11.sp,
                    color = AppColors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
            Spacer(modifier = Modifier.height(8.dp))

            // Custom fields list (Dynamic properties)
            if (layer.fieldsSchema.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    layer.fieldsSchema.forEach { field ->
                        val value = point.customValues[field.key] ?: field.defaultValue
                        if (field.type == LayerFieldType.BOOLEAN) {
                            val isTrue = value.equals("true", ignoreCase = true)
                            Row(
                                modifier = Modifier.padding(vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${field.name}: ",
                                    fontSize = 12.5.sp,
                                    color = AppColors.textSecondary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isTrue) Color(0x33EF4444) else Color(0x3310B981))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isTrue) "Да" else "Нет",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTrue) Color(0xFFEF4444) else Color(0xFF10B981)
                                    )
                                }
                            }
                        } else if (value.isNotBlank()) {
                            Row(modifier = Modifier.padding(vertical = 1.dp)) {
                                Text(
                                    text = "${field.name}: ",
                                    fontSize = 12.5.sp,
                                    color = AppColors.textSecondary
                                )
                                Text(
                                    text = value,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.textPrimary
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Action buttons: Delete, Center on Map, Edit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Удалить точку",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onCenterMapClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MyLocation,
                        contentDescription = "Центрировать на карте",
                        tint = AccentSkyBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                DialogSaveButton(
                    text = "Изменить",
                    onClick = onEditClick
                )
            }
        }
    }
}
