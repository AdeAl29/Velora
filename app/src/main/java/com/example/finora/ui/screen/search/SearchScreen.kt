package com.example.finora.ui.screen.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finora.core.util.CurrencyFormatter
import com.example.finora.ui.components.*
import com.example.finora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTransactionDetail: (Long) -> Unit,
    onNavigateToGoalDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pencarian Global", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::setQuery,
                placeholder = { Text("Cari transaksi, kategori, rekening, tujuan...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    if (uiState.query.isNotBlank()) {
                        IconButton(onClick = { viewModel.setQuery("") }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Hapus", modifier = Modifier.size(20.dp))
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

            val totalResults = uiState.transactions.size + uiState.categories.size +
                    uiState.wallets.size + uiState.goals.size + uiState.bills.size

            if (uiState.query.isBlank()) {
                FinoraEmptyState(
                    type = EmptyStateType.SEARCH,
                    title = "Cari Apa Saja",
                    message = "Ketik kata kunci untuk mencari catatan transaksi, tujuan, dompet, atau tagihanmu.",
                    modifier = Modifier.padding(32.dp)
                )
            } else if (totalResults == 0 && uiState.hasSearched) {
                FinoraEmptyState(
                    type = EmptyStateType.SEARCH,
                    title = "Tidak Ditemukan",
                    message = "Tidak ada data yang cocok dengan kata kunci \"${uiState.query}\".",
                    modifier = Modifier.padding(32.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Transactions Section
                    if (uiState.transactions.isNotEmpty()) {
                        item {
                            Text(
                                text = "Transaksi (${uiState.transactions.size})",
                                style = FinoraTextStyles.sectionTitle,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        items(uiState.transactions, key = { "tx_${it.transaction.id}" }) { item ->
                            FinoraTransactionCard(
                                categoryName = item.categoryName,
                                categoryIconName = item.categoryIconName,
                                categoryColorHex = item.categoryColorHex,
                                note = item.transaction.note,
                                amount = item.transaction.amount,
                                isIncome = item.transaction.transactionType == "INCOME",
                                date = item.transaction.date,
                                walletName = item.walletName,
                                onClick = { onNavigateToTransactionDetail(item.transaction.id) }
                            )
                        }
                    }

                    // Goals Section
                    if (uiState.goals.isNotEmpty()) {
                        item {
                            Text(
                                text = "Tujuan Finansial (${uiState.goals.size})",
                                style = FinoraTextStyles.sectionTitle,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        items(uiState.goals, key = { "goal_${it.id}" }) { goal ->
                            FinoraGoalCard(
                                name = goal.name,
                                iconName = goal.iconName,
                                colorHex = goal.colorHex,
                                targetAmount = goal.targetAmount,
                                currentAmount = goal.currentAmount,
                                deadline = goal.deadline,
                                onClick = { onNavigateToGoalDetail(goal.id) }
                            )
                        }
                    }

                    // Wallets Section
                    if (uiState.wallets.isNotEmpty()) {
                        item {
                            Text(
                                text = "Rekening (${uiState.wallets.size})",
                                style = FinoraTextStyles.sectionTitle,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        items(uiState.wallets, key = { "wallet_${it.id}" }) { wallet ->
                            val color = getCategoryColor(wallet.colorHex)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(color.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        FinoraCategoryIcon(iconName = wallet.iconName, color = color, size = 22.dp)
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(text = wallet.name, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }

                    // Bills Section
                    if (uiState.bills.isNotEmpty()) {
                        item {
                            Text(
                                text = "Tagihan (${uiState.bills.size})",
                                style = FinoraTextStyles.sectionTitle,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        items(uiState.bills, key = { "bill_${it.id}" }) { bill ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${bill.dueDate}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = bill.name, style = MaterialTheme.typography.titleSmall)
                                        Text(text = CurrencyFormatter.formatRupiah(bill.amount), style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}
