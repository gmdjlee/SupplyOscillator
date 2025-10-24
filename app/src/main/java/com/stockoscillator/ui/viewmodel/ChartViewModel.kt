package com.stockoscillator.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stockoscillator.data.calculator.OscillatorCalculator
import com.stockoscillator.data.model.OscillatorResult
import com.stockoscillator.data.model.SignalAnalysis
import com.stockoscillator.data.model.UiState
import com.stockoscillator.data.repository.SearchHistoryRepository
import com.stockoscillator.data.repository.StockRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 차트 분석 ViewModel (검색 기록 + 자동완성 + 날짜 표시)
 */
class ChartViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StockRepository(application.applicationContext)
    private val historyRepository = SearchHistoryRepository(application.applicationContext)

    // UI 상태
    private val _uiState = MutableStateFlow<UiState<OscillatorResult>>(UiState.Idle)
    val uiState: StateFlow<UiState<OscillatorResult>> = _uiState.asStateFlow()

    // 종목 정보
    private val _stockInfo = MutableStateFlow<Pair<String, String>?>(null)
    val stockInfo: StateFlow<Pair<String, String>?> = _stockInfo.asStateFlow()

    // 매매 신호 분석 결과
    private val _analysisResult = MutableStateFlow<SignalAnalysis?>(null)
    val analysisResult: StateFlow<SignalAnalysis?> = _analysisResult.asStateFlow()

    // 최신 데이터 날짜
    private val _latestDataDate = MutableStateFlow<String?>(null)
    val latestDataDate: StateFlow<String?> = _latestDataDate.asStateFlow()

    // 검색 기록
    val searchHistory = historyRepository.searchHistory

    // 자동완성 제안
    private val _autocompleteSuggestions = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val autocompleteSuggestions: StateFlow<List<Pair<String, String>>> = _autocompleteSuggestions.asStateFlow()

    private var autocompleteJob: Job? = null

    /**
     * 자동완성 검색
     */
    fun searchAutocomplete(query: String) {
        // 이전 작업 취소
        autocompleteJob?.cancel()

        if (query.isEmpty()) {
            _autocompleteSuggestions.value = emptyList()
            return
        }

        // 디바운싱: 300ms 후에 검색
        autocompleteJob = viewModelScope.launch {
            delay(300)
            val suggestions = repository.searchStocksForAutocomplete(query)
            _autocompleteSuggestions.value = suggestions
        }
    }

    /**
     * 종목 검색 및 분석
     */
    fun analyzeStock(query: String, days: Int = 180) {
        if (query.isBlank()) {
            _uiState.value = UiState.Error("종목명을 입력해주세요")
            return
        }

        // 자동완성 제안 초기화
        _autocompleteSuggestions.value = emptyList()

        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                _stockInfo.value = null
                _analysisResult.value = null
                _latestDataDate.value = null

                // 1. 종목 검색
                val stockInfo = repository.searchStock(query)
                if (stockInfo == null) {
                    _uiState.value = UiState.Error("'$query' 종목을 찾을 수 없습니다")
                    return@launch
                }

                _stockInfo.value = stockInfo

                // 검색 기록 추가
                historyRepository.addSearchHistory(stockInfo.first, stockInfo.second)

                // 2. 데이터 수집
                val stockData = repository.getStockData(stockInfo.first, days)
                if (stockData == null) {
                    _uiState.value = UiState.Error("데이터를 불러올 수 없습니다")
                    return@launch
                }

                if (stockData.dates.isEmpty()) {
                    _uiState.value = UiState.Error("수집된 데이터가 없습니다")
                    return@launch
                }

                // 최신 데이터 날짜 저장
                _latestDataDate.value = stockData.dates.lastOrNull()

                // 3. 오실레이터 계산
                val result = OscillatorCalculator.calculate(stockData)

                // 4. 매매 신호 분석
                val analysis = OscillatorCalculator.analyzeSignal(result)
                _analysisResult.value = analysis

                // 5. UI 업데이트
                _uiState.value = UiState.Success(result)

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = UiState.Error(
                    message = "분석 중 오류가 발생했습니다: ${e.message}",
                    exception = e
                )
                _stockInfo.value = null
                _analysisResult.value = null
                _latestDataDate.value = null
            }
        }
    }

    /**
     * 검색 기록에서 항목 선택
     */
    fun selectFromHistory(ticker: String, name: String) {
        analyzeStock(name)
    }

    /**
     * 검색 기록 삭제
     */
    fun removeSearchHistory(ticker: String) {
        historyRepository.removeSearchHistory(ticker)
    }

    /**
     * 모든 검색 기록 삭제
     */
    fun clearAllHistory() {
        historyRepository.clearAllHistory()
    }

    /**
     * 상태 초기화
     */
    fun reset() {
        _uiState.value = UiState.Idle
        _stockInfo.value = null
        _analysisResult.value = null
        _latestDataDate.value = null
        _autocompleteSuggestions.value = emptyList()
    }
}