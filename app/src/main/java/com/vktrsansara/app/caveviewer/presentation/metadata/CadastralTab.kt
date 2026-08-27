package com.vktrsansara.app.caveviewer.presentation.metadata

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.CadastralItem
import com.vktrsansara.app.caveviewer.domain.model.CadastralSection
import com.vktrsansara.app.caveviewer.ui.theme.AccentRed
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import kotlinx.coroutines.launch

private val GreenPlusColor = Color(0xFF10B981)
private val PurpleListColor = Color(0xFFA78BFA)

/**
 * Tab component for cave Cadastre passport with secondary 7-icon subtabs bar and editable records.
 */
@Composable
fun CadastralTab(
    cadastralData: Map<String, List<CadastralItem>>,
    onCadastralDataChange: (Map<String, List<CadastralItem>>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf(CadastralSection.CLASSIFICATION) }
    var isContentDialogVisible by remember { mutableStateOf(false) }

    val currentItems = cadastralData[selectedSection.key] ?: emptyList()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    if (isContentDialogVisible) {
        CadastralContentDialog(
            sectionTitle = selectedSection.title,
            items = currentItems,
            onSelectIndex = { index ->
                coroutineScope.launch {
                    listState.animateScrollToItem(index)
                }
            },
            onDismiss = { isContentDialogVisible = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bgMain)
    ) {
        // 1. Secondary 7-icon subtabs bar (Icons only, no labels)
        CadastralSubTabBar(
            selectedSection = selectedSection,
            onSectionSelected = { selectedSection = it }
        )

        // 2. Section Content Cards List
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            CadastralSectionCard(
                section = selectedSection,
                items = currentItems,
                onOpenContentDialog = { isContentDialogVisible = true },
                onAddItem = {
                    val newItem = CadastralItem(
                        section = selectedSection.key,
                        title = "",
                        content = ""
                    )
                    val newMap = cadastralData.toMutableMap()
                    newMap[selectedSection.key] = currentItems + newItem
                    onCadastralDataChange(newMap)
                    coroutineScope.launch {
                        listState.animateScrollToItem(currentItems.size)
                    }
                },
                onUpdateItem = { index, updated ->
                    val newItems = currentItems.toMutableList()
                    newItems[index] = updated
                    val newMap = cadastralData.toMutableMap()
                    newMap[selectedSection.key] = newItems
                    onCadastralDataChange(newMap)
                },
                onDeleteItem = { index ->
                    val newItems = currentItems.toMutableList()
                    newItems.removeAt(index)
                    val newMap = cadastralData.toMutableMap()
                    newMap[selectedSection.key] = newItems
                    onCadastralDataChange(newMap)
                },
                listState = listState
            )
        }
    }
}

@Composable
private fun CadastralSubTabBar(
    selectedSection: CadastralSection,
    onSectionSelected: (CadastralSection) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.bgSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CadastralSection.entries.forEach { section ->
                val isSelected = section == selectedSection
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.pressedColor),
                            onClick = { onSectionSelected(section) }
                        )
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = section.icon,
                        contentDescription = section.title,
                        tint = if (isSelected) section.color else section.color.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Bottom line indicator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(2.dp)
                            .background(if (isSelected) section.color else Color.Transparent)
                    )
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
    }
}

@Composable
private fun CadastralSectionCard(
    section: CadastralSection,
    items: List<CadastralItem>,
    onOpenContentDialog: () -> Unit,
    onAddItem: () -> Unit,
    onUpdateItem: (Int, CadastralItem) -> Unit,
    onDeleteItem: (Int) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.bgCard)
            .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        // Card Header (Row)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = section.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Table of contents button
                IconButton(
                    onClick = onOpenContentDialog,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ListAlt,
                        contentDescription = "Содержимое",
                        tint = PurpleListColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Add record button
                IconButton(
                    onClick = onAddItem,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Добавить",
                        tint = GreenPlusColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Записи отсутствуют. Нажмите «+» для создания новой записи.",
                    fontSize = 13.sp,
                    color = AppColors.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(items) { index, item ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Spacer(modifier = Modifier.height(2.dp))
                        HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
                        Spacer(modifier = Modifier.height(2.dp))

                        // Record Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val recordTitle = if (item.title.isNotBlank()) item.title else "Запись #${index + 1}"
                            Text(
                                text = recordTitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = { onDeleteItem(index) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Удалить запись",
                                    tint = AccentRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Title field
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Заголовок:",
                                fontSize = 11.sp,
                                color = AppColors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AppColors.bgSurface)
                                    .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BasicTextField(
                                    value = item.title,
                                    onValueChange = { onUpdateItem(index, item.copy(title = it)) },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AppColors.textPrimary
                                    ),
                                    cursorBrush = SolidColor(AccentSkyBlue),
                                    decorationBox = { innerTextField ->
                                        if (item.title.isEmpty()) {
                                            Text(
                                                text = "Введите заголовок",
                                                fontSize = 13.sp,
                                                color = AppColors.textSecondary
                                            )
                                        }
                                        innerTextField()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Content field
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Содержание:",
                                fontSize = 11.sp,
                                color = AppColors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(76.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AppColors.bgSurface)
                                    .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                BasicTextField(
                                    value = item.content,
                                    onValueChange = { onUpdateItem(index, item.copy(content = it)) },
                                    minLines = 3,
                                    maxLines = 4,
                                    textStyle = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = AppColors.textPrimary,
                                        lineHeight = 17.sp
                                    ),
                                    cursorBrush = SolidColor(AccentSkyBlue),
                                    decorationBox = { innerTextField ->
                                        if (item.content.isEmpty()) {
                                            Text(
                                                text = "Введите содержание",
                                                fontSize = 13.sp,
                                                color = AppColors.textSecondary,
                                                lineHeight = 17.sp
                                            )
                                        }
                                        innerTextField()
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
