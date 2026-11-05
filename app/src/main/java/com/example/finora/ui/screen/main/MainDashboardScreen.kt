package com.example.finora.ui.screen.main

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
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
import kotlinx.coroutines.delay
import kotlin.math.abs

private val IosDecelerateEasing = CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)

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

    // iOS 26 Liquid Glass Dynamic States
    var isBottomBarMinimized by rememberSaveable { mutableStateOf(false) }
    var isBottomBarIdle by remember { mutableStateOf(false) }
    var interactionTick by remember { mutableLongStateOf(0L) }
    var lastInteractionUptime by remember { mutableLongStateOf(0L) }

    // Throttled interaction wakeup so 120Hz scrolling never triggers unnecessary recompositions
    val notifyUserInteraction = remember {
        {
            if (isBottomBarIdle) {
                isBottomBarIdle = false
            }
            val now = SystemClock.uptimeMillis()
            if (now - lastInteractionUptime > 400L) {
                lastInteractionUptime = now
                interactionTick++
            }
        }
    }

    // Real-time GraphicsLayer capturing content underneath for iOS Liquid Glass backdrop blur
    val backdropGraphicsLayer = rememberGraphicsLayer()
    var rootOffsetInWindow by remember { mutableStateOf(Offset.Zero) }

    // Reset minimize & idle state whenever the user switches tabs
    LaunchedEffect(selectedTabIndex) {
        isBottomBarMinimized = false
        isBottomBarIdle = false
        interactionTick++
    }

    // Idle / untouched timer (3.5 seconds without touch or scroll -> Ambient Liquid Glass)
    LaunchedEffect(interactionTick, showAddSheet) {
        isBottomBarIdle = false
        if (!showAddSheet) {
            delay(3500L)
            isBottomBarIdle = true
        }
    }

    // iOS `.tabBarMinimizeBehavior(.onScrollDown)` detector across all LazyColumns
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            private var scrollDeltaAccumulator = 0f

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val deltaY = consumed.y
                if (deltaY != 0f) {
                    notifyUserInteraction()
                }

                when {
                    // User is scrolling DOWN (content moves up) -> accumulate negative delta to minimize
                    deltaY < -0.5f -> {
                        if (scrollDeltaAccumulator > 0f) scrollDeltaAccumulator = 0f
                        scrollDeltaAccumulator += deltaY
                        if (scrollDeltaAccumulator < -34f && !isBottomBarMinimized) {
                            isBottomBarMinimized = true
                            scrollDeltaAccumulator = 0f
                        }
                    }
                    // User is scrolling UP (content moves down) -> accumulate positive delta to expand
                    deltaY > 0.5f -> {
                        if (scrollDeltaAccumulator < 0f) scrollDeltaAccumulator = 0f
                        scrollDeltaAccumulator += deltaY
                        if (scrollDeltaAccumulator > 22f && isBottomBarMinimized) {
                            isBottomBarMinimized = false
                            scrollDeltaAccumulator = 0f
                        }
                    }
                    // User pulled down while already at the very top of the list -> ensure expanded
                    available.y > 10f -> {
                        if (isBottomBarMinimized) {
                            isBottomBarMinimized = false
                        }
                        scrollDeltaAccumulator = 0f
                    }
                }
                return Offset.Zero
            }
        }
    }

    BackHandler(enabled = selectedTabIndex != 0) {
        selectedTabIndex = 0
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
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
                    icon = Icons.AutoMirrored.Rounded.TrendingDown,
                    title = "Pengeluaran Baru",
                    subtitle = "Catat belanja, makanan, transportasi, atau struk AI",
                    color = FinoraExpense,
                    onClick = {
                        showAddSheet = false
                        onNavigateToAddTransaction("EXPENSE_VARIABLE")
                    }
                )

                QuickAddOption(
                    icon = Icons.AutoMirrored.Rounded.TrendingUp,
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
                .nestedScroll(nestedScrollConnection)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitPointerEvent(pass = PointerEventPass.Initial)
                        notifyUserInteraction()
                    }
                }
        ) {
            // Content area recorded into backdropGraphicsLayer so the floating iOS Liquid Glass
            // bottom bar refracts and blurs the actual scrolling content in real time.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coords ->
                        val pos = coords.positionInWindow()
                        if (abs(pos.x - rootOffsetInWindow.x) > 0.5f ||
                            abs(pos.y - rootOffsetInWindow.y) > 0.5f
                        ) {
                            rootOffsetInWindow = pos
                        }
                    }
                    .drawWithContent {
                        backdropGraphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(backdropGraphicsLayer)
                    }
            ) {
                AnimatedContent(
                    targetState = selectedTabIndex,
                    transitionSpec = {
                        val direction = if (targetState > initialState) 1 else -1
                        (fadeIn(animationSpec = tween(280, easing = IosDecelerateEasing)) +
                            scaleIn(
                                initialScale = 0.985f,
                                animationSpec = tween(300, easing = IosDecelerateEasing)
                            ) +
                            slideInHorizontally(
                                animationSpec = tween(300, easing = IosDecelerateEasing)
                            ) { (it * 0.045f * direction).toInt() })
                            .togetherWith(
                                fadeOut(animationSpec = tween(190, easing = IosDecelerateEasing)) +
                                    scaleOut(
                                        targetScale = 0.985f,
                                        animationSpec = tween(210, easing = IosDecelerateEasing)
                                    ) +
                                    slideOutHorizontally(
                                        animationSpec = tween(210, easing = IosDecelerateEasing)
                                    ) { (-it * 0.035f * direction).toInt() }
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
            }

            // Floating iOS 26 Liquid Glass Bottom Bar overlaid at the bottom
            FinoraBottomNav(
                selectedIndex = selectedTabIndex,
                onItemSelected = {
                    selectedTabIndex = it
                    isBottomBarMinimized = false
                    notifyUserInteraction()
                },
                onAddClick = {
                    notifyUserInteraction()
                    showAddSheet = true
                },
                isAddSheetOpen = showAddSheet,
                isMinimized = isBottomBarMinimized,
                isIdle = isBottomBarIdle,
                onExpandRequest = {
                    isBottomBarMinimized = false
                    notifyUserInteraction()
                },
                backdropGraphicsLayer = backdropGraphicsLayer,
                rootOffsetInWindow = rootOffsetInWindow,
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
