package com.example.finora.ui.screen.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finora.core.util.CurrencyFormatter
import com.example.finora.core.util.DateUtils
import com.example.finora.ui.components.*
import com.example.finora.ui.components.charts.*
import com.example.finora.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToTransactionDetail: (Long) -> Unit,
    onNavigateToAllTransactions: () -> Unit,
    onNavigateToAllGoals: () -> Unit,
    onNavigateToGoalDetail: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        FinoraLoadingState(
            modifier = modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Greeting Header
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${DateUtils.getGreeting()} ${DateUtils.getGreetingEmoji()}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = uiState.userName.ifBlank { "Kelola Keuanganmu" },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = "Cari",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Balance Card
        item {
            FinoraBalanceCard(
                totalBalance = uiState.totalBalance,
                monthlyIncome = uiState.monthlyIncome,
                monthlyExpense = uiState.monthlyExpense,
                percentChange = uiState.percentChange,
                isBalanceHidden = uiState.isBalanceHidden,
                onToggleVisibility = viewModel::toggleBalanceVisibility,
                sparklineData = uiState.sparklineData,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        // Monthly Summary
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                SectionHeader(title = "Ringkasan Bulan Ini")
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        title = "Tabungan",
                        amount = uiState.monthlySavings,
                        isPositive = uiState.monthlySavings >= 0,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Tersisa",
                        amount = null,
                        percentage = uiState.percentRemaining,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Expense Chart
        if (uiState.categoryExpenses.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SectionHeader(title = "Pengeluaran per Kategori")
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        FinoraDonutChart(
                            data = uiState.categoryExpenses.take(6).map { ce ->
                                ChartDataPoint(
                                    label = ce.categoryName,
                                    value = ce.amount.toFloat(),
                                    color = getCategoryColor(ce.categoryColorHex)
                                )
                            },
                            centerText = CurrencyFormatter.formatCompact(uiState.monthlyExpense),
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            }
        }

        // Fixed vs Variable Expense
        if (uiState.fixedExpense > 0 || uiState.variableExpense > 0) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SectionHeader(title = "Pengeluaran")
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ExpenseTypeCard(
                            title = "Tetap",
                            amount = uiState.fixedExpense,
                            color = FinoraSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        ExpenseTypeCard(
                            title = "Tidak Tetap",
                            amount = uiState.variableExpense,
                            color = FinoraWarning,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Goals
        if (uiState.activeGoals.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionHeader(title = "Tujuan Keuangan")
                        FinoraTextButton(text = "Lihat Semua", onClick = onNavigateToAllGoals)
                    }
                }
            }
            items(uiState.activeGoals, key = { it.id }) { goal ->
                FinoraGoalCard(
                    name = goal.name,
                    iconName = goal.iconName,
                    colorHex = goal.colorHex,
                    targetAmount = goal.targetAmount,
                    currentAmount = goal.currentAmount,
                    deadline = goal.deadline,
                    onClick = { onNavigateToGoalDetail(goal.id) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }

        // Recent Transactions
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(title = "Transaksi Terbaru")
                    FinoraTextButton(text = "Lihat Semua", onClick = onNavigateToAllTransactions)
                }
            }
        }

        if (uiState.recentTransactions.isEmpty()) {
            item {
                FinoraEmptyState(
                    type = EmptyStateType.TRANSACTION,
                    title = "Belum Ada Transaksi",
                    message = "Mulai catat transaksi pertamamu untuk melihat kondisi keuanganmu.",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        } else {
            items(uiState.recentTransactions, key = { it.transaction.id }) { txWithCat ->
                FinoraTransactionCard(
                    categoryName = txWithCat.categoryName,
                    categoryIconName = txWithCat.categoryIconName,
                    categoryColorHex = txWithCat.categoryColorHex,
                    note = txWithCat.transaction.note,
                    amount = txWithCat.transaction.amount,
                    isIncome = txWithCat.transaction.transactionType == "INCOME",
                    date = txWithCat.transaction.date,
                    walletName = txWithCat.walletName,
                    onClick = { onNavigateToTransactionDetail(txWithCat.transaction.id) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }

        // Wallets
        if (uiState.wallets.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SectionHeader(title = "Rekening")
                    Spacer(modifier = Modifier.height(12.dp))
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    items(uiState.wallets, key = { it.wallet.id }) { walletWithBalance ->
                        FinoraWalletCard(
                            name = walletWithBalance.wallet.name,
                            iconName = walletWithBalance.wallet.iconName,
                            colorHex = walletWithBalance.wallet.colorHex,
                            balance = walletWithBalance.balance,
                            onClick = { },
                            modifier = Modifier.width(160.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = FinoraTextStyles.sectionTitle,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun SummaryCard(
    title: String,
    amount: Long? = null,
    percentage: Float? = null,
    isPositive: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (amount != null) {
                Text(
                    text = CurrencyFormatter.formatRupiah(kotlin.math.abs(amount)),
                    style = FinoraTextStyles.currencyAmount,
                    color = if (isPositive) FinoraColors.income() else FinoraColors.expense()
                )
            }
            if (percentage != null) {
                val safePercent = if (percentage.isFinite()) percentage else 0f
                Text(
                    text = "${String.format(java.util.Locale.US, "%.1f", safePercent)}%",
                    style = FinoraTextStyles.currencyAmount,
                    color = if (safePercent > 30) FinoraColors.income() else FinoraColors.expense()
                )
            }
        }
    }
}

@Composable
private fun ExpenseTypeCard(
    title: String,
    amount: Long,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = CurrencyFormatter.formatRupiah(amount),
                style = FinoraTextStyles.currencyAmount,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
