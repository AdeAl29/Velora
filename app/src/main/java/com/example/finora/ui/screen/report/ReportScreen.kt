package com.example.finora.ui.screen.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finora.core.util.CurrencyFormatter
import com.example.finora.ui.components.FinoraLoadingState
import com.example.finora.ui.components.FinoraPrimaryButton
import com.example.finora.ui.components.FinoraSecondaryButton
import com.example.finora.ui.screen.statistics.StatPeriod
import com.example.finora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Keuangan", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            FinoraLoadingState(modifier = Modifier.padding(paddingValues).padding(20.dp))
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Period Filters
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(StatPeriod.entries) { period ->
                            val isSelected = uiState.period == period
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setPeriod(period) },
                                label = { Text(period.label) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // Summary Report Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Ringkasan Laporan (${uiState.period.label})",
                                style = FinoraTextStyles.sectionTitle,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            ReportStatRow("Total Pemasukan", CurrencyFormatter.formatRupiah(uiState.totalIncome), FinoraIncome)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            ReportStatRow("Total Pengeluaran", CurrencyFormatter.formatRupiah(uiState.totalExpense), FinoraExpense)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            ReportStatRow("Pengeluaran Tetap", CurrencyFormatter.formatRupiah(uiState.totalFixedExpense), FinoraSecondary)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            ReportStatRow("Pengeluaran Tidak Tetap", CurrencyFormatter.formatRupiah(uiState.totalVariableExpense), FinoraWarning)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            ReportStatRow("Total Tabungan Disisihkan", CurrencyFormatter.formatRupiah(uiState.totalSavings), FinoraPrimary)
                        }
                    }
                }

                // Transaction Volume
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Volume Transaksi", style = MaterialTheme.typography.titleSmall)
                                Text("Total transaksi tercatat pada periode ini", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = "${uiState.transactions.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Export Actions
                item {
                    Text(
                        text = "Ekspor & Bagikan Dokumen",
                        style = FinoraTextStyles.sectionTitle,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FinoraPrimaryButton(
                            text = "Ekspor PDF",
                            icon = Icons.Rounded.PictureAsPdf,
                            onClick = { viewModel.exportPdf(context) },
                            isLoading = uiState.isExporting,
                            modifier = Modifier.weight(1f)
                        )
                        FinoraSecondaryButton(
                            text = "Ekspor CSV",
                            icon = Icons.Rounded.TableChart,
                            onClick = { viewModel.exportCsv(context) },
                            enabled = !uiState.isExporting,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(48.dp)) }
            }
        }
    }
}

@Composable
private fun ReportStatRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = FinoraTextStyles.currencyAmount, color = valueColor, fontWeight = FontWeight.Bold)
    }
}
