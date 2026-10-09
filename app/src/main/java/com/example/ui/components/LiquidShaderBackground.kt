package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.ThemePreferences
import kotlin.math.cos
import kotlin.math.sin

/**
 * Palette configuration for Liquid Shader Gradients across different screens.
 */
enum class LiquidThemeVariant {
    AUTO_PRESET,       // Adapts automatically to user's selected 5 AppThemePresets
    CYBER_LIQUID,      // Amber Gold, Neon Energon Blue, Deep Indigo (Home / Dashboard)
    METALLIC_SHINE,    // Platinum Chrome, Brushed Steel, Titanium Sheen (Metallic Theme)
    NEON_VIBE,         // Violet, Cyan, Fuchsia (Practice / Chapters)
    AURORA_CALM,       // Emerald, Mint, Electric Cyan (Analysis / Stats)
    SUNSET_GLASS,      // Warm Coral, Sunset Amber, Magenta (Test / Instructions)
    DEEP_OCEAN         // Midnight Navy, Azure, Electric Teal (Profile / History)
}

/**
 * Liquid Shader Gradient Canvas Background:
 * Inspired by react-three-fiber mesh shaders, ruucm/shadergradient, and liquid-logo.
 * Features continuous non-resetting organic time and multi-directional floating bubbles.
 */
@Composable
fun LiquidShaderBackground(
    modifier: Modifier = Modifier,
    variant: LiquidThemeVariant = LiquidThemeVariant.AUTO_PRESET,
    isDarkTheme: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val activePreset by ThemePreferences.themePreset.collectAsState()

    // Continuous real-time monotonically increasing clock (in seconds)
    // Runs endlessly WITHOUT repeating, looping, or restarting like a video!
    val continuousTime by produceState(initialValue = 0f) {
        val startNanos = System.nanoTime()
        while (true) {
            withFrameNanos { frameTimeNanos ->
                value = (frameTimeNanos - startNanos) / 1_000_000_000f
            }
        }
    }

    val effectiveVariant = if (variant == LiquidThemeVariant.AUTO_PRESET) {
        when (activePreset) {
            AppThemePreset.CYBER_GOLD -> LiquidThemeVariant.CYBER_LIQUID
            AppThemePreset.METALLIC_TITANIUM -> LiquidThemeVariant.METALLIC_SHINE
            AppThemePreset.NEON_CYBERPUNK -> LiquidThemeVariant.NEON_VIBE
            AppThemePreset.AURORA_EMERALD -> LiquidThemeVariant.AURORA_CALM
            AppThemePreset.DEEP_OCEAN -> LiquidThemeVariant.DEEP_OCEAN
        }
    } else {
        variant
    }

    // Palette selection based on variant
    val (baseColor, color1, color2, color3, color4) = when (effectiveVariant) {
        LiquidThemeVariant.AUTO_PRESET,
        LiquidThemeVariant.CYBER_LIQUID -> if (isDarkTheme) {
            Tuple5(
                Color(0xFF090B10), // Base Obsidian
                Color(0xFFFFB800), // Vibrant Amber Gold
                Color(0xFF00E5FF), // Electric Energon Cyan
                Color(0xFF6366F1), // Royal Indigo
                Color(0xFFFF5E7E)  // Electric Coral
            )
        } else {
            Tuple5(
                Color(0xFFF6F8FC), // Soft Off-white
                Color(0xFFFBBF24), // Warm Marigold
                Color(0xFF38BDF8), // Sky Liquid Cyan
                Color(0xFF818CF8), // Pastel Periwinkle
                Color(0xFFFDA4AF)  // Soft Rose
            )
        }
        LiquidThemeVariant.METALLIC_SHINE -> if (isDarkTheme) {
            Tuple5(
                Color(0xFF0B0E14), // Deep Titanium Obsidian
                Color(0xFFE2E8F0), // Polished Platinum Chrome
                Color(0xFF94A3B8), // Brushed Silver Steel
                Color(0xFF64748B), // Slate Gunmetal Alloy
                Color(0xFFCBD5E1)  // Specular Reflection Highlight
            )
        } else {
            Tuple5(
                Color(0xFFF1F5F9), // Silver Mist
                Color(0xFF94A3B8), // Brushed Titanium
                Color(0xFF64748B), // Slate Steel
                Color(0xFFCBD5E1), // Platinum
                Color(0xFFE2E8F0)  // Polished Chrome
            )
        }
        LiquidThemeVariant.NEON_VIBE -> if (isDarkTheme) {
            Tuple5(
                Color(0xFF0B0914), // Deep Violet Black
                Color(0xFFA855F7), // Liquid Violet
                Color(0xFF06B6D4), // Liquid Cyan
                Color(0xFFEC4899), // Neon Fuchsia
                Color(0xFF3B82F6)  // Vivid Blue
            )
        } else {
            Tuple5(
                Color(0xFFF9F5FF),
                Color(0xFFC084FC),
                Color(0xFF67E8F9),
                Color(0xFFF472B6),
                Color(0xFF93C5FD)
            )
        }
        LiquidThemeVariant.AURORA_CALM -> if (isDarkTheme) {
            Tuple5(
                Color(0xFF06100E),
                Color(0xFF10B981), // Emerald
                Color(0xFF06B6D4), // Cyan
                Color(0xFF3B82F6), // Blue
                Color(0xFF84CC16)  // Lime
            )
        } else {
            Tuple5(
                Color(0xFFF0FDF4),
                Color(0xFF34D399),
                Color(0xFF38BDF8),
                Color(0xFF60A5FA),
                Color(0xFFA3E635)
            )
        }
        LiquidThemeVariant.SUNSET_GLASS -> if (isDarkTheme) {
            Tuple5(
                Color(0xFF12080C),
                Color(0xFFF43F5E), // Rose
                Color(0xFFF59E0B), // Warm Amber
                Color(0xFF8B5CF6), // Purple
                Color(0xFFFF7A00)  // Vivid Orange
            )
        } else {
            Tuple5(
                Color(0xFFFFF7ED),
                Color(0xFFFB7185),
                Color(0xFFFBBF24),
                Color(0xFFA78BFA),
                Color(0xFFFB923C)
            )
        }
        LiquidThemeVariant.DEEP_OCEAN -> if (isDarkTheme) {
            Tuple5(
                Color(0xFF050B14),
                Color(0xFF0284C7), // Deep Azure
                Color(0xFF0D9488), // Teal
                Color(0xFF4F46E5), // Indigo
                Color(0xFF06B6D4)  // Cyan
            )
        } else {
            Tuple5(
                Color(0xFFF0F9FF),
                Color(0xFF38BDF8),
                Color(0xFF2DD4BF),
                Color(0xFF818CF8),
                Color(0xFF67E8F9)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val t = continuousTime

            // Multi-frequency incommensurate harmonic motion:
            // Using irrational frequency ratios so orbits NEVER repeat or jump!
            val c1X = w * (0.50f + 0.35f * sin(t * 0.18f).toFloat() + 0.08f * cos(t * 0.43f).toFloat())
            val c1Y = h * (0.28f + 0.22f * cos(t * 0.15f).toFloat() + 0.07f * sin(t * 0.37f).toFloat())
            val r1 = (w.coerceAtLeast(h) * 0.55f)

            val c2X = w * (0.35f + 0.30f * cos(t * 0.21f + 1.57f).toFloat() + 0.09f * sin(t * 0.39f).toFloat())
            val c2Y = h * (0.70f + 0.22f * sin(t * 0.17f + 2.14f).toFloat() + 0.06f * cos(t * 0.45f).toFloat())
            val r2 = (w.coerceAtLeast(h) * 0.50f)

            val c3X = w * (0.75f + 0.25f * sin(t * 0.14f + 3.14f).toFloat() + 0.08f * cos(t * 0.31f).toFloat())
            val c3Y = h * (0.60f + 0.25f * cos(t * 0.23f + 0.78f).toFloat() + 0.07f * sin(t * 0.41f).toFloat())
            val r3 = (w.coerceAtLeast(h) * 0.45f)

            val c4X = w * (0.20f + 0.25f * cos(t * 0.19f + 4.28f).toFloat() + 0.06f * sin(t * 0.33f).toFloat())
            val c4Y = h * (0.35f + 0.22f * sin(t * 0.16f + 1.25f).toFloat() + 0.08f * cos(t * 0.29f).toFloat())
            val r4 = (w.coerceAtLeast(h) * 0.40f)

            val alphaMultiplier = if (isDarkTheme) 0.38f else 0.24f

            // Attractor 1:
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color1.copy(alpha = alphaMultiplier * 1.0f),
                        color1.copy(alpha = alphaMultiplier * 0.45f),
                        Color.Transparent
                    ),
                    center = Offset(c1X, c1Y),
                    radius = r1
                ),
                radius = r1,
                center = Offset(c1X, c1Y)
            )

            // Attractor 2:
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color2.copy(alpha = alphaMultiplier * 0.95f),
                        color2.copy(alpha = alphaMultiplier * 0.40f),
                        Color.Transparent
                    ),
                    center = Offset(c2X, c2Y),
                    radius = r2
                ),
                radius = r2,
                center = Offset(c2X, c2Y)
            )

            // Attractor 3:
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color3.copy(alpha = alphaMultiplier * 0.85f),
                        color3.copy(alpha = alphaMultiplier * 0.35f),
                        Color.Transparent
                    ),
                    center = Offset(c3X, c3Y),
                    radius = r3
                ),
                radius = r3,
                center = Offset(c3X, c3Y)
            )

            // Attractor 4:
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color4.copy(alpha = alphaMultiplier * 0.75f),
                        color4.copy(alpha = alphaMultiplier * 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(c4X, c4Y),
                    radius = r4
                ),
                radius = r4,
                center = Offset(c4X, c4Y)
            )

            // Continuous floating translucent bubbles wandering smoothly in random directions
            // No video-style restarts; perpetual smooth floating motion
            val bubbleSeeds = listOf(
                BubbleSeed(0.25f, 0.35f, 0.12f, 0.17f, 38.dp.toPx(), color1),
                BubbleSeed(0.70f, 0.20f, 0.16f, 0.11f, 52.dp.toPx(), color2),
                BubbleSeed(0.40f, 0.75f, 0.14f, 0.19f, 44.dp.toPx(), color3),
                BubbleSeed(0.85f, 0.65f, 0.11f, 0.15f, 32.dp.toPx(), color4),
                BubbleSeed(0.15f, 0.85f, 0.18f, 0.13f, 48.dp.toPx(), color1),
                BubbleSeed(0.60f, 0.45f, 0.13f, 0.22f, 28.dp.toPx(), color2)
            )

            for (seed in bubbleSeeds) {
                // Smooth wandering in random directions
                val bx = w * (seed.baseX + 0.18f * sin(t * seed.speedX + seed.baseX * 10f).toFloat() + 0.08f * cos(t * seed.speedY * 0.7f).toFloat())
                val by = h * (seed.baseY + 0.18f * cos(t * seed.speedY + seed.baseY * 10f).toFloat() + 0.08f * sin(t * seed.speedX * 0.9f).toFloat())

                // Draw soft translucent bubble core
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            seed.color.copy(alpha = 0.22f),
                            seed.color.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(bx, by),
                        radius = seed.radius
                    ),
                    radius = seed.radius,
                    center = Offset(bx, by)
                )

                // Delicate luminous glass rim
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f),
                    radius = seed.radius * 0.85f,
                    center = Offset(bx, by),
                    style = Stroke(width = 1.2f)
                )

                // Glint highlight on top-left of bubble
                drawCircle(
                    color = Color.White.copy(alpha = 0.35f),
                    radius = seed.radius * 0.18f,
                    center = Offset(bx - seed.radius * 0.35f, by - seed.radius * 0.35f)
                )
            }
        }

        content()
    }
}

private data class BubbleSeed(
    val baseX: Float,
    val baseY: Float,
    val speedX: Float,
    val speedY: Float,
    val radius: Float,
    val color: Color
)

/**
 * Liquid Glassmorphic Surface Container (liquid-glass-js & react-three-fiber aesthetic).
 * Offers:
 * - High-specular specular reflection highlight at top edge
 * - Subtle inner frosted tint
 * - Chromatic aberration edge rim
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    tintColor: Color = MaterialTheme.colorScheme.surface,
    frostAlpha: Float = 0.65f,
    borderGlowColor: Color = Color.White.copy(alpha = 0.22f),
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        tintColor.copy(alpha = frostAlpha + 0.15f),
                        tintColor.copy(alpha = frostAlpha)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        borderGlowColor,
                        borderGlowColor.copy(alpha = 0.05f),
                        borderGlowColor.copy(alpha = 0.25f)
                    )
                ),
                shape = shape
            )
            .drawBehind {
                // Top-left specular liquid glass sheen
                val specularPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width * 0.45f, 0f)
                    lineTo(0f, size.height * 0.35f)
                    close()
                }
                drawPath(
                    path = specularPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
            }
    ) {
        content()
    }
}

/**
 * Helper tuple container
 */
private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
