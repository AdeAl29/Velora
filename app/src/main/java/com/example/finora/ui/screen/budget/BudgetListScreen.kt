package com.example.finora.ui.screen.budget

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
import com.example.finora.core.database.entity.BudgetEntity
import com.example.finora.core.util.CurrencyFormatter
import com.example.finora.ui.components.*
import com.example.finora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetListScreen(
    viewModel: BudgetViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddBudget: () -> Unit,
    onNavigateToEditBudget: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState by viewModel.listState.collectAsStateWithLifecycle()
    var budgetToDelete by remember { mutableStateOf<BudgetEntity?>(null) }

    if (budgetToDelete != null) {
        AlertDialog(
            onDismissRequest = { budgetToDelete = null },
            title = { Text("Hapus Anggaran?") },
            text = { Text("Anggaran untuk kategori ini akan dihapus.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        budgetToDelete?.let { viewModel.deleteBudget(it) }
                        budgetToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { budgetToDelete = null }) { Text("Batal") }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anggaran Bulanan", style = MaterialTheme.typography.titleLarge) },
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
                onClick = onNavigateToAddBudget,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Buat Anggaran", fontWeight = FontWeight.SemiBold) }
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
                // Header overview
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
                                    brush = Brush.horizontalGradient(FinoraPrimaryGradient),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .padding(24.dp)
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL ANGGARAN TERPAKAI",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${CurrencyFormatter.formatRupiah(listState.totalSpent)} / ${CurrencyFormatter.formatRupiah(listState.totalBudget)}",
                                    style = FinoraTextStyles.balanceMedium,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                val totalPct = if (listState.totalBudget > 0)
                                    (listState.totalSpent.toFloat() / listState.totalBudget * 100).coerceIn(0f, 100f)
                                else 0f
                                LinearProgressIndicator(
                                    progress = { totalPct / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = if (totalPct >= 100f) FinoraExpense else Color.White,
                                    trackColor = Color.White.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Kategori Anggaran (${listState.budgets.size})",
                        style = FinoraTextStyles.sectionTitle,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (listState.budgets.isEmpty()) {
                    item {
                        FinoraEmptyState(
                            type = EmptyStateType.BUDGET,
                            title = "Belum Ada Anggaran",
                            message = "Buat batas pengeluaran bulanan per kategori untuk mengontrol keuanganmu.",
                            actionText = "+ Buat Anggaran",
                            onAction = onNavigateToAddBudget
                        )
                    }
                } else {
                    items(listState.budgets, key = { it.budget.id }) { item ->
                        FinoraBudgetCard(
                            categoryName = item.category?.name ?: "Lainnya",
                            categoryIconName = item.category?.iconName ?: "other",
                            categoryColorHex = item.category?.colorHex ?: "#6C63FF",
                            budgetAmount = item.budget.amount,
                            spentAmount = item.spentAmount,
                            onClick = { onNavigateToEditBudget(item.budget.id) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }
}
