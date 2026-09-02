package com.example.finora.ui.screen.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finora.ui.components.FinoraBottomNav
import com.example.finora.ui.screen.goal.GoalListScreen
import com.example.finora.ui.screen.goal.GoalViewModel
import com.example.finora.ui.screen.home.HomeScreen
import com.example.finora.ui.screen.home.HomeViewModel
import com.example.finora.ui.screen.more.MoreScreen
import com.example.finora.ui.screen.statistics.StatisticsScreen
import com.example.finora.ui.screen.statistics.StatisticsViewModel
import com.example.finora.ui.screen.transaction.TransactionListScreen
import com.example.finora.ui.screen.transaction.TransactionListViewModel
import com.example.finora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    onNavigateToAddTransaction: (String?) -> Unit,
    onNavigateToTransactionDetail: (Long) -> Unit,
    onNavigateToGoalDetail: (Long) -> Unit,
    onNavigateToAddGoal: () -> Unit,
    onNavigateToWallets: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToRecurring: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddSheet by remember { mutableStateOf(false) }

    val homeViewModel: HomeViewModel = hiltViewModel()
    val transactionListViewModel: TransactionListViewModel = hiltViewModel()
    val statisticsViewModel: StatisticsViewModel = hiltViewModel()
    val goalViewModel: GoalViewModel = hiltViewModel()

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Catat Transaksi Cepat",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                QuickAddOption(
                    icon = Icons.Rounded.TrendingDown,
                    title = "Pengeluaran Baru",
                    subtitle = "Catat belanja, makanan, transportasi, dll",
                    color = FinoraExpense,
                    onClick = {
                        showAddSheet = false
                        onNavigateToAddTransaction("EXPENSE_VARIABLE")
                    }
                )

                QuickAddOption(
                    icon = Icons.Rounded.TrendingUp,
                    title = "Pemasukan Baru",
                    subtitle = "Catat gaji, bonus, freelance, hadiah",
                    color = FinoraIncome,
                    onClick = {
                        showAddSheet = false
                        onNavigateToAddTransaction("INCOME")
                    }
                )

                QuickAddOption(
                    icon = Icons.Rounded.Repeat,
                    title = "Pengeluaran Tetap Rutin",
                    subtitle = "Catat sewa kos, cicilan, listrik rutin",
                    color = FinoraSecondary,
                    onClick = {
                        showAddSheet = false
                        onNavigateToAddTransaction("EXPENSE_FIXED")
                    }
                )

                QuickAddOption(
                    icon = Icons.Rounded.SwapHoriz,
                    title = "Transfer Saldo",
                    subtitle = "Pindahkan dana antar rekening tanpa dihitung pengeluaran",
                    color = Color(0xFF818CF8),
                    onClick = {
                        showAddSheet = false
                        onNavigateToWallets()
                    }
                )
            }
        }
    }

    Scaffold(
        bottomBar = {
            FinoraBottomNav(
                selectedIndex = selectedTabIndex,
                onItemSelected = { selectedTabIndex = it },
                onAddClick = { showAddSheet = true }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = 12.dp)
        ) {
            when (selectedTabIndex) {
                0 -> HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToTransactionDetail = onNavigateToTransactionDetail,
                    onNavigateToAllTransactions = { selectedTabIndex = 1 },
                    onNavigateToAllGoals = { selectedTabIndex = 3 },
                    onNavigateToGoalDetail = onNavigateToGoalDetail,
                    onNavigateToSearch = onNavigateToSearch
                )
                1 -> TransactionListScreen(
                    viewModel = transactionListViewModel,
                    onNavigateToDetail = onNavigateToTransactionDetail,
                    onNavigateToAdd = { onNavigateToAddTransaction(null) }
                )
                2 -> StatisticsScreen(
                    viewModel = statisticsViewModel,
                    onNavigateBack = { selectedTabIndex = 0 }
                )
                3 -> GoalListScreen(
                    viewModel = goalViewModel,
                    onNavigateBack = { selectedTabIndex = 0 },
                    onNavigateToAddGoal = onNavigateToAddGoal,
                    onNavigateToGoalDetail = onNavigateToGoalDetail
                )
                4 -> MoreScreen(
                    onNavigateToWallets = onNavigateToWallets,
                    onNavigateToBudgets = onNavigateToBudgets,
                    onNavigateToBills = onNavigateToBills,
                    onNavigateToRecurring = onNavigateToRecurring,
                    onNavigateToCalendar = onNavigateToCalendar,
                    onNavigateToReports = onNavigateToReports,
                    onNavigateToSettings = onNavigateToSettings
                )
            }
        }
    }
}

@Composable
private fun QuickAddOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
