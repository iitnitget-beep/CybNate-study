package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Liquid Logo Component:
 * Inspired by collidingScopes/liquid-logo and Three.js physics blobs.
 * Features an organic morphing liquid blob with fluid waviness, glossy specular shine,
 * and high-contrast liquid rim highlights.
 */
@Composable
fun LiquidMorphingLogo(
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    primaryColor: Color = Color(0xFFFFB800),
    secondaryColor: Color = Color(0xFF00E5FF),
    accentColor: Color = Color(0xFFEC4899)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_logo_morph")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "liquid_phase"
    )

    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "liquid_spin"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val center = Offset(w / 2f, h / 2f)
            val baseRadius = (w.coerceAtMost(h) / 2f) * 0.78f

            // 1. Generate Morphing Blob Contour using radial harmonic perturbation
            val pointsCount = 48
            val path = Path()

            for (i in 0..pointsCount) {
                val angle = (i.toFloat() / pointsCount) * (2 * PI).toFloat()
                // 3 harmonic wave frequencies for liquid ripple effect
                val rOffset = baseRadius * (
                    1.0f +
                    0.13f * sin(3f * angle + phase) +
                    0.09f * cos(2f * angle - phase * 1.3f) +
                    0.05f * sin(5f * angle + phase * 0.8f)
                )

                val x = center.x + rOffset * cos(angle)
                val y = center.y + rOffset * sin(angle)

                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            path.close()

            // 2. Liquid Body Gradient Fill
            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor,
                        secondaryColor,
                        accentColor
                    ),
                    center = Offset(center.x * 0.85f, center.y * 0.80f),
                    radius = baseRadius * 1.3f
                ),
                style = Fill
            )

            // 3. Liquid Rim Glow Stroke (Glass refraction edge)
            drawPath(
                path = path,
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.8f),
                        primaryColor.copy(alpha = 0.4f),
                        secondaryColor.copy(alpha = 0.8f),
                        accentColor.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.8f)
                    ),
                    center = center
                ),
                style = Stroke(width = 2.5f)
            )

            // 4. Specular Liquid Bubble Highlight (Glass reflection)
            val specCenterX = center.x - baseRadius * 0.32f
            val specCenterY = center.y - baseRadius * 0.32f
            val specRadius = baseRadius * 0.36f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.70f),
                        Color.White.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(specCenterX, specCenterY),
                    radius = specRadius
                ),
                radius = specRadius,
                center = Offset(specCenterX, specCenterY)
            )

            // Secondary mini liquid droplet highlight
            val miniSpecX = center.x + baseRadius * 0.28f
            val miniSpecY = center.y + baseRadius * 0.28f
            drawCircle(
                color = Color.White.copy(alpha = 0.45f),
                radius = baseRadius * 0.08f,
                center = Offset(miniSpecX, miniSpecY)
            )
        }
    }
}
