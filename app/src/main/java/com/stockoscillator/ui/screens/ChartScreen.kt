package com.stockoscillator.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stockoscillator.data.model.SignalAnalysis
import com.stockoscillator.data.model.TradeSignal
import com.stockoscillator.data.model.UiState
import com.stockoscillator.ui.components.MacdChart
import com.stockoscillator.ui.components.MarketCapOscillatorChart
import com.stockoscillator.ui.viewmodel.ChartViewModel

@Composable
fun ChartScreen() {
    val context = LocalContext.current
    val viewModel: ChartViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ChartViewModel(context.applicationContext as Application) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    val stockInfo by viewModel.stockInfo.collectAsState()
    val analysisResult by viewModel.analysisResult.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 검색 섹션
        SearchSection(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { viewModel.analyzeStock(searchQuery) },
            isLoading = uiState is UiState.Loading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 상태별 UI
        when (val state = uiState) {
            is UiState.Idle -> {
                InfoCard(
                    text = "종목명을 입력하고 '차트 분석' 버튼을 눌러주세요"
                )
            }

            is UiState.Loading -> {
                LoadingCard(text = "데이터를 수집하고 있습니다...")
            }

            is UiState.Error -> {
                ErrorCard(message = state.message)
            }

            is UiState.Success -> {
                val result = state.data

                // 종목 정보
                stockInfo?.let { (ticker, name) ->
                    StockInfoCard(ticker = ticker, name = name)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 매매 신호
                analysisResult?.let { analysis ->
                    SignalCard(analysis = analysis)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 시가총액 + 수급 오실레이터 복합 차트 (Double Y-Axis)
                MarketCapOscillatorChart(
                    result = result,
                    marketCap = result.marketCap  // 실제 시가총액 데이터 사용
                )

                Spacer(modifier = Modifier.height(16.dp))

                // MACD 차트
                MacdChart(result = result)
            }
        }
    }
}

// === 헬퍼 Composables ===

@Composable
private fun SearchSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "종목 검색",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("종목명 입력") },
                placeholder = { Text("예: 삼성전자") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSearch,
                enabled = !isLoading && query.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("차트 분석")
            }
        }
    }
}

@Composable
private fun StockInfoCard(ticker: String, name: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = ticker,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SignalCard(analysis: SignalAnalysis) {
    val (bgColor, textColor) = when (analysis.signal) {
        TradeSignal.STRONG_BUY -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        TradeSignal.BUY -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        TradeSignal.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        TradeSignal.SELL -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        TradeSignal.STRONG_SELL -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "매매 신호",
                style = MaterialTheme.typography.titleMedium,
                color = textColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = analysis.signal.name.replace("_", " "),
                style = MaterialTheme.typography.headlineMedium,
                color = textColor
            )

            Text(
                text = "점수: ${String.format("%.1f", analysis.score)}/100",
                style = MaterialTheme.typography.bodyLarge,
                color = textColor.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "추세: ${analysis.trend}", color = textColor.copy(alpha = 0.9f))
            Text(text = "외국인: ${analysis.foreignTrend}", color = textColor.copy(alpha = 0.9f))
            Text(text = "기관: ${analysis.institutionTrend}", color = textColor.copy(alpha = 0.9f))

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "💡 ${analysis.recommendation}",
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
        }
    }
}

@Composable
private fun InfoCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoadingCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "오류",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}