package com.vktrsansara.app.caveviewer.presentation.map.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

/**
 * Help dialog explaining the Grid tool and sizing options.
 */
@Composable
fun GridHelpDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Сетка",
        onDismissRequest = onDismiss,
        buttons = {
            DialogCancelButton(
                text = "Закрыть",
                onClick = onDismiss
            )
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "• Накладывает координатную сетку строго поверх схемы пещеры, начиная от ее верхнего левого угла.",
                fontSize = 13.5.sp,
                color = AppColors.textPrimary,
                lineHeight = 19.sp
            )

            Text(
                text = "• При наличии масштабной привязки размер ячеек рассчитывается в метрах (по умолчанию 10 м или масштаб из метаданных карты).",
                fontSize = 13.5.sp,
                color = AppColors.textPrimary,
                lineHeight = 19.sp
            )

            Text(
                text = "• Для непривязанных схем размер задается в пикселях (по умолчанию 100 px).",
                fontSize = 13.5.sp,
                color = AppColors.textPrimary,
                lineHeight = 19.sp
            )

            Text(
                text = "• Вы можете гибко настроить цвет и прозрачность линий в палитре.",
                fontSize = 13.5.sp,
                color = AppColors.textPrimary,
                lineHeight = 19.sp
            )
        }
    }
}
