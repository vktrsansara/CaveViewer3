package com.vktrsansara.app.caveviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vktrsansara.app.caveviewer.domain.model.ThemeMode
import com.vktrsansara.app.caveviewer.presentation.main.MainScreen
import com.vktrsansara.app.caveviewer.presentation.main.MainViewModel
import com.vktrsansara.app.caveviewer.ui.theme.CaveViewerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

            val isDarkTheme = when (uiState.settings.theme) {
                ThemeMode.AUTO -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            LaunchedEffect(isDarkTheme, uiState.settings.isFullscreen) {
                val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
                windowInsetsController.isAppearanceLightStatusBars = !isDarkTheme
                windowInsetsController.isAppearanceLightNavigationBars = !isDarkTheme

                if (uiState.settings.isFullscreen) {
                    windowInsetsController.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                } else {
                    windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
                }
            }

            CaveViewerTheme(darkTheme = isDarkTheme) {
                MainScreen(viewModel = mainViewModel)
            }
        }
    }
}