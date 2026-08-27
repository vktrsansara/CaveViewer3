package com.vktrsansara.app.caveviewer.presentation.settings

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

val CursorPaletteColors = listOf(
    0xFFEF4444L, // Красный
    0xFF10B981L, // Зеленый
    0xFF38BDF8L, // Голубой
    0xFFF59E0BL, // Желтый
    0xFFFFFFFFL, // Белый
    0xFFA855F7L, // Пурпурный
    0xFFFB923CL, // Оранжевый
    0xFF84CC16L  // Салатовый
)

@Composable
fun CursorColorPickerDialog(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.bgCard)
                .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Цвет курсора",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
            Spacer(modifier = Modifier.height(14.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(CursorPaletteColors) { colorLong ->
                    val color = Color(colorLong.toInt())
                    val isSelected = colorLong == selectedColor

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) {
                                    if (colorLong == 0xFFFFFFFFL) Color.Black else Color.White
                                } else {
                                    Color(0x40000000)
                                },
                                shape = CircleShape
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = Color.White.copy(alpha = 0.4f)),
                                onClick = {
                                    onColorSelected(colorLong)
                                    onDismiss()
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Выбрано",
                                tint = if (colorLong == 0xFFFFFFFFL) Color.Black else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                DialogCancelButton(
                    text = "Отмена",
                    onClick = onDismiss
                )
            }
        }
    }
}
