package com.vktrsansara.app.caveviewer.presentation.map.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
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
            HelpItem(
                title = "Наложение сетки:",
                description = "Накладывает координатную сетку строго поверх схемы пещеры, начиная от ее верхнего левого угла."
            )

            HelpItem(
                title = "Масштабная привязка (в метрах):",
                description = "При наличии масштабной привязки размер ячеек рассчитывается в метрах (по умолчанию 10 м или масштаб из метаданных карты)."
            )

            HelpItem(
                title = "Непривязанные схемы (в пикселях):",
                description = "Для непривязанных схем размер ячеек задается в пикселях (по умолчанию 100 px)."
            )

            HelpItem(
                title = "Цвет и прозрачность:",
                description = "Вы можете гибко настроить цвет и прозрачность линий в палитре."
            )
        }
    }
}

@Composable
private fun HelpItem(title: String, description: String) {
    Column {
        Text(
            text = title,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = AccentSkyBlue
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            fontSize = 12.sp,
            color = AppColors.textSecondary,
            lineHeight = 17.sp
        )
    }
}
