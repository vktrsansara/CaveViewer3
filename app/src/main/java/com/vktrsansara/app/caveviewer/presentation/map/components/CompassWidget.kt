package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.ui.theme.AppColors

private val NeedleRedFill = Color(0xFFEF4444)
private val NeedleRedStroke = Color(0xFF991B1B)
private val NeedleSilverFill = Color(0xFFCBD5E1)
private val NeedleSilverStroke = Color(0xFF475569)

private val CompassBgDark = Color(0x8C121820)
private val CompassBgLight = Color(0xD9FFFFFF)
private val CompassBorderDark = Color(0xFF000000)
private val CompassBorderLight = Color(0xFFCBD5E1)

/**
 * Interactive Compass Overlay Widget displaying north azimuth and current camera rotation.
 * Clicking resets map rotation bearing to 0°.
 */
@Composable
fun CompassWidget(
    angleNorth: Float,
    mapBearing: Double,
    onResetBearing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppColors.isDark
    val compassBg = if (isDark) CompassBgDark else CompassBgLight
    val compassBorder = if (isDark) CompassBorderDark else CompassBorderLight

    Column(
        modifier = modifier.size(width = 64.dp, height = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // North indicator label 'N'
        Text(
            text = "N",
            color = NeedleRedFill,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Circular compass disk
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(compassBg)
                .border(width = 2.dp, color = compassBorder, shape = CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = NeedleRedFill.copy(alpha = 0.3f)),
                    onClick = onResetBearing
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(48.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val needleLength = 18.dp.toPx()
                val needleHalfWidth = 5.dp.toPx()

                // Fixed rotation: angleNorth - mapBearing so needle points to true North as camera rotates
                val totalRotation = (angleNorth - mapBearing.toFloat())

                rotate(degrees = totalRotation, pivot = Offset(cx, cy)) {
                    // 1. North Needle (Pointing Up)
                    val northPath = Path().apply {
                        moveTo(cx, cy - needleLength)
                        lineTo(cx + needleHalfWidth, cy)
                        lineTo(cx - needleHalfWidth, cy)
                        close()
                    }
                    drawPath(path = northPath, color = NeedleRedFill)
                    drawPath(
                        path = northPath,
                        color = NeedleRedStroke,
                        style = Stroke(width = 1.5f)
                    )

                    // 2. South Needle (Pointing Down)
                    val southPath = Path().apply {
                        moveTo(cx, cy + needleLength)
                        lineTo(cx + needleHalfWidth, cy)
                        lineTo(cx - needleHalfWidth, cy)
                        close()
                    }
                    drawPath(path = southPath, color = NeedleSilverFill)
                    drawPath(
                        path = southPath,
                        color = NeedleSilverStroke,
                        style = Stroke(width = 1.5f)
                    )

                    // 3. Center Pivot Dot
                    drawCircle(
                        color = Color.White,
                        radius = 2.5.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = Color.Black,
                        radius = 2.5.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(width = 1f)
                    )
                }
            }
        }
    }
}
