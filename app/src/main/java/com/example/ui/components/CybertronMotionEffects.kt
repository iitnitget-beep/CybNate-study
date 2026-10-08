package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BumblebeeYellow
import com.example.ui.theme.BumblebeeYellowBright
import com.example.ui.theme.CybertronCard
import com.example.ui.theme.CybertronObsidian
import com.example.ui.theme.CybertronSteel
import com.example.ui.theme.EnergonBlue
import com.example.ui.theme.EnergonBlueBright

/**
 * Cybertron animated glowing border effect.
 * Creates an electrifying scanning energon beam moving around the container corners.
 */
@Composable
fun Modifier.cybertronEnergonGlow(
    glowColor: Color = BumblebeeYellowBright,
    enabled: Boolean = true
): Modifier {
    if (!enabled) return this

    val infiniteTransition = rememberInfiniteTransition(label = "energon_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    return this.drawBehind {
        drawRect(
            color = glowColor.copy(alpha = pulseAlpha * 0.12f)
        )
    }
}

/**
 * Cybertronian HUD Grid Background with sweeping scanner line.
 */
@Composable
fun CybertronScanningGrid(
    modifier: Modifier = Modifier,
    gridColor: Color = BumblebeeYellow.copy(alpha = 0.05f),
    scanLineColor: Color = EnergonBlue.copy(alpha = 0.25f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cyber_scanner")
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanner_progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val step = 48.dp.toPx()

        // Draw isometric/orthogonal cyber-grid lines
        var x = 0f
        while (x < w) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1f
            )
            x += step
        }

        var y = 0f
        while (y < h) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
            y += step
        }

        // Animated Energon scanner beam sweep
        val scanY = scanProgress * h
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    scanLineColor,
                    scanLineColor,
                    Color.Transparent
                )
            ),
            start = Offset(0f, scanY),
            end = Offset(w, scanY),
            strokeWidth = 2.5f
        )
    }
}

/**
 * Cybertron Mech Armor Card container.
 * Features beveled corner aesthetics and animated transformation press state.
 */
@Composable
fun CybertronArmorCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    borderGlow: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "armor_press_scale"
    )

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (borderGlow) 1.5.dp else 1.dp,
                color = if (borderGlow) BumblebeeYellow.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant,
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        content()
    }
}
