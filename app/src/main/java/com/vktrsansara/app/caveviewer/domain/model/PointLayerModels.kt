package com.vktrsansara.app.caveviewer.domain.model

// 1. Категории форм маркера
enum class PointShapeCategory(val title: String) {
    SIGNS("Знаки"),
    ICONS("Иконки")
}

// 2. Доступные формы маркеров
enum class PointShape(val title: String, val category: PointShapeCategory = PointShapeCategory.SIGNS) {
    // Вкладка 1: «Знаки» (Геометрия и символы)
    CIRCLE("Круг", PointShapeCategory.SIGNS),
    SQUARE("Квадрат", PointShapeCategory.SIGNS),
    TRIANGLE_UP("Треугольник вверх", PointShapeCategory.SIGNS),
    TRIANGLE_DOWN("Треугольник вниз", PointShapeCategory.SIGNS),
    TRIANGLE_LEFT("Треугольник влево", PointShapeCategory.SIGNS),
    TRIANGLE_RIGHT("Треугольник вправо", PointShapeCategory.SIGNS),
    DIAMOND("Ромб", PointShapeCategory.SIGNS),
    STAR("Звезда", PointShapeCategory.SIGNS),
    CROSS("Крест", PointShapeCategory.SIGNS),
    PLUS("Плюс", PointShapeCategory.SIGNS),
    PENTAGON("Пятиугольник", PointShapeCategory.SIGNS),
    HEXAGON("Шестиугольник", PointShapeCategory.SIGNS),
    OCTAGON("Восьмиугольник", PointShapeCategory.SIGNS),
    SEMICIRCLE_TOP("Полукруг верх", PointShapeCategory.SIGNS),
    SEMICIRCLE_BOTTOM("Полукруг низ", PointShapeCategory.SIGNS),
    HOLLOW_CIRCLE("Кольцо", PointShapeCategory.SIGNS),
    RING_DOT("Мишень", PointShapeCategory.SIGNS),
    TRIANGLE_DOT("Треугольник с точкой", PointShapeCategory.SIGNS),
    ARCH("Арка", PointShapeCategory.SIGNS),
    INVERTED_ARCH("Перевернутая арка", PointShapeCategory.SIGNS),
    SPIRAL("Спираль", PointShapeCategory.SIGNS),
    ASTERISK("Звездочка", PointShapeCategory.SIGNS),
    HASH("Решетка", PointShapeCategory.SIGNS),
    ARROW_UP("Стрелка вверх", PointShapeCategory.SIGNS),
    ARROW_DOWN("Стрелка вниз", PointShapeCategory.SIGNS),
    ARROW_LEFT("Стрелка влево", PointShapeCategory.SIGNS),
    ARROW_RIGHT("Стрелка вправо", PointShapeCategory.SIGNS),
    ARROW_NE("Стрелка СВ", PointShapeCategory.SIGNS),
    ARROW_NW("Стрелка СЗ", PointShapeCategory.SIGNS),
    ARROW_SE("Стрелка ЮВ", PointShapeCategory.SIGNS),
    ARROW_SW("Стрелка ЮЗ", PointShapeCategory.SIGNS),

    // Вкладка 2: «Иконки» (Спелеологические знаки Therion / UIS)
    ENTRANCE("Вход в пещеру", PointShapeCategory.ICONS),
    PITCH("Колодец", PointShapeCategory.ICONS),
    CHIMNEY("Камин / Орган", PointShapeCategory.ICONS),
    WATER_SPRING("Источник / Родник", PointShapeCategory.ICONS),
    WATER_FLOW("Водоток / Ручей", PointShapeCategory.ICONS),
    LAKE("Подземное озеро", PointShapeCategory.ICONS),
    DANGER("Опасность", PointShapeCategory.ICONS),
    COLLAPSE("Обвал", PointShapeCategory.ICONS),
    BOULDER("Глыбы / Завал", PointShapeCategory.ICONS),
    CAMP("Стоянка / Лагерь", PointShapeCategory.ICONS),
    STATION("Пикет / Репер", PointShapeCategory.ICONS),
    AIR_DRAFT("Тяга воздуха", PointShapeCategory.ICONS),
    BAT("Летучая мышь", PointShapeCategory.ICONS),
    BONES("Кости / Палео", PointShapeCategory.ICONS)
}

// 2. Типы динамических полей слоя
enum class LayerFieldType(val title: String) {
    TEXT("Текст"),
    NUMBER("Число"),
    BOOLEAN("Флаг (Да/Нет)"),
    SELECT("Список"),
    DATETIME("Дата / Время")
}

// Утилиты форматирования значений типа DATETIME
object LayerFieldDateTimeUtils {
    const val DEFAULT_NOW_DATETIME = "NOW_DATETIME"
    const val DEFAULT_NOW_DATE = "NOW_DATE"
    const val DEFAULT_NOW_TIME = "NOW_TIME"

    fun formatNow(includeDate: Boolean, includeTime: Boolean): String {
        val pattern = when {
            includeDate && includeTime -> "dd.MM.yyyy HH:mm"
            includeDate -> "dd.MM.yyyy"
            includeTime -> "HH:mm"
            else -> ""
        }
        if (pattern.isEmpty()) return ""
        return java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault()).format(System.currentTimeMillis())
    }

    fun resolveDefaultValue(token: String): String {
        return when (token) {
            DEFAULT_NOW_DATETIME, "CURRENT_DATETIME" -> formatNow(includeDate = true, includeTime = true)
            DEFAULT_NOW_DATE, "CURRENT_DATE" -> formatNow(includeDate = true, includeTime = false)
            DEFAULT_NOW_TIME, "CURRENT_TIME" -> formatNow(includeDate = false, includeTime = true)
            else -> token
        }
    }

    fun getDisplayDefaultLabel(token: String): String {
        return when (token) {
            DEFAULT_NOW_DATETIME, "CURRENT_DATETIME" -> "Текущие дата и время"
            DEFAULT_NOW_DATE, "CURRENT_DATE" -> "Текущая дата"
            DEFAULT_NOW_TIME, "CURRENT_TIME" -> "Текущее время"
            else -> if (token.isNotEmpty()) token else "не задано"
        }
    }
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
