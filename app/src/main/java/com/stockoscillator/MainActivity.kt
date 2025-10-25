package com.stockoscillator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.stockoscillator.ui.screens.MainScreen
import com.stockoscillator.ui.theme.StockOscillatorTheme
import com.stockoscillator.viewmodel.SettingsViewModel

/**
 * 메인 액티비티
 *
 * 앱의 진입점이며 테마를 설정합니다.
 * 설정 화면에서 변경한 테마 설정이 즉시 반영됩니다.
 */
class MainActivity : ComponentActivity() {

    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-Edge 모드 활성화
        enableEdgeToEdge()

        // SettingsViewModel 초기화
        settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]

        setContent {
            // 설정 값을 실시간으로 감지
            val dynamicColor by settingsViewModel.dynamicColor.collectAsState()
            val darkMode by settingsViewModel.darkMode.collectAsState()

            // 테마 적용
            StockOscillatorTheme(
                darkTheme = darkMode,
                dynamicColor = dynamicColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = settingsViewModel)
                }
            }
        }
    }
}