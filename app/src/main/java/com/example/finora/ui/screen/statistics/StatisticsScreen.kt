package com.example.finora.ui.screen.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finora.core.util.CurrencyFormatter
import com.example.finora.ui.components.FinoraLoadingState
import com.example.finora.ui.components.charts.*
import com.example.finora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        FinoraLoadingState(modifier = modifier.padding(20.dp))
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Screen Header
            item {
                Text(
                    text = "Statistik & Analisis",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
            }

            // Period Filters
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(StatPeriod.entries) { period ->
                            val isSelected = uiState.selectedPeriod == period
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setPeriod(period) },
                                label = { Text(period.label) },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Summary Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatMetricCard(
                            label = "Pemasukan",
                            amount = uiState.totalIncome,
                            color = FinoraIncome,
                            icon = Icons.Rounded.TrendingUp,
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricCard(
                            label = "Pengeluaran",
                            amount = uiState.totalExpense,
                            color = FinoraExpense,
                            icon = Icons.Rounded.TrendingDown,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatMetricCard(
                            label = "Arus Kas (Net)",
                            amount = uiState.netCashFlow,
                            color = if (uiState.netCashFlow >= 0) FinoraSecondary else FinoraExpense,
                            icon = Icons.Rounded.AccountBalance,
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricCard(
                            label = "Tabungan",
                            amount = uiState.totalSavings,
                            color = FinoraPrimary,
                            icon = Icons.Rounded.Savings,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Bar Chart: Income vs Expense
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Perbandingan Pemasukan & Pengeluaran",
                                style = FinoraTextStyles.sectionTitle,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            FinoraBarChart(
                                data = uiState.incomeExpenseBarData,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Line Chart: Trend
                if (uiState.cashFlowLineData.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Tren Perkembangan Saldo Kas",
                                    style = FinoraTextStyles.sectionTitle,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                FinoraLineChart(
                                    data = uiState.cashFlowLineData,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Donut Chart: Category Expenses
                if (uiState.categoryExpenses.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Proporsi Pengeluaran per Kategori",
                                    style = FinoraTextStyles.sectionTitle,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.align(Alignment.Start)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                FinoraDonutChart(
                                    data = uiState.categoryExpenses.take(6).map { ce ->
                                        ChartDataPoint(
                                            label = ce.categoryName,
                                            value = ce.amount.toFloat(),
                                            color = getCategoryColor(ce.categoryColorHex)
                                        )
                                    },
                                    centerText = CurrencyFormatter.formatCompact(uiState.totalExpense),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Fixed vs Variable Expense Breakdown
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Pengeluaran Tetap vs Tidak Tetap",
                                style = FinoraTextStyles.sectionTitle,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Tetap (Kos, Listrik, dsb)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(CurrencyFormatter.formatRupiah(uiState.fixedExpense), style = FinoraTextStyles.currencyAmount, color = FinoraSecondary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Tidak Tetap (Makan, Hiburan)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(CurrencyFormatter.formatRupiah(uiState.variableExpense), style = FinoraTextStyles.currencyAmount, color = FinoraWarning)
                                }
                            }
                        }
                    }
                }

                // Automatic Smart Insights
                if (uiState.insights.isNotEmpty()) {
                    item {
                        Text(
                            text = "Insight Finansial Otomatis",
                            style = FinoraTextStyles.sectionTitle,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    items(uiState.insights) { insight ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (insight.isPositive)
                                    MaterialTheme.colorScheme.surface
                                else
                                    FinoraExpense.copy(alpha = 0.08f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (insight.isPositive) FinoraIncome.copy(alpha = 0.15f)
                                            else FinoraExpense.copy(alpha = 0.2f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (insight.isPositive) Icons.Rounded.Lightbulb else Icons.Rounded.Warning,
                                        contentDescription = null,
                                        tint = if (insight.isPositive) FinoraIncome else FinoraExpense,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = insight.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = insight.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }

@Composable
private fun StatMetricCard(
    label: String,
    amount: Long,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = CurrencyFormatter.formatCompact(amount),
                style = FinoraTextStyles.balanceMedium.copy(fontSize = 18.sp),
                color = color
            )
        }
    }
}
