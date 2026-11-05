package com.example.finora.ui.screen.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    onNavigateToWallets: () -> Unit = {},
    onNavigateToTransfer: () -> Unit = {},
    onNavigateToBudgets: () -> Unit = {},
    onNavigateToBills: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
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
        contentPadding = PaddingValues(top = 12.dp, bottom = 115.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Greeting Header
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onNavigateToCalendar,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        ) {
                            Icon(
                                Icons.Rounded.CalendarMonth,
                                contentDescription = "Kalender Keuangan",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onNavigateToSearch,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        ) {
                            Icon(
                                Icons.Rounded.Search,
                                contentDescription = "Cari",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
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

        // iOS-Style Quick Action Pills
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HomeQuickActionButton(
                    icon = Icons.Rounded.SwapHoriz,
                    label = "Transfer",
                    color = Color(0xFF38BDF8),
                    onClick = onNavigateToTransfer,
                    modifier = Modifier.weight(1f)
                )
                HomeQuickActionButton(
                    icon = Icons.Rounded.AccountBalanceWallet,
                    label = "Rekening",
                    color = Color(0xFF818CF8),
                    onClick = onNavigateToWallets,
                    modifier = Modifier.weight(1f)
                )
                HomeQuickActionButton(
                    icon = Icons.Rounded.PieChart,
                    label = "Anggaran",
                    color = Color(0xFF34D399),
                    onClick = onNavigateToBudgets,
                    modifier = Modifier.weight(1f)
                )
                HomeQuickActionButton(
                    icon = Icons.Rounded.ReceiptLong,
                    label = "Tagihan",
                    color = Color(0xFFFBBF24),
                    badgeCount = uiState.unpaidBills.size,
                    onClick = onNavigateToBills,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Wallets Horizontal Carousel
        if (uiState.wallets.isNotEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionHeader(title = "Rekening & Dompet")
                        FinoraTextButton(text = "Kelola", onClick = onNavigateToWallets)
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp)
                    ) {
                        items(uiState.wallets, key = { it.wallet.id }) { walletWithBalance ->
                            FinoraWalletCard(
                                name = walletWithBalance.wallet.name,
                                iconName = walletWithBalance.wallet.iconName,
                                colorHex = walletWithBalance.wallet.colorHex,
                                balance = if (uiState.isBalanceHidden) 0L else walletWithBalance.balance,
                                onClick = onNavigateToWallets,
                                modifier = Modifier.width(168.dp)
                            )
                        }
                    }
                }
            }
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
                        title = "Arus Kas Bersih",
                        amount = uiState.monthlySavings,
                        isPositive = uiState.monthlySavings >= 0,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Sisa Pemasukan",
                        amount = null,
                        percentage = uiState.percentRemaining,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Fixed vs Variable Expense
        if (uiState.fixedExpense > 0 || uiState.variableExpense > 0) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SectionHeader(title = "Komposisi Pengeluaran")
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

        // Expense Chart
        if (uiState.categoryExpenses.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SectionHeader(title = "Pengeluaran per Kategori")
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        shape = RoundedCornerShape(22.dp),
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

        // Unpaid Bills Section
        if (uiState.unpaidBills.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionHeader(title = "Tagihan Mendatang")
                        FinoraTextButton(text = "Lihat Semua", onClick = onNavigateToBills)
                    }
                }
            }
            items(uiState.unpaidBills, key = { "bill_${it.id}" }) { bill ->
                FinoraBillCard(
                    name = bill.name,
                    amount = bill.amount,
                    dueDate = bill.dueDate,
                    isPaid = bill.isPaid,
                    onClick = onNavigateToBills,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
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
    }
}

@Composable
private fun HomeQuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(FinoraExpense),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
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
        shape = RoundedCornerShape(18.dp),
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
                val sign = if (amount > 0) "+" else if (amount < 0) "-" else ""
                Text(
                    text = "$sign${CurrencyFormatter.formatRupiah(kotlin.math.abs(amount))}",
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
        shape = RoundedCornerShape(18.dp),
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
