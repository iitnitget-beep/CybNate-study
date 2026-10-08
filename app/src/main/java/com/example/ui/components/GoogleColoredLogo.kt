package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Authentic Google "G" multicolored logo rendered using Canvas geometry:
 * - Red (#EA4335)
 * - Yellow (#FBBC05)
 * - Green (#34A853)
 * - Blue (#4285F4)
 */
@Composable
fun GoogleColoredLogo(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = w / 2f
        val innerR = r * 0.58f

        // Google Official Brand Colors
        val blue = Color(0xFF4285F4)
        val green = Color(0xFF34A853)
        val yellow = Color(0xFFFBBC05)
        val red = Color(0xFFEA4335)

        // 1. Blue horizontal crossbar and right sector seamlessly joined, clipped flush with inner radius
        val barHeight = h * 0.18f
        val bluePath = Path().apply {
            moveTo(cx + innerR, cy - barHeight / 2f)
            lineTo(w, cy - barHeight / 2f)
            arcTo(
                rect = Rect(0f, 0f, w, h),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 45f,
                forceMoveTo = false
            )
            arcTo(
                rect = Rect(cx - innerR, cy - innerR, cx + innerR, cy + innerR),
                startAngleDegrees = 45f,
                sweepAngleDegrees = -45f,
                forceMoveTo = false
            )
            lineTo(cx + innerR, cy + barHeight / 2f)
            close()
        }
        drawPath(bluePath, blue, style = Fill)

        // 2. Green bottom arc (45 deg to 140 deg)
        val greenPath = Path().apply {
            arcTo(
                rect = Rect(0f, 0f, w, h),
                startAngleDegrees = 45f,
                sweepAngleDegrees = 95f,
                forceMoveTo = true
            )
            arcTo(
                rect = Rect(cx - innerR, cy - innerR, cx + innerR, cy + innerR),
                startAngleDegrees = 140f,
                sweepAngleDegrees = -95f,
                forceMoveTo = false
            )
            close()
        }
        drawPath(greenPath, green, style = Fill)

        // 3. Yellow left arc (140 deg to 220 deg)
        val yellowPath = Path().apply {
            arcTo(
                rect = Rect(0f, 0f, w, h),
                startAngleDegrees = 140f,
                sweepAngleDegrees = 80f,
                forceMoveTo = true
            )
            arcTo(
                rect = Rect(cx - innerR, cy - innerR, cx + innerR, cy + innerR),
                startAngleDegrees = 220f,
                sweepAngleDegrees = -80f,
                forceMoveTo = false
            )
            close()
        }
        drawPath(yellowPath, yellow, style = Fill)

        // 4. Red top arc (220 deg to 360 deg / 0 deg, continuous with blue arc at 360°)
        val redPath = Path().apply {
            arcTo(
                rect = Rect(0f, 0f, w, h),
                startAngleDegrees = 220f,
                sweepAngleDegrees = 140f,
                forceMoveTo = true
            )
            arcTo(
                rect = Rect(cx - innerR, cy - innerR, cx + innerR, cy + innerR),
                startAngleDegrees = 360f,
                sweepAngleDegrees = -140f,
                forceMoveTo = false
            )
            close()
        }
        drawPath(redPath, red, style = Fill)
    }
}
