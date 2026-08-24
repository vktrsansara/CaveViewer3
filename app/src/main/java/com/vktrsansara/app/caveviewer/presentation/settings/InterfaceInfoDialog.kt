package com.vktrsansara.app.caveviewer.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vktrsansara.app.caveviewer.ui.theme.BgCard
import com.vktrsansara.app.caveviewer.ui.theme.BgSurface
import com.vktrsansara.app.caveviewer.ui.theme.BorderColor
import com.vktrsansara.app.caveviewer.ui.theme.PressedColor
import com.vktrsansara.app.caveviewer.ui.theme.TextPrimary
import com.vktrsansara.app.caveviewer.ui.theme.TextSecondary

/**
 * Information dialog for the "Interface" settings section.
 */
@Composable
fun InterfaceInfoDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(8.dp))
                .background(BgCard)
                .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = "Справка: Интерфейс",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 1.dp, color = BorderColor)
            Spacer(modifier = Modifier.height(12.dp))

            // Body
            Text(
                text = "Во весь экран",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Скрывает верхнюю системную шторку и нижнюю панель навигации Android для максимального увеличения рабочей области карты.",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Close button
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .clip(RoundedCornerShape(6.dp))
                    .background(BgSurface)
                    .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(6.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = PressedColor),
                        onClick = onDismiss
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Закрыть",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
    }
}

@Preview
@Composable
private fun InterfaceInfoDialogPreview() {
    InterfaceInfoDialog(onDismiss = {})
}
