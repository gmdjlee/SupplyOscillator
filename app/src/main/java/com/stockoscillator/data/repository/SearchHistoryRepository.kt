package com.stockoscillator.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * 검색 기록 저장소
 */
class SearchHistoryRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "search_history",
        Context.MODE_PRIVATE
    )

    private val _searchHistory = MutableStateFlow<List<SearchHistoryItem>>(emptyList())
    val searchHistory: StateFlow<List<SearchHistoryItem>> = _searchHistory.asStateFlow()

    init {
        loadHistory()
    }

    /**
     * 검색 기록 추가
     */
    fun addSearchHistory(ticker: String, name: String) {
        val currentHistory = _searchHistory.value.toMutableList()

        // 중복 제거
        currentHistory.removeAll { it.ticker == ticker }

        // 최신 항목을 맨 앞에 추가
        currentHistory.add(0, SearchHistoryItem(
            ticker = ticker,
            name = name,
            timestamp = System.currentTimeMillis()
        ))

        // 최대 20개만 유지
        if (currentHistory.size > MAX_HISTORY_SIZE) {
            currentHistory.subList(MAX_HISTORY_SIZE, currentHistory.size).clear()
        }

        _searchHistory.value = currentHistory
        saveHistory()
    }

    /**
     * 검색 기록 삭제
     */
    fun removeSearchHistory(ticker: String) {
        val currentHistory = _searchHistory.value.toMutableList()
        currentHistory.removeAll { it.ticker == ticker }
        _searchHistory.value = currentHistory
        saveHistory()
    }

    /**
     * 모든 검색 기록 삭제
     */
    fun clearAllHistory() {
        _searchHistory.value = emptyList()
        saveHistory()
    }

    /**
     * 검색 기록 로드
     */
    private fun loadHistory() {
        try {
            val json = prefs.getString(KEY_HISTORY, null) ?: return
            val jsonArray = JSONArray(json)

            val history = mutableListOf<SearchHistoryItem>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                history.add(
                    SearchHistoryItem(
                        ticker = item.getString("ticker"),
                        name = item.getString("name"),
                        timestamp = item.getLong("timestamp")
                    )
                )
            }

            _searchHistory.value = history
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 검색 기록 저장
     */
    private fun saveHistory() {
        try {
            val jsonArray = JSONArray()
            _searchHistory.value.forEach { item ->
                val jsonObject = JSONObject().apply {
                    put("ticker", item.ticker)
                    put("name", item.name)
                    put("timestamp", item.timestamp)
                }
                jsonArray.put(jsonObject)
            }

            prefs.edit()
                .putString(KEY_HISTORY, jsonArray.toString())
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val KEY_HISTORY = "history"
        private const val MAX_HISTORY_SIZE = 20
    }
}

/**
 * 검색 기록 아이템
 */
data class SearchHistoryItem(
    val ticker: String,
    val name: String,
    val timestamp: Long
)