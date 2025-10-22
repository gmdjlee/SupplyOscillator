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
import com.stockoscillator.data.model.UiState
import com.stockoscillator.ui.components.MarketDepositChart
import com.stockoscillator.ui.viewmodel.AnalysisViewModel
import com.stockoscillator.ui.viewmodel.InvestorData

@Composable
fun AnalysisScreen() {
    val context = LocalContext.current
    val viewModel: AnalysisViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AnalysisViewModel(context.applicationContext as Application) as T
            }
        }
    )

    val investorUiState by viewModel.investorUiState.collectAsState()
    val marketUiState by viewModel.marketUiState.collectAsState()
    val marketAnalysis by viewModel.marketAnalysis.collectAsState()
    val stockInfo by viewModel.stockInfo.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 탭 선택
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("종목별 수급") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("증시 자금") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> {
                // 종목별 투자자 수급
                InvestorAnalysisTab(
                    searchQuery = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { viewModel.analyzeInvestors(searchQuery) },
                    uiState = investorUiState,
                    stockInfo = stockInfo
                )
            }
            1 -> {
                // 증시 자금 동향
                MarketAnalysisTab(
                    onAnalyze = { viewModel.analyzeMarket() },
                    uiState = marketUiState,
                    analysis = marketAnalysis
                )
            }
        }
    }
}

@Composable
private fun InvestorAnalysisTab(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    uiState: UiState<List<InvestorData>>,
    stockInfo: Pair<String, String>?
) {
    Column {
        // 검색 섹션
        SearchCard(
            query = searchQuery,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            isLoading = uiState is UiState.Loading,
            label = "종목명 입력",
            buttonText = "수급 분석"
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is UiState.Idle -> {
                InfoCard("종목명을 입력하고 '수급 분석' 버튼을 눌러주세요")
            }

            is UiState.Loading -> {
                LoadingCard("투자자별 수급을 분석하고 있습니다...")
            }

            is UiState.Error -> {
                ErrorCard(state.message)
            }

            is UiState.Success -> {
                stockInfo?.let { (ticker, name) ->
                    StockInfoCard(ticker, name)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                state.data.forEach { investor ->
                    InvestorCard(investor)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun MarketAnalysisTab(
    onAnalyze: () -> Unit,
    uiState: UiState<com.stockoscillator.data.model.MarketDepositData>,
    analysis: String
) {
    Column {
        // 분석 버튼
        Button(
            onClick = onAnalyze,
            enabled = uiState !is UiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("증시 자금 동향 분석")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is UiState.Idle -> {
                InfoCard("버튼을 눌러 증시 자금 동향을 확인하세요")
            }

            is UiState.Loading -> {
                LoadingCard("증시 자금 동향을 분석하고 있습니다...")
            }

            is UiState.Error -> {
                ErrorCard(state.message)
            }

            is UiState.Success -> {
                if (analysis.isNotEmpty()) {
                    AnalysisCard(analysis)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                MarketDepositChart(data = state.data)

                Spacer(modifier = Modifier.height(16.dp))

                DepositSummaryCard(state.data)
            }
        }
    }
}

// === 헬퍼 Composables ===

@Composable
private fun SearchCard(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    isLoading: Boolean,
    label: String,
    buttonText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text(label) },
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
                Text(buttonText)
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
        Column(modifier = Modifier.padding(16.dp)) {
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
private fun InvestorCard(investor: InvestorData) {
    val bgColor = if (investor.isPositive) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    val textColor = if (investor.isPositive) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = investor.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor
                )
                Text(
                    text = investor.trend,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.8f)
                )
            }

            Text(
                text = formatMoney(investor.netBuying),
                style = MaterialTheme.typography.titleLarge,
                color = textColor
            )
        }
    }
}

@Composable
private fun AnalysisCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "시장 분석",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun DepositSummaryCard(data: com.stockoscillator.data.model.MarketDepositData) {
    if (data.dates.isEmpty()) return

    val lastIdx = data.dates.size - 1

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "최신 데이터 (${data.dates[lastIdx]})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("고객예탁금", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "${String.format("%.0f", data.depositAmounts[lastIdx])}억원",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Column {
                    Text("신용잔고", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "${String.format("%.0f", data.creditAmounts[lastIdx])}억원",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
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
        Column(modifier = Modifier.padding(16.dp)) {
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

private fun formatMoney(value: Long): String {
    val absValue = kotlin.math.abs(value)
    val formatted = when {
        absValue >= 100_000_000 -> String.format("%.1f억", value / 100_000_000.0)
        absValue >= 10_000 -> String.format("%.1f만", value / 10_000.0)
        else -> String.format("%d", value)
    }
    return if (value >= 0) "+$formatted" else formatted
}