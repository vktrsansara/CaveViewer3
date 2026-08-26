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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.AppSettings
import com.vktrsansara.app.caveviewer.domain.model.CompassTapMode
import com.vktrsansara.app.caveviewer.domain.model.ThemeMode
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import com.vktrsansara.app.caveviewer.ui.theme.CaveViewerTheme

/**
 * Screen displaying the application settings with H1/H2/H3 typography hierarchy and compact Speleo layout.
 */
@Composable
fun AppSettingsScreen(
    settings: AppSettings,
    onThemeChanged: (ThemeMode) -> Unit,
    onFullscreenChanged: (Boolean) -> Unit,
    onShowCompassChanged: (Boolean) -> Unit,
    onCompassTapModeChanged: (CompassTapMode) -> Unit,
    onShowScaleBarChanged: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isInfoDialogVisible by remember { mutableStateOf(false) }

    if (isInfoDialogVisible) {
        InterfaceInfoDialog(
            onDismiss = { isInfoDialogVisible = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bgMain)
    ) {
        // 1. Settings Header (H1)
        SettingsHeader(onNavigateBack = onNavigateBack)

        // 2. Settings Content Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Block 1: Theme
            SettingsCard {
                // H2: Section Title
                Text(
                    text = "Тема оформления",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    // H3: Options
                    ThemeOptionRow(
                        title = "Авто (системная)",
                        selected = settings.theme == ThemeMode.AUTO,
                        onClick = { onThemeChanged(ThemeMode.AUTO) }
                    )

                    ThemeOptionRow(
                        title = "Светлая",
                        selected = settings.theme == ThemeMode.LIGHT,
                        onClick = { onThemeChanged(ThemeMode.LIGHT) }
                    )

                    ThemeOptionRow(
                        title = "Темная",
                        selected = settings.theme == ThemeMode.DARK,
                        onClick = { onThemeChanged(ThemeMode.DARK) }
                    )
                }
            }

            // Block 2: Interface
            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Интерфейс",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textPrimary
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppColors.bgSurface)
                            .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(6.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = AppColors.pressedColor),
                                onClick = { isInfoDialogVisible = true }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Справка по интерфейсу",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
                Spacer(modifier = Modifier.height(6.dp))

                val switchColors = SwitchDefaults.colors(
                    checkedThumbColor = AppColors.textPrimary,
                    checkedTrackColor = AppColors.accent,
                    uncheckedThumbColor = AppColors.textSecondary,
                    uncheckedTrackColor = AppColors.bgSurface,
                    uncheckedBorderColor = AppColors.borderColor
                )

                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    // 1. Во весь экран
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = AppColors.pressedColor),
                                onClick = { onFullscreenChanged(!settings.isFullscreen) }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Во весь экран",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.textPrimary
                        )
                        Switch(
                            checked = settings.isFullscreen,
                            onCheckedChange = onFullscreenChanged,
                            modifier = Modifier.scale(0.85f),
                            colors = switchColors
                        )
                    }

                    // 2. Полоска масштаба
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = AppColors.pressedColor),
                                onClick = { onShowScaleBarChanged(!settings.showScaleBar) }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Полоска масштаба",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.textPrimary
                        )
                        Switch(
                            checked = settings.showScaleBar,
                            onCheckedChange = onShowScaleBarChanged,
                            modifier = Modifier.scale(0.85f),
                            colors = switchColors
                        )
                    }

                    // 3. Показывать компас
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = AppColors.pressedColor),
                                onClick = { onShowCompassChanged(!settings.showCompass) }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Показывать компас",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.textPrimary
                        )
                        Switch(
                            checked = settings.showCompass,
                            onCheckedChange = onShowCompassChanged,
                            modifier = Modifier.scale(0.85f),
                            colors = switchColors
                        )
                    }

                    // 4. Подблок: Нажатие на компас (показывается, если компас включен)
                    if (settings.showCompass) {
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Нажатие на компас",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.textPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.padding(start = 2.dp)
                        ) {
                            // Вариант 1: Выравнивание по горизонтали
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(color = AppColors.pressedColor),
                                        onClick = { onCompassTapModeChanged(CompassTapMode.HORIZONTAL) }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.compassTapMode == CompassTapMode.HORIZONTAL,
                                    onClick = { onCompassTapModeChanged(CompassTapMode.HORIZONTAL) },
                                    modifier = Modifier
                                        .size(20.dp)
                                        .scale(0.85f),
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = AppColors.accent,
                                        unselectedColor = AppColors.textSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Выравнивание по горизонтали",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.textPrimary
                                )
                            }

                            // Вариант 2: Выравнивание по экрану
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(color = AppColors.pressedColor),
                                        onClick = { onCompassTapModeChanged(CompassTapMode.SCREEN_NORTH) }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.compassTapMode == CompassTapMode.SCREEN_NORTH,
                                    onClick = { onCompassTapModeChanged(CompassTapMode.SCREEN_NORTH) },
                                    modifier = Modifier
                                        .size(20.dp)
                                        .scale(0.85f),
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = AppColors.accent,
                                        unselectedColor = AppColors.textSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Выравнивание по экрану",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.textPrimary
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
private fun SettingsHeader(
    onNavigateBack: () -> Unit
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
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button: 32x32 dp
            Box(
                modifier = Modifier
                    .size(32.dp)
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
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // H1: Screen Title
            Text(
                text = "Настройки приложения",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
        }

        HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
    }
}

@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.bgCard)
            .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        content()
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = AppColors.pressedColor),
                onClick = onClick
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            modifier = Modifier
                .size(20.dp)
                .scale(0.85f),
            colors = RadioButtonDefaults.colors(
                selectedColor = AppColors.accent,
                unselectedColor = AppColors.textSecondary
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // H3: Option Label
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.textPrimary
        )
    }
}

@Preview(name = "Settings - Dark Mode", showBackground = true)
@Composable
private fun AppSettingsScreenDarkPreview() {
    CaveViewerTheme(darkTheme = true) {
        AppSettingsScreen(
            settings = AppSettings(theme = ThemeMode.DARK),
            onThemeChanged = {},
            onFullscreenChanged = {},
            onShowCompassChanged = {},
            onCompassTapModeChanged = {},
            onShowScaleBarChanged = {},
            onNavigateBack = {}
        )
    }
}

@Preview(name = "Settings - Light Mode", showBackground = true)
@Composable
private fun AppSettingsScreenLightPreview() {
    CaveViewerTheme(darkTheme = false) {
        AppSettingsScreen(
            settings = AppSettings(theme = ThemeMode.LIGHT),
            onThemeChanged = {},
            onFullscreenChanged = {},
            onShowCompassChanged = {},
            onCompassTapModeChanged = {},
            onShowScaleBarChanged = {},
            onNavigateBack = {}
        )
    }
}
