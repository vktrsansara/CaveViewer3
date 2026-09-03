package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.AltRoute
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.presentation.components.FloatingBarButton
import com.vktrsansara.app.caveviewer.presentation.components.FloatingBarContainer
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue

/**
 * Расчет расстояния от верхнего края панели до кнопки "Закрыть" для NavigationSideControl.
 * Обеспечивает строгое фиксирование кнопки Закрыть [✕] на высоте 66.7% экрана.
 */
fun getNavigationSideControlCloseOffset(hasAlternativeRoute: Boolean, pointsCount: Int): Dp {
    var buttonsAboveClose = 0
    if (hasAlternativeRoute) buttonsAboveClose++
    if (pointsCount > 0) buttonsAboveClose++ // Кнопка Отменить точку
    if (pointsCount > 1) buttonsAboveClose++ // Кнопка Сбросить все
    return (6 + buttonsAboveClose * 40).dp
}

/**
 * Плавающая боковая панель управления навигатором по пещере.
 */
@Composable
fun NavigationSideControl(
    hasAlternativeRoute: Boolean,
    pointsCount: Int,
    onToggleActiveRoute: () -> Unit,
    onUndoPoint: () -> Unit,
    onResetPoints: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingBarContainer(modifier = modifier) {
        // Кнопка Переключить маршрут (с основного на альтернативный и обратно)
        if (hasAlternativeRoute) {
            FloatingBarButton(
                icon = Icons.AutoMirrored.Rounded.AltRoute,
                contentDescription = "Переключить маршрут",
                iconTint = AccentSkyBlue,
                onClick = onToggleActiveRoute
            )
        }

        // Кнопка Отменить последнюю точку
        if (pointsCount > 0) {
            FloatingBarButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Отменить точку",
                iconTint = AccentSkyBlue,
                onClick = onUndoPoint
            )
        }

        // Кнопка Сбросить все точки (появляется от 2 точек)
        if (pointsCount > 1) {
            FloatingBarButton(
                icon = Icons.Rounded.RestartAlt,
                contentDescription = "Сбросить все точки",
                iconTint = AccentSkyBlue,
                onClick = onResetPoints
            )
        }

        // Кнопка Закрыть навигатор
        FloatingBarButton(
            icon = Icons.Rounded.Close,
            contentDescription = "Закрыть",
            isDanger = true,
            onClick = onClose
        )
    }
}
