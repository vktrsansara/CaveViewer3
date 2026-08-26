package com.vktrsansara.app.caveviewer.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

/**
 * Standardized primary action button for modal dialogs with clean typography matching DialogCancelButton.
 *
 * @param text The button label (e.g., "Сохранить", "Готово").
 * @param enabled Whether the button is clickable.
 * @param onClick Action callback when pressed.
 * @param modifier Custom layout modifier.
 */
@Composable
fun DialogSaveButton(
    text: String = "Сохранить",
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (enabled) AccentSkyBlue else AccentSkyBlue.copy(alpha = 0.3f))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White.copy(alpha = 0.3f)),
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Color(0xFF121820) else AppColors.textSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
