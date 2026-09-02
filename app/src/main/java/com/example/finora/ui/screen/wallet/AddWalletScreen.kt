package com.example.finora.ui.screen.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
fun AddWalletScreen(
    viewModel: WalletViewModel,
    onNavigateBack: () -> Unit,
    walletId: Long? = null,
    modifier: Modifier = Modifier
) {
    val addState by viewModel.addState.collectAsStateWithLifecycle()

    LaunchedEffect(walletId) {
        if (walletId != null && walletId > 0) {
            viewModel.loadWalletForEdit(walletId)
        } else {
            viewModel.resetAddState()
        }
    }

    LaunchedEffect(addState.isSaved) {
        if (addState.isSaved) {
            onNavigateBack()
        }
    }

    val availableIcons = listOf("bank", "cash", "ewallet", "salary", "investment", "technology", "pocket_money")
    val availableColors = listOf("#6C63FF", "#38BDF8", "#34D399", "#FBBF24", "#F472B6", "#FB923C", "#818CF8", "#F87171")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (addState.isEditing) "Edit Rekening" else "Tambah Rekening", style = MaterialTheme.typography.titleLarge) },
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
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    Column {
                        Text(
                            text = addState.name.ifBlank { "Nama Rekening" },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (addState.initialBalance.isBlank()) "Saldo Awal: Rp0" else "Saldo Awal: Rp${addState.initialBalance}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Name Field
            OutlinedTextField(
                value = addState.name,
                onValueChange = viewModel::setName,
                label = { Text("Nama Rekening (contoh: BCA, DANA, Cash)") },
                isError = addState.nameError != null,
                supportingText = addState.nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Initial Balance Field
            OutlinedTextField(
                value = addState.initialBalance,
                onValueChange = viewModel::setInitialBalance,
                label = { Text("Saldo Awal (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

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
                            .size(44.dp)
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
            Text("Pilih Warna Aksen", style = FinoraTextStyles.sectionTitle, color = MaterialTheme.colorScheme.onBackground)
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

            Spacer(modifier = Modifier.height(36.dp))

            FinoraPrimaryButton(
                text = if (addState.isEditing) "Simpan Perubahan" else "Tambah Rekening",
                onClick = viewModel::saveWallet,
                isLoading = addState.isLoading,
                icon = Icons.Rounded.Check
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
