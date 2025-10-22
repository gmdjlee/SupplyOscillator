package com.stockoscillator.data.calculator

import com.stockoscillator.data.model.*
import kotlin.math.abs

/**
 * 수급 오실레이터 계산기
 */
object OscillatorCalculator {

    /**
     * 수급 오실레이터 계산
     *
     * 공식: (외국인5일 + 기관5일) / 시가총액 * 100
     */
    fun calculate(data: StockData): OscillatorResult {
        val dates = data.dates
        val marketCap = data.marketCap
        val oscillator = mutableListOf<Double>()

        // 1. 수급 오실레이터 계산
        for (i in data.marketCap.indices) {
            val mcap = data.marketCap[i].toDouble()
            val foreign = data.foreign5d[i].toDouble()
            val institution = data.institution5d[i].toDouble()

            val osc = if (mcap > 0) {
                ((foreign + institution) / mcap) * 100.0
            } else {
                0.0
            }

            oscillator.add(osc)
        }

        // 2. EMA 계산 (12일)
        val ema = calculateEMA(oscillator, 12)

        // 3. MACD 계산
        val ema12 = calculateEMA(oscillator, 12)
        val ema26 = calculateEMA(oscillator, 26)
        val macd = ema12.zip(ema26) { e12, e26 -> e12 - e26 }

        // 4. Signal 계산 (MACD의 9일 EMA)
        val signal = calculateEMA(macd, 9)

        // 5. Histogram 계산
        val histogram = macd.zip(signal) { m, s -> m - s }

        return OscillatorResult(
            dates = dates,
            marketCap = marketCap,  // 원본 시가총액 데이터 포함
            oscillator = oscillator,
            ema = ema,
            macd = macd,
            signal = signal,
            histogram = histogram
        )
    }

    /**
     * EMA (지수 이동 평균) 계산
     */
    private fun calculateEMA(values: List<Double>, period: Int): List<Double> {
        if (values.isEmpty()) return emptyList()

        val result = mutableListOf<Double>()
        val multiplier = 2.0 / (period + 1)

        // 첫 번째 EMA는 단순 평균
        var ema = values.take(period).average()
        result.add(ema)

        // 이후 EMA 계산
        for (i in period until values.size) {
            ema = (values[i] - ema) * multiplier + ema
            result.add(ema)
        }

        // 앞부분을 0으로 채움
        return List(period - 1) { 0.0 } + result
    }

    /**
     * 매매 신호 분석
     */
    fun analyzeSignal(result: OscillatorResult): SignalAnalysis {
        if (result.dates.isEmpty()) {
            return SignalAnalysis(
                signal = TradeSignal.NEUTRAL,
                score = 0.0,
                trend = "데이터 없음",
                foreignTrend = "알 수 없음",
                institutionTrend = "알 수 없음",
                recommendation = "데이터를 확인해주세요"
            )
        }

        // 최근 데이터
        val lastIdx = result.oscillator.size - 1
        val recentOsc = result.oscillator.takeLast(5)
        val recentMACD = result.macd.takeLast(5)
        val recentSignal = result.signal.takeLast(5)
        val recentHisto = result.histogram.takeLast(5)

        // 점수 계산 (-100 ~ +100)
        var score = 0.0

        // 1. 오실레이터 값 평가 (±40점)
        val avgOsc = recentOsc.average()
        score += when {
            avgOsc > 0.5 -> 40.0
            avgOsc > 0.2 -> 20.0
            avgOsc < -0.5 -> -40.0
            avgOsc < -0.2 -> -20.0
            else -> 0.0
        }

        // 2. MACD 골든크로스/데드크로스 (±30점)
        val macdCross = recentMACD.last() - recentSignal.last()
        val prevMacdCross = recentMACD[recentMACD.size - 2] - recentSignal[recentSignal.size - 2]

        if (macdCross > 0 && prevMacdCross <= 0) {
            score += 30.0 // 골든크로스
        } else if (macdCross < 0 && prevMacdCross >= 0) {
            score -= 30.0 // 데드크로스
        } else if (macdCross > 0) {
            score += 15.0
        } else {
            score -= 15.0
        }

        // 3. 히스토그램 추세 (±30점)
        val histoTrend = recentHisto.takeLast(3)
        if (histoTrend.all { it > 0 } && histoTrend[2] > histoTrend[0]) {
            score += 30.0 // 상승 추세
        } else if (histoTrend.all { it < 0 } && histoTrend[2] < histoTrend[0]) {
            score -= 30.0 // 하락 추세
        }

        // 신호 결정
        val signal = when {
            score >= 60 -> TradeSignal.STRONG_BUY
            score >= 20 -> TradeSignal.BUY
            score <= -60 -> TradeSignal.STRONG_SELL
            score <= -20 -> TradeSignal.SELL
            else -> TradeSignal.NEUTRAL
        }

        // 추세 설명
        val trend = when {
            avgOsc > 0.3 -> "강한 매수세"
            avgOsc > 0 -> "매수 우위"
            avgOsc < -0.3 -> "강한 매도세"
            avgOsc < 0 -> "매도 우위"
            else -> "균형"
        }

        // 외국인/기관 동향 (단순화)
        val foreignTrend = if (avgOsc > 0) "순매수" else "순매도"
        val institutionTrend = if (avgOsc > 0) "순매수" else "순매도"

        // 투자 권고
        val recommendation = when (signal) {
            TradeSignal.STRONG_BUY -> "적극 매수 검토"
            TradeSignal.BUY -> "매수 관심"
            TradeSignal.NEUTRAL -> "관망"
            TradeSignal.SELL -> "매도 검토"
            TradeSignal.STRONG_SELL -> "적극 매도 검토"
        }

        return SignalAnalysis(
            signal = signal,
            score = score.coerceIn(-100.0, 100.0),
            trend = trend,
            foreignTrend = foreignTrend,
            institutionTrend = institutionTrend,
            recommendation = recommendation
        )
    }

    /**
     * 증시 자금 동향 분석
     */
    fun analyzeMarketDeposit(data: MarketDepositData): String {
        if (data.dates.isEmpty()) return "데이터 없음"

        val recentDeposit = data.depositAmounts.takeLast(5)
        val recentCredit = data.creditAmounts.takeLast(5)

        val depositTrend = recentDeposit.last() - recentDeposit.first()
        val creditTrend = recentCredit.last() - recentCredit.first()

        return when {
            depositTrend > 0 && creditTrend > 0 -> "자금 유입 & 신용 증가 - 시장 긍정적"
            depositTrend > 0 && creditTrend < 0 -> "자금 유입 & 신용 감소 - 안정적"
            depositTrend < 0 && creditTrend > 0 -> "자금 유출 & 신용 증가 - 주의"
            depositTrend < 0 && creditTrend < 0 -> "자금 유출 & 신용 감소 - 시장 부정적"
            else -> "보합"
        }
    }
}