package com.stockoscillator.navigation

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.stockoscillator.ui.screens.AnalysisScreen
import com.stockoscillator.ui.screens.ChartScreen
import com.stockoscillator.ui.screens.HomeScreen
import com.stockoscillator.ui.screens.SettingsScreen
import com.stockoscillator.viewmodel.SettingsViewModel

/**
 * 앱 네비게이션 설정
 *
 * ✅ 개선사항 (1단계):
 * - SettingsViewModel을 파라미터로 받아 SettingsScreen에 전달
 * - 단일 ViewModel 인스턴스 공유
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel, // ✅ 개선: ViewModel 파라미터 추가
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val windowSizeClass = calculateWindowSizeClass(context as android.app.Activity)
    val isExpandedScreen = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.Chart.route) {
            ChartScreen()
        }

        composable(Screen.Analysis.route) {
            AnalysisScreen()
        }

        composable(Screen.Settings.route) {
            // ✅ 개선: MainScreen에서 전달받은 ViewModel 사용
            SettingsScreen(viewModel = settingsViewModel)
        }
    }
}