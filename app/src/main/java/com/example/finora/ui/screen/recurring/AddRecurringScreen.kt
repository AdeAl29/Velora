package com.example.finora.ui.screen.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finora.core.model.Frequency
import com.example.finora.ui.components.FinoraCategoryIcon
import com.example.finora.ui.components.FinoraPrimaryButton
import com.example.finora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecurringScreen(
    viewModel: RecurringViewModel,
    onNavigateBack: () -> Unit,
    recurringId: Long? = null,
    modifier: Modifier = Modifier
) {
    val addState by viewModel.addState.collectAsStateWithLifecycle()

    LaunchedEffect(recurringId) {
        if (recurringId != null && recurringId > 0) {
            viewModel.loadForEdit(recurringId)
        } else {
            viewModel.resetAddState()
        }
    }

    LaunchedEffect(addState.isSaved) {
        if (addState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (addState.isEditing) "Edit Pengeluaran Tetap" else "Tambah Pengeluaran Tetap", style = MaterialTheme.typography.titleLarge) },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Name
            OutlinedTextField(
                value = addState.name,
                onValueChange = viewModel::setName,
                label = { Text("Nama Pengeluaran (contoh: Kos, WiFi Indihome)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Amount
            OutlinedTextField(
                value = addState.amount,
                onValueChange = viewModel::setAmount,
                label = { Text("Nominal (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Frequency
            Text("Frekuensi Pembayaran", style = FinoraTextStyles.sectionTitle, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Frequency.entries.forEach { freq ->
                    val isSelected = addState.frequency == freq
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFrequency(freq) },
                        label = { Text(freq.toDisplayString()) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tanggal Mulai (Default: Hari ini, tap untuk custom)
            var showDatePicker by remember { mutableStateOf(false) }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = addState.startDate
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { utcMillis ->
                                val localDate = java.time.Instant.ofEpochMilli(utcMillis)
                                    .atZone(java.time.ZoneOffset.UTC)
                                    .toLocalDate()
                                val localEpoch = com.example.finora.core.util.DateUtils.toEpochMillis(localDate)
                                viewModel.setStartDate(localEpoch)
                            }
                            showDatePicker = false
                        }) {
                            Text("Pilih")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Batal")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Text("Tanggal Mulai", style = FinoraTextStyles.sectionTitle, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showDatePicker = true },
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(FinoraPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = null,
                            tint = FinoraPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tanggal Mulai",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val isToday = com.example.finora.core.util.DateUtils.isSameDay(addState.startDate, com.example.finora.core.util.DateUtils.today())
                        Text(
                            text = if (isToday) "Hari ini (${com.example.finora.core.util.DateUtils.formatFullDate(addState.startDate)})" else com.example.finora.core.util.DateUtils.formatFullDate(addState.startDate),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Ubah",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        color = FinoraPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Category Selection
            Text("Kategori", style = FinoraTextStyles.sectionTitle, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.heightIn(max = 240.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                items(addState.categories, key = { it.id }) { cat ->
                    val isSelected = cat.id == addState.selectedCategoryId
                    val color = getCategoryColor(cat.colorHex)
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .clickable { viewModel.setCategory(cat.id) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(color.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            FinoraCategoryIcon(iconName = cat.iconName, color = color, size = 18.dp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cat.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Wallet Selection
            Text("Rekening Pembayaran", style = FinoraTextStyles.sectionTitle, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                addState.wallets.forEach { w ->
                    val isSelected = w.id == addState.selectedWalletId
                    val color = getCategoryColor(w.colorHex)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setWallet(w.id) },
                        label = { Text(w.name) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (addState.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = addState.errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            FinoraPrimaryButton(
                text = if (addState.isEditing) "Simpan Perubahan" else "Simpan Pengeluaran Tetap",
                onClick = viewModel::saveRecurring,
                isLoading = addState.isLoading,
                icon = Icons.Rounded.Check
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
