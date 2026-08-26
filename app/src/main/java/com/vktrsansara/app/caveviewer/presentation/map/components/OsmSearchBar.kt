package com.vktrsansara.app.caveviewer.presentation.map.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.location.GeocodingResult
import com.vktrsansara.app.caveviewer.domain.location.OsmGeocodingService
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import kotlinx.coroutines.delay

/**
 * OpenStreetMap interactive search bar with debounced query and results dropdown.
 */
@Composable
fun OsmSearchBar(
    onSelectLocation: (lat: Double, lon: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<GeocodingResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isDropdownVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Debounced search trigger
    LaunchedEffect(query) {
        if (query.trim().length >= 3) {
            delay(500) // Wait 500ms before sending request
            isLoading = true
            val searchResults = OsmGeocodingService.search(query)
            results = searchResults
            isLoading = false
            isDropdownVisible = searchResults.isNotEmpty()
        } else {
            results = emptyList()
            isLoading = false
            isDropdownVisible = false
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Search Input Field
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = {
                Text(
                    text = "Поиск места на карте (город, гора, урочище)...",
                    color = AppColors.textSecondary.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Поиск",
                    tint = AccentSkyBlue,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = AccentSkyBlue,
                        modifier = Modifier.size(16.dp)
                    )
                } else if (query.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Очистить",
                        tint = AppColors.textSecondary,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = false, radius = 14.dp),
                                onClick = {
                                    query = ""
                                    results = emptyList()
                                    isDropdownVisible = false
                                }
                            )
                    )
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AppColors.textPrimary,
                unfocusedTextColor = AppColors.textPrimary,
                focusedBorderColor = AccentSkyBlue,
                unfocusedBorderColor = AppColors.borderColor,
                cursorColor = AccentSkyBlue,
                focusedContainerColor = AppColors.bgCard.copy(alpha = 0.95f),
                unfocusedContainerColor = AppColors.bgCard.copy(alpha = 0.92f)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(8.dp))
        )

        // Dropdown Search Results
        AnimatedVisibility(
            visible = isDropdownVisible && results.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.bgCard)
                    .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp)
                ) {
                    items(results) { result ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = AppColors.pressedColor),
                                    onClick = {
                                        onSelectLocation(result.lat, result.lon)
                                        isDropdownVisible = false
                                        focusManager.clearFocus()
                                    }
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF10B981), // Emerald
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = result.name,
                                fontSize = 12.5.sp,
                                color = AppColors.textPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = AppColors.borderColor.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}
