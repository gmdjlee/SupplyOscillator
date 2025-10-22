package com.stockoscillator.ui.components

import android.graphics.Color
import android.graphics.DashPathEffect
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.stockoscillator.data.model.MarketDepositData
import com.stockoscillator.data.model.OscillatorResult

/**
 * 시가총액 + 수급 오실레이터 복합 차트 (Double Y-Axis)
 */
@Composable
fun MarketCapOscillatorChart(
    result: OscillatorResult,
    marketCap: List<Long>,
    modifier: Modifier = Modifier
) {
    ChartCard(
        title = "시가총액 & 수급 오실레이터",
        modifier = modifier
    ) {
        AndroidView(
            factory = { context ->
                CombinedChart(context).apply {
                    description.isEnabled = false
                    setTouchEnabled(true)
                    isDragEnabled = true
                    setScaleEnabled(true)
                    setPinchZoom(true)
                    setDrawGridBackground(false)
                    setDrawOrder(arrayOf(
                        CombinedChart.DrawOrder.LINE,
                        CombinedChart.DrawOrder.LINE
                    ))

                    // X축 설정
                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        setDrawGridLines(true)
                        gridLineWidth = 1f
                        enableGridDashedLine(10f, 5f, 0f)
                        granularity = 1f
                        labelRotationAngle = -45f
                        setLabelCount(10, false)
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                val index = value.toInt()
                                return if (index >= 0 && index < result.dates.size) {
                                    result.dates[index]
                                } else {
                                    ""
                                }
                            }
                        }
                    }

                    // 왼쪽 Y축 (시가총액 - 억원 단위)
                    axisLeft.apply {
                        setDrawGridLines(true)
                        gridLineWidth = 1f
                        enableGridDashedLine(10f, 5f, 0f)
                        textColor = Color.parseColor("#6200EE")
                        setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                val billions = (value / 100_000_000).toInt()
                                return when {
                                    billions >= 10000 -> "${billions / 10000}조"
                                    billions >= 1000 -> String.format("%.1f조", billions / 10000f)
                                    else -> "${billions}억"
                                }
                            }
                        }
                    }

                    // 오른쪽 Y축 (오실레이터)
                    axisRight.apply {
                        isEnabled = true
                        setDrawGridLines(false)
                        textColor = Color.parseColor("#03DAC5")
                        setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                    }

                    legend.apply {
                        isEnabled = true
                        textSize = 12f
                    }
                }
            },
            update = { chart ->
                // 시가총액 라인 (왼쪽 Y축)
                val marketCapEntries = marketCap.mapIndexed { index, value ->
                    Entry(index.toFloat(), value.toFloat())
                }
                val marketCapDataSet = LineDataSet(marketCapEntries, "시가총액").apply {
                    axisDependency = YAxis.AxisDependency.LEFT
                    color = Color.parseColor("#6200EE")
                    lineWidth = 2.5f
                    setCircleColor(Color.parseColor("#6200EE"))
                    circleRadius = 2f
                    setDrawCircleHole(false)
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                }

                // 오실레이터 라인 (오른쪽 Y축)
                val oscEntries = result.oscillator.mapIndexed { index, value ->
                    Entry(index.toFloat(), value.toFloat())
                }
                val oscDataSet = LineDataSet(oscEntries, "오실레이터").apply {
                    axisDependency = YAxis.AxisDependency.RIGHT
                    color = Color.parseColor("#03DAC5")
                    lineWidth = 2.5f
                    setCircleColor(Color.parseColor("#03DAC5"))
                    circleRadius = 2f
                    setDrawCircleHole(false)
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                }

                val lineData = LineData(marketCapDataSet, oscDataSet)

                val combinedData = CombinedData().apply {
                    setData(lineData)
                }

                chart.data = combinedData
                chart.invalidate()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
        )
    }
}

/**
 * MACD 차트
 */
@Composable
fun MacdChart(
    result: OscillatorResult,
    modifier: Modifier = Modifier
) {
    ChartCard(
        title = "MACD",
        modifier = modifier
    ) {
        AndroidView(
            factory = { context ->
                CombinedChart(context).apply {
                    description.isEnabled = false
                    setTouchEnabled(true)
                    isDragEnabled = true
                    setScaleEnabled(true)
                    setPinchZoom(true)
                    setDrawGridBackground(false)
                    setDrawOrder(arrayOf(
                        CombinedChart.DrawOrder.BAR,
                        CombinedChart.DrawOrder.LINE
                    ))

                    // X축 설정
                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        setDrawGridLines(true)
                        gridLineWidth = 1f
                        enableGridDashedLine(10f, 5f, 0f)
                        granularity = 1f
                        labelRotationAngle = -45f
                        setLabelCount(10, false)
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                val index = value.toInt()
                                return if (index >= 0 && index < result.dates.size) {
                                    result.dates[index]
                                } else {
                                    ""
                                }
                            }
                        }
                    }

                    // Y축 설정
                    axisLeft.apply {
                        setDrawGridLines(true)
                        gridLineWidth = 1f
                        enableGridDashedLine(10f, 5f, 0f)
                        setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                    }
                    axisRight.isEnabled = false

                    legend.apply {
                        isEnabled = true
                        textSize = 12f
                    }
                }
            },
            update = { chart ->
                // Histogram (Bar) - 범례에 표시 안 함
                val barEntries = result.histogram.mapIndexed { index, value ->
                    BarEntry(index.toFloat(), value.toFloat())
                }
                val barDataSet = BarDataSet(barEntries, "").apply {  // 빈 라벨로 범례에서 제외
                    colors = result.histogram.map { value ->
                        if (value >= 0) Color.parseColor("#03DAC5")
                        else Color.parseColor("#CF6679")
                    }
                    setDrawValues(false)
                    // 범례에서 이 데이터셋 제외
                    isHighlightEnabled = false
                }
                val barData = BarData(barDataSet).apply {
                    barWidth = 0.8f
                }

                // MACD 라인
                val macdEntries = result.macd.mapIndexed { index, value ->
                    Entry(index.toFloat(), value.toFloat())
                }
                val macdDataSet = LineDataSet(macdEntries, "MACD").apply {
                    color = Color.parseColor("#6200EE")
                    lineWidth = 2f
                    setCircleColor(Color.parseColor("#6200EE"))
                    circleRadius = 2f
                    setDrawCircleHole(false)
                    setDrawValues(false)
                }

                // Signal 라인
                val signalEntries = result.signal.mapIndexed { index, value ->
                    Entry(index.toFloat(), value.toFloat())
                }
                val signalDataSet = LineDataSet(signalEntries, "Signal").apply {
                    color = Color.parseColor("#FF6200")
                    lineWidth = 2f
                    setCircleColor(Color.parseColor("#FF6200"))
                    circleRadius = 2f
                    setDrawCircleHole(false)
                    setDrawValues(false)
                    enableDashedLine(10f, 5f, 0f)
                }

                val lineData = LineData(macdDataSet, signalDataSet)

                val combinedData = CombinedData().apply {
                    setData(barData)
                    setData(lineData)
                }

                chart.data = combinedData
                chart.legend.isEnabled = true
                chart.invalidate()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
    }
}

/**
 * 증시 자금 동향 차트 (Double Y-Axis)
 */
@Composable
fun MarketDepositChart(
    data: MarketDepositData,
    modifier: Modifier = Modifier
) {
    ChartCard(
        title = "증시 자금 동향 (고객예탁금 & 신용잔고)",
        modifier = modifier
    ) {
        AndroidView(
            factory = { context ->
                CombinedChart(context).apply {
                    description.isEnabled = false
                    setTouchEnabled(true)
                    isDragEnabled = true
                    setScaleEnabled(true)
                    setPinchZoom(true)
                    setDrawGridBackground(false)

                    // X축 설정
                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        setDrawGridLines(true)
                        gridLineWidth = 1f
                        enableGridDashedLine(10f, 5f, 0f)
                        granularity = 1f
                        labelRotationAngle = -45f
                        setLabelCount(8, false)
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                val index = value.toInt()
                                return if (index >= 0 && index < data.dates.size) {
                                    data.dates[index]
                                } else {
                                    ""
                                }
                            }
                        }
                    }

                    // 왼쪽 Y축 (고객예탁금)
                    axisLeft.apply {
                        setDrawGridLines(true)
                        gridLineWidth = 1f
                        enableGridDashedLine(10f, 5f, 0f)
                        textColor = Color.parseColor("#6200EE")
                        setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return "${value.toInt()}억"
                            }
                        }
                    }

                    // 오른쪽 Y축 (신용잔고)
                    axisRight.apply {
                        isEnabled = true
                        setDrawGridLines(false)
                        textColor = Color.parseColor("#03DAC5")
                        setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return "${value.toInt()}억"
                            }
                        }
                    }

                    legend.apply {
                        isEnabled = true
                        textSize = 12f
                    }
                }
            },
            update = { chart ->
                // 고객예탁금 (왼쪽 Y축)
                val depositEntries = data.depositAmounts.mapIndexed { index, value ->
                    Entry(index.toFloat(), value.toFloat())
                }
                val depositDataSet = LineDataSet(depositEntries, "고객예탁금").apply {
                    axisDependency = YAxis.AxisDependency.LEFT
                    color = Color.parseColor("#6200EE")
                    lineWidth = 2.5f
                    setCircleColor(Color.parseColor("#6200EE"))
                    circleRadius = 2f
                    setDrawCircleHole(false)
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                }

                // 신용잔고 (오른쪽 Y축)
                val creditEntries = data.creditAmounts.mapIndexed { index, value ->
                    Entry(index.toFloat(), value.toFloat())
                }
                val creditDataSet = LineDataSet(creditEntries, "신용잔고").apply {
                    axisDependency = YAxis.AxisDependency.RIGHT
                    color = Color.parseColor("#03DAC5")
                    lineWidth = 2.5f
                    setCircleColor(Color.parseColor("#03DAC5"))
                    circleRadius = 2f
                    setDrawCircleHole(false)
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                }

                val lineData = LineData(depositDataSet, creditDataSet)

                val combinedData = CombinedData().apply {
                    setData(lineData)
                }

                chart.data = combinedData
                chart.invalidate()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
        )
    }
}

/**
 * 차트 카드 컨테이너
 */
@Composable
private fun ChartCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}