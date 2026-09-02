package com.example.finora.ui.screen.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finora.core.util.CurrencyFormatter
import com.example.finora.ui.components.*
import com.example.finora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalListScreen(
    viewModel: GoalViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddGoal: () -> Unit,
    onNavigateToGoalDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState by viewModel.listState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tujuan Keuangan", style = MaterialTheme.typography.titleLarge) },
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
                onClick = onNavigateToAddGoal,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Tujuan Baru", fontWeight = FontWeight.SemiBold) }
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
                // Header card
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
                                    text = "TOTAL PROGRESS DANA TERKUMPUL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${CurrencyFormatter.formatRupiah(listState.totalCollected)} / ${CurrencyFormatter.formatRupiah(listState.totalTarget)}",
                                    style = FinoraTextStyles.balanceMedium,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                val pct = if (listState.totalTarget > 0)
                                    (listState.totalCollected.toFloat() / listState.totalTarget * 100).coerceIn(0f, 100f)
                                else 0f
                                LinearProgressIndicator(
                                    progress = { pct / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = Color(0xFF6EE7B7),
                                    trackColor = Color.White.copy(alpha = 0.3f)
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
                            text = { Text("Aktif (${listState.activeGoals.size})", fontWeight = FontWeight.Medium) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Tercapai (${listState.completedGoals.size})", fontWeight = FontWeight.Medium) }
                        )
                    }
                }

                val currentList = if (selectedTab == 0) listState.activeGoals else listState.completedGoals

                if (currentList.isEmpty()) {
                    item {
                        FinoraEmptyState(
                            type = EmptyStateType.GOAL,
                            title = if (selectedTab == 0) "Belum Ada Tujuan Aktif" else "Belum Ada Tujuan Tercapai",
                            message = if (selectedTab == 0) "Rencanakan impianmu, seperti membeli laptop, dana darurat, atau liburan."
                            else "Tujuan yang telah mencapai target 100% akan muncul di sini.",
                            actionText = if (selectedTab == 0) "+ Buat Tujuan" else null,
                            onAction = if (selectedTab == 0) onNavigateToAddGoal else null
                        )
                    }
                } else {
                    items(currentList, key = { it.id }) { goal ->
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

                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }
}
