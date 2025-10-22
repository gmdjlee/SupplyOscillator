package com.stockoscillator.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stockoscillator.data.calculator.OscillatorCalculator
import com.stockoscillator.data.model.MarketDepositData
import com.stockoscillator.data.model.StockData
import com.stockoscillator.data.model.UiState
import com.stockoscillator.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 투자자별 수급 데이터
 */
data class InvestorData(
    val name: String,
    val netBuying: Long,
    val trend: String,
    val isPositive: Boolean
)

/**
 * 수급 분석 ViewModel
 */
class AnalysisViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StockRepository(application.applicationContext)

    // 종목별 투자자 수급 분석 상태
    private val _investorUiState = MutableStateFlow<UiState<List<InvestorData>>>(UiState.Idle)
    val investorUiState: StateFlow<UiState<List<InvestorData>>> = _investorUiState.asStateFlow()

    // 증시 자금 동향 분석 상태
    private val _marketUiState = MutableStateFlow<UiState<MarketDepositData>>(UiState.Idle)
    val marketUiState: StateFlow<UiState<MarketDepositData>> = _marketUiState.asStateFlow()

    // 증시 분석 요약
    private val _marketAnalysis = MutableStateFlow<String>("")
    val marketAnalysis: StateFlow<String> = _marketAnalysis.asStateFlow()

    // 종목 정보
    private val _stockInfo = MutableStateFlow<Pair<String, String>?>(null)
    val stockInfo: StateFlow<Pair<String, String>?> = _stockInfo.asStateFlow()

    /**
     * 종목의 투자자별 수급 분석
     */
    fun analyzeInvestors(query: String, days: Int = 180) {
        if (query.isBlank()) {
            _investorUiState.value = UiState.Error("종목명을 입력해주세요")
            return
        }

        viewModelScope.launch {
            try {
                _investorUiState.value = UiState.Loading
                _stockInfo.value = null

                // 1. 종목 검색
                val stockInfo = repository.searchStock(query)
                if (stockInfo == null) {
                    _investorUiState.value = UiState.Error("'$query' 종목을 찾을 수 없습니다")
                    return@launch
                }

                _stockInfo.value = stockInfo

                // 2. 데이터 수집
                val stockData = repository.getStockData(stockInfo.first, days)
                if (stockData == null) {
                    _investorUiState.value = UiState.Error("데이터를 불러올 수 없습니다")
                    return@launch
                }

                if (stockData.dates.isEmpty()) {
                    _investorUiState.value = UiState.Error("수집된 데이터가 없습니다")
                    return@launch
                }

                // 3. 투자자별 수급 계산
                val investorList = calculateInvestorData(stockData)

                // 4. UI 업데이트
                _investorUiState.value = UiState.Success(investorList)

            } catch (e: Exception) {
                e.printStackTrace()
                _investorUiState.value = UiState.Error(
                    message = "분석 중 오류가 발생했습니다: ${e.message}",
                    exception = e
                )
                _stockInfo.value = null
            }
        }
    }

    /**
     * 증시 자금 동향 분석
     */
    fun analyzeMarket(numPages: Int = 3) {
        viewModelScope.launch {
            try {
                android.util.Log.d("AnalysisViewModel", "증시 자금 동향 분석 시작")

                _marketUiState.value = UiState.Loading
                _marketAnalysis.value = ""

                // 증시 자금 동향 데이터 수집 (페이지 수 줄임)
                val marketData = repository.getMarketDepositData(numPages)

                android.util.Log.d("AnalysisViewModel", "데이터 수집 결과: ${marketData != null}")

                if (marketData == null) {
                    android.util.Log.e("AnalysisViewModel", "증시 데이터가 null입니다")
                    _marketUiState.value = UiState.Error("증시 데이터를 불러올 수 없습니다. 네트워크 연결을 확인하거나 나중에 다시 시도해주세요.")
                    return@launch
                }

                if (marketData.dates.isEmpty()) {
                    android.util.Log.e("AnalysisViewModel", "수집된 데이터가 비어있습니다")
                    _marketUiState.value = UiState.Error("수집된 데이터가 없습니다")
                    return@launch
                }

                android.util.Log.d("AnalysisViewModel", "데이터 개수: ${marketData.dates.size}")

                // 분석 수행
                val analysis = OscillatorCalculator.analyzeMarketDeposit(marketData)
                _marketAnalysis.value = analysis

                android.util.Log.d("AnalysisViewModel", "분석 완료: $analysis")

                // UI 업데이트
                _marketUiState.value = UiState.Success(marketData)

            } catch (e: Exception) {
                android.util.Log.e("AnalysisViewModel", "증시 분석 중 오류", e)
                e.printStackTrace()
                _marketUiState.value = UiState.Error(
                    message = "분석 중 오류가 발생했습니다: ${e.message}",
                    exception = e
                )
                _marketAnalysis.value = ""
            }
        }
    }

    /**
     * 투자자별 수급 데이터 계산
     */
    private fun calculateInvestorData(stockData: StockData): List<InvestorData> {
        // 최근 5일 평균 계산
        val recentForeign = stockData.foreign5d.takeLast(5).average().toLong()
        val recentInstitution = stockData.institution5d.takeLast(5).average().toLong()
        val recentIndividual = -(recentForeign + recentInstitution) // 개인 = -(외국인 + 기관)

        // 추세 판단
        val foreignTrend = when {
            recentForeign > 1_000_000_000 -> "강한 순매수"
            recentForeign > 0 -> "순매수"
            recentForeign < -1_000_000_000 -> "강한 순매도"
            recentForeign < 0 -> "순매도"
            else -> "보합"
        }

        val institutionTrend = when {
            recentInstitution > 1_000_000_000 -> "강한 순매수"
            recentInstitution > 0 -> "순매수"
            recentInstitution < -1_000_000_000 -> "강한 순매도"
            recentInstitution < 0 -> "순매도"
            else -> "보합"
        }

        val individualTrend = when {
            recentIndividual > 1_000_000_000 -> "강한 순매수"
            recentIndividual > 0 -> "순매수"
            recentIndividual < -1_000_000_000 -> "강한 순매도"
            recentIndividual < 0 -> "순매도"
            else -> "보합"
        }

        return listOf(
            InvestorData(
                name = "외국인",
                netBuying = recentForeign,
                trend = foreignTrend,
                isPositive = recentForeign > 0
            ),
            InvestorData(
                name = "기관",
                netBuying = recentInstitution,
                trend = institutionTrend,
                isPositive = recentInstitution > 0
            ),
            InvestorData(
                name = "개인",
                netBuying = recentIndividual,
                trend = individualTrend,
                isPositive = recentIndividual > 0
            )
        )
    }

    /**
     * 상태 초기화
     */
    fun reset() {
        _investorUiState.value = UiState.Idle
        _marketUiState.value = UiState.Idle
        _stockInfo.value = null
        _marketAnalysis.value = ""
    }
}