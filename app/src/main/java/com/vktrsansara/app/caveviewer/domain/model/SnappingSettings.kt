package com.vktrsansara.app.caveviewer.domain.model

enum class IntersectionMode(val title: String) {
    NO("Нет"),            // Не создавать перекресток
    YES("Да"),           // Автоматически ставить вершину на пересечении
    ASK("Спрашивать")    // Задавать вопрос пользователю при пересечении
}

data class SnappingSettings(
    val isEnabled: Boolean = true,
    val snapToVertices: Boolean = true,      // К вершинам линий (по умолчанию ВКЛ)
    val snapToEdges: Boolean = false,         // К ребрам линий
    val snapToPoints: Boolean = true,        // К точкам слоев
    val snapPointsToLines: Boolean = true,   // Привязывать точки к линиям (по умолчанию ВКЛ)
    val snapRadiusDp: Float = 12.0f,         // Радиус захвата курсора в dp (по умолчанию 12 dp, диапазон 2..20)
    val intersectionMode: IntersectionMode = IntersectionMode.NO // По умолчанию "Нет"
)
