package com.vktrsansara.app.caveviewer.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Polyline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

/**
 * Floating bottom control bar with 8.dp rounded shape and menu button,
 * plus dynamically added Magnet, Search, Point Layers, and Line Layers mode buttons.
 */
@Composable
fun FloatingBottomBar(
    onMenuClick: () -> Unit,
    showSearchButton: Boolean = false,
    onSearchClick: () -> Unit = {},
    showMagnetButton: Boolean = false,
    isMagnetEnabled: Boolean = true,
    onMagnetClick: () -> Unit = {},
    showNavigationButton: Boolean = false,
    isNavigationModeActive: Boolean = false,
    onNavigationClick: () -> Unit = {},
    isPointLayersModeActive: Boolean = false,
    onPointLayersClick: () -> Unit = {},
    onClosePointLayersClick: () -> Unit = {},
    isLineLayersModeActive: Boolean = false,
    onLineLayersClick: () -> Unit = {},
    onCloseLineLayersClick: () -> Unit = {},
    onCloseAllLayersClick: () -> Unit = {
        onClosePointLayersClick()
        onCloseLineLayersClick()
    },
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.barBackground)
            .border(
                width = 1.dp,
                color = AppColors.borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Кнопка магнита слева от кнопки Меню
        if (showMagnetButton) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.92f else 1f,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
                label = "MagnetButtonScale"
            )

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(RoundedCornerShape(6.dp))
                    .background(AppColors.bgCard)
                    .border(
                        width = 1.dp,
                        color = if (isMagnetEnabled) AccentSkyBlue.copy(alpha = 0.6f) else AppColors.borderColor,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(color = AppColors.pressedColor),
                        onClick = onMagnetClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                MagnetIcon(
                    size = 18.dp,
                    isEnabled = isMagnetEnabled
                )
            }
        }

        // Кнопка поиска слева от кнопки Меню
        if (showSearchButton) {
            BarIconButton(
                icon = Icons.Rounded.Search,
                contentDescription = "Поиск",
                tint = AccentSkyBlue,
                onClick = onSearchClick
            )
        }

        // Кнопка меню
        BarIconButton(
            icon = Icons.Rounded.Menu,
            contentDescription = "Меню",
            onClick = onMenuClick
        )

        // Кнопка режима «Навигация по ходам» справа от кнопки Меню
        if (showNavigationButton) {
            BarIconButton(
                icon = Icons.Rounded.Explore,
                contentDescription = "Навигация по ходам",
                tint = if (isNavigationModeActive) Color(0xFF06B6D4) else AccentSkyBlue,
                onClick = onNavigationClick
            )
        }

        // Кнопка режима «Слои точек»
        if (isPointLayersModeActive) {
            BarIconButton(
                icon = Icons.Rounded.AddLocationAlt,
                contentDescription = "Слои точек",
                tint = AccentSkyBlue,
                onClick = onPointLayersClick
            )
        }

        // Кнопка режима «Слои линий»
        if (isLineLayersModeActive) {
            BarIconButton(
                icon = Icons.Rounded.Polyline,
                contentDescription = "Слои линий",
                tint = Color(0xFF10B981),
                onClick = onLineLayersClick
            )
        }

        // Кнопка/кнопки закрытия режимов справа
        if (isPointLayersModeActive && isLineLayersModeActive) {
            // Единая кнопка закрытия обоих режимов
            BarIconButton(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Закрыть режимы слоев",
                tint = Color(0xFFEF4444),
                onClick = onCloseAllLayersClick
            )
        } else if (isPointLayersModeActive) {
            BarIconButton(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Закрыть режим слоев точек",
                tint = Color(0xFFEF4444),
                onClick = onClosePointLayersClick
            )
        } else if (isLineLayersModeActive) {
            BarIconButton(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Закрыть режим слоев линий",
                tint = Color(0xFFEF4444),
                onClick = onCloseLineLayersClick
            )
        }
    }
}

@Preview
@Composable
private fun FloatingBottomBarPreview() {
    FloatingBottomBar(
        onMenuClick = {},
        showMagnetButton = true,
        isMagnetEnabled = true,
        isPointLayersModeActive = true,
        isLineLayersModeActive = true
    )
}
