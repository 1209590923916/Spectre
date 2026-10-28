package dev.thomasbuilds.spectre.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class AnimationLevel(val label: String, val description: String) {
  FULL("Full", "Ambient effects + tap & swipe reactions. Coolest, uses more battery."),
  REDUCED("Reduced", "Tap & swipe reactions only. No always-on background effects."),
  OFF("Off", "No animations at all. Best performance and battery."),
  INTENSE("Intense", "Faster particles, brighter glow, bigger tap effects."),
  MAXIMUM("Maximum", "Everything cranked to maximum intensity.")
}

enum class WifiScanMode(val label: String, val periodMs: Long) {
  THROTTLED("Standard (throttled)", 35_000L),
  FAST("Fast (unthrottled)", 5_000L)
}

data class Settings(
  val themeMode: ThemeMode = ThemeMode.SYSTEM,
  val showStaleWifi: Boolean = false,
  val showStaleBluetooth: Boolean = false,
  val palette: String = "DEFAULT",
  val customColor: Long = 0xFF62E843,
  val animationLevel: AnimationLevel = AnimationLevel.FULL,
  val wifiScanMode: WifiScanMode = WifiScanMode.THROTTLED,
  val fxGlowIntensity: Float = 1f,
  val fxGlowSpeed: Float = 1f,
  val fxColorShiftSpeed: Float = 0.5f,
  val fxGradientSpeed: Float = 4f,
  val fxParticleDensity: Float = 1f,
  val fxEffectScale: Float = 1f,
  val fxEnableColorShift: Boolean = false,
  val fxEnableScanlineEffect: Boolean = true,
  val fxEnablePressFeedback: Boolean = true,
  val fxEnableSignalPulse: Boolean = true
) {
  companion object {
    val DEFAULTS = Settings()
  }
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "spectre_settings")

class SettingsRepository(
  context: Context
) {
  private val store = context.applicationContext.dataStore

  val settings: Flow<Settings> =
    store.data.map { prefs ->
      Settings(
        themeMode =
          prefs[KEY_THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: Settings.DEFAULTS.themeMode,
        showStaleWifi = prefs[KEY_SHOW_STALE_WIFI] ?: Settings.DEFAULTS.showStaleWifi,
        showStaleBluetooth = prefs[KEY_SHOW_STALE_BT] ?: Settings.DEFAULTS.showStaleBluetooth,
        palette = prefs[KEY_PALETTE] ?: Settings.DEFAULTS.palette,
        customColor = prefs[KEY_CUSTOM_COLOR] ?: Settings.DEFAULTS.customColor,
        animationLevel =
          prefs[KEY_ANIMATION]?.let { runCatching { AnimationLevel.valueOf(it) }.getOrNull() }
            ?: Settings.DEFAULTS.animationLevel,
        wifiScanMode =
          prefs[KEY_WIFI_MODE]?.let { runCatching { WifiScanMode.valueOf(it) }.getOrNull() }
            ?: Settings.DEFAULTS.wifiScanMode,
        fxGlowIntensity = prefs[KEY_FX_GLOW_INTENSITY] ?: Settings.DEFAULTS.fxGlowIntensity,
        fxGlowSpeed = prefs[KEY_FX_GLOW_SPEED] ?: Settings.DEFAULTS.fxGlowSpeed,
        fxColorShiftSpeed = prefs[KEY_FX_COLOR_SHIFT_SPEED] ?: Settings.DEFAULTS.fxColorShiftSpeed,
        fxGradientSpeed = prefs[KEY_FX_GRADIENT_SPEED] ?: Settings.DEFAULTS.fxGradientSpeed,
        fxParticleDensity = prefs[KEY_FX_PARTICLE_DENSITY] ?: Settings.DEFAULTS.fxParticleDensity,
        fxEffectScale = prefs[KEY_FX_EFFECT_SCALE] ?: Settings.DEFAULTS.fxEffectScale,
        fxEnableColorShift = prefs[KEY_FX_COLOR_SHIFT] ?: Settings.DEFAULTS.fxEnableColorShift,
        fxEnableScanlineEffect = prefs[KEY_FX_SCANLINE_EFFECT] ?: Settings.DEFAULTS.fxEnableScanlineEffect,
        fxEnablePressFeedback = prefs[KEY_FX_PRESS_FEEDBACK] ?: Settings.DEFAULTS.fxEnablePressFeedback,
        fxEnableSignalPulse = prefs[KEY_FX_SIGNAL_PULSE] ?: Settings.DEFAULTS.fxEnableSignalPulse
      )
    }

  suspend fun setThemeMode(mode: ThemeMode) {
    store.edit { it[KEY_THEME] = mode.name }
  }

  suspend fun setShowStaleWifi(show: Boolean) {
    store.edit { it[KEY_SHOW_STALE_WIFI] = show }
  }

  suspend fun setShowStaleBluetooth(show: Boolean) {
    store.edit { it[KEY_SHOW_STALE_BT] = show }
  }

  suspend fun setPalette(name: String) {
    store.edit { it[KEY_PALETTE] = name }
  }

  suspend fun setCustomColor(argb: Long) {
    store.edit { it[KEY_CUSTOM_COLOR] = argb }
  }

  suspend fun setAnimationLevel(level: AnimationLevel) {
    store.edit { it[KEY_ANIMATION] = level.name }
  }

  suspend fun setWifiScanMode(mode: WifiScanMode) {
    store.edit { it[KEY_WIFI_MODE] = mode.name }
  }

  suspend fun setFxGlowIntensity(value: Float) {
    store.edit { it[KEY_FX_GLOW_INTENSITY] = value }
  }

  suspend fun setFxGlowSpeed(value: Float) {
    store.edit { it[KEY_FX_GLOW_SPEED] = value }
  }

  suspend fun setFxColorShiftSpeed(value: Float) {
    store.edit { it[KEY_FX_COLOR_SHIFT_SPEED] = value }
  }

  suspend fun setFxGradientSpeed(value: Float) {
    store.edit { it[KEY_FX_GRADIENT_SPEED] = value }
  }

  suspend fun setFxParticleDensity(value: Float) {
    store.edit { it[KEY_FX_PARTICLE_DENSITY] = value }
  }

  suspend fun setFxEffectScale(value: Float) {
    store.edit { it[KEY_FX_EFFECT_SCALE] = value }
  }

  suspend fun setFxEnableColorShift(enabled: Boolean) {
    store.edit { it[KEY_FX_COLOR_SHIFT] = enabled }
  }

  suspend fun setFxEnableScanlineEffect(enabled: Boolean) {
    store.edit { it[KEY_FX_SCANLINE_EFFECT] = enabled }
  }

  suspend fun setFxEnablePressFeedback(enabled: Boolean) {
    store.edit { it[KEY_FX_PRESS_FEEDBACK] = enabled }
  }

  suspend fun setFxEnableSignalPulse(enabled: Boolean) {
    store.edit { it[KEY_FX_SIGNAL_PULSE] = enabled }
  }

  suspend fun resetToDefaults() {
    store.edit { it.clear() }
  }

  private companion object {
    val KEY_THEME = stringPreferencesKey("theme_mode")
    val KEY_SHOW_STALE_WIFI = booleanPreferencesKey("show_stale_wifi")
    val KEY_SHOW_STALE_BT = booleanPreferencesKey("show_stale_bluetooth")
    val KEY_PALETTE = stringPreferencesKey("palette")
    val KEY_CUSTOM_COLOR = longPreferencesKey("custom_color")
    val KEY_ANIMATION = stringPreferencesKey("animation_level")
    val KEY_WIFI_MODE = stringPreferencesKey("wifi_scan_mode")
    val KEY_FX_GLOW_INTENSITY = floatPreferencesKey("fx_glow_intensity")
    val KEY_FX_GLOW_SPEED = floatPreferencesKey("fx_glow_speed")
    val KEY_FX_COLOR_SHIFT_SPEED = floatPreferencesKey("fx_color_shift_speed")
    val KEY_FX_GRADIENT_SPEED = floatPreferencesKey("fx_gradient_speed")
    val KEY_FX_PARTICLE_DENSITY = floatPreferencesKey("fx_particle_density")
    val KEY_FX_EFFECT_SCALE = floatPreferencesKey("fx_effect_scale")
    val KEY_FX_COLOR_SHIFT = booleanPreferencesKey("fx_color_shift")
    val KEY_FX_SCANLINE_EFFECT = booleanPreferencesKey("fx_scanline_effect")
    val KEY_FX_PRESS_FEEDBACK = booleanPreferencesKey("fx_press_feedback")
    val KEY_FX_SIGNAL_PULSE = booleanPreferencesKey("fx_signal_pulse")
  }
}
