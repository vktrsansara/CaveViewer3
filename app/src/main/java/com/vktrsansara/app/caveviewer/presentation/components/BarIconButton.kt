package com.vktrsansara.app.caveviewer.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

/**
 * Control bar item button (34x34 dp) with smooth press scale down animation.
 */
@Composable
fun BarIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
        label = "BarButtonScale"
    )

    Box(
        modifier = modifier
            .size(34.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(6.dp))
            .background(AppColors.bgCard)
            .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = AppColors.pressedColor),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = AppColors.textPrimary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Preview
@Composable
private fun BarIconButtonPreview() {
    BarIconButton(
        icon = Icons.Rounded.Menu,
        contentDescription = "Menu",
        onClick = {}
    )
}
