package com.vktrsansara.app.caveviewer.presentation.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import com.vktrsansara.app.caveviewer.ui.theme.CaveViewerTheme

/**
 * Information dialog for the raster project creation screen.
 */
@Composable
fun ProjectHelpDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppDialogContainer(
        title = "Справка: Новый проект",
        onDismissRequest = onDismiss,
        modifier = modifier,
        buttons = {
            DialogCancelButton(
                text = "Закрыть",
                onClick = onDismiss
            )
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Section 1: Storage
            Column {
                Text(
                    text = "Расположение проектов",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentSkyBlue
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Проекты хранятся в: Память телефона / Documents / CaveViewer / Projects /",
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    color = AppColors.textSecondary
                )
            }

            // Section 2: Map file
            Column {
                Text(
                    text = "Файл карты",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentSkyBlue
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Исходное изображение плана или схемы пещеры конвертируется в формат image.png и помещается в папку map/ проекта для мгновенной загрузки.",
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    color = AppColors.textSecondary
                )
            }
        }
    }
}

@Preview
@Composable
private fun ProjectHelpDialogPreview() {
    CaveViewerTheme(darkTheme = true) {
        ProjectHelpDialog(onDismiss = {})
    }
}
