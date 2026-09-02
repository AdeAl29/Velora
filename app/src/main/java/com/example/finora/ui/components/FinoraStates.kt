package com.example.finora.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.finora.ui.theme.*

enum class EmptyStateType {
    TRANSACTION, BUDGET, GOAL, WALLET, BILL, SEARCH, ERROR, OFFLINE
}

@Composable
fun FinoraEmptyState(
    type: EmptyStateType,
    title: String,
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated illustration
        EmptyStateIllustration(type = type)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            FinoraPrimaryButton(
                text = actionText,
                onClick = onAction,
                modifier = Modifier.widthIn(max = 240.dp)
            )
        }
    }
}

@Composable
private fun EmptyStateIllustration(type: EmptyStateType) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty_anim")
    val animatedFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_anim"
    )

    val offset = animatedFloat * 8f

    Canvas(
        modifier = Modifier
            .size(120.dp)
            .offset(y = offset.dp)
    ) {
        val s = size.minDimension
        val color1 = FinoraPrimary.copy(alpha = 0.6f)
        val color2 = FinoraSecondary.copy(alpha = 0.4f)
        val color3 = FinoraPrimaryLight.copy(alpha = 0.3f)

        when (type) {
            EmptyStateType.TRANSACTION -> {
                // Empty wallet illustration
                drawRoundRect(
                    color = color1,
                    topLeft = Offset(s * 0.2f, s * 0.25f),
                    size = androidx.compose.ui.geometry.Size(s * 0.6f, s * 0.45f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(s * 0.06f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = s * 0.025f)
                )
                // Open flap
                val flapPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(s * 0.2f, s * 0.4f)
                    lineTo(s * 0.5f, s * 0.2f)
                    lineTo(s * 0.8f, s * 0.4f)
                }
                drawPath(flapPath, color1, style = androidx.compose.ui.graphics.drawscope.Stroke(width = s * 0.025f))
                // Coin floating out
                drawCircle(color2, s * 0.06f, Offset(s * 0.5f, s * 0.15f + offset * 0.5f))
            }
            EmptyStateType.GOAL -> {
                // Target with flag
                drawCircle(color1, s * 0.3f, Offset(s * 0.5f, s * 0.5f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = s * 0.025f))
                drawCircle(color2, s * 0.18f, Offset(s * 0.5f, s * 0.5f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = s * 0.025f))
                drawCircle(color1, s * 0.06f, Offset(s * 0.5f, s * 0.5f))
                // Flag
                drawLine(color1, Offset(s * 0.5f, s * 0.5f), Offset(s * 0.5f, s * 0.15f), strokeWidth = s * 0.02f)
                val flagPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(s * 0.5f, s * 0.15f)
                    lineTo(s * 0.7f, s * 0.22f)
                    lineTo(s * 0.5f, s * 0.3f)
                }
                drawPath(flagPath, color3, style = androidx.compose.ui.graphics.drawscope.Fill)
            }
            EmptyStateType.BUDGET -> {
                // Pie chart empty
                drawCircle(color1, s * 0.28f, Offset(s * 0.5f, s * 0.5f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = s * 0.06f))
                // Dashed section
                drawArc(color2, 0f, 90f, false, Offset(s * 0.22f, s * 0.22f), androidx.compose.ui.geometry.Size(s * 0.56f, s * 0.56f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = s * 0.06f))
            }
            EmptyStateType.WALLET -> {
                // Credit card
                drawRoundRect(color1, Offset(s * 0.15f, s * 0.25f), androidx.compose.ui.geometry.Size(s * 0.7f, s * 0.45f), androidx.compose.ui.geometry.CornerRadius(s * 0.05f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = s * 0.025f))
                drawLine(color2, Offset(s * 0.15f, s * 0.42f), Offset(s * 0.85f, s * 0.42f), strokeWidth = s * 0.025f)
                drawCircle(color3, s * 0.05f, Offset(s * 0.75f, s * 0.58f))
                drawCircle(color3, s * 0.05f, Offset(s * 0.65f, s * 0.58f))
            }
            EmptyStateType.BILL -> {
                // Receipt
                val receiptPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(s * 0.25f, s * 0.15f)
                    lineTo(s * 0.75f, s * 0.15f)
                    lineTo(s * 0.75f, s * 0.78f)
                    lineTo(s * 0.65f, s * 0.72f)
                    lineTo(s * 0.55f, s * 0.78f)
                    lineTo(s * 0.45f, s * 0.72f)
                    lineTo(s * 0.35f, s * 0.78f)
                    lineTo(s * 0.25f, s * 0.72f)
                    close()
                }
                drawPath(receiptPath, color1, style = androidx.compose.ui.graphics.drawscope.Stroke(width = s * 0.025f))
                // Lines
                drawLine(color2, Offset(s * 0.35f, s * 0.35f), Offset(s * 0.65f, s * 0.35f), strokeWidth = s * 0.02f)
                drawLine(color3, Offset(s * 0.35f, s * 0.48f), Offset(s * 0.6f, s * 0.48f), strokeWidth = s * 0.02f)
            }
            EmptyStateType.SEARCH -> {
                // Magnifying glass
                drawCircle(color1, s * 0.2f, Offset(s * 0.45f, s * 0.4f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = s * 0.03f))
                drawLine(color1, Offset(s * 0.6f, s * 0.55f), Offset(s * 0.75f, s * 0.7f), strokeWidth = s * 0.04f)
                // Question mark
                drawCircle(color2, s * 0.03f, Offset(s * 0.45f, s * 0.45f))
            }
            EmptyStateType.ERROR -> {
                // Warning triangle
                val triPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(s * 0.5f, s * 0.15f)
                    lineTo(s * 0.82f, s * 0.75f)
                    lineTo(s * 0.18f, s * 0.75f)
                    close()
                }
                drawPath(triPath, FinoraExpense.copy(alpha = 0.5f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = s * 0.025f))
                drawLine(FinoraExpense.copy(alpha = 0.6f), Offset(s * 0.5f, s * 0.35f), Offset(s * 0.5f, s * 0.55f), strokeWidth = s * 0.03f)
                drawCircle(FinoraExpense.copy(alpha = 0.6f), s * 0.025f, Offset(s * 0.5f, s * 0.65f))
            }
            EmptyStateType.OFFLINE -> {
                // Cloud with x
                val cloudPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(s * 0.25f, s * 0.55f)
                    quadraticBezierTo(s * 0.15f, s * 0.55f, s * 0.15f, s * 0.45f)
                    quadraticBezierTo(s * 0.15f, s * 0.32f, s * 0.3f, s * 0.32f)
                    quadraticBezierTo(s * 0.33f, s * 0.2f, s * 0.48f, s * 0.2f)
                    quadraticBezierTo(s * 0.65f, s * 0.2f, s * 0.68f, s * 0.32f)
                    quadraticBezierTo(s * 0.85f, s * 0.33f, s * 0.85f, s * 0.45f)
                    quadraticBezierTo(s * 0.85f, s * 0.55f, s * 0.75f, s * 0.55f)
                    close()
                }
                drawPath(cloudPath, color1, style = androidx.compose.ui.graphics.drawscope.Stroke(width = s * 0.025f))
                drawLine(color2, Offset(s * 0.42f, s * 0.62f), Offset(s * 0.58f, s * 0.78f), strokeWidth = s * 0.025f)
                drawLine(color2, Offset(s * 0.58f, s * 0.62f), Offset(s * 0.42f, s * 0.78f), strokeWidth = s * 0.025f)
            }
        }
    }
}

@Composable
fun FinoraLoadingState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balance card skeleton
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(24.dp))
        )

        // Section skeleton
        ShimmerBox(
            modifier = Modifier
                .width(120.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        // Card skeletons
        repeat(3) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }
    }
}

@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    Box(
        modifier = modifier.background(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                startX = translateAnim * 300f,
                endX = translateAnim * 300f + 300f
            )
        )
    )
}

@Composable
fun FinoraErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    FinoraEmptyState(
        type = EmptyStateType.ERROR,
        title = "Terjadi Kesalahan",
        message = message,
        actionText = if (onRetry != null) "Coba Lagi" else null,
        onAction = onRetry,
        modifier = modifier
    )
}
