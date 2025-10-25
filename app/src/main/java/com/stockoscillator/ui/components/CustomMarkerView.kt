package com.stockoscillator.ui.components

import android.content.Context
import android.util.Log
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.stockoscillator.R

private const val TAG = "CustomMarkerView"

/**
 * 차트 값의 타입
 */
enum class ValueType {
    CURRENCY,      // 화폐 (원, 억원, 조원)
    RATIO,         // 비율 (단위 없음)
    PERCENTAGE,    // 퍼센트 (%)
    NUMBER         // 일반 숫자
}

/**
 * 범용 마커 뷰 - 값 포맷터를 받아서 사용
 */
class CustomMarkerView(
    context: Context,
    layoutResource: Int,
    private val dates: List<String>,
    private val formatter: (Float) -> String
) : MarkerView(context, layoutResource) {

    private var tvDate: TextView? = null
    private var tvValue: TextView? = null

    init {
        try {
            tvDate = findViewById(R.id.tvDate)
            tvValue = findViewById(R.id.tvValue)

            if (tvDate == null || tvValue == null) {
                Log.e(TAG, "CustomMarkerView: TextView not found in layout")
            }
        } catch (e: Exception) {
            Log.e(TAG, "CustomMarkerView init error", e)
        }
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        try {
            if (e == null) {
                Log.w(TAG, "CustomMarkerView: Entry is null")
                return
            }

            val index = e.x.toInt()

            // 날짜 표시
            if (index >= 0 && index < dates.size) {
                tvDate?.text = dates[index]
            } else {
                tvDate?.text = "N/A"
                Log.w(TAG, "CustomMarkerView: Index out of bounds: $index, size: ${dates.size}")
            }

            // 값 표시
            try {
                tvValue?.text = formatter(e.y)
            } catch (e: Exception) {
                tvValue?.text = "Error"
                Log.e(TAG, "CustomMarkerView: Formatter error", e)
            }

            super.refreshContent(e, highlight)
        } catch (e: Exception) {
            Log.e(TAG, "CustomMarkerView refreshContent error", e)
        }
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat())
    }
}

/**
 * 시가총액 차트 전용 마커 뷰
 */
class MarketCapMarkerView(
    context: Context,
    layoutResource: Int,
    private val dates: List<String>,
    private val valueType: ValueType = ValueType.CURRENCY
) : MarkerView(context, layoutResource) {

    private var tvDate: TextView? = null
    private var tvValue: TextView? = null

    init {
        try {
            tvDate = findViewById(R.id.tvDate)
            tvValue = findViewById(R.id.tvValue)

            if (tvDate == null || tvValue == null) {
                Log.e(TAG, "MarketCapMarkerView: TextView not found in layout")
            }
        } catch (e: Exception) {
            Log.e(TAG, "MarketCapMarkerView init error", e)
        }
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        try {
            if (e == null) {
                Log.w(TAG, "MarketCapMarkerView: Entry is null")
                return
            }

            val index = e.x.toInt()

            // 날짜 표시
            if (index >= 0 && index < dates.size) {
                tvDate?.text = dates[index]
            } else {
                tvDate?.text = "N/A"
                Log.w(TAG, "MarketCapMarkerView: Index out of bounds: $index, size: ${dates.size}")
            }

            // dataSetIndex로 시가총액과 오실레이터 구분
            val value = e.y
            val formattedValue = try {
                when (highlight?.dataSetIndex) {
                    0 -> formatCurrency(value)  // 시가총액 (첫 번째 데이터셋)
                    1 -> formatRatio(value)     // 오실레이터 (두 번째 데이터셋)
                    else -> formatCurrency(value)
                }
            } catch (ex: Exception) {
                Log.e(TAG, "MarketCapMarkerView: Format error", ex)
                "Error"
            }

            tvValue?.text = formattedValue

            super.refreshContent(e, highlight)
        } catch (e: Exception) {
            Log.e(TAG, "MarketCapMarkerView refreshContent error", e)
        }
    }

    /**
     * 화폐 값 포맷팅
     */
    private fun formatCurrency(value: Float): String {
        return try {
            val absValue = Math.abs(value.toDouble())
            when {
                absValue >= 1_000_000_000_000 -> {
                    val trillion = value / 1_000_000_000_000
                    String.format("%.2f조원", trillion)
                }
                absValue >= 100_000_000 -> {
                    val hundred_million = value / 100_000_000
                    String.format("%.2f억원", hundred_million)
                }
                absValue >= 10_000 -> {
                    val tenThousand = value / 10_000
                    String.format("%.2f만원", tenThousand)
                }
                else -> {
                    String.format("%.0f원", value)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "formatCurrency error", e)
            "Error"
        }
    }

    /**
     * 비율 값 포맷팅 (단위 없음)
     */
    private fun formatRatio(value: Float): String {
        return try {
            val absValue = Math.abs(value)
            when {
                absValue < 0.001 -> String.format("%.6f", value)
                absValue < 1 -> String.format("%.4f", value)
                absValue < 100 -> String.format("%.2f", value)
                else -> String.format("%.0f", value)
            }
        } catch (e: Exception) {
            Log.e(TAG, "formatRatio error", e)
            "Error"
        }
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat())
    }
}

/**
 * MACD 차트 전용 마커 뷰 (개선된 버전)
 * marker_view.xml 사용 (tvDate, tvValue만 사용)
 */
class MacdMarkerView(
    context: Context,
    layoutResource: Int,
    private val dates: List<String>,
    private val macdValues: List<Double>,
    private val signalValues: List<Double>
) : MarkerView(context, layoutResource) {

    private var tvDate: TextView? = null
    private var tvValue: TextView? = null

    init {
        try {
            // R.id.tvDate와 R.id.tvValue 사용 (marker_view.xml과 호환)
            tvDate = findViewById(R.id.tvDate)
            tvValue = findViewById(R.id.tvValue)

            if (tvDate == null || tvValue == null) {
                Log.e(TAG, "MacdMarkerView: TextView not found. tvDate=${tvDate}, tvValue=${tvValue}")
                Log.e(TAG, "MacdMarkerView: Layout resource ID: $layoutResource")
            } else {
                Log.d(TAG, "MacdMarkerView: TextViews found successfully")
            }

            Log.d(TAG, "MacdMarkerView initialized: dates=${dates.size}, macd=${macdValues.size}, signal=${signalValues.size}")

            // 데이터 검증
            if (macdValues.isEmpty() || signalValues.isEmpty()) {
                Log.w(TAG, "MacdMarkerView: Empty data lists")
            } else if (macdValues.all { it == 0.0 } && signalValues.all { it == 0.0 }) {
                Log.w(TAG, "MacdMarkerView: All MACD and Signal values are ZERO!")
                Log.w(TAG, "MacdMarkerView: This might be a data calculation issue")
            }
        } catch (e: Exception) {
            Log.e(TAG, "MacdMarkerView init error", e)
        }
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        try {
            if (e == null) {
                Log.w(TAG, "MacdMarkerView: Entry is null")
                return
            }

            val index = e.x.toInt()

            // 디버깅 로그
            Log.d(TAG, "MacdMarkerView refreshContent: index=$index, dataSetIndex=${highlight?.dataSetIndex}, entryY=${e.y}")

            // 날짜 표시
            if (index >= 0 && index < dates.size) {
                tvDate?.text = dates[index]
            } else {
                tvDate?.text = "N/A"
                Log.w(TAG, "MacdMarkerView: Date index out of bounds: $index, size: ${dates.size}")
            }

            // MACD 값과 Signal 값 표시
            val text = try {
                if (index >= 0 && index < macdValues.size && index < signalValues.size) {
                    val macd = macdValues[index]
                    val signal = signalValues[index]

                    Log.d(TAG, "MacdMarkerView values: macd=$macd, signal=$signal")

                    // 값이 0인지 경고
                    if (macd == 0.0 && signal == 0.0) {
                        Log.w(TAG, "MacdMarkerView: Both values are ZERO at index $index")
                        Log.w(TAG, "MacdMarkerView: Check MACD calculation or API data")
                    }

                    // 여러 줄로 표시
                    "MACD: ${String.format("%.3f", macd)}\nSignal: ${String.format("%.3f", signal)}"
                } else {
                    Log.w(TAG, "MacdMarkerView: Index out of range - index=$index, macd.size=${macdValues.size}, signal.size=${signalValues.size}")
                    "N/A"
                }
            } catch (ex: Exception) {
                Log.e(TAG, "MacdMarkerView: Format error", ex)
                "Error"
            }

            tvValue?.text = text
            Log.d(TAG, "MacdMarkerView: Set text to: $text")

            super.refreshContent(e, highlight)
        } catch (e: Exception) {
            Log.e(TAG, "MacdMarkerView refreshContent error", e)
        }
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat())
    }
}