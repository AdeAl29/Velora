package com.example.finora.ui.screen.bill

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
import com.example.finora.core.database.entity.BillEntity
import com.example.finora.core.util.CurrencyFormatter
import com.example.finora.ui.components.*
import com.example.finora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillListScreen(
    viewModel: BillViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddBill: () -> Unit,
    onNavigateToEditBill: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState by viewModel.listState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var billToDelete by remember { mutableStateOf<BillEntity?>(null) }

    if (billToDelete != null) {
        AlertDialog(
            onDismissRequest = { billToDelete = null },
            title = { Text("Hapus Tagihan?") },
            text = { Text("Tagihan ini akan dihapus dari daftar pengingat.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        billToDelete?.let { viewModel.deleteBill(it) }
                        billToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { billToDelete = null }) { Text("Batal") }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tagihan & Pengingat", style = MaterialTheme.typography.titleLarge) },
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
                onClick = onNavigateToAddBill,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Tambah Tagihan", fontWeight = FontWeight.SemiBold) }
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
                // Header Card
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
                                    brush = Brush.horizontalGradient(listOf(Color(0xFFFB923C), Color(0xFFFBBF24))),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .padding(24.dp)
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL TAGIHAN BELUM DIBAYAR",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = CurrencyFormatter.formatRupiah(listState.totalUnpaid),
                                    style = FinoraTextStyles.balanceDisplay,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${listState.unpaidBills.size} tagihan menanti pembayaran",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Tab Selector
                item {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clip(RoundedCornerShape(14.dp)),
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Belum Dibayar (${listState.unpaidBills.size})", fontWeight = FontWeight.Medium) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Sudah Dibayar (${listState.paidBills.size})", fontWeight = FontWeight.Medium) }
                        )
                    }
                }

                val currentBills = if (selectedTab == 0) listState.unpaidBills else listState.paidBills

                if (currentBills.isEmpty()) {
                    item {
                        FinoraEmptyState(
                            type = EmptyStateType.BILL,
                            title = if (selectedTab == 0) "Tidak Ada Tagihan Tertunggak" else "Belum Ada Riwayat Pembayaran",
                            message = if (selectedTab == 0) "Hebat! Semua tagihanmu sudah terselesaikan tepat waktu."
                            else "Tagihan yang ditandai lunas akan muncul di sini.",
                            actionText = if (selectedTab == 0) "+ Tambah Tagihan" else null,
                            onAction = if (selectedTab == 0) onNavigateToAddBill else null
                        )
                    }
                } else {
                    items(currentBills, key = { it.id }) { bill ->
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
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (bill.isPaid) FinoraIncome.copy(alpha = 0.15f) else FinoraWarning.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${bill.dueDate}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (bill.isPaid) FinoraIncome else FinoraWarning
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bill.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = CurrencyFormatter.formatRupiah(bill.amount),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (bill.isPaid) viewModel.markUnpaid(bill.id)
                                            else viewModel.markPaid(bill.id)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (bill.isPaid) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                            contentDescription = "Tandai bayar",
                                            tint = if (bill.isPaid) FinoraIncome else MaterialTheme.colorScheme.outline
                                        )
                                    }

                                    IconButton(onClick = { onNavigateToEditBill(bill.id) }) {
                                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    IconButton(onClick = { billToDelete = bill }) {
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
