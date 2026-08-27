package com.vktrsansara.app.caveviewer.presentation.map.dialogs

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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.PointLayer
import com.vktrsansara.app.caveviewer.domain.model.PointShape
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.presentation.components.DialogSaveButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * Modal dialog for managing point layers (create, visibility, edit styling, custom schema, and delete).
 */
@Composable
fun LayerManagerDialog(
    layers: List<PointLayer>,
    pointCounts: Map<Long, Int> = emptyMap(),
    onCreateLayerClick: () -> Unit,
    onStartEditingPoints: (layer: PointLayer) -> Unit = {},
    onToggleVisibility: (layerId: Long, isVisible: Boolean) -> Unit,
    onEditStyle: (layer: PointLayer) -> Unit = {},
    onEditSchema: (layer: PointLayer) -> Unit = {},
    onDeleteLayer: (layerId: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var layerPendingDelete by remember { mutableStateOf<PointLayer?>(null) }

    if (layerPendingDelete != null) {
        val target = layerPendingDelete!!
        AppDialogContainer(
            title = "Удаление слоя",
            onDismissRequest = { layerPendingDelete = null },
            buttons = {
                DialogCancelButton(
                    text = "Отмена",
                    onClick = { layerPendingDelete = null }
                )
                Spacer(modifier = Modifier.width(8.dp))
                DialogSaveButton(
                    text = "Удалить",
                    onClick = {
                        onDeleteLayer(target.id)
                        layerPendingDelete = null
                    }
                )
            }
        ) {
            Text(
                text = "Вы действительно хотите удалить слой «${target.name}» и все точки в нем?",
                fontSize = 13.5.sp,
                color = AppColors.textPrimary,
                lineHeight = 19.sp
            )
        }
    }

    AppDialogContainer(
        title = "Слои точек",
        onDismissRequest = onDismiss,
        buttons = {
            DialogCancelButton(
                text = "Закрыть",
                onClick = onDismiss
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top action bar: "+ Создать слой" button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Слои (${layers.size})",
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
                            onClick = onCreateLayerClick
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Создать слой",
                        tint = AccentSkyBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Создать слой",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentSkyBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
            Spacer(modifier = Modifier.height(10.dp))

            // Layers List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (layers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Нет созданных слоев. Нажмите «Создать слой», чтобы добавить первый слой (например, «Пикеты» или «Опасности»).",
                            fontSize = 13.sp,
                            color = AppColors.textSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                } else {
                    layers.forEach { layer ->
                        val count = pointCounts[layer.id] ?: 0
                        LayerItemCard(
                            layer = layer,
                            pointsCount = count,
                            onStartEditingPoints = { onStartEditingPoints(layer) },
                            onToggleVisibility = { onToggleVisibility(layer.id, !layer.isVisible) },
                            onEditStyle = { onEditStyle(layer) },
                            onEditSchema = { onEditSchema(layer) },
                            onDelete = { layerPendingDelete = layer }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual layer card with shape indicator, point count, and action buttons.
 */
@Composable
private fun LayerItemCard(
    layer: PointLayer,
    pointsCount: Int,
    onStartEditingPoints: () -> Unit,
    onToggleVisibility: () -> Unit,
    onEditStyle: () -> Unit,
    onEditSchema: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(AppColors.bgSurface)
            .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Shape marker + Layer name + point count
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(AppColors.bgCard)
                    .border(1.dp, AppColors.borderColor, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                PointShapeMarker(
                    shape = layer.defaultShape,
                    color = Color(layer.defaultColor.toInt()),
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = layer.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatPointsCount(pointsCount),
                    fontSize = 11.5.sp,
                    color = AppColors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Right: 5 Action Buttons (28x28 dp)
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 0. Point Editor Mode on Map (Location Icon)
            LayerActionButton(
                icon = Icons.Rounded.AddLocationAlt,
                tint = AccentSkyBlue,
                contentDescription = "Расставить точки",
                onClick = onStartEditingPoints
            )

            // 1. Visibility toggle (Eye)
            LayerActionButton(
                icon = if (layer.isVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                tint = if (layer.isVisible) AccentSkyBlue else AppColors.textSecondary,
                contentDescription = if (layer.isVisible) "Скрыть слой" else "Показать слой",
                onClick = onToggleVisibility
            )

            // 2. Style Settings (Palette)
            LayerActionButton(
                icon = Icons.Rounded.Palette,
                tint = Color(0xFFF59E0B), // Amber
                contentDescription = "Стиль маркера",
                onClick = onEditStyle
            )

            // 3. Custom Fields / Schema (Tune)
            LayerActionButton(
                icon = Icons.Rounded.Tune,
                tint = Color(0xFF10B981), // Green
                contentDescription = "Свойства и поля",
                onClick = onEditSchema
            )

            // 4. Delete (Trash)
            LayerActionButton(
                icon = Icons.Rounded.Delete,
                tint = Color(0xFFEF4444), // Red
                contentDescription = "Удалить слой",
                onClick = onDelete
            )
        }
    }
}

/**
 * Compact 28x28 dp action button for layer cards.
 */
@Composable
private fun LayerActionButton(
    icon: ImageVector,
    tint: Color,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(AppColors.bgCard)
            .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = AppColors.pressedColor),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(15.dp)
        )
    }
}

/**
 * Canvas rendering the 7 available marker shapes.
 */
@Composable
fun PointShapeMarker(
    shape: PointShape,
    color: Color,
    modifier: Modifier = Modifier.size(18.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        when (shape) {
            PointShape.CIRCLE -> {
                drawCircle(color = color, radius = minOf(cx, cy))
            }
            PointShape.SQUARE -> {
                drawRect(color = color, topLeft = Offset(0f, 0f), size = size)
            }
            PointShape.TRIANGLE_UP -> {
                val path = Path().apply {
                    moveTo(cx, 0f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(path, color)
            }
            PointShape.TRIANGLE_DOWN -> {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w, 0f)
                    lineTo(cx, h)
                    close()
                }
                drawPath(path, color)
            }
            PointShape.DIAMOND -> {
                val path = Path().apply {
                    moveTo(cx, 0f)
                    lineTo(w, cy)
                    lineTo(cx, h)
                    lineTo(0f, cy)
                    close()
                }
                drawPath(path, color)
            }
            PointShape.STAR -> {
                val path = Path().apply {
                    val outerR = minOf(cx, cy)
                    val innerR = outerR * 0.45f
                    for (i in 0 until 10) {
                        val r = if (i % 2 == 0) outerR else innerR
                        val angle = (i * Math.PI / 5 - Math.PI / 2).toFloat()
                        val x = cx + r * cos(angle)
                        val y = cy + r * sin(angle)
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(path, color)
            }
            PointShape.CROSS -> {
                val strokePx = 2.5.dp.toPx()
                drawLine(color, Offset(cx, 0f), Offset(cx, h), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(color, Offset(0f, cy), Offset(w, cy), strokeWidth = strokePx, cap = StrokeCap.Round)
            }
        }
    }
}

private fun formatPointsCount(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    val suffix = when {
        mod100 in 11..19 -> "точек"
        mod10 == 1 -> "точка"
        mod10 in 2..4 -> "точки"
        else -> "точек"
    }
    return "$count $suffix"
}
