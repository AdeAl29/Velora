package com.example.finora.ui.screen.recurring

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finora.core.database.entity.RecurringTransactionEntity
import com.example.finora.core.util.CurrencyFormatter
import com.example.finora.core.util.DateUtils
import com.example.finora.ui.components.*
import com.example.finora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringListScreen(
    viewModel: RecurringViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddRecurring: () -> Unit,
    onNavigateToEditRecurring: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState by viewModel.listState.collectAsStateWithLifecycle()
    var itemToDelete by remember { mutableStateOf<RecurringTransactionEntity?>(null) }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Hapus Pengeluaran Tetap?") },
            text = { Text("Pengeluaran berulang ini akan dihapus dari sistem otomatis.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDelete?.let { viewModel.deleteRecurring(it) }
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) { Text("Batal") }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengeluaran Tetap", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddRecurring,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Tambah Rutin", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { paddingValues ->
        if (listState.isLoading) {
            FinoraLoadingState(modifier = Modifier.padding(paddingValues).padding(20.dp))
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Total Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.horizontalGradient(listOf(FinoraPrimary, FinoraSecondary)),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .padding(24.dp)
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL PENGELUARAN TETAP BULANAN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = CurrencyFormatter.formatRupiah(listState.totalMonthlyAmount),
                                    style = FinoraTextStyles.balanceDisplay,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${listState.items.size} transaksi berulang aktif",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Daftar Rutin",
                        style = FinoraTextStyles.sectionTitle,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (listState.items.isEmpty()) {
                    item {
                        FinoraEmptyState(
                            type = EmptyStateType.BILL,
                            title = "Belum Ada Pengeluaran Tetap",
                            message = "Catat biaya sewa kos, langganan internet, atau cicilan yang dibayar rutin.",
                            actionText = "+ Tambah Pengeluaran Rutin",
                            onAction = onNavigateToAddRecurring
                        )
                    }
                } else {
                    items(listState.items, key = { it.recurring.id }) { item ->
                        val rec = item.recurring
                        val catColor = getCategoryColor(item.category?.colorHex ?: "#6C63FF")

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(catColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    FinoraCategoryIcon(
                                        iconName = item.category?.iconName ?: "housing",
                                        color = catColor,
                                        size = 24.dp
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = rec.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${CurrencyFormatter.formatRupiah(rec.amount)} • ${rec.frequency}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Berikutnya: ${DateUtils.formatFullDate(rec.nextExecutionDate)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { onNavigateToEditRecurring(rec.id) }) {
                                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(onClick = { itemToDelete = rec }) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }
}
