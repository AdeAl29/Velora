package com.example.finora.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finora.core.util.CurrencyFormatter
import com.example.finora.ui.theme.*

@Composable
fun FinoraBalanceCard(
    totalBalance: Long,
    monthlyIncome: Long,
    monthlyExpense: Long,
    percentChange: Float,
    isBalanceHidden: Boolean,
    onToggleVisibility: () -> Unit,
    sparklineData: List<Float> = emptyList(),
    modifier: Modifier = Modifier
) {
    val animatedBalance by animateLongAsState(totalBalance)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            FinoraPrimary,
                            FinoraPrimary.copy(alpha = 0.85f),
                            FinoraSecondary.copy(alpha = 0.7f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.MAX_VALUE, Float.MAX_VALUE)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .clip(RoundedCornerShape(24.dp))
        ) {
            // Decorative background pattern
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                // Subtle circles
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = w * 0.4f,
                    center = Offset(w * 0.85f, h * 0.2f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.03f),
                    radius = w * 0.3f,
                    center = Offset(w * 0.1f, h * 0.9f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL SALDO",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.5.sp
                    )
                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isBalanceHidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = if (isBalanceHidden) "Tampilkan saldo" else "Sembunyikan saldo",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Balance amount
                Text(
                    text = if (isBalanceHidden) "Rp••••••••" else CurrencyFormatter.formatRupiah(animatedBalance),
                    style = FinoraTextStyles.balanceDisplay,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Percentage change
                if (!isBalanceHidden) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val isPositive = percentChange >= 0
                        Text(
                            text = "${if (isPositive) "↑" else "↓"} ${String.format("%.1f", kotlin.math.abs(percentChange))}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isPositive) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = " dari bulan lalu",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                // Sparkline
                if (sparklineData.isNotEmpty() && !isBalanceHidden) {
                    Spacer(modifier = Modifier.height(16.dp))
                    MiniSparkline(
                        data = sparklineData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Income / Expense summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BalanceSummaryItem(
                        label = "Pemasukan",
                        amount = monthlyIncome,
                        isIncome = true,
                        isHidden = isBalanceHidden
                    )
                    BalanceSummaryItem(
                        label = "Pengeluaran",
                        amount = monthlyExpense,
                        isIncome = false,
                        isHidden = isBalanceHidden
                    )
                }
            }
        }
    }
}

@Composable
private fun BalanceSummaryItem(
    label: String,
    amount: Long,
    isIncome: Boolean,
    isHidden: Boolean
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isIncome) Color(0xFF6EE7B7) else Color(0xFFFCA5A5))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isHidden) "Rp••••" else CurrencyFormatter.formatRupiah(amount),
            style = FinoraTextStyles.currencyAmount,
            color = Color.White
        )
    }
}

@Composable
private fun MiniSparkline(
    data: List<Float>,
    modifier: Modifier = Modifier
) {
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(1000, easing = EaseOutCubic))
    }

    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas
        val w = size.width
        val h = size.height
        val maxVal = data.max()
        val minVal = data.min()
        val range = if (maxVal == minVal) 1f else maxVal - minVal

        val points = data.mapIndexed { index, value ->
            val x = (index.toFloat() / (data.size - 1)) * w
            val y = h - ((value - minVal) / range) * h * 0.8f - h * 0.1f
            Offset(x, y)
        }

        val animatedPoints = points.map { point ->
            Offset(point.x * animationProgress.value, point.y)
        }

        // Draw gradient fill under the line
        val fillPath = Path().apply {
            moveTo(0f, h)
            animatedPoints.forEachIndexed { i, pt ->
                if (i == 0) lineTo(pt.x, pt.y)
                else {
                    val prev = animatedPoints[i - 1]
                    val cpX = (prev.x + pt.x) / 2
                    cubicTo(cpX, prev.y, cpX, pt.y, pt.x, pt.y)
                }
            }
            lineTo(animatedPoints.last().x, h)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.15f),
                    Color.White.copy(alpha = 0.0f)
                )
            ),
            style = Fill
        )

        // Draw the line
        val linePath = Path().apply {
            animatedPoints.forEachIndexed { i, pt ->
                if (i == 0) moveTo(pt.x, pt.y)
                else {
                    val prev = animatedPoints[i - 1]
                    val cpX = (prev.x + pt.x) / 2
                    cubicTo(cpX, prev.y, cpX, pt.y, pt.x, pt.y)
                }
            }
        }

        drawPath(
            path = linePath,
            color = Color.White.copy(alpha = 0.6f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
        )

        // Draw end dot
        if (animatedPoints.isNotEmpty()) {
            val last = animatedPoints.last()
            drawCircle(Color.White, radius = 4f, center = last)
        }
    }
}

@Composable
private fun animateLongAsState(targetValue: Long): State<Long> {
    val animatable = remember { Animatable(targetValue.toFloat()) }
    LaunchedEffect(targetValue) {
        animatable.animateTo(
            targetValue.toFloat(),
            animationSpec = tween(600, easing = EaseOutCubic)
        )
    }
    return remember { derivedStateOf { animatable.value.toLong() } }
}
