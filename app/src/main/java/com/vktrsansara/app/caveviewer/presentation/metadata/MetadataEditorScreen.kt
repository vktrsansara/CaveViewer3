package com.vktrsansara.app.caveviewer.presentation.metadata

import java.util.Locale
import kotlin.math.roundToInt
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddLocation
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldDefinition
import com.vktrsansara.app.caveviewer.domain.model.LayerFieldType
import com.vktrsansara.app.caveviewer.domain.model.LayerSearchConfig
import com.vktrsansara.app.caveviewer.domain.model.LineLayer
import com.vktrsansara.app.caveviewer.domain.model.MapLocation
import com.vktrsansara.app.caveviewer.domain.model.MapMetadata
import com.vktrsansara.app.caveviewer.domain.model.NavigationConfig
import com.vktrsansara.app.caveviewer.domain.model.PointLayer
import com.vktrsansara.app.caveviewer.domain.model.SearchFieldItem
import com.vktrsansara.app.caveviewer.presentation.components.AppDialogContainer
import com.vktrsansara.app.caveviewer.presentation.components.DialogCancelButton
import com.vktrsansara.app.caveviewer.presentation.components.DialogSaveButton
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
    pointLayers: List<PointLayer> = emptyList(),
    lineLayers: List<LineLayer> = emptyList(),
    onSaveMetadata: (MapMetadata, MapLocation, List<EntranceCoordinate>, Map<String, List<CadastralItem>>) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(MetadataTab.MAIN) }

    val initialProjectName = remember(metadata) { metadata.projectName }
    val initialPpm = remember(metadata) {
        if (metadata.pixelsPerMeter > 0.0) String.format(Locale.US, "%.4f", metadata.pixelsPerMeter) else "0.0"
    }
    val initialScale = remember(metadata) {
        if (metadata.scaleMeters > 0.0) metadata.scaleMeters.toString() else "0.0"
    }
    val initialAngle = remember(metadata) {
        if (metadata.angleNorth > 0.0) metadata.angleNorth.toString() else "0.0"
    }
    val initialLocation = remember(location) { location }
    val initialEntrances = remember(entrances) { entrances }
    val initialCadastral = remember(cadastralData) { cadastralData }

    // Form fields initialized with current metadata
    var projectName by remember(metadata) { mutableStateOf(initialProjectName) }
    var pixelsPerMeter by remember(metadata) { mutableStateOf(initialPpm) }
    var scaleMeters by remember(metadata) { mutableStateOf(initialScale) }
    var angleNorth by remember(metadata) { mutableStateOf(initialAngle) }

    // Geolocation form fields
    var locationState by remember(location) { mutableStateOf(initialLocation) }
    var entrancesState by remember(entrances) { mutableStateOf(initialEntrances) }

    // Cadastral data form fields
    var cadastralState by remember(cadastralData) { mutableStateOf(initialCadastral) }

    // Layer search & navigation configs
    var pointsSearchConfigState by remember(metadata) { mutableStateOf(metadata.pointsSearchConfig) }
    var linesSearchConfigState by remember(metadata) { mutableStateOf(metadata.linesSearchConfig) }
    var navigationConfigState by remember(metadata) { mutableStateOf(metadata.navigationConfig) }

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
        projectName, pixelsPerMeter, scaleMeters, angleNorth,
        initialProjectName, initialPpm, initialScale, initialAngle,
        locationState, initialLocation, entrancesState, initialEntrances, cadastralState, initialCadastral,
        pointsSearchConfigState, metadata.pointsSearchConfig,
        linesSearchConfigState, metadata.linesSearchConfig,
        navigationConfigState, metadata.navigationConfig
    ) {
        projectName.trim() != initialProjectName.trim() ||
                pixelsPerMeter.trim() != initialPpm.trim() ||
                scaleMeters.trim() != initialScale.trim() ||
                angleNorth.trim() != initialAngle.trim() ||
                locationState != initialLocation ||
                entrancesState != initialEntrances ||
                cadastralState != initialCadastral ||
                pointsSearchConfigState != metadata.pointsSearchConfig ||
                linesSearchConfigState != metadata.linesSearchConfig ||
                navigationConfigState != metadata.navigationConfig
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
            crs = "Simple",
            pointsSearchConfig = pointsSearchConfigState,
            linesSearchConfig = linesSearchConfigState,
            navigationConfig = navigationConfigState
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
                LayersSearchHelpDialog(onDismiss = { isHelpDialogVisible = false })
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
                LayersTabContent(
                    pointLayers = pointLayers,
                    lineLayers = lineLayers,
                    pointsConfig = pointsSearchConfigState,
                    onPointsConfigChange = { pointsSearchConfigState = it },
                    linesConfig = linesSearchConfigState,
                    onLinesConfigChange = { linesSearchConfigState = it },
                    navigationConfig = navigationConfigState,
                    onNavigationConfigChange = { navigationConfigState = it }
                )
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
            .padding(horizontal = 12.dp, vertical = 10.dp),
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
private fun LayersTabContent(
    pointLayers: List<PointLayer>,
    lineLayers: List<LineLayer>,
    pointsConfig: LayerSearchConfig,
    onPointsConfigChange: (LayerSearchConfig) -> Unit,
    linesConfig: LayerSearchConfig,
    onLinesConfigChange: (LayerSearchConfig) -> Unit,
    navigationConfig: NavigationConfig,
    onNavigationConfigChange: (NavigationConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Сбор всех доступных кастомных и стандартных полей для точек
    val availablePointFields = remember(pointLayers) {
        val customFields = pointLayers.flatMap { it.fieldsSchema }.distinctBy { it.key }
        val defaults = listOf(
            LayerFieldDefinition(key = "typeCategory", name = "Категория", type = LayerFieldType.TEXT)
        )
        (defaults + customFields).distinctBy { it.key }
    }

    // Сбор всех доступных кастомных и стандартных полей для линий
    val availableLineFields = remember(lineLayers) {
        val customFields = lineLayers.flatMap { it.fieldsSchema }.distinctBy { it.key }
        val defaults = listOf(
            LayerFieldDefinition(key = "difficulty", name = "Сложность", type = LayerFieldType.TEXT),
            LayerFieldDefinition(key = "environmentType", name = "Среда (ореол)", type = LayerFieldType.TEXT)
        )
        (defaults + customFields).distinctBy { it.key }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        val hasPointLayers = pointLayers.isNotEmpty()
        val hasLineLayers = lineLayers.isNotEmpty()

        if (!hasPointLayers && !hasLineLayers) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.bgCard)
                    .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Слои точек и линий пока не созданы в проекте",
                    fontSize = 13.5.sp,
                    color = AppColors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            if (hasPointLayers) {
                LayerSearchConfigCard(
                    blockTitle = "Слой точек",
                    checkboxTitle = "Поиск по точкам",
                    defaultFieldTitle = "Название точки",
                    availableFields = availablePointFields,
                    config = pointsConfig,
                    onConfigChange = onPointsConfigChange
                )
            }

            if (hasLineLayers) {
                LayerSearchConfigCard(
                    blockTitle = "Слой линий",
                    checkboxTitle = "Поиск по линиям",
                    defaultFieldTitle = "Название линии",
                    availableFields = availableLineFields,
                    config = linesConfig,
                    onConfigChange = onLinesConfigChange
                )

                NavigationConfigCard(
                    config = navigationConfig,
                    onConfigChange = onNavigationConfigChange
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun LayerSearchConfigCard(
    blockTitle: String,
    checkboxTitle: String,
    defaultFieldTitle: String,
    availableFields: List<LayerFieldDefinition>,
    config: LayerSearchConfig,
    onConfigChange: (LayerSearchConfig) -> Unit
) {
    // Сворачивание по умолчанию (isExpanded = false)
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var isAddSearchFieldDropdownOpen by remember { mutableStateOf(false) }
    var isAddSubtitleDropdownOpen by remember { mutableStateOf(false) }

    val isSearchEnabled = config.isSearchEnabled

    // Синхронизация: если поиск выключен, принудительно сворачиваем
    LaunchedEffect(isSearchEnabled) {
        if (!isSearchEnabled) {
            isExpanded = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.bgCard)
            .border(
                width = 1.dp,
                color = if (isSearchEnabled) AppColors.borderColor else AppColors.borderColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        // А. Заголовок блока (Accordion Header)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = isSearchEnabled) { isExpanded = !isExpanded }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = isSearchEnabled,
                    onCheckedChange = { isChecked ->
                        isExpanded = isChecked
                        onConfigChange(config.copy(isSearchEnabled = isChecked))
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AccentSkyBlue,
                        uncheckedColor = AppColors.textSecondary,
                        checkmarkColor = AppColors.bgMain
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = checkboxTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSearchEnabled) AppColors.textPrimary else AppColors.textSecondary.copy(alpha = 0.6f)
                    )
                    Text(
                        text = blockTitle,
                        fontSize = 11.5.sp,
                        color = if (isSearchEnabled) AppColors.textSecondary else AppColors.textSecondary.copy(alpha = 0.5f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSearchEnabled) AppColors.bgSurface else AppColors.bgSurface.copy(alpha = 0.4f))
                    .border(
                        width = 1.dp,
                        color = if (isSearchEnabled) AppColors.borderColor else AppColors.borderColor.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable(enabled = isSearchEnabled) { isExpanded = !isExpanded },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                    tint = if (isSearchEnabled) AccentSkyBlue else AppColors.textSecondary.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Развернутое содержимое аккордеона
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
            ) {
                HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
                Spacer(modifier = Modifier.height(12.dp))

                // Б. Секция 1: «Поиск по атрибуту:»
                Text(
                    text = "Поиск по атрибуту:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentSkyBlue
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Основная строка по умолчанию («Название»)
                val nameItem = config.searchFields.firstOrNull { it.key == "name" }
                    ?: SearchFieldItem(key = "name", title = defaultFieldTitle, isEnabled = true)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = nameItem.isEnabled,
                        onCheckedChange = { checked ->
                            val updatedFields = if (config.searchFields.any { it.key == "name" }) {
                                config.searchFields.map {
                                    if (it.key == "name") it.copy(isEnabled = checked) else it
                                }
                            } else {
                                listOf(nameItem.copy(isEnabled = checked)) + config.searchFields
                            }
                            onConfigChange(config.copy(searchFields = updatedFields))
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = AccentSkyBlue,
                            uncheckedColor = AppColors.textSecondary,
                            checkmarkColor = AppColors.bgMain
                        )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Планка с текстом [Название точки / линии]
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppColors.bgSurface)
                            .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = defaultFieldTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Кнопка [+] справа
                    Box {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(AppColors.bgSurface)
                                .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                                .clickable { isAddSearchFieldDropdownOpen = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Добавить атрибут поиска",
                                tint = AccentSkyBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isAddSearchFieldDropdownOpen,
                            onDismissRequest = { isAddSearchFieldDropdownOpen = false },
                            modifier = Modifier
                                .background(AppColors.bgCard)
                                .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                        ) {
                            val existingKeys = config.searchFields.map { it.key }.toSet()
                            val unaddedFields = availableFields.filter { it.key !in existingKeys && it.key != "name" }

                            if (unaddedFields.isEmpty()) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (availableFields.isEmpty()) "Нет доступных кастомных полей" else "Все поля уже добавлены",
                                            fontSize = 12.5.sp,
                                            color = AppColors.textSecondary
                                        )
                                    },
                                    onClick = { isAddSearchFieldDropdownOpen = false },
                                    enabled = false
                                )
                            } else {
                                unaddedFields.forEach { field ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = field.name,
                                                fontSize = 13.sp,
                                                color = AppColors.textPrimary
                                            )
                                        },
                                        onClick = {
                                            isAddSearchFieldDropdownOpen = false
                                            val newItem = SearchFieldItem(key = field.key, title = field.name, isEnabled = true)
                                            onConfigChange(config.copy(searchFields = config.searchFields + newItem))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Добавленные кастомные поля поиска
                val customSearchFields = config.searchFields.filter { it.key != "name" }
                customSearchFields.forEach { item ->
                    CustomSearchFieldRow(
                        item = item,
                        availableFields = availableFields,
                        existingKeys = config.searchFields.map { it.key }.toSet(),
                        onToggleEnabled = { isEnabled ->
                            val updatedFields = config.searchFields.map {
                                if (it.key == item.key) it.copy(isEnabled = isEnabled) else it
                            }
                            onConfigChange(config.copy(searchFields = updatedFields))
                        },
                        onSelectField = { newField ->
                            val updatedFields = config.searchFields.map {
                                if (it.key == item.key) SearchFieldItem(key = newField.key, title = newField.name, isEnabled = item.isEnabled) else it
                            }
                            onConfigChange(config.copy(searchFields = updatedFields))
                        },
                        onDelete = {
                            val updatedFields = config.searchFields.filter { it.key != item.key }
                            onConfigChange(config.copy(searchFields = updatedFields))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
                Spacer(modifier = Modifier.height(12.dp))

                // В. Секция 2: «Подписи в результатах поиска:»
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Подписи в результатах:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentSkyBlue
                    )

                    Box {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(AppColors.bgSurface)
                                .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                                .clickable { isAddSubtitleDropdownOpen = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Добавить подпись",
                                tint = AccentSkyBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isAddSubtitleDropdownOpen,
                            onDismissRequest = { isAddSubtitleDropdownOpen = false },
                            modifier = Modifier
                                .background(AppColors.bgCard)
                                .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
                        ) {
                            val existingSubtitleKeys = config.subtitleFields.toSet()
                            val unaddedSubtitleFields = availableFields.filter { it.key !in existingSubtitleKeys }

                            if (unaddedSubtitleFields.isEmpty()) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (availableFields.isEmpty()) "Нет доступных кастомных полей" else "Все подписи уже добавлены",
                                            fontSize = 12.5.sp,
                                            color = AppColors.textSecondary
                                        )
                                    },
                                    onClick = { isAddSubtitleDropdownOpen = false },
                                    enabled = false
                                )
                            } else {
                                unaddedSubtitleFields.forEach { field ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = field.name,
                                                fontSize = 13.sp,
                                                color = AppColors.textPrimary
                                            )
                                        },
                                        onClick = {
                                            isAddSubtitleDropdownOpen = false
                                            onConfigChange(config.copy(subtitleFields = config.subtitleFields + field.key))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (config.subtitleFields.isEmpty()) {
                    Text(
                        text = "Подписи не выбраны (в результатах будет только название)",
                        fontSize = 12.sp,
                        color = AppColors.textSecondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    config.subtitleFields.forEach { subKey ->
                        val fieldName = availableFields.find { it.key == subKey }?.name ?: subKey
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AppColors.bgSurface)
                                    .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = fieldName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.textPrimary
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AppColors.bgSurface)
                                    .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                                    .clickable {
                                        val updatedSubtitles = config.subtitleFields.filter { it != subKey }
                                        onConfigChange(config.copy(subtitleFields = updatedSubtitles))
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Удалить подпись",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomSearchFieldRow(
    item: SearchFieldItem,
    availableFields: List<LayerFieldDefinition>,
    existingKeys: Set<String>,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectField: (LayerFieldDefinition) -> Unit,
    onDelete: () -> Unit
) {
    var isDropdownOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isEnabled,
            onCheckedChange = onToggleEnabled,
            colors = CheckboxDefaults.colors(
                checkedColor = AccentSkyBlue,
                uncheckedColor = AppColors.textSecondary,
                checkmarkColor = AppColors.bgMain
            )
        )

        Spacer(modifier = Modifier.width(6.dp))

        Box(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AppColors.bgSurface)
                    .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                    .clickable { isDropdownOpen = true }
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.textPrimary
                )

                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = "Выбрать поле",
                    tint = AccentSkyBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = isDropdownOpen,
                onDismissRequest = { isDropdownOpen = false },
                modifier = Modifier
                    .background(AppColors.bgCard)
                    .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp))
            ) {
                val candidateFields = availableFields.filter { it.key !in existingKeys || it.key == item.key }
                candidateFields.forEach { field ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = field.name,
                                fontSize = 13.sp,
                                color = if (field.key == item.key) AccentSkyBlue else AppColors.textPrimary
                            )
                        },
                        onClick = {
                            isDropdownOpen = false
                            if (field.key != item.key) {
                                onSelectField(field)
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(AppColors.bgSurface)
                .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                .clickable(onClick = onDelete),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = "Удалить поле поиска",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun NavigationConfigCard(
    config: NavigationConfig,
    onConfigChange: (NavigationConfig) -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var isHelpDialogOpen by remember { mutableStateOf(false) }

    val isNavEnabled = config.isEnabled

    if (isHelpDialogOpen) {
        NavigationHelpDialog(onDismiss = { isHelpDialogOpen = false })
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.bgCard)
            .border(
                width = 1.dp,
                color = if (isNavEnabled) AccentSkyBlue.copy(alpha = 0.5f) else AppColors.borderColor,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        // Заголовок блока
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = isNavEnabled,
                    onCheckedChange = { isChecked ->
                        onConfigChange(config.copy(isEnabled = isChecked))
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AccentSkyBlue,
                        uncheckedColor = AppColors.textSecondary,
                        checkmarkColor = AppColors.bgMain
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "Навигация по ходам",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isNavEnabled) AppColors.textPrimary else AppColors.textSecondary.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Построение маршрутов A*",
                        fontSize = 11.5.sp,
                        color = if (isNavEnabled) AccentSkyBlue else AppColors.textSecondary.copy(alpha = 0.5f)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Кнопка [ℹ️ Справка]
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AppColors.bgSurface)
                        .border(
                            width = 1.dp,
                            color = AppColors.borderColor,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AppColors.pressedColor),
                            onClick = { isHelpDialogOpen = true }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "Справка",
                        tint = GreenInfoColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Кнопка сворачивания [🔽 / 🔼]
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AppColors.bgSurface)
                        .border(
                            width = 1.dp,
                            color = AppColors.borderColor,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { isExpanded = !isExpanded },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                        tint = AccentSkyBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Развернутое содержимое
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
            ) {
                HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
                Spacer(modifier = Modifier.height(12.dp))

                // 1. Слайдер «Точность построения маршрута»
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Точность построения маршрута:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textPrimary
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f", config.accuracyQuality),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentSkyBlue
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Быстро (1.0)",
                        fontSize = 11.sp,
                        color = AppColors.textSecondary
                    )
                    Text(
                        text = "Точно (2.0)",
                        fontSize = 11.sp,
                        color = AppColors.textSecondary
                    )
                }

                Slider(
                    value = config.accuracyQuality,
                    onValueChange = { newVal ->
                        val rounded = (newVal * 10f).roundToInt() / 10f
                        onConfigChange(config.copy(accuracyQuality = rounded))
                    },
                    valueRange = 1.0f..2.0f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentSkyBlue,
                        activeTrackColor = AccentSkyBlue,
                        inactiveTrackColor = AppColors.borderColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "При значении 1.0 выбирается кратчайший физический путь. При 2.0 сильно штрафуются узости и сложные участки, отдавая приоритет просторным ходам.",
                    fontSize = 11.5.sp,
                    color = AppColors.textSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                // 2. Чекбокс «Альтернативный маршрут»
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            onConfigChange(config.copy(isAlternativeRouteEnabled = !config.isAlternativeRouteEnabled))
                        }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = config.isAlternativeRouteEnabled,
                        onCheckedChange = { isChecked ->
                            onConfigChange(config.copy(isAlternativeRouteEnabled = isChecked))
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = AccentSkyBlue,
                            uncheckedColor = AppColors.textSecondary,
                            checkmarkColor = AppColors.bgMain
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "Альтернативный маршрут",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.textPrimary
                        )
                        Text(
                            text = "Рассчитывает второй вариант пути в разветвленных лабиринтах",
                            fontSize = 11.5.sp,
                            color = AppColors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationHelpDialog(onDismiss: () -> Unit) {
    AppDialogContainer(
        title = "Справка: Навигация",
        onDismissRequest = onDismiss,
        buttons = {
            DialogCancelButton(text = "Закрыть", onClick = onDismiss)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Предупреждающая плашка
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2E2000))
                    .border(width = 1.dp, color = Color(0xFFD97706), shape = RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "⚠️",
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Эта функция актуальна для больших пещерных систем лабиринтного типа",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFDE047),
                        lineHeight = 17.sp
                    )
                }
            }

            HelpItem(
                title = "Построение графа ходов",
                description = "Навигатор объединяет векторные линии ходов пещеры в единый топологический граф. Учитываются физическая длина отрезков в метрах и коэффициент их сложности (0.1..8.0)."
            )

            HelpItem(
                title = "Алгоритм A* и точность",
                description = "Маршрут строится с помощью алгоритма A* с весовой формулой: вес = длина × сложность^точность. При значении 1.0 строится кратчайший путь, а при 2.0 система избегает узостей, завалов и трудных участков."
            )

            HelpItem(
                title = "Альтернативный маршрут",
                description = "При включенной опции система рассчитывает второй путь через параллельные галереи и обходные кольцовки лабиринта."
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
    AppDialogContainer(
        title = "Несохраненные изменения",
        onDismissRequest = onDismiss,
        buttons = {
            DialogCancelButton(
                text = "Без сохран.",
                onClick = onDiscardAndExit
            )
            Spacer(modifier = Modifier.width(8.dp))
            DialogSaveButton(
                text = "Сохранить",
                onClick = onSaveAndExit
            )
        }
    ) {
        Text(
            text = "Вы внесли изменения, которые не были сохранены. Хотите сохранить их перед выходом?",
            fontSize = 13.sp,
            color = AppColors.textSecondary,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun MainMetadataHelpDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Основные данные",
        onDismissRequest = onDismiss,
        buttons = {
            DialogCancelButton(
                text = "Закрыть",
                onClick = onDismiss
            )
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
    }
}

@Composable
private fun LayersSearchHelpDialog(
    onDismiss: () -> Unit
) {
    AppDialogContainer(
        title = "Справка: Поиск по слоям",
        onDismissRequest = onDismiss,
        buttons = {
            DialogCancelButton(
                text = "Закрыть",
                onClick = onDismiss
            )
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            HelpItem(
                title = "Поиск по точкам и линиям:",
                description = "Включает или выключает индексацию и поиск объектов соответствующего векторного слоя."
            )
            HelpItem(
                title = "Поиск по атрибуту:",
                description = "Позволяет искать объекты не только по их названию, но и по значениям кастомных полей (псевдонимы, описание, глубина и т.д.)."
            )
            HelpItem(
                title = "Подписи в результатах поиска:",
                description = "Значения выбранных полей будут выводиться в качестве дополнительной подписи под названием объекта в списке результатов."
            )
        }
    }
}

@Composable
private fun HelpItem(title: String, description: String) {
    Column {
        Text(
            text = title,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF38BDF8)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            fontSize = 12.sp,
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
