package dev.thomasbuilds.spectre.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.thomasbuilds.spectre.settings.Settings
import dev.thomasbuilds.spectre.settings.SettingsRepository
import dev.thomasbuilds.spectre.settings.ThemeMode
import dev.thomasbuilds.spectre.settings.WifiScanMode
import dev.thomasbuilds.spectre.ui.theme.SpectrePalette
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
  repository: SettingsRepository,
  appVersion: String,
  onBack: () -> Unit,
  onOpenHelp: () -> Unit,
  onOpenAppearance: () -> Unit
) {
  val scope = rememberCoroutineScope()
  val settings by repository.settings.collectAsStateWithLifecycle(initialValue = Settings.DEFAULTS)
  val paletteLabel = remember(settings.palette) {
    runCatching { SpectrePalette.valueOf(settings.palette).label }.getOrDefault("Default")
  }

  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Column(modifier = Modifier.fillMaxSize().padding(WindowInsets.safeDrawing.asPaddingValues())) {
      BackTopBar(title = "Settings", onBack = onBack) {}
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        SettingsCard("Appearance") {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable(onClick = onOpenAppearance),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f).padding(vertical = 4.dp)) {
              Text(
                "Themes & effects",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                paletteLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Text(
              "›",
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Spacer(Modifier.height(12.dp))
          Text(
            "Base mode",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(Modifier.height(6.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = settings.themeMode == ThemeMode.SYSTEM,
              onClick = { scope.launch { repository.setThemeMode(ThemeMode.SYSTEM) } },
              label = { Text("System") }
            )
            FilterChip(
              selected = settings.themeMode == ThemeMode.LIGHT,
              onClick = { scope.launch { repository.setThemeMode(ThemeMode.LIGHT) } },
              label = { Text("Light") }
            )
            FilterChip(
              selected = settings.themeMode == ThemeMode.DARK,
              onClick = { scope.launch { repository.setThemeMode(ThemeMode.DARK) } },
              label = { Text("Dark") }
            )
          }
        }

        SettingsCard("Foxhunt") {
          Text(
            "Wi-Fi scan speed",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(Modifier.height(6.dp))
          Text(
            "Make sure 'Wi-Fi scan throttling' is disabled in Developer options before activating fast mode.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = settings.wifiScanMode == WifiScanMode.THROTTLED,
              onClick = { scope.launch { repository.setWifiScanMode(WifiScanMode.THROTTLED) } },
              label = { Text("Standard") }
            )
            FilterChip(
              selected = settings.wifiScanMode == WifiScanMode.FAST,
              onClick = { scope.launch { repository.setWifiScanMode(WifiScanMode.FAST) } },
              label = { Text("Fast") }
            )
          }
        }

        SettingsCard("Lists") {
          ToggleRow(
            title = "Show out-of-range Wi-Fi APs",
            checked = settings.showStaleWifi
          ) { scope.launch { repository.setShowStaleWifi(it) } }
          Spacer(Modifier.height(8.dp))
          ToggleRow(
            title = "Show out-of-range Bluetooth devices",
            checked = settings.showStaleBluetooth
          ) { scope.launch { repository.setShowStaleBluetooth(it) } }
        }

        SettingsCard("About") {
          InfoRow("Version", appVersion)
          Spacer(Modifier.height(10.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onOpenHelp, modifier = Modifier.weight(1f)) {
              Text("Help")
            }
            OutlinedButton(
              onClick = { scope.launch { repository.resetToDefaults() } },
              modifier = Modifier.weight(1f)
            ) {
              Text("Reset defaults")
            }
          }
        }
        Spacer(Modifier.height(24.dp))
      }
    }
  }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
      Spacer(Modifier.height(10.dp))
      content()
    }
  }
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      title,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1f)
    )
    Switch(checked = checked, onCheckedChange = onChecked)
  }
}

@Composable
private fun InfoRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(
      value,
      style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}
