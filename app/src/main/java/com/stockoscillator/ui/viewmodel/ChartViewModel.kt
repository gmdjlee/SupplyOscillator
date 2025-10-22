package com.stockoscillator.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stockoscillator.data.calculator.OscillatorCalculator
import com.stockoscillator.data.model.OscillatorResult
import com.stockoscillator.data.model.SignalAnalysis
import com.stockoscillator.data.model.UiState
import com.stockoscillator.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 차트 분석 ViewModel
 */
class ChartViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StockRepository(application.applicationContext)

    // UI 상태
    private val _uiState = MutableStateFlow<UiState<OscillatorResult>>(UiState.Idle)
    val uiState: StateFlow<UiState<OscillatorResult>> = _uiState.asStateFlow()

    // 종목 정보
    private val _stockInfo = MutableStateFlow<Pair<String, String>?>(null)
    val stockInfo: StateFlow<Pair<String, String>?> = _stockInfo.asStateFlow()

    // 매매 신호 분석 결과
    private val _analysisResult = MutableStateFlow<SignalAnalysis?>(null)
    val analysisResult: StateFlow<SignalAnalysis?> = _analysisResult.asStateFlow()

    /**
     * 종목 검색 및 분석
     */
    fun analyzeStock(query: String, days: Int = 180) {
        if (query.isBlank()) {
            _uiState.value = UiState.Error("종목명을 입력해주세요")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                _stockInfo.value = null
                _analysisResult.value = null

                // 1. 종목 검색
                val stockInfo = repository.searchStock(query)
                if (stockInfo == null) {
                    _uiState.value = UiState.Error("'$query' 종목을 찾을 수 없습니다")
                    return@launch
                }

                _stockInfo.value = stockInfo

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
            }
        }
    }

    /**
     * 상태 초기화
     */
    fun reset() {
        _uiState.value = UiState.Idle
        _stockInfo.value = null
        _analysisResult.value = null
    }
}