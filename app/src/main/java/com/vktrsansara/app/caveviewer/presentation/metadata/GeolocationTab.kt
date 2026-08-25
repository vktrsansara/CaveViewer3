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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.EntranceCoordinate
import com.vktrsansara.app.caveviewer.domain.model.MapLocation
import com.vktrsansara.app.caveviewer.ui.theme.AccentRed
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

private val GreenPlusColor = Color(0xFF10B981)

/**
 * Tab content for cave geolocation, administrative location, and GPS entrances list.
 */
@Composable
fun GeolocationTab(
    location: MapLocation,
    onLocationChange: (MapLocation) -> Unit,
    entrances: List<EntranceCoordinate>,
    onEntrancesChange: (List<EntranceCoordinate>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Block 1: Расположение
        GeolocationCard {
            Text(
                text = "Расположение",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Страна
                LocationInputField(
                    label = "Страна:",
                    value = location.country,
                    placeholder = "Страна",
                    onValueChange = { onLocationChange(location.copy(country = it)) }
                )

                // Регион/Область
                LocationInputField(
                    label = "Регион/Область:",
                    value = location.region,
                    placeholder = "Регион/Область",
                    onValueChange = { onLocationChange(location.copy(region = it)) }
                )

                // Район
                LocationInputField(
                    label = "Район:",
                    value = location.district,
                    placeholder = "Район",
                    onValueChange = { onLocationChange(location.copy(district = it)) }
                )

                // Дополнительно
                LocationMultilineInputField(
                    label = "Дополнительно:",
                    value = location.description,
                    placeholder = "Сведения о расположении, подъездах и ориентирах",
                    onValueChange = { onLocationChange(location.copy(description = it)) }
                )
            }
        }

        // Block 2: Точки входа
        GeolocationCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Точки входа",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textPrimary
                )

                IconButton(
                    onClick = {
                        val newPoint = EntranceCoordinate(
                            pointIndex = entrances.size,
                            name = "",
                            lat = null,
                            lon = null,
                            alt = null
                        )
                        onEntrancesChange(entrances + newPoint)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Добавить точку",
                        tint = GreenPlusColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (entrances.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Точки входа не заданы. Нажмите «+» для добавления.",
                        fontSize = 13.sp,
                        color = AppColors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    entrances.forEachIndexed { index, entrance ->
                        Spacer(modifier = Modifier.height(2.dp))
                        HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
                        Spacer(modifier = Modifier.height(2.dp))

                        EntranceItemView(
                            index = index,
                            entrance = entrance,
                            onUpdate = { updated ->
                                val newList = entrances.toMutableList()
                                newList[index] = updated
                                onEntrancesChange(newList)
                            },
                            onDelete = {
                                val newList = entrances.toMutableList()
                                newList.removeAt(index)
                                // Re-index remaining points
                                val reindexed = newList.mapIndexed { idx, item -> item.copy(pointIndex = idx) }
                                onEntrancesChange(reindexed)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EntranceItemView(
    index: Int,
    entrance: EntranceCoordinate,
    onUpdate: (EntranceCoordinate) -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Point header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Геолокация #${index + 1}:",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Удалить точку",
                    tint = AccentRed,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Name field
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Название:",
                fontSize = 11.sp,
                color = AppColors.textSecondary
            )

            Spacer(modifier = Modifier.height(2.dp))

            DarkInputBox(modifier = Modifier.fillMaxWidth()) {
                BasicTextField(
                    value = entrance.name,
                    onValueChange = { onUpdate(entrance.copy(name = it)) },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.textPrimary
                    ),
                    cursorBrush = SolidColor(AccentSkyBlue),
                    decorationBox = { innerTextField ->
                        if (entrance.name.isEmpty()) {
                            Text(
                                text = "Например: Главный вход, Провал №2",
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

        // GPS Coordinates row (Lat, Lon, Alt)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Lat
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Lat:",
                    fontSize = 11.sp,
                    color = AppColors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(2.dp))
                DarkInputBox(modifier = Modifier.fillMaxWidth()) {
                    val latStr = entrance.lat?.toString() ?: ""
                    BasicTextField(
                        value = latStr,
                        onValueChange = { str ->
                            val parsed = str.toDoubleOrNull()
                            onUpdate(entrance.copy(lat = if (str.isBlank()) null else parsed))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = TextStyle(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.textPrimary,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(AccentSkyBlue),
                        decorationBox = { innerTextField ->
                            if (latStr.isEmpty()) {
                                Text(
                                    text = "Широта",
                                    fontSize = 11.5.sp,
                                    color = AppColors.textSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Lon
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Lon:",
                    fontSize = 11.sp,
                    color = AppColors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(2.dp))
                DarkInputBox(modifier = Modifier.fillMaxWidth()) {
                    val lonStr = entrance.lon?.toString() ?: ""
                    BasicTextField(
                        value = lonStr,
                        onValueChange = { str ->
                            val parsed = str.toDoubleOrNull()
                            onUpdate(entrance.copy(lon = if (str.isBlank()) null else parsed))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = TextStyle(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.textPrimary,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(AccentSkyBlue),
                        decorationBox = { innerTextField ->
                            if (lonStr.isEmpty()) {
                                Text(
                                    text = "Долгота",
                                    fontSize = 11.5.sp,
                                    color = AppColors.textSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Alt
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Alt:",
                    fontSize = 11.sp,
                    color = AppColors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(2.dp))
                DarkInputBox(modifier = Modifier.fillMaxWidth()) {
                    val altStr = entrance.alt?.toString() ?: ""
                    BasicTextField(
                        value = altStr,
                        onValueChange = { str ->
                            val parsed = str.toDoubleOrNull()
                            onUpdate(entrance.copy(alt = if (str.isBlank()) null else parsed))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = TextStyle(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.textPrimary,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(AccentSkyBlue),
                        decorationBox = { innerTextField ->
                            if (altStr.isEmpty()) {
                                Text(
                                    text = "Высота, м",
                                    fontSize = 11.5.sp,
                                    color = AppColors.textSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationInputField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = AppColors.textSecondary
        )
        Spacer(modifier = Modifier.height(3.dp))
        DarkInputBox(modifier = Modifier.fillMaxWidth()) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.textPrimary
                ),
                cursorBrush = SolidColor(AccentSkyBlue),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
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
}

@Composable
private fun LocationMultilineInputField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
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
                value = value,
                onValueChange = onValueChange,
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
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
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

@Composable
private fun DarkInputBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(AppColors.bgSurface)
            .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

@Composable
private fun GeolocationCard(
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.bgCard)
            .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        content()
    }
}
