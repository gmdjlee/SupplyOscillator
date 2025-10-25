package com.stockoscillator.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stockoscillator.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 설정 화면 ViewModel
 * 앱 설정을 관리하고 저장
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    /**
     * 동적 색상 사용 여부
     */
    val dynamicColor: StateFlow<Boolean> = repository.dynamicColorFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    /**
     * 다크 모드 사용 여부
     */
    val darkMode: StateFlow<Boolean> = repository.darkModeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    /**
     * 알림 사용 여부
     */
    val notification: StateFlow<Boolean> = repository.notificationFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    /**
     * 동적 색상 설정 변경
     */
    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDynamicColor(enabled)
        }
    }

    /**
     * 다크 모드 설정 변경
     */
    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDarkMode(enabled)
        }
    }

    /**
     * 알림 설정 변경
     */
    fun setNotification(enabled: Boolean) {
        viewModelScope.launch {
            repository.setNotification(enabled)
        }
    }
}