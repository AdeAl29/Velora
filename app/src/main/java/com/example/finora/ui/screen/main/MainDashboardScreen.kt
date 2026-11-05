package com.example.finora.ui.screen.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
    onNavigateToTransfer: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToRecurring: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = selectedTabIndex != 0) {
        selectedTabIndex = 0
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Aksi Cepat",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Pilih pencatatan yang ingin kamu tambahkan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { showAddSheet = false },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Tutup",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                QuickAddOption(
                    icon = Icons.Rounded.TrendingDown,
                    title = "Pengeluaran Baru",
                    subtitle = "Catat belanja, makanan, transportasi, atau struk AI",
                    color = FinoraExpense,
                    onClick = {
                        showAddSheet = false
                        onNavigateToAddTransaction("EXPENSE_VARIABLE")
                    }
                )

                QuickAddOption(
                    icon = Icons.Rounded.TrendingUp,
                    title = "Pemasukan Baru",
                    subtitle = "Catat gaji, bonus, proyek freelance, atau hadiah",
                    color = FinoraIncome,
                    onClick = {
                        showAddSheet = false
                        onNavigateToAddTransaction("INCOME")
                    }
                )

                QuickAddOption(
                    icon = Icons.Rounded.Repeat,
                    title = "Pengeluaran Tetap Rutin",
                    subtitle = "Catat sewa kos, cicilan, listrik, internet bulanan",
                    color = FinoraSecondary,
                    onClick = {
                        showAddSheet = false
                        onNavigateToAddTransaction("EXPENSE_FIXED")
                    }
                )

                QuickAddOption(
                    icon = Icons.Rounded.SwapHoriz,
                    title = "Transfer Antar Rekening",
                    subtitle = "Pindahkan dana antar rekening tanpa dihitung pengeluaran",
                    color = Color(0xFF818CF8),
                    onClick = {
                        showAddSheet = false
                        onNavigateToTransfer()
                    }
                )

                QuickAddOption(
                    icon = Icons.Rounded.Flag,
                    title = "Target Keuangan Baru",
                    subtitle = "Buat tabungan impian baru beserta estimasi bulanan",
                    color = Color(0xFFFBBF24),
                    onClick = {
                        showAddSheet = false
                        onNavigateToAddGoal()
                    }
                )
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            // Content area that extends behind the floating iOS bottom bar
            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (fadeIn(animationSpec = tween(220)) +
                        slideInHorizontally(animationSpec = tween(240)) { (it * 0.06f * direction).toInt() })
                        .togetherWith(
                            fadeOut(animationSpec = tween(160)) +
                                slideOutHorizontally(animationSpec = tween(200)) { (-it * 0.04f * direction).toInt() }
                        )
                },
                label = "main_tab_transition",
                modifier = Modifier.fillMaxSize()
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> {
                        val homeViewModel: HomeViewModel = hiltViewModel()
                        HomeScreen(
                            viewModel = homeViewModel,
                            onNavigateToTransactionDetail = onNavigateToTransactionDetail,
                            onNavigateToAllTransactions = { selectedTabIndex = 1 },
                            onNavigateToAllGoals = { selectedTabIndex = 3 },
                            onNavigateToGoalDetail = onNavigateToGoalDetail,
                            onNavigateToSearch = onNavigateToSearch,
                            onNavigateToWallets = onNavigateToWallets,
                            onNavigateToTransfer = onNavigateToTransfer,
                            onNavigateToBudgets = onNavigateToBudgets,
                            onNavigateToBills = onNavigateToBills,
                            onNavigateToCalendar = onNavigateToCalendar
                        )
                    }
                    1 -> {
                        val transactionListViewModel: TransactionListViewModel = hiltViewModel()
                        TransactionListScreen(
                            viewModel = transactionListViewModel,
                            onNavigateToDetail = onNavigateToTransactionDetail,
                            onNavigateToAdd = { onNavigateToAddTransaction(null) }
                        )
                    }
                    2 -> {
                        val statisticsViewModel: StatisticsViewModel = hiltViewModel()
                        StatisticsScreen(
                            viewModel = statisticsViewModel,
                            onNavigateBack = { selectedTabIndex = 0 }
                        )
                    }
                    3 -> {
                        val goalViewModel: GoalViewModel = hiltViewModel()
                        GoalListScreen(
                            viewModel = goalViewModel,
                            onNavigateBack = { selectedTabIndex = 0 },
                            onNavigateToAddGoal = onNavigateToAddGoal,
                            onNavigateToGoalDetail = onNavigateToGoalDetail
                        )
                    }
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

            // Floating iOS Glassmorphic Bottom Bar overlaid at the bottom
            FinoraBottomNav(
                selectedIndex = selectedTabIndex,
                onItemSelected = { selectedTabIndex = it },
                onAddClick = { showAddSheet = true },
                isAddSheetOpen = showAddSheet,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
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
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
