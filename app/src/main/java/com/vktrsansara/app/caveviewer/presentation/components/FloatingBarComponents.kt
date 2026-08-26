package com.vktrsansara.app.caveviewer.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

/**
 * Специальный layout для размещения плавающей боковой панели справа,
 * привязывающий кнопку "Закрыть" строго к отметке 1/3 от низа экрана (66.7% от верха).
 *
 * @param closeButtonTopInBar расстояние в dp от верхнего края панели до верхнего края кнопки "Закрыть".
 */
@Composable
fun FloatingDockAnchorLayout(
    closeButtonTopInBar: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeable = measurables.firstOrNull()?.measure(
            constraints.copy(minWidth = 0, minHeight = 0)
        ) ?: return@Layout layout(0, 0) {}

        // Отметка 1/3 от низа экрана (66.7% от верха)
        val anchorY = (constraints.maxHeight * 0.667f).toInt()
        val closeOffsetPx = closeButtonTopInBar.roundToPx()

        val x = constraints.maxWidth - 15.dp.roundToPx() - placeable.width
        val y = anchorY - closeOffsetPx

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.place(x, y)
        }
    }
}

/**
 * Единый контейнер для всех плавающих панелей управления (CaveViewer2 Style).
 */
@Composable
fun FloatingBarContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.bgCard.copy(alpha = 0.90f))
            .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        content()
    }
}

/**
 * Стандартизированная кнопка 34x34 dp для плавающих панелей (ImageVector).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FloatingBarButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    isActive: Boolean = false,
    isDanger: Boolean = false,
    activeColor: Color = AppColors.accent,
    iconTint: Color? = null
) {
    val borderColor = when {
        isDanger -> Color(0xFFEF4444)
        isActive -> activeColor
        else -> AppColors.borderColor
    }
    val backgroundColor = when {
        isActive -> activeColor.copy(alpha = 0.20f)
        else -> AppColors.bgSurface
    }
    val tint = when {
        iconTint != null -> iconTint
        isDanger -> Color(0xFFEF4444)
        isActive -> activeColor
        else -> AppColors.textPrimary
    }
    val borderWidth = if (isActive) 2.dp else 1.dp
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(6.dp))
            .then(
                if (onLongClick != null) {
                    Modifier.combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = if (isDanger) Color(0x33EF4444) else AppColors.pressedColor),
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                } else {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = if (isDanger) Color(0x33EF4444) else AppColors.pressedColor),
                        onClick = onClick
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Стандартизированная кнопка 34x34 dp для плавающих панелей (Painter).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FloatingBarButton(
    painter: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    isActive: Boolean = false,
    isDanger: Boolean = false,
    activeColor: Color = AppColors.accent,
    iconTint: Color? = null
) {
    val borderColor = when {
        isDanger -> Color(0xFFEF4444)
        isActive -> activeColor
        else -> AppColors.borderColor
    }
    val backgroundColor = when {
        isActive -> activeColor.copy(alpha = 0.20f)
        else -> AppColors.bgSurface
    }
    val tint = when {
        iconTint != null -> iconTint
        isDanger -> Color(0xFFEF4444)
        isActive -> activeColor
        else -> AppColors.textPrimary
    }
    val borderWidth = if (isActive) 2.dp else 1.dp
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(6.dp))
            .then(
                if (onLongClick != null) {
                    Modifier.combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = if (isDanger) Color(0x33EF4444) else AppColors.pressedColor),
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                } else {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = if (isDanger) Color(0x33EF4444) else AppColors.pressedColor),
                        onClick = onClick
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}
