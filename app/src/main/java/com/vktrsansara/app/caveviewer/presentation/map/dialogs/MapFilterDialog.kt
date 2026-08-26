package com.vktrsansara.app.caveviewer.presentation.map.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.MapFilterMode
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

@Composable
fun MapFilterDialog(
    currentFilter: MapFilterMode,
    onFilterSelected: (MapFilterMode) -> Unit,
    onOpenHelp: () -> Unit,
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Фильтры",
        onDismissRequest = onDismiss,
        onInfoClick = onOpenHelp,
        buttons = {
            DialogCancelButton(text = "Закрыть", onClick = onDismiss)
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            MapFilterMode.entries.forEach { mode ->
                val isSelected = mode == currentFilter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.pressedColor),
                            onClick = { onFilterSelected(mode) }
                        )
                        .padding(horizontal = 6.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onFilterSelected(mode) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = AccentSkyBlue,
                            unselectedColor = AppColors.textSecondary
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = mode.title,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) AccentSkyBlue else AppColors.textPrimary
                    )
                }
            }
        }
    }
}
