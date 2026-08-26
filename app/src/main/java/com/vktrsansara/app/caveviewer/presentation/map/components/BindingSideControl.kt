package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.presentation.components.FloatingBarButton
import com.vktrsansara.app.caveviewer.presentation.components.FloatingBarContainer

/**
 * Расчет расстояния от верхнего края панели до кнопки "Закрыть" для BindingSideControl.
 */
fun getBindingSideControlCloseOffset(pointsCount: Int): Dp {
    return if (pointsCount > 0) {
        46.dp // padding(6) + undo(34) + spacing(6)
    } else {
        6.dp  // padding(6)
    }
}

/**
 * Одиночная боковая панель управления (для 1 инструмента или режима привязки).
 */
@Composable
fun BindingSideControl(
    pointsCount: Int,
    onClose: () -> Unit,
    onUndo: () -> Unit,
    onHelp: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    FloatingBarContainer(modifier = modifier) {
        if (pointsCount > 0) {
            // Кнопка Отменить (шаг назад)
            FloatingBarButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Отменить точку",
                onClick = onUndo
            )
        }

        // Кнопка Закрыть
        FloatingBarButton(
            icon = Icons.Rounded.Close,
            contentDescription = "Закрыть",
            isDanger = true,
            onClick = onClose
        )

        // Кнопка Справка (если передана)
        if (onHelp != null) {
            FloatingBarButton(
                icon = Icons.Rounded.Info,
                contentDescription = "Справка",
                iconTint = Color(0xFF10B981),
                onClick = onHelp
            )
        }
    }
}
