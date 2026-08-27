package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.vktrsansara.app.caveviewer.domain.model.PointLayer
import com.vktrsansara.app.caveviewer.presentation.components.FloatingBarButton
import com.vktrsansara.app.caveviewer.presentation.components.FloatingBarContainer

/**
 * Боковая стандартизированная плавающая панель управления для режима расстановки точек слоя (Point Editor).
 */
@Composable
fun PointEditorSideControl(
    layer: PointLayer,
    onAddPoint: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingBarContainer(modifier = modifier) {
        // Кнопка Добавить точку [+]
        FloatingBarButton(
            icon = Icons.Rounded.Add,
            contentDescription = "Добавить точку",
            iconTint = Color(layer.defaultColor.toInt()),
            onClick = onAddPoint
        )

        // Кнопка Закрыть [✕]
        FloatingBarButton(
            icon = Icons.Rounded.Close,
            contentDescription = "Завершить редактирование",
            isDanger = true,
            onClick = onClose
        )
    }
}
