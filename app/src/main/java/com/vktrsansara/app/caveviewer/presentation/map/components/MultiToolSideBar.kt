package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.domain.model.ToolType
import com.vktrsansara.app.caveviewer.presentation.components.FloatingBarButton
import com.vktrsansara.app.caveviewer.presentation.components.FloatingBarContainer
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

/**
 * Расчет расстояния от верхнего края панели до кнопки "Закрыть" для MultiToolSideBar.
 */
fun getMultiToolSideBarCloseOffset(toolCount: Int): Dp {
    return (40 * toolCount + 53).dp
}

@Composable
fun MultiToolSideBar(
    dockedTools: List<ToolType>,
    activeTool: ToolType?,
    isFavorite: Boolean,
    onSelectTool: (ToolType) -> Unit,
    onUndoActiveToolPoint: () -> Unit,
    onCloseClick: () -> Unit,
    onCloseAllLongClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenHelp: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (dockedTools.isEmpty()) return

    FloatingBarContainer(modifier = modifier) {
        // 1. Стек иконок инструментов (кнопки 34x34 dp) - разворачивается вверх от Закрыть
        dockedTools.forEach { tool ->
            val isActive = tool == activeTool
            FloatingBarButton(
                icon = tool.icon,
                contentDescription = tool.title,
                isActive = isActive,
                activeColor = tool.color,
                iconTint = if (isActive) tool.color else AppColors.textSecondary,
                onClick = { onSelectTool(tool) }
            )
        }

        HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor, modifier = Modifier.width(26.dp))

        // 2. Кнопка Отмена
        FloatingBarButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Отменить точку",
            onClick = onUndoActiveToolPoint
        )

        // 3. Кнопка Закрыть (фиксированный якорь)
        FloatingBarButton(
            icon = Icons.Rounded.Close,
            contentDescription = "Закрыть / Долгое нажатие - закрыть все",
            isDanger = true,
            onClick = onCloseClick,
            onLongClick = onCloseAllLongClick
        )

        HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor, modifier = Modifier.width(26.dp))

        // 4. Кнопка Избранное (⭐) - разворачивается вниз от Закрыть
        FloatingBarButton(
            icon = Icons.Rounded.Star,
            contentDescription = "Избранный набор",
            iconTint = if (isFavorite) Color(0xFFF59E0B) else AppColors.textSecondary,
            onClick = onToggleFavorite
        )

        // 5. Кнопка Справка по панели (ℹ️) - разворачивается вниз от Закрыть
        FloatingBarButton(
            icon = Icons.Rounded.Info,
            contentDescription = "Справка",
            iconTint = Color(0xFF10B981),
            onClick = onOpenHelp
        )
    }
}
