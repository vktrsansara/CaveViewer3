package com.vktrsansara.app.caveviewer.domain.model

// 1. Предустановленные типы среды для векторных топографических засечек (UIS/Therion)
enum class LineEnvironmentType(val title: String, val symbol: String, val defaultHaloColor: Long?) {
    NONE("Обычный ход", "───", null),
    STANDING_WATER("Стоячая вода / Озеро", "──⬭──", 0xFF38BDF8), // Голубой (как у водотока)
    WATER("Водоток / Ручей", "──>──", 0xFF38BDF8),             // Голубой
    BOULDER("Завал / Глыбы", "──┼──", 0xFF6E7681),             // Графитовый
    CLAY("Глина / Вязкая грязь", "──•──", 0xFF9B381F),         // Терракотовый
    SUMP("Сифон / Полусифон", "──//──", 0xFF1E40AF),            // Темно-синий
    ICE("Лед / Наледь", "──◇──", 0xFF06B6D4),                 // Морозный циан
    GAS("Загазованность (CO₂)", "──///──", 0xFFA855F7),          // Пурпурный
    AIR_DRAFT("Тяга воздуха (Ветер)", "──>>──", 0xFF38BDF8),    // Бирюзовый
    SAND("Песок / Гравий / Осыпь", "──⁖──", 0xFFF59E0B),       // Песочно-золотистый
    ROPE("Перила / Навеска", "──○──", 0xFFE11D48),             // Веревочный красный
    SQUEEZE("Узость / Калибр", "──><──", 0xFFEA580C),           // Оранжевый
    DROP("Уступ / Сброс", "──┰──", 0xFF64748B),                // Сланцево-серый
    FLOWSTONE("Натеки / Кальцит", "──⌒──", 0xFFFCD34D),        // Кальцитовый желтый
    CUSTOM("Свой цвет засечек", "──•──", 0xFFEAB308)
}

// 2. Стиль линии
enum class LineStyle(val title: String) {
    SOLID("Сплошная"),
    DASHED("Пунктир"),
    DOTTED("Точки")
}

// 3. Слой линий
data class LineLayer(
    val id: Long = 0,
    val name: String,
    val isVisible: Boolean = true,
    val defaultWidth: Float = 3.0f,
    val defaultHaloWidth: Float = 4.0f,   // Дополнительная толщина ореола (dp)
    val isHeatmapEnabled: Boolean = true, // Тепловая шкала сложности 0.0..8.0
    val defaultColor: Long = 0xFF10B981,  // Цвет, если шкала выключена
    val defaultEnvironment: LineEnvironmentType = LineEnvironmentType.NONE,
    val fieldsSchema: List<LayerFieldDefinition> = emptyList(), // Кастомные поля (высота, ширина и т.д.)
    val createdAt: Long = System.currentTimeMillis()
)

// 4. Полилиния слоя
data class LayerLine(
    val id: Long = 0,
    val layerId: Long,
    val name: String,
    val points: List<Pair<Double, Double>>, // Координаты вершин на растре (x, y в px)
    val lengthMeters: Double = 0.0,         // Длина в метрах (точность 2 знака)
    val lengthPx: Double = 0.0,             // Длина в пикселях
    val difficulty: Float = 1.0f,           // Сложность хода: 0.0 .. 8.0
    val style: LineStyle = LineStyle.SOLID,
    val environmentType: LineEnvironmentType = LineEnvironmentType.NONE, // Ореол среды
    val haloColor: Long? = null,            // Цвет ореола (если включен)
    val colorOverride: Long? = null,        // Принудительный цвет линии
    val customValues: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
