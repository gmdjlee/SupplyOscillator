package com.stockoscillator.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * 자동완성 기능이 있는 종목 검색 필드
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockSearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    suggestions: List<Pair<String, String>>, // (ticker, name)
    onSuggestionClick: (String, String) -> Unit,
    onSearch: () -> Unit,
    enabled: Boolean = true,
    label: String = "종목명 입력",
    placeholder: String = "예: 삼성전자",
    modifier: Modifier = Modifier
) {
    var showSuggestions by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = { newQuery ->
                onQueryChange(newQuery)
                showSuggestions = newQuery.isNotEmpty()
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            onQueryChange("")
                            showSuggestions = false
                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "지우기")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors()
        )

        // 자동완성 제안 목록
        AnimatedVisibility(
            visible = showSuggestions && suggestions.isNotEmpty()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(suggestions.take(5)) { (ticker, name) ->
                        SuggestionItem(
                            ticker = ticker,
                            name = name,
                            onClick = {
                                onQueryChange(name)
                                onSuggestionClick(ticker, name)
                                showSuggestions = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 자동완성 제안 항목
 */
@Composable
private fun SuggestionItem(
    ticker: String,
    name: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = ticker,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
    Divider()
}