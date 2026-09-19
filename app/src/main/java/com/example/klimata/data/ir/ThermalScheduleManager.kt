package com.example.klimata.data.ir

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.klimata.data.DispatchState
import com.example.klimata.data.RoomState
import com.example.klimata.data.ThermalStep
import com.example.klimata.data.storage.KlimataPreferences
import java.util.Calendar
import java.util.Locale

/**
 * Coordinates autonomous overnight stepping across configured AC rooms.
 *
 * Emits exact RTC_WAKEUP alarms to trigger hardware IR bursts at critical diurnal boundaries
 * (Pre-Cool, Metabolic Drift, Ambient Trough Sync, and Fan Coasting) while the device screen
 * is locked and CPU is in low-power Doze state.
 */
object ThermalScheduleManager {

    const val TAG = "ThermalScheduleManager"
    const val ACTION_THERMAL_STEP_DISPATCH = "com.example.klimata.action.THERMAL_STEP_DISPATCH"
    const val REQUEST_CODE_THERMAL_STEP = 9012

    /**
     * Calculates the millisecond timestamp of the next top-of-the-hour step boundary.
     */
    fun calculateNextStepTimeMillis(fromMillis: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = fromMillis
            add(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    /**
     * Resolves whether any configured room has active power and autonomous eco scheduling enabled.
     */
    fun hasActiveAutonomousRooms(rooms: List<RoomState>): Boolean {
        return rooms.any { it.isPowerOn && it.isEcoEnabled }
    }

    /**
     * Re-arms the exact background wake-up alarm for the next scheduled thermal transition.
     */
    fun scheduleNextThermalDispatch(context: Context): Long? {
        val rooms = KlimataPreferences.loadRooms(context)
        if (!hasActiveAutonomousRooms(rooms)) {
            cancelSchedule(context)
            Log.d(TAG, "No active autonomous rooms with eco enabled. Cancelled thermal schedule alarms.")
            return null
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager == null) {
            Log.w(TAG, "AlarmManager not available on device")
            return null
        }

        val triggerTimeMs = calculateNextStepTimeMillis()
        val intent = Intent(context, ThermalScheduleReceiver::class.java).apply {
            action = ACTION_THERMAL_STEP_DISPATCH
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_THERMAL_STEP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
            }
            Log.i(TAG, "Armed autonomous thermal dispatch alarm for timestamp $triggerTimeMs")
            return triggerTimeMs
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing exact alarm permission. Cannot schedule exact wake-up.", e)
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to arm thermal dispatch alarm", e)
            return null
        }
    }

    /**
     * Cancels any pending scheduled alarm.
     */
    fun cancelSchedule(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ThermalScheduleReceiver::class.java).apply {
            action = ACTION_THERMAL_STEP_DISPATCH
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_THERMAL_STEP,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    /**
     * Executes the physical IR dispatch for all currently active rooms matching the current hour.
     *
     * Synchronizes room state in persistent storage so user-facing UI immediately reflects
     * autonomous setpoints and fan states upon next launch.
     */
    fun dispatchActiveStepNow(
        context: Context,
        currentHourKey: String = String.format(Locale.US, "%02d:00", Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
    ): Int {
        val rooms = KlimataPreferences.loadRooms(context)
        if (rooms.isEmpty()) return 0

        val irBlaster = IrBlasterService(context)
        var dispatchedCount = 0

        val updatedRooms = rooms.map { room ->
            if (!room.isPowerOn || !room.isEcoEnabled) {
                room
            } else {
                val activeStep = room.thermalSteps.find { it.time == currentHourKey }
                    ?: room.thermalSteps.firstOrNull { it.isActive }

                if (activeStep != null) {
                    val targetTemp = if (activeStep.setpointCelsius > 0) {
                        activeStep.setpointCelsius
                    } else {
                        room.profile.currentSetpoint
                    }

                    val effectiveMode = if (activeStep.setpointCelsius == 0) {
                        "Fan"
                    } else {
                        room.profile.mode
                    }

                    val effectiveFan = when (activeStep.fanMode.lowercase(Locale.ROOT)) {
                        "high fan" -> "High"
                        "quiet fan" -> "Quiet"
                        "eco fan" -> "Eco"
                        "circulation" -> "Low"
                        else -> "Auto"
                    }

                    if (irBlaster.hasEmitter) {
                        irBlaster.dispatchAcCommand(
                            brand = room.profile.brand,
                            power = true,
                            temp = targetTemp,
                            mode = effectiveMode,
                            fanSpeed = effectiveFan,
                            isEco = true,
                            swing = room.profile.swing,
                            codeSetId = room.profile.irCodeSet
                        )
                    }

                    dispatchedCount++

                    val updatedSteps = room.thermalSteps.map { s ->
                        s.copy(
                            isActive = (s.time == currentHourKey),
                            isCompleted = isStepBeforeHour(s.time, currentHourKey)
                        )
                    }

                    room.copy(
                        currentTemp = targetTemp,
                        targetTemp = targetTemp,
                        profile = room.profile.copy(
                            currentSetpoint = targetTemp,
                            mode = effectiveMode,
                            fanSpeed = effectiveFan
                        ),
                        thermalSteps = updatedSteps,
                        dispatchState = DispatchState(
                            isAutonomous = true,
                            statusLabel = "Autonomous Active (${activeStep.label})",
                            dispatchMethod = if (irBlaster.hasEmitter) "ConsumerIR (38 kHz)" else "Telemetry Sync"
                        )
                    )
                } else {
                    room
                }
            }
        }

        if (dispatchedCount > 0) {
            KlimataPreferences.saveRooms(context, updatedRooms)
            Log.i(TAG, "Autonomous thermal dispatch complete: updated $dispatchedCount rooms for step $currentHourKey")
        }

        return dispatchedCount
    }

    private fun isStepBeforeHour(stepTime: String, currentHourTime: String): Boolean {
        val stepHour = stepTime.split(":").firstOrNull()?.toIntOrNull() ?: return false
        val currentHour = currentHourTime.split(":").firstOrNull()?.toIntOrNull() ?: return false
        return stepHour < currentHour
    }
}
