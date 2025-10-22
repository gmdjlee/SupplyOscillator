package com.stockoscillator.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 Shape Scale
 *
 * - Extra Small: 4dp - 작은 컴포넌트 (칩, 버튼)
 * - Small: 8dp - 카드, 다이얼로그
 * - Medium: 12dp - 시트, 탐색 서랍
 * - Large: 16dp - FAB, 큰 카드
 * - Extra Large: 28dp - 대형 컴포넌트
 */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)