package com.vktrsansara.app.caveviewer.presentation.help

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Terminal
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import com.vktrsansara.app.caveviewer.ui.theme.CaveViewerTheme

/**
 * Screen displaying application details, version 3.0.0, and external links.
 */
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBack)
    val uriHandler = LocalUriHandler.current

    val openUrl: (String) -> Unit = { url ->
        try {
            uriHandler.openUri(url)
        } catch (_: Exception) {
            // Fallback gracefully if no browser handler is found
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bgMain)
    ) {
        // Standard Header
        AboutHeader(onNavigateBack = onNavigateBack)

        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: App Info & Version
            AboutCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColors.bgSurface)
                            .border(1.dp, AccentSkyBlue.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Explore,
                            contentDescription = null,
                            tint = AccentSkyBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "CaveViewer",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.textPrimary
                        )
                        Text(
                            text = "Версия 3.0.0",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentSkyBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Приложение для просмотра, измерений и редактирования спелеологических карт, топосъемок и крупномасштабных растровых планов пещер и каменоломен.",
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    color = AppColors.textSecondary
                )
            }

            // Card 2: Links
            AboutCard {
                SectionHeader(
                    icon = Icons.Rounded.Link,
                    title = "Ссылки",
                    tint = AccentSkyBlue
                )

                Spacer(modifier = Modifier.height(6.dp))

                LinkRow(
                    icon = Icons.AutoMirrored.Rounded.Send,
                    iconTint = Color(0xFF38BDF8),
                    title = "Телеграмм",
                    url = "https://t.me/+KBAAkh2s3aw1N2My",
                    onClick = { openUrl("https://t.me/+KBAAkh2s3aw1N2My") }
                )

                HorizontalDivider(thickness = 0.5.dp, color = AppColors.borderColor.copy(alpha = 0.5f))

                LinkRow(
                    icon = Icons.Rounded.People,
                    iconTint = Color(0xFF818CF8),
                    title = "ВК",
                    url = "https://vk.ru/dev_coffeecode",
                    onClick = { openUrl("https://vk.ru/dev_coffeecode") }
                )

                HorizontalDivider(thickness = 0.5.dp, color = AppColors.borderColor.copy(alpha = 0.5f))

                LinkRow(
                    icon = Icons.Rounded.Code,
                    iconTint = Color(0xFFF59E0B),
                    title = "GitHub",
                    url = "https://github.com/vktrsansara",
                    onClick = { openUrl("https://github.com/vktrsansara") }
                )

                HorizontalDivider(thickness = 0.5.dp, color = AppColors.borderColor.copy(alpha = 0.5f))

                LinkRow(
                    icon = Icons.Rounded.Terminal,
                    iconTint = Color(0xFF10B981),
                    title = "GitVerse",
                    url = "https://gitverse.ru/vktrsansara",
                    onClick = { openUrl("https://gitverse.ru/vktrsansara") }
                )
            }

            // Card 3: Additional Links
            AboutCard {
                SectionHeader(
                    icon = Icons.Rounded.Info,
                    title = "Дополнительно",
                    tint = Color(0xFF10B981)
                )

                Spacer(modifier = Modifier.height(6.dp))

                LinkRow(
                    icon = Icons.Rounded.Map,
                    iconTint = Color(0xFF84CC16),
                    title = "OpenStreetMap",
                    url = "https://www.openstreetmap.org/",
                    onClick = { openUrl("https://www.openstreetmap.org/") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LinkRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    url: String,
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
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(AppColors.bgSurface)
                .border(1.dp, AppColors.borderColor, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.textPrimary
            )
            Text(
                text = url,
                fontSize = 11.5.sp,
                color = AppColors.textSecondary,
                lineHeight = 15.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
            contentDescription = "Открыть",
            tint = AppColors.textSecondary,
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    tint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.textPrimary
        )
    }
}

@Composable
private fun AboutCard(
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.bgCard)
            .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        content()
    }
}

@Composable
private fun AboutHeader(
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.bgSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button (32x32 dp)
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
                text = "О программе",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
        }

        HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
    }
}

@Preview(name = "About Screen", showBackground = true)
@Composable
private fun AboutScreenPreview() {
    CaveViewerTheme(darkTheme = true) {
        AboutScreen(onNavigateBack = {})
    }
}
