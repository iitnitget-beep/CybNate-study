package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BumblebeeYellow
import com.example.ui.theme.BumblebeeYellowBright
import com.example.ui.theme.EnergonBlue
import com.example.ui.theme.EnergonBlueBright

/**
 * Animated Cybertronian Bumblebee Face Logo.
 * Features:
 * - Cybertron Autobot yellow helmet & horn crests
 * - Pulsing energized Energon Blue visor optics with living cybernetic breathing glow
 * - High-precision mouthplate & aerodynamic chin vents
 */
@Composable
fun BumblebeeFaceLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    tint: Color = BumblebeeYellowBright,
    animatedOptics: Boolean = true,
    opticsColor: Color = EnergonBlue
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bumblebee_optics_pulse")
    val opticsAlpha by infiniteTransition.animateFloat(
        initialValue = if (animatedOptics) 0.65f else 1f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "opticsPulseAlpha"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // 1. Central Forehead Crest / Horns (Autobot yellow)
        val crestPath = Path().apply {
            moveTo(w * 0.50f, h * 0.12f)
            lineTo(w * 0.58f, h * 0.22f)
            lineTo(w * 0.53f, h * 0.32f)
            lineTo(w * 0.47f, h * 0.32f)
            lineTo(w * 0.42f, h * 0.22f)
            close()
        }
        drawPath(crestPath, color = tint, style = Fill)

        // 2. Left and Right Crest Fins (Antennae/Horns)
        val leftFinPath = Path().apply {
            moveTo(w * 0.38f, h * 0.20f)
            lineTo(w * 0.26f, h * 0.14f)
            lineTo(w * 0.30f, h * 0.30f)
            lineTo(w * 0.39f, h * 0.32f)
            close()
        }
        drawPath(leftFinPath, color = tint, style = Fill)

        val rightFinPath = Path().apply {
            moveTo(w * 0.62f, h * 0.20f)
            lineTo(w * 0.74f, h * 0.14f)
            lineTo(w * 0.70f, h * 0.30f)
            lineTo(w * 0.61f, h * 0.32f)
            close()
        }
        drawPath(rightFinPath, color = tint, style = Fill)

        // 3. Helmet Brow / Forehead Band (Stinger Racing Accent)
        val browPath = Path().apply {
            moveTo(w * 0.25f, h * 0.35f)
            lineTo(w * 0.50f, h * 0.38f)
            lineTo(w * 0.75f, h * 0.35f)
            lineTo(w * 0.80f, h * 0.45f)
            lineTo(w * 0.70f, h * 0.46f)
            lineTo(w * 0.50f, h * 0.44f)
            lineTo(w * 0.30f, h * 0.46f)
            lineTo(w * 0.20f, h * 0.45f)
            close()
        }
        drawPath(browPath, color = tint, style = Fill)

        // 4. Bumblebee Visor Optics (Pulsing Animated Energon Cyan)
        val leftEyePath = Path().apply {
            moveTo(w * 0.32f, h * 0.50f)
            lineTo(w * 0.44f, h * 0.50f)
            lineTo(w * 0.41f, h * 0.58f)
            lineTo(w * 0.34f, h * 0.56f)
            close()
        }
        val currentOpticsColor = opticsColor.copy(alpha = opticsAlpha)
        drawPath(leftEyePath, color = currentOpticsColor, style = Fill)

        val rightEyePath = Path().apply {
            moveTo(w * 0.56f, h * 0.50f)
            lineTo(w * 0.68f, h * 0.50f)
            lineTo(w * 0.66f, h * 0.56f)
            lineTo(w * 0.59f, h * 0.58f)
            close()
        }
        drawPath(rightEyePath, color = currentOpticsColor, style = Fill)

        // 5. Cheeks and Face Guard Panels
        val leftCheekPath = Path().apply {
            moveTo(w * 0.22f, h * 0.48f)
            lineTo(w * 0.30f, h * 0.50f)
            lineTo(w * 0.32f, h * 0.65f)
            lineTo(w * 0.26f, h * 0.72f)
            lineTo(w * 0.18f, h * 0.58f)
            close()
        }
        drawPath(leftCheekPath, color = tint, style = Fill)

        val rightCheekPath = Path().apply {
            moveTo(w * 0.78f, h * 0.48f)
            lineTo(w * 0.70f, h * 0.50f)
            lineTo(w * 0.68f, h * 0.65f)
            lineTo(w * 0.74f, h * 0.72f)
            lineTo(w * 0.82f, h * 0.58f)
            close()
        }
        drawPath(rightCheekPath, color = tint, style = Fill)

        // 6. Central Nose Bridge and Mouthplate / Chin Guard
        val mouthplatePath = Path().apply {
            moveTo(w * 0.48f, h * 0.52f)
            lineTo(w * 0.52f, h * 0.52f)
            lineTo(w * 0.54f, h * 0.64f)
            lineTo(w * 0.62f, h * 0.72f)
            lineTo(w * 0.56f, h * 0.84f)
            lineTo(w * 0.50f, h * 0.88f)
            lineTo(w * 0.44f, h * 0.84f)
            lineTo(w * 0.38f, h * 0.72f)
            lineTo(w * 0.46f, h * 0.64f)
            close()
        }
        drawPath(mouthplatePath, color = tint, style = Fill)

        // 7. Proportional chin vents
        val ventStrokeWidth = maxOf(1.5f, w * 0.03f)
        val vent1 = Path().apply {
            moveTo(w * 0.44f, h * 0.75f)
            lineTo(w * 0.56f, h * 0.75f)
        }
        val vent2 = Path().apply {
            moveTo(w * 0.46f, h * 0.80f)
            lineTo(w * 0.54f, h * 0.80f)
        }
        val ventColor = Color.Black.copy(alpha = 0.4f)
        val strokeStyle = Stroke(width = ventStrokeWidth, cap = StrokeCap.Round)
        drawPath(vent1, color = ventColor, style = strokeStyle)
        drawPath(vent2, color = ventColor, style = strokeStyle)
    }
}
