package com.example.finora.ui.components

import androidx.compose.animation.*
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finora.ui.theme.*
import kotlin.math.roundToInt

/**
 * iOS 26 "Liquid Glass" (Apple Music & Apple Photos style) Floating Bottom Navigation Bar.
 *
 * Key iOS Liquid Glass Mechanics Implemented:
 * 1. **Real-Time Backdrop Blur & Refraction Lens**:
 *    Samples the live screen [backdropGraphicsLayer] underneath the bar, blurs it (`26.dp`),
 *    and layers translucent optical glass tint, chromatic dispersion, and dual-rim specular catchlights.
 * 2. **Scroll-Down Minimize (`.tabBarMinimizeBehavior(.onScrollDown)`)**:
 *    When [isMinimized] is `true` (user scrolls down), the 5-tab capsule fluidly morphs into a
 *    compact floating Liquid Glass pill showing only the active tab. Tapping the minimized pill
 *    (or scrolling back up) immediately expands the full 5-tab bar.
 * 3. **Ambient Idle / Untouched State ([isIdle])**:
 *    When the user leaves the screen untouched for a few seconds, the glass enters an ambient
 *    ultra-translucent resting state (higher glass clarity, softened specular rim, compact height,
 *    and hidden inactive labels) until the user touches or scrolls again.
 * 4. **Interactive Liquid Lens Gesture**:
 *    Users can tap or drag horizontally across the capsule to slide the magnified liquid glass
 *    pill across tabs with tactile haptic feedback.
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

    val safeSelectedIndex = selectedIndex.coerceIn(0, items.lastIndex)
    val activeItem = items[safeSelectedIndex]

    // Interactive horizontal drag state for liquid lens magnification
    var isDraggingCapsule by remember { mutableStateOf(false) }

    // Ambient idle & scroll animations
    val barScale by animateFloatAsState(
        targetValue = when {
            isDraggingCapsule -> 1.02f
            isMinimized && isIdle -> 0.94f
            isIdle -> 0.97f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessLow
        ),
        label = "liquid_bar_scale"
    )

    val barVerticalOffsetDp by animateDpAsState(
        targetValue = when {
            isMinimized && isIdle -> 6.dp
            isMinimized -> 3.dp
            isIdle -> 4.dp
            else -> 0.dp
        },
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessLow
        ),
        label = "liquid_bar_offset_y"
    )

    val barAlpha by animateFloatAsState(
        targetValue = when {
            isMinimized && isIdle -> 0.78f
            isIdle -> 0.88f
            else -> 1f
        },
        animationSpec = tween(durationMillis = 420),
        label = "liquid_bar_alpha"
    )

    val capsuleHeight by animateDpAsState(
        targetValue = when {
            isMinimized -> 50.dp
            isIdle -> 54.dp
            else -> 64.dp
        },
        animationSpec = spring(
            dampingRatio = 0.74f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "liquid_capsule_height"
    )

    // Subtle ambient shimmer phase for liquid glass refraction
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_glass_shimmer")
    val refractionShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "refraction_shift"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .offset {
                IntOffset(0, with(density) { barVerticalOffsetDp.roundToPx() })
            }
            .scale(barScale)
            .alpha(barAlpha)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val capsuleShape = RoundedCornerShape(34.dp)

            // 1. iOS Liquid Glass Tab Capsule (Morphs between Full 5-Tab Bar and Minimized Pill)
            AnimatedContent(
                targetState = isMinimized,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220)) +
                        scaleIn(
                            initialScale = 0.86f,
                            animationSpec = spring(
                                dampingRatio = 0.72f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ))
                        .togetherWith(
                            fadeOut(animationSpec = tween(150)) +
                                scaleOut(targetScale = 0.88f, animationSpec = tween(160))
                        )
                        .using(
                            SizeTransform(
                                clip = false,
                                sizeAnimationSpec = { _, _ ->
                                    spring(
                                        dampingRatio = 0.74f,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                }
                            )
                        )
                },
                modifier = Modifier.weight(1f, fill = !isMinimized),
                label = "ios_liquid_glass_morph"
            ) { minimized ->
                if (minimized) {
                    // MINIMIZED STATE (Apple Music / Photos onScrollDown compact pill)
                    LiquidGlassSurface(
                        shape = capsuleShape,
                        isDark = isDark,
                        isIdle = isIdle,
                        isHighlighted = false,
                        refractionShift = refractionShift,
                        backdropGraphicsLayer = backdropGraphicsLayer,
                        rootOffsetInWindow = rootOffsetInWindow,
                        modifier = Modifier
                            .height(capsuleHeight)
                            .wrapContentWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onExpandRequest()
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Active tab glowing droplet badge
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                FinoraPrimary.copy(alpha = if (isDark) 0.42f else 0.22f),
                                                FinoraSecondary.copy(alpha = if (isDark) 0.28f else 0.14f)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 0.8.dp,
                                        color = Color.White.copy(alpha = if (isDark) 0.35f else 0.75f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = activeItem.selectedIcon,
                                    contentDescription = activeItem.label,
                                    tint = if (isDark) FinoraPrimaryLight else FinoraPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Text(
                                text = activeItem.label,
                                fontSize = 13.sp,
                                fontFamily = FinoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                maxLines = 1
                            )

                            // Mini pill dots indicator showing it can be expanded
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 2.dp)
                            ) {
                                repeat(3) { dotIndex ->
                                    Box(
                                        modifier = Modifier
                                            .size(if (dotIndex == 1) 4.5.dp else 3.5.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isDark) Color.White.copy(alpha = 0.45f)
                                                else Color(0xFF475569).copy(alpha = 0.45f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // EXPANDED FULL 5-TAB LIQUID GLASS CAPSULE
                    LiquidGlassSurface(
                        shape = capsuleShape,
                        isDark = isDark,
                        isIdle = isIdle,
                        isHighlighted = isDraggingCapsule,
                        refractionShift = refractionShift,
                        backdropGraphicsLayer = backdropGraphicsLayer,
                        rootOffsetInWindow = rootOffsetInWindow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(capsuleHeight)
                    ) {
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 5.dp, vertical = 5.dp)
                                .pointerInput(items.size) {
                                    detectHorizontalDragGestures(
                                        onDragStart = {
                                            isDraggingCapsule = true
                                        },
                                        onDragEnd = {
                                            isDraggingCapsule = false
                                        },
                                        onDragCancel = {
                                            isDraggingCapsule = false
                                        },
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
                        ) {
                            val tabCount = items.size
                            val tabWidth = maxWidth / tabCount

                            // Fluid spring-animated sliding active Liquid Lens pill offset
                            val indicatorOffset by animateDpAsState(
                                targetValue = tabWidth * safeSelectedIndex,
                                animationSpec = spring(
                                    dampingRatio = 0.72f,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "ios_liquid_sliding_pill"
                            )

                            // Liquid Lens magnification when pressed or dragged
                            val lensScaleX by animateFloatAsState(
                                targetValue = if (isDraggingCapsule) 1.08f else 1.0f,
                                animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
                                label = "lens_scale_x"
                            )
                            val lensScaleY by animateFloatAsState(
                                targetValue = if (isDraggingCapsule) 1.06f else 1.0f,
                                animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
                                label = "lens_scale_y"
                            )

                            // Sliding Active Liquid Glass Pill (Droplet Lens)
                            val pillShape = RoundedCornerShape(28.dp)
                            val activePillBrush = if (isDark) {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = if (isIdle) 0.14f else 0.22f),
                                        FinoraPrimary.copy(alpha = if (isIdle) 0.22f else 0.34f),
                                        FinoraSecondary.copy(alpha = if (isIdle) 0.14f else 0.22f)
                                    )
                                )
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = if (isIdle) 0.65f else 0.88f),
                                        FinoraPrimary.copy(alpha = if (isIdle) 0.12f else 0.20f),
                                        FinoraSecondary.copy(alpha = if (isIdle) 0.08f else 0.14f)
                                    )
                                )
                            }

                            val activePillBorderBrush = if (isDark) {
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.55f),
                                        FinoraPrimaryLight.copy(alpha = 0.40f),
                                        Color.White.copy(alpha = 0.12f)
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.98f),
                                        FinoraPrimary.copy(alpha = 0.32f),
                                        Color.White.copy(alpha = 0.65f)
                                    )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .offset(x = indicatorOffset)
                                    .width(tabWidth)
                                    .fillMaxHeight()
                                    .scale(scaleX = lensScaleX, scaleY = lensScaleY)
                                    .clip(pillShape)
                                    .background(brush = activePillBrush)
                                    .border(
                                        width = 1.dp,
                                        brush = activePillBorderBrush,
                                        shape = pillShape
                                    )
                            ) {
                                // Top curved liquid lens specular reflection inside the active pill
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    // Top gloss arc
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
                                                FinoraPrimaryLight.copy(alpha = if (isDark) 0.26f else 0.18f),
                                                Color.Transparent
                                            ),
                                            center = Offset(size.width / 2f, size.height * 0.85f),
                                            radius = size.width * 0.65f
                                        )
                                    )
                                }
                            }

                            // 5 Tab Items Row
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items.forEachIndexed { index, item ->
                                    val isSelected = safeSelectedIndex == index
                                    IosBottomTabItem(
                                        item = item,
                                        isSelected = isSelected,
                                        isIdle = isIdle,
                                        isDark = isDark,
                                        onClick = {
                                            if (selectedIndex != index) {
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
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // 2. Detached iOS Liquid Glass Circular Action Button (+)
            IosFloatingAddButton(
                isExpanded = isAddSheetOpen,
                isMinimized = isMinimized,
                isIdle = isIdle,
                isDark = isDark,
                backdropGraphicsLayer = backdropGraphicsLayer,
                rootOffsetInWindow = rootOffsetInWindow,
                onClick = onAddClick
            )
        }
    }
}

/**
 * Reusable iOS 26 Liquid Glass Container.
 * Renders real-time blurred background from [backdropGraphicsLayer] with translucent glass tint,
 * chromatic edge dispersion, and dual-rim specular catchlights.
 */
@Composable
private fun LiquidGlassSurface(
    shape: Shape,
    isDark: Boolean,
    isIdle: Boolean,
    isHighlighted: Boolean,
    refractionShift: Float,
    backdropGraphicsLayer: GraphicsLayer?,
    rootOffsetInWindow: Offset,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var surfaceOffsetInWindow by remember { mutableStateOf(Offset.Zero) }

    // Translucent glass base alpha: becomes even clearer when untouched (isIdle)
    val baseGlassColor by animateColorAsState(
        targetValue = when {
            isDark && isIdle -> Color(0xFF0F172A).copy(alpha = 0.36f)
            isDark && isHighlighted -> Color(0xFF1E293B).copy(alpha = 0.60f)
            isDark -> Color(0xFF0F172A).copy(alpha = 0.50f)
            !isDark && isIdle -> Color(0xFFFFFFFF).copy(alpha = 0.42f)
            !isDark && isHighlighted -> Color(0xFFFFFFFF).copy(alpha = 0.68f)
            else -> Color(0xFFFFFFFF).copy(alpha = 0.55f)
        },
        animationSpec = tween(durationMillis = 350),
        label = "glass_base_tint"
    )

    val borderAlphaMultiplier by animateFloatAsState(
        targetValue = when {
            isHighlighted -> 1.25f
            isIdle -> 0.55f
            else -> 1f
        },
        animationSpec = tween(durationMillis = 350),
        label = "glass_border_alpha"
    )

    // Dual-rim specular catchlights (top-left bright reflection + bottom-right glass refraction rim)
    val specularBorderBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = (0.48f * borderAlphaMultiplier).coerceAtMost(0.95f)),
                FinoraPrimaryLight.copy(alpha = (0.28f * borderAlphaMultiplier).coerceAtMost(0.8f)),
                Color.White.copy(alpha = (0.08f * borderAlphaMultiplier).coerceAtMost(0.5f)),
                FinoraSecondary.copy(alpha = (0.24f * borderAlphaMultiplier).coerceAtMost(0.8f)),
                Color.White.copy(alpha = (0.35f * borderAlphaMultiplier).coerceAtMost(0.9f))
            ),
            start = Offset(0f, 0f),
            end = Offset(900f, 260f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = (0.95f * borderAlphaMultiplier).coerceAtMost(1f)),
                FinoraPrimaryLight.copy(alpha = (0.35f * borderAlphaMultiplier).coerceAtMost(0.85f)),
                Color(0xFFCBD5E1).copy(alpha = (0.45f * borderAlphaMultiplier).coerceAtMost(0.85f)),
                Color.White.copy(alpha = (0.90f * borderAlphaMultiplier).coerceAtMost(1f))
            ),
            start = Offset(0f, 0f),
            end = Offset(900f, 260f)
        )
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { coords ->
                surfaceOffsetInWindow = coords.positionInWindow()
            }
            .shadow(
                elevation = if (isIdle) 10.dp else if (isDark) 22.dp else 16.dp,
                shape = shape,
                ambientColor = if (isDark) Color.Black.copy(alpha = 0.65f) else FinoraPrimary.copy(alpha = 0.20f),
                spotColor = if (isDark) Color.Black.copy(alpha = 0.75f) else FinoraPrimary.copy(alpha = 0.25f)
            )
            .clip(shape)
    ) {
        // Layer 1: Live Blurred Backdrop (Real Optical Refraction of content scrolling behind the glass)
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

            // Top curved glass surface specular reflection
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDark) 0.16f * borderAlphaMultiplier else 0.55f * borderAlphaMultiplier),
                        Color.White.copy(alpha = if (isDark) 0.03f else 0.12f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h * 0.55f
                )
            )

            // Moving liquid prismatic dispersion highlight
            val glowCenterX = w * (0.2f + 0.6f * refractionShift)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        FinoraPrimaryLight.copy(alpha = if (isDark) 0.12f * borderAlphaMultiplier else 0.10f * borderAlphaMultiplier),
                        FinoraSecondary.copy(alpha = if (isDark) 0.06f * borderAlphaMultiplier else 0.05f * borderAlphaMultiplier),
                        Color.Transparent
                    ),
                    center = Offset(glowCenterX, h * 0.5f),
                    radius = w * 0.55f
                )
            )

            // Bottom inner rim bounce light
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = if (isDark) 0.07f * borderAlphaMultiplier else 0.30f * borderAlphaMultiplier)
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

        // Foreground Content
        content()
    }
}

@Composable
private fun IosBottomTabItem(
    item: IosTabItemData,
    isSelected: Boolean,
    isIdle: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.86f
            isSelected -> 1.0f
            else -> 0.96f
        },
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ios_tab_scale"
    )

    val iconBounce by animateFloatAsState(
        targetValue = when {
            isSelected && !isIdle -> 1.12f
            isSelected && isIdle -> 1.05f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "ios_icon_bounce"
    )

    val activeColor = if (isDark) Color.White else FinoraPrimaryDark
    val activeIconTint = if (isDark) FinoraPrimaryLight else FinoraPrimary
    val inactiveColor = if (isDark) {
        Color(0xFFCBD5E1).copy(alpha = if (isIdle) 0.60f else 0.85f)
    } else {
        Color(0xFF334155).copy(alpha = if (isIdle) 0.65f else 0.88f)
    }

    val animatedIconTint by animateColorAsState(
        targetValue = if (isSelected) activeIconTint else inactiveColor,
        animationSpec = tween(durationMillis = 220),
        label = "ios_tab_icon_color"
    )

    val animatedTextColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 220),
        label = "ios_tab_text_color"
    )

    // In ambient idle mode, inactive tab labels gently hide so the glass bar looks ultra-clean
    val showLabel = !isIdle || isSelected

    Column(
        modifier = modifier
            .scale(pressScale)
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
                .size(if (isIdle && !isSelected) 20.dp else 21.dp)
                .scale(iconBounce)
        )

        AnimatedVisibility(
            visible = showLabel,
            enter = fadeIn(tween(180)) + expandVertically(tween(200)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(180))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                    letterSpacing = (-0.15).sp
                )
            }
        }
    }
}

@Composable
fun IosFloatingAddButton(
    isExpanded: Boolean = false,
    isMinimized: Boolean = false,
    isIdle: Boolean = false,
    isDark: Boolean = true,
    backdropGraphicsLayer: GraphicsLayer? = null,
    rootOffsetInWindow: Offset = Offset.Zero,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var buttonOffsetInWindow by remember { mutableStateOf(Offset.Zero) }

    val buttonSize: Dp by animateDpAsState(
        targetValue = when {
            isMinimized -> 50.dp
            isIdle -> 52.dp
            else -> 60.dp
        },
        animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow),
        label = "ios_add_size"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.86f else 1f,
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
            .onGloballyPositioned { coords ->
                buttonOffsetInWindow = coords.positionInWindow()
            }
            .scale(scale)
            .size(buttonSize)
            .shadow(
                elevation = if (isIdle) 10.dp else 18.dp,
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
        // Real-time blurred backdrop behind the circular Liquid Glass (+) button
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

        // Translucent vibrant Liquid Glass tint for the (+) button
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            FinoraPrimary.copy(alpha = if (isIdle) 0.72f else 0.86f),
                            Color(0xFF3B82F6).copy(alpha = if (isIdle) 0.68f else 0.82f),
                            FinoraSecondary.copy(alpha = if (isIdle) 0.72f else 0.86f)
                        )
                    )
                )
        )

        // Liquid lens top highlight & rim border
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
                        Color.White.copy(alpha = 0.75f),
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.45f)
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
                .size(if (isMinimized) 22.dp else 26.dp)
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
