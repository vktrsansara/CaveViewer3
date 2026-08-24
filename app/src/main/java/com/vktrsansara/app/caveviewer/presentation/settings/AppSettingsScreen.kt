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
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.domain.model.AppSettings
import com.vktrsansara.app.caveviewer.domain.model.AppTheme
import com.vktrsansara.app.caveviewer.ui.theme.BgCard
import com.vktrsansara.app.caveviewer.ui.theme.BgMain
import com.vktrsansara.app.caveviewer.ui.theme.BgSurface
import com.vktrsansara.app.caveviewer.ui.theme.BorderColor
import com.vktrsansara.app.caveviewer.ui.theme.PressedColor
import com.vktrsansara.app.caveviewer.ui.theme.TextPrimary
import com.vktrsansara.app.caveviewer.ui.theme.TextSecondary

/**
 * Screen displaying the application settings (Theme, Fullscreen mode).
 */
@Composable
fun AppSettingsScreen(
    settings: AppSettings,
    onThemeChanged: (AppTheme) -> Unit,
    onFullscreenChanged: (Boolean) -> Unit,
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
            .background(BgMain)
    ) {
        // 1. Settings Header
        SettingsHeader(onNavigateBack = onNavigateBack)

        // 2. Settings Content Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Block 1: Theme
            SettingsCard {
                Text(
                    text = "Тема оформления",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                ThemeOptionRow(
                    title = "Авто",
                    selected = settings.theme == AppTheme.AUTO,
                    onClick = { onThemeChanged(AppTheme.AUTO) }
                )

                ThemeOptionRow(
                    title = "Светлая",
                    selected = settings.theme == AppTheme.LIGHT,
                    onClick = { onThemeChanged(AppTheme.LIGHT) }
                )

                ThemeOptionRow(
                    title = "Темная",
                    selected = settings.theme == AppTheme.DARK,
                    onClick = { onThemeChanged(AppTheme.DARK) }
                )
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
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    IconButton(
                        onClick = { isInfoDialogVisible = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Справка по интерфейсу",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = PressedColor),
                            onClick = { onFullscreenChanged(!settings.isFullscreen) }
                        )
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Во весь экран",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary
                    )

                    Switch(
                        checked = settings.isFullscreen,
                        onCheckedChange = onFullscreenChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextPrimary,
                            checkedTrackColor = Color(0xFF38BDF8),
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = BgSurface,
                            uncheckedBorderColor = BorderColor
                        )
                    )
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
            .background(BgSurface)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button: 32x32 dp
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(BgCard)
                    .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(6.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = PressedColor),
                        onClick = onNavigateBack
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Настройки приложения",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }

        HorizontalDivider(thickness = 1.dp, color = BorderColor)
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
            .background(BgCard)
            .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
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
                indication = ripple(color = PressedColor),
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF38BDF8),
                unselectedColor = TextSecondary
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = TextPrimary
        )
    }
}

@Preview
@Composable
private fun AppSettingsScreenPreview() {
    AppSettingsScreen(
        settings = AppSettings(),
        onThemeChanged = {},
        onFullscreenChanged = {},
        onNavigateBack = {}
    )
}
