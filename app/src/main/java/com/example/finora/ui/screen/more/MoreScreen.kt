package com.example.finora.ui.screen.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.finora.ui.theme.FinoraTextStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigateToWallets: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToRecurring: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu Lainnya", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Pengelolaan Finansial", style = FinoraTextStyles.sectionTitle, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        MoreMenuItem(
                            icon = Icons.Rounded.AccountBalanceWallet,
                            title = "Rekening & Dompet Digital",
                            subtitle = "Kelola rekening bank, cash, dan transfer saldo",
                            iconColor = Color(0xFF38BDF8),
                            onClick = onNavigateToWallets
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        MoreMenuItem(
                            icon = Icons.Rounded.PieChart,
                            title = "Anggaran Bulanan (Budget)",
                            subtitle = "Atur batas belanja per kategori",
                            iconColor = Color(0xFF34D399),
                            onClick = onNavigateToBudgets
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        MoreMenuItem(
                            icon = Icons.Rounded.ReceiptLong,
                            title = "Tagihan & Pengingat",
                            subtitle = "Jadwal bayar kos, listrik, internet, dan langganan",
                            iconColor = Color(0xFFFBBF24),
                            onClick = onNavigateToBills
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        MoreMenuItem(
                            icon = Icons.Rounded.Repeat,
                            title = "Pengeluaran Tetap / Berulang",
                            subtitle = "Catatan pengeluaran rutin otomatis",
                            iconColor = Color(0xFFFB7185),
                            onClick = onNavigateToRecurring
                        )
                    }
                }
            }

            item {
                Text("Wawasan & Analisis", style = FinoraTextStyles.sectionTitle, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        MoreMenuItem(
                            icon = Icons.Rounded.CalendarMonth,
                            title = "Kalender Keuangan",
                            subtitle = "Lihat catatan pemasukan dan pengeluaran harian",
                            iconColor = Color(0xFF818CF8),
                            onClick = onNavigateToCalendar
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        MoreMenuItem(
                            icon = Icons.Rounded.Assessment,
                            title = "Laporan & Ekspor Data",
                            subtitle = "Unduh laporan dalam format PDF atau CSV",
                            iconColor = Color(0xFF6C63FF),
                            onClick = onNavigateToReports
                        )
                    }
                }
            }

            item {
                Text("Aplikasi", style = FinoraTextStyles.sectionTitle, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        MoreMenuItem(
                            icon = Icons.Rounded.Settings,
                            title = "Pengaturan Umum",
                            subtitle = "Tema, mata uang, notifikasi, dan data sampel",
                            iconColor = MaterialTheme.colorScheme.primary,
                            onClick = onNavigateToSettings
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun MoreMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
