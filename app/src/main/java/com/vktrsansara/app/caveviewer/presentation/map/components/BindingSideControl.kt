package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.R
import com.vktrsansara.app.caveviewer.presentation.components.BarIconButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentRed
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

/**
 * Floating side bar for calibration modes styled identically to the FloatingBottomBar.
 * Contains:
 * - "Undo" button (undo_24 vector, amber tint) when points are placed.
 * - "Close" button (Close icon, red tint) to cancel calibration mode.
 */
@Composable
fun BindingSideControl(
    pointsCount: Int,
    onClose: () -> Unit,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.barBackground)
            .border(
                width = 1.dp,
                color = AppColors.borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Undo button (visible when at least 1 point is placed)
        AnimatedVisibility(
            visible = pointsCount > 0,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f)
        ) {
            BarIconButton(
                painter = painterResource(R.drawable.undo_24),
                contentDescription = "Отменить точку",
                onClick = onUndo,
                tint = Color(0xFFFB923C) // Warm Amber/Orange
            )
        }

        // Close button (always available to exit mode)
        BarIconButton(
            icon = Icons.Rounded.Close,
            contentDescription = "Закрыть режим привязки",
            onClick = onClose,
            tint = AccentRed
        )
    }
}
