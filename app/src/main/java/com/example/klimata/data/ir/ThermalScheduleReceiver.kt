package com.example.klimata.data.ir

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log

/**
 * Handles exact wake-up alarms dispatched by Android's [android.app.AlarmManager].
 *
 * Acquires a temporary partial [PowerManager.WakeLock] to hold CPU execution while
 * pulsing 38 kHz infrared packets through the hardware emitter, re-arming the next
 * alarm step before releasing lock back to system power management.
 */
class ThermalScheduleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ThermalScheduleManager.ACTION_THERMAL_STEP_DISPATCH) return

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "klimata:ThermalScheduleWakeLock"
        )?.apply {
            setReferenceCounted(false)
            acquire(WAKELOCK_TIMEOUT_MS)
        }

        try {
            Log.i(TAG, "Autonomous thermal dispatch alarm triggered. Firing active step.")
            ThermalScheduleManager.dispatchActiveStepNow(context)
            ThermalScheduleManager.scheduleNextThermalDispatch(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error executing scheduled thermal dispatch", e)
        } finally {
            try {
                if (wakeLock?.isHeld == true) {
                    wakeLock.release()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to release wake lock cleanly", e)
            }
        }
    }

    companion object {
        private const val TAG = "ThermalScheduleReceiver"
        private const val WAKELOCK_TIMEOUT_MS = 5000L
    }
}
