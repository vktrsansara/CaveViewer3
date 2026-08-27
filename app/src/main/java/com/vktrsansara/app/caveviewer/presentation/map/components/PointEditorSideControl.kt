package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PanToolAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.vktrsansara.app.caveviewer.domain.model.PointLayer
import com.vktrsansara.app.caveviewer.domain.model.PointPlacementMode
import com.vktrsansara.app.caveviewer.presentation.components.FloatingBarButton
import com.vktrsansara.app.caveviewer.presentation.components.FloatingBarContainer
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue

/**
 * Боковая стандартизированная плавающая панель управления для режима расстановки точек слоя (Point Editor).
 */
@Composable
fun PointEditorSideControl(
    layer: PointLayer,
    placementMode: PointPlacementMode,
    onAddPoint: () -> Unit,
    onClose: () -> Unit,
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingBarContainer(modifier = modifier) {
        // 1. Верхняя кнопка установки (динамическая иконка по режиму)
        when (placementMode) {
            PointPlacementMode.CURSOR_BUTTON_AND_TAP,
            PointPlacementMode.CURSOR_BUTTON_ONLY -> {
                FloatingBarButton(
                    icon = Icons.Rounded.Add,
                    contentDescription = "Добавить точку",
                    iconTint = Color(layer.defaultColor.toInt()),
                    onClick = onAddPoint
                )
            }
            PointPlacementMode.CURSOR_TAP_ONLY -> {
                FloatingBarButton(
                    icon = Icons.Rounded.TouchApp,
                    contentDescription = "Режим тапа от курсора",
                    iconTint = Color(layer.defaultColor.toInt()),
                    onClick = { /* Индикатор: действия по кнопке отключены */ }
                )
            }
            PointPlacementMode.FREE_TAP -> {
                FloatingBarButton(
                    icon = Icons.Rounded.PanToolAlt,
                    contentDescription = "Свободная установка тапом",
                    iconTint = Color(layer.defaultColor.toInt()),
                    onClick = { /* Индикатор: действия по кнопке отключены */ }
                )
            }
        }

        // 2. Кнопка Закрыть [✕] (Красная)
        FloatingBarButton(
            icon = Icons.Rounded.Close,
            contentDescription = "Завершить редактирование",
            isDanger = true,
            onClick = onClose
        )

        // 3. Кнопка «Управление» (Шестерёнка)
        FloatingBarButton(
            icon = Icons.Rounded.Settings,
            contentDescription = "Управление",
            iconTint = AccentSkyBlue,
            onClick = onSettingsClick
        )

        // 4. Кнопка «Справка» (Зелёная)
        FloatingBarButton(
            icon = Icons.Rounded.Info,
            contentDescription = "Справка по режиму",
            iconTint = Color(0xFF10B981),
            onClick = onHelpClick
        )
    }
}
