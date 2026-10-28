package dev.thomasbuilds.spectre.foxhunt

import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

enum class FoxhuntTech(val label: String) {
  BLE("Bluetooth LE"),
  WIFI("Wi-Fi")
}

data class FoxhuntDevice(
  val tech: FoxhuntTech,
  val address: String,
  val name: String?,
  val lastRssi: Int?,
  val lastSeenMs: Long = SystemClock.elapsedRealtime()
)

data class FoxhuntReading(
  val rssi: Int,
  val smoothed: Float,
  val timestampMs: Long
)

object FoxhuntTracker {
  private const val EMA_ALPHA = 0.35f
  private const val MAX_HISTORY = 120
  private const val MAX_DEVICES = 60

  val recentDevices = MutableStateFlow<List<FoxhuntDevice>>(emptyList())
  val activeTarget = MutableStateFlow<FoxhuntDevice?>(null)
  val readings = MutableStateFlow<List<FoxhuntReading>>(emptyList())

  private val ema = HashMap<String, Float>()

  fun register(tech: FoxhuntTech, address: String, name: String?, rssi: Int?) {
    recentDevices.update { list ->
      val existing = list.firstOrNull { it.tech == tech && it.address == address }
      val merged = FoxhuntDevice(
        tech = tech,
        address = address,
        name = name?.takeIf { it.isNotBlank() } ?: existing?.name,
        lastRssi = rssi ?: existing?.lastRssi,
        lastSeenMs = SystemClock.elapsedRealtime()
      )
      (listOf(merged) + list.filterNot { it.tech == tech && it.address == address })
        .take(MAX_DEVICES)
    }
  }

  fun updateRssi(tech: FoxhuntTech, address: String, name: String?, rssi: Int) {
    val key = "${tech.name}:$address"
    val previous = ema[key]
    val smoothed = if (previous == null) rssi.toFloat()
    else previous + EMA_ALPHA * (rssi - previous)
    ema[key] = smoothed
    register(tech, address, name, rssi)
    val active = activeTarget.value
    if (active != null && active.tech == tech && active.address == address) {
      readings.update {
        (it + FoxhuntReading(rssi, smoothed, SystemClock.elapsedRealtime()))
          .takeLast(MAX_HISTORY)
      }
    }
  }

  fun select(device: FoxhuntDevice) {
    activeTarget.value = device
    readings.value = emptyList()
    ema.remove("${device.tech.name}:${device.address}")
  }

  fun clearTarget() {
    activeTarget.value = null
    readings.value = emptyList()
  }

  fun resetReadings() {
    activeTarget.value?.let { ema.remove("${it.tech.name}:${it.address}") }
    readings.value = emptyList()
  }
}

object FoxhuntLauncher {
  var open: (() -> Unit)? = null
}
