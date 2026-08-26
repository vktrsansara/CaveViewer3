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

@Composable
fun MapFilterHelpDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Фильтры карты",
        onDismissRequest = onDismiss,
        buttons = {
            DialogCancelButton(text = "Закрыть", onClick = onDismiss)
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterHelpItem(
                title = "Повыш. контрастности",
                desc = "Усиливает слабые карандашные и тушевые линии на выцветших архивных сканах, делая текст и пикеты легко различимыми."
            )
            FilterHelpItem(
                title = "Инвер. цвета (Ночной режим)",
                desc = "Инвертирует белые поля карты в темные, а черные ходы — в белые. Не слепит глаза в темноте пещеры и экономит заряд батареи на OLED экранах."
            )
            FilterHelpItem(
                title = "Черно-белый",
                desc = "Обесцвечивает карту, удаляя желтизну старой миллиметровки и цветовые шумы сканирования."
            )
            FilterHelpItem(
                title = "Инверсия (Ч/Б Негатив)",
                desc = "Монохромный негатив с максимальной резкостью контуров ходов и залов."
            )
        }
    }
}

@Composable
private fun FilterHelpItem(title: String, desc: String) {
    Column {
        Text(text = title, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = AccentSkyBlue)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, fontSize = 12.sp, lineHeight = 17.sp, color = AppColors.textSecondary)
    }
}
