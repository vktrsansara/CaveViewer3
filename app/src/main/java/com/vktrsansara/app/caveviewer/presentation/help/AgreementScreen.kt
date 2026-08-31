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
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Warning
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.ui.theme.AccentSkyBlue
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import com.vktrsansara.app.caveviewer.ui.theme.CaveViewerTheme

private val CautionRed = Color(0xFFEF4444)
private val CautionBg = Color(0xFF261214)
private val CautionBorder = Color(0xFF7F1D1D)
private val SectionTitleBlue = AccentSkyBlue

/**
 * Dedicated screen displaying the License Agreement and Terms of Use for CaveViewer.
 */
@Composable
fun AgreementScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBack)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bgMain)
    ) {
        // Standard Header
        AgreementHeader(onNavigateBack = onNavigateBack)

        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Intro Card
            AgreementCard {
                Text(
                    text = "ЛИЦЕНЗИОННОЕ СОГЛАШЕНИЕ НА ИСПОЛЬЗОВАНИЕ ПРОГРАММНОГО ОБЕСПЕЧЕНИЯ",
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Перед началом использования Программного обеспечения внимательно прочтите условия настоящего Лицензионного соглашения. Используя Программное обеспечение, Вы подтверждаете, что полностью ознакомились с текстом настоящего Соглашения, понимаете его и принимаете все его условия без каких-либо исключений. Если Вы не согласны с какими-либо условиями Соглашения, Вы не вправе использовать Программное обеспечение.",
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    color = AppColors.textSecondary
                )
            }

            // Section 1: Предмет соглашения
            AgreementCard {
                SectionHeader(
                    icon = Icons.Rounded.Description,
                    title = "1. Предмет соглашения",
                    tint = SectionTitleBlue
                )
                Spacer(modifier = Modifier.height(8.dp))

                ParagraphItem(
                    num = "1.1.",
                    text = "Настоящее Соглашение регулирует отношения между Вами, как конечным пользователем (далее — «Пользователь»), и правообладателем Программного обеспечения (далее — «Лицензиар»)."
                )
                Spacer(modifier = Modifier.height(6.dp))
                ParagraphItem(
                    num = "1.2.",
                    text = "Объектом данного Соглашения является предоставление простой (неисключительной) лицензии на использование компьютерной программы: CaveViewer (далее — «Программа»)."
                )
            }

            // Section 2: Права и ограничения
            AgreementCard {
                SectionHeader(
                    icon = Icons.Rounded.Shield,
                    title = "2. Права и ограничения",
                    tint = SectionTitleBlue
                )
                Spacer(modifier = Modifier.height(8.dp))

                ParagraphItem(
                    num = "2.1.",
                    text = "Вам предоставляется право использовать Программу на вашем устройстве в соответствии с ее основным функциональным назначением."
                )
                Spacer(modifier = Modifier.height(6.dp))
                ParagraphItem(
                    num = "2.2.",
                    text = "Любое коммерческое использование, распространение, модификация или копирование Программы без разрешения Лицензиара запрещено."
                )
            }

            // Section 3: Отказ от гарантий и ограничение ответственности (Ключевой раздел)
            KeySectionCard()

            // Section 4: Заключительные положения
            AgreementCard {
                SectionHeader(
                    icon = Icons.Rounded.Gavel,
                    title = "4. Заключительные положения",
                    tint = SectionTitleBlue
                )
                Spacer(modifier = Modifier.height(8.dp))

                ParagraphItem(
                    num = "4.1.",
                    text = "Настоящее Соглашение вступает в силу с момента начала использования Программы Пользователем."
                )
                Spacer(modifier = Modifier.height(6.dp))
                ParagraphItem(
                    num = "4.2.",
                    text = "Лицензиар оставляет за собой право в одностороннем порядке изменять условия настоящего Соглашения с обязательной публикацией новой редакции в Программе."
                )
            }

            // Separate Block: MIT License
            MitLicenseBlock()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun KeySectionCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CautionBg)
            .border(width = 1.5.dp, color = CautionBorder, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = CautionRed,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "3. Отказ от гарантий и ограничение ответственности (Ключевой раздел)",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = CautionRed,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(thickness = 1.dp, color = CautionBorder)
        Spacer(modifier = Modifier.height(10.dp))

        // 3.1
        ParagraphItem(
            num = "3.1.",
            text = "Программа предоставляется на условиях «как есть» (AS IS). Лицензиар не предоставляет никаких гарантий того, что Программа будет соответствовать вашим ожиданиям, работать бесперебойно и без ошибок.",
            numColor = CautionRed,
            textColor = Color(0xFFFECACA)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 3.2
        ParagraphItem(
            num = "3.2.",
            text = "Программа не является системой навигации или средством обеспечения безопасности. Ее основное функциональное назначение — служить цифровым блокнотом (записной книжкой) для просмотра приблизительных топографических материалов и ведения пользовательских заметок.",
            numColor = CautionRed,
            textColor = Color(0xFFFECACA),
            isBoldText = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 3.3
        ParagraphItem(
            num = "3.3.",
            text = "Картографические материалы, включая план Гурьевских каменоломен, являются схематичными, приблизительными и могут содержать ошибки. Пользователь осознает и принимает на себя весь риск, связанный с использованием данных материалов. План не является гарантией безопасности и не учитывает все реальные опасности и изменения в пещерах.",
            numColor = CautionRed,
            textColor = Color(0xFFFECACA)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 3.4
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "3.4.",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = CautionRed,
                    modifier = Modifier.width(32.dp)
                )
                Text(
                    text = "Программа нацелена на целевую аудиторию, имеющую:",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 17.sp,
                    color = Color(0xFFFECACA)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            BulletItem(
                text = "Опыт работы с топографическими материалами (картами, схемами, планами).",
                textColor = Color(0xFFFCA5A5)
            )
            BulletItem(
                text = "Достаточный и подтвержденный опыт ориентирования в сложных подземных условиях, в частности, в Гурьевских каменоломнях.",
                textColor = Color(0xFFFCA5A5)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // 3.5
        ParagraphItem(
            num = "3.5.",
            text = "Лицензиар не несет ответственности за любые прямые или косвенные последствия, которые могут возникнуть в результате использования или невозможности использования Программы, включая, но не ограничиваясь: травмы, ущерб здоровью, материальный ущерб, упущенную выгоду, полученную в результате посещения пещер и каменоломен.",
            numColor = CautionRed,
            textColor = Color(0xFFFECACA)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 3.6
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "3.6.",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = CautionRed,
                    modifier = Modifier.width(32.dp)
                )
                Text(
                    text = "Пользователь обязуется:",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 17.sp,
                    color = Color(0xFFFECACA)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            BulletItem(
                text = "Полностью осознавать все риски, связанные с ориентированием в пещерах.",
                textColor = Color(0xFFFCA5A5)
            )
            BulletItem(
                text = "Использовать Программу только как вспомогательный инструмент в комплексе с другими проверенными методами навигации и средствами безопасности.",
                textColor = Color(0xFFFCA5A5)
            )
            BulletItem(
                text = "Нести полную личную ответственность за свои решения и действия.",
                textColor = Color(0xFFFCA5A5)
            )
        }
    }
}

@Composable
private fun MitLicenseBlock() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.bgCard)
            .border(width = 1.dp, color = AppColors.borderColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        SectionHeader(
            icon = Icons.Rounded.Code,
            title = "The MIT License (MIT)",
            tint = AccentSkyBlue
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Copyright © 2026 <copyright holders>",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = AccentSkyBlue
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:\n\n" +
                    "The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.\n\n" +
                    "THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.",
            fontSize = 11.5.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 16.sp,
            color = AppColors.textSecondary
        )
    }
}

@Composable
private fun ParagraphItem(
    num: String,
    text: String,
    numColor: Color = AccentSkyBlue,
    textColor: Color = AppColors.textSecondary,
    isBoldText: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = num,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = numColor,
            modifier = Modifier.width(32.dp)
        )
        Text(
            text = text,
            fontSize = 12.5.sp,
            fontWeight = if (isBoldText) FontWeight.SemiBold else FontWeight.Normal,
            lineHeight = 18.sp,
            color = textColor
        )
    }
}

@Composable
private fun BulletItem(
    text: String,
    textColor: Color = AppColors.textSecondary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = CautionRed,
            modifier = Modifier.width(14.dp)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = textColor
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
private fun AgreementCard(
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
private fun AgreementHeader(
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
                text = "Лицензионное соглашение",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
        }

        HorizontalDivider(thickness = 1.dp, color = AppColors.borderColor)
    }
}

@Preview(name = "Agreement Screen", showBackground = true)
@Composable
private fun AgreementScreenPreview() {
    CaveViewerTheme(darkTheme = true) {
        AgreementScreen(onNavigateBack = {})
    }
}
