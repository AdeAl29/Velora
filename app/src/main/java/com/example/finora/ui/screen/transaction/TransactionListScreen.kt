package com.example.finora.ui.screen.transaction

import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finora.core.util.CurrencyFormatter
import com.example.finora.ui.components.*
import com.example.finora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    viewModel: TransactionListViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }

    // Delete confirmation dialog
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Hapus Transaksi") },
            text = { Text("Apakah kamu yakin ingin menghapus transaksi ini? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog?.let { viewModel.deleteTransaction(it) }
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Screen Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Transaksi",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (!uiState.isLoading) {
                    Text(
                        text = "${uiState.totalCount} catatan ditemukan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(
                onClick = onNavigateToAdd,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(FinoraPrimary.copy(alpha = 0.12f))
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Tambah Transaksi", tint = FinoraPrimary)
            }
        }

        // Search bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::setSearchQuery,
            placeholder = { Text("Cari catatan transaksi...") },
            leadingIcon = { Icon(Icons.Rounded.Search, "Cari", modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (uiState.searchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Rounded.Close, "Hapus", modifier = Modifier.size(20.dp))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )

        // Filter chips (Type, Sort, and Wallets)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = uiState.selectedTypeFilter == null && uiState.selectedWalletId == null,
                    onClick = {
                        viewModel.setTypeFilter(null)
                        viewModel.setWalletFilter(null)
                    },
                    label = { Text("Semua") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
            item {
                FilterChip(
                    selected = uiState.selectedTypeFilter == "INCOME",
                    onClick = {
                        viewModel.setTypeFilter(if (uiState.selectedTypeFilter == "INCOME") null else "INCOME")
                    },
                    label = { Text("Pemasukan") },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FinoraIncome.copy(alpha = 0.15f),
                        selectedLabelColor = FinoraIncome
                    )
                )
            }
            item {
                FilterChip(
                    selected = uiState.selectedTypeFilter == "EXPENSE_FIXED",
                    onClick = {
                        viewModel.setTypeFilter(if (uiState.selectedTypeFilter == "EXPENSE_FIXED") null else "EXPENSE_FIXED")
                    },
                    label = { Text("Tetap") },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FinoraExpense.copy(alpha = 0.15f),
                        selectedLabelColor = FinoraExpense
                    )
                )
            }
            item {
                FilterChip(
                    selected = uiState.selectedTypeFilter == "EXPENSE_VARIABLE",
                    onClick = {
                        viewModel.setTypeFilter(if (uiState.selectedTypeFilter == "EXPENSE_VARIABLE") null else "EXPENSE_VARIABLE")
                    },
                    label = { Text("Tidak Tetap") },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FinoraWarning.copy(alpha = 0.15f),
                        selectedLabelColor = FinoraWarning
                    )
                )
            }
            item {
                FilterChip(
                    selected = uiState.sortByAmount,
                    onClick = { viewModel.toggleSortByAmount() },
                    label = { Text("Nominal") },
                    leadingIcon = {
                        Icon(
                            if (uiState.sortByAmount) Icons.Rounded.ArrowDownward else Icons.Rounded.Sort,
                            null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }
            items(uiState.wallets, key = { "w_${it.id}" }) { wallet ->
                val isSelected = uiState.selectedWalletId == wallet.id
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        viewModel.setWalletFilter(if (isSelected) null else wallet.id)
                    },
                    label = { Text(wallet.name) },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Transaction list
        if (uiState.isLoading) {
            FinoraLoadingState(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
        } else if (uiState.isEmpty) {
            FinoraEmptyState(
                type = if (uiState.searchQuery.isNotBlank()) EmptyStateType.SEARCH else EmptyStateType.TRANSACTION,
                title = if (uiState.searchQuery.isNotBlank()) "Tidak Ditemukan" else "Belum Ada Transaksi",
                message = if (uiState.searchQuery.isNotBlank()) "Tidak ada transaksi yang cocok dengan \"${uiState.searchQuery}\""
                else "Mulai catat transaksi pertamamu untuk melihat kondisi keuanganmu.",
                actionText = if (uiState.searchQuery.isBlank()) "+ Tambah Transaksi" else null,
                onAction = if (uiState.searchQuery.isBlank()) onNavigateToAdd else null,
                modifier = Modifier.padding(32.dp)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(top = 4.dp, bottom = 115.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Quick Filtered Summary Strip
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Masuk",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "+${CurrencyFormatter.formatCompact(uiState.filteredIncome)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = FinoraColors.income()
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Total Keluar",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "-${CurrencyFormatter.formatCompact(uiState.filteredExpense)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = FinoraColors.expense()
                                )
                            }
                        }
                    }
                }

                uiState.transactions.forEach { (dateGroup, transactions) ->
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateGroup.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val dayNet = transactions.sumOf {
                                if (it.transaction.transactionType == "INCOME") it.transaction.amount else -it.transaction.amount
                            }
                            Text(
                                text = CurrencyFormatter.formatSigned(kotlin.math.abs(dayNet), dayNet >= 0),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (dayNet >= 0) FinoraColors.income() else FinoraColors.expense()
                            )
                        }
                    }
                    items(transactions, key = { it.transaction.id }) { txWithCat ->
                        FinoraTransactionCard(
                            categoryName = txWithCat.categoryName,
                            categoryIconName = txWithCat.categoryIconName,
                            categoryColorHex = txWithCat.categoryColorHex,
                            note = txWithCat.transaction.note,
                            amount = txWithCat.transaction.amount,
                            isIncome = txWithCat.transaction.transactionType == "INCOME",
                            date = txWithCat.transaction.date,
                            walletName = txWithCat.walletName,
                            onClick = { onNavigateToDetail(txWithCat.transaction.id) },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }
        }
    }
}
