package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.vktrsansara.app.caveviewer.domain.model.CaveRoute
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

@Composable
fun RouteInfoCard(
    startPoint: Pair<Double, Double>?,
    endPoint: Pair<Double, Double>?,
    primaryRoute: CaveRoute?,
    alternativeRoute: CaveRoute?,
    isCalculating: Boolean,
    errorMessage: String?,
    onResetClick: () -> Unit,
    onCloseClick: () -> Unit,
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
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Левая информационная часть
            Column(
                modifier = Modifier.weight(1f),
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
                                fontSize = 12.sp,
                                color = Color(0xFFEF4444),
                                lineHeight = 16.sp
                            )
                        }
                    }
                    primaryRoute != null -> {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = AppColors.textSecondary, fontSize = 12.5.sp, fontWeight = FontWeight.Normal)) {
                                    append(if (alternativeRoute != null) "Основной: " else "Маршрут: ")
                                }
                                withStyle(SpanStyle(color = Color(0xFF06B6D4), fontSize = 13.sp, fontWeight = FontWeight.Bold)) {
                                    append("${primaryRoute.lengthMeters} м")
                                }
                                withStyle(SpanStyle(color = AppColors.textSecondary, fontSize = 12.5.sp, fontWeight = FontWeight.Normal)) {
                                    append(" • Сложность: ")
                                }
                                withStyle(SpanStyle(color = AppColors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)) {
                                    append("${primaryRoute.averageDifficulty}")
                                }
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (alternativeRoute != null) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(color = AppColors.textSecondary, fontSize = 11.5.sp, fontWeight = FontWeight.Normal)) {
                                        append("Альтернативный: ")
                                    }
                                    withStyle(SpanStyle(color = Color(0xFFF59E0B), fontSize = 12.sp, fontWeight = FontWeight.Bold)) {
                                        append("${alternativeRoute.lengthMeters} м")
                                    }
                                    withStyle(SpanStyle(color = AppColors.textSecondary, fontSize = 11.5.sp, fontWeight = FontWeight.Normal)) {
                                        append(" • Сложность: ")
                                    }
                                    withStyle(SpanStyle(color = AppColors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)) {
                                        append("${alternativeRoute.averageDifficulty}")
                                    }
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    startPoint != null && endPoint == null -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🟢",
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Точка А выбрана • Выберите точку Б (Финиш)",
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
                                text = "Нажмите на карту для выбора точки А (Старт)",
                                fontSize = 12.5.sp,
                                color = AppColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Правые кнопки управления: [↩️ Сбросить] и [✕ Выход]
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка [↩️ Сбросить]
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AppColors.bgSurface)
                        .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.pressedColor),
                            onClick = onResetClick
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RestartAlt,
                        contentDescription = "Сбросить маршрут",
                        tint = AccentSkyBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Кнопка [✕ Выход]
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AppColors.bgSurface)
                        .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.pressedColor),
                            onClick = onCloseClick
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Выход из навигатора",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
