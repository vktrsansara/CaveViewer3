package com.vktrsansara.app.caveviewer.presentation.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

private val AmberTopoColor = Color(0xFFF59E0B)
private val PurpleTherionColor = Color(0xFFA78BFA)

/**
 * Modal dialog for selecting a project creation type with styled option cards and colorful semantic icons.
 */
@Composable
fun ProjectTypeDialog(
    onSelectRasterProject: () -> Unit,
    onSelectTopographyProject: () -> Unit,
    onSelectTherionProject: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppDialogContainer(
        title = "Новый проект",
        onDismissRequest = onDismiss,
        modifier = modifier,
        maxWidth = 360.dp,
        buttons = {
            DialogCancelButton(
                text = "Не стоит",
                onClick = onDismiss
            )
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Option 1: Растр + Слои (Sky Blue Layers)
            ProjectTypeCardButton(
                icon = Icons.Rounded.Layers,
                iconTint = AccentSkyBlue,
                title = "Растр + Слои",
                onClick = onSelectRasterProject
            )

            // Option 2: Топосъемка (Amber Topo)
            ProjectTypeCardButton(
                icon = Icons.Rounded.TableChart,
                iconTint = AmberTopoColor,
                title = "Топосъемка",
                onClick = onSelectTopographyProject
            )

            // Option 3: Therion (Purple Therion)
            ProjectTypeCardButton(
                icon = Icons.Rounded.AccountTree,
                iconTint = PurpleTherionColor,
                title = "Therion",
                onClick = onSelectTherionProject
            )
        }
    }
}

@Composable
private fun ProjectTypeCardButton(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(AppColors.bgSurface)
            .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = AppColors.pressedColor),
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(19.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = title,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.textPrimary
        )
    }
}
