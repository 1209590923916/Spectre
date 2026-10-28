package dev.thomasbuilds.spectre.ui

import android.os.SystemClock
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.thomasbuilds.spectre.foxhunt.FoxhuntDevice
import dev.thomasbuilds.spectre.foxhunt.FoxhuntLauncher
import dev.thomasbuilds.spectre.foxhunt.FoxhuntReading
import dev.thomasbuilds.spectre.foxhunt.FoxhuntScanner
import dev.thomasbuilds.spectre.foxhunt.FoxhuntTech
import dev.thomasbuilds.spectre.foxhunt.FoxhuntTracker
import dev.thomasbuilds.spectre.settings.WifiScanMode
import dev.thomasbuilds.spectre.ui.components.Badge
import dev.thomasbuilds.spectre.ui.theme.SpectreGreen
import dev.thomasbuilds.spectre.ui.theme.SpectreOrange
import dev.thomasbuilds.spectre.ui.theme.SpectreRed
import dev.thomasbuilds.spectre.ui.theme.SpectreYellow
import dev.thomasbuilds.spectre.ui.theme.spectreGlow
import dev.thomasbuilds.spectre.ui.theme.spectreGradientBorder
import dev.thomasbuilds.spectre.ui.theme.spectreScanline
import dev.thomasbuilds.spectre.ui.theme.spectreSignalPulse
import kotlinx.coroutines.delay
import kotlin.math.pow

@Composable
fun FoxhuntScreen(onBack: () -> Unit, wifiScanMode: WifiScanMode) {
  val context = LocalContext.current
  val scanner = remember(wifiScanMode) { FoxhuntScanner(context, wifiScanMode) }
  val target by FoxhuntTracker.activeTarget.collectAsState()

  DisposableEffect(scanner) {
    scanner.start()
    onDispose { scanner.stop() }
  }

  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Column(modifier = Modifier.fillMaxSize().padding(WindowInsets.safeDrawing.asPaddingValues())) {
      BackTopBar(title = "Foxhunt", onBack = onBack) {}
      val active = target
      if (active == null) {
        FoxhuntPicker(bleReady = scanner.bleReady, wifiScanMode = wifiScanMode)
      } else {
        FoxhuntTracking(active)
      }
    }
  }
}

@Composable
private fun FoxhuntPicker(bleReady: Boolean, wifiScanMode: WifiScanMode) {
  val devices by FoxhuntTracker.recentDevices.collectAsState()
  var tab by rememberSaveable { mutableStateOf(0) }
  val tech = if (tab == 0) FoxhuntTech.BLE else FoxhuntTech.WIFI
  val shown = remember(devices, tab) {
    devices.filter { it.tech == tech }.sortedByDescending { it.lastRssi ?: -127 }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    TabRow(selectedTabIndex = tab) {
      Tab(
        selected = tab == 0,
        onClick = { tab = 0 },
        text = { Text("Bluetooth LE") },
        icon = { Icon(Icons.Rounded.Bluetooth, contentDescription = null) }
      )
      Tab(
        selected = tab == 1,
        onClick = { tab = 1 },
        text = { Text("Wi-Fi") },
        icon = { Icon(Icons.Rounded.Wifi, contentDescription = null) }
      )
    }
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      item { LiveCard(tech, shown.size, bleReady, wifiScanMode) }
      if (shown.isEmpty()) {
        item { EmptyCard(tech) }
      } else {
        item {
          Text(
            "${shown.size} found — strongest first",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
          )
        }
        items(shown, key = { it.address }) { device ->
          FoxhuntPickRow(device)
        }
      }
    }
  }
}

@Composable
private fun LiveCard(tech: FoxhuntTech, found: Int, bleReady: Boolean, wifiScanMode: WifiScanMode) {
  val transition = rememberInfiniteTransition(label = "pulse")
  val pulse by transition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing)),
    label = "pulseAlpha"
  )
  val primaryColor = MaterialTheme.colorScheme.primary
  val tertiaryColor = MaterialTheme.colorScheme.tertiary
  val secondaryColor = MaterialTheme.colorScheme.secondary

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .spectreGradientBorder(
        colors = listOf(primaryColor, tertiaryColor, secondaryColor, primaryColor),
        borderWidth = 2.dp,
        cornerRadius = 14.dp
      ),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(
      modifier = Modifier
        .padding(16.dp)
        .spectreScanline(color = primaryColor, alpha = 0.06f)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          Modifier
            .size(9.dp)
            .spectreSignalPulse(isActive = true, color = SpectreGreen)
            .background(SpectreGreen.copy(alpha = pulse), CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text(
          "Scanning ${tech.label} · $found found",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
      Spacer(Modifier.height(6.dp))
      Text(
        when {
          tech == FoxhuntTech.BLE && !bleReady ->
            "Bluetooth is off — turn it on to track BLE devices."
          tech == FoxhuntTech.WIFI && wifiScanMode == WifiScanMode.THROTTLED ->
            "Wi-Fi mode is Standard: readings arrive about every 35 s."
          tech == FoxhuntTech.WIFI ->
            "Fast Wi-Fi mode: readings every ~5 s."
          else ->
            "Tap a device to track it. Walk around and follow the signal — everything is " +
              "sorted strongest-first and updates live."
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun EmptyCard(tech: FoxhuntTech) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Text(
      if (tech == FoxhuntTech.BLE) {
        "No Bluetooth devices yet. Make sure Bluetooth is on."
      } else {
        "No Wi-Fi networks yet."
      },
      modifier = Modifier.padding(16.dp),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
private fun FoxhuntPickRow(device: FoxhuntDevice) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { FoxhuntTracker.select(device) }
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    device.lastRssi?.let { SignalBars(it, rssiColor(it)) } ?: SignalBars(-100, MaterialTheme.colorScheme.surfaceVariant)
    Spacer(Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        device.name ?: "Unknown device",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        device.address,
        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    device.lastRssi?.let { rssi ->
      Column(horizontalAlignment = Alignment.End) {
        Text(
          "$rssi dBm",
          style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
          color = rssiColor(rssi)
        )
        Text(
          "≈ ${"%.0f".format(estimateDistanceMeters(rssi, device.tech))} m",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
private fun SignalBars(rssi: Int, color: Color) {
  val level = when {
    rssi > -50 -> 4
    rssi > -65 -> 3
    rssi > -80 -> 2
    else -> 1
  }
  val transition = rememberInfiniteTransition(label = "signalBars")
  val barGlow by transition.animateFloat(
    initialValue = 0.7f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      tween(if (rssi > -60) 400 else 1000, easing = LinearEasing),
      repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
    ),
    label = "barGlow"
  )

  Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
    repeat(4) { i ->
      val isActive = i < level
      Box(
        Modifier
          .size(width = 4.dp, height = (6 + i * 3).dp)
          .spectreGlow(
            color = if (isActive) color else Color.Transparent,
            cornerRadius = 1.dp,
            borderWidth = 1.dp,
            maxAlpha = if (isActive) 0.4f * barGlow else 0f
          )
          .background(
            if (isActive) color.copy(alpha = barGlow) else MaterialTheme.colorScheme.surfaceVariant,
            RoundedCornerShape(1.dp)
          )
      )
    }
  }
}

@Composable
private fun FoxhuntTracking(target: FoxhuntDevice) {
  val readings by FoxhuntTracker.readings.collectAsState()
  var nowMs by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
  LaunchedEffect(Unit) {
    while (true) {
      nowMs = SystemClock.elapsedRealtime()
      delay(1_000)
    }
  }
  val currentRssi = readings.lastOrNull()?.smoothed?.toInt() ?: target.lastRssi

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    TargetCard(target, currentRssi)
    if (currentRssi == null) {
      WaitingCard(target.tech)
    } else {
      GaugeCard(currentRssi)
      StatsCard(target.tech, currentRssi, readings, nowMs)
      if (readings.size >= 2) {
        TrendCard(readings)
        HistoryCard(readings)
      }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      OutlinedButton(
        onClick = { FoxhuntTracker.clearTarget() },
        modifier = Modifier.weight(1f)
      ) { Text("Switch target") }
      OutlinedButton(
        onClick = { FoxhuntTracker.resetReadings() },
        modifier = Modifier.weight(1f)
      ) { Text("Reset history") }
    }
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun TargetCard(target: FoxhuntDevice, currentRssi: Int?) {
  val isStrong = (currentRssi ?: -100) > -65
  val accent = if (currentRssi != null) rssiColor(currentRssi) else MaterialTheme.colorScheme.primary

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .spectreGlow(
        color = accent,
        cornerRadius = 14.dp,
        borderWidth = 2.dp,
        maxAlpha = if (isStrong) 0.8f else 0.5f
      ),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Badge(if (target.tech == FoxhuntTech.BLE) "BLE" else "Wi-Fi")
        Spacer(Modifier.width(12.dp))
        Text(
          target.name ?: "Unknown device",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.weight(1f)
        )
        currentRssi?.let {
          SignalBars(it, rssiColor(it))
          Spacer(Modifier.width(8.dp))
          Text(
            "$it dBm",
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
            color = rssiColor(it),
            modifier = Modifier.spectreSignalPulse(isActive = isStrong, color = accent)
          )
        }
      }
      Spacer(Modifier.height(6.dp))
      Text(
        target.address,
        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun WaitingCard(tech: FoxhuntTech) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        "Waiting for signal…",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
      )
      Spacer(Modifier.height(6.dp))
      Text(
        "No ${tech.label} readings yet. The scanner is running — make sure the device is on " +
          "and advertising. Wi-Fi targets update about every 30 s (or ~5 s in Fast mode).",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun GaugeCard(rssi: Int) {
  val progressTarget = ((rssi + 100f) / 60f).coerceIn(0f, 1f)
  val progress by animateFloatAsState(
    targetValue = progressTarget,
    animationSpec = tween(600),
    label = "signal"
  )
  val trackColor = MaterialTheme.colorScheme.surfaceVariant
  val accent = rssiColor(rssi)
  val hint = when {
    progress < 0.34f -> "Weak — move around and watch the gauge climb"
    progress < 0.67f -> "Fair — you're in range, keep closing in"
    else -> "Strong — the transmitter is very close"
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .spectreGlow(color = accent, cornerRadius = 14.dp, borderWidth = 2.dp),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Canvas(modifier = Modifier.size(200.dp)) {
        val stroke = 14.dp.toPx()
        val inset = stroke / 2
        val arcSize = Size(size.width - stroke, size.height - stroke)
        drawArc(
          color = trackColor,
          startAngle = -90f,
          sweepAngle = 360f,
          useCenter = false,
          topLeft = Offset(inset, inset),
          size = arcSize,
          style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        if (progress > 0.02f) {
          drawArc(
            color = accent.copy(alpha = 0.3f),
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = stroke * 1.6f, cap = StrokeCap.Round)
          )
          drawArc(
            color = accent,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
          )
        }
      }
      Spacer(Modifier.height(12.dp))
      Text(
        "$rssi dBm",
        style = MaterialTheme.typography.displaySmall.copy(
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        ),
        color = accent,
        modifier = Modifier.spectreSignalPulse(isActive = progress > 0.7f, color = accent)
      )
      Spacer(Modifier.height(4.dp))
      Text(
        hint,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun StatsCard(
  tech: FoxhuntTech,
  rssi: Int,
  readings: List<FoxhuntReading>,
  nowMs: Long
) {
  SelectionContainer {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      StatRow("Smoothed RSSI", "$rssi dBm")
      readings.lastOrNull()?.let {
        StatRow("Last raw reading", "${it.rssi} dBm · ${ago(nowMs - it.timestampMs)}")
      }
      StatRow("Estimated distance", "≈ ${"%.1f".format(estimateDistanceMeters(rssi, tech))} m")
      StatRow("Distance confidence", distanceConfidenceLabel(readings))
      StatRow("Readings captured", "${readings.size}")
    }
  }
}

@Composable
private fun StatRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      value,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurface,
      fontFamily = FontFamily.Monospace
    )
  }
}

@Composable
private fun TrendCard(readings: List<FoxhuntReading>) {
  val delta = readings.last().smoothed - readings[readings.size - 2].smoothed
  val label: String
  val color: Color
  when {
    delta > 2f -> { label = "Getting warmer"; color = SpectreGreen }
    delta < -2f -> { label = "Getting colder"; color = SpectreRed }
    else -> { label = "Holding steady"; color = MaterialTheme.colorScheme.onSurfaceVariant }
  }
  val deltaText = "%+.0f".format(delta)
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          "Trend",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.primary
        )
        Text(
          "Latest two smoothed readings of ${readings.size} captured.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Text(
        "$label ($deltaText dB)",
        style = MaterialTheme.typography.titleMedium,
        color = color
      )
    }
  }
}

@Composable
private fun HistoryCard(readings: List<FoxhuntReading>) {
  val lineColor = MaterialTheme.colorScheme.primary
  val gridColor = MaterialTheme.colorScheme.surfaceVariant
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          "Signal history",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.weight(1f)
        )
        val span = readings.last().timestampMs - readings.first().timestampMs
        Text(
          "${readings.size} pts · ${formatSpan(span)}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Spacer(Modifier.height(8.dp))
      Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
        val values = readings.map { it.smoothed }
        if (values.size < 2) return@Canvas
        val min = values.minOrNull() ?: 0f
        val max = values.maxOrNull() ?: 0f
        val range = (max - min).coerceAtLeast(4f)
        val stepX = size.width / (values.size - 1)
        drawLine(
          color = gridColor,
          start = Offset(0f, size.height / 2),
          end = Offset(size.width, size.height / 2),
          strokeWidth = 1.dp.toPx()
        )
        val path = Path()
        values.forEachIndexed { i, v ->
          val x = i * stepX
          val y = size.height - ((v - min) / range) * (size.height - 12f) - 6f
          if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
          path = path,
          color = lineColor,
          style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
      }
    }
  }
}

/* ---------------- Shared bits ---------------- */

internal fun rssiColor(rssi: Int): Color = when {
  rssi > -60 -> SpectreGreen
  rssi > -75 -> SpectreYellow
  rssi > -85 -> SpectreOrange
  else -> SpectreRed
}

private fun estimateDistanceMeters(rssi: Int, tech: FoxhuntTech): Float {
  val txPower = if (tech == FoxhuntTech.BLE) -59f else -50f
  val pathLossN = if (tech == FoxhuntTech.BLE) 2.2f else 3.0f
  return 10f.pow((txPower - rssi) / (10f * pathLossN))
}

private fun distanceConfidenceLabel(readings: List<FoxhuntReading>): String {
  val window = readings.takeLast(12).map { it.smoothed }
  if (window.size < 2) return "low (needs more readings)"
  val mean = window.average()
  val variance = window.map { (it - mean) * (it - mean) }.average()
  return when {
    variance < 25 -> "high"
    variance < 100 -> "medium"
    else -> "low"
  }
}

private fun ago(ms: Long): String = when {
  ms < 1_000 -> "just now"
  ms < 60_000 -> "${ms / 1000}s ago"
  else -> "${ms / 60_000}m ago"
}

private fun formatSpan(ms: Long): String {
  val totalSeconds = ms / 1000
  return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}

@Composable
fun FoxhuntButton(
  tech: FoxhuntTech,
  address: String,
  name: String?,
  modifier: Modifier = Modifier
) {
  OutlinedButton(
    onClick = {
      FoxhuntTracker.select(FoxhuntDevice(tech, address, name, null))
      FoxhuntLauncher.open?.invoke()
    },
    modifier = modifier,
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
  ) {
    Text("Foxhunt", style = MaterialTheme.typography.labelMedium)
  }
}
