package com.example.finora.ui.screen.bill

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finora.ui.components.FinoraCategoryIcon
import com.example.finora.ui.components.FinoraPrimaryButton
import com.example.finora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBillScreen(
    viewModel: BillViewModel,
    onNavigateBack: () -> Unit,
    billId: Long? = null,
    modifier: Modifier = Modifier
) {
    val addState by viewModel.addState.collectAsStateWithLifecycle()

    LaunchedEffect(billId) {
        if (billId != null && billId > 0) {
            viewModel.loadBillForEdit(billId)
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
                title = { Text(if (addState.isEditing) "Edit Tagihan" else "Tambah Tagihan", style = MaterialTheme.typography.titleLarge) },
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
                label = { Text("Nama Tagihan (contoh: Kos, Internet, Listrik)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Amount
            OutlinedTextField(
                value = addState.amount,
                onValueChange = viewModel::setAmount,
                label = { Text("Nominal Tagihan (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Due Date of month (1 - 31)
            Text("Tanggal Jatuh Tempo (Setiap Bulan)", style = FinoraTextStyles.sectionTitle, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items((1..31).toList()) { day ->
                    val isSelected = addState.dueDate == day
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { viewModel.setDueDate(day) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$day",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Reminder Days
            Text("Pengingat Sebelum Jatuh Tempo", style = FinoraTextStyles.sectionTitle, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(1 to "1 Hari", 3 to "3 Hari", 5 to "5 Hari", 7 to "7 Hari").forEach { (days, label) ->
                    val isSelected = addState.reminderDaysBefore == days
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setReminderDays(days) },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
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
                text = if (addState.isEditing) "Simpan Perubahan" else "Simpan Tagihan",
                onClick = viewModel::saveBill,
                isLoading = addState.isLoading,
                icon = Icons.Rounded.Check
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
