package com.example.klimata.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class WalkedCorner(
    val position: Offset,
    val wallLengthMeters: Float,
    val cornerIndex: Int
)

class RoomWalkerSensorManager(
    private val context: Context,
    val strideLengthMeters: Float = 0.70f,
    val onStateUpdate: () -> Unit = {}
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val stepDetector = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private val rotationVector = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    var isTracking = false
        private set

    var totalSteps = 0
        private set

    var currentWallSteps = 0
        private set

    var currentHeadingDeg = 0f
        private set

    var currentPosition = Offset.Zero
        private set

    private val _corners = mutableListOf<Offset>()
    val corners: List<Offset> get() = _corners

    private val _walkedCorners = mutableListOf<WalkedCorner>()
    val walkedCorners: List<WalkedCorner> get() = _walkedCorners

    var calculatedAreaM2 = 0
        private set

    // Accelerometer peak detection fallback for devices/emulators without dedicated step detectors
    private var lastAccelMagnitude = 9.8f
    private var lastStepTime = 0L

    fun startTracking() {
        if (isTracking) return
        reset()
        isTracking = true

        stepDetector?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        rotationVector?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        if (stepDetector == null) {
            accelerometer?.let {
                sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
        onStateUpdate()
    }

    fun stopTracking() {
        if (!isTracking) return
        isTracking = false
        sensorManager?.unregisterListener(this)
        onStateUpdate()
    }

    fun reset() {
        totalSteps = 0
        currentWallSteps = 0
        currentPosition = Offset.Zero
        _corners.clear()
        _walkedCorners.clear()
        _corners.add(Offset.Zero)
        calculatedAreaM2 = 0
        onStateUpdate()
    }

    fun markCorner(): Boolean {
        if (!isTracking) return false
        val wallLength = currentWallSteps * strideLengthMeters

        val newCorner = currentPosition
        _corners.add(newCorner)
        _walkedCorners.add(
            WalkedCorner(
                position = newCorner,
                wallLengthMeters = (wallLength * 10f).roundToInt() / 10f,
                cornerIndex = _corners.size - 1
            )
        )
        currentWallSteps = 0
        updateEstimatedArea()
        onStateUpdate()
        return true
    }

    fun finishRoom(): Int {
        if (_corners.size >= 3) {
            val lastWallLength = currentWallSteps * strideLengthMeters
            if (lastWallLength > 0.5f) {
                _walkedCorners.add(
                    WalkedCorner(
                        position = Offset.Zero,
                        wallLengthMeters = (lastWallLength * 10f).roundToInt() / 10f,
                        cornerIndex = _corners.size
                    )
                )
            }
            updateEstimatedArea()
        }
        stopTracking()
        return calculatedAreaM2.coerceAtLeast(10)
    }

    fun simulateStep() {
        processStep()
    }

    fun simulateWalkCorner(lengthMeters: Float) {
        val steps = (lengthMeters / strideLengthMeters).roundToInt().coerceAtLeast(1)
        for (i in 0 until steps) {
            processStep()
        }
        markCorner()
        currentHeadingDeg = (currentHeadingDeg + 90f) % 360f
    }

    private fun processStep() {
        totalSteps++
        currentWallSteps++

        val rad = Math.toRadians(currentHeadingDeg.toDouble())
        val dx = (strideLengthMeters * sin(rad)).toFloat()
        val dy = -(strideLengthMeters * cos(rad)).toFloat()

        currentPosition = Offset(currentPosition.x + dx, currentPosition.y + dy)
        updateEstimatedArea()
        onStateUpdate()
    }

    private fun updateEstimatedArea() {
        if (_corners.size < 3) {
            val currentWallLength = currentWallSteps * strideLengthMeters
            val firstWallLength = _walkedCorners.firstOrNull()?.wallLengthMeters ?: currentWallLength
            calculatedAreaM2 = (firstWallLength * currentWallLength).roundToInt().coerceIn(0, 80)
            return
        }

        // Polygon Shoelace area formula
        var areaSum = 0.0
        val poly = _corners.toMutableList()
        poly.add(poly.first()) // close polygon

        for (i in 0 until poly.size - 1) {
            val p1 = poly[i]
            val p2 = poly[i + 1]
            areaSum += (p1.x * p2.y - p2.x * p1.y)
        }

        val absArea = abs(areaSum / 2.0).toFloat()
        calculatedAreaM2 = absArea.roundToInt().coerceIn(8, 120)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !isTracking) return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_DETECTOR -> {
                processStep()
            }
            Sensor.TYPE_ROTATION_VECTOR -> {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                currentHeadingDeg = (azimuthDeg + 360f) % 360f
                onStateUpdate()
            }
            Sensor.TYPE_ACCELEROMETER -> {
                // Accelerometer peak detection fallback
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt(x * x + y * y + z * z)
                val now = System.currentTimeMillis()

                if (magnitude - lastAccelMagnitude > 2.4f && now - lastStepTime > 320L) {
                    lastStepTime = now
                    processStep()
                }
                lastAccelMagnitude = magnitude
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
