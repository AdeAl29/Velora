package com.example.finora.ui.screen.transaction

import androidx.compose.animation.*
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
                    Text("Hapus")
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
        // Search bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::setSearchQuery,
            placeholder = { Text("Cari transaksi...") },
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
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )

        // Filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            item {
                FilterChip(
                    selected = uiState.selectedTypeFilter == null,
                    onClick = { viewModel.setTypeFilter(null) },
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
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                uiState.transactions.forEach { (dateGroup, transactions) ->
                    item {
                        Text(
                            text = dateGroup.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing
                        )
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
