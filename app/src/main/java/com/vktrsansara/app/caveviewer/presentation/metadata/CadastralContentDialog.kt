package com.vktrsansara.app.caveviewer.presentation.metadata

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.CadastralItem
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

/**
 * Modal dialog for navigating through cadastral records within a section (Table of Contents).
 */
@Composable
fun CadastralContentDialog(
    sectionTitle: String,
    items: List<CadastralItem>,
    onSelectIndex: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Содержимое: $sectionTitle",
        onDismissRequest = onDismiss,
        buttons = {
            DialogCancelButton(
                text = "Закрыть",
                onClick = onDismiss
            )
        }
    ) {
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Список пуст",
                    fontSize = 13.sp,
                    color = AppColors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items.forEachIndexed { index, item ->
                    val displayTitle = if (item.title.isNotBlank()) item.title else "Без названия"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppColors.bgSurface)
                            .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = AppColors.pressedColor),
                                onClick = {
                                    onSelectIndex(index)
                                    onDismiss()
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#${index + 1}: $displayTitle",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
