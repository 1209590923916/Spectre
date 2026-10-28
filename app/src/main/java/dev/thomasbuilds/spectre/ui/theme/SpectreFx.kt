package dev.thomasbuilds.spectre.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import dev.thomasbuilds.spectre.settings.AnimationLevel
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

internal enum class Ambient {
  NONE, EMBERS, FIREBALLS, BUBBLES, FISH, STARS, SNOW, PETALS,
  GOLD, NEON, SCANLINES, GRAIN, HALFTONE, AURORA, CLOUDS, POLLEN, DUST
}

internal enum class TapFx { NONE, CRACK, CLAW, SPARKS, BURST, GLITCH, STARBURST, TARGET }

internal data class FxStyle(
  val ambient: Ambient,
  val tap: TapFx,
  val tint: Color? = null,
  val tint2: Color? = null,
  val rainbow: Boolean = false
)

private const val TAP_LIFE_MS = 1200L

internal fun fxFor(p: SpectrePalette): FxStyle = when (p) {
  SpectrePalette.FIRE -> FxStyle(Ambient.EMBERS, TapFx.CRACK, Color(0xFFFF7043), Color(0xFFFFE082))
  SpectrePalette.MAGMA -> FxStyle(Ambient.FIREBALLS, TapFx.CRACK, Color(0xFFFF4E1A), Color(0xFFFFB199))
  SpectrePalette.DRAGON, SpectrePalette.BESTIARY -> FxStyle(Ambient.EMBERS, TapFx.CLAW, Color(0xFFE53935), Color(0xFFFFA726))
  SpectrePalette.SAMURAI -> FxStyle(Ambient.PETALS, TapFx.CLAW, Color(0xFFEC6A8D), Color(0xFFD4A24E))
  SpectrePalette.SUNSET -> FxStyle(Ambient.PETALS, TapFx.SPARKS, Color(0xFFFF7043), Color(0xFFAB47BC))
  SpectrePalette.SUNRISE -> FxStyle(Ambient.PETALS, TapFx.SPARKS, Color(0xFFE65100), Color(0xFFFFD180))
  SpectrePalette.CRIMSON -> FxStyle(Ambient.EMBERS, TapFx.CRACK, Color(0xFFFF5252), Color(0xFFEF9A9A))
  SpectrePalette.CYBERPUNK -> FxStyle(Ambient.NEON, TapFx.GLITCH, Color(0xFFFF2EC4), Color(0xFF00E5FF))
  SpectrePalette.NEON_NIGHT_MARKET -> FxStyle(Ambient.NEON, TapFx.GLITCH, Color(0xFFFF2EC4), Color(0xFFB388FF))
  SpectrePalette.TRANSIT -> FxStyle(Ambient.NEON, TapFx.GLITCH, Color(0xFFFF9100), Color(0xFF00E676))
  SpectrePalette.VAPORWAVE -> FxStyle(Ambient.NEON, TapFx.GLITCH, Color(0xFFFF6EC7), Color(0xFF1FE0C0))
  SpectrePalette.RETRO_OS -> FxStyle(Ambient.SCANLINES, TapFx.GLITCH, Color(0xFF0F7F7F), Color(0xFF1A1AA8))
  SpectrePalette.ARCADE -> FxStyle(Ambient.SCANLINES, TapFx.GLITCH, Color(0xFFFFE600), Color(0xFFFF4040))
  SpectrePalette.FILM -> FxStyle(Ambient.GRAIN, TapFx.GLITCH, Color(0xFFDE8A5C), Color(0xFF70A5A0))
  SpectrePalette.UNDERWATER, SpectrePalette.AERO -> FxStyle(Ambient.BUBBLES, TapFx.BURST, Color(0xFF4FC3F7), Color(0xFF26C6DA))
  SpectrePalette.AQUARIUM -> FxStyle(Ambient.FISH, TapFx.BURST, Color(0xFF22D3EE), Color(0xFFFB7185))
  SpectrePalette.ICEWAVE -> FxStyle(Ambient.SNOW, TapFx.SPARKS, Color(0xFF4FC3F7), Color(0xFFB3E5FC))
  SpectrePalette.CYAN -> FxStyle(Ambient.BUBBLES, TapFx.SPARKS, Color(0xFF00E5FF), Color(0xFF84FFFF))
  SpectrePalette.COSMIC, SpectrePalette.DEEP_SPACE -> FxStyle(Ambient.STARS, TapFx.SPARKS, Color(0xFFFFD180), Color(0xFF9575CD))
  SpectrePalette.DESERT -> FxStyle(Ambient.DUST, TapFx.SPARKS, Color(0xFFC96F4A), Color(0xFFD9B481))
  SpectrePalette.AURORA -> FxStyle(Ambient.AURORA, TapFx.SPARKS, Color(0xFF39D98A), Color(0xFF7C6BD9))
  SpectrePalette.CLOUDS -> FxStyle(Ambient.CLOUDS, TapFx.SPARKS, Color(0xFF8FA6F2), Color(0xFFFFB59E))
  SpectrePalette.GOLD, SpectrePalette.LUXURY, SpectrePalette.PALACE, SpectrePalette.MUSEUM ->
    FxStyle(Ambient.GOLD, TapFx.SPARKS, Color(0xFFD4AF37), Color(0xFFFFE9A8))
  SpectrePalette.BOTANICAL, SpectrePalette.GLASSHOUSE -> FxStyle(Ambient.POLLEN, TapFx.SPARKS, Color(0xFF7CB342), Color(0xFF558B2F))
  SpectrePalette.ALCHEMY -> FxStyle(Ambient.POLLEN, TapFx.SPARKS, Color(0xFFC8814E), Color(0xFF9B6BDF))
  SpectrePalette.COMIC -> FxStyle(Ambient.HALFTONE, TapFx.STARBURST, Color(0xFFE23A2E), Color(0xFFF7C31C))
  SpectrePalette.HACKER -> FxStyle(Ambient.POLLEN, TapFx.SPARKS, Color(0xFF00E676), Color(0xFFB9F6CA))
  SpectrePalette.KALI -> FxStyle(Ambient.NEON, TapFx.SPARKS, Color(0xFF557FF2), Color(0xFF66D9FF))
  SpectrePalette.BLUE -> FxStyle(Ambient.NEON, TapFx.SPARKS, Color(0xFF64B5F6), Color(0xFF90CAF9))
  SpectrePalette.TOXIC -> FxStyle(Ambient.POLLEN, TapFx.SPARKS, Color(0xFFC6FF00), Color(0xFFFFEE58))
  SpectrePalette.TINY_CITY -> FxStyle(Ambient.POLLEN, TapFx.SPARKS, Color(0xFF4FA3E3), Color(0xFFE07850))
  SpectrePalette.AGENT -> FxStyle(Ambient.DUST, TapFx.TARGET, Color(0xFFE53935), Color(0xFF90A4AE))
  SpectrePalette.RAINBOW -> FxStyle(Ambient.STARS, TapFx.SPARKS, rainbow = true)
  SpectrePalette.DEFAULT, SpectrePalette.LIGHT, SpectrePalette.DARK,
  SpectrePalette.MONO, SpectrePalette.CUSTOM -> FxStyle(Ambient.DUST, TapFx.SPARKS)
}

internal class TapEffect(
  val x: Float,
  val y: Float,
  val kind: TapFx,
  val swipe: Boolean,
  val angleRad: Float,
  val bornMs: Long,
  val seed: Int
)

@Composable
internal fun SpectreFxHost(
  palette: SpectrePalette,
  animationLevel: AnimationLevel,
  fxConfig: SpectreFxConfig = SpectreFxConfig(),
  content: @Composable () -> Unit
) {
  if (animationLevel == AnimationLevel.OFF) {
    CompositionLocalProvider(LocalSpectreFxConfig provides fxConfig) {
      content()
    }
    return
  }
  val style = remember(palette) { fxFor(palette) }
  val taps = remember { mutableStateListOf<TapEffect>() }
  val frameNowMs = remember { mutableLongStateOf(0L) }

  CompositionLocalProvider(LocalSpectreFxConfig provides fxConfig) {
    Box(
      Modifier
        .fillMaxSize()
        .pointerInput(style) {
          awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            var last = down.position
            var ev = awaitPointerEvent()
            while (ev.changes.any { it.pressed }) {
              ev.changes.forEach { c -> if (c.pressed) last = c.position }
              ev = awaitPointerEvent()
            }
            ev.changes.forEach { c -> last = c.position }
            val dx = last.x - down.position.x
            val dy = last.y - down.position.y
            val dist = sqrt(dx * dx + dy * dy)
            val swipe = dist > 90.dp.toPx()
            taps.add(
              TapEffect(
                x = (down.position.x + last.x) / 2f,
                y = (down.position.y + last.y) / 2f,
                kind = style.tap,
                swipe = swipe,
                angleRad = kotlin.math.atan2(dy, dx),
                bornMs = frameNowMs.longValue, // ← same clock as the renderer
                seed = (down.position.x.toInt() * 31 + down.position.y.toInt())
              )
            )
            if (taps.size > 5) taps.removeAt(0)
          }
        }
    ) {
      content()
      FxCanvas(style = style, level = animationLevel, taps = taps, config = fxConfig, frameNowMs = frameNowMs)
    }
  }
}

@Composable
private fun FxCanvas(
  style: FxStyle,
  level: AnimationLevel,
  taps: SnapshotStateList<TapEffect>,
  config: SpectreFxConfig,
  frameNowMs: MutableLongState
) {
  var frameMs by remember { mutableLongStateOf(0L) }
  val scheme = MaterialTheme.colorScheme
  val sim = remember(style, config, scheme.primary, scheme.tertiary) {
    FxSim(style, config.particleDensity, scheme.primary, scheme.tertiary, config.effectScale)
  }
  LaunchedEffect(style, level) {
    while (true) {
      withFrameNanos { ns ->
        frameMs = ns / 1_000_000L
        frameNowMs.longValue = frameMs
        taps.removeAll { frameMs - it.bornMs > TAP_LIFE_MS }
      }
    }
  }
  Canvas(Modifier.fillMaxSize()) {
    val t = frameMs
    if (t == 0L) return@Canvas
    sim.step(size.width, size.height, t)
    when (level) {
      AnimationLevel.FULL, AnimationLevel.INTENSE, AnimationLevel.MAXIMUM -> sim.drawAmbient(this, t)
      else -> {}
    }
    taps.forEach { sim.drawTap(this, it, t) }
  }
}

private fun hsv(h: Float, s: Float, v: Float): Color {
  val hue = ((h % 360f) + 360f) % 360f
  return Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, s.coerceIn(0f, 1f), v.coerceIn(0f, 1f))))
}

private class Part(
  var x: Float,
  var y: Float,
  var vx: Float,
  var vy: Float,
  var r: Float,
  var ph: Float
)

private class FxSim(
  val style: FxStyle,
  private val densityMultiplier: Float = 1f,
  private val fallback1: Color = Color(0xFFFFD180),
  private val fallback2: Color = Color(0xFF00E5FF),
  private val scale: Float = 1f
) {
  private val sc = scale.coerceIn(0.25f, 4f)
  private val rnd = java.util.Random(9137)
  private var lastMs = 0L
  private var stepDt = 16f
  private val parts = ArrayList<Part>()
  private var shoot: Part? = null
  private var nextShoot = 4000L

  private val c1: Color get() = style.tint ?: fallback1
  private val c2: Color get() = style.tint2 ?: fallback2

  private fun targetCount(base: Int): Int = (base * densityMultiplier).toInt().coerceAtLeast(1)

  private fun spawn(w: Float, h: Float) = Part(
    x = rnd.nextFloat() * w,
    y = rnd.nextFloat() * h,
    vx = if (rnd.nextBoolean()) -(25f + rnd.nextFloat() * 35f) else (25f + rnd.nextFloat() * 35f),
    vy = 0f,
    r = 1f + rnd.nextFloat() * 2.5f,
    ph = rnd.nextFloat() * 6.283f
  )

  private fun ensure(n: Int, w: Float, h: Float) {
    while (parts.size < n) parts.add(spawn(w, h))
  }

  fun step(w: Float, h: Float, now: Long) {
    stepDt = if (lastMs == 0L) 16f else (now - lastMs).coerceIn(4L, 64L).toFloat()
    lastMs = now
    val s = stepDt / 1000f
    val t = now.toFloat()
    when (style.ambient) {
      Ambient.EMBERS -> {
        ensure(targetCount(34), w, h)
        parts.forEach { p ->
          p.y -= (26f + p.r * 14f) * s
          p.x += sin(t * 0.002f + p.ph) * 22f * s
          if (p.y < -8f) { p.y = h + 8f; p.x = rnd.nextFloat() * w }
        }
      }
      Ambient.FIREBALLS -> {
        ensure(targetCount(12), w, h)
        parts.forEach { p ->
          p.y += (140f + p.r * 26f) * s
          p.x += sin(t * 0.001f + p.ph) * 30f * s
          if (p.y > h + 12f) { p.y = -12f; p.x = rnd.nextFloat() * w; p.r = 3f + rnd.nextFloat() * 5f }
        }
      }
      Ambient.BUBBLES -> {
        ensure(targetCount(26), w, h)
        parts.forEach { p ->
          p.y -= (24f + p.r * 10f) * s
          p.x += sin(t * 0.0016f + p.ph) * 26f * s
          if (p.y < -8f) { p.y = h + 8f; p.x = rnd.nextFloat() * w }
        }
      }
      Ambient.FISH -> {
        ensure(targetCount(7), w, h)
        parts.forEach { p ->
          p.x += p.vx * s
          p.y += sin(t * 0.001f + p.ph) * 12f * s
          if (p.x > w + 30f) p.x = -30f
          if (p.x < -30f) p.x = w + 30f
        }
      }
      Ambient.STARS -> {
        ensure(targetCount(60), w, h)
        if (now > nextShoot) {
          shoot = Part(-40f, rnd.nextFloat() * h * 0.6f, 520f, 180f, 2f, 0f)
          nextShoot = now + 5000L + (rnd.nextLong() % 4000L)
        }
        shoot?.let { sh ->
          sh.x += sh.vx * s
          sh.y += sh.vy * s
          if (sh.x > w + 60f) shoot = null
        }
      }
      Ambient.SNOW -> {
        ensure(targetCount(44), w, h)
        parts.forEach { p ->
          p.y += (14f + p.r * 9f) * s
          p.x += sin(t * 0.0012f + p.ph) * 20f * s
          if (p.y > h + 4f) { p.y = -4f; p.x = rnd.nextFloat() * w }
        }
      }
      Ambient.PETALS -> {
        ensure(targetCount(18), w, h)
        parts.forEach { p ->
          p.y += (20f + p.r * 6f) * s
          p.x += sin(t * 0.0014f + p.ph) * 34f * s
          if (p.y > h + 8f) { p.y = -8f; p.x = rnd.nextFloat() * w }
        }
      }
      Ambient.GOLD -> {
        ensure(targetCount(26), w, h)
        parts.forEach { p ->
          p.y -= (7f + p.r * 3f) * s
          p.x += sin(t * 0.001f + p.ph) * 12f * s
          if (p.y < -6f) { p.y = h + 6f; p.x = rnd.nextFloat() * w }
        }
      }
      Ambient.POLLEN, Ambient.DUST -> {
        ensure(targetCount(if (style.ambient == Ambient.POLLEN) 30 else 24), w, h)
        parts.forEach { p ->
          p.x += sin(t * 0.0007f + p.ph) * 14f * s
          p.y += cos(t * 0.0009f + p.ph * 1.7f) * 12f * s
        }
      }
      else -> {}
    }
  }

  fun drawAmbient(scope: DrawScope, t: Long) = with(scope) {
    val w = size.width
    val h = size.height
    val tf = t.toFloat()
    val tint = style.tint ?: fallback1
    when (style.ambient) {
      Ambient.EMBERS -> parts.forEach { p ->
        val a = (p.y / h).coerceIn(0.15f, 1f) * 0.8f
        val col = lerp(tint, Color(0xFFFFE082), ((sin(p.ph) + 1f) / 2f) * 0.6f)
        drawCircle(col.copy(alpha = a * 0.25f), radius = p.r * 3f * sc, center = Offset(p.x, p.y))
        drawCircle(col.copy(alpha = a), radius = p.r * sc, center = Offset(p.x, p.y))
      }
      Ambient.FIREBALLS -> parts.forEach { p ->
        repeat(3) { i ->
          drawCircle(Color(0xFFFF6D2E).copy(alpha = (0.22f - i * 0.06f)), radius = p.r * (1f - i * 0.22f) * sc, center = Offset(p.x - sin(tf * 0.001f + p.ph) * (i + 1) * 4f, p.y - (i + 1) * p.r * 1.3f))
        }
        drawCircle(Color(0xFFFFAB6E).copy(alpha = 0.30f), radius = p.r * 1.9f * sc, center = Offset(p.x, p.y))
        drawCircle(Color(0xFFFF5722).copy(alpha = 0.85f), radius = p.r * sc, center = Offset(p.x, p.y))
      }
      Ambient.BUBBLES -> parts.forEach { p ->
        val rr = (p.r + 1.5f) * sc
        drawCircle(Color(0xCCFFFFFF), radius = rr, center = Offset(p.x, p.y), alpha = 0.16f)
        drawCircle(tint, radius = rr, center = Offset(p.x, p.y), alpha = 0.35f, style = Stroke(1.2f))
        drawCircle(Color.White, radius = 0.9f, center = Offset(p.x - rr * 0.35f, p.y - rr * 0.35f), alpha = 0.6f)
      }
      Ambient.FISH -> parts.forEach { p ->
        val heading = if (p.vx >= 0) 0f else 180f
        val wig = sin(tf * 0.006f + p.ph) * 9f
        val bodyCol = if (p.ph > 3f) c2 else if (p.ph > 1.5f) Color(0xFFFFB74D) else c1
        rotate(heading + wig, pivot = Offset(p.x, p.y)) {
          drawOval(bodyCol.copy(alpha = 0.75f), topLeft = Offset(p.x - 10f, p.y - 4f), size = Size(20f, 8f))
          val tail = Path()
          tail.moveTo(p.x - 9f, p.y)
          tail.lineTo(p.x - 16f, p.y - 4f)
          tail.lineTo(p.x - 16f, p.y + 4f)
          tail.close()
          drawPath(tail, bodyCol.copy(alpha = 0.75f))
          drawCircle(Color(0xFF102027), radius = 1.5f, center = Offset(p.x + 6f, p.y - 1f))
        }
      }
      Ambient.STARS -> {
        parts.forEach { p ->
          val a = 0.25f + 0.6f * kotlin.math.abs(sin(tf * 0.001f * (0.5f + p.r * 0.3f) + p.ph * 3f))
          val col = when {
            style.rainbow -> hsv(p.ph * 57.3f, 0.85f, 1f)
            p.r > 2.4f -> tint
            else -> Color.White
          }
          drawCircle(col, radius = p.r * sc, center = Offset(p.x, p.y), alpha = a)
        }
        shoot?.let { sh ->
          drawLine(Color(0xFFFFFFFF).copy(alpha = 0.5f), Offset(sh.x, sh.y), Offset(sh.x - 60f, sh.y - 20f), 2f)
        }
      }
      Ambient.SNOW -> parts.forEach { p ->
        drawCircle(Color.White, radius = p.r * sc, center = Offset(p.x, p.y), alpha = 0.35f + p.r * 0.15f)
      }
      Ambient.PETALS -> parts.forEach { p ->
        rotate(tf * 0.05f + p.ph * 57f, pivot = Offset(p.x, p.y)) {
          drawOval(tint.copy(alpha = 0.7f), topLeft = Offset(p.x - p.r * 1.5f * sc, p.y - p.r * 0.8f * sc), size = Size(p.r * 3f * sc, p.r * 1.6f * sc))
        }
      }
      Ambient.GOLD -> parts.forEach { p ->
        val a = 0.3f + 0.7f * kotlin.math.abs(sin(tf * 0.003f + p.ph))
        val rr = p.r * 1.6f * sc
        drawLine(tint.copy(alpha = a), Offset(p.x - rr, p.y), Offset(p.x + rr, p.y), 1.4f)
        drawLine(tint.copy(alpha = a), Offset(p.x, p.y - rr), Offset(p.x, p.y + rr), 1.4f)
      }
      Ambient.NEON -> {
        if (tf % 6300f < 90f) drawRect(Color.White.copy(alpha = 0.02f))
        parts.forEach { p ->
          val c = if (p.ph > 2.1f) c1 else c2
          val a = 0.04f + 0.05f * kotlin.math.abs(sin(tf * 0.0012f + p.ph))
          drawCircle(c, radius = (60f + p.r * 20f) * sc, center = Offset(p.x, p.y), alpha = a)
        }
      }
      Ambient.SCANLINES -> {
        var y = 0f
        while (y < h) {
          drawLine(Color(0xFF000000).copy(alpha = 0.045f), Offset(0f, y), Offset(w, y), 1.5f)
          y += 6f
        }
        val band = ((tf * 0.06f) % (h + 240f)) - 120f
        drawRect(Color(0xFF008080).copy(alpha = 0.05f), topLeft = Offset(0f, band), size = Size(w, 90f))
      }
      Ambient.GRAIN -> {
        repeat(30) {
          drawCircle(Color.Gray.copy(alpha = 0.06f), radius = 1f, center = Offset(rnd.nextFloat() * w, rnd.nextFloat() * h))
        }
        repeat(2) {
          drawLine(Color(0xFFFFFFFF).copy(alpha = 0.05f), Offset(rnd.nextFloat() * w, 0f), Offset(rnd.nextFloat() * w, h), 1f)
        }
      }
      Ambient.HALFTONE -> {
        var y = 0f
        var row = 0
        while (y < h) {
          var x = if (row % 2 == 0) 0f else 13f
          while (x < w) {
            val r = if ((x + y) % 52f < 26f) 2.2f else 1.2f
            drawCircle(Color(0xFF1A1A1A).copy(alpha = 0.05f), radius = r, center = Offset(x, y))
            x += 26f
          }
          y += 26f
          row++
        }
      }
      Ambient.AURORA -> {
        val cols = listOf(c1, c2, Color(0xFF4DD0E1))
        for (i in 0 until 3) {
          val base = h * (0.28f + i * 0.14f)
          val amp = 50f + i * 18f
          val path = Path()
          path.moveTo(0f, base)
          var x = 0f
          while (x <= w) {
            path.lineTo(x, base + sin(x * 0.006f + tf * 0.0007f * (i + 1) + i * 2.1f) * amp)
            x += 26f
          }
          path.lineTo(w, base + 90f)
          x = w
          while (x >= 0f) {
            path.lineTo(x, base + 90f + sin(x * 0.006f + tf * 0.0007f * (i + 1) + i * 2.1f + 0.9f) * amp)
            x -= 26f
          }
          path.close()
          drawPath(path, cols[i].copy(alpha = 0.05f + 0.02f * i))
        }
      }
      Ambient.CLOUDS -> parts.forEach { p ->
        val cx = ((p.x + tf * 0.005f * (1f + p.ph)) % (w + 400f)) - 200f
        val cy = p.y * 0.6f
        val rad = 70f + p.r * 22f
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0f)),
            center = Offset(cx, cy),
            radius = rad
          ),
          radius = rad,
          center = Offset(cx, cy)
        )
      }
      Ambient.POLLEN -> parts.forEach { p ->
        drawCircle(tint, radius = p.r * sc, center = Offset(p.x, p.y), alpha = 0.10f)
      }
      Ambient.DUST -> parts.forEach { p ->
        drawCircle(tint, radius = p.r * sc, center = Offset(p.x, p.y), alpha = 0.08f)
      }
      else -> {}
    }
  }

  fun drawTap(scope: DrawScope, e: TapEffect, now: Long) = with(scope) {
    val w = size.width
    val h = size.height
    val p = ((now - e.bornMs) / 1000f).coerceIn(0f, 1f)
    if (p >= 1f) return@with
    val tint = style.tint ?: fallback1
    val tint2 = style.tint2 ?: fallback2

    when (e.kind) {
      TapFx.CRACK -> {
        val fade = 1f - p
        val grow = 1f - (1f - p) * (1f - p)
        val maxLen = min(w, h) * 0.55f * sc
        if (p < 0.22f) drawRect(tint.copy(alpha = 0.16f * (1f - p / 0.22f)))
        drawCircle(tint.copy(alpha = 0.35f * fade), radius = maxLen * (0.25f + 0.75f * p), center = Offset(e.x, e.y), style = Stroke(3f * sc + 8f * fade))
        drawCircle(Color(0xFFFFE082).copy(alpha = 0.5f * fade), radius = maxLen * 0.35f * p, center = Offset(e.x, e.y), style = Stroke(1.5f * sc))
        val r = java.util.Random(e.seed * 31 + 7L)
        repeat(9) {
          var ang = r.nextFloat() * 6.283f
          var x = e.x
          var y = e.y
          val total = maxLen * (0.5f + r.nextFloat() * 0.5f) * grow
          val segs = 7
          repeat(segs) { i ->
            val len = total / segs
            ang += (r.nextFloat() - 0.5f) * 0.75f
            val nx2 = x + cos(ang) * len
            val ny2 = y + sin(ang) * len
            val a = fade * (1f - i * 0.06f)
            drawLine(tint.copy(alpha = 0.25f * a), Offset(x, y), Offset(nx2, ny2), 9f * sc)
            drawLine(tint.copy(alpha = 0.9f * a), Offset(x, y), Offset(nx2, ny2), 3.2f * sc)
            drawLine(Color.White.copy(alpha = 0.95f * a), Offset(x, y), Offset(nx2, ny2), 1.2f * sc)
            if (i == 2 || i == 4) {
              val bang = ang + (if (r.nextBoolean()) 1 else -1) * (0.5f + r.nextFloat() * 0.6f)
              val bl = total * 0.25f
              drawLine(tint.copy(alpha = 0.6f * a), Offset(x, y), Offset(x + cos(bang) * bl, y + sin(bang) * bl), 2f * sc)
            }
            x = nx2
            y = ny2
          }
        }
        val r2 = java.util.Random(e.seed * 13 + 5L)
        repeat(16) {
          val a2 = r2.nextFloat() * 6.283f
          val sp = (0.25f + r2.nextFloat() * 0.75f) * grow
          val d2 = maxLen * 1.1f * sp
          val px = e.x + cos(a2) * d2
          val py = e.y + sin(a2) * d2 + 60f * p * p * sc
          drawCircle(tint2.copy(alpha = 0.85f * fade), radius = (1.6f + r2.nextFloat() * 2.4f) * sc, center = Offset(px, py))
        }
        if (p < 0.5f) drawCircle(Color.White.copy(alpha = (1f - p * 2f) * 0.8f), radius = 8f * sc * (1f - p), center = Offset(e.x, e.y))
      }

      TapFx.CLAW -> {
        val fade = (1f - ((p - 0.55f) / 0.45f).coerceAtLeast(0f)).coerceIn(0f, 1f)
        val prog = 1f - (1f - p) * (1f - p)
        val dirAngle = if (e.swipe) e.angleRad else -0.55f + (e.seed % 3) * 0.25f
        val dx = cos(dirAngle)
        val dy = sin(dirAngle)
        val nx = -dy
        val ny = dx
        val l = max(w, h) * 1.3f

        drawRect(Color(0xFFB71C1C).copy(alpha = 0.22f * (1f - p)))

        val shake = (1f - p) * 7f * sc
        val jx = if (shake > 0.5f) sin(p * 95f) * shake else 0f
        val jy = if (shake > 0.5f) cos(p * 77f) * shake else 0f

        for (k in -1..1) {
          val cx = e.x + nx * 46f * sc * k + jx
          val cy = e.y + ny * 46f * sc * k + jy
          val sx = cx - dx * l / 2f
          val sy = cy - dy * l / 2f
          val tipx = sx + dx * l * prog
          val tipy = sy + dy * l * prog
          repeat(3) { g ->
            val off = (g + 1) * 16f * sc * (1f - prog)
            drawLine(Color(0xFFB71C1C).copy(alpha = 0.22f * fade), Offset(sx - dx * off, sy - dy * off), Offset(tipx - dx * off, tipy - dy * off), 12f * sc, StrokeCap.Round)
          }
          drawLine(Color(0xFF7F0000).copy(alpha = 0.4f * fade), Offset(sx, sy), Offset(tipx, tipy), 24f * sc, StrokeCap.Round)
          drawLine(tint.copy(alpha = 0.9f * fade), Offset(sx, sy), Offset(tipx, tipy), 11f * sc, StrokeCap.Round)
          drawLine(Color(0xFFFFECB3).copy(alpha = 0.95f * fade), Offset(sx, sy), Offset(tipx, tipy), 3.5f * sc, StrokeCap.Round)
          drawCircle(Color(0xFFFFCCBC).copy(alpha = 0.6f * fade), radius = 7f * sc, center = Offset(tipx, tipy))
        }
        val r = java.util.Random(e.seed * 17 + 3L)
        repeat(14) {
          val fr = r.nextFloat()
          val px = e.x + dx * (fr - 0.5f) * l * prog + nx * 46f * sc * (r.nextInt(3) - 1)
          val py = e.y + dy * (fr - 0.5f) * l * prog + ny * 46f * sc * (r.nextInt(3) - 1)
          drawCircle(tint, radius = (2f + r.nextFloat() * 2.5f) * sc, center = Offset(px, py), alpha = fade)
        }
      }

      TapFx.SPARKS -> {
        val fade = 1f - p
        val r = java.util.Random(e.seed * 7 + 11L)
        val count = (16 + (14 * sc)).toInt().coerceAtLeast(12)
        drawCircle(tint.copy(alpha = 0.5f * fade), radius = 20f * sc + 190f * sc * p, center = Offset(e.x, e.y), style = Stroke(2.5f * sc))
        drawCircle(tint2.copy(alpha = 0.3f * fade), radius = 10f * sc + 260f * sc * p, center = Offset(e.x, e.y), style = Stroke(1.2f * sc))
        if (p < 0.3f) drawCircle(Color.White.copy(alpha = (1f - p / 0.3f) * 0.7f), radius = 16f * sc, center = Offset(e.x, e.y))
        repeat(count) { i ->
          val a2 = r.nextFloat() * 6.283f
          val speed = 0.35f + r.nextFloat() * 0.65f
          val d2 = (40f + 260f * speed) * sc * p
          val px = e.x + cos(a2) * d2
          val py = e.y + sin(a2) * d2 + 80f * p * p * sc
          val col = when {
            style.rainbow -> hsv(i * 360f / count + p * 120f, 0.85f, 1f)
            i % 3 == 0 -> tint2
            i % 3 == 1 -> tint
            else -> Color(0xFFFFE082)
          }
          val streakLen = 14f * sc * (1f - p * 0.4f)
          drawLine(col.copy(alpha = 0.8f * fade), Offset(px - cos(a2) * streakLen, py - sin(a2) * streakLen), Offset(px, py), 2f * sc, StrokeCap.Round)
          drawCircle(col.copy(alpha = 0.9f * fade), radius = (1.8f + r.nextFloat() * 1.6f) * sc, center = Offset(px, py))
        }
      }

      TapFx.BURST -> {
        val fade = 1f - p
        repeat(3) { i ->
          val rp = (p - i * 0.12f).coerceIn(0f, 1f)
          if (rp > 0f) {
            drawCircle(tint.copy(alpha = (0.65f - i * 0.15f) * fade), radius = (30f + 210f * rp) * sc, center = Offset(e.x, e.y), style = Stroke(((4f - i) * fade + 1.5f) * sc))
          }
        }
        if (p < 0.25f) drawRect(tint.copy(alpha = 0.12f * (1f - p / 0.25f)))
        val r = java.util.Random(e.seed * 23 + 9L)
        repeat(18) { i ->
          val bx = e.x + (i - 8.5f) * 9f * sc + (r.nextFloat() - 0.5f) * 12f
          val by = e.y - 40f * p * p * sc * (0.6f + r.nextFloat()) - i * 3f * p
          val rad = (2.5f + r.nextFloat() * 4f) * sc
          drawCircle(Color(0xB3FFFFFF), radius = rad, center = Offset(bx, by), alpha = fade * 0.7f)
          drawCircle(tint, radius = rad, center = Offset(bx, by), alpha = fade * 0.35f, style = Stroke(1f))
          drawCircle(Color.White, radius = rad * 0.25f, center = Offset(bx - rad * 0.3f, by - rad * 0.3f), alpha = fade * 0.8f)
        }
      }

      TapFx.GLITCH -> {
        val r = java.util.Random(e.seed * 13 + 1L)
        val active = p < 0.55f
        if (active) {
          val a = 1f - p / 0.55f
          drawRect(Color(0xFF00E5FF).copy(alpha = 0.05f * a), topLeft = Offset(-8f * sc, 0f), size = Size(w, h))
          drawRect(Color(0xFFFF2EC4).copy(alpha = 0.05f * a), topLeft = Offset(8f * sc, 0f), size = Size(w, h))
          repeat(10) { i ->
            val yy = r.nextFloat() * h
            val hh = (6f + r.nextFloat() * 26f) * sc
            val off = (r.nextFloat() - 0.5f) * 140f * sc
            val c = when (i % 3) { 0 -> tint; 1 -> tint2; else -> Color.White }
            drawRect(c.copy(alpha = 0.20f * a), topLeft = Offset(off, yy), size = Size(w, hh))
            drawRect(Color.Black.copy(alpha = 0.10f * a), topLeft = Offset(off, yy), size = Size(w * 0.3f, hh * 0.6f))
          }
          val scanY = (p / 0.55f) * h
          drawRect(Color.White.copy(alpha = 0.07f * a), topLeft = Offset(0f, scanY), size = Size(w, 14f * sc))
        }
        repeat(40) {
          drawRect(Color.White.copy(alpha = 0.10f * (1f - p)), topLeft = Offset(r.nextFloat() * w, r.nextFloat() * h), size = Size(2f, 2f))
        }
      }

      TapFx.STARBURST -> {
        val fade = 1f - p
        val grow = 1f - (1f - p) * (1f - p)
        val R = min(w, h) * 0.4f * sc
        val r = java.util.Random(e.seed * 29 + 2L)
        repeat(28) {
          val ang = r.nextFloat() * 6.283f
          val inner = R * (1.1f + r.nextFloat() * 0.3f)
          val outer = inner + max(w, h) * (0.3f + r.nextFloat() * 0.5f) * grow
          drawLine(Color(0xFF1A1A1A).copy(alpha = 0.35f * fade * grow), Offset(e.x + cos(ang) * inner, e.y + sin(ang) * inner), Offset(e.x + cos(ang) * outer, e.y + sin(ang) * outer), (1f + r.nextFloat() * 2.5f) * sc)
        }
        rotate(p * 25f, pivot = Offset(e.x, e.y)) {
          val path = Path()
          var ang = -1.5708f
          val outer = R * grow
          val inner = R * 0.42f * grow
          repeat(12) { i ->
            val rad = if (i % 2 == 0) outer else inner
            val px = e.x + cos(ang) * rad
            val py = e.y + sin(ang) * rad
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            ang += 0.5236f
          }
          path.close()
          drawPath(path, Color(0xFFFFF176).copy(alpha = 0.9f * fade))
          drawPath(path, tint.copy(alpha = 0.9f * fade), style = Stroke(4f * sc))
          drawPath(path, Color.White.copy(alpha = 0.7f * fade), style = Stroke(1.5f * sc))
        }
        repeat(16) { i ->
          val ang = i * 0.3927f + p
          val d = R * 1.25f
          drawCircle(Color(0xFF1A1A1A).copy(alpha = 0.25f * fade), radius = 3f * sc * grow, center = Offset(e.x + cos(ang) * d, e.y + sin(ang) * d))
        }
        if (p < 0.35f) drawCircle(Color.White.copy(alpha = 1f - p / 0.35f), radius = R * 0.3f, center = Offset(e.x, e.y))
      }

      TapFx.TARGET -> {
        val settle = (p / 0.25f).coerceIn(0f, 1f)
        val fade = if (p < 0.75f) 1f else (1f - p) / 0.25f
        val halfBase = max(w, h) * 0.16f * sc
        val half = halfBase * (3f - 2f * settle)
        rotate(p * 120f, pivot = Offset(e.x, e.y)) {
          repeat(4) { i ->
            rotate(i * 90f, pivot = Offset(e.x, e.y)) {
              drawArc(
                color = tint.copy(alpha = 0.7f * fade),
                startAngle = -35f,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = Offset(e.x - half * 1.5f, e.y - half * 1.5f),
                size = Size(half * 3f, half * 3f),
                style = Stroke(2.5f * sc)
              )
            }
          }
        }
        val arm = half * 0.45f
        val col = tint.copy(alpha = fade)
        val c1c = Offset(e.x - half, e.y - half)
        val c2c = Offset(e.x + half, e.y - half)
        val c3c = Offset(e.x + half, e.y + half)
        val c4c = Offset(e.x - half, e.y + half)
        drawLine(col, Offset(c1c.x, c1c.y + arm), c1c, 3f * sc)
        drawLine(col, c1c, Offset(c1c.x + arm, c1c.y), 3f * sc)
        drawLine(col, Offset(c2c.x - arm, c2c.y), c2c, 3f * sc)
        drawLine(col, c2c, Offset(c2c.x, c2c.y + arm), 3f * sc)
        drawLine(col, Offset(c3c.x, c3c.y - arm), c3c, 3f * sc)
        drawLine(col, c3c, Offset(c3c.x - arm, c3c.y), 3f * sc)
        drawLine(col, Offset(c4c.x + arm, c4c.y), c4c, 3f * sc)
        drawLine(col, c4c, Offset(c4c.x, c4c.y - arm), 3f * sc)
        drawLine(col.copy(alpha = 0.35f * fade), Offset(0f, e.y), Offset(w, e.y), 1f)
        drawLine(col.copy(alpha = 0.35f * fade), Offset(e.x, 0f), Offset(e.x, h), 1f)
        if (p < 0.5f) {
          val sweepAng = p * 2f * 3.14159f
          drawLine(col.copy(alpha = 0.5f * (1f - p * 2f)), Offset(e.x, e.y), Offset(e.x + cos(sweepAng) * half * 2.2f, e.y + sin(sweepAng) * half * 2.2f), 2f * sc)
        }
        drawCircle(col, radius = 4f * sc, center = Offset(e.x, e.y))
        drawCircle(col.copy(alpha = 0.5f * fade), radius = 14f * sc * (1f + 0.3f * sin(p * 12f)), center = Offset(e.x, e.y), style = Stroke(1.5f))
      }

      TapFx.NONE -> {}
    }
  }
}
