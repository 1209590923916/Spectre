package dev.thomasbuilds.spectre.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import dev.thomasbuilds.spectre.settings.AnimationLevel
import dev.thomasbuilds.spectre.settings.ThemeMode
import kotlin.math.pow

enum class SpectrePalette(val label: String) {
  DEFAULT("Default (follows light/dark)"),
  LIGHT("Light"),
  DARK("Dark"),
  HACKER("Hacker Green"),
  CYBERPUNK("Cyberpunk"),
  KALI("Kali Blue"),
  BLUE("Blue"),
  CYAN("Cyan"),
  ICEWAVE("Icewave"),
  SUNSET("Sunset"),
  SUNRISE("Sunrise"),
  CRIMSON("Crimson"),
  RAINBOW("Rainbow"),
  FIRE("Fire"),
  MAGMA("Magma"),
  DRAGON("Dragon"),
  GOLD("Gold"),
  TOXIC("Toxic"),
  NEON_NIGHT_MARKET("Neon Night Market"),
  TRANSIT("Cyberpunk Transit"),
  VAPORWAVE("Vaporwave"),
  RETRO_OS("Retro Operating System"),
  ARCADE("Arcade Cabinet"),
  COSMIC("Cosmic Observatory"),
  DEEP_SPACE("Deep Space"),
  AURORA("Aurora"),
  CLOUDS("Cloud Kingdom"),
  AERO("Frutiger Aero"),
  UNDERWATER("Underwater City"),
  AQUARIUM("Digital Aquarium"),
  BOTANICAL("Botanical Lab"),
  GLASSHOUSE("Futuristic Glasshouse"),
  DESERT("Desert Stargazer"),
  SAMURAI("Samurai Sakura"),
  FILM("Analog Film"),
  COMIC("Comic Book"),
  LUXURY("Luxury Editorial"),
  PALACE("Golden White Palace"),
  MUSEUM("Museum of the Future"),
  ALCHEMY("Alchemy Workshop"),
  BESTIARY("Mythical Bestiary"),
  AGENT("Secret Agent"),
  TINY_CITY("Tiny City"),
  MONO("Mono + Accent"),
  CUSTOM("Custom Color")
}

private val DarkColors = darkColorScheme(
  primary = DarkAccent,
  onPrimary = Color.Black,
  secondary = DarkAccent,
  onSecondary = Color.Black,
  background = DarkBg,
  onBackground = DarkOnSurface,
  surface = DarkSurface,
  onSurface = DarkOnSurface,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = DarkOnSurfaceVariant,
  outline = DarkOutline,
  error = SpectreRed,
  onError = Color.Black
)

private val LightColors = lightColorScheme(
  primary = LightAccent,
  onPrimary = Color.White,
  secondary = LightAccent,
  onSecondary = Color.White,
  background = LightBg,
  onBackground = LightOnSurface,
  surface = LightSurface,
  onSurface = LightOnSurface,
  surfaceVariant = LightSurfaceVariant,
  onSurfaceVariant = LightOnSurfaceVariant,
  outline = LightOutline,
  error = SpectreRed,
  onError = Color.White
)

private fun lum(c: Color): Float {
  fun lin(v: Float) = if (v <= 0.03928f) v / 12.92f else ((v + 0.055f) / 1.055f).pow(2.4f)
  return 0.2126f * lin(c.red) + 0.7152f * lin(c.green) + 0.0722f * lin(c.blue)
}

private fun onFor(c: Color): Color = if (lum(c) > 0.35f) Color(0xFF101010) else Color(0xFFF5F5F5)

private fun shift(c: Color, dh: Float = 0f, ds: Float = 0f, dv: Float = 0f): Color {
  val hsv = FloatArray(3)
  android.graphics.Color.colorToHSV(c.toArgb(), hsv)
  return Color(
    android.graphics.Color.HSVToColor(
      floatArrayOf(
        ((hsv[0] + dh) % 360f + 360f) % 360f,
        (hsv[1] + ds).coerceIn(0f, 1f),
        (hsv[2] + dv).coerceIn(0f, 1f)
      )
    )
  )
}

/**
 * Compact theme factory: accents + neutrals in, full ColorScheme out.
 * Now fills the ENTIRE Material 3 container family so cards, chips,
 * tab rows and switches never fall back to stock white/gray.
 */
private fun mk(
  dark: Boolean,
  p: Long, s: Long, t: Long,
  bg: Long, sf: Long, sv: Long,
  os: Long, osv: Long, ln: Long
): ColorScheme {
  val primary = Color(p)
  val secondary = Color(s)
  val tertiary = Color(t)
  val background = Color(bg)
  val surface = Color(sf)
  val surfaceVariant = Color(sv)
  val outlineC = Color(ln)
  fun cont(c: Color) = if (dark) shift(c, ds = -0.15f, dv = -0.42f) else shift(c, ds = -0.25f, dv = 0.42f)
  fun onCont(c: Color) = if (dark) shift(c, ds = -0.10f, dv = 0.30f) else shift(c, ds = 0.05f, dv = -0.35f)

  // ── Full container scale (this fixes the "partly white" light themes) ──
  val toward = if (dark) background else Color.White
  val surfaceContainerLowest = lerp(surface, toward, 0.6f)
  val surfaceContainerLow = lerp(surface, toward, 0.3f)
  val surfaceContainer = surface
  val surfaceContainerHigh = lerp(surface, surfaceVariant, 0.45f)
  val surfaceContainerHighest = lerp(surface, surfaceVariant, 0.8f)
  val surfaceDim = if (dark) background else lerp(surface, surfaceVariant, 0.3f)
  val surfaceBright = if (dark) lerp(surfaceContainerHigh, Color.White, 0.15f) else Color.White
  val outlineVariant = lerp(outlineC, surfaceVariant, 0.55f)
  val inverseSurface = Color(os)
  val inverseOnSurface = background
  val inversePrimary = if (dark) shift(primary, dv = -0.30f) else shift(primary, dv = 0.10f)
  val errorContainer = cont(SpectreRed)
  val onErrorContainer = onCont(SpectreRed)

  return if (dark) darkColorScheme(
    primary = primary, onPrimary = onFor(primary),
    primaryContainer = cont(primary), onPrimaryContainer = onCont(primary),
    secondary = secondary, onSecondary = onFor(secondary),
    secondaryContainer = cont(secondary), onSecondaryContainer = onCont(secondary),
    tertiary = tertiary, onTertiary = onFor(tertiary),
    tertiaryContainer = cont(tertiary), onTertiaryContainer = onCont(tertiary),
    background = background, onBackground = Color(os),
    surface = surface, onSurface = Color(os),
    surfaceVariant = surfaceVariant, onSurfaceVariant = Color(osv),
    surfaceDim = surfaceDim, surfaceBright = surfaceBright,
    surfaceContainerLowest = surfaceContainerLowest,
    surfaceContainerLow = surfaceContainerLow,
    surfaceContainer = surfaceContainer,
    surfaceContainerHigh = surfaceContainerHigh,
    surfaceContainerHighest = surfaceContainerHighest,
    outline = outlineC, outlineVariant = outlineVariant,
    inverseSurface = inverseSurface, inverseOnSurface = inverseOnSurface, inversePrimary = inversePrimary,
    error = SpectreRed, onError = Color.Black,
    errorContainer = errorContainer, onErrorContainer = onErrorContainer
  ) else lightColorScheme(
    primary = primary, onPrimary = onFor(primary),
    primaryContainer = cont(primary), onPrimaryContainer = onCont(primary),
    secondary = secondary, onSecondary = onFor(secondary),
    secondaryContainer = cont(secondary), onSecondaryContainer = onCont(secondary),
    tertiary = tertiary, onTertiary = onFor(tertiary),
    tertiaryContainer = cont(tertiary), onTertiaryContainer = onCont(tertiary),
    background = background, onBackground = Color(os),
    surface = surface, onSurface = Color(os),
    surfaceVariant = surfaceVariant, onSurfaceVariant = Color(osv),
    surfaceDim = surfaceDim, surfaceBright = surfaceBright,
    surfaceContainerLowest = surfaceContainerLowest,
    surfaceContainerLow = surfaceContainerLow,
    surfaceContainer = surfaceContainer,
    surfaceContainerHigh = surfaceContainerHigh,
    surfaceContainerHighest = surfaceContainerHighest,
    outline = outlineC, outlineVariant = outlineVariant,
    inverseSurface = inverseSurface, inverseOnSurface = inverseOnSurface, inversePrimary = inversePrimary,
    error = SpectreRed, onError = Color.White,
    errorContainer = errorContainer, onErrorContainer = onErrorContainer
  )
}

private val Hacker = mk(true, 0xFF00E676, 0xFF7DDBA6, 0xFFB9F6CA, 0xFF090D09, 0xFF0D120D, 0xFF13201A, 0xFFDDE5DD, 0xFFBCC9BE, 0xFF86938A)
private val Cyberpunk = mk(true, 0xFFFF2EC4, 0xFF00E5FF, 0xFFFFD600, 0xFF0B0B12, 0xFF12121C, 0xFF1A1A26, 0xFFE6E1EB, 0xFFC4C1D0, 0xFF8E8B99)
private val Kali = mk(true, 0xFF557FF2, 0xFF9C7BED, 0xFF66D9FF, 0xFF0A0E1A, 0xFF111827, 0xFF1A2136, 0xFFE0E2F0, 0xFFC0C4D6, 0xFF8A8FA5)
private val Blue = mk(true, 0xFF64B5F6, 0xFF90CAF9, 0xFF4DD0E1, 0xFF0A1020, 0xFF101828, 0xFF182338, 0xFFE1E6F0, 0xFFC0C8D8, 0xFF8A93A8)
private val Cyan = mk(true, 0xFF00E5FF, 0xFF4DD0E1, 0xFF84FFFF, 0xFF061012, 0xFF0A191C, 0xFF0F2326, 0xFFDDF0F3, 0xFFB4C8CC, 0xFF7E9296)
private val Icewave = mk(false, 0xFF0288D1, 0xFF00ACC1, 0xFF5C6BC0, 0xFFF0F8FF, 0xFFFAFDFF, 0xFFD8ECF5, 0xFF1A2230, 0xFF3F4A57, 0xFF6E7C88)
private val Sunset = mk(true, 0xFFFF7043, 0xFFFFAB91, 0xFFAB47BC, 0xFF191009, 0xFF211310, 0xFF2A1B11, 0xFFF2E2D5, 0xFFD6BCA8, 0xFF9E8471)
private val Sunrise = mk(false, 0xFFE65100, 0xFFD81B60, 0xFF7B1FA2, 0xFFFFF8F2, 0xFFFFFDF7, 0xFFF7E6D8, 0xFF33251C, 0xFF715343, 0xFFA38372)
private val Crimson = mk(true, 0xFFFF5252, 0xFFEF9A9A, 0xFFFF8A65, 0xFF120A0A, 0xFF1A0F0F, 0xFF251414, 0xFFF3DADA, 0xFFD3B4B4, 0xFF9C7E7E)
private val Gold = mk(true, 0xFFF5C84C, 0xFFD9A441, 0xFFFFE9A8, 0xFF121008, 0xFF181407, 0xFF241D0B, 0xFFF2E8D5, 0xFFC4B89B, 0xFF8A7D5C)
private val Fire = mk(true, 0xFFFF5722, 0xFFFF9800, 0xFFFFE082, 0xFF0F0705, 0xFF150A06, 0xFF221008, 0xFFF5E3DC, 0xFFC9AC9F, 0xFF8C6355)
private val Magma = mk(true, 0xFFFF4E1A, 0xFFC72E00, 0xFFFFB199, 0xFF0A0503, 0xFF100805, 0xFF1B0D07, 0xFFEFDCD6, 0xFFBEA099, 0xFF7E5A50)
private val Dragon = mk(true, 0xFFE53935, 0xFFFFA726, 0xFF66BB6A, 0xFF140807, 0xFF1A0A09, 0xFF280F0D, 0xFFF2E4E2, 0xFFC7A9A6, 0xFF8D615D)
private val Toxic = mk(true, 0xFFC6FF00, 0xFFAEEA00, 0xFFFFEE58, 0xFF0A0D08, 0xFF0F130B, 0xFF181D10, 0xFFE4EBDD, 0xFFB3BFA6, 0xFF5C684E)
private val NeonMarket = mk(true, 0xFFFF2EC4, 0xFF00E5FF, 0xFFB388FF, 0xFF0B0B14, 0xFF12121E, 0xFF1B1B2C, 0xFFE8E6F0, 0xFFB9B7CC, 0xFF56547A)
private val Transit = mk(true, 0xFFFF9100, 0xFF00E676, 0xFF2979FF, 0xFF0A0B0D, 0xFF101215, 0xFF191C21, 0xFFE7EAEE, 0xFFB4BBC4, 0xFF565E68)
private val Vaporwave = mk(true, 0xFFFF6EC7, 0xFF1FE0C0, 0xFF9B6BFF, 0xFF0E0B17, 0xFF140F20, 0xFF1E1731, 0xFFEFEAFA, 0xFFBEB3D9, 0xFF5E5480)
private val RetroOs = mk(false, 0xFF0F7F7F, 0xFF1A1AA8, 0xFF7A7A2E, 0xFFC9CFD4, 0xFFD4D8DC, 0xFFB8C0C6, 0xFF20242A, 0xFF4A5560, 0xFF7E8790)
private val Arcade = mk(true, 0xFFFFE600, 0xFFFF4040, 0xFF3DDC84, 0xFF0A0A0F, 0xFF101018, 0xFF1A1A24, 0xFFF0F0F5, 0xFFBDBDC9, 0xFF5A5A68)
private val Cosmic = mk(true, 0xFFF2C94C, 0xFF7986CB, 0xFF4DD0E1, 0xFF070A18, 0xFF0D1226, 0xFF161D3A, 0xFFE4E8F5, 0xFFB0BAD6, 0xFF5A658C)
private val DeepSpace = mk(true, 0xFF9575CD, 0xFF4DD0E1, 0xFFFFD180, 0xFF05060D, 0xFF0B0D16, 0xFF131624, 0xFFE6E9F2, 0xFFB0B7CB, 0xFF4A5169)
private val Aurora = mk(true, 0xFF39D98A, 0xFF7C6BD9, 0xFF4DD0E1, 0xFF060911, 0xFF0C111C, 0xFF141B2A, 0xFFE2E9F2, 0xFFABB8CB, 0xFF4F5F78)
private val Clouds = mk(false, 0xFF8FA6F2, 0xFFFFB59E, 0xFFC7A9F2, 0xFFF3F6FC, 0xFFFDFFFE, 0xFFE4EAF6, 0xFF2B3348, 0xFF5D6A8C, 0xFFA9B4CE)
private val Aero = mk(false, 0xFF17B873, 0xFF2196F3, 0xFF64D8CB, 0xFFE9F5F2, 0xFFF6FEFC, 0xFFD7ECE6, 0xFF1E332C, 0xFF4E6B62, 0xFF9FBFB5)
private val Underwater = mk(true, 0xFF26C6DA, 0xFFFF7043, 0xFF4FC3F7, 0xFF04121F, 0xFF081A2B, 0xFF0E2438, 0xFFDCE9F2, 0xFFA3BACC, 0xFF4F6E85)
private val Aquarium = mk(true, 0xFF22D3EE, 0xFFFB7185, 0xFF34D399, 0xFF06202B, 0xFF0A2937, 0xFF103545, 0xFFE0F2F8, 0xFFA9CBD8, 0xFF56808F)
private val Botanical = mk(false, 0xFF558B2F, 0xFF7CB342, 0xFF8D6E63, 0xFFF7F5EE, 0xFFFDFCF8, 0xFFE9E9DE, 0xFF2E3527, 0xFF66705C, 0xFFA9AE9C)
private val Glasshouse = mk(false, 0xFF52A88C, 0xFFA8D5C2, 0xFF9AC8E8, 0xFFF4FAF7, 0xFFFDFFFE, 0xFFE3F0EA, 0xFF22332C, 0xFF5A7368, 0xFFA8C2B6)
private val Desert = mk(false, 0xFFC96F4A, 0xFF33547E, 0xFFD9B481, 0xFFFAF3E8, 0xFFFFFBF3, 0xFFEDE2D0, 0xFF3A2F26, 0xFF6E6053, 0xFFB3A48F)
private val Samurai = mk(true, 0xFFEC6A8D, 0xFFAEB6BF, 0xFFD4A24E, 0xFF111114, 0xFF17171B, 0xFF212127, 0xFFEDEDEF, 0xFFB9B9C0, 0xFF62626B)
private val Film = mk(true, 0xFFDE8A5C, 0xFF70A5A0, 0xFFB08A6E, 0xFF151110, 0xFF1B1614, 0xFF271F1B, 0xFFEFE6DD, 0xFFC4B4A8, 0xFF7E7166)
private val Comic = mk(false, 0xFFE23A2E, 0xFFF7C31C, 0xFF27354A, 0xFFFDFDF9, 0xFFFFFFFF, 0xFFEDE8DA, 0xFF1A1A1A, 0xFF4A4A44, 0xFF2B2B2B)
private val Luxury = mk(true, 0xFFD4AF37, 0xFFEFE6D0, 0xFF8C7853, 0xFF0C0B09, 0xFF121110, 0xFF1C1A16, 0xFFF0EBE3, 0xFFC2BBAE, 0xFF6B665C)
private val Palace = mk(false, 0xFFC9A24B, 0xFFE4C878, 0xFFB08D33, 0xFFFCF9F2, 0xFFFFFDF8, 0xFFF2ECDC, 0xFF33291A, 0xFF6B5E45, 0xFFC0B189)
private val Museum = mk(false, 0xFF9C6B3F, 0xFF3B3B3B, 0xFFC9A870, 0xFFFAF8F4, 0xFFFFFFFF, 0xFFECE7DF, 0xFF2C2A26, 0xFF5F5B54, 0xFFB2AAA0)
private val Alchemy = mk(true, 0xFFC8814E, 0xFF35A06C, 0xFF9B6BDF, 0xFF120E0B, 0xFF181310, 0xFF241C17, 0xFFEDE2D8, 0xFFC0B0A2, 0xFF7A6A5B)
private val Bestiary = mk(true, 0xFFC62828, 0xFFD4AF37, 0xFF2E7D32, 0xFF130D0A, 0xFF1A120E, 0xFF281B15, 0xFFEFE3D0, 0xFFC0AC93, 0xFF7C6A55)
private val Agent = mk(true, 0xFFE53935, 0xFF90A4AE, 0xFFCFD8DC, 0xFF0A0C0D, 0xFF101314, 0xFF181C1E, 0xFFE8EAEB, 0xFFB6BDBF, 0xFF5C6467)
private val TinyCity = mk(false, 0xFF4FA3E3, 0xFFE07850, 0xFF5CB85C, 0xFFF2F6FA, 0xFFFCFFFF, 0xFFDFE8F0, 0xFF27313C, 0xFF5A6875, 0xFFA9B7C3)
private val RainbowStatic = mk(true, 0xFFFF5252, 0xFF00E676, 0xFF448AFF, 0xFF0B0B10, 0xFF12121A, 0xFF1B1B26, 0xFFEDEDF2, 0xFFC5C5D4, 0xFF666677)

private fun toHexLong(c: Color): Long = c.toArgb().toLong() and 0xFFFFFFFFL

/** Direct palette → scheme. No fallback path: light palettes stay light, dark stay dark. */
internal fun staticScheme(palette: SpectrePalette, customColor: Long): ColorScheme = when (palette) {
  SpectrePalette.DEFAULT, SpectrePalette.DARK -> DarkColors
  SpectrePalette.LIGHT -> LightColors
  SpectrePalette.RAINBOW -> RainbowStatic
  SpectrePalette.HACKER -> Hacker
  SpectrePalette.CYBERPUNK -> Cyberpunk
  SpectrePalette.KALI -> Kali
  SpectrePalette.BLUE -> Blue
  SpectrePalette.CYAN -> Cyan
  SpectrePalette.ICEWAVE -> Icewave
  SpectrePalette.SUNSET -> Sunset
  SpectrePalette.SUNRISE -> Sunrise
  SpectrePalette.CRIMSON -> Crimson
  SpectrePalette.FIRE -> Fire
  SpectrePalette.MAGMA -> Magma
  SpectrePalette.DRAGON -> Dragon
  SpectrePalette.GOLD -> Gold
  SpectrePalette.TOXIC -> Toxic
  SpectrePalette.NEON_NIGHT_MARKET -> NeonMarket
  SpectrePalette.TRANSIT -> Transit
  SpectrePalette.VAPORWAVE -> Vaporwave
  SpectrePalette.RETRO_OS -> RetroOs
  SpectrePalette.ARCADE -> Arcade
  SpectrePalette.COSMIC -> Cosmic
  SpectrePalette.DEEP_SPACE -> DeepSpace
  SpectrePalette.AURORA -> Aurora
  SpectrePalette.CLOUDS -> Clouds
  SpectrePalette.AERO -> Aero
  SpectrePalette.UNDERWATER -> Underwater
  SpectrePalette.AQUARIUM -> Aquarium
  SpectrePalette.BOTANICAL -> Botanical
  SpectrePalette.GLASSHOUSE -> Glasshouse
  SpectrePalette.DESERT -> Desert
  SpectrePalette.SAMURAI -> Samurai
  SpectrePalette.FILM -> Film
  SpectrePalette.COMIC -> Comic
  SpectrePalette.LUXURY -> Luxury
  SpectrePalette.PALACE -> Palace
  SpectrePalette.MUSEUM -> Museum
  SpectrePalette.ALCHEMY -> Alchemy
  SpectrePalette.BESTIARY -> Bestiary
  SpectrePalette.AGENT -> Agent
  SpectrePalette.TINY_CITY -> TinyCity
  SpectrePalette.MONO -> mk(true, customColor, 0xFF9E9E9E, 0xFFBDBDBD, 0xFF0D0D0D, 0xFF141414, 0xFF202020, 0xFFEDEDED, 0xFF9E9E9E, 0xFF333333)
  SpectrePalette.CUSTOM -> {
    val a = Color(customColor)
    mk(true, customColor, toHexLong(shift(a, dh = 40f)), toHexLong(shift(a, dh = -40f)), 0xFF101013, 0xFF16161A, 0xFF212127, 0xFFEDEDEF, 0xFFB7B7BD, 0xFF5A5A63)
  }
}

private fun hsvColor(h: Float, s: Float, v: Float): Color {
  val hue = ((h % 360f) + 360f) % 360f
  return Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, s.coerceIn(0f, 1f), v.coerceIn(0f, 1f))))
}

@Composable
private fun rainbowScheme(speedMultiplier: Float = 1f): ColorScheme {
  val transition = rememberInfiniteTransition(label = "rainbow")
  val hue by transition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      tween((10_000 / speedMultiplier).toInt().coerceAtLeast(1000), easing = LinearEasing)
    ),
    label = "hue"
  )
  val h = hue % 360f
  return darkColorScheme(
    primary = hsvColor(h, 0.85f, 1f),
    onPrimary = Color(0xFF000000),
    primaryContainer = hsvColor(h, 0.75f, 0.28f),
    onPrimaryContainer = hsvColor(h, 0.25f, 0.96f),
    secondary = hsvColor(h + 120f, 0.85f, 1f),
    onSecondary = Color(0xFF000000),
    secondaryContainer = hsvColor(h + 120f, 0.75f, 0.28f),
    onSecondaryContainer = hsvColor(h + 120f, 0.25f, 0.96f),
    tertiary = hsvColor(h + 240f, 0.85f, 1f),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = hsvColor(h + 240f, 0.75f, 0.28f),
    onTertiaryContainer = hsvColor(h + 240f, 0.25f, 0.96f),
    background = Color(0xFF0B0B10),
    onBackground = Color(0xFFEDEDF2),
    surface = Color(0xFF12121A),
    onSurface = Color(0xFFEDEDF2),
    surfaceVariant = Color(0xFF1B1B26),
    onSurfaceVariant = Color(0xFFC5C5D4),
    outline = hsvColor(h, 0.25f, 0.55f),
    error = SpectreRed,
    onError = Color.Black
  )
}

@Composable
fun SpectreTheme(
  mode: ThemeMode = ThemeMode.SYSTEM,
  paletteName: String = SpectrePalette.DEFAULT.name,
  customColor: Long = 0xFF62E843,
  animationLevel: AnimationLevel = AnimationLevel.FULL,
  fxConfig: SpectreFxConfig = SpectreFxConfig(),
  content: @Composable () -> Unit
) {
  val palette = remember(paletteName) {
    runCatching { SpectrePalette.valueOf(paletteName) }.getOrDefault(SpectrePalette.DEFAULT)
  }
  val useDark = when (mode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
  }

  val adjustedConfig = remember(animationLevel, fxConfig) {
    when (animationLevel) {
      AnimationLevel.OFF -> fxConfig.copy(
        glowIntensity = 0f,
        enableColorShift = false,
        enableScanlineEffect = false,
        particleDensity = 0f,
        enablePressFeedback = false,
        enableSignalPulse = false,
        effectScale = 0f
      )
      AnimationLevel.REDUCED -> fxConfig.copy(
        glowIntensity = fxConfig.glowIntensity * 0.3f,
        enableScanlineEffect = false,
        particleDensity = 0f
      )
      AnimationLevel.FULL -> fxConfig
      AnimationLevel.INTENSE -> fxConfig.copy(
        glowIntensity = fxConfig.glowIntensity * 1.8f,
        glowSpeed = fxConfig.glowSpeed * 1.5f,
        particleDensity = fxConfig.particleDensity * 1.5f,
        effectScale = fxConfig.effectScale * 1.4f
      )
      AnimationLevel.MAXIMUM -> fxConfig.copy(
        glowIntensity = fxConfig.glowIntensity * 3f,
        glowSpeed = fxConfig.glowSpeed * 2.5f,
        particleDensity = fxConfig.particleDensity * 2f,
        enableColorShift = true,
        colorShiftSpeed = fxConfig.colorShiftSpeed * 3f,
        gradientRotationSpeed = fxConfig.gradientRotationSpeed / 2f,
        effectScale = fxConfig.effectScale * 2f
      )
    }
  }

  val scheme = when (palette) {
    SpectrePalette.DEFAULT -> if (useDark) DarkColors else LightColors
    SpectrePalette.LIGHT -> LightColors
    SpectrePalette.DARK -> DarkColors
    SpectrePalette.RAINBOW -> rainbowScheme(
      speedMultiplier = when (animationLevel) {
        AnimationLevel.OFF -> 0.01f
        AnimationLevel.REDUCED -> 0.5f
        AnimationLevel.FULL -> 1f
        AnimationLevel.INTENSE -> 1.5f
        AnimationLevel.MAXIMUM -> 3f
      }
    )
    else -> staticScheme(palette, customColor)
  }
  MaterialTheme(colorScheme = scheme) {
    SpectreFxHost(
      palette = palette,
      animationLevel = animationLevel,
      fxConfig = adjustedConfig
    ) {
      content()
    }
  }
}
