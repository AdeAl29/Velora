package com.example.finora.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.finora.core.util.CurrencyFormatter
import com.example.finora.ui.theme.*

@Composable
fun FinoraBudgetCard(
    categoryName: String,
    categoryIconName: String,
    categoryColorHex: String,
    budgetAmount: Long,
    spentAmount: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = getCategoryColor(categoryColorHex)
    val remaining = budgetAmount - spentAmount
    val percentage = if (budgetAmount > 0) (spentAmount.toFloat() / budgetAmount * 100) else 0f
    val clampedPercentage = percentage.coerceIn(0f, 100f)

    val animatedProgress by animateFloatAsState(
        targetValue = clampedPercentage / 100f,
        animationSpec = tween(800, easing = EaseOutCubic)
    )

    val statusColor = when {
        percentage >= 100 -> FinoraExpense
        percentage >= 90 -> FinoraExpense.copy(alpha = 0.8f)
        percentage >= 75 -> FinoraWarning
        percentage >= 50 -> FinoraWarning.copy(alpha = 0.7f)
        else -> FinoraIncome
    }

    androidx.compose.material3.Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular progress
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 6f
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    // Background track
                    drawArc(
                        color = categoryColor.copy(alpha = 0.15f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress
                    drawArc(
                        color = statusColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Icon in center
                FinoraCategoryIcon(
                    iconName = categoryIconName,
                    color = categoryColor,
                    size = 22.dp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${CurrencyFormatter.formatRupiah(spentAmount)} / ${CurrencyFormatter.formatRupiah(budgetAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format("%.0f", clampedPercentage)}%",
                    style = MaterialTheme.typography.titleSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (remaining >= 0) "Sisa ${CurrencyFormatter.formatCompact(remaining)}" else "Lebih ${CurrencyFormatter.formatCompact(-remaining)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (remaining >= 0) MaterialTheme.colorScheme.onSurfaceVariant else FinoraExpense
                )
            }
        }
    }
}

@Composable
fun FinoraGoalCard(
    name: String,
    iconName: String,
    colorHex: String,
    targetAmount: Long,
    currentAmount: Long,
    deadline: Long?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (targetAmount > 0) (currentAmount.toFloat() / targetAmount).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800, easing = EaseOutCubic)
    )
    val color = getCategoryColor(colorHex)

    androidx.compose.material3.Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    FinoraCategoryIcon(
                        iconName = iconName,
                        color = color,
                        size = 22.dp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${CurrencyFormatter.formatCompact(currentAmount)} / ${CurrencyFormatter.formatCompact(targetAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${String.format("%.1f", progress * 100)}%",
                    style = MaterialTheme.typography.titleSmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(color, color.copy(alpha = 0.7f))
                            )
                        )
                )
            }

            if (deadline != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val daysLeft = com.example.finora.core.util.DateUtils.daysUntil(deadline)
                Text(
                    text = if (daysLeft > 0) "Target ${com.example.finora.core.util.DateUtils.formatFullDate(deadline)} • $daysLeft hari lagi"
                    else "Target sudah lewat",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (daysLeft > 0) MaterialTheme.colorScheme.onSurfaceVariant else FinoraExpense
                )
            }
        }
    }
}

@Composable
fun FinoraWalletCard(
    name: String,
    iconName: String,
    colorHex: String,
    balance: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = getCategoryColor(colorHex)

    androidx.compose.material3.Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                FinoraCategoryIcon(iconName = iconName, color = color, size = 20.dp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = CurrencyFormatter.formatRupiah(balance),
                style = FinoraTextStyles.currencyAmount,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun FinoraBillCard(
    name: String,
    amount: Long,
    dueDate: Int,
    isPaid: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = if (isPaid) FinoraIncome else {
        val today = java.time.LocalDate.now().dayOfMonth
        if (dueDate - today <= 3 && dueDate >= today) FinoraWarning
        else if (dueDate < today) FinoraExpense
        else MaterialTheme.colorScheme.onSurfaceVariant
    }

    androidx.compose.material3.Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Due date indicator
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(statusColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dueDate.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isPaid) "Sudah dibayar" else "Jatuh tempo tanggal $dueDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor
                )
            }

            Text(
                text = CurrencyFormatter.formatRupiah(amount),
                style = FinoraTextStyles.currencyAmount,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
