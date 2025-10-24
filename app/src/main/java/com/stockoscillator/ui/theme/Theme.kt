package com.stockoscillator.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/**
 * 다크 테마 색상 스킴 (민트 그린 톤)
 */
private val DarkColorScheme = darkColorScheme(
    primary = DarkMintGreen60,
    onPrimary = Color(0xFF003300),
    primaryContainer = DarkMintGreen30,
    onPrimaryContainer = DarkMintGreen80,

    secondary = TealAccent60,
    onSecondary = Color(0xFF003D33),
    secondaryContainer = TealAccent40,
    onSecondaryContainer = TealAccent80,

    tertiary = MintGreen50,
    onTertiary = Color(0xFF002200),
    tertiaryContainer = MintGreen30,
    onTertiaryContainer = MintGreen80,

    error = ErrorRed40,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = ErrorRed80,

    background = BackgroundDark,
    onBackground = Color(0xFFE6E6E6),
    surface = SurfaceDark,
    onSurface = Color(0xFFE6E6E6),
    surfaceVariant = Color(0xFF3D3D3D),
    onSurfaceVariant = Color(0xFFC8C8C8),

    outline = NeutralGray60,
    outlineVariant = NeutralGray80
)

/**
 * 라이트 테마 색상 스킴 (민트 그린 톤)
 */
private val LightColorScheme = lightColorScheme(
    primary = MintGreen60,
    onPrimary = Color.White,
    primaryContainer = MintGreen10,
    onPrimaryContainer = MintGreen90,

    secondary = TealAccent60,
    onSecondary = Color.White,
    secondaryContainer = TealAccent10,
    onSecondaryContainer = TealAccent80,

    tertiary = MintGreen50,
    onTertiary = Color.White,
    tertiaryContainer = MintGreen20,
    onTertiaryContainer = MintGreen80,

    error = ErrorRed40,
    onError = Color.White,
    errorContainer = ErrorRed80,
    onErrorContainer = Color(0xFF410002),

    background = BackgroundLight,
    onBackground = Color(0xFF1C1B1F),
    surface = SurfaceLight,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = NeutralGray10,
    onSurfaceVariant = NeutralGray80,

    outline = NeutralGray60,
    outlineVariant = NeutralGray30
)

@Composable
fun StockOscillatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // 민트 톤을 유지하기 위해 기본값을 false로 변경
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // System UI Controller for edge-to-edge
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = !darkTheme
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}