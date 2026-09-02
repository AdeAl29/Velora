package com.example.finora.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.finora.core.util.CurrencyFormatter
import com.example.finora.core.util.DateUtils
import com.example.finora.ui.theme.*

@Composable
fun FinoraTransactionCard(
    categoryName: String,
    categoryIconName: String,
    categoryColorHex: String,
    note: String?,
    amount: Long,
    isIncome: Boolean,
    date: Long,
    walletName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = getCategoryColor(categoryColorHex)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(categoryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                FinoraCategoryIcon(
                    iconName = categoryIconName,
                    color = categoryColor,
                    size = 24.dp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Transaction info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note?.takeIf { it.isNotBlank() } ?: categoryName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$walletName • ${DateUtils.formatDate(date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Amount
            Text(
                text = CurrencyFormatter.formatSigned(amount, isIncome),
                style = FinoraTextStyles.currencyAmount,
                color = if (isIncome) FinoraColors.income() else FinoraColors.expense(),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun FinoraTransactionCardCompact(
    categoryName: String,
    categoryIconName: String,
    categoryColorHex: String,
    amount: Long,
    isIncome: Boolean,
    note: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = getCategoryColor(categoryColorHex)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(categoryColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            FinoraCategoryIcon(
                iconName = categoryIconName,
                color = categoryColor,
                size = 20.dp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = note?.takeIf { it.isNotBlank() } ?: categoryName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = CurrencyFormatter.formatSigned(amount, isIncome),
            style = FinoraTextStyles.currencyAmount.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize),
            color = if (isIncome) FinoraColors.income() else FinoraColors.expense()
        )
    }
}
