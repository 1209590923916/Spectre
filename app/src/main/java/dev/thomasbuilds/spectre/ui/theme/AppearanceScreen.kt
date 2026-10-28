package dev.thomasbuilds.spectre.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.thomasbuilds.spectre.settings.AnimationLevel
import dev.thomasbuilds.spectre.settings.Settings
import dev.thomasbuilds.spectre.settings.SettingsRepository
import dev.thomasbuilds.spectre.ui.BackTopBar
import kotlinx.coroutines.launch

private data class PaletteSection(val title: String, val palettes: List<SpectrePalette>)

private val Sections = listOf(
  PaletteSection("Core", listOf(SpectrePalette.DEFAULT, SpectrePalette.LIGHT, SpectrePalette.DARK)),
  PaletteSection("Spectre classics", listOf(SpectrePalette.HACKER, SpectrePalette.CYBERPUNK, SpectrePalette.KALI, SpectrePalette.BLUE, SpectrePalette.CYAN, SpectrePalette.ICEWAVE, SpectrePalette.SUNSET, SpectrePalette.SUNRISE, SpectrePalette.CRIMSON, SpectrePalette.RAINBOW)),
  PaletteSection("Fire & fury", listOf(SpectrePalette.FIRE, SpectrePalette.MAGMA, SpectrePalette.DRAGON, SpectrePalette.GOLD, SpectrePalette.TOXIC)),
  PaletteSection("Neon worlds", listOf(SpectrePalette.NEON_NIGHT_MARKET, SpectrePalette.TRANSIT, SpectrePalette.VAPORWAVE, SpectrePalette.RETRO_OS, SpectrePalette.ARCADE)),
  PaletteSection("Sky & space", listOf(SpectrePalette.COSMIC, SpectrePalette.DEEP_SPACE, SpectrePalette.AURORA, SpectrePalette.CLOUDS, SpectrePalette.AERO)),
  PaletteSection("Water & nature", listOf(SpectrePalette.UNDERWATER, SpectrePalette.AQUARIUM, SpectrePalette.BOTANICAL, SpectrePalette.GLASSHOUSE, SpectrePalette.DESERT, SpectrePalette.SAMURAI)),
  PaletteSection("Paper & craft", listOf(SpectrePalette.FILM, SpectrePalette.COMIC, SpectrePalette.LUXURY, SpectrePalette.PALACE, SpectrePalette.MUSEUM, SpectrePalette.ALCHEMY, SpectrePalette.BESTIARY)),
  PaletteSection("Special", listOf(SpectrePalette.AGENT, SpectrePalette.TINY_CITY, SpectrePalette.MONO, SpectrePalette.CUSTOM))
)

private fun argb(r: Int, g: Int, b: Int): Long =
  (0xFFL shl 24) or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()

@Composable
fun AppearanceScreen(repository: SettingsRepository, onBack: () -> Unit) {
  val scope = rememberCoroutineScope()
  val settings by repository.settings.collectAsStateWithLifecycle(initialValue = Settings.DEFAULTS)
  var red by remember(settings.customColor) { mutableStateOf(((settings.customColor shr 16) and 0xFFL).toInt()) }
  var green by remember(settings.customColor) { mutableStateOf(((settings.customColor shr 8) and 0xFFL).toInt()) }
  var blue by remember(settings.customColor) { mutableStateOf((settings.customColor and 0xFFL).toInt()) }

  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Column(modifier = Modifier.fillMaxSize().padding(WindowInsets.safeDrawing.asPaddingValues())) {
      BackTopBar(title = "Appearance", onBack = onBack) {}
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        Text(
          "Pick a palette — it applies instantly across the whole app. Tap or swipe anywhere on any screen to see each theme's effects.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Sections.forEach { sec ->
          Text(
            sec.title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
          )
          sec.palettes.forEach { pal ->
            val scheme = staticScheme(pal, settings.customColor)
            PaletteRow(
              label = pal.label,
              selected = settings.palette == pal.name,
              c1 = scheme.primary,
              c2 = scheme.secondary,
              c3 = scheme.tertiary,
              onClick = { scope.launch { repository.setPalette(pal.name) } }
            )
            Spacer(Modifier.height(8.dp))
          }
        }

        Text(
          "Custom color",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
        )
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(Color(settings.customColor))
                  .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
              )
              Spacer(Modifier.width(12.dp))
              Text(
                "Used by the 'Custom Color' and 'Mono + Accent' palettes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Spacer(Modifier.height(12.dp))
            Text("Presets", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            listOf(
              0xFF62E843, 0xFF00E676, 0xFF00E5FF, 0xFF2979FF, 0xFF557FF2, 0xFF9B6BFF, 0xFFB388FF,
              0xFFFF2EC4, 0xFFEC6A8D, 0xFFFF5722, 0xFFFFC107, 0xFFD4AF37, 0xFFE53935, 0xFF70A5A0
            ).chunked(7).forEach { rowColors ->
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowColors.forEach { c ->
                  Box(
                    Modifier
                      .size(32.dp)
                      .clip(CircleShape)
                      .background(Color(c))
                      .clickable { scope.launch { repository.setCustomColor(c) } }
                  )
                }
              }
              Spacer(Modifier.height(8.dp))
            }
            ColorSlider("Red", red, { red = it }) { scope.launch { repository.setCustomColor(argb(red, green, blue)) } }
            ColorSlider("Green", green, { green = it }) { scope.launch { repository.setCustomColor(argb(red, green, blue)) } }
            ColorSlider("Blue", blue, { blue = it }) { scope.launch { repository.setCustomColor(argb(red, green, blue)) } }
            Text(
              "#%02X%02X%02X".format(red, green, blue),
              style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Text(
          "Theme animations",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 14.dp, bottom = 4.dp)
        )
        AnimationLevel.entries.forEach { lvl ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { scope.launch { repository.setAnimationLevel(lvl) } },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (settings.animationLevel == lvl) MaterialTheme.colorScheme.primaryContainer
              else MaterialTheme.colorScheme.surface
            )
          ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
              Column(modifier = Modifier.weight(1f)) {
                Text(lvl.label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(lvl.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              if (settings.animationLevel == lvl) {
                Text("Active", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
              }
            }
          }
          Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(20.dp))
        Text(
          "Animation Studio",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
          "Change the intensity of your theme effects. These apply on top of the animation level above.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(bottom = 8.dp)
        )

        FxSlider(
          label = "Glow Intensity",
          value = settings.fxGlowIntensity,
          valueRange = 0f..3f,
          format = { "%.1fx".format(it) },
          onChange = { scope.launch { repository.setFxGlowIntensity(it) } }
        )

        FxSlider(
          label = "Glow Speed",
          value = settings.fxGlowSpeed,
          valueRange = 0.25f..4f,
          format = { "%.1fx".format(it) },
          onChange = { scope.launch { repository.setFxGlowSpeed(it) } }
        )

        FxSlider(
          label = "Gradient Rotation Speed",
          value = settings.fxGradientSpeed,
          valueRange = 1f..15f,
          format = { "%.1fs".format(it) },
          onChange = { scope.launch { repository.setFxGradientSpeed(it) } }
        )

        FxSlider(
          label = "Particle Density",
          value = settings.fxParticleDensity,
          valueRange = 0.5f..3f,
          format = { "%.1fx".format(it) },
          onChange = { scope.launch { repository.setFxParticleDensity(it) } }
        )

        FxSlider(
          label = "Tap Effect Size",
          value = settings.fxEffectScale,
          valueRange = 0.5f..3f,
          format = { "%.1fx".format(it) },
          onChange = { scope.launch { repository.setFxEffectScale(it) } }
        )

        Spacer(Modifier.height(8.dp))
        FxToggleRow("Color Shift", settings.fxEnableColorShift) {
          scope.launch { repository.setFxEnableColorShift(it) }
        }
        FxToggleRow("Shimmer Effect", settings.fxEnableScanlineEffect) {
          scope.launch { repository.setFxEnableScanlineEffect(it) }
        }
        FxToggleRow("Press Feedback", settings.fxEnablePressFeedback) {
          scope.launch { repository.setFxEnablePressFeedback(it) }
        }
        FxToggleRow("Signal Pulse (Foxhunt)", settings.fxEnableSignalPulse) {
          scope.launch { repository.setFxEnableSignalPulse(it) }
        }

        Spacer(Modifier.height(12.dp))
        Text("Quick Presets", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(
            onClick = {
              scope.launch {
                repository.setFxGlowIntensity(0.5f)
                repository.setFxGlowSpeed(0.7f)
                repository.setFxGradientSpeed(8f)
                repository.setFxEnableColorShift(false)
                repository.setFxParticleDensity(0.5f)
                repository.setFxEffectScale(0.6f)
              }
            }
          ) { Text("Subtle", style = MaterialTheme.typography.labelSmall) }

          OutlinedButton(
            onClick = {
              scope.launch {
                repository.setFxGlowIntensity(1f)
                repository.setFxGlowSpeed(1f)
                repository.setFxGradientSpeed(4f)
                repository.setFxEnableColorShift(false)
                repository.setFxParticleDensity(1f)
                repository.setFxEffectScale(1f)
              }
            }
          ) { Text("Balanced", style = MaterialTheme.typography.labelSmall) }

          OutlinedButton(
            onClick = {
              scope.launch {
                repository.setFxGlowIntensity(2f)
                repository.setFxGlowSpeed(2f)
                repository.setFxGradientSpeed(2f)
                repository.setFxEnableColorShift(true)
                repository.setFxColorShiftSpeed(1f)
                repository.setFxParticleDensity(2f)
                repository.setFxEffectScale(1.6f)
              }
            }
          ) { Text("Intense", style = MaterialTheme.typography.labelSmall) }

          OutlinedButton(
            onClick = {
              scope.launch {
                repository.setFxGlowIntensity(3f)
                repository.setFxGlowSpeed(4f)
                repository.setFxGradientSpeed(1f)
                repository.setFxEnableColorShift(true)
                repository.setFxColorShiftSpeed(3f)
                repository.setFxParticleDensity(3f)
                repository.setFxEffectScale(3f)
              }
            }
          ) { Text("MAX", style = MaterialTheme.typography.labelSmall) }
        }

        Spacer(Modifier.height(24.dp))
      }
    }
  }
}

@Composable
private fun PaletteRow(label: String, selected: Boolean, c1: Color, c2: Color, c3: Color, onClick: () -> Unit) {
  Card(
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    )
  ) {
    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(16.dp).clip(CircleShape).background(c1))
        Box(Modifier.size(16.dp).clip(CircleShape).background(c2))
        Box(Modifier.size(16.dp).clip(CircleShape).background(c3))
      }
      Spacer(Modifier.width(12.dp))
      Text(
        label,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f)
      )
      if (selected) {
        Text("Active", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
      }
    }
  }
}

@Composable
private fun ColorSlider(label: String, value: Int, onChange: (Int) -> Unit, onCommit: () -> Unit) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(52.dp))
    Slider(
      value = value.toFloat(),
      onValueChange = { onChange(it.toInt()) },
      onValueChangeFinished = onCommit,
      valueRange = 0f..255f,
      modifier = Modifier.weight(1f)
    )
    Text(
      "%3d".format(value),
      style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.width(34.dp)
    )
  }
}

@Composable
private fun FxSlider(
  label: String,
  value: Float,
  valueRange: ClosedFloatingPointRange<Float>,
  format: (Float) -> String,
  onChange: (Float) -> Unit
) {
  Column(modifier = Modifier.padding(vertical = 4.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
      Text(
        format(value),
        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
        color = MaterialTheme.colorScheme.primary
      )
    }
    Slider(
      value = value,
      onValueChange = onChange,
      valueRange = valueRange
    )
  }
}

@Composable
private fun FxToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    Switch(checked = checked, onCheckedChange = onCheckedChange)
  }
}
