package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

/**
 * Dual-action floating side control button for scale binding mode.
 * - 0 points: Red "Close" button to cancel calibration mode.
 * - >=1 points: "Undo" button to remove the last placed point.
 */
@Composable
fun BindingSideControl(
    pointsCount: Int,
    onClose: () -> Unit,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUndo = pointsCount > 0
    val interactionSource = remember { MutableInteractionSource() }

    val borderColor = if (isUndo) AppColors.borderColor else Color(0xFFEF4444)
    val iconColor = if (isUndo) AppColors.textPrimary else Color(0xFFEF4444)
    val iconVector = if (isUndo) Icons.AutoMirrored.Filled.ArrowBack else Icons.Rounded.Close
    val contentDescription = if (isUndo) "Отменить точку" else "Закрыть привязку"

    Box(
        modifier = modifier
            .size(44.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.bgCard)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = AppColors.pressedColor),
                onClick = {
                    if (isUndo) onUndo() else onClose()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
    }
}
