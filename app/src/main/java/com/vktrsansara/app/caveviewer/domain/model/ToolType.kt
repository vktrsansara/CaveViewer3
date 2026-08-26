package com.vktrsansara.app.caveviewer.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Architecture
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.LocationSearching
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.SquareFoot
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolType(
    val title: String,
    val icon: ImageVector,
    val color: Color
) {
    RULER("Линейка", Icons.Rounded.Straighten, Color(0xFF8A2BE2)),
    AREA("Площадь", Icons.Rounded.SquareFoot, Color(0xFFA855F7)),
    ANGLE("Угол", Icons.Rounded.Architecture, Color(0xFFF59E0B)),
    AZIMUTH("Азимут", Icons.Rounded.Explore, Color(0xFF06B6D4)),
    FAULT_LINE("Ось разломов", Icons.Rounded.Timeline, Color(0xFFEC4899)),
    DELTA_OFFSET("Смещение (ΔX, ΔY)", Icons.Rounded.LocationSearching, Color(0xFF6366F1)),
    RADIUS("Радиус", Icons.Rounded.RadioButtonUnchecked, Color(0xFF10B981))
}
