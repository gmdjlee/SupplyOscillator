package com.stockoscillator.utils

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * 포맷 유틸리티
 */
object FormatUtils {

    private val decimalFormat = DecimalFormat("#,###")
    private val percentFormat = DecimalFormat("#,##0.00")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
    private val shortDateFormat = SimpleDateFormat("MM/dd", Locale.KOREA)

    /**
     * 숫자를 천 단위 구분자로 포맷
     *
     * @param value 숫자
     * @return 포맷된 문자열 (예: "1,234,567")
     */
    fun formatNumber(value: Long): String {
        return decimalFormat.format(value)
    }

    /**
     * 숫자를 한국 화폐 단위로 포맷
     *
     * @param value 금액
     * @return 포맷된 문자열 (예: "1,234억", "567만")
     */
    fun formatKoreanCurrency(value: Long): String {
        val absValue = kotlin.math.abs(value)
        val sign = if (value >= 0) "+" else "-"

        return when {
            absValue >= 1_000_000_000_000 -> {
                val v = absValue / 1_000_000_000_000.0
                "$sign${String.format("%.1f", v)}조"
            }
            absValue >= 100_000_000 -> {
                val v = absValue / 100_000_000.0
                "$sign${String.format("%.0f", v)}억"
            }
            absValue >= 10_000 -> {
                val v = absValue / 10_000.0
                "$sign${String.format("%.0f", v)}만"
            }
            else -> {
                "$sign${formatNumber(absValue)}"
            }
        }
    }

    /**
     * 퍼센트로 포맷
     *
     * @param value 값
     * @return 포맷된 문자열 (예: "+5.23%")
     */
    fun formatPercent(value: Float): String {
        val sign = if (value >= 0) "+" else ""
        return "$sign${percentFormat.format(value)}%"
    }

    /**
     * 날짜 포맷
     *
     * @param date 날짜 문자열 (yyyy-MM-dd)
     * @return 포맷된 날짜 (yyyy-MM-dd)
     */
    fun formatDate(date: String): String {
        return try {
            val d = dateFormat.parse(date)
            dateFormat.format(d ?: Date())
        } catch (e: Exception) {
            date
        }
    }

    /**
     * 짧은 날짜 포맷
     *
     * @param date 날짜 문자열 (yyyy-MM-dd)
     * @return 포맷된 날짜 (MM/dd)
     */
    fun formatShortDate(date: String): String {
        return try {
            val d = dateFormat.parse(date)
            shortDateFormat.format(d ?: Date())
        } catch (e: Exception) {
            date.substring(5) // MM-dd 부분만
        }
    }

    /**
     * 시가총액 포맷 (단위: 억원)
     *
     * @param marketCap 시가총액
     * @return 포맷된 문자열
     */
    fun formatMarketCap(marketCap: Long): String {
        val value = marketCap / 100_000_000.0
        return when {
            value >= 10_000 -> "${String.format("%.1f", value / 10_000)}조원"
            else -> "${String.format("%.0f", value)}억원"
        }
    }

    /**
     * 오실레이터 값 색상 판단
     *
     * @param value 오실레이터 값
     * @return true: 양수(매수), false: 음수(매도)
     */
    fun isPositive(value: Float): Boolean = value >= 0

    /**
     * 매매 신호 텍스트
     *
     * @param oscillator 오실레이터 값
     * @return 신호 문자열
     */
    fun getTradeSignalText(oscillator: Float): String {
        return when {
            oscillator > 0.5f -> "강한 매수"
            oscillator > 0.0f -> "매수"
            oscillator > -0.5f -> "중립"
            else -> "매도"
        }
    }
}

/**
 * Float 확장 함수
 */
fun Float.toPercent(): String = FormatUtils.formatPercent(this)

/**
 * Long 확장 함수
 */
fun Long.toKoreanWon(): String = FormatUtils.formatKoreanCurrency(this)

fun Long.toMarketCap(): String = FormatUtils.formatMarketCap(this)

/**
 * String 확장 함수
 */
fun String.toShortDate(): String = FormatUtils.formatShortDate(this)