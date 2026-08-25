package com.vktrsansara.app.caveviewer.presentation.metadata

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.AddLocation
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vktrsansara.app.caveviewer.domain.model.CadastralItem
import com.vktrsansara.app.caveviewer.domain.model.EntranceCoordinate
import com.vktrsansara.app.caveviewer.domain.model.MapLocation
import com.vktrsansara.app.caveviewer.domain.model.MapMetadata
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.ui.theme.AccentRed
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import com.vktrsansara.app.caveviewer.ui.theme.CaveViewerTheme

private val LockGoldColor = Color(0xFFFBBF24)
private val GreenInfoColor = Color(0xFF10B981)

enum class MetadataTab(val title: String, val icon: ImageVector, val color: Color) {
    MAIN("Основное", Icons.Rounded.Tune, Color(0xFF38BDF8)),
    GEOLOCATION("Геолокация", Icons.Rounded.AddLocation, Color(0xFF10B981)),
    CADASTRE("Кадастр", Icons.Rounded.Description, Color(0xFFA78BFA)),
    LAYERS("Слои", Icons.Rounded.Layers, Color(0xFFF59E0B))
}

/**
 * Screen for viewing and editing project map metadata with classic structured cards and styled tabs.
 */
@Composable
fun MetadataEditorScreen(
    metadata: MapMetadata,
    location: MapLocation = MapLocation(),
    entrances: List<EntranceCoordinate> = emptyList(),
    cadastralData: Map<String, List<CadastralItem>> = emptyMap(),
    onSaveMetadata: (MapMetadata, MapLocation, List<EntranceCoordinate>, Map<String, List<CadastralItem>>) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(MetadataTab.MAIN) }

    // Form fields initialized with current metadata
    var projectName by remember(metadata) { mutableStateOf(metadata.projectName) }
    var pixelsPerMeter by remember(metadata) {
        mutableStateOf(if (metadata.pixelsPerMeter > 0.0) metadata.pixelsPerMeter.toString() else "0.0")
    }
    var scaleMeters by remember(metadata) {
        mutableStateOf(if (metadata.scaleMeters > 0.0) metadata.scaleMeters.toString() else "0.0")
    }
    var angleNorth by remember(metadata) {
        mutableStateOf(if (metadata.angleNorth > 0.0) metadata.angleNorth.toString() else "0.0")
    }

    // Geolocation form fields
    var locationState by remember(location) { mutableStateOf(location) }
    var entrancesState by remember(entrances) { mutableStateOf(entrances) }

    // Cadastral data form fields
    var cadastralState by remember(cadastralData) { mutableStateOf(cadastralData) }

    // Lock states (default locked)
    var isNameLocked by remember { mutableStateOf(true) }
    var isPpmLocked by remember { mutableStateOf(true) }
    var isScaleLocked by remember { mutableStateOf(true) }
    var isAngleLocked by remember { mutableStateOf(true) }
    var isCrsLocked by remember { mutableStateOf(true) }

    // Dialog states
    var isHelpDialogVisible by remember { mutableStateOf(false) }
    var isUnsavedChangesDialogVisible by remember { mutableStateOf(false) }

    val hasUnsavedChanges = remember(
        projectName, pixelsPerMeter, scaleMeters, angleNorth, metadata,
        locationState, location, entrancesState, entrances, cadastralState, cadastralData
    ) {
        projectName.trim() != metadata.projectName ||
                (pixelsPerMeter.toDoubleOrNull() ?: 0.0) != metadata.pixelsPerMeter ||
                (scaleMeters.toDoubleOrNull() ?: 0.0) != metadata.scaleMeters ||
                (angleNorth.toDoubleOrNull() ?: 0.0) != metadata.angleNorth ||
                locationState != location ||
                entrancesState != entrances ||
                cadastralState != cadastralData
    }

    val handleBackPress = {
        if (hasUnsavedChanges) {
            isUnsavedChangesDialogVisible = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler(onBack = handleBackPress)

    val doSave = {
        val ppmVal = pixelsPerMeter.toDoubleOrNull() ?: 0.0
        val scaleVal = scaleMeters.toDoubleOrNull() ?: 0.0
        val angleVal = angleNorth.toDoubleOrNull() ?: 0.0
        val updated = metadata.copy(
            projectName = projectName.trim(),
            pixelsPerMeter = ppmVal,
            scaleMeters = scaleVal,
            angleNorth = angleVal,
            crs = "Simple"
        )
        onSaveMetadata(updated, locationState, entrancesState, cadastralState)
    }

    // Unsaved Changes Confirmation Dialog
    if (isUnsavedChangesDialogVisible) {
        UnsavedChangesDialog(
            onSaveAndExit = {
                isUnsavedChangesDialogVisible = false
                doSave()
                onNavigateBack()
            },
            onDiscardAndExit = {
                isUnsavedChangesDialogVisible = false
                onNavigateBack()
            },
            onDismiss = { isUnsavedChangesDialogVisible = false }
        )
    }

    // Contextual Help Dialog
    if (isHelpDialogVisible) {
        when (selectedTab) {
            MetadataTab.MAIN -> {
                MainMetadataHelpDialog(onDismiss = { isHelpDialogVisible = false })
            }
            MetadataTab.GEOLOCATION -> {
                GeolocationHelpDialog(onDismiss = { isHelpDialogVisible = false })
            }
            MetadataTab.CADASTRE -> {
                CadastralHelpDialog(onDismiss = { isHelpDialogVisible = false })
            }
            MetadataTab.LAYERS -> {
                UnderDevelopmentHelpDialog(
                    title = "Справка: Слои",
                    description = "Раздел управления слоями карты находится в разработке.",
                    onDismiss = { isHelpDialogVisible = false }
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bgMain)
    ) {
        // 1. Screen Header
        MetadataHeader(
            title = "Метаданные карты",
            onNavigateBack = handleBackPress,
            onInfoClick = { isHelpDialogVisible = true },
            onSaveClick = doSave
        )

        // 2. Tab Navigation Bar
        MetadataTabBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        // 3. Tab Content
        when (selectedTab) {
            MetadataTab.MAIN -> {
                MainTabContent(
                    metadata = metadata,
                    projectName = projectName,
                    onProjectNameChange = { projectName = it },
                    isNameLocked = isNameLocked,
                    onToggleNameLock = { isNameLocked = !isNameLocked },
                    pixelsPerMeter = pixelsPerMeter,
                    onPixelsPerMeterChange = { pixelsPerMeter = it },
                    isPpmLocked = isPpmLocked,
                    onTogglePpmLock = { isPpmLocked = !isPpmLocked },
                    scaleMeters = scaleMeters,
                    onScaleMetersChange = { scaleMeters = it },
                    isScaleLocked = isScaleLocked,
                    onToggleScaleLock = { isScaleLocked = !isScaleLocked },
                    angleNorth = angleNorth,
                    onAngleNorthChange = { angleNorth = it },
                    isAngleLocked = isAngleLocked,
                    onToggleAngleLock = { isAngleLocked = !isAngleLocked },
                    isCrsLocked = isCrsLocked,
                    onToggleCrsLock = { isCrsLocked = !isCrsLocked }
                )
            }
            MetadataTab.GEOLOCATION -> {
                GeolocationTab(
                    location = locationState,
                    onLocationChange = { locationState = it },
                    entrances = entrancesState,
                    onEntrancesChange = { entrancesState = it }
                )
            }
            MetadataTab.CADASTRE -> {
                CadastralTab(
                    cadastralData = cadastralState,
                    onCadastralDataChange = { cadastralState = it }
                )
            }
            MetadataTab.LAYERS -> {
                TabUnderDevelopmentContent(tabTitle = selectedTab.title)
            }
        }
    }
}

@Composable
private fun MetadataHeader(
    title: String,
    onNavigateBack: () -> Unit,
    onInfoClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.bgSurface)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button: 34x34 dp
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AppColors.bgCard)
                    .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = AppColors.pressedColor),
                        onClick = onNavigateBack
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = AccentSkyBlue,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary,
                modifier = Modifier.weight(1f)
            )

            // Info Button (34x34 dp) with green 'i'
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AppColors.bgCard)
                    .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = AppColors.pressedColor),
                        onClick = onInfoClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = "Справка",
                    tint = GreenInfoColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Save Button (34x34 dp) with blue floppy icon
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AppColors.bgCard)
                    .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = AppColors.pressedColor),
                        onClick = onSaveClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Save,
                    contentDescription = "Сохранить",
                    tint = AccentSkyBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
    }
}

@Composable
private fun MetadataTabBar(
    selectedTab: MetadataTab,
    onTabSelected: (MetadataTab) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.bgSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MetadataTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.pressedColor),
                            onClick = { onTabSelected(tab) }
                        )
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = if (isSelected) tab.color else tab.color.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = tab.title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) tab.color else AppColors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Bottom underline indicator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(2.dp)
                            .background(if (isSelected) tab.color else Color.Transparent)
                    )
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
    }
}

@Composable
private fun MainTabContent(
    metadata: MapMetadata,
    projectName: String,
    onProjectNameChange: (String) -> Unit,
    isNameLocked: Boolean,
    onToggleNameLock: () -> Unit,
    pixelsPerMeter: String,
    onPixelsPerMeterChange: (String) -> Unit,
    isPpmLocked: Boolean,
    onTogglePpmLock: () -> Unit,
    scaleMeters: String,
    onScaleMetersChange: (String) -> Unit,
    isScaleLocked: Boolean,
    onToggleScaleLock: () -> Unit,
    angleNorth: String,
    onAngleNorthChange: (String) -> Unit,
    isAngleLocked: Boolean,
    onToggleAngleLock: () -> Unit,
    isCrsLocked: Boolean,
    onToggleCrsLock: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Card 1: Project Name
        MetadataSectionCard {
            Text(
                text = "Название проекта",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GoldLockToggleButton(
                    isLocked = isNameLocked,
                    onToggle = onToggleNameLock
                )

                Spacer(modifier = Modifier.width(8.dp))

                DarkTextInputField(
                    value = projectName,
                    onValueChange = onProjectNameChange,
                    enabled = !isNameLocked,
                    placeholder = "Введите название проекта",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "По названию проекта создается папка в /Documents/CaveViewer/Projects/",
                fontSize = 11.sp,
                color = AppColors.textSecondary,
                lineHeight = 15.sp
            )
        }

        // Card 2: Map Characteristics
        MetadataSectionCard {
            Text(
                text = "Характеристики карты",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Readonly Info Rows
            InfoRow(label = "Тип карты:", value = "Тайловая")
            InfoRow(label = "Исходное разрешение:", value = "${metadata.imageWidth} x ${metadata.imageHeight}")
            InfoRow(label = "Разрешение тайлов:", value = "${metadata.tileSize}")

            Spacer(modifier = Modifier.height(4.dp))

            // Zoom levels row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Уровни зума:",
                    fontSize = 13.sp,
                    color = AppColors.textSecondary
                )

                Text(
                    text = "MIN: ${metadata.zoomMin}  •  MAX: ${metadata.zoomMax}  •  DEF: ${metadata.zoomDefault}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
            Spacer(modifier = Modifier.height(8.dp))

            // Editable Rows with Lock Buttons
            EditableParamRow(
                label = "Пикселей на метр:",
                value = pixelsPerMeter,
                onValueChange = onPixelsPerMeterChange,
                suffix = "px",
                isLocked = isPpmLocked,
                onToggleLock = onTogglePpmLock
            )

            EditableParamRow(
                label = "Масштаб:",
                value = scaleMeters,
                onValueChange = onScaleMetersChange,
                suffix = "m",
                isLocked = isScaleLocked,
                onToggleLock = onToggleScaleLock
            )

            EditableParamRow(
                label = "Угол на Север:",
                value = angleNorth,
                onValueChange = onAngleNorthChange,
                suffix = "°",
                isLocked = isAngleLocked,
                onToggleLock = onToggleAngleLock
            )

            // Coordinate System Row (Fixed to Simple)
            CrsSimpleRow(
                isLocked = isCrsLocked,
                onToggleLock = onToggleCrsLock
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = AppColors.textSecondary
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.textPrimary
        )
    }
}

@Composable
private fun EditableParamRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String,
    isLocked: Boolean,
    onToggleLock: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GoldLockToggleButton(
            isLocked = isLocked,
            onToggle = onToggleLock
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = label,
            fontSize = 13.sp,
            color = AppColors.textPrimary,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            modifier = Modifier
                .width(110.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isLocked) AppColors.bgSurface.copy(alpha = 0.5f) else AppColors.bgSurface)
                .border(
                    width = 1.dp,
                    color = if (isLocked) AppColors.borderColor else AccentSkyBlue,
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = !isLocked,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isLocked) AppColors.textSecondary else AppColors.textPrimary,
                    textAlign = TextAlign.End
                ),
                cursorBrush = SolidColor(AccentSkyBlue),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = suffix,
                fontSize = 12.sp,
                color = AppColors.textSecondary
            )
        }
    }
}

@Composable
private fun CrsSimpleRow(
    isLocked: Boolean,
    onToggleLock: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GoldLockToggleButton(
            isLocked = isLocked,
            onToggle = onToggleLock
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Система координат:",
            fontSize = 13.sp,
            color = AppColors.textPrimary,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .width(110.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isLocked) AppColors.bgSurface.copy(alpha = 0.5f) else AppColors.bgSurface)
                .border(
                    width = 1.dp,
                    color = if (isLocked) AppColors.borderColor else AccentSkyBlue,
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Simple",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isLocked) AppColors.textSecondary else AppColors.textPrimary
            )
        }
    }
}

@Composable
private fun GoldLockToggleButton(
    isLocked: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isLocked) AppColors.bgSurface.copy(alpha = 0.5f) else AccentSkyBlue.copy(alpha = 0.15f))
            .border(
                width = 1.dp,
                color = if (isLocked) AppColors.borderColor else AccentSkyBlue,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = AppColors.pressedColor),
                onClick = onToggle
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
            contentDescription = if (isLocked) "Разблокировать" else "Заблокировать",
            tint = if (isLocked) LockGoldColor else AccentSkyBlue,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun DarkTextInputField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (enabled) AppColors.bgSurface else AppColors.bgSurface.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = if (enabled) AccentSkyBlue else AppColors.borderColor,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = if (enabled) AppColors.textPrimary else AppColors.textSecondary
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

@Composable
private fun MetadataSectionCard(
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

@Composable
private fun TabUnderDevelopmentContent(tabTitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Раздел «$tabTitle» находится в разработке",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = AppColors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun UnsavedChangesDialog(
    onSaveAndExit: () -> Unit,
    onDiscardAndExit: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.bgCard)
                .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Несохраненные изменения",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Вы внесли изменения, которые не были сохранены. Хотите сохранить их перед выходом?",
                fontSize = 13.sp,
                color = AppColors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Save Button (Blue Solid)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF007AFF))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.White.copy(alpha = 0.3f)),
                            onClick = onSaveAndExit
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Сохранить",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Discard Button (Red Border)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Transparent)
                        .border(width = 1.dp, color = AccentRed, shape = RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AccentRed.copy(alpha = 0.2f)),
                            onClick = onDiscardAndExit
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Без сохран.",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentRed
                    )
                }
            }
        }
    }
}

@Composable
private fun MainMetadataHelpDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.bgCard)
                .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Справка: Основные данные",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HelpItem(
                    title = "Название проекта:",
                    description = "Название папки проекта в файловой системе устройства."
                )
                HelpItem(
                    title = "Пикселей на метр (px):",
                    description = "Физический масштаб растра. Показывает, сколько пикселей изображения приходится на 1 метр в реальности."
                )
                HelpItem(
                    title = "Масштаб (m):",
                    description = "Масштабный шаг (например, если масштаб 10м, а пикселей на метр 12, то 10 метров равны 120 пикселям)."
                )
                HelpItem(
                    title = "Угол на Север (0..360°):",
                    description = "Направление истинного севера на схеме пещеры (угол отклонения от верхнего края экрана)."
                )
                HelpItem(
                    title = "Система координат (Simple):",
                    description = "Прямоугольная декартова система координат изображения (X, Y в пикселях/метрах) для схем пещер без географической привязки."
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                DialogCancelButton(
                    text = "Закрыть",
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
private fun UnderDevelopmentHelpDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.bgCard)
                .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                color = AppColors.textSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                DialogCancelButton(
                    text = "Закрыть",
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
private fun HelpItem(title: String, description: String) {
    Column {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.textPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            fontSize = 12.5.sp,
            color = AppColors.textSecondary,
            lineHeight = 17.sp
        )
    }
}

@Preview
@Composable
private fun MetadataEditorScreenPreview() {
    CaveViewerTheme(darkTheme = true) {
        MetadataEditorScreen(
            metadata = MapMetadata(
                projectName = "Снежная пещера",
                imageWidth = 6400,
                imageHeight = 4800,
                tileSize = 256,
                zoomMin = 1,
                zoomMax = 5,
                zoomDefault = 3,
                pixelsPerMeter = 12.5,
                scaleMeters = 10.0,
                angleNorth = 15.0,
                crs = "Simple"
            ),
            location = MapLocation(country = "Россия", region = "Кавказ", district = "Арабика"),
            entrances = listOf(
                EntranceCoordinate(pointIndex = 0, name = "Главный вход", lat = 43.456, lon = 40.123, alt = 1250.0)
            ),
            cadastralData = emptyMap(),
            onSaveMetadata = { _, _, _, _ -> },
            onNavigateBack = {}
        )
    }
}
