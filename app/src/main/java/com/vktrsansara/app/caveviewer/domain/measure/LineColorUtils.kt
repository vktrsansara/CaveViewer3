package com.vktrsansara.app.caveviewer.domain.measure

import androidx.compose.ui.graphics.Color
import com.vktrsansara.app.caveviewer.domain.model.LineEnvironmentType

object LineColorUtils {
    /**
     * Расчет цвета ядра линии по шкале сложности хода (0.0 .. 8.0)
     */
    fun getDifficultyColor(difficulty: Float): Color {
        val diff = difficulty.coerceIn(0.0f, 8.0f)
        return when {
            diff <= 1.0f -> Color(0xFF10B981) // 0.0..1.0 - Зеленый (Просторный ход)
            diff <= 2.0f -> Color(0xFF84CC16) // 1.1..2.0 - Лайм (Удобный ход)
            diff <= 3.5f -> Color(0xFFEAB308) // 2.1..3.5 - Желтый (Низкий ход)
            diff <= 5.0f -> Color(0xFFF97316) // 3.6..5.0 - Оранжевый (Шкуродер)
            diff <= 6.5f -> Color(0xFFEA580C) // 5.1..6.5 - Оранжево-красный (Узкий калибр)
            else -> Color(0xFF8B0000)         // 6.6..8.0 - Бордово-красный (Экстрим/Сифон)
        }
    }

    /**
     * Расчет цвета внешнего ореола среды
     */
    fun getHaloColor(environmentType: LineEnvironmentType, customHaloColor: Long?): Color? {
        return when (environmentType) {
            LineEnvironmentType.NONE -> null
            LineEnvironmentType.CUSTOM -> customHaloColor?.let { Color(it.toInt()) }
            else -> environmentType.defaultHaloColor?.let { Color(it.toInt()) }
        }
    }
}
