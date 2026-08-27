package com.vktrsansara.app.caveviewer.presentation.map.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.PointShape
import com.vktrsansara.app.caveviewer.domain.model.PointShapeCategory
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.presentation.components.DialogSaveButton
import com.vktrsansara.app.caveviewer.presentation.map.components.PointShapeMarker
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

@Composable
fun PointShapePickerDialog(
    selectedShape: PointShape,
    markerColor: Long,
    onShapeSelected: (PointShape) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelectedShape by remember { mutableStateOf(selectedShape) }
    var selectedTab by remember {
        mutableIntStateOf(if (selectedShape.category == PointShapeCategory.ICONS) 1 else 0)
    }

    val signsList = remember { PointShape.entries.filter { it.category == PointShapeCategory.SIGNS } }
    val iconsList = remember { PointShape.entries.filter { it.category == PointShapeCategory.ICONS } }

    val currentList = if (selectedTab == 0) signsList else iconsList
    val colorToDisplay = remember(markerColor) { Color(markerColor.toInt()) }
    var isHelpOpen by remember { mutableStateOf(false) }

    if (isHelpOpen) {
        PointShapePickerHelpDialog(onDismiss = { isHelpOpen = false })
    }

    AppDialogContainer(
        title = "Форма маркера",
        onDismissRequest = onDismiss,
        onInfoClick = { isHelpOpen = true },
        isScrollable = false,
        buttons = {
            DialogCancelButton(
                text = "Отмена",
                onClick = onDismiss
            )
            Spacer(modifier = Modifier.width(8.dp))
            DialogSaveButton(
                text = "Выбрать",
                onClick = {
                    onShapeSelected(tempSelectedShape)
                    onDismiss()
                }
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Tab Switcher (Знаки vs Иконки)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(AppColors.bgSurface)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab 0: Знаки
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (selectedTab == 0) AccentSkyBlue.copy(alpha = 0.2f) else Color.Transparent)
                        .border(
                            width = if (selectedTab == 0) 1.dp else 0.dp,
                            color = if (selectedTab == 0) AccentSkyBlue else Color.Transparent,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.pressedColor),
                            onClick = { selectedTab = 0 }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Знаки (${signsList.size})",
                        fontSize = 12.5.sp,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) AccentSkyBlue else AppColors.textSecondary
                    )
                }

                // Tab 1: Иконки
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (selectedTab == 1) AccentSkyBlue.copy(alpha = 0.2f) else Color.Transparent)
                        .border(
                            width = if (selectedTab == 1) 1.dp else 0.dp,
                            color = if (selectedTab == 1) AccentSkyBlue else Color.Transparent,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.pressedColor),
                            onClick = { selectedTab = 1 }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Иконки UIS (${iconsList.size})",
                        fontSize = 12.5.sp,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) AccentSkyBlue else AppColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Compact Grid of Shapes (max height 210 dp to keep dialog <= 1/3 screen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 210.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AppColors.bgSurface)
                    .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 44.dp),
                    contentPadding = PaddingValues(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(currentList, key = { it.name }) { shape ->
                        val isSelected = tempSelectedShape == shape
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) AccentSkyBlue.copy(alpha = 0.2f) else AppColors.bgCard)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) AccentSkyBlue else AppColors.borderColor,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = AppColors.pressedColor),
                                    onClick = { tempSelectedShape = shape }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            PointShapeMarker(
                                shape = shape,
                                color = colorToDisplay,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Selected shape title footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Выбрано: ",
                    fontSize = 12.sp,
                    color = AppColors.textSecondary
                )
                Text(
                    text = tempSelectedShape.title,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentSkyBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Help dialog explaining the marker shape catalog and categories.
 */
@Composable
fun PointShapePickerHelpDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Форма маркера",
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
                title = "Библиотека форм маркеров:",
                description = "Содержит более 40 стандартизированных знаков для наглядного картирования объектов пещеры."
            )

            HelpItem(
                title = "Вкладка «Знаки»:",
                description = "Базовые геометрические фигуры (круги, квадраты, треугольники, ромбы, звёзды, кольца, мишени) и стрелки направлений (8 румбов для ориентации ходов и трещин)."
            )

            HelpItem(
                title = "Вкладка «Иконки» (UIS / Therion):",
                description = "Международные спелеологические знаки: вход, колодец, орган/камин, источник, водоток, озеро, опасность, обвал, завал глыб, лагерь, съемочный пикет, тяга воздуха, летучая мышь, кости."
            )

            HelpItem(
                title = "Применение формы:",
                description = "Нажмите на нужную форму и подтвердите выбор кнопкой «Выбрать». Выбранная форма применится к слою или отдельной редактируемой точке."
            )
        }
    }
}

@Composable
private fun HelpItem(title: String, description: String) {
    Column {
        Text(
            text = title,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = AccentSkyBlue
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            fontSize = 12.sp,
            color = AppColors.textSecondary,
            lineHeight = 17.sp
        )
    }
}