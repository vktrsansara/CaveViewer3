package com.vktrsansara.app.caveviewer.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

/**
 * Floating bottom control bar with 8.dp rounded shape and menu button.
 *
 * @param onMenuClick Callback triggered when clicking the menu button.
 * @param modifier Custom modifier for styling and layout.
 */
@Composable
fun FloatingBottomBar(
    onMenuClick: () -> Unit,
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
        BarIconButton(
            icon = Icons.Rounded.Menu,
            contentDescription = "Меню",
            onClick = onMenuClick
        )
    }
}

@Preview
@Composable
private fun FloatingBottomBarPreview() {
    FloatingBottomBar(
        onMenuClick = {}
    )
}
