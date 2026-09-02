package com.example.finora.ui.screen.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finora.core.model.CurrencyInfo
import com.example.finora.ui.components.FinoraCategoryIcon
import com.example.finora.ui.components.FinoraPrimaryButton
import com.example.finora.ui.components.FinoraSecondaryButton
import com.example.finora.ui.theme.*

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onOnboardingFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onOnboardingFinished()
        }
    }

    if (!uiState.isSetupPhase) {
        OnboardingCarousel(
            currentStep = uiState.currentStep,
            onNext = viewModel::nextStep,
            onSkip = { viewModel.nextStep() }
        )
    } else {
        OnboardingSetupForm(
            uiState = uiState,
            viewModel = viewModel
        )
    }
}

@Composable
private fun OnboardingCarousel(
    currentStep: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val steps = listOf(
        OnboardingStepData(
            title = "Kelola Uangmu Lebih Baik",
            description = "Atur saldo, pantau arus kas masuk dan keluar secara real-time dengan tampilan modern.",
            icon = Icons.Rounded.AccountBalanceWallet,
            accentColor = FinoraPrimary
        ),
        OnboardingStepData(
            title = "Catat Pemasukan & Pengeluaran",
            description = "Pisahkan pengeluaran tetap dan variabel untuk memahami kebiasaan finansialmu sehari-hari.",
            icon = Icons.Rounded.ReceiptLong,
            accentColor = FinoraIncome
        ),
        OnboardingStepData(
            title = "Tentukan Tujuan Keuangan",
            description = "Wujudkan impian membeli gadget, dana darurat, hingga liburan dengan progress tracking otomatis.",
            icon = Icons.Rounded.Flag,
            accentColor = FinoraSecondary
        ),
        OnboardingStepData(
            title = "Pantau Perkembangan Finansialmu",
            description = "Dapatkan grafik interaktif, analisis kategori belanja, dan insight otomatis tanpa ribet.",
            icon = Icons.Rounded.AutoGraph,
            accentColor = FinoraWarning
        )
    )

    val currentData = steps[currentStep]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        // Skip Button
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onSkip) {
                Text("Lewati", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Center Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(currentData.accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = currentData.icon,
                    contentDescription = null,
                    tint = currentData.accentColor,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = currentData.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = currentData.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Step Indicator Dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                steps.indices.forEach { index ->
                    val isSelected = index == currentStep
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (isSelected) 24.dp else 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }

        // Bottom Button
        FinoraPrimaryButton(
            text = if (currentStep == steps.lastIndex) "Mulai Sekarang" else "Lanjutkan",
            onClick = onNext,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun OnboardingSetupForm(
    uiState: OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Setup Akun Aruphia",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Lengkapi beberapa informasi dasar untuk memulai pencatatan keuanganmu.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Name
        OutlinedTextField(
            value = uiState.userName,
            onValueChange = viewModel::setUserName,
            label = { Text("Nama Panggilan Anda") },
            placeholder = { Text("Contoh: Budi, Sarah") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Currency selection
        Text("Pilih Mata Uang", style = FinoraTextStyles.sectionTitle)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(CurrencyInfo.IDR, CurrencyInfo.USD, CurrencyInfo.SGD).forEach { curr ->
                val isSelected = uiState.currencyCode == curr.code
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setCurrencyCode(curr.code) },
                    label = { Text("${curr.symbol} (${curr.code})") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // First Wallet Setup
        Text("Rekening Pertama Anda", style = FinoraTextStyles.sectionTitle)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.walletName,
            onValueChange = viewModel::setWalletName,
            label = { Text("Nama Rekening/Dompet") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.initialBalance,
            onValueChange = viewModel::setInitialBalance,
            label = { Text("Saldo Awal (Rp)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = uiState.error!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        FinoraPrimaryButton(
            text = "Mulai Masuk ke Aruphia",
            onClick = viewModel::completeSetup,
            isLoading = uiState.isLoading
        )
    }
}

private data class OnboardingStepData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color
)
