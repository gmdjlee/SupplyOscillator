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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 차트 분석 ViewModel (검색 기록 + 자동완성 + 날짜 표시)
 *
 * ✅ 개선사항 (4단계):
 * - Flow 기반 자동완성 디바운싱으로 변경
 * - 이전: Job 취소 방식 (비효율적)
 * - 개선: Flow 연산자 활용 (debounce, distinctUntilChanged, mapLatest)
 * - 성능 향상 및 메모리 효율성 개선
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

    // ✅ 개선: Flow 기반 검색 쿼리
    private val searchQueryFlow = MutableStateFlow("")

    // ✅ 개선: Flow 연산자로 자동완성 처리
    private val _autocompleteSuggestions = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val autocompleteSuggestions: StateFlow<List<Pair<String, String>>> = _autocompleteSuggestions.asStateFlow()

    init {
        // ✅ 개선: Flow 기반 자동완성 처리
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300) // 300ms 디바운싱
                .filter { it.isNotEmpty() } // 빈 문자열 필터링
                .distinctUntilChanged() // 중복 쿼리 제거
                .mapLatest { query ->
                    // 백그라운드 스레드에서 검색 실행
                    repository.searchStocksForAutocomplete(query)
                }
                .catch { exception ->
                    // 에러 발생 시 빈 리스트 반환
                    android.util.Log.e("ChartViewModel", "자동완성 검색 실패", exception)
                    emit(emptyList())
                }
                .collect { suggestions ->
                    _autocompleteSuggestions.value = suggestions
                }
        }
    }

    /**
     * 자동완성 검색
     *
     * ✅ 개선: Flow에 쿼리만 전달 (디바운싱은 Flow가 처리)
     */
    fun searchAutocomplete(query: String) {
        if (query.isEmpty()) {
            _autocompleteSuggestions.value = emptyList()
            searchQueryFlow.value = ""
        } else {
            searchQueryFlow.value = query
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
        searchQueryFlow.value = ""

        viewModelScope.launch(Dispatchers.IO) {
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
                android.util.Log.e("ChartViewModel", "분석 중 오류", e)
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
        searchQueryFlow.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        // Flow는 자동으로 취소되므로 별도 정리 불필요
        android.util.Log.d("ChartViewModel", "ViewModel cleared")
    }
}