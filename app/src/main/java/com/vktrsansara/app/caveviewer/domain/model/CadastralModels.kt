package com.vktrsansara.app.caveviewer.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 7 standard cadastral subcategories for cave documentation with thematic semantic colors.
 */
enum class CadastralSection(
    val key: String,
    val title: String,
    val icon: ImageVector,
    val color: Color
) {
    CLASSIFICATION("classification", "Классификация", Icons.Rounded.Category, Color(0xFFFB923C)),   // Orange
    TOPOLOGY("topology", "Топология", Icons.Rounded.AccountTree, Color(0xFF818CF8)),               // Indigo
    MORPHOLOGY("morphology", "Морфология", Icons.Rounded.Landscape, Color(0xFF10B981)),            // Emerald Green
    CLIMATE("climate", "Климат", Icons.Rounded.Thermostat, Color(0xFFF43F5E)),                     // Coral / Rose
    HYDROLOGY("hydrology", "Гидрология", Icons.Rounded.WaterDrop, Color(0xFF06B6D4)),              // Cyan / Aqua
    BIOTA("biota", "Биота", Icons.Rounded.Forest, Color(0xFF84CC16)),                              // Lime / Forest
    DESCRIPTION("description", "Дополнительно", Icons.Rounded.Description, Color(0xFFFBBF24))       // Amber / Gold
}

/**
 * Individual cadastral record with title and description within a category.
 */
data class CadastralItem(
    val id: Long = 0,
    val section: String,
    val title: String = "",
    val content: String = ""
)
