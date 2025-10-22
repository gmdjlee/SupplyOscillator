package com.stockoscillator.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : Screen(
        route = "home",
        title = "홈",
        icon = Icons.Default.Home
    )

    data object Chart : Screen(
        route = "chart",
        title = "차트 분석",
        icon = Icons.Default.TrendingUp
    )

    data object Analysis : Screen(
        route = "analysis",
        title = "수급 분석",
        icon = Icons.Default.Analytics
    )

    data object Settings : Screen(
        route = "settings",
        title = "설정",
        icon = Icons.Default.Settings
    )
}