package com.example.finora.ui.screen.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finora.core.util.CurrencyFormatter
import com.example.finora.core.util.DateUtils
import com.example.finora.ui.components.FinoraCategoryIcon
import com.example.finora.ui.components.FinoraPrimaryButton
import com.example.finora.ui.theme.*
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(
    viewModel: GoalViewModel,
    onNavigateBack: () -> Unit,
    goalId: Long? = null,
    modifier: Modifier = Modifier
) {
    val addState by viewModel.addState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(goalId) {
        if (goalId != null && goalId > 0) {
            viewModel.loadGoalForEdit(goalId)
        } else {
            viewModel.resetAddState()
        }
    }

    LaunchedEffect(addState.isSaved) {
        if (addState.isSaved) {
            onNavigateBack()
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = addState.deadline ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { utcMillis ->
                            val localDate = Instant.ofEpochMilli(utcMillis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            viewModel.setDeadline(DateUtils.toEpochMillis(localDate))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Pilih", fontWeight = FontWeight.Bold)
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

    val availableIcons = listOf("laptop", "emergency", "travel", "housing", "transport", "education", "target", "gift")
    val availableColors = listOf("#6C63FF", "#38BDF8", "#34D399", "#FBBF24", "#F472B6", "#FB923C", "#818CF8", "#F87171")

    val targetLong = addState.targetAmount.toLongOrNull() ?: 0L
    val currentLong = addState.currentAmount.toLongOrNull() ?: 0L
    val progress = if (targetLong > 0) (currentLong.toFloat() / targetLong).coerceIn(0f, 1f) else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (addState.isEditing) "Edit Tujuan" else "Tujuan Baru", style = MaterialTheme.typography.titleLarge) },
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
            // Live Preview Card
            val currentColor = getCategoryColor(addState.colorHex)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(currentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            FinoraCategoryIcon(iconName = addState.iconName, color = currentColor, size = 30.dp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = addState.name.ifBlank { "Nama Tujuan Finansial" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${CurrencyFormatter.formatRupiah(currentLong)} / ${CurrencyFormatter.formatRupiah(targetLong)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.0f", progress * 100)}%",
                            style = MaterialTheme.typography.titleSmall,
                            color = currentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(currentColor.copy(alpha = 0.12f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(currentColor, currentColor.copy(alpha = 0.7f))
                                    )
                                )
                        )
                    }

                    if (addState.deadline != null && targetLong > currentLong) {
                        val remaining = targetLong - currentLong
                        val monthsLeft = DateUtils.monthsUntil(addState.deadline!!).coerceAtLeast(1L)
                        val monthlyRecommended = remaining / monthsLeft
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Rekomendasi tabungan: ${CurrencyFormatter.formatRupiah(monthlyRecommended)} / bulan",
                            style = MaterialTheme.typography.labelSmall,
                            color = currentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Name
            OutlinedTextField(
                value = addState.name,
                onValueChange = viewModel::setName,
                label = { Text("Nama Tujuan (contoh: Beli MacBook, DP Rumah)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Target Amount
            OutlinedTextField(
                value = addState.targetAmount,
                onValueChange = viewModel::setTargetAmount,
                label = { Text("Target Dana (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Initial Amount
            OutlinedTextField(
                value = addState.currentAmount,
                onValueChange = viewModel::setCurrentAmount,
                label = { Text("Dana Terkumpul Saat Ini (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Target Deadline Selector
            Text("Target Tanggal Tercapai", style = FinoraTextStyles.sectionTitle, color = MaterialTheme.colorScheme.onBackground)
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
                            .clip(CircleShape)
                            .background(currentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Event,
                            contentDescription = null,
                            tint = currentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Batas Waktu Target",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val deadlineMillis = addState.deadline
                        Text(
                            text = if (deadlineMillis != null) {
                                val daysLeft = DateUtils.daysUntil(deadlineMillis)
                                "${DateUtils.formatFullDate(deadlineMillis)} (${if (daysLeft > 0) "$daysLeft hari lagi" else "Hari ini"})"
                            } else {
                                "Tanpa batas waktu (Fleksibel)"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (addState.deadline != null) {
                        IconButton(
                            onClick = { viewModel.setDeadline(null) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Hapus batas waktu",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Pilih",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = FinoraPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Icon Selector
            Text("Pilih Ikon", style = FinoraTextStyles.sectionTitle, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                availableIcons.forEach { iconName ->
                    val isSelected = addState.iconName == iconName
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) currentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .clickable { viewModel.setIconName(iconName) },
                        contentAlignment = Alignment.Center
                    ) {
                        FinoraCategoryIcon(iconName = iconName, color = if (isSelected) currentColor else MaterialTheme.colorScheme.onSurfaceVariant, size = 22.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Color Selector
            Text("Pilih Warna", style = FinoraTextStyles.sectionTitle, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                availableColors.forEach { colorHex ->
                    val color = getCategoryColor(colorHex)
                    val isSelected = addState.colorHex.equals(colorHex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { viewModel.setColorHex(colorHex) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Priority Selector
            Text("Tingkat Prioritas", style = FinoraTextStyles.sectionTitle, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1 to "Tinggi", 2 to "Sedang", 3 to "Rendah").forEach { (level, title) ->
                    val isSelected = addState.priority == level
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setPriority(level) },
                        label = { Text(title) },
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
                text = if (addState.isEditing) "Simpan Perubahan" else "Buat Tujuan Keuangan",
                onClick = viewModel::saveGoal,
                isLoading = addState.isLoading,
                icon = Icons.Rounded.Check
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
