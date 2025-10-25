package com.stockoscillator.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 앱 설정 저장소
 * DataStore를 사용하여 설정을 안전하게 저장하고 관리
 */
class SettingsRepository(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

        private val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val NOTIFICATION_KEY = booleanPreferencesKey("notification")
    }

    /**
     * 동적 색상 사용 여부 Flow
     */
    val dynamicColorFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DYNAMIC_COLOR_KEY] ?: true // 기본값: true
    }

    /**
     * 다크 모드 사용 여부 Flow
     */
    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false // 기본값: 시스템 설정 따름
    }

    /**
     * 알림 사용 여부 Flow
     */
    val notificationFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_KEY] ?: true // 기본값: true
    }

    /**
     * 동적 색상 설정 저장
     */
    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = enabled
        }
    }

    /**
     * 다크 모드 설정 저장
     */
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    /**
     * 알림 설정 저장
     */
    suspend fun setNotification(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_KEY] = enabled
        }
    }
}