package com.vktrsansara.app.caveviewer.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

/**
 * Universal container for all modal dialogs in the app.
 * Provides a pinned header, scrollable body (when content overflows), and a pinned footer with action buttons.
 */
@Composable
fun AppDialogContainer(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    maxWidth: Dp = 440.dp,
    maxHeightFraction: Float = 0.85f,
    onInfoClick: (() -> Unit)? = null,
    onCloseClick: (() -> Unit)? = null,
    isScrollable: Boolean = true,
    buttons: @Composable (RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = modifier
                    .widthIn(max = maxWidth)
                    .fillMaxWidth()
                    .fillMaxHeight(maxHeightFraction)
                    .wrapContentHeight()
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.bgCard)
                    .border(1.dp, AppColors.borderColor, RoundedCornerShape(10.dp))
            ) {
                // 1. Pinned Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    if (onInfoClick != null) {
                        IconButton(
                            onClick = onInfoClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = "Справка",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (onCloseClick != null) {
                        IconButton(
                            onClick = onCloseClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Закрыть",
                                tint = AppColors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)

                // 2. Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .then(if (isScrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    content()
                }

                // 3. Pinned Footer
                if (buttons != null) {
                    HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        buttons()
                    }
                }
            }
        }
    }
}
