package com.vktrsansara.app.caveviewer.domain.model

// 1. 7 доступных форм маркера
enum class PointShape(val title: String) {
    CIRCLE("Круг"),
    SQUARE("Квадрат"),
    TRIANGLE_UP("Треугольник вверх"),
    TRIANGLE_DOWN("Треугольник вниз"),
    DIAMOND("Ромб"),
    STAR("Звезда"),
    CROSS("Крест")
}

// 2. Типы динамических полей слоя
enum class LayerFieldType(val title: String) {
    TEXT("Текст"),
    NUMBER("Число"),
    BOOLEAN("Флаг (Да/Нет)"),
    SELECT("Список")
}

// 3. Описание одного кастомного поля схемы слоя
data class LayerFieldDefinition(
    val key: String,                  // "hazard", "depth", "district"
    val name: String,                 // "Опасно", "Глубина (м)", "Район"
    val type: LayerFieldType,
    val defaultValue: String = "",    // Значение по умолчанию для новых точек
    val options: List<String> = emptyList() // Для типа SELECT: ["Шлямбур", "Спит", "Трос"]
)

// 4. Слой точек
data class PointLayer(
    val id: Long = 0,
    val name: String,
    val isVisible: Boolean = true,
    val defaultShape: PointShape = PointShape.CIRCLE,
    val defaultColor: Long = 0xFF38BDF8, // Цвет по умолчанию (Sky Blue)
    val defaultSize: Float = 6f,
    val showLabels: Boolean = true,
    val fieldsSchema: List<LayerFieldDefinition> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

// 5. Отдельная точка слоя
data class LayerPoint(
    val id: Long = 0,
    val layerId: Long,
    val name: String,
    val x: Double,                     // Координата X на растре (px)
    val y: Double,                     // Координата Y на растре (px)
    val shape: PointShape = PointShape.CIRCLE, // Индивидуальная форма точки
    val color: Long = 0xFF38BDF8,      // Индивидуальный цвет точки
    val typeCategory: String? = null,  // Быстрая категория ("picket", "camp", "hazard", "water")
    val customValues: Map<String, String> = emptyMap(), // Значения кастомных полей {"hazard": "true", "depth": "14.5"}
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
