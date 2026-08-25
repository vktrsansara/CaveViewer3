package com.vktrsansara.app.caveviewer.presentation.main.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vktrsansara.app.caveviewer.R
import com.vktrsansara.app.caveviewer.ui.theme.AppColors
import com.vktrsansara.app.caveviewer.ui.theme.CaveViewerTheme

/**
 * Placeholder component displayed on the main screen when no project is loaded.
 */
@Composable
fun NoProjectPlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bgMain)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. Start Title Illustration
            Image(
                painter = painterResource(id = R.drawable.ui_main_title_start),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.width(190.dp)
            )

            // 2. Spacer
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Title
            Text(
                text = "Нет загруженных проектов",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary,
                textAlign = TextAlign.Center
            )

            // 4. Spacer
            Spacer(modifier = Modifier.height(6.dp))

            // 5. Subtitle
            Text(
                text = "Создайте проект или импортируйте существующий",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = AppColors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.widthIn(max = 260.dp)
            )
        }
    }
}

@Preview(name = "NoProject - Dark", showBackground = true)
@Composable
private fun NoProjectPlaceholderDarkPreview() {
    CaveViewerTheme(darkTheme = true) {
        NoProjectPlaceholder()
    }
}

@Preview(name = "NoProject - Light", showBackground = true)
@Composable
private fun NoProjectPlaceholderLightPreview() {
    CaveViewerTheme(darkTheme = false) {
        NoProjectPlaceholder()
    }
}
