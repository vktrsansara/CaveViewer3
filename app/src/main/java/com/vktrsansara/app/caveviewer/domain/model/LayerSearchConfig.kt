package com.vktrsansara.app.caveviewer.domain.model

/**
 * Configuration for searching through point or line layers.
 */
data class LayerSearchConfig(
    val isSearchEnabled: Boolean = false,                 // Включен ли поиск по данному типу слоев (по умолчанию false)
    val searchFields: List<SearchFieldItem> = listOf(SearchFieldItem(key = "name", title = "Название", isEnabled = true)),
    val subtitleFields: List<String> = emptyList()        // Список ключей полей, отображаемых в подписи результата
)

data class SearchFieldItem(
    val key: String,            // "name" или ключ кастомного поля (например, "dist", "aliases")
    val title: String,          // Отображаемое название
    val isEnabled: Boolean = true
)
