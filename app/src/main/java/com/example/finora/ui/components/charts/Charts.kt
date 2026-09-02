package com.example.finora.ui.components.charts

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finora.core.util.CurrencyFormatter
import com.example.finora.ui.theme.*

data class ChartDataPoint(
    val label: String,
    val value: Float,
    val color: Color = FinoraPrimary
)

@Composable
fun FinoraBarChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOf { it.value }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, tween(800, easing = EaseOutCubic))
    }

    var selectedIndex by remember { mutableIntStateOf(-1) }

    Column(modifier = modifier) {
        // Selected tooltip
        if (selectedIndex >= 0 && selectedIndex < data.size) {
            val item = data[selectedIndex]
            Text(
                text = "${item.label}: ${CurrencyFormatter.formatRupiah(item.value.toLong())}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        val barWidth = size.width.toFloat() / data.size
                        val index = (offset.x / barWidth).toInt().coerceIn(0, data.size - 1)
                        selectedIndex = if (selectedIndex == index) -1 else index
                    }
                }
        ) {
            val barCount = data.size
            val totalWidth = size.width
            val chartHeight = size.height - (if (showLabels) 24f else 0f)
            val barSpacing = totalWidth * 0.15f / barCount
            val barWidth = (totalWidth - barSpacing * (barCount + 1)) / barCount

            data.forEachIndexed { index, point ->
                val barHeight = if (maxValue > 0) (point.value / maxValue) * chartHeight * animationProgress.value else 0f
                val x = barSpacing + index * (barWidth + barSpacing)
                val y = chartHeight - barHeight

                val isSelected = index == selectedIndex
                val cornerRad = barWidth * 0.3f

                drawRoundRect(
                    color = point.color.copy(alpha = if (isSelected) 1f else 0.7f),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(cornerRad, cornerRad)
                )

                // Glow effect for selected
                if (isSelected) {
                    drawRoundRect(
                        color = point.color.copy(alpha = 0.2f),
                        topLeft = Offset(x - 4f, y - 4f),
                        size = Size(barWidth + 8f, barHeight + 4f),
                        cornerRadius = CornerRadius(cornerRad + 4f, cornerRad + 4f)
                    )
                }
            }
        }

        if (showLabels) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                data.forEach { point ->
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun FinoraDonutChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    showLegend: Boolean = true,
    centerText: String? = null
) {
    if (data.isEmpty()) return

    val total = data.sumOf { it.value.toDouble() }.toFloat()
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, tween(1000, easing = EaseOutCubic))
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 28f
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                // Background track
                drawArc(
                    color = Color.Gray.copy(alpha = 0.1f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                var startAngle = -90f
                data.forEach { point ->
                    val sweep = if (total > 0) (point.value / total) * 360f * animationProgress.value else 0f
                    drawArc(
                        color = point.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += sweep
                }
            }

            // Center text
            if (centerText != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = centerText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (showLegend) {
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                data.forEach { point ->
                    val percentage = if (total > 0) (point.value / total * 100) else 0f
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(point.color, shape = androidx.compose.foundation.shape.CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = point.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${String.format("%.1f", percentage)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FinoraLineChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = FinoraPrimary,
    fillGradient: Boolean = true,
    showDots: Boolean = true
) {
    if (data.size < 2) return

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, tween(1000, easing = EaseOutCubic))
    }

    var selectedIndex by remember { mutableIntStateOf(-1) }

    Column(modifier = modifier) {
        if (selectedIndex >= 0 && selectedIndex < data.size) {
            val item = data[selectedIndex]
            Text(
                text = "${item.label}: ${CurrencyFormatter.formatRupiah(item.value.toLong())}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        val spacing = size.width.toFloat() / (data.size - 1)
                        val index = (offset.x / spacing + 0.5f).toInt().coerceIn(0, data.size - 1)
                        selectedIndex = if (selectedIndex == index) -1 else index
                    }
                }
        ) {
            val maxVal = data.maxOf { it.value }
            val minVal = data.minOf { it.value }
            val range = if (maxVal == minVal) 1f else maxVal - minVal
            val h = size.height
            val w = size.width

            val points = data.mapIndexed { i, dp ->
                val x = (i.toFloat() / (data.size - 1)) * w
                val y = h - ((dp.value - minVal) / range) * h * 0.85f - h * 0.075f
                Offset(x, y)
            }

            // Animate points
            val animPoints = points.mapIndexed { i, pt ->
                val ratio = i.toFloat() / (points.size - 1)
                if (ratio <= animationProgress.value) pt
                else {
                    val lastVisible = points.getOrNull(i - 1) ?: pt
                    Offset(pt.x, lastVisible.y)
                }
            }

            // Gradient fill
            if (fillGradient) {
                val fillPath = Path().apply {
                    moveTo(0f, h)
                    animPoints.forEachIndexed { i, pt ->
                        if (i == 0) lineTo(pt.x, pt.y)
                        else {
                            val prev = animPoints[i - 1]
                            val cpX = (prev.x + pt.x) / 2
                            cubicTo(cpX, prev.y, cpX, pt.y, pt.x, pt.y)
                        }
                    }
                    lineTo(w, h)
                    close()
                }
                drawPath(
                    fillPath,
                    brush = Brush.verticalGradient(
                        listOf(lineColor.copy(alpha = 0.25f), lineColor.copy(alpha = 0f))
                    )
                )
            }

            // Line
            val linePath = Path().apply {
                animPoints.forEachIndexed { i, pt ->
                    if (i == 0) moveTo(pt.x, pt.y)
                    else {
                        val prev = animPoints[i - 1]
                        val cpX = (prev.x + pt.x) / 2
                        cubicTo(cpX, prev.y, cpX, pt.y, pt.x, pt.y)
                    }
                }
            }
            drawPath(linePath, lineColor, style = Stroke(width = 3f, cap = StrokeCap.Round))

            // Dots
            if (showDots) {
                animPoints.forEachIndexed { i, pt ->
                    val ratio = i.toFloat() / (points.size - 1)
                    if (ratio <= animationProgress.value) {
                        val isSelected = i == selectedIndex
                        drawCircle(lineColor, if (isSelected) 7f else 4f, pt)
                        drawCircle(Color.White, if (isSelected) 4f else 2f, pt)
                    }
                }
            }
        }

        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEachIndexed { i, dp ->
                if (i == 0 || i == data.lastIndex || i == data.size / 2) {
                    Text(
                        text = dp.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
