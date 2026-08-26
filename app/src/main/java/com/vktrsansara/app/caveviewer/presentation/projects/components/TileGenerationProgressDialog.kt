package com.vktrsansara.app.caveviewer.presentation.projects.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

/**
 * Modal progress dialog displayed during tile generation with an abort/cancel action.
 */
@Composable
fun TileGenerationProgressDialog(
    projectName: String,
    progressFraction: Float,
    statusText: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppDialogContainer(
        title = "Создание проекта: $projectName",
        onDismissRequest = onCancel,
        modifier = modifier,
        isScrollable = false,
        buttons = {
            DialogCancelButton(
                text = "Отмена",
                onClick = onCancel
            )
        }
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { progressFraction.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = AppColors.accent,
            trackColor = AppColors.bgSurface,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = statusText.ifEmpty { "Подготовка файлов..." },
            fontSize = 12.5.sp,
            color = AppColors.textSecondary
        )
    }
}
