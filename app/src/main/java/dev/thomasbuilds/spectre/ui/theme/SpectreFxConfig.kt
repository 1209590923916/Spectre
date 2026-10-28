package dev.thomasbuilds.spectre.ui.theme

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class SpectreFxConfig(
  val glowIntensity: Float = 1f,
  val glowSpeed: Float = 1f,
  val colorShiftSpeed: Float = 0.5f,
  val gradientRotationSpeed: Float = 4f,
  val particleDensity: Float = 1f,
  val effectScale: Float = 1f,
  val enableColorShift: Boolean = false,
  val enableScanlineEffect: Boolean = true,
  val enablePressFeedback: Boolean = true,
  val enableSignalPulse: Boolean = true
) {
  val glowDurationMs: Int get() = (1800 / glowSpeed).toInt().coerceAtLeast(200)
  val gradientDurationMs: Int get() = (gradientRotationSpeed * 1000).toInt()
}

val LocalSpectreFxConfig = staticCompositionLocalOf { SpectreFxConfig() }

fun Modifier.spectreGlow(
  color: Color,
  cornerRadius: Dp = 12.dp,
  borderWidth: Dp = 2.dp,
  minAlpha: Float = 0.2f,
  maxAlpha: Float = 0.6f
): Modifier = composed {
  val config = LocalSpectreFxConfig.current
  if (config.glowIntensity <= 0f || maxAlpha <= 0f) return@composed this

  val transition = rememberInfiniteTransition(label = "spectreGlow")
  val alpha by transition.animateFloat(
    initialValue = (minAlpha * config.glowIntensity).coerceAtMost(maxAlpha * config.glowIntensity),
    targetValue = (maxAlpha * config.glowIntensity).coerceAtMost(1f),
    animationSpec = infiniteRepeatable(
      animation = tween(config.glowDurationMs, easing = EaseInOut),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glowAlpha"
  )

  drawBehind {
    val radius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
    val stroke = borderWidth.toPx()

    drawRoundRect(
      color = color.copy(alpha = alpha * 0.4f),
      cornerRadius = radius,
      style = Stroke(stroke * 3f)
    )
    drawRoundRect(
      color = color.copy(alpha = alpha),
      cornerRadius = radius,
      style = Stroke(stroke)
    )
  }
}

fun Modifier.spectreGradientBorder(
  colors: List<Color>,
  borderWidth: Dp = 2.dp,
  cornerRadius: Dp = 12.dp
): Modifier = composed {
  val config = LocalSpectreFxConfig.current

  val transition = rememberInfiniteTransition(label = "spectreGradient")
  val rotation by transition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(config.gradientDurationMs, easing = LinearEasing)
    ),
    label = "rotation"
  )

  drawBehind {
    val stroke = borderWidth.toPx()
    val radius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())

    rotate(degrees = rotation, pivot = center) {
      val brush = Brush.sweepGradient(
        colors = colors,
        center = center
      )
      drawRoundRect(
        brush = brush,
        cornerRadius = radius,
        style = Stroke(stroke)
      )
    }
  }
}

fun Modifier.spectreScanline(
  color: Color = Color.White,
  alpha: Float = 0.08f,
  durationMs: Int = 2400
): Modifier = composed {
  val config = LocalSpectreFxConfig.current
  if (!config.enableScanlineEffect) return@composed this

  val transition = rememberInfiniteTransition(label = "spectreScanline")
  val xShift by transition.animateFloat(
    initialValue = -0.5f,
    targetValue = 1.5f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        (durationMs / config.glowSpeed).toInt().coerceAtLeast(300),
        easing = LinearEasing
      )
    ),
    label = "xShift"
  )

  drawBehind {
    val gradientWidth = size.width * 0.4f
    val x = xShift * size.width

    val brush = Brush.linearGradient(
      colors = listOf(
        Color.Transparent,
        color.copy(alpha = alpha),
        Color.Transparent
      ),
      start = Offset(x, 0f),
      end = Offset(x + gradientWidth, size.height)
    )
    drawRect(brush)
  }
}

fun Modifier.spectreSignalPulse(
  isActive: Boolean,
  color: Color,
  intensity: Float = 1f
): Modifier = composed {
  val config = LocalSpectreFxConfig.current
  if (!config.enableSignalPulse || !isActive) return@composed this

  val transition = rememberInfiniteTransition(label = "signalPulse")
  val pulseAlpha by transition.animateFloat(
    initialValue = 0.2f,
    targetValue = (0.7f * intensity * config.glowIntensity).coerceAtMost(1f),
    animationSpec = infiniteRepeatable(
      animation = tween(
        (600 / config.glowSpeed).toInt().coerceAtLeast(150),
        easing = EaseInOut
      ),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseAlpha"
  )

  val pulseScale by transition.animateFloat(
    initialValue = 1f,
    targetValue = 1.08f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        (600 / config.glowSpeed).toInt().coerceAtLeast(150),
        easing = EaseInOut
      ),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseScale"
  )

  drawBehind {
    drawCircle(
      color = color.copy(alpha = pulseAlpha * 0.3f),
      radius = size.minDimension * pulseScale * 0.6f,
      center = center
    )
  }
}

fun Modifier.spectrePressEffect(
  baseColor: Color
): Modifier = composed {
  val config = LocalSpectreFxConfig.current
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  if (!config.enablePressFeedback) return@composed this

  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.96f else 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "pressScale"
  )

  val glowAlpha by animateFloatAsState(
    targetValue = if (isPressed) (0.8f * config.glowIntensity).coerceAtMost(1f) else 0f,
    animationSpec = tween(150),
    label = "pressGlow"
  )

  this
    .graphicsLayer {
      scaleX = scale
      scaleY = scale
    }
    .clickable(
      interactionSource = interactionSource,
      indication = null,
      onClick = { }
    )
    .drawBehind {
      if (glowAlpha > 0.01f) {
        drawRoundRect(
          color = baseColor.copy(alpha = glowAlpha),
          cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
          style = Stroke(3.dp.toPx())
        )
      }
    }
}
