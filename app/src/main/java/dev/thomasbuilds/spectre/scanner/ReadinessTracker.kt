package dev.thomasbuilds.spectre.scanner

internal class ReadinessTracker(
  private val warmupMs: Long
) {
  private var okSinceMs: Long = 0L
  private var hadData = false
  @Synchronized
  fun compute(
    statusOk: Boolean,
    hasData: Boolean,
    now: Long
  ): Boolean {
    if (!statusOk) {
      okSinceMs = 0L
      hadData = false
      return true
    }
    if (okSinceMs == 0L) okSinceMs = now
    if (hasData) hadData = true
    return hadData || (now - okSinceMs) > warmupMs
  }
}
