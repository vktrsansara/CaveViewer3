package com.vktrsansara.app.caveviewer.presentation.settings

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

/**
 * Information dialog for the "Interface" settings section.
 */
@Composable
fun InterfaceInfoDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Интерфейс",
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
                title = "Во весь экран",
                desc = "Скрывает системные панели Android для максимального расширения рабочей области карты."
            )
            HelpItem(
                title = "Полоска масштаба",
                desc = "Отображает в правом верхнем углу линейку реального масштаба в метрах с динамическим пересчетом при зуме."
            )
            HelpItem(
                title = "Показывать компас",
                desc = "Отображает в левом верхнем углу виджет компаса с азимутом направления на север."
            )
            HelpItem(
                title = "Нажатие на компас: Выравнивание по горизонтали",
                desc = "Клик по компасу плавно возвращает карту в исходную ориентацию растрового скана (поворот 0°)."
            )
            HelpItem(
                title = "Нажатие на компас: Выравнивание по экрану",
                desc = "Клик по компасу плавно поворачивает карту так, чтобы истинный север пещеры (angle_north) смотрел строго в верхний край экрана смартфона."
            )
        }
    }
}

@Composable
private fun HelpItem(
    title: String,
    desc: String
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AccentSkyBlue
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = desc,
            fontSize = 12.5.sp,
            lineHeight = 17.sp,
            color = AppColors.textSecondary
        )
    }
}

@Preview
@Composable
private fun InterfaceInfoDialogPreview() {
    InterfaceInfoDialog(onDismiss = {})
}
