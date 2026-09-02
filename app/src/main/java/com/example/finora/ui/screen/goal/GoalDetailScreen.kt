package com.example.finora.ui.screen.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finora.core.util.CurrencyFormatter
import com.example.finora.core.util.DateUtils
import com.example.finora.ui.components.FinoraCategoryIcon
import com.example.finora.ui.components.FinoraLoadingState
import com.example.finora.ui.components.FinoraPrimaryButton
import com.example.finora.ui.components.FinoraSecondaryButton
import com.example.finora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    viewModel: GoalViewModel,
    goalId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val goal by viewModel.selectedGoal.collectAsStateWithLifecycle()
    var showAddFundsDialog by remember { mutableStateOf(false) }
    var showWithdrawFundsDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var fundInputAmount by remember { mutableStateOf("") }

    LaunchedEffect(goalId) {
        viewModel.loadGoalDetail(goalId)
    }

    if (showAddFundsDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddFundsDialog = false
                fundInputAmount = ""
            },
            title = { Text("Tambah Tabungan") },
            text = {
                Column {
                    Text("Masukkan nominal yang ingin disisihkan untuk tujuan ini:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = fundInputAmount,
                        onValueChange = { fundInputAmount = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Nominal (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = fundInputAmount.toLongOrNull() ?: 0L
                        if (amount > 0) {
                            viewModel.addFunds(goalId, amount)
                        }
                        showAddFundsDialog = false
                        fundInputAmount = ""
                    }
                ) {
                    Text("Tambah", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddFundsDialog = false
                    fundInputAmount = ""
                }) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showWithdrawFundsDialog) {
        AlertDialog(
            onDismissRequest = {
                showWithdrawFundsDialog = false
                fundInputAmount = ""
            },
            title = { Text("Ambil Tabungan") },
            text = {
                Column {
                    Text("Masukkan nominal yang ingin ditarik dari tujuan ini:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = fundInputAmount,
                        onValueChange = { fundInputAmount = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Nominal (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = fundInputAmount.toLongOrNull() ?: 0L
                        if (amount > 0) {
                            viewModel.removeFunds(goalId, amount)
                        }
                        showWithdrawFundsDialog = false
                        fundInputAmount = ""
                    }
                ) {
                    Text("Tarik", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showWithdrawFundsDialog = false
                    fundInputAmount = ""
                }) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Tujuan?") },
            text = { Text("Tujuan finansial ini akan dihapus secara permanen.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteGoal(goalId)
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Batal") }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Tujuan", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(goalId) }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit Tujuan")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        val currentGoal = goal
        if (currentGoal == null) {
            FinoraLoadingState(modifier = Modifier.padding(paddingValues).padding(20.dp))
        } else {
            val color = getCategoryColor(currentGoal.colorHex)
            val pct = if (currentGoal.targetAmount > 0)
                (currentGoal.currentAmount.toFloat() / currentGoal.targetAmount * 100).coerceIn(0f, 100f)
            else 0f
            val remaining = (currentGoal.targetAmount - currentGoal.currentAmount).coerceAtLeast(0L)

            // Monthly saving calculation
            val monthsLeft = currentGoal.deadline?.let { DateUtils.monthsUntil(it) } ?: 0L
            val monthlySavingEstimate = if (monthsLeft > 0 && remaining > 0) remaining / monthsLeft else remaining

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    FinoraCategoryIcon(iconName = currentGoal.iconName, color = color, size = 42.dp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentGoal.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${CurrencyFormatter.formatRupiah(currentGoal.currentAmount)} dari ${CurrencyFormatter.formatRupiah(currentGoal.targetAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Progress Bar Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${String.format("%.1f", pct)}%",
                            style = FinoraTextStyles.balanceDisplay,
                            color = color
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { pct / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = color,
                            trackColor = color.copy(alpha = 0.15f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kekurangan:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(CurrencyFormatter.formatRupiah(remaining), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        if (currentGoal.deadline != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Deadline:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(DateUtils.formatFullDate(currentGoal.deadline), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tabungan per bulan:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${CurrencyFormatter.formatRupiah(monthlySavingEstimate)} / bulan", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons for Funds
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FinoraPrimaryButton(
                        text = "+ Tabung Dana",
                        onClick = { showAddFundsDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                    FinoraSecondaryButton(
                        text = "− Tarik Dana",
                        onClick = { showWithdrawFundsDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
