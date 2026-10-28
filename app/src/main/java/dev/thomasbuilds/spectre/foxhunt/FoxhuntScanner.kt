package dev.thomasbuilds.spectre.foxhunt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat
import dev.thomasbuilds.spectre.settings.WifiScanMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Self-contained discovery for the Foxhunt screen: continuous BLE scanning plus
 * paced Wi-Fi scans. Everything it sees is pushed into [FoxhuntTracker].
 */
@SuppressLint("MissingPermission")
class FoxhuntScanner(
  private val context: Context,
  private val wifiScanMode: WifiScanMode
) {

  private val bluetoothAdapter =
    (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
  private val wifiManager =
    context.getSystemService(Context.WIFI_SERVICE) as? WifiManager

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private var running = false
  private var wifiTick: Job? = null

  val bleReady: Boolean
    get() = bluetoothAdapter?.isEnabled == true && bluetoothAdapter?.bluetoothLeScanner != null

  private val bleCallback = object : ScanCallback() {
    override fun onScanResult(callbackType: Int, result: ScanResult) {
      val name = runCatching { result.device.name }.getOrNull()
        ?: result.scanRecord?.deviceName
      FoxhuntTracker.updateRssi(FoxhuntTech.BLE, result.device.address, name, result.rssi)
    }

    override fun onScanFailed(errorCode: Int) {}
  }

  private val wifiReceiver = object : BroadcastReceiver() {
    override fun onReceive(c: Context?, intent: Intent?) {
      if (intent?.action != WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) return
      publishWifiResults()
    }
  }

  fun start() {
    if (running) return
    running = true
    runCatching {
      bluetoothAdapter?.bluetoothLeScanner?.startScan(
        null,
        ScanSettings.Builder()
          .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
          .build(),
        bleCallback
      )
    }
    runCatching {
      ContextCompat.registerReceiver(
        context,
        wifiReceiver,
        IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION),
        ContextCompat.RECEIVER_NOT_EXPORTED
      )
    }
    publishWifiResults()
    wifiTick = scope.launch {
      while (isActive) {
        @Suppress("DEPRECATION")
        runCatching { wifiManager?.startScan() }
        delay(wifiScanMode.periodMs)
      }
    }
  }

  fun stop() {
    if (!running) return
    running = false
    wifiTick?.cancel()
    wifiTick = null
    runCatching { bluetoothAdapter?.bluetoothLeScanner?.stopScan(bleCallback) }
    runCatching { context.unregisterReceiver(wifiReceiver) }
  }

  private fun publishWifiResults() {
    val results = runCatching { wifiManager?.scanResults }.getOrNull() ?: return
    results.forEach { sr ->
      val bssid = sr.BSSID
      if (bssid.isNullOrBlank()) return@forEach
      FoxhuntTracker.updateRssi(
        FoxhuntTech.WIFI,
        bssid,
        sr.SSID.takeIf { it.isNotBlank() },
        sr.level
      )
    }
  }
}
