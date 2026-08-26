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
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            HelpFilterSection(
                title = "Инверсия (Ночной ч/б)",
                desc = "Полное обесцвечивание в градации серого и инверсия (глубокий черный фон и яркие белые контуры ходов). Создан для работы в темноте пещеры и существенно экономит заряд батареи на OLED-экранах."
            )
            HelpFilterSection(
                title = "Красный ночной",
                desc = "Монохромный рубиново-красный негатив на черном фоне. Красный спектр не разрушает светочувствительный пигмент (родопсин) глаз. Позволяет смотреть в карту и моментально переводить взгляд в темноту пещеры без ослепления."
            )
            HelpFilterSection(
                title = "Инвер. цвета (RGB Негатив)",
                desc = "Поканальная инверсия всех RGB-каналов. Белый фон становится черным, но цветные слои (синяя вода, красная тяга воздуха, зеленые отметки) сохраняют свои контрастные цветовые оттенки."
            )
            HelpFilterSection(
                title = "Повыш. контрастности",
                desc = "Усиливает резкость слабых карандашных линий, выцветшей туши и рукописных отметок пикетов на архивных планах на 60%."
            )
            HelpFilterSection(
                title = "Теплый / Защита глаз (Янтарь)",
                desc = "Мягкий янтарно-сепийный оттенок, полностью блокирующий резкое синее свечение экрана. Снимает усталость глаз при долгой работе с картой в подземном базовом лагере."
            )
            HelpFilterSection(
                title = "Чертёж (Синька / Blueprint)",
                desc = "Классический инженерный чертежный стиль: глубокий темно-синий фон с неоново-голубыми контурами ходов. Высокая читаемость мелких цифр и эстетичный контраст."
            )
            HelpFilterSection(
                title = "Черно-белый",
                desc = "Удаляет желтизну архивной бумаги, следы старого скотча, пятна и цветные шумы камеры при съемке старых бумажных топосъемок."
            )
            HelpFilterSection(
                title = "Макс. резкость / Бинаризация",
                desc = "Жесткий пороговый фильтр: отсекает все полутона, оставляя только 100% черный и 100% белый. Полностью удаляет серую грязь сканера, следы пальцев и разводы."
            )
            HelpFilterSection(
                title = "Очистка фона (Белый лист)",
                desc = "Осветляет серый и бежевый фон бумаги в чистый белый цвет (#FFFFFF), сохраняя насыщенность черных линий туши."
            )
            HelpFilterSection(
                title = "Усиление цвета",
                desc = "Увеличивает насыщенность цветных слоев на 120%. Помогает мгновенно разглядеть бледные цветные карандашные пометки (гидрология, геология, направления воздуха)."
            )
        }
    }
}

@Composable
private fun HelpFilterSection(title: String, desc: String) {
    Column {
        Text(text = title, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = AccentSkyBlue)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, fontSize = 12.sp, lineHeight = 17.sp, color = AppColors.textSecondary)
    }
}
