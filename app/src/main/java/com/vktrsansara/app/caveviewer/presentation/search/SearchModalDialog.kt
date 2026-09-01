package com.vktrsansara.app.caveviewer.presentation.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.LayersClear
import androidx.compose.material.icons.rounded.Polyline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.measure.LineColorUtils
import com.vktrsansara.app.caveviewer.domain.measure.MeasureUtils
import com.vktrsansara.app.caveviewer.domain.model.LayerLine
import com.vktrsansara.app.caveviewer.domain.model.LayerPoint
import com.vktrsansara.app.caveviewer.domain.model.LayerSearchConfig
import com.vktrsansara.app.caveviewer.domain.model.LineLayer
import com.vktrsansara.app.caveviewer.domain.model.PointLayer
import com.vktrsansara.app.caveviewer.presentation.main.SearchResultItem
import com.vktrsansara.app.caveviewer.presentation.map.components.PointShapeMarker
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import java.util.Locale

/**
 * Top-anchored modal search dialog with live filtering by point/line attributes and subtitles.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchModalDialog(
    allPoints: List<LayerPoint>,
    pointLayers: List<PointLayer>,
    pointsConfig: LayerSearchConfig,
    allLines: List<LayerLine>,
    lineLayers: List<LineLayer>,
    linesConfig: LayerSearchConfig,
    pixelsPerMeter: Double,
    searchHistory: List<String>,
    onSelectResult: (SearchResultItem) -> Unit,
    onClearHistory: () -> Unit,
    onClearMarkers: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Live search results filtering
    val results = remember(
        searchQuery,
        allPoints,
        pointLayers,
        pointsConfig,
        allLines,
        lineLayers,
        linesConfig,
        pixelsPerMeter
    ) {
        val q = searchQuery.trim()
        if (q.isBlank()) return@remember emptyList<SearchResultItem>()

        val list = mutableListOf<SearchResultItem>()

        // 1. Поиск по точкам
        if (pointsConfig.isSearchEnabled) {
            val isNameSearch = pointsConfig.searchFields.any { it.key == "name" && it.isEnabled }
            val activeCustomFields = pointsConfig.searchFields.filter { it.key != "name" && it.isEnabled }
            val pointLayerMap = pointLayers.associateBy { it.id }

            for (point in allPoints) {
                val layer = pointLayerMap[point.layerId] ?: continue
                var matchedAttr: String? = null
                var matched = false

                if (isNameSearch && point.name.contains(q, ignoreCase = true)) {
                    matched = true
                }

                if (!matched && activeCustomFields.isNotEmpty()) {
                    for (item in activeCustomFields) {
                        val value = if (item.key == "typeCategory") {
                            point.typeCategory
                        } else {
                            point.customValues[item.key]
                        }
                        if (!value.isNullOrBlank() && value.contains(q, ignoreCase = true)) {
                            matched = true
                            matchedAttr = "${item.title}: $value"
                            break
                        }
                    }
                }

                if (matched) {
                    val subs = mutableListOf<String>()
                    subs.add(layer.name)
                    for (subKey in pointsConfig.subtitleFields) {
                        val fieldDef = layer.fieldsSchema.find { it.key == subKey }
                        val title = fieldDef?.name ?: when (subKey) {
                            "typeCategory" -> "Категория"
                            else -> subKey
                        }
                        val value = if (subKey == "typeCategory") {
                            point.typeCategory
                        } else {
                            point.customValues[subKey]
                        }
                        if (!value.isNullOrBlank()) {
                            subs.add("$title: $value")
                        }
                    }
                    list.add(
                        SearchResultItem.PointResult(
                            point = point,
                            layer = layer,
                            matchedFieldTitle = matchedAttr,
                            subtitles = subs
                        )
                    )
                }
            }
        }

        // 2. Поиск по линиям
        if (linesConfig.isSearchEnabled) {
            val isNameSearch = linesConfig.searchFields.any { it.key == "name" && it.isEnabled }
            val activeCustomFields = linesConfig.searchFields.filter { it.key != "name" && it.isEnabled }
            val lineLayerMap = lineLayers.associateBy { it.id }

            for (line in allLines) {
                val layer = lineLayerMap[line.layerId] ?: continue
                var matchedAttr: String? = null
                var matched = false

                if (isNameSearch && line.name.contains(q, ignoreCase = true)) {
                    matched = true
                }

                if (!matched && activeCustomFields.isNotEmpty()) {
                    for (item in activeCustomFields) {
                        val value = when (item.key) {
                            "difficulty" -> if (line.difficulty > 0.0) String.format(Locale.US, "%.1f", line.difficulty) else null
                            "environmentType" -> line.environmentType.title
                            else -> line.customValues[item.key]
                        }
                        if (!value.isNullOrBlank() && value.contains(q, ignoreCase = true)) {
                            matched = true
                            matchedAttr = "${item.title}: $value"
                            break
                        }
                    }
                }

                if (matched) {
                    val subs = mutableListOf<String>()
                    subs.add(layer.name)
                    for (subKey in linesConfig.subtitleFields) {
                        val fieldDef = layer.fieldsSchema.find { it.key == subKey }
                        val title = fieldDef?.name ?: when (subKey) {
                            "difficulty" -> "Сложность"
                            "environmentType" -> "Среда"
                            else -> subKey
                        }
                        val value = when (subKey) {
                            "difficulty" -> if (line.difficulty > 0.0) String.format(Locale.US, "%.1f", line.difficulty) else null
                            "environmentType" -> line.environmentType.title
                            else -> line.customValues[subKey]
                        }
                        if (!value.isNullOrBlank()) {
                            subs.add("$title: $value")
                        }
                    }
                    if (line.lengthMeters > 0.0) {
                        subs.add(MeasureUtils.formatDistance(line.lengthMeters, pixelsPerMeter))
                    }
                    list.add(
                        SearchResultItem.LineResult(
                            line = line,
                            layer = layer,
                            matchedFieldTitle = matchedAttr,
                            subtitles = subs
                        )
                    )
                }
            }
        }

        list.take(60)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    ) {
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.bgSurface)
                    .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Consume click inside dialog
                    )
            ) {
                // А. Поисковая строка
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppColors.bgCard)
                            .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = AccentSkyBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppColors.bgCard)
                            .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = AccentSkyBlue,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = AppColors.textPrimary
                            ),
                            cursorBrush = SolidColor(AccentSkyBlue),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {}),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Поиск по карте...",
                                        fontSize = 13.5.sp,
                                        color = AppColors.textSecondary
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                        )

                        if (searchQuery.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { searchQuery = "" },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Очистить",
                                    tint = AppColors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)

                // Б. Результаты поиска или подсказки
                val queryNotEmpty = searchQuery.trim().isNotEmpty()

                if (queryNotEmpty) {
                    if (results.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ничего не найдено по запросу «$searchQuery»",
                                fontSize = 13.sp,
                                color = AppColors.textSecondary
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 380.dp)
                        ) {
                            items(results, key = { it.id }) { item ->
                                SearchResultRow(
                                    item = item,
                                    searchQuery = searchQuery.trim(),
                                    onClick = { onSelectResult(item) }
                                )
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = AppColors.borderColor.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                } else {
                    // Пустой запрос: отображение истории
                    if (searchHistory.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Недавние запросы:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.textSecondary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                searchHistory.take(8).forEach { historyQuery ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AppColors.bgCard)
                                            .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                                            .clickable { searchQuery = historyQuery }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = historyQuery,
                                            fontSize = 12.5.sp,
                                            color = AppColors.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Введите название или атрибут для поиска",
                                fontSize = 13.sp,
                                color = AppColors.textSecondary
                            )
                        }
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)

                // Г. Нижняя строка действий
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onClearHistory)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteSweep,
                            contentDescription = null,
                            tint = AppColors.textSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Очистить историю",
                            fontSize = 12.sp,
                            color = AppColors.textSecondary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onClearMarkers)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LayersClear,
                            contentDescription = null,
                            tint = AccentSkyBlue,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Очистить маркеры",
                            fontSize = 12.sp,
                            color = AccentSkyBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    item: SearchResultItem,
    searchQuery: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = AppColors.pressedColor),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Иконка слева
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(AppColors.bgCard)
                .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            when (item) {
                is SearchResultItem.PointResult -> {
                    PointShapeMarker(
                        shape = item.point.shape,
                        color = Color(item.point.color),
                        modifier = Modifier.size(18.dp)
                    )
                }
                is SearchResultItem.LineResult -> {
                    val lineColor = LineColorUtils.getDifficultyColor(item.line.difficulty)
                    Icon(
                        imageVector = Icons.Rounded.Polyline,
                        contentDescription = null,
                        tint = lineColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Основная информация
        Column(modifier = Modifier.weight(1f)) {
            val primaryColor = AppColors.textPrimary
            val secondaryColor = AppColors.textSecondary

            val highlightedName = remember(item.name, searchQuery, primaryColor) {
                highlightMatches(
                    text = item.name,
                    query = searchQuery,
                    normalColor = primaryColor,
                    highlightColor = AccentSkyBlue
                )
            }

            Text(
                text = highlightedName,
                fontSize = 13.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Подпись (Subtitle)
            val subtitleText = item.subtitles.joinToString(" • ")
            if (subtitleText.isNotEmpty()) {
                Text(
                    text = subtitleText,
                    fontSize = 11.5.sp,
                    color = secondaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Дополнительная строка: совпадение по кастомному полю
            if (item.matchedFieldTitle != null) {
                val highlightedMatchedAttr = remember(item.matchedFieldTitle, searchQuery, secondaryColor) {
                    highlightMatches(
                        text = item.matchedFieldTitle!!,
                        query = searchQuery,
                        normalColor = secondaryColor,
                        highlightColor = AccentSkyBlue
                    )
                }
                Text(
                    text = highlightedMatchedAttr,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = AppColors.textSecondary.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Builds an AnnotatedString with query matches highlighted in bold and accent color.
 */
private fun highlightMatches(
    text: String,
    query: String,
    normalColor: Color,
    highlightColor: Color
): AnnotatedString {
    if (query.isBlank()) {
        return AnnotatedString(text, spanStyle = SpanStyle(color = normalColor))
    }

    val builder = AnnotatedString.Builder()
    var currentIndex = 0
    val lowerText = text.lowercase(Locale.ROOT)
    val lowerQuery = query.lowercase(Locale.ROOT)

    while (currentIndex < text.length) {
        val matchIndex = lowerText.indexOf(lowerQuery, currentIndex)
        if (matchIndex < 0) {
            builder.pushStyle(SpanStyle(color = normalColor))
            builder.append(text.substring(currentIndex))
            builder.pop()
            break
        }

        if (matchIndex > currentIndex) {
            builder.pushStyle(SpanStyle(color = normalColor))
            builder.append(text.substring(currentIndex, matchIndex))
            builder.pop()
        }

        val matchEnd = matchIndex + query.length
        builder.pushStyle(SpanStyle(color = highlightColor, fontWeight = FontWeight.Bold))
        builder.append(text.substring(matchIndex, matchEnd))
        builder.pop()

        currentIndex = matchEnd
    }

    return builder.toAnnotatedString()
}
