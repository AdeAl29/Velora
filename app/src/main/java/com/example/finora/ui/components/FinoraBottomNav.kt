package com.example.finora.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finora.ui.theme.*

/**
 * iOS (Apple Music / Apple Photos / iOS Liquid Glass) style floating bottom navigation bar.
 * Features a frosted glassmorphic capsule with a fluid spring-animated sliding pill indicator
 * for all 5 main tabs, paired with a detached iOS circular quick-action (+) button.
 */
@Composable
fun FinoraBottomNav(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAddSheetOpen: Boolean = false
) {
    val isDark = LocalIsDarkTheme.current
    val haptic = LocalHapticFeedback.current

    val items = remember {
        listOf(
            IosTabItemData(
                label = "Beranda",
                selectedIcon = Icons.Rounded.Home,
                unselectedIcon = Icons.Outlined.Home
            ),
            IosTabItemData(
                label = "Transaksi",
                selectedIcon = Icons.Rounded.ReceiptLong,
                unselectedIcon = Icons.Outlined.ReceiptLong
            ),
            IosTabItemData(
                label = "Statistik",
                selectedIcon = Icons.Rounded.InsertChart,
                unselectedIcon = Icons.Outlined.InsertChartOutlined
            ),
            IosTabItemData(
                label = "Tujuan",
                selectedIcon = Icons.Rounded.Flag,
                unselectedIcon = Icons.Outlined.Flag
            ),
            IosTabItemData(
                label = "Lainnya",
                selectedIcon = Icons.Rounded.GridView,
                unselectedIcon = Icons.Outlined.GridView
            )
        )
    }

    val bgColor = MaterialTheme.colorScheme.background

    // Frosted Glass colors inspired by iOS Apple Music & Photos tab bar
    val glassBaseColor = if (isDark) {
        Color(0xFF151B2E).copy(alpha = 0.90f)
    } else {
        Color(0xFFFFFFFF).copy(alpha = 0.92f)
    }

    val glassTopHighlight = if (isDark) {
        Color.White.copy(alpha = 0.10f)
    } else {
        Color.White.copy(alpha = 0.85f)
    }

    val glassBorderBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.22f),
                Color.White.copy(alpha = 0.08f),
                Color.White.copy(alpha = 0.04f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f),
                Color(0xFFCBD5E1).copy(alpha = 0.55f),
                Color(0xFF94A3B8).copy(alpha = 0.30f)
            )
        )
    }

    val activePillBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                FinoraPrimary.copy(alpha = 0.32f),
                FinoraSecondary.copy(alpha = 0.18f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                FinoraPrimary.copy(alpha = 0.15f),
                FinoraSecondary.copy(alpha = 0.10f)
            )
        )
    }

    val activePillBorderBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                FinoraPrimaryLight.copy(alpha = 0.45f),
                FinoraPrimary.copy(alpha = 0.12f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                FinoraPrimary.copy(alpha = 0.30f),
                FinoraPrimary.copy(alpha = 0.08f)
            )
        )
    }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // Subtle iOS bottom gradient scrim so content scrolling underneath fades smoothly
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            bgColor.copy(alpha = 0.65f),
                            bgColor.copy(alpha = 0.92f)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Main iOS Floating Glass Capsule (Apple Photos / Apple Music style)
            val capsuleShape = RoundedCornerShape(32.dp)

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .height(62.dp)
                    .shadow(
                        elevation = if (isDark) 24.dp else 16.dp,
                        shape = capsuleShape,
                        ambientColor = if (isDark) Color.Black.copy(alpha = 0.7f) else FinoraPrimary.copy(alpha = 0.18f),
                        spotColor = if (isDark) Color.Black.copy(alpha = 0.8f) else FinoraPrimary.copy(alpha = 0.22f)
                    )
                    .clip(capsuleShape)
                    .background(glassBaseColor)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(glassTopHighlight, Color.Transparent)
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = glassBorderBrush,
                        shape = capsuleShape
                    )
                    .padding(horizontal = 5.dp, vertical = 5.dp)
            ) {
                val tabCount = items.size
                val tabWidth = maxWidth / tabCount
                val safeSelectedIndex = selectedIndex.coerceIn(0, tabCount - 1)

                // Fluid spring-animated sliding active pill offset
                val indicatorOffset by animateDpAsState(
                    targetValue = tabWidth * safeSelectedIndex,
                    animationSpec = spring(
                        dampingRatio = 0.76f,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "ios_sliding_pill"
                )

                // Sliding Active Pill Indicator (Apple Photos / iOS 18+ style)
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(27.dp))
                        .background(brush = activePillBrush)
                        .border(
                            width = 0.8.dp,
                            brush = activePillBorderBrush,
                            shape = RoundedCornerShape(27.dp)
                        )
                ) {
                    // Subtle top specular glow inside the active pill
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    FinoraPrimaryLight.copy(alpha = if (isDark) 0.22f else 0.14f),
                                    Color.Transparent
                                ),
                                center = Offset(size.width / 2f, 0f),
                                radius = size.width * 0.65f
                            )
                        )
                    }
                }

                // Tab Items Row
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        val isSelected = safeSelectedIndex == index
                        IosBottomTabItem(
                            item = item,
                            isSelected = isSelected,
                            isDark = isDark,
                            onClick = {
                                if (selectedIndex != index) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onItemSelected(index)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }

            // 2. Detached iOS Circular Action Button (+)
            IosFloatingAddButton(
                isExpanded = isAddSheetOpen,
                onClick = onAddClick
            )
        }
    }
}

@Composable
private fun IosBottomTabItem(
    item: IosTabItemData,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.88f
            isSelected -> 1.0f
            else -> 0.96f
        },
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ios_tab_scale"
    )

    val iconBounce by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "ios_icon_bounce"
    )

    val activeColor = if (isDark) FinoraPrimaryLight else FinoraPrimary
    val inactiveColor = if (isDark) {
        Color(0xFF94A3B8).copy(alpha = 0.85f)
    } else {
        Color(0xFF64748B).copy(alpha = 0.90f)
    }

    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 220),
        label = "ios_tab_color"
    )

    Column(
        modifier = modifier
            .scale(pressScale)
            .clip(RoundedCornerShape(27.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.label,
            tint = animatedColor,
            modifier = Modifier
                .size(21.dp)
                .scale(iconBounce)
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = item.label,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontFamily = FinoraFontFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = animatedColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            letterSpacing = (-0.1).sp
        )
    }
}

@Composable
fun IosFloatingAddButton(
    isExpanded: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
        label = "ios_add_scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
        label = "ios_add_rotation"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(58.dp)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = FinoraPrimary.copy(alpha = 0.45f),
                spotColor = FinoraPrimary.copy(alpha = 0.55f)
            )
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        FinoraPrimary,
                        Color(0xFF3B82F6),
                        FinoraSecondary
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.10f)
                    )
                ),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.White, bounded = true)
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        // Subtle inner top gloss
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.35f, size.height * 0.25f),
                    radius = size.width * 0.5f
                )
            )
        }

        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Catat Transaksi Cepat",
            tint = Color.White,
            modifier = Modifier
                .size(26.dp)
                .rotate(rotation)
        )
    }
}

@Composable
fun FinoraAddButtonSmall(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IosFloatingAddButton(onClick = onClick, modifier = modifier)
}

private data class IosTabItemData(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
