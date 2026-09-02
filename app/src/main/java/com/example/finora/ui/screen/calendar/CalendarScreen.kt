package com.example.finora.ui.screen.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finora.core.util.CurrencyFormatter
import com.example.finora.ui.components.FinoraEmptyState
import com.example.finora.ui.components.FinoraTransactionCard
import com.example.finora.ui.components.EmptyStateType
import com.example.finora.ui.theme.*
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTransactionDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val monthTitle = uiState.currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("id", "ID")))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kalender Keuangan", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Header with Prev/Next buttons
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = viewModel::previousMonth) {
                                Icon(Icons.Rounded.ChevronLeft, contentDescription = "Bulan sebelumnya")
                            }

                            Text(
                                text = monthTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            IconButton(onClick = viewModel::nextMonth) {
                                Icon(Icons.Rounded.ChevronRight, contentDescription = "Bulan berikutnya")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Days of week header
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min").forEach { dayName ->
                                Text(
                                    text = dayName,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Calendar Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            modifier = Modifier.height(240.dp),
                            userScrollEnabled = false,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Empty cells for first day of week offset
                            val firstDayOfWeek = uiState.currentMonth.atDay(1).dayOfWeek.value // 1 = Monday, 7 = Sunday
                            items(firstDayOfWeek - 1) {
                                Box(modifier = Modifier.size(36.dp))
                            }

                            items(uiState.daysInMonth) { dayData ->
                                val isSelected = dayData.date == uiState.selectedDate
                                Column(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else if (dayData.isToday) MaterialTheme.colorScheme.primaryContainer
                                            else Color.Transparent
                                        )
                                        .clickable { viewModel.selectDate(dayData.date) },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${dayData.date.dayOfMonth}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) Color.White
                                        else if (dayData.isToday) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected || dayData.isToday) FontWeight.Bold else FontWeight.Normal
                                    )

                                    // Dot indicators for income / expense
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        modifier = Modifier.padding(top = 1.dp)
                                    ) {
                                        if (dayData.hasIncome) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) Color.White else FinoraIncome)
                                            )
                                        }
                                        if (dayData.hasExpense) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) Color.White else FinoraExpense)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Daily Summary Card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Pemasukan Hari Ini", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(CurrencyFormatter.formatRupiah(uiState.selectedDayIncome), style = MaterialTheme.typography.titleMedium, color = FinoraIncome)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Pengeluaran Hari Ini", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(CurrencyFormatter.formatRupiah(uiState.selectedDayExpense), style = MaterialTheme.typography.titleMedium, color = FinoraExpense)
                        }
                    }
                }
            }

            // Section Header
            item {
                Text(
                    text = "Transaksi pada ${uiState.selectedDate.dayOfMonth} ${monthTitle}",
                    style = FinoraTextStyles.sectionTitle,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            if (uiState.selectedDayTransactions.isEmpty()) {
                item {
                    FinoraEmptyState(
                        type = EmptyStateType.TRANSACTION,
                        title = "Tidak Ada Transaksi",
                        message = "Tidak ada aktivitas keuangan yang tercatat pada tanggal ini.",
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            } else {
                items(uiState.selectedDayTransactions, key = { it.transaction.id }) { item ->
                    FinoraTransactionCard(
                        categoryName = item.categoryName,
                        categoryIconName = item.categoryIconName,
                        categoryColorHex = item.categoryColorHex,
                        note = item.transaction.note,
                        amount = item.transaction.amount,
                        isIncome = item.transaction.transactionType == "INCOME",
                        date = item.transaction.date,
                        walletName = item.walletName,
                        onClick = { onNavigateToTransactionDetail(item.transaction.id) },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }
}
