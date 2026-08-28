package com.vktrsansara.app.caveviewer.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Polyline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

/**
 * Floating bottom control bar with 8.dp rounded shape and menu button,
 * plus dynamically added Point Layers and Line Layers mode buttons.
 */
@Composable
fun FloatingBottomBar(
    onMenuClick: () -> Unit,
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
        // Кнопка меню
        BarIconButton(
            icon = Icons.Rounded.Menu,
            contentDescription = "Меню",
            onClick = onMenuClick
        )

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
        isPointLayersModeActive = true,
        isLineLayersModeActive = true
    )
}
