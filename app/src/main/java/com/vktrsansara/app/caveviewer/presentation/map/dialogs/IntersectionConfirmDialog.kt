package com.vktrsansara.app.caveviewer.presentation.map.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.presentation.components.DialogSaveButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

@Composable
fun IntersectionConfirmDialog(
    lineName: String,
    onConfirm: () -> Unit,
    onDecline: () -> Unit
) {
    AppDialogContainer(
        title = "Связывание перекрестка",
        onDismissRequest = onDecline,
        buttons = {
            DialogCancelButton(
                text = "Не связывать",
                onClick = onDecline
            )
            Spacer(modifier = Modifier.width(8.dp))
            DialogSaveButton(
                text = "Создать узел",
                onClick = onConfirm
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Обнаружено геометрическое пересечение с линией «${if (lineName.isNotBlank()) lineName else "Линия"}».",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.textPrimary
            )
            Text(
                text = "Создать общий топологический узел (вершину) в точке пересечения?",
                fontSize = 12.5.sp,
                color = AccentSkyBlue,
                lineHeight = 17.sp
            )
        }
    }
}
