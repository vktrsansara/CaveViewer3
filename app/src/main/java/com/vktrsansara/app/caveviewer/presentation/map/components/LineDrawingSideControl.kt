package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.vktrsansara.app.caveviewer.domain.model.LineLayer
import com.vktrsansara.app.caveviewer.presentation.components.FloatingBarButton
import com.vktrsansara.app.caveviewer.presentation.components.FloatingBarContainer
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

/**
 * Floating side control bar for interactive line drawing mode.
 * Contains:
 * - [+] Add vertex at center cursor
 * - [←] Undo last vertex
 * - [✓] Complete drawing and open EditLineDialog (enabled when >= 2 vertices)
 * - [✕] Cancel drawing and exit mode
 */
@Composable
fun LineDrawingSideControl(
    layer: LineLayer,
    pointsCount: Int,
    onAddVertex: () -> Unit,
    onUndo: () -> Unit,
    onComplete: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canUndo = pointsCount > 0
    val canComplete = pointsCount >= 2
    val layerColor = if (layer.isHeatmapEnabled) Color(0xFF10B981) else Color(layer.defaultColor.toInt())

    FloatingBarContainer(modifier = modifier) {
        // 1. Add vertex [+]
        FloatingBarButton(
            icon = Icons.Rounded.Add,
            contentDescription = "Добавить вершину",
            iconTint = layerColor,
            onClick = onAddVertex
        )

        // 2. Undo vertex [←]
        FloatingBarButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Отменить вершину",
            iconTint = if (canUndo) AccentSkyBlue else AppColors.textSecondary.copy(alpha = 0.4f),
            onClick = {
                if (canUndo) onUndo()
            }
        )

        // 3. Complete drawing [✓]
        FloatingBarButton(
            icon = Icons.Rounded.Check,
            contentDescription = "Завершить линию",
            iconTint = if (canComplete) Color(0xFF10B981) else AppColors.textSecondary.copy(alpha = 0.4f),
            onClick = {
                if (canComplete) onComplete()
            }
        )

        // 4. Cancel and Close [✕]
        FloatingBarButton(
            icon = Icons.Rounded.Close,
            contentDescription = "Отменить рисование",
            isDanger = true,
            onClick = onClose
        )
    }
}
