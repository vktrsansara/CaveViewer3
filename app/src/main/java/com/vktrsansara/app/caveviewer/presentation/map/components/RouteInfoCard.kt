package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.measure.LineColorUtils
import com.vktrsansara.app.caveviewer.domain.model.CaveRoute
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

/**
 * Плавающая информационная плашка статуса построения маршрута и его параметров.
 */
@Composable
fun RouteInfoCard(
    waypointsCount: Int,
    primaryRoute: CaveRoute?,
    alternativeRoute: CaveRoute?,
    isAlternativeActive: Boolean = false,
    isCalculating: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.bgCard)
            .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            when {
                isCalculating -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFF06B6D4),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Поиск оптимального маршрута...",
                            fontSize = 12.5.sp,
                            color = AppColors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                errorMessage != null -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = "Ошибка",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = errorMessage,
                            fontSize = 12.5.sp,
                            color = Color(0xFFEF4444),
                            lineHeight = 16.sp
                        )
                    }
                }
                primaryRoute != null -> {
                    val firstRoute = if (isAlternativeActive) alternativeRoute ?: primaryRoute else primaryRoute
                    val secondRoute = if (isAlternativeActive) primaryRoute else alternativeRoute
                    val firstLabel = if (isAlternativeActive) "Альтернативный: " else (if (alternativeRoute != null) "Основной: " else "Маршрут: ")
                    val secondLabel = if (isAlternativeActive) "Основной: " else "Альтернативный: "
                    val firstColor = LineColorUtils.getDifficultyColor(firstRoute.averageDifficulty)
                    val secondColor = secondRoute?.let { LineColorUtils.getDifficultyColor(it.averageDifficulty) } ?: Color(0xFFF59E0B)

                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = AppColors.textSecondary, fontSize = 12.5.sp, fontWeight = FontWeight.Normal)) {
                                append(firstLabel)
                            }
                            withStyle(SpanStyle(color = firstColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)) {
                                append("${firstRoute.lengthMeters} м")
                            }
                            withStyle(SpanStyle(color = AppColors.textSecondary, fontSize = 12.5.sp, fontWeight = FontWeight.Normal)) {
                                append(" • Сложность: ")
                            }
                            withStyle(SpanStyle(color = AppColors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)) {
                                append("${firstRoute.averageDifficulty}")
                            }
                            if (waypointsCount > 2) {
                                withStyle(SpanStyle(color = AppColors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Normal)) {
                                    append(" • Точек: $waypointsCount")
                                }
                            }
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (secondRoute != null) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = AppColors.textSecondary, fontSize = 11.5.sp, fontWeight = FontWeight.Normal)) {
                                    append(secondLabel)
                                }
                                withStyle(SpanStyle(color = secondColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)) {
                                    append("${secondRoute.lengthMeters} м")
                                }
                                withStyle(SpanStyle(color = AppColors.textSecondary, fontSize = 11.5.sp, fontWeight = FontWeight.Normal)) {
                                    append(" • Сложность: ")
                                }
                                withStyle(SpanStyle(color = AppColors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)) {
                                    append("${secondRoute.averageDifficulty} (пунктир)")
                                }
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                waypointsCount == 1 -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🟢",
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Точка 1 выбрана • Выберите точку 2",
                            fontSize = 12.5.sp,
                            color = AppColors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                waypointsCount >= 2 -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🟢",
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Точек: $waypointsCount • Выберите следующую точку",
                            fontSize = 12.5.sp,
                            color = AppColors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                else -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Explore,
                            contentDescription = "Навигация",
                            tint = Color(0xFF06B6D4),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Нажмите на карту для выбора точки 1 (Старт)",
                            fontSize = 12.5.sp,
                            color = AppColors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
