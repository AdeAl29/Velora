package com.example.finora.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.example.finora.ui.theme.*
import kotlin.math.abs

// Apple iOS signature smooth deceleration curve
private val IosFluidEasing = CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)

/**
 * Ultra-Smooth 120Hz iOS 26 "Liquid Glass" Floating Bottom Navigation Bar.
 *
 * Engineered for zero-jank, continuous fluid motion:
 * 1. **Single Continuous Capsule Morph**:
 *    Uses a single persistent [LiquidGlassSurface] that smoothly interpolates its width and height
 *    between the full 5-tab bar and the compact minimized pill—zero cross-fade flicker or remounting.
 * 2. **Dual-Spring Liquid Droplet Physics**:
 *    The active indicator pill animates its left and right edges with asymmetric spring stiffnesses,
 *    creating an organic water-droplet stretch in mid-flight and a gentle liquid cushion on arrival.
 * 3. **RenderThread `graphicsLayer` Animations**:
 *    Scale, alpha, translation, and idle label transitions run in the draw phase via `graphicsLayer`,
 *    avoiding layout thrashing during scroll or idle transitions.
 */
@Composable
fun FinoraBottomNav(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAddSheetOpen: Boolean = false,
    isMinimized: Boolean = false,
    isIdle: Boolean = false,
    onExpandRequest: () -> Unit = {},
    backdropGraphicsLayer: GraphicsLayer? = null,
    rootOffsetInWindow: Offset = Offset.Zero
) {
    val isDark = LocalIsDarkTheme.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val items = remember {
        listOf(
            IosTabItemData(
                label = "Beranda",
                selectedIcon = Icons.Rounded.Home,
                unselectedIcon = Icons.Outlined.Home
            ),
            IosTabItemData(
                label = "Transaksi",
                selectedIcon = Icons.AutoMirrored.Rounded.ReceiptLong,
                unselectedIcon = Icons.AutoMirrored.Outlined.ReceiptLong
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

    val safeSelectedIndex = selectedIndex.coerceIn(0, items.lastIndex)
    val activeItem = items[safeSelectedIndex]

    var isDraggingCapsule by remember { mutableStateOf(false) }

    // Continuous 0f..1f progress for Minimize (Scroll Down) with silky critically-damped spring
    val minimizeProgress by animateFloatAsState(
        targetValue = if (isMinimized) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.84f,
            stiffness = 240f,
            visibilityThreshold = 0.001f
        ),
        label = "minimize_progress"
    )

    // Continuous 0f..1f progress for Ambient Idle (Untouched)
    val idleProgress by animateFloatAsState(
        targetValue = if (isIdle && !isDraggingCapsule) 1f else 0f,
        animationSpec = tween(
            durationMillis = 520,
            easing = IosFluidEasing
        ),
        label = "idle_progress"
    )

    // Interactive press/drag magnification progress
    val dragProgress by animateFloatAsState(
        targetValue = if (isDraggingCapsule) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.68f,
            stiffness = 380f
        ),
        label = "drag_progress"
    )

    // Subtle continuous prismatic light drift
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_glass_shimmer")
    val refractionShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "refraction_shift"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .graphicsLayer {
                val targetScale = lerp(
                    start = lerp(1f, 0.968f, idleProgress),
                    stop = lerp(0.985f, 0.945f, idleProgress),
                    fraction = minimizeProgress
                ) + (0.022f * dragProgress)

                scaleX = targetScale
                scaleY = targetScale
                translationY = (lerp(0f, 3.5f, idleProgress) + lerp(0f, 2.5f, minimizeProgress)).dp.toPx()
                alpha = lerp(
                    start = lerp(1f, 0.90f, idleProgress),
                    stop = lerp(0.96f, 0.80f, idleProgress),
                    fraction = minimizeProgress
                )
            }
    ) {
        val totalAvailableWidth = maxWidth
        val spacing = 10.dp

        // Smoothly interpolate (+) button size from 60.dp -> 50.dp
        val addButtonSize = lerp(
            start = lerp(60.dp, 54.dp, idleProgress),
            stop = 50.dp,
            fraction = minimizeProgress
        )

        // Smoothly interpolate main capsule width & height (Single continuous capsule morph!)
        val expandedCapsuleWidth = (totalAvailableWidth - addButtonSize - spacing).coerceAtLeast(160.dp)
        val minimizedCapsuleWidth = 158.dp
        val currentCapsuleWidth = lerp(expandedCapsuleWidth, minimizedCapsuleWidth, minimizeProgress)

        val currentCapsuleHeight = lerp(
            start = lerp(64.dp, 55.dp, idleProgress),
            stop = 50.dp,
            fraction = minimizeProgress
        )

        val capsuleShape = RoundedCornerShape(34.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Single Continuous iOS Liquid Glass Capsule (Never unmounts between Expanded & Minimized!)
            LiquidGlassSurface(
                shape = capsuleShape,
                isDark = isDark,
                idleProgress = idleProgress,
                highlightProgress = dragProgress,
                refractionShift = refractionShift,
                backdropGraphicsLayer = backdropGraphicsLayer,
                rootOffsetInWindow = rootOffsetInWindow,
                modifier = Modifier
                    .width(currentCapsuleWidth)
                    .height(currentCapsuleHeight)
            ) {
                val innerPadding = 5.dp
                val innerWidth = (currentCapsuleWidth - innerPadding * 2).coerceAtLeast(100.dp)
                val tabCount = items.size
                val expandedTabWidth = ((expandedCapsuleWidth - innerPadding * 2) / tabCount).coerceAtLeast(40.dp)

                // Target left & right edges for the active Liquid Glass Pill in Expanded mode
                val targetPillLeftExpanded = expandedTabWidth * safeSelectedIndex
                val targetPillRightExpanded = targetPillLeftExpanded + expandedTabWidth

                // Dual-spring physics: leading & trailing edges move with slightly different stiffness
                // so the pill organically stretches like a water droplet while sliding between tabs!
                var previousTabIndex by remember { mutableIntStateOf(safeSelectedIndex) }
                val isMovingRight = safeSelectedIndex >= previousTabIndex
                SideEffect {
                    if (previousTabIndex != safeSelectedIndex) {
                        previousTabIndex = safeSelectedIndex
                    }
                }

                val leftEdgeSpring = if (isMovingRight) {
                    // Trailing edge when moving right (softer stiffness -> stretches pill)
                    spring<Dp>(dampingRatio = 0.80f, stiffness = 230f)
                } else {
                    // Leading edge when moving left (snappier stiffness)
                    spring<Dp>(dampingRatio = 0.80f, stiffness = 360f)
                }

                val rightEdgeSpring = if (isMovingRight) {
                    // Leading edge when moving right (snappier stiffness)
                    spring<Dp>(dampingRatio = 0.80f, stiffness = 360f)
                } else {
                    // Trailing edge when moving left (softer stiffness -> stretches pill)
                    spring<Dp>(dampingRatio = 0.80f, stiffness = 230f)
                }

                val animatedPillLeftExpanded by animateDpAsState(
                    targetValue = targetPillLeftExpanded,
                    animationSpec = leftEdgeSpring,
                    label = "pill_left_edge"
                )
                val animatedPillRightExpanded by animateDpAsState(
                    targetValue = targetPillRightExpanded,
                    animationSpec = rightEdgeSpring,
                    label = "pill_right_edge"
                )

                // As minimizeProgress goes 0f -> 1f, the active liquid pill smoothly glides to fill the compact pill!
                val pillLeft = lerp(animatedPillLeftExpanded, 0.dp, minimizeProgress)
                val pillRight = lerp(animatedPillRightExpanded, innerWidth, minimizeProgress)
                val pillWidth = (pillRight - pillLeft).coerceAtLeast(36.dp)

                // Calculate how much the droplet is currently stretched in mid-flight (0f..1f)
                val stretchRatio = with(density) {
                    val naturalW = expandedTabWidth.toPx().coerceAtLeast(1f)
                    val currentW = (animatedPillRightExpanded - animatedPillLeftExpanded).toPx()
                    ((abs(currentW - naturalW) / naturalW) * 1.4f).coerceIn(0f, 1f)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .pointerInput(isMinimized, items.size) {
                            if (!isMinimized) {
                                detectHorizontalDragGestures(
                                    onDragStart = { isDraggingCapsule = true },
                                    onDragEnd = { isDraggingCapsule = false },
                                    onDragCancel = { isDraggingCapsule = false },
                                    onHorizontalDrag = { change, _ ->
                                        change.consume()
                                        val widthPx = size.width.toFloat().coerceAtLeast(1f)
                                        val xPos = change.position.x.coerceIn(0f, widthPx - 1f)
                                        val targetIndex = ((xPos / widthPx) * items.size)
                                            .toInt()
                                            .coerceIn(0, items.lastIndex)
                                        if (targetIndex != selectedIndex) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onItemSelected(targetIndex)
                                        }
                                    }
                                )
                            }
                        }
                ) {
                    // Shared Liquid Droplet Active Pill (Morphs seamlessly between tab slot & compact capsule!)
                    val pillShape = RoundedCornerShape(28.dp)
                    val activePillBrush = if (isDark) {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = lerp(0.24f, 0.14f, idleProgress)),
                                FinoraPrimary.copy(alpha = lerp(0.36f, 0.22f, idleProgress)),
                                FinoraSecondary.copy(alpha = lerp(0.24f, 0.14f, idleProgress))
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = lerp(0.90f, 0.68f, idleProgress)),
                                FinoraPrimary.copy(alpha = lerp(0.20f, 0.12f, idleProgress)),
                                FinoraSecondary.copy(alpha = lerp(0.14f, 0.08f, idleProgress))
                            )
                        )
                    }

                    val activePillBorderBrush = if (isDark) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.58f),
                                FinoraPrimaryLight.copy(alpha = 0.42f),
                                Color.White.copy(alpha = 0.14f)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.98f),
                                FinoraPrimary.copy(alpha = 0.34f),
                                Color.White.copy(alpha = 0.68f)
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .offset(x = pillLeft)
                            .width(pillWidth)
                            .fillMaxHeight()
                            .graphicsLayer {
                                // Subtle vertical squish during rapid horizontal droplet stretch + lens swell on drag
                                val dropletScaleY = 1f - (0.045f * stretchRatio) + (0.055f * dragProgress)
                                val dropletScaleX = 1f + (0.06f * dragProgress)
                                scaleX = dropletScaleX
                                scaleY = dropletScaleY
                            }
                            .clip(pillShape)
                            .background(brush = activePillBrush)
                            .border(
                                width = 1.dp,
                                brush = activePillBorderBrush,
                                shape = pillShape
                            )
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Top curved liquid lens specular reflection
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = if (isDark) 0.28f else 0.65f),
                                        Color.Transparent
                                    ),
                                    startY = 0f,
                                    endY = size.height * 0.48f
                                ),
                                cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                            )
                            // Bottom chromatic dispersion glow
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        FinoraPrimaryLight.copy(alpha = if (isDark) 0.28f else 0.18f),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width / 2f, size.height * 0.85f),
                                    radius = size.width * 0.65f
                                )
                            )
                        }
                    }

                    // Layer A: Full 5-Tab Row (Smoothly fades & compresses as minimizeProgress -> 1f)
                    if (minimizeProgress < 0.98f) {
                        val tabsAlpha = (1f - (minimizeProgress * 1.45f)).coerceIn(0f, 1f)
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = tabsAlpha
                                    val s = lerp(1f, 0.90f, minimizeProgress)
                                    scaleX = s
                                    scaleY = s
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items.forEachIndexed { index, item ->
                                val isSelected = safeSelectedIndex == index
                                IosBottomTabItem(
                                    item = item,
                                    isSelected = isSelected,
                                    idleProgress = idleProgress,
                                    isDark = isDark,
                                    onClick = {
                                        if (isMinimized) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onExpandRequest()
                                        } else if (selectedIndex != index) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onItemSelected(index)
                                        } else {
                                            onExpandRequest()
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                            }
                        }
                    }

                    // Layer B: Minimized Compact Pill Content (Smoothly blooms in as minimizeProgress -> 1f)
                    if (minimizeProgress > 0.02f) {
                        val compactAlpha = ((minimizeProgress - 0.22f) / 0.78f).coerceIn(0f, 1f)
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = compactAlpha
                                    val s = lerp(0.88f, 1f, minimizeProgress)
                                    scaleX = s
                                    scaleY = s
                                }
                                .clip(pillShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onExpandRequest()
                                }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = activeItem.selectedIcon,
                                    contentDescription = activeItem.label,
                                    tint = if (isDark) FinoraPrimaryLight else FinoraPrimary,
                                    modifier = Modifier.size(19.dp)
                                )
                                Text(
                                    text = activeItem.label,
                                    fontSize = 12.5.sp,
                                    fontFamily = FinoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // 3 liquid micro-dots indicating tap-to-expand
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(3) { dotIndex ->
                                    Box(
                                        modifier = Modifier
                                            .size(if (dotIndex == 1) 4.dp else 3.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isDark) Color.White.copy(alpha = 0.55f)
                                                else Color(0xFF334155).copy(alpha = 0.50f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Detached iOS Liquid Glass Circular Action Button (+)
            IosFloatingAddButton(
                buttonSize = addButtonSize,
                isExpanded = isAddSheetOpen,
                minimizeProgress = minimizeProgress,
                idleProgress = idleProgress,
                backdropGraphicsLayer = backdropGraphicsLayer,
                rootOffsetInWindow = rootOffsetInWindow,
                onClick = onAddClick
            )
        }
    }
}

/**
 * Reusable iOS 26 Liquid Glass Container driven by continuous [idleProgress] & [highlightProgress]
 * so zero state snapping occurs during transitions.
 */
@Composable
private fun LiquidGlassSurface(
    shape: Shape,
    isDark: Boolean,
    idleProgress: Float,
    highlightProgress: Float,
    refractionShift: Float,
    backdropGraphicsLayer: GraphicsLayer?,
    rootOffsetInWindow: Offset,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var surfaceOffsetInWindow by remember { mutableStateOf(Offset.Zero) }

    val baseAlpha = if (isDark) {
        lerp(0.50f, 0.35f, idleProgress) + (0.10f * highlightProgress)
    } else {
        lerp(0.56f, 0.40f, idleProgress) + (0.12f * highlightProgress)
    }

    val baseGlassColor = if (isDark) {
        Color(0xFF0F172A).copy(alpha = baseAlpha.coerceIn(0.25f, 0.80f))
    } else {
        Color(0xFFFFFFFF).copy(alpha = baseAlpha.coerceIn(0.30f, 0.85f))
    }

    val borderAlphaMultiplier = lerp(1f, 0.52f, idleProgress) + (0.25f * highlightProgress)

    val specularBorderBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = (0.48f * borderAlphaMultiplier).coerceIn(0f, 0.95f)),
                FinoraPrimaryLight.copy(alpha = (0.28f * borderAlphaMultiplier).coerceIn(0f, 0.85f)),
                Color.White.copy(alpha = (0.08f * borderAlphaMultiplier).coerceIn(0f, 0.50f)),
                FinoraSecondary.copy(alpha = (0.24f * borderAlphaMultiplier).coerceIn(0f, 0.85f)),
                Color.White.copy(alpha = (0.36f * borderAlphaMultiplier).coerceIn(0f, 0.90f))
            ),
            start = Offset(0f, 0f),
            end = Offset(900f, 260f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = (0.95f * borderAlphaMultiplier).coerceIn(0f, 1f)),
                FinoraPrimaryLight.copy(alpha = (0.35f * borderAlphaMultiplier).coerceIn(0f, 0.85f)),
                Color(0xFFCBD5E1).copy(alpha = (0.45f * borderAlphaMultiplier).coerceIn(0f, 0.85f)),
                Color.White.copy(alpha = (0.90f * borderAlphaMultiplier).coerceIn(0f, 1f))
            ),
            start = Offset(0f, 0f),
            end = Offset(900f, 260f)
        )
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { coords ->
                val newPos = coords.positionInWindow()
                if (abs(newPos.x - surfaceOffsetInWindow.x) > 0.5f ||
                    abs(newPos.y - surfaceOffsetInWindow.y) > 0.5f
                ) {
                    surfaceOffsetInWindow = newPos
                }
            }
            .shadow(
                elevation = lerp(if (isDark) 22.dp else 16.dp, 10.dp, idleProgress),
                shape = shape,
                ambientColor = if (isDark) Color.Black.copy(alpha = 0.65f) else FinoraPrimary.copy(alpha = 0.20f),
                spotColor = if (isDark) Color.Black.copy(alpha = 0.75f) else FinoraPrimary.copy(alpha = 0.25f)
            )
            .clip(shape)
    ) {
        // Layer 1: Live Blurred Backdrop (Real Optical Refraction)
        if (backdropGraphicsLayer != null) {
            val relativeOffset = surfaceOffsetInWindow - rootOffsetInWindow
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .blur(
                        radius = 26.dp,
                        edgeTreatment = BlurredEdgeTreatment.Unbounded
                    )
            ) {
                translate(
                    left = -relativeOffset.x,
                    top = -relativeOffset.y
                ) {
                    drawLayer(backdropGraphicsLayer)
                }
            }
        }

        // Layer 2: Translucent Liquid Glass Base Tint
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(baseGlassColor)
        )

        // Layer 3: Optical Specular Sheen + Prismatic Iridescence
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(
                            alpha = (if (isDark) 0.16f else 0.55f) * borderAlphaMultiplier
                        ),
                        Color.White.copy(alpha = if (isDark) 0.03f else 0.12f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h * 0.55f
                )
            )

            val glowCenterX = w * (0.2f + 0.6f * refractionShift)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        FinoraPrimaryLight.copy(
                            alpha = (if (isDark) 0.12f else 0.10f) * borderAlphaMultiplier
                        ),
                        FinoraSecondary.copy(
                            alpha = (if (isDark) 0.06f else 0.05f) * borderAlphaMultiplier
                        ),
                        Color.Transparent
                    ),
                    center = Offset(glowCenterX, h * 0.5f),
                    radius = w * 0.55f
                )
            )

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(
                            alpha = (if (isDark) 0.07f else 0.30f) * borderAlphaMultiplier
                        )
                    ),
                    startY = h * 0.72f,
                    endY = h
                )
            )
        }

        // Layer 4: Outer Specular Glass Border
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 1.2.dp,
                    brush = specularBorderBrush,
                    shape = shape
                )
        )

        content()
    }
}

/**
 * Individual Tab Item with 120Hz `graphicsLayer` animations (no `AnimatedVisibility` layout jank).
 */
@Composable
private fun IosBottomTabItem(
    item: IosTabItemData,
    isSelected: Boolean,
    idleProgress: Float,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.85f
            isSelected -> 1.0f
            else -> 0.96f
        },
        animationSpec = spring(
            dampingRatio = 0.64f,
            stiffness = 400f
        ),
        label = "ios_tab_press_scale"
    )

    val selectionProgress by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.70f,
            stiffness = 320f
        ),
        label = "ios_tab_selection_progress"
    )

    // Smoothly keep label visible for selected tab, and smoothly fade/glide for inactive tabs when idle
    val labelVisibility = lerp(1f - idleProgress, 1f, selectionProgress).coerceIn(0f, 1f)

    val activeTextColor = if (isDark) Color.White else FinoraPrimaryDark
    val activeIconTint = if (isDark) FinoraPrimaryLight else FinoraPrimary
    val inactiveColor = if (isDark) {
        Color(0xFFCBD5E1).copy(alpha = lerp(0.85f, 0.58f, idleProgress))
    } else {
        Color(0xFF334155).copy(alpha = lerp(0.88f, 0.62f, idleProgress))
    }

    val animatedIconTint by animateColorAsState(
        targetValue = if (isSelected) activeIconTint else inactiveColor,
        animationSpec = tween(durationMillis = 260, easing = IosFluidEasing),
        label = "ios_tab_icon_color"
    )

    val animatedTextColor by animateColorAsState(
        targetValue = if (isSelected) activeTextColor else inactiveColor,
        animationSpec = tween(durationMillis = 260, easing = IosFluidEasing),
        label = "ios_tab_text_color"
    )

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(RoundedCornerShape(28.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 3.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.label,
            tint = animatedIconTint,
            modifier = Modifier
                .size(21.dp)
                .graphicsLayer {
                    val bounce = lerp(1f, 1.12f, selectionProgress)
                    scaleX = bounce
                    scaleY = bounce
                    // Smoothly center the icon vertically as the inactive label fades out in idle mode
                    translationY = lerp(5.dp.toPx(), 0f, labelVisibility)
                }
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = item.label,
            fontSize = 10.sp,
            lineHeight = 11.sp,
            fontFamily = FinoraFontFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            color = animatedTextColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            letterSpacing = (-0.15).sp,
            modifier = Modifier.graphicsLayer {
                alpha = labelVisibility
                val textScale = lerp(0.82f, 1f, labelVisibility)
                scaleX = textScale
                scaleY = textScale
                translationY = lerp(4.dp.toPx(), 0f, labelVisibility)
            }
        )
    }
}

@Composable
fun IosFloatingAddButton(
    buttonSize: Dp = 60.dp,
    isExpanded: Boolean = false,
    minimizeProgress: Float = 0f,
    idleProgress: Float = 0f,
    backdropGraphicsLayer: GraphicsLayer? = null,
    rootOffsetInWindow: Offset = Offset.Zero,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var buttonOffsetInWindow by remember { mutableStateOf(Offset.Zero) }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = 0.60f, stiffness = 380f),
        label = "ios_add_scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 135f else 0f,
        animationSpec = spring(dampingRatio = 0.68f, stiffness = 260f),
        label = "ios_add_rotation"
    )

    Box(
        modifier = modifier
            .onGloballyPositioned { coords ->
                val newPos = coords.positionInWindow()
                if (abs(newPos.x - buttonOffsetInWindow.x) > 0.5f ||
                    abs(newPos.y - buttonOffsetInWindow.y) > 0.5f
                ) {
                    buttonOffsetInWindow = newPos
                }
            }
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .size(buttonSize)
            .shadow(
                elevation = lerp(18.dp, 10.dp, idleProgress),
                shape = CircleShape,
                ambientColor = FinoraPrimary.copy(alpha = 0.45f),
                spotColor = FinoraPrimary.copy(alpha = 0.55f)
            )
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.White, bounded = true)
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (backdropGraphicsLayer != null) {
            val relativeOffset = buttonOffsetInWindow - rootOffsetInWindow
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .blur(radius = 22.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
            ) {
                translate(left = -relativeOffset.x, top = -relativeOffset.y) {
                    drawLayer(backdropGraphicsLayer)
                }
            }
        }

        val tintAlpha = lerp(0.86f, 0.72f, idleProgress)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            FinoraPrimary.copy(alpha = tintAlpha),
                            Color(0xFF3B82F6).copy(alpha = tintAlpha - 0.04f),
                            FinoraSecondary.copy(alpha = tintAlpha)
                        )
                    )
                )
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.38f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.32f, size.height * 0.22f),
                    radius = size.width * 0.55f
                )
            )
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.78f),
                        Color.White.copy(alpha = 0.16f),
                        Color.White.copy(alpha = 0.48f)
                    )
                ),
                style = Stroke(width = 1.4.dp.toPx())
            )
        }

        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Catat Transaksi Cepat",
            tint = Color.White,
            modifier = Modifier
                .size(lerp(26.dp, 22.dp, minimizeProgress))
                .graphicsLayer {
                    rotationZ = rotation
                }
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
