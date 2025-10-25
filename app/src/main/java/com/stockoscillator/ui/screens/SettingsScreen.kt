package com.stockoscillator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stockoscillator.viewmodel.SettingsViewModel

/**
 * 설정 화면
 *
 * 기능:
 * - 동적 색상 사용 토글 (실제 적용됨)
 * - 다크 모드 토글 (실제 적용됨)
 * - 알림 설정
 * - 앱 정보 표시
 *
 * ✅ 개선사항 (1단계):
 * - ViewModel을 파라미터로 받아서 사용 (기본값 제거)
 * - MainScreen에서 전달받은 단일 인스턴스 활용
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel // ✅ 개선: 파라미터로만 받음 (기본값 제거)
) {
    val dynamicColorEnabled by viewModel.dynamicColor.collectAsState()
    val darkModeEnabled by viewModel.darkMode.collectAsState()
    val notificationEnabled by viewModel.notification.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 헤더
        Text(
            text = "설정",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // 테마 설정
        Text(
            text = "테마",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SettingSwitch(
                    icon = Icons.Default.Palette,
                    title = "동적 색상",
                    description = "시스템 배경화면에 맞춰 색상 변경 (Android 12+)",
                    checked = dynamicColorEnabled,
                    onCheckedChange = { viewModel.setDynamicColor(it) }
                )

                HorizontalDivider()

                SettingSwitch(
                    icon = Icons.Default.DarkMode,
                    title = "다크 모드",
                    description = "어두운 테마 사용",
                    checked = darkModeEnabled,
                    onCheckedChange = { viewModel.setDarkMode(it) }
                )
            }
        }

        // 알림 설정
        Text(
            text = "알림",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                SettingSwitch(
                    icon = Icons.Default.Notifications,
                    title = "알림 받기",
                    description = "중요한 업데이트 알림",
                    checked = notificationEnabled,
                    onCheckedChange = { viewModel.setNotification(it) }
                )
            }
        }

        // 앱 정보
        Text(
            text = "앱 정보",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingInfo(
                    icon = Icons.Default.Info,
                    title = "버전",
                    value = "1.0.0"
                )

                HorizontalDivider()

                SettingInfo(
                    icon = Icons.Default.Build,
                    title = "빌드",
                    value = "2025.10.25"
                )
            }
        }

        // Phase 2 안내
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = "더 많은 설정이 준비 중입니다",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Phase 2에서 추가될 예정",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * 설정 스위치 컴포넌트
 */
@Composable
private fun SettingSwitch(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * 설정 정보 컴포넌트
 */
@Composable
private fun SettingInfo(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}