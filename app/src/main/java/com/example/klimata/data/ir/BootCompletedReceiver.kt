package com.example.klimata.data.ir

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Restores autonomous overnight alarms after device restarts or app package updates.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Log.i(TAG, "Device reboot or package update detected. Restoring autonomous thermal schedule.")
                ThermalScheduleManager.scheduleNextThermalDispatch(context)
            }
        }
    }

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
}
